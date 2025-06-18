package com.hieu2409.MyBareApp

import android.util.Log
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class CalendarModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    // Tên của module khi gọi từ JavaScript
    override fun getName() = "CalendarModule"

    // Hàm native để gọi từ JavaScript
    @ReactMethod
    fun createCalendarEvent(name: String, location: String) {
        Log.d("CalendarModule", "Create event called with name: $name and location: $location")
        // Ở đây bạn có thể thêm logic xử lý thực tế như tạo sự kiện với Calendar API
    }
}
