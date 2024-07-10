package com.example.nfcpkg

import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.TagTechnology
import io.flutter.plugin.common.MethodChannel.Result


data class MifareInfo(
    val type: Int,
    val size: Int,
    val blockSize: Int, // in bytes
    val blockCount: Int,
    val sectorCount: Int?, // might be null
) {

    val typeStr get() = run {
        when (sectorCount) {
            null -> {
                // Ultralight
                when (type) {
                    MifareUltralight.TYPE_ULTRALIGHT -> "ultralight"
                    MifareUltralight.TYPE_ULTRALIGHT_C -> "ultralight_c"
                    else -> "ultralight_unknown"
                }
            }
            else -> {
                // Classic
                when (type) {
                    MifareClassic.TYPE_CLASSIC -> "classic"
                    MifareClassic.TYPE_PLUS -> "plus"
                    MifareClassic.TYPE_PRO -> "pro"
                    else -> "classic_unknown"
                }
            }
        }
    }

    companion object {
        private fun ultralightPageCount(type: Int): Int {
            return when (type) {
                MifareUltralight.TYPE_ULTRALIGHT -> {
                    (0x0F + 1)
                }
                MifareUltralight.TYPE_ULTRALIGHT_C -> {
                    (0x2B + 1)
                }
                else -> {
                    -1 // unknown
                }
            }
        }
        private fun ultralightSize(type: Int): Int {
            return when (type) {
                MifareUltralight.TYPE_ULTRALIGHT, MifareUltralight.TYPE_ULTRALIGHT_C -> {
                    ultralightPageCount(type) * MifareUltralight.PAGE_SIZE
                }
                else -> {
                    -1 // unknown
                }
            }
        }

        fun fromUltralight(type: Int): MifareInfo {
            return MifareInfo(
                type,
                ultralightSize(type),
                MifareUltralight.PAGE_SIZE,
                ultralightPageCount(type),
                null
            )
        }
    }
}

/// These functions must not be called on UI thread
object MifareUtils {

    private val TAG = MifareUtils::class.java.name

    /// read one block (16 bytes)
    fun TagTechnology.readBlock(offset: Int, result: Result){
        when (this) {
            is MifareClassic -> {
                val data = readBlock(offset)
                result.success(data)
                return
            }
            is MifareUltralight -> {
                val data = readPages(offset)
                result.success(data)
                return
            }
            else -> {
                result.error("405", "Cannot invoke read on non-Mifare card", null)
                return
            }
        }
    }

    /// write one smallest unit (1 block for Classic, 1 page for Ultralight)
    fun TagTechnology.writeBlock(offset: Int, data: ByteArray, result: Result) {
        when (this) {
            is MifareClassic -> {
                writeBlock(offset, data)
                result.success("")
                return
            }
            is MifareUltralight -> {
                writePage(offset, data)
                result.success("")
                return
            }
            else -> {
                result.error("405", "Cannot invoke write on non-Mifare card", null)
                return
            }
        }
    }

    fun MifareClassic.readSector(index: Int): ByteArray {
        val begin = sectorToBlock(index)
        val end = begin + getBlockCountInSector(index)
        var data = ByteArray(0)
        for (i in begin until end) {
            data += readBlock(i)
        }
        return data
    }

}
