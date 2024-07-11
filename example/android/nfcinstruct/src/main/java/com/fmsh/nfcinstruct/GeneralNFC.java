package com.fmsh.nfcinstruct;

import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;

import com.fmsh.nfcinstruct.callback.OnResultCallback;
import com.fmsh.nfcinstruct.tools.CommandHandle;
import com.fmsh.nfcinstruct.tools.InstructMap;
import com.fmsh.nfcinstruct.tools.NFCUtils;
import com.fmsh.nfcinstruct.utils.MyConstant;
import com.fmsh.nfcinstruct.utils.TransUtil;

/**
 * @author wuyajiang
 * @date 2020/8/10
 */
public class GeneralNFC {
    private static GeneralNFC generalNFC;
    private Tag mTag;
    private NfcA nfcA;

    private GeneralNFC() {
    }

    public static GeneralNFC getInstance() {
        if (generalNFC == null) {
            synchronized (GeneralNFC.class) {
                if (generalNFC == null) {
                    generalNFC = new GeneralNFC();
                }
            }
        }
        return generalNFC;
    }


    /**
     * 外部传入TAG
     *
     * @param tag
     */
    public void setTag(Tag tag) {
        this.mTag = tag;
        this.nfcA = NfcA.get(tag);
        if (nfcA != null) {
            Log.i("kiki", "setTag nfcA.isConnected() = " + nfcA.isConnected());

            try {
                if (!nfcA.isConnected()) {
                    nfcA.connect();
                }
                Log.i("kiki", "nfc timeout = " + nfcA.getTimeout());

            } catch (Exception e) {
                try {
                    Log.i("kiki", "setTag nfcA close");
                    nfcA.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                e.printStackTrace();

            }
        }
    }

    /**
     * 获取tag对象
     *
     * @return
     */
    public Tag getTag() {
        return mTag;
    }

    public NfcA getNfc() {
        return this.nfcA;
    }

    /**
     * 获取当前SDK的版本号
     *
     * @return
     */
    public String getLibVersion() {
        return MyConstant.VERSION_CODE;
    }

    /**
     * 获取UID
     *
     * @return
     */
    public String getUid() {
        if (mTag != null) {
            byte[] id = mTag.getId();
            return TransUtil.byteToHex(id);
        }
        return null;
    }


    /**
     * 读取场强,温度,电压等数据
     *
     * @param callback
     */
    public void getBasicData(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 0);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 检测唤醒状态
     *
     * @param callback
     */
    public void checkWakeUp(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 1);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 休眠
     *
     * @param callback
     */
    public void doSleep(OnResultCallback callback) {

        Bundle bundle = new Bundle();
        bundle.putInt("position", 2);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 超高频初始化
     *
     * @param callback
     */
    public void initUHF(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 3);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * open led
     *
     * @param callback
     */
    public void turnOnLED(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 4);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * close led
     *
     * @param callback
     */
    public void turnOffLED(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 5);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 查看测温状态
     *
     * @param callback
     */
    public void checkStatus(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 6);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 开始定时测温
     *
     * @param callback
     */
    public void startLogging(int delayMinutes, int intervalSeconds, int loggingCount,
                             int minTemperature, int maxTemperature, int mode, OnResultCallback callback) {
        InstructMap.setParameter(delayMinutes, intervalSeconds, loggingCount, minTemperature, maxTemperature);
        Bundle bundle = new Bundle();
        bundle.putInt("position", 7);
        bundle.putInt("mode", mode);
        bundle.putInt("loggingCount", loggingCount);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 停止定时测温
     *
     * @param callback
     */
    public void stopLogging(String pwd, OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 8);
        bundle.putString("pwd", pwd);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }


    /**
     * 获取定时测温数据
     *
     * @param isFiled  true 代表存在场强数据,false 不存在场强值
     * @param callback
     */
    public void getLoggingResult(boolean isFiled, OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 9);
        bundle.putBoolean("filed", isFiled);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 通用指令发送接口
     *
     * @param data
     * @param callback
     */
    public void sendInstruct(byte[] data, OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 10);
        bundle.putByteArray("instruct", data);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 配置原始数据模式
     *
     * @param callback
     */
    public void configPrimitiveMode(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 11);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 配置标准数据模式
     *
     * @param mode     0代表2位小数格式数据,1代表3位小数格式数据
     * @param callback
     */
    public void configStandardMode(int mode, OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("mode", mode);
        bundle.putInt("position", 12);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 配置密码
     *
     * @param pwd      4字节密码
     * @param address  2字节 地址值
     * @param callback
     */
    public void settingPassword(String pwd, byte[] address, OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("address", address);
        bundle.putString("pwd", pwd);
        bundle.putInt("position", 13);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 更新密码
     *
     * @param oldPwd   旧密码
     * @param newPwd   新密码
     * @param address
     * @param callback
     */
    public void updatePassword(String oldPwd, String newPwd, byte[] address, OnResultCallback
            callback) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("address", address);
        bundle.putString("oldPwd", oldPwd);
        bundle.putString("newPwd", newPwd);
        bundle.putInt("position", 14);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    public void switchStorageMode(int mode, OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 15);
        bundle.putInt("mode", mode);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    /**
     * 查看标签启动时间
     *
     * @param callback
     */
    public void getLoggingStartTime(OnResultCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 16);
        NFCUtils.parseTag(mTag, nfcA, bundle, callback);
    }

    public byte[] readMemory(byte[] address, int length) {
        if (address == null || address.length != 2) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", 0);
        bundle.putByteArray("address", address);
        bundle.putInt("length", length);
        return CommandHandle.getInstance().parseTag(mTag, bundle);
    }

    public int writeMemory(byte[] address, byte[] data) {
        if (address == null || address.length != 2) {
            return 1;
        }
        if (data == null) {
            return 1;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", 1);
        bundle.putByteArray("address", address);
        bundle.putByteArray("data", data);
        byte[] bytes = CommandHandle.getInstance().parseTag(mTag, bundle);
        if (bytes == null) {
            return 1;
        }
        if ("000000".equals(TransUtil.byteToHex(bytes))) {
            return 0;
        }
        return 1;
    }

    public byte[] readReg(byte[] address) {
        if (address == null || address.length != 2) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", 2);
        bundle.putByteArray("address", address);
        return CommandHandle.getInstance().parseTag(mTag, bundle);
    }

    public int writeReg(byte[] address, byte[] data) {
        if (address == null || address.length != 2) {
            return 1;
        }
        if (data == null || data.length != 2) {
            return 1;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", 3);
        bundle.putByteArray("address", address);
        bundle.putByteArray("data", data);
        byte[] bytes = CommandHandle.getInstance().parseTag(mTag, bundle);
        if (bytes == null) {
            return 1;
        }
        if ("000000".equals(TransUtil.byteToHex(bytes))) {
            return 0;
        }
        return 1;
    }


    /**
     * @param password 密码必须是4字节长度
     * @param type     密码认证类型
     * @return
     */
    public int passwordAuth(byte[] password, byte type) {
        if (password == null || password.length != 4) {
            return 1;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", 4);
        bundle.putByteArray("password", password);
        bundle.putByte("type", type);
        byte[] bytes = CommandHandle.getInstance().parseTag(mTag, bundle);
        if (bytes == null) {
            return 1;
        }
        if ((bytes[1] & (byte) 0x80) == (byte) 0x80) {
            return 0;
        }
        return 1;

    }

    public int wakeUp() {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 5);
        byte[] bytes = CommandHandle.getInstance().parseTag(mTag, bundle);
        if (bytes == null) {
            return 1;
        }
        if ("000000".equals(TransUtil.byteToHex(bytes))) {
            return 0;
        } else {
            return 1;
        }
    }

}
