package com.hieu2409.MyBareApp

import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.rscja.deviceapi.RFIDWithUHF
import com.rscja.deviceapi.RFIDWithUHF.BankEnum
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.IUHF
import java.util.*

class C72RfidScannerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

    private val mReader: RFIDWithUHFUART? = try {
        RFIDWithUHFUART.getInstance()
    } catch (e: Exception) {
        Log.e("C72RfidScanner", "Failed to initialize RFIDWithUHFUART", e)
        null // Or handle the error appropriately, like throwing a runtime exception
    }

    private var mReaderStatus = false
    private val scannedTags = mutableListOf<String>()
    private var uhfInventoryStatus = false
    private var deviceName = ""

    init {
        reactContext.addLifecycleEventListener(this)
    }

    override fun getName(): String {
        return "C72RfidScanner"
    }

    override fun onHostDestroy() {
        UhfReaderPower(false).start()
    }

    override fun onHostResume() {}

    override fun onHostPause() {}

    private fun sendEvent(eventName: String, @Nullable array: WritableArray?) {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, array)
    }

    private fun sendEvent(eventName: String, @Nullable status: String?) {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, status)
    }

    @ReactMethod
    private fun initializeReader() {
        Log.d("UHF Reader", "Initializing Reader")
        UhfReaderPower().start()
    }

    @ReactMethod
    fun deInitializeReader() {
        Log.d("UHF Reader", "DeInitializing Reader")
        UhfReaderPower(false).start()
    }

    companion object {
        private const val UHF_READER_POWER_ON_ERROR = "UHF_READER_POWER_ON_ERROR"
        private const val UHF_READER_INIT_ERROR = "UHF_READER_INIT_ERROR"
        private const val UHF_READER_READ_ERROR = "UHF_READER_READ_ERROR"
        private const val UHF_READER_RELEASE_ERROR = "UHF_READER_RELEASE_ERROR"
        private const val UHF_READER_WRITE_ERROR = "UHF_READER_WRITE_ERROR"
        private const val UHF_READER_OTHER_ERROR = "UHF_READER_OTHER_ERROR"

        fun convertArrayToWritableArray(tag: Array<String>): WritableArray {
            val array = WritableNativeArray()
            for (tagId in tag) {
                array.pushString(tagId)
            }
            return array
        }
    }


    @ReactMethod
    fun readSingleTag(promise: Promise) {
        try {
            val tag = mReader?.inventorySingleTag()

            if (tag?.epc?.isNotEmpty() == true) {
                val tagData = arrayOf(tag.epc, tag.rssi)
                promise.resolve(convertArrayToWritableArray(tagData))
            } else {
                promise.reject(UHF_READER_READ_ERROR, "READ FAILED")
            }

        } catch (ex: Exception) {
            promise.reject(UHF_READER_READ_ERROR, ex)
        }
    }

    @ReactMethod
    fun startReadingTags(callback: Callback) {
        uhfInventoryStatus = mReader?.startInventoryTag() ?: false
        TagThread().start()
        callback.invoke(uhfInventoryStatus)
    }

    @ReactMethod
    fun stopReadingTags(callback: Callback) {
        uhfInventoryStatus = mReader?.stopInventory()?.not() ?: true
        callback.invoke(scannedTags.size)
    }

    @ReactMethod
    fun readPower(promise: Promise) {
        try {
            val uhfPower = mReader?.power ?: -1

            if (uhfPower >= 0) {
                promise.resolve(uhfPower)
            } else {
                promise.reject(UHF_READER_OTHER_ERROR, "INVALID POWER VALUE")
            }
            Log.d("UHF_SCANNER", uhfPower.toString())

        } catch (ex: Exception) {
            Log.d("UHF_SCANNER", ex.localizedMessage ?: "Unknown error")
            promise.reject(UHF_READER_OTHER_ERROR, ex.localizedMessage)
        }
    }


    @ReactMethod
    fun changePower(powerValue: Int, promise: Promise) {
        try {
            val uhfPowerState = mReader?.setPower(powerValue) ?: false
            if (uhfPowerState)
                promise.resolve(uhfPowerState)
            else
                promise.reject(UHF_READER_OTHER_ERROR, "Can't Change Power")
        } catch (ex: Exception) {
            Log.d("UHF_SCANNER", ex.localizedMessage ?: "Unknown error")
            promise.reject(UHF_READER_OTHER_ERROR, ex.localizedMessage)
        }
    }

    @ReactMethod
    fun writeDataIntoEpc(epc: String, promise: Promise) {
        if (epc.length == 6 * 4) {
            val extendedEpc = epc + "00000000"
            // Access Password, Bank Enum (EPC(1), TID(2),...), Pointer, Count, Data
            //Boolean uhfWriteState = mReader.writeData_Ex("00000000", BankEnum.valueOf("UII"), 2, 6, epc);
            val uhfWriteState = mReader?.writeData("00000000", IUHF.Bank_EPC, 2, 6, extendedEpc) ?: false

            if (uhfWriteState)
                promise.resolve(uhfWriteState)
            else
                promise.reject(UHF_READER_WRITE_ERROR, "Can't Write Data")
        } else {
            promise.reject(UHF_READER_WRITE_ERROR, "Invalid Data")
        }
    }

    @ReactMethod
    fun clearAllTags() {
        scannedTags.clear()
    }

    internal inner class UhfReaderPower(private val powerOn: Boolean = true) : Thread() {

        override fun run() {
            if (powerOn) {
                powerOn()
            } else {
                powerOff()
            }
        }

        private fun powerOn() {
            if (mReader == null || !mReaderStatus) {
                try {
                    mReaderStatus = mReader?.init() ?: false
                    //mReader.setEPCTIDMode(true);
                    mReader?.setEPCAndTIDMode()
                    sendEvent("UHF_POWER", "success: power on")
                } catch (ex: Exception) {
                    sendEvent("UHF_POWER", "failed: init error")
                }
            }
        }


        private fun powerOff() {
            if (mReader != null) {
                try {
                    mReader?.free()
                    sendEvent("UHF_POWER", "success: power off")

                } catch (ex: Exception) {
                    sendEvent("UHF_POWER", "failed: " + ex.message)
                }
            }
        }
    }

    @ReactMethod
    fun findTag(findEpc: String, callback: Callback) {
        uhfInventoryStatus = mReader?.startInventoryTag() ?: false
        TagThread(findEpc).start()
        callback.invoke(uhfInventoryStatus)
    }

    internal inner class TagThread(private val findEpc: String = "") : Thread() {
        override fun run() {
            var res: UHFTAGInfo? = null
            while (uhfInventoryStatus) {
                res = mReader?.readTagFromBuffer()
                if (res != null) {
                    if (findEpc.isEmpty())
                        addIfNotExists(res)
                    else
                        lostTagOnly(res)
                }
            }
        }

        private fun lostTagOnly(tag: UHFTAGInfo) {
            val epc = tag.epc //mReader.convertUiiToEPC(tag[1]);
            if (epc == findEpc) {
                // Same Tag Found
                //tag[1] = mReader.convertUiiToEPC(tag[1]);
                val tagData = arrayOf(tag.epc, tag.rssi)
                sendEvent("UHF_TAG", convertArrayToWritableArray(tagData))
            }
        }

        private fun addIfNotExists(tid: UHFTAGInfo) {
            if (!scannedTags.contains(tid.epc)) {
                scannedTags.add(tid.epc)
                val tagData = arrayOf(tid.epc, tid.rssi)
                sendEvent("UHF_TAG", convertArrayToWritableArray(tagData))
            }
        }
    }
}