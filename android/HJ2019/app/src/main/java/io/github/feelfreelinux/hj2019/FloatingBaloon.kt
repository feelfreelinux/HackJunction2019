package io.github.feelfreelinux.hj2019

import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.net.ConnectivityManager
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

class FloatingBalloon : Service(), FloatingViewListener {

    private val TAG = "ChatHeadService"

    val EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area"

    private val NOTIFICATION_ID = 9083150
    lateinit var iconText: TextView
    lateinit var floatingButton: View

    private val WIDTH_DP = 390
    private val HEIGHT_DP = 420

    private var mFloatingViewManager: FloatingViewManager? = null
    var connectionBroadcastReceiver = object : DisableHeadReceiver() {
        override fun broadcastResult(connected: Boolean) {
            stopSelf()
        }
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
        iconView.bubble_text.text = intent.getStringExtra("HINT")
        val layout = iconView.findViewById<FrameLayout>(R.id.bubble_layout)
        val animationView = iconView.findViewById<LottieAnimationView>(R.id.animation_view)

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