package com.example.nfcpkg

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.nfc.tech.TagTechnology
import android.os.Build

import com.example.nfcpkg.ByteUtils.canonicalizeData
import com.example.nfcpkg.ByteUtils.hexToBytes
import com.example.nfcpkg.ByteUtils.toHexString
import com.example.nfcpkg.MifareUtils.readBlock
import com.example.nfcpkg.MifareUtils.readSector
import com.example.nfcpkg.MifareUtils.writeBlock

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException
import java.lang.Exception
import java.util.*
import io.flutter.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer

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

//    private lateinit var nfcHandlerThread: HandlerThread
//    private lateinit var nfcHandler: Handler

    private lateinit var channel: MethodChannel
    private lateinit var adapter: NfcAdapter


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "nfcpkg")
        channel.setMethodCallHandler(this)
        adapter = NfcAdapter.getDefaultAdapter(flutterPluginBinding.applicationContext)
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
            
        } else {
            result.notImplemented()
        }
    }


    private fun pollTag(nfcAdapter: NfcAdapter, result: Result, timeout: Int, technologies: Int) {

//        pollingTimeoutTask = Timer().schedule(timeout.toLong()) {
//            try {
//                if (activity.get() != null) {
//
//                    nfcAdapter.disableReaderMode(activity.get())
//                }
//            } catch (ex: Exception) {
//                Log.w(TAG, "Cannot disable reader mode", ex)
//            }
//            result.error("408", "Polling tag timeout", null)
//        }

        val pollHandler = NfcAdapter.ReaderCallback { tag ->
//            pollingTimeoutTask?.cancel()

            // common fields
            val type: String
            val id = tag.id.toHexString()
            val standard: String
            // ISO 14443 Type A
            var atqa = ""
            var sak = ""
            // ISO 14443 Type B
            var protocolInfo = ""
            var applicationData = ""
            // ISO 7816
            var historicalBytes = ""
            var hiLayerResponse = ""
            // NFC-F / Felica
            var manufacturer = ""
            var systemCode = ""
            // NFC-V
            var dsfId = ""
            // NDEF
            var ndefAvailable = false
            var ndefWritable = false
            var ndefCanMakeReadOnly = false
            var ndefCapacity = 0
            var ndefType = ""

            if (tag.techList.contains(NfcA::class.java.name)) {
                val aTag = NfcA.get(tag)
                atqa = aTag.atqa.toHexString()
                sak = byteArrayOf(aTag.sak.toByte()).toHexString()
                tagTechnology = aTag
                when {
                    tag.techList.contains(IsoDep::class.java.name) -> {
                        standard = "ISO 14443-4 (Type A)"
                        type = "iso7816"
                        val isoDep = IsoDep.get(tag)
                        tagTechnology = isoDep
                        historicalBytes = isoDep.historicalBytes.toHexString()
                    }
                    tag.techList.contains(MifareClassic::class.java.name) -> {
                        standard = "ISO 14443-3 (Type A)"
                        type = "mifare_classic"
                        with(MifareClassic.get(tag)) {
                            tagTechnology = this
                            mifareInfo = MifareInfo(
                                this.type,
                                size,
                                MifareClassic.BLOCK_SIZE,
                                blockCount,
                                sectorCount
                            )
                        }
                    }
                    tag.techList.contains(MifareUltralight::class.java.name) -> {
                        standard = "ISO 14443-3 (Type A)"
                        type = "mifare_ultralight"
                        with(MifareUltralight.get(tag)) {
                            tagTechnology = this
                            mifareInfo = MifareInfo.fromUltralight(this.type)
                        }
                    }
                    else -> {
                        standard = "ISO 14443-3 (Type A)"
                        type = "unknown"
                    }
                }
            } else if (tag.techList.contains(NfcB::class.java.name)) {
                val bTag = NfcB.get(tag)
                protocolInfo = bTag.protocolInfo.toHexString()
                applicationData = bTag.applicationData.toHexString()
                if (tag.techList.contains(IsoDep::class.java.name)) {
                    type = "iso7816"
                    standard = "ISO 14443-4 (Type B)"
                    val isoDep = IsoDep.get(tag)
                    tagTechnology = isoDep
                    hiLayerResponse = isoDep.hiLayerResponse.toHexString()
                } else {
                    type = "unknown"
                    standard = "ISO 14443-3 (Type B)"
                    tagTechnology = bTag
                }
            } else if (tag.techList.contains(NfcF::class.java.name)) {
                standard = "ISO 18092 (FeliCa)"
                type = "iso18092"
                val fTag = NfcF.get(tag)
                manufacturer = fTag.manufacturer.toHexString()
                systemCode = fTag.systemCode.toHexString()
                tagTechnology = fTag
            } else if (tag.techList.contains(NfcV::class.java.name)) {
                standard = "ISO 15693"
                type = "iso15693"
                val vTag = NfcV.get(tag)
                dsfId = vTag.dsfId.toHexString()
                tagTechnology = vTag
            } else {
                type = "unknown"
                standard = "unknown"
            }

            // detect ndef
            if (tag.techList.contains(Ndef::class.java.name)) {
                val ndefTag = Ndef.get(tag)
                ndefTechnology = ndefTag
                ndefAvailable = true
                ndefType = ndefTag.type
                ndefWritable = ndefTag.isWritable
                ndefCanMakeReadOnly = ndefTag.canMakeReadOnly()
                ndefCapacity = ndefTag.maxSize
            }

            val jsonResult = JSONObject(mapOf(
                "type" to type,
                "id" to id,
                "standard" to standard,
                "atqa" to atqa,
                "sak" to sak,
                "historicalBytes" to historicalBytes,
                "protocolInfo" to protocolInfo,
                "applicationData" to applicationData,
                "hiLayerResponse" to hiLayerResponse,
                "manufacturer" to manufacturer,
                "systemCode" to systemCode,
                "dsfId" to dsfId,
                "ndefAvailable" to ndefAvailable,
                "ndefType" to ndefType,
                "ndefWritable" to ndefWritable,
                "ndefCanMakeReadOnly" to ndefCanMakeReadOnly,
                "ndefCapacity" to ndefCapacity,
            ))

            if (mifareInfo != null) {
                with(mifareInfo!!) {
                    jsonResult.put("mifareInfo", JSONObject(mapOf(
                        "type" to typeStr,
                        "size" to size,
                        "blockSize" to blockSize,
                        "blockCount" to blockCount,
                        "sectorCount" to sectorCount
                    )))
                }
            }

            result.success(jsonResult.toString())
        }

        nfcAdapter.enableReaderMode(activity, pollHandler, technologies, null)
    }
}
