package io.github.feelfreelinux.hj2019

import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
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

class FloatingBalloon : Service(), FloatingViewListener {

    private val TAG = "ChatHeadService"

    val EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area"

    private val NOTIFICATION_ID = 9083150
    lateinit var iconText: TextView
    lateinit var floatingButton: View

    private var mFloatingViewManager: FloatingViewManager? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (mFloatingViewManager != null) {
            return START_STICKY
        }

        val metrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        val inflater = LayoutInflater.from(this)
        val iconView = inflater.inflate(R.layout.widget_balloon, null, false)

        val layout = iconView.findViewById<FrameLayout>(R.id.bubble_layout)
        val animationView = iconView.findViewById<LottieAnimationView>(R.id.animation_view)

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
        options.floatingViewWidth = (390 * metrics.density).toInt()
        options.floatingViewHeight = (150 * metrics.density).toInt()
        options.moveDirection = MOVE_DIRECTION_RIGHT
        options.overMargin = (16 * metrics.density).toInt()
        mFloatingViewManager!!.addViewToWindow(iconView, options)

        startForeground(NOTIFICATION_ID, createNotification(this))

        return START_REDELIVER_INTENT

    }


    override fun onDestroy() {
        destroy()
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