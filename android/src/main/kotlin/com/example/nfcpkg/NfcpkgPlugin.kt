package com.example.nfcpkg

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.TagTechnology
import android.os.Bundle
import com.example.nfcpkg.ByteUtils.canonicalizeData
import com.example.nfcpkg.ByteUtils.toHexString
import com.fmsh.nfcinstruct.GeneralNFC
import com.fmsh.nfcinstruct.callback.OnResultCallback
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.TimerTask

class NfcpkgPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private val TAG = "NfcpkgPlugin: "
    private lateinit var activity: Activity
    private var pollingTimeoutTask: TimerTask? = null
    private var tagTechnology: TagTechnology? = null
    private var ndefTechnology: Ndef? = null
    private var mifareInfo: MifareInfo? = null
    private var nfcAred: NfcA? = null

//    private lateinit var nfcHandlerThread: HandlerThread
//    private lateinit var nfcHandler: Handler

    private lateinit var channel: MethodChannel
    private lateinit var adapter: NfcAdapter

    private lateinit var mWorkerThreadHan: WorkerThreadHandler

    private var tagAll: Tag? = null
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "nfcpkg")
        channel.setMethodCallHandler(this)
        adapter = NfcAdapter.getDefaultAdapter(flutterPluginBinding.applicationContext)
        mWorkerThreadHan = WorkerThreadHandler()
//        tags = mutableMapOf()
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        // no op
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // no op
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "poll") {
            val timeout = call.argument<Int>("timeout")!!
            val technologies = call.argument<Int>("technologies")!!
            pollTag(adapter, result, timeout, technologies)
        } else if (call.method == "transceive") {
            val data = call.argument<String>("data")!!
//            40B1B040000000
//                    40C00600000000
//                    40C08400000000
//            val (bt1, _) = canonicalizeData("40B1B040000000")
//            val (bt2, _) = canonicalizeData("40C00600000000")
//            val (bt3, _) = canonicalizeData("40C08400000000")
            var tag = tagAll as Tag
////            val aTag = NfcA.get(tag)
////            val resp = nfcAred?.transceive(sendingBytes)
//            mWorkerThreadHan.handleMessage(10, bt1)
//            mWorkerThreadHan.handleMessage(10, bt2)
//            Thread.sleep(400)
//            mWorkerThreadHan.handleMessage(10, bt3)

            val aTag = NfcA.get(tag)
//            aTag.connect()
//            var data1 = aTag.transceive(bt1)
//            var data2 = aTag.transceive(bt2)
//            Thread.sleep(800)
//            var data3 = aTag.transceive(bt3)
//            System.out.println(data1.toHexString())
//            System.out.println(data2.toHexString())
//            System.out.println(data3.toHexString())
            val (bt, _) = canonicalizeData(data)
            var data1 = aTag.transceive(bt)
            result.success(data1.toHexString())
        } else {
            result.notImplemented()
        }
    }


    private fun pollTag(nfcAdapter: NfcAdapter, result: Result, timeout: Int, technologies: Int) {
//        mWorkerThreadHan.

        val pollHandler = NfcAdapter.ReaderCallback { tag ->
            tagAll = tag
            GeneralNFC.getInstance().setTag(tag)
            result.success(true)
        }
        val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_V
        val option = Bundle()
        // 延迟对卡片的检测
        option.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000)

        nfcAdapter.enableReaderMode(activity, pollHandler, READER_FLAGS, option)

    }


    private class WorkerThreadHandler() {
        private var mType = 0
        private val mOnResultCallback: OnResultCallback = object : OnResultCallback {
            override fun onResult(status: Boolean, vararg response: String) {
//                sendMessage(mType, status, response)
//                System.out.println(response[0])
                Log.w("NfcpkgPlugin: ", response[0])
            }

            override fun onFailed(errorCode: Int, errorMsg: String?) {
//                UIUtils.getHandler().sendEmptyMessage(-1)
            }
        }

        fun handleMessage(mType: Int, data: ByteArray) {
//            val aTag = NfcA.get(tags)
//            val tag = tagAll
//            GeneralNFC.getInstance().setTag(tag)
            when (mType) {
                0 -> GeneralNFC.getInstance().getBasicData(mOnResultCallback)
//                1 -> GeneralNFC.getInstance().checkWakeUp(mOnResultCallback)
//                2 -> GeneralNFC.getInstance().doSleep(mOnResultCallback)
//                3 -> GeneralNFC.getInstance().initUHF(mOnResultCallback)
//                4 -> GeneralNFC.getInstance().turnOnLED(mOnResultCallback)
//                5 -> GeneralNFC.getInstance().turnOffLED(mOnResultCallback)
//                6 -> GeneralNFC.getInstance().checkStatus(mOnResultCallback)
//                    7 -> GeneralNFC.getInstance().startLogging(
//                        SpUtils.getIntValue(MyConstant.delayTime, 0),
//                        SpUtils.getIntValue(MyConstant.intervalTime, 1),
//                        SpUtils.getIntValue(MyConstant.tpCount, 10),
//                        SpUtils.getIntValue(MyConstant.min_limit0, 0),
//                        SpUtils.getIntValue(MyConstant.max_limit0, 20),
//                        SpUtils.getIntValue(MyConstant.tpMode, 0), mOnResultCallback
//                    )

//                8 -> {
////                    val msgData: Bundle = msg.getData()
//                    GeneralNFC.getInstance()
//                        .stopLogging(msgData.getString("pwd"), mOnResultCallback)
//                }
//
//                9 -> {
//                    val data2: Bundle = msg.getData()
//                    val filed = data2.getBoolean("filed")
//                    GeneralNFC.getInstance().getLoggingResult(true, mOnResultCallback)
//                }

                10 -> {
//                        val data: Bundle = msg.getData()
//                        val dataString = data.getString("data")
                    try {
                        GeneralNFC.getInstance().sendInstruct(
                            data,
                            mOnResultCallback
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
//                                UIUtils.getHandler().sendEmptyMessage(-1)
                    }
                }

//                11 -> GeneralNFC.getInstance().configPrimitiveMode(mOnResultCallback)
//                12 -> {
//                    val data1: Bundle = msg.getData()
//                    val mode = data1.getInt("mode")
//                    GeneralNFC.getInstance().configStandardMode(mode, mOnResultCallback)
//                }
//
//                13 -> {
//                    val bundle: Bundle = msg.getData()
//                    val pwd = bundle.getString("pwd")
//                    val address = bundle.getByteArray("address")
//                    GeneralNFC.getInstance()
//                        .settingPassword(pwd, address, mOnResultCallback)
//                }
//
//                14 -> {
//                    val bundle1: Bundle = msg.getData()
//                    GeneralNFC.getInstance().updatePassword(
//                        bundle1.getString("oldPwd"),
//                        bundle1.getString("newPwd"),
//                        bundle1.getByteArray("address"),
//                        mOnResultCallback
//                    )
//                }
//
//                15 -> {
//                    val bundleData: Bundle = msg.getData()
//                    GeneralNFC.getInstance().switchStorageMode(
//                        bundleData.getInt("mode"),
////                                setDataToBundle(),
//                        mOnResultCallback
//                    )
//                }

                else -> {}
            }


        }

        private fun sendMessage(what: Int, status: Boolean, data: Array<String>) {
//            val bundle = Bundle()
//            bundle.putBoolean("status", status)
//            bundle.putStringArray("data", data)
//            val message = Message()
//            message.what = what
//            message.obj = bundle
//            if (UIUtils.getHandler() != null) {
//                UIUtils.getHandler().sendMessage(message)
//            }
        }


    }
//
}
