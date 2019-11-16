package io.github.feelfreelinux.hj2019

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.AccessibilityServiceInfo
import android.widget.Toast
import android.R
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T






class TypeViewTextChangedEventObject(
    var text: String?,
    var lastText: String?,
    var packageName: String?,
    var beginTimestamp: Long,
    var endTimestamp: Long?
)

class TypeViewTextSelectionChangedEventObject(
    var mText: String?,
    var mPackageName: String?,
    var mIsPassword: Boolean
)

class AnaylyzingService : AccessibilityService() {
    private val TAG = AccessibilityService::class.java.simpleName
    private var mTypeViewTextChangedEventObject: TypeViewTextChangedEventObject? = null
    private var mTypeViewTextSelectionChangedEventObject: TypeViewTextSelectionChangedEventObject? =
        null

    private fun onTypeViewSelected(accessibilityEvent: AccessibilityEvent) {
        val packageName = accessibilityEvent.packageName


        if (packageName != null && packageName != getPackageName()) {
            endTypeViewTextChangedEvent()
        }
    }

    private fun onTypeViewFocused(accessibilityEvent: AccessibilityEvent) {
        val packageName = accessibilityEvent.packageName


        if (packageName != null && packageName != getPackageName()) {
            endTypeViewTextChangedEvent()
        }
    }

    private fun onTypeViewTextSelectionChanged(
        accessibilityEvent: AccessibilityEvent,
        accessibilityNodeInfo: AccessibilityNodeInfo?
    ) {
        if (accessibilityNodeInfo != null) {
            try {
                accessibilityNodeInfo.recycle()
            } catch (e: Exception) {
                Log.e(TAG, e.message)
                Log.e(TAG, e.toString())

                e.printStackTrace()
            }

        }


        val packageName = accessibilityEvent.packageName


        if (packageName != null && packageName != getPackageName()) {
            Log.v(TAG, "GOT DATA, EVENT OBJECT")
            Log.v(TAG, accessibilityEvent.text.first().toString())


        }
    }

    private fun disable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                disableSelf()
            } catch (e: Exception) {
                Log.e(TAG, e.message)
                Log.e(TAG, e.toString())

                e.printStackTrace()
            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service create")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroy")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")

        try {
            Toast.makeText(
                this, " Accessibility service connected",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            Log.e(TAG, e.toString())

            e.printStackTrace()
        }

        var accessibilityServiceInfo: AccessibilityServiceInfo? = null
        try {
            accessibilityServiceInfo = serviceInfo
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            Log.e(TAG, e.toString())

            e.printStackTrace()
        }

        if (accessibilityServiceInfo != null) {
            accessibilityServiceInfo.eventTypes = (AccessibilityEvent.TYPE_VIEW_SELECTED
                    or AccessibilityEvent.TYPE_VIEW_FOCUSED
                    or AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                    or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    or AccessibilityEvent.TYPE_WINDOWS_CHANGED
                    or AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED)

            /*accessibilityServiceInfo.flags = AccessibilityServiceInfo.DEFAULT;*/

            accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK

            try {
                serviceInfo = accessibilityServiceInfo
            } catch (e: Exception) {
                disable()

                Log.e(TAG, e.message)
                Log.e(TAG, e.toString())

                e.printStackTrace()
            }

        } else {
            disable()
        }
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent?) {
        if (accessibilityEvent == null) {
            return
        }

        Log.i(TAG, "Accessibility event")

        val eventType = accessibilityEvent.eventType

        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_SELECTED -> {
                Log.i(TAG, "Accessibility event (event type): Type view selected")

                onTypeViewSelected(accessibilityEvent)
            }

            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                Log.i(TAG, "Accessibility event (event type): Type view focused")

                onTypeViewFocused(accessibilityEvent)
            }



            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED  -> {
                Log.v(TAG, "OKNO ZMIENIONE")
                // Log.v(TAG, accessibilityEvent.source.viewIdResourceName.toString())
                Log.v(TAG, rootInActiveWindow?.childCount?.toString() ?: "")
                searchForTextViews(rootInActiveWindow)


            }

            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                Log.i(TAG, "Accessibility event (event type): Type view text selection changed")

                var typeViewTextSelectionChangedNodeInfo: AccessibilityNodeInfo? = null
                try {
                    typeViewTextSelectionChangedNodeInfo = accessibilityEvent.source
                } catch (e: Exception) {
                    Log.e(TAG, e.message)
                    Log.e(TAG, e.toString())

                    e.printStackTrace()
                }

                onTypeViewTextSelectionChanged(
                    accessibilityEvent,
                    typeViewTextSelectionChangedNodeInfo
                )
            }
        }
    }

    fun getAllTextViews(rootNode: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        if (rootNode == null || rootNode.className == null) {
            return listOf()
        }
        return if (rootNode.childCount == 0) {
            listOf(rootNode)
        } else {
            val parsedTextViews = mutableListOf<AccessibilityNodeInfo>()
            for (i in 0 until rootNode!!.childCount - 1) {

                parsedTextViews.addAll( getAllTextViews(rootNode!!.getChild(i)))
            }

            parsedTextViews
        }
    }
    fun searchForTextViews(rootInActiveWindow2: AccessibilityNodeInfo?) {
//         Log.i(TAG, "searchPacket node: ${rootInActiveWindow2} childCount: ${rootInActiveWindow2?.childCount}   idName: ${rootInActiveWindow2?.getViewIdResourceName() ?: ""}")
        if (rootInActiveWindow2?.className?.contains("TextView") ?: false) {
            SuspiciousEventsDetector.textViewFound(rootInActiveWindow2?.viewIdResourceName ?: "", rootInActiveWindow2?.text.toString() ?: "", applicationContext)
            Log.v("OI", "Found textview ! " + " id lol" + rootInActiveWindow2?.viewIdResourceName ?: "" )
            Log.v("OI", "CONTAINS TEXT ? " + rootInActiveWindow2?.text ?: "oi")
        } else {
            for (i in 0 until (rootInActiveWindow2?.childCount ?: -1)) {
                searchForTextViews(rootInActiveWindow2?.getChild(i))
            }
        }
    }
    override fun onInterrupt() {
        Log.i(TAG, "Interrupt")
    }

    private fun endTypeViewTextChangedEvent() {
        val endTimestamp = System.currentTimeMillis()

        if (mTypeViewTextChangedEventObject != null || mTypeViewTextSelectionChangedEventObject != null) {
            Log.i(TAG, "Type view text changed event: End")

            if (mTypeViewTextChangedEventObject != null) {
                if (mTypeViewTextChangedEventObject?.text != null
                    && mTypeViewTextChangedEventObject?.lastText != null
                    && mTypeViewTextChangedEventObject?.packageName != null
                ) {
                    mTypeViewTextChangedEventObject?.endTimestamp = endTimestamp
                } else {
                    // mTypeViewTextChangedEventObject.deleteFromRealm()
                }

                mTypeViewTextChangedEventObject = null
            }

            if (mTypeViewTextSelectionChangedEventObject != null) {
                if (!(mTypeViewTextSelectionChangedEventObject!!.mText != null && mTypeViewTextSelectionChangedEventObject?.mPackageName != null)) {
                    // mTypeViewTextSelectionChangedEventObject.deleteFromRealm()
                }

                mTypeViewTextSelectionChangedEventObject = null
            }
        }
    }
}
