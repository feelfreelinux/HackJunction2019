package io.github.feelfreelinux.hj2019

import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager.MOVE_DIRECTION_RIGHT
import kotlinx.android.synthetic.main.widget_balloon.*
import kotlinx.android.synthetic.main.widget_balloon.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class DisableHeadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "KILL_IT")
            broadcastResult(true)
    }

    protected abstract fun broadcastResult(connected: Boolean)
}

data class ButtonData(val title: String, val callback: (Context) -> Unit)

class ScenarioData(
    val scenarioId: String,
    val hint: List<String>,
    val strict: Boolean,
    val buttonData: ButtonData? = null
)

class FloatingBalloon : Service(), FloatingViewListener {

    var SCENARIO_DATAS = listOf(
        ScenarioData(
            "fileDownload",
            listOf("\uD83D\uDE1F It's not safe to download files from the Internet."),
            false
        ),
        ScenarioData("googlePlay", listOf("This app might share your location \uD83D\uDE31 Discuss it with your parent!"), false),
        ScenarioData(
            "http",
            listOf(
                "\uD83D\uDCA1 This website is not safe. Avoid it!",
                "\uD83E\uDD28 This is an HTTP website. Try to avoid it. \uD83D\uDE4FHTTP is not as secure as HTTPS."
            ),
            false
        ),
        ScenarioData("infoPictures", listOf("Don't send your pictures to strangers on internet! \n\uD83D\uDE44\uD83D\uDE44\uD83D\uDE44"), true),
        ScenarioData(
            "mp3Download",
            listOf(
                "\uD83C\uDFB5 Use Apple Music or Spotify to listen to music \uD83D\uDE42. ",
                "\uD83D\uDE1F It is dangerous to download music from websites. Use the app."
            ),
            false,
            buttonData = ButtonData("Open Spotify") {
                val launchIntent = it.packageManager.getLaunchIntentForPackage("com.spotify.music")
                it.startActivity(launchIntent)
                stopSelf()
            }
        ),
        ScenarioData(
            "videoDownload",
            listOf("\uD83C\uDFAC Use Youtube to watch videos. It is not safe here \uD83D\uDE44."),
            false,
            buttonData = ButtonData("Open YouTube") {
                val launchIntent =
                    it.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                it.startActivity(launchIntent)
                stopSelf()
            }
        ),
        ScenarioData(
            "appDownload",
            listOf("Use Google Play to download apps. It is not safe here \uD83D\uDE44."),
            false,
            buttonData = ButtonData("Open Google Play") {
                val launchIntent =
                    it.packageManager.getLaunchIntentForPackage("com.android.vending")
                it.startActivity(launchIntent)
                stopSelf()
            }
        ),
        ScenarioData(
            "possibleThief",
            listOf(
                "\uD83D\uDE31 This person may be a thief. Talk to your parents \uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66!",
                "\uD83C\uDD98 This person can be a fraud. Contact your parents, please \uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66!"
            ),
            true,
            buttonData = ButtonData("Call mom") {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = (Uri.parse("tel:" + "123456789"))

                it.startActivity(intent)
                stopSelf()
            }),
        ScenarioData(
            "infoLocation",
            listOf(
                "\uD83D\uDCCD Be careful. Don't let strangers know where you or your parents are \uD83E\uDD2B",
                "⛔ Don't share your location on the Internet. It's unsafe! \uD83D\uDE14",
                "\uD83D\uDEAB Sharing location on the Internet is not safe! \uD83E\uDD2B"
            ), true
        ),
        ScenarioData(
            "infoAge",
            listOf(
                "❗ Be careful. Don't let strangers know your age or any other info about you \uD83E\uDD2D",
                "⛔ Don't share your age on the Internet. It's unsafe! \uD83D\uDE14"
            ), false
        ),
        ScenarioData(
            "bully",
            listOf(
                "\uD83D\uDC7F This person is aggressive. Do you know them? Consider blocking them ⛔",
                "\uD83D\uDCA9 Don't argue with the bully. Block this contact and call your parents \uD83D\uDCDE"
            ), true,
            buttonData = ButtonData("Call mom") {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = (Uri.parse("tel:" + "123456789"))

                it.startActivity(intent)
                stopSelf()
            })

    )

    private val TAG = "ChatHeadService"

    val EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area"

    private val NOTIFICATION_ID = 9083150
    lateinit var iconText: TextView
    lateinit var floatingButton: View

    private val WIDTH_DP = 390
    private val HEIGHT_DP = 390

    private var mFloatingViewManager: FloatingViewManager? = null
    var connectionBroadcastReceiver = object : DisableHeadReceiver() {
        override fun broadcastResult(connected: Boolean) {
            stopSelf()
        }
    }

    companion object {
        var scenariosMap = hashMapOf<String, Int>()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (mFloatingViewManager != null) {
            return START_STICKY
        }

        val metrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        val inflater = LayoutInflater.from(this)
        val iconView = inflater.inflate(R.layout.widget_balloon, null, false)

        var scenario = SCENARIO_DATAS.first {
            it.scenarioId == intent.getStringExtra("SCENARIO_ID")
        }

        iconView.setOnClickListener {
            scenario.buttonData?.callback?.invoke(applicationContext)
        }

        iconView.balloon_button.isVisible = scenario.buttonData != null
        iconView.balloon_button.text = scenario.buttonData?.title ?: ""

        if (!scenariosMap.containsKey(scenario.scenarioId)) {
            scenariosMap[scenario.scenarioId] = 0
        }
        iconView.bubble_text.text =
            scenario.hint[scenariosMap[scenario.scenarioId]!! % scenario.hint.size]
        scenariosMap[scenario.scenarioId] = scenariosMap[scenario.scenarioId]!! + 1


        val layout = iconView.findViewById<FrameLayout>(R.id.bubble_layout)
        val animationView = iconView.findViewById<LottieAnimationView>(R.id.animation_view)

        if (scenario.strict) {
            animationView.setAnimation(R.raw.balloon2v1)
        } else {
            animationView.setAnimation(R.raw.balloon1v1)

        }

        val widgetFrameLayout = iconView.widget_frame
        widgetFrameLayout.doOnLayout { view ->
            val yValue = view.y
            view.alpha = 0f
            view.y = -36 * metrics.density

            val alphaAnimator = ofFloat(0f, 1f)
            alphaAnimator.duration = 400
            alphaAnimator.interpolator = DecelerateInterpolator()
            alphaAnimator.addUpdateListener {
                view.alpha = it.animatedValue as Float
            }
            alphaAnimator.startDelay = 400
            alphaAnimator.start()

            val animator = ofFloat(-36 * metrics.density, yValue)
            animator.duration = 400
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                view.y = animator.animatedValue as Float
            }
            animator.startDelay = 400
            animator.start()
        }

        layout.doOnLayout { view ->
            val yValue = view.y
            val linearAnimator = ofFloat(0f, 1f)
            linearAnimator.duration = 3000
            linearAnimator.repeatCount = INFINITE
            linearAnimator.repeatMode = RESTART
            linearAnimator.interpolator = LinearInterpolator()
            linearAnimator.addUpdateListener {
                animationView.progress = it.animatedValue as Float
            }

            val bubbleAnimator = ofFloat(yValue, yValue - 10 * metrics.density)
            bubbleAnimator.duration = 1500
            bubbleAnimator.repeatCount = INFINITE
            bubbleAnimator.repeatMode = REVERSE
            bubbleAnimator.interpolator = AccelerateDecelerateInterpolator()
            bubbleAnimator.addUpdateListener {
                view.y = it.animatedValue as Float
            }
            linearAnimator.start()
            bubbleAnimator.start()
        }


        mFloatingViewManager = FloatingViewManager(this, this)

        val options = FloatingViewManager.Options()
        options.floatingViewWidth = (WIDTH_DP * metrics.density).toInt()
        options.floatingViewHeight = (HEIGHT_DP * metrics.density).toInt()
        options.moveDirection = MOVE_DIRECTION_RIGHT
        options.overMargin = (16 * metrics.density).toInt()
        mFloatingViewManager!!.addViewToWindow(iconView, options)

        startForeground(NOTIFICATION_ID, createNotification(this))

        return START_REDELIVER_INTENT

    }

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction("KILL_IT")
        this.registerReceiver(connectionBroadcastReceiver, intentFilter)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base))
    }


    override fun onDestroy() {
        destroy()
        unregisterReceiver(connectionBroadcastReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onFinishFloatingView() {
        stopSelf()
        Log.d(TAG, "finish deleted")
    }

    override fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int) {
        if (isFinishing) {
            Log.d(TAG, "deleted soon")
        } else {
            Log.d(TAG, "touch finished position: $x $y")
        }
    }

    private fun destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager!!.removeAllViewToWindow()
            mFloatingViewManager = null
        }
    }

    private fun createNotification(context: Context): Notification {
        val channelId = "ID_OF_CHANNEL"
        val channelName = "example name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(defaultChannel)
        }

        val builder = NotificationCompat.Builder(
            context,
            channelId
        )
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("notification title")
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_MIN
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)

        return builder.build()
    }
}