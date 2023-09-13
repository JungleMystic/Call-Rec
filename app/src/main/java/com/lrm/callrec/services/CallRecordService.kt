package com.lrm.callrec.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.lrm.callrec.constants.TAG

class CallRecordService: AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.i(TAG, "accessibilityEvent ${event.eventType}")
            
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onInterrupt() {}

}