package com.lrm.callrec.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class CallRecord: AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {}

}