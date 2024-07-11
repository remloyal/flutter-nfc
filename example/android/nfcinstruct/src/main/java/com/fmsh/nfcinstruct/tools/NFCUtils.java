package com.fmsh.nfcinstruct.tools;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.fmsh.nfcinstruct.callback.OnResultCallback;
import com.fmsh.nfcinstruct.utils.LogUtil;
import com.fmsh.nfcinstruct.utils.MyConstant;
import com.fmsh.nfcinstruct.utils.TransUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by wyj on 2018/7/6.
 */
public class NFCUtils {


    /**
     * 解析tag包含的芯片类型
     *
     * @param tag
     */
    public static void parseTag(Tag tag, NfcA nfcA, Bundle bundle, OnResultCallback callback) {
        if (tag == null) {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_IS_NULL, "this tag is null");
            return;
        }
        String[] techList = tag.getTechList();
        for (String taType : techList) {
            if (taType.endsWith("NfcA")) {
                startA(nfcA, bundle, callback);
                break;
            }
            if (taType.endsWith("NfcV")) {
                startV(tag, bundle, callback);
                break;
            }
        }
    }

    /**
     * 14443芯片指令发送接收方法
     *
     * @param nfcA
     */
    public static void startA(NfcA nfcA, Bundle bundle, OnResultCallback callback) {
        if (nfcA != null && nfcA.isConnected()) {
            Log.i("kiki", "startA nfcA.isConnected() = " + nfcA.isConnected());
            try {

                Log.i("kiki", "after nfcA.isConnected() = " + nfcA.isConnected());
                INfcA iNfcA = new INfcA(nfcA);
                int position = bundle.getInt("position");
                switch (position) {
                    case 0:
                        checkChipType(InstructMap.instructA(48), InstructMap.instructA(49), iNfcA);
                        measureBaseData(iNfcA, InstructMap.getBaseInstructList(true), callback);
                        break;
                    case 1:
                        //检测唤醒状态
                        checkWakeUp(iNfcA, InstructMap.getInstruct(true, 2), callback);
                        break;
                    case 2:
                        //休眠
                        singleInstruct(iNfcA, InstructMap.getInstruct(true, 24), callback);
                        break;
                    case 3:
                        //超高频初始化
                        singleInstruct(iNfcA, InstructMap.getInstruct(true, 25), callback);
                        break;
                    case 4:
                        //打开led
                        singleInstruct(iNfcA, InstructMap.getInstruct(true, 26), callback);
                        break;
                    case 5:
                        //关闭led
                        singleInstruct(iNfcA, InstructMap.getInstruct(true, 27), callback);
                        break;
                    case 6:
                        //查看测温状态
                        checkStatus(iNfcA, InstructMap.getInstruct(true, 9), callback);
                        break;
                    case 7:
                        //开启测温
                        startLogging(iNfcA, InstructMap.getParameterInstructList(true), bundle.getInt("loggingCount"), callback);
                        break;
                    case 8:
                        //停止测温
                        stopLogging(iNfcA, TransUtil.hexToByte(bundle.getString("pwd")), InstructMap.getStopInstruct(true), callback);
                        break;
                    case 9:
                        checkChipType(InstructMap.instructA(48), InstructMap.instructA(49), iNfcA);
                        //读取测温数据
                        boolean filed = bundle.getBoolean("filed");
                        getLoggingResult(filed, iNfcA, InstructMap.getResultInstruct(true), callback);
                        break;
                    case 10:
                        //通用指令发送接口
                        byte[] instructs = bundle.getByteArray("instruct");
                        String transceive = iNfcA.transceive(instructs);
//                        checkChipType(InstructMap.instructA(48), InstructMap.instructA(49), iNfcA);
//
////                        byte[] direct = hexStringToByteArray("40B1B040000000");
////                        String val = iNfcA.transceive(direct);
////                        Log.w("40B1B040000000 ",val);
//
//                        byte[] direct1 = hexStringToByteArray("40C00600000000");
//                        String val1 = iNfcA.transceive(direct1);
//                        Log.w("40C00600000000 ",val1);
//
//                        SystemClock.sleep(1500);
//                        byte[] direct2 = hexStringToByteArray("40C08600000000");
//                        String val2 = iNfcA.transceive(direct2);
//                        Log.w("40C08600000000 ",val2);
//                        float str1 = strFromat(val2);
//                        Log.w("40C08600000000 ", String.valueOf(str1));

                        callback.onResult(true, transceive);
                        break;
                    case 11:
                        //配置原始模式
                        setConfig(iNfcA, InstructMap.getConfig(true), callback);
                        break;
                    case 12:
                        //配置标准模式
                        int mode = bundle.getInt("mode");
                        configStandardMode(iNfcA, mode, InstructMap.getStandard(true), callback);
                        break;
                    case 13:
                        // 设置密码
                        String pwd = bundle.getString("pwd");
                        byte[] address = bundle.getByteArray("address");
                        settingPassword(iNfcA, TransUtil.hexToByte(pwd), address, InstructMap.instructA(54), callback);
                        break;
                    case 14:
                        //更新密码
                        String oldPwd = bundle.getString("oldPwd");
                        String newPwd = bundle.getString("newPwd");
                        updatePassword(iNfcA, TransUtil.hexToByte(oldPwd), TransUtil.hexToByte(newPwd), bundle.getByteArray("address"), InstructMap.getUpdatePassword(true), callback);
                        break;
                    case 15:
                        switchStorageMode(iNfcA, bundle.getInt("mode"), callback);
                        break;
                    case 16:
                        // 获取启动时间
                        getLoggingStartTime(iNfcA, InstructMap.getInstruct(true, 23), callback);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("kiki", "exception nfcA.isConnected() = " + nfcA.isConnected());
                try {
                    Log.i("kiki", "close nfcA");
                    nfcA.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                int errorCode = MyConstant.NFC_ERROR_CODE_TAG_TRANSCEIVE_FAIL;
                if (null != e && null != e.getMessage()) {
                    if (e instanceof TagLostException) {
                        errorCode = MyConstant.NFC_ERROR_CODE_TAG_LOST;
                    } else if (e.getMessage().equals("Transceive length exceeds supported maximum")) {
                        errorCode = MyConstant.NFC_ERROR_CODE_TAG_TRANSCEIVE_EXCEEDED_LENGTH;
                    } else if (e instanceof SecurityException) {
                        // Permission Denial: Tag is out of date
                        errorCode = MyConstant.NFC_ERROR_CODE_TAG_IS_OUT_OF_DATE;
                    }
                }
                callback.onFailed(errorCode, e.getMessage());
            } finally {

            }

        }
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] byteArray = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }

        return byteArray;
    }

    /**
     * 15693芯片指令发送接收方法
     *
     * @param tag
     */
    public static void startV(Tag tag, Bundle bundle, OnResultCallback callback) {
        NfcV nfcV = NfcV.get(tag);
        if (nfcV != null) {
            if (!nfcV.isConnected()) {
                try {
                    nfcV.connect();
                    INfcV iNfcV = new INfcV(nfcV);
                    int position = bundle.getInt("position");
                    switch (position) {
                        case 0:
                            checkChipType(InstructMap.instructV(48), InstructMap.instructV(49), iNfcV);
                            measureBaseData(iNfcV, InstructMap.getBaseInstructList(false), callback);
                            break;
                        case 1:
                            checkWakeUp(iNfcV, InstructMap.getInstruct(false, 2), callback);
                            break;
                        case 2:
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 24), callback);
                            break;
                        case 3:
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 25), callback);
                            break;
                        case 4:
                            //打开led
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 26), callback);
                            break;
                        case 5:
                            //关闭led
                            singleInstruct(iNfcV, InstructMap.getInstruct(false, 27), callback);
                            break;
                        case 6:
                            //查看测温状态
                            checkStatus(iNfcV, InstructMap.getInstruct(false, 9), callback);
                            break;
                        case 7:
                            //开启测温
                            startLogging(iNfcV, InstructMap.getParameterInstructList(false), bundle.getInt("loggingCount"), callback);
                            break;
                        case 8:
                            //停止测温
                            stopLogging(iNfcV, TransUtil.hexToByte(bundle.getString("pwd")), InstructMap.getStopInstruct(false), callback);
                            break;
                        case 9:
                            //读取测温数据
                            checkChipType(InstructMap.instructV(48), InstructMap.instructV(49), iNfcV);
                            boolean filed = bundle.getBoolean("filed");
                            getLoggingResult(filed, iNfcV, InstructMap.getResultInstruct(false), callback);
                            break;
                        case 10:
                            //通用指令发送接口
                            byte[] instructs = bundle.getByteArray("instruct");
                            String transceive = iNfcV.transceive(instructs);
                            callback.onResult(true, transceive);
                            break;
                        case 11:
                            //配置原始模式
                            setConfig(iNfcV, InstructMap.getConfig(false), callback);
                            break;
                        case 12:
                            //配置标准模式
                            int mode = bundle.getInt("mode");
                            configStandardMode(iNfcV, mode, InstructMap.getStandard(false), callback);
                            break;
                        case 13:
                            // 设置密码
                            String pwd = bundle.getString("pwd");
                            byte[] address = bundle.getByteArray("address");
                            settingPassword(iNfcV, TransUtil.hexToByte(pwd), address, InstructMap.instructV(54), callback);
                            break;
                        case 14:
                            //更新密码
                            String oldPwd = bundle.getString("oldPwd");
                            String newPwd = bundle.getString("newPwd");
                            updatePassword(iNfcV, TransUtil.hexToByte(oldPwd), TransUtil.hexToByte(newPwd), bundle.getByteArray("address"), InstructMap.getUpdatePassword(false), callback);
                            break;
                        case 15:
                            switchStorageMode(iNfcV, bundle.getInt("mode"), callback);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    int errorCode = MyConstant.NFC_ERROR_CODE_TAG_TRANSCEIVE_FAIL;
                    if (e instanceof TagLostException) {
                        errorCode = MyConstant.NFC_ERROR_CODE_TAG_LOST;
                    } else if (e.getMessage().equals("Transceive length exceeds supported maximum")) {
                        errorCode = MyConstant.NFC_ERROR_CODE_TAG_TRANSCEIVE_EXCEEDED_LENGTH;
                    }
                    callback.onFailed(errorCode, e.getMessage());
                } finally {
                    try {
                        nfcV.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    /**
     * 切换温度数据存储模式
     *
     * @param baseNfc
     * @param mode     0正常存储模式 1压缩存储模式
     * @param callback
     * @throws Exception
     */
    private static void switchStorageMode(BaseNfc baseNfc, int mode, OnResultCallback callback) throws Exception {

        if (baseNfc instanceof INfcV) {

            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xcf, (byte) 0x1d, (byte) 0x01, 0, 0});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }
            byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb1, (byte) 0x1d, (byte) 0xb0, 0x40, 0, 0});
            if (mode == 0) {
                sendCommand[1] = (byte) ((sendCommand[0] & (byte) 0xE3) | (byte) 0xC);
            } else {
                sendCommand[1] = (byte) ((sendCommand[0] & (byte) 0xE3) | (byte) 0x04);
            }
            String transceive = baseNfc.transceive(new byte[]{0x02, (byte) 0xb3, 0x1d, (byte) 0xb0, 0x40, 0x03, sendCommand[1], (byte) ~sendCommand[1], sendCommand[3], (byte) ~sendCommand[3]});
            if ("000000".equals(transceive)) {
                // 成功
                callback.onResult(true);
            } else {
                //失败
                callback.onResult(false, TransUtil.byteToHex(new byte[]{0x02, (byte) 0xb3, 0x1d, (byte) 0xb0, 0x40, 0x03, sendCommand[1], (byte) ~sendCommand[1], sendCommand[3], (byte) ~sendCommand[3]}), transceive);
            }
        } else {
            //测温流程中不能改变模式
            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xcf, (byte) 0x01, (byte) 0x00, 0x00, 0x00, 0x00});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }
            byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x40, 0, 0, 0});
            if (mode == 0) {
                sendCommand[0] = (byte) ((sendCommand[0] & (byte) 0xE3) | (byte) 0xC);
            } else {
                sendCommand[0] = (byte) ((sendCommand[0] & (byte) 0xE3) | (byte) 0x04);
            }
            String transceive = baseNfc.transceive(new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, 0x40, 0x03, 0, 0, sendCommand[0], (byte) ~sendCommand[0], sendCommand[2], (byte) ~sendCommand[2]});
            if ("000000".equals(transceive)) {
                // 成功
                callback.onResult(true);
            } else {
                //失败
                callback.onResult(false, TransUtil.byteToHex(new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, 0x40, 0x03, 0, 0, sendCommand[0], (byte) ~sendCommand[0], sendCommand[2], (byte) ~sendCommand[2]}), transceive);
            }
        }
    }


    /**
     * 获取场强,温度,电压
     *
     * @param baseNfc
     * @param callback
     * @throws IOException
     */
    private static void measureBaseData(BaseNfc baseNfc, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        // mode = 3 正常模式 4byte一个温度数据 mode=1 压缩模式 4byte 3个温度数据 mode =6 limit2模式 4byte 8个温度数据 mode=7 原始模式4byte2个温度数据
        int mode;
        if (baseNfc instanceof INfcV) {
            byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb1, (byte) 0x1d, (byte) 0xb0, 0x40, 0, 0});
            mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
        } else {
            byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x40, 0, 0, 0});
            mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
        }
        if (mode != 3) {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_NOT_SUPPORT_LARGE_MODE, "not support large mode");
            return;
        }

        String[] response = new String[3];
        //温度
        byte[] bytes = byteList.get(1);
        String byteToHex = baseNfc.transceive(bytes);
        SystemClock.sleep(400);
        if (byteToHex.contains("FAFF")) {
            double temp = singleTemp(baseNfc.transceive(byteList.get(2)).substring(2));
            response[1] = String.valueOf(temp);
        } else {
            callback.onResult(false);
            return;
        }

        //状态
        String transceiveStatus = baseNfc.transceive(InstructMap.getInstruct(true, 9));
        byte[] bytesFlag = TransUtil.hexToByte(transceiveStatus);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            //处于测温中
            response[0] = String.valueOf(true);
        } else {
            //非测温中
            response[0] = String.valueOf(false);
        }


        //电压
        String transceive = baseNfc.transceive(byteList.get(3));
        byte[] toByte = TransUtil.hexToByte(transceive);
        if ((toByte[2] & (byte) 0x01) == 1) {

            baseNfc.transceive(byteList.get(4));
            baseNfc.transceive(byteList.get(5));
            SystemClock.sleep(500);
            String voltage = baseNfc.transceive(byteList.get(6));
            baseNfc.transceive(byteList.get(7));
            voltage = voltage.substring(voltage.length() - 2) + voltage.substring(voltage.length() - 4, voltage.length() - 2);
            double i = Integer.parseInt(voltage, 16) / 8192.00 * 2.5;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            response[2] = decimalFormat.format(i);
        } else {
            response[2] = "0.00";
        }
        callback.onResult(true, response);

    }

    /**
     * 检测唤醒转态
     *
     * @param baseNfc
     * @param bytes
     * @param callback
     * @throws IOException
     */
    private static void checkWakeUp(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        String transceive = baseNfc.transceive(bytes);
        if ("005555".equals(transceive)) {
            //已唤醒状态
            callback.onResult(true);
        } else {
            //未唤醒状态
            callback.onResult(false);
        }

    }

    /**
     * 单条指令
     *
     * @param baseNfc
     * @param bytes
     * @param callback
     * @throws IOException
     */
    private static void singleInstruct(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        String transceive = baseNfc.transceive(bytes);
        if ("000000".equals(transceive)) {
            // 成功
            callback.onResult(true);
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(bytes), transceive);
        }
    }

    private static void setConfig(BaseNfc baseNfc, List<byte[]> byteList, OnResultCallback callback) throws IOException {
        if (baseNfc instanceof INfcV) {

            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xcf, (byte) 0x1d, (byte) 0x01, 0, 0});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }
        } else {
            //测温流程中不能改变模式
            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xcf, (byte) 0x01, (byte) 0x00, 0x00, 0x00, 0x00});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }
        }
        byte[] config = baseNfc.sendCommand(byteList.get(0));
        byte value = (byte) (config[config.length - 4] | 0x1C);
        byte[] send = byteList.get(1);
        if (send.length > 11) {
            send[send.length - 4] = value;
            send[send.length - 3] = (byte) ~value;
            send[send.length - 2] = config[config.length - 2];
            send[send.length - 1] = (byte) ~config[config.length - 2];

        } else {
            send[7] = value;
            send[8] = (byte) ~value;
            send[9] = config[config.length - 2];
            send[10] = (byte) ~config[config.length - 2];
        }
        String transceive = baseNfc.transceive(send);

        if ("000000".equals(transceive)) {
            // 成功
            callback.onResult(true);
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(send), transceive);
        }


    }

    /**
     * 查看测温状态
     *
     * @param baseNfc
     * @param bytes
     * @param callback
     * @throws IOException
     */
    private static void checkStatus(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        String transceive = baseNfc.transceive(bytes);
        byte[] bytesFlag = TransUtil.hexToByte(transceive);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            //处于测温中
            callback.onResult(true);
        } else {
            //非测温中
            callback.onResult(false);
        }
    }

    private static void getLoggingStartTime(BaseNfc baseNfc, byte[] bytes, OnResultCallback callback) throws Exception {
        Log.i("kiki", "--------- start getLoggingStartTime ");
        String startTimeHex = baseNfc.transceive(bytes);
        if (startTimeHex.length() == 8) {
            startTimeHex = "00" + startTimeHex;
        }
        String[] reponse = new String[1];

        reponse[0] = String.valueOf(Long.parseLong(startTimeHex.substring(2), 16));
        Log.i("kiki", "--------- end getLoggingStartTime ");
        callback.onResult(true, reponse);
    }


    /**
     * 开启测温
     *
     * @param baseNfc
     * @param byteList
     * @param loggingCount 测温次数
     * @param callback
     * @throws IOException
     */
    private static void startLogging(BaseNfc baseNfc, List<byte[]> byteList, int loggingCount, OnResultCallback callback) throws Exception {
        boolean indexSetOk = false;

        String transceive = baseNfc.transceive(byteList.get(0));
        byte[] bytesFlag = TransUtil.hexToByte(transceive);
        int flag = bytesFlag[2] & 0x10;
        if (flag == 16) {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_IN_RECORDING_CANNOT_START_ERROR, "Tag is running,no need start again");
        } else {
            // mode = 3 正常模式 4byte一个温度数据 mode=1 压缩模式 4byte 3个温度数据 mode =6 limit2模式 4byte 8个温度数据 mode=7 原始模式4byte2个温度数据
            int mode;
            if (baseNfc instanceof INfcV) {
                byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb1, (byte) 0x1d, (byte) 0xb0, 0x40, 0, 0});
                mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
            } else {
                byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x40, 0, 0, 0});
                mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
            }
            if (mode != 3) {
                callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_NOT_SUPPORT_LARGE_MODE, "not support large mode");
                return;
            }
            if (mode == 3 && loggingCount > 4864) {
                callback.onResult(false, "0");
                return;
            }
            if (mode == 7 && loggingCount > 9728) {
                callback.onResult(false, "0");
                return;
            }
            if (mode == 1 && loggingCount > 14592) {
                callback.onResult(false, "0");
                return;
            }
            // 判断出厂是否有用过,没用过index从0开始，用过，index就反转
            // 注意，出厂会有一条数据，但是时间为0，这算未用过
            // 这里默认用mode 3，如果用其他mode 再改代码
            String hexCount = baseNfc.transceive(byteList.get(14));
            int currentCount = 1 + Integer.parseInt(hexCount.substring(hexCount.length() - 6, hexCount.length() - 4) + hexCount.substring(hexCount.length() - 8, hexCount.length() - 6), 16);
            LogUtil.d("kiki", "上一单记录了多少数据：" + currentCount);
            if (currentCount <= 1) {
                LogUtil.d("kiki", "currentCount<=1，认为没用过，设置startindex=0");
                // 认为没用过,没用过就设置startindex =0
                baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_0_CODE));
                // 设置index 为0后，再检测一下
                if (baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_CHECK_CODE)).equals(MyConstant.TAG_INDEX_0_CALLBACK_CODE)) {
                    // 设置成功
                    indexSetOk = true;
                }
            } else {
                // 不在记录时，先检查原先是否有过数据
                // 先检查index
                String indexCheckResult = baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_CHECK_CODE));
                LogUtil.d("kiki", "indexCheckResult =" + indexCheckResult);
                // 如果index是0，那么就设置index 1，如果index是1，那么就设置index 0
                if (indexCheckResult.equals(MyConstant.TAG_INDEX_0_CALLBACK_CODE)) {
                    LogUtil.d("kiki", "start point index = 0,那我们设置为1");
                    baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_1_CODE));
                    // 设置index 为1后，再检测一下
                    if (baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_CHECK_CODE)).equals(MyConstant.TAG_INDEX_1_CALLBACK_CODE)) {
                        // 设置成功
                        indexSetOk = true;
                    }
                } else if (indexCheckResult.equals(MyConstant.TAG_INDEX_1_CALLBACK_CODE)) {
                    LogUtil.d("kiki", "start point index = 1,那我们设置为0");
                    baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_0_CODE));
                    // 设置index 为0后，再检测一下
                    if (baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_CHECK_CODE)).equals(MyConstant.TAG_INDEX_0_CALLBACK_CODE)) {
                        // 设置成功
                        indexSetOk = true;
                    }
                }
            }

            if (indexSetOk) {
                // 再做一次op check
                baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_OP_MODE_CHECK_CODE));
            } else {
                // 返回callback，提醒开启失败
                callback.onResult(false, "set start point index fail!");
                return;
            }

            baseNfc.transceive(byteList.get(1));
            String wakeStatus = baseNfc.transceive(byteList.get(2));
            long startTime = 0;
            if ("005555".equals(wakeStatus)) {
                for (int i = 3; i < 14; i++) {
                    byte[] bytes = byteList.get(i);
                    if (i == 12) {
                        // 我们想返回启动时间，这里自己控制启动指令
                        startTime = System.currentTimeMillis() / 1000;
                        String format = String.format("%08x", startTime);
                        byte[] timeByte = TransUtil.hexToByte(format);
                        bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x01, (byte) 0x40, 0x03, 0x00, 0x00, (byte) timeByte[0], (byte) timeByte[1], (byte) timeByte[2], (byte) timeByte[3]};
                    }
                    String data = baseNfc.transceive(bytes);
                    int parseInt = Integer.parseInt(data, 16);
                    if (parseInt != 0) {
                        callback.onResult(false, TransUtil.byteToHex(byteList.get(i)), data);
                        return;
                    }
                    if (i == 3 || i == 4) {
                        //查看延时时间配置
                        if (!check(baseNfc, byteList.get(i))) {
                            callback.onResult(false, TransUtil.byteToHex(byteList.get(i)), data);
                            return;
                        }
                    }
                }
                // 将启动时间传回去
                String[] reponse = new String[1];
                reponse[0] = String.valueOf(startTime);
                callback.onResult(true, reponse);
            } else {
                callback.onResult(false, "Wakeup fail");
            }
        }

    }

    /**
     * 检测延迟时间和时间间隔是否写入
     *
     * @param baseNfc
     * @param data
     * @return
     * @throws IOException
     */
    private static boolean check(BaseNfc baseNfc, byte[] data) throws IOException {
        if (data.length > 7) {
            byte[] readReg = baseNfc.readReg(new byte[]{data[data.length - 4], data[data.length - 3]});
            if (readReg[readReg.length - 1] == data[data.length - 2] && readReg[readReg.length - 2] == data[data.length - 1]) {
                return true;
            }
            for (int i = 0; i < 3; i++) {
                baseNfc.sendCommand(data);
            }
            readReg = baseNfc.readReg(new byte[]{data[data.length - 4], data[data.length - 3]});
            if (readReg[readReg.length - 1] == data[data.length - 2] && readReg[readReg.length - 2] == data[data.length - 1]) {
                return true;
            }
            return false;
        } else {
            byte[] readReg = baseNfc.readReg(new byte[]{data[2], data[3]});
            if (readReg[readReg.length - 1] == data[data.length - 3] && readReg[readReg.length - 2] == data[data.length - 2]) {
                return true;
            }
            for (int i = 0; i < 3; i++) {
                baseNfc.sendCommand(data);
            }
            readReg = baseNfc.readReg(new byte[]{data[2], data[3]});
            if (readReg[readReg.length - 1] == data[data.length - 3] && readReg[readReg.length - 2] == data[data.length - 2]) {
                return true;
            }
            return false;
        }

    }

    /**
     * 停止测温
     *
     * @param baseNfc
     * @param byteList
     * @param callback
     * @throws IOException
     */
    private static void stopLogging(BaseNfc baseNfc, byte[] pwd, List<byte[]> byteList, OnResultCallback callback) throws Exception {
//        baseNfc.transceive(new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, 0x0a, 0x00, 0x07});

        // mode = 3 正常模式 4byte一个温度数据 mode=1 压缩模式 4byte 3个温度数据 mode =6 limit2模式 4byte 8个温度数据 mode=7 原始模式4byte2个温度数据
        int mode;
        if (baseNfc instanceof INfcV) {
            byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xb1, (byte) 0x1d, (byte) 0xb0, 0x40, 0, 0});
            mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
        } else {
            byte[] sendCommand = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x40, 0, 0, 0});
            mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
        }
        if (mode != 3) {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_NOT_SUPPORT_LARGE_MODE, "not support large mode");
            return;
        }

        byte[] bytes1 = encryptionPwd(baseNfc, pwd, byteList);
        boolean authPwd = authPwd(baseNfc, bytes1, byteList);
        if (authPwd) {
            byte[] bytes = byteList.get(3);
            for (int i = 3; i >= 0; i--) {
                bytes[bytes.length - i - 1] = bytes1[i];
            }
            byte[] transceive1 = baseNfc.sendCommand(bytes);
            SystemClock.sleep(100);
            if ((transceive1[1] & 0x02) != 2) {
                callback.onResult(true);
            } else {
                callback.onResult(false);
            }
        } else {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_STOP_AUTH_PWD_ERROR, "stop password is wrong");
        }


    }

    /**
     * 获取测温数据和各种参数
     *
     * @param baseNfc
     * @param byteList
     * @param callback
     * @throws Exception
     */
    private static void getLoggingResult(boolean isFiled, BaseNfc baseNfc, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        //电池状态异常
        byte[] toByte = TransUtil.hexToByte(baseNfc.transceive(byteList.get(12)));
        if ((toByte[2] & (byte) 0x01) != 1) {
            callback.onResult(false, new String[]{"2"});
            return;
        }
        // 读取测温数据指令
        String sizeString = baseNfc.transceive(byteList.get(0));
        //存储的数据区域大小
        int size = Integer.parseInt(sizeString.substring(sizeString.length() - 2), 16) * 1024;

        //测温次数
        String strCount = baseNfc.transceive(byteList.get(1));
        int tpCount = Integer.parseInt(strCount.substring(strCount.length() - 6, strCount.length() - 4) + strCount.substring(strCount.length() - 8, strCount.length() - 6), 16);

        //测温间隔时间和测温延时时间
        String times = baseNfc.transceive(byteList.get(2));
        if (times.length() == 24) {
            times = "00" + times;
        }
        //延时时间
        String delayTime = String.valueOf(Integer.parseInt(times.substring(2, 6), 16));
        //测温间隔时间
        String intervalTime = String.valueOf(Integer.parseInt(times.substring(10, 14), 16));

        String startTimeHex = baseNfc.transceive(byteList.get(3));
        if (startTimeHex.length() == 8) {
            startTimeHex = "00" + startTimeHex;
        }
        String startTime = String.valueOf(Long.parseLong(startTimeHex.substring(2), 16));


        byte[] toByte1 = TransUtil.hexToByte(baseNfc.transceive(byteList.get(12)));
        int intFlag = toByte1[2] & 0x10;
        int allCount = tpCount;
        String hexCount = "";
        int currentCount = 0;
        // 测温状态
        String tempStatus = "0";
        //查看温度数据配置
        byte[] sendCommand = baseNfc.sendCommand(byteList.get(byteList.size() - 1));
        // mode = 3 正常模式 4byte一个温度数据 mode=1 压缩模式 4byte 3个温度数据 mode = 7 原始数据模式 一个block 2个温度数据
        int mode = (sendCommand[sendCommand.length - 4] >> 2) & (byte) 0x07;
        if (mode != 3) {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_NOT_SUPPORT_LARGE_MODE, "not support large mode");
            return;
        }
        //正在测量中
        if (intFlag == 16) {
            hexCount = baseNfc.transceive(byteList.get(13));
            tpCount = Integer.parseInt(hexCount.substring(4) + hexCount.substring(2, 4), 16);
            tempStatus = "1";
            currentCount = tpCount;

        } else {
            //测量停止
            hexCount = baseNfc.transceive(byteList.get(14));
            tpCount = Integer.parseInt(hexCount.substring(hexCount.length() - 6, hexCount.length() - 4) + hexCount.substring(hexCount.length() - 8, hexCount.length() - 6), 16);
            tempStatus = "3";
            int pointer = Integer.parseInt(hexCount.substring(hexCount.length() - 4, hexCount.length() - 2), 16);
            if (chipType) {
                if (pointer == 0) {
                    currentCount = (tpCount + 1) * 2 - 1;
                } else {
                    currentCount = (tpCount + 1) * 2;
                }
            } else {
                if (mode == 3) {
                    currentCount = tpCount + 1;
                } else {
                    currentCount = tpCount * 3 + pointer + 1;
                }
            }
        }
        LogUtil.d("kiki", "tpCount = " + tpCount);
        long currentTimeMillis = System.currentTimeMillis();
        long timeDifference = currentTimeMillis - Long.parseLong(startTime) * 1000;
        long valueTime = Integer.parseInt(delayTime) * 60 * 1000 + Integer.parseInt(intervalTime) * 1000;
        if (timeDifference > valueTime && currentCount == 0) {
            callback.onFailed(MyConstant.NFC_ERROR_CODE_TAG_READ_DATA_TIME_GO_NO_DATA_ERROR, "data records abnormal,no data found");
            return;
        }

        int startIndex = 0;
        String indexCheckResult = baseNfc.transceive(TransUtil.hexToByte(MyConstant.TAG_INDEX_CHECK_CODE));
        // 如果index是0，那么是从头开始记录的
        if (indexCheckResult.equals(MyConstant.TAG_INDEX_0_CALLBACK_CODE)) {
            startIndex = 0;
        } else if (indexCheckResult.equals(MyConstant.TAG_INDEX_1_CALLBACK_CODE)) {
            startIndex = 1;
        }
        LogUtil.d("kiki", "startIndex = " + startIndex);

        int readCount = currentCount + startIndex;

        int readSize;
        if (chipType) {
            readSize = readCount * 2;
        } else {
            if (mode == 3) {
                readSize = readCount * 4;
            } else {
                if (readCount % 3 == 0) {
                    readSize = readCount / 3 * 4;
                } else {
                    readSize = (readCount / 3 + 1) * 4;
                }
            }
        }

        String tempData = "";
        if (readSize > size) {

            tempData = readData(baseNfc, byteList, size);
        } else if (readSize > 0) {

            tempData = readData(baseNfc, byteList, readSize);
        }
        if (tempData.length() > readSize) {
            tempData = tempData.substring(0, readSize * 2 > tempData.length() ? tempData.length() : readSize * 2);
        }
        List<Float> list = new ArrayList<>();
        List<String> strList = new ArrayList<>();
        int tempSize = 8;
        int indexSize = 4;
        if (chipType) {
            tempSize = 4;
        } else {
            tempSize = 8;
            indexSize = 4;
        }

        // 8BE0 3800
        // 小端存储，31位：奇偶校验位，30~16：时间标记，15~12：相关标志flag，9~0:10bit温度数据
        for (int i = 0; i < tempData.length(); i += tempSize) {

            if (mode == 3 || chipType) {
                String data = tempData.substring(i, i + 4); // 这里取的就是8BE0
                String indexData = tempData.substring(i + 4, i + 4 + indexSize);// 这里取的就是3800
                float tp = NFCUtils.formatTemp(data);
                int index = NFCUtils.formatIndex(indexData);
                LogUtil.d("kiki", "index = " + index);
                list.add(tp);
                if (!chipType && isFiled) {
                    int filed = parseField(data);
                    strList.add(tp + ":" + filed);
                } else {
                    strList.add(String.valueOf(tp) + "~" + index);
                }
            } else {
                String data = tempData.substring(i, i + 8);
                formatCompressTemp(list, strList, data);
            }

        }
        if (mode == 1) {
            int size1 = list.size();
            for (int i = 0; i < size1 - currentCount; i++) {
                list.remove(list.size() - 1);
                strList.remove(strList.size() - 1);
            }
        }

        if (currentCount != allCount && !"1".equals(tempStatus)) {
            tempStatus = "2";
            //异常停止
            if (currentCount > 1) {
                currentCount--;
                list.remove(list.size() - 1);
                strList.remove(strList.size() - 1);
            }
        }
        if (list.size() == 0 && !"0".equals(delayTime)) {
            tempStatus = "0";
        }
//        overMaxLimit = 0;
//        overMinLimit = 0;
//
//        if (list.size() != 0) {
//            maxTep = Collections.max(list);
//            minTep = Collections.min(list);
//        }
//        for (int i = 0; i < list.size(); i++) {
//            if (list.get(i) > currentSettingMaxTemp) {
//                overMaxLimit++;
//            }
//            if (list.get(i) < currentSettingMinTemp) {
//                overMinLimit++;
//            }
//        }

        if (startIndex == 1 && strList.size() >= 1) {
            // 第一个点不需要
            strList.remove(0);
        }

        Object[] tempArr = strList.toArray();
        LogUtil.d("kiki", "共读出 strList条数据：" + strList.size());
        String[] reponse = new String[12 + tempArr.length];
        System.arraycopy(tempArr, 0, reponse, 12, tempArr.length);

        reponse[0] = String.valueOf(startIndex);
        reponse[1] = startTime;
        reponse[2] = String.valueOf(allCount);
        reponse[3] = String.valueOf(currentCount);
        reponse[4] = delayTime;
        reponse[5] = intervalTime;
//        reponse[6] = String.valueOf(minTep);
//        reponse[7] = String.valueOf(maxTep);
//        reponse[8] = String.valueOf(currentSettingMinTemp);
//        reponse[9] = String.valueOf(currentSettingMaxTemp);
//        reponse[10] = String.valueOf(overMinLimit);
//        reponse[11] = String.valueOf(overMaxLimit);
        callback.onResult(true, reponse);

    }


    /**
     * 读取温度数据
     *
     * @param baseNfc
     * @param byteList
     * @param size
     * @return
     * @throws Exception
     */
    private static String readData(BaseNfc baseNfc, List<byte[]> byteList, int size) throws Exception {
        int address = 4096;
        // 每次读取会多出 4byte,所以计算次数要加248+4
        int count = size / 252;
        int percent = size % 252;
        StringBuffer buffer = new StringBuffer();
        if (size < 252) {
            byte[] bytes = byteList.get(14);
            if (bytes.length > 7) {
                bytes[bytes.length - 4] = (byte) 0x10;
                bytes[bytes.length - 3] = (byte) 0x00;
                bytes[bytes.length - 1] = (byte) percent;
            } else {
                bytes[bytes.length - 5] = (byte) 0x10;
                bytes[bytes.length - 4] = (byte) 0x00;
                bytes[bytes.length - 2] = (byte) percent;
            }
            String transceive = baseNfc.transceive(bytes);
            if (bytes.length > 7) {

                buffer.append(transceive.substring(2));
            } else {
                buffer.append(transceive);
            }
        }

        for (int i = 0; i < count; i++) {
            if (i != 0) {
                address = address + 252;
            }
            byte[] bytes = byteList.get(14);
            if (bytes.length > 7) {
                bytes[bytes.length - 4] = (byte) ((address >> 8) & 0xff);
                bytes[bytes.length - 3] = (byte) (address & 0xff);
                bytes[bytes.length - 1] = (byte) 248;
            } else {
                bytes[bytes.length - 5] = (byte) ((address >> 8) & 0xff);
                bytes[bytes.length - 4] = (byte) (address & 0xff);
                bytes[bytes.length - 2] = (byte) 248;
            }
            String transceive = baseNfc.transceive(bytes);
            if ("0300".equals(transceive)) {
                break;
            }
            if (bytes.length > 7) {

                buffer.append(transceive.substring(2));
            } else {

                buffer.append(transceive);
            }
        }
        if (count != 0 && percent != 0) {
            address = address + 252;
            byte[] bytes = byteList.get(14);
            if (bytes.length > 7) {
                bytes[bytes.length - 4] = (byte) ((address >> 8) & 0xff);
                bytes[bytes.length - 3] = (byte) (address & 0xff);
                bytes[bytes.length - 1] = (byte) (percent - 4);
            } else {
                bytes[bytes.length - 5] = (byte) ((address >> 8) & 0xff);
                bytes[bytes.length - 4] = (byte) (address & 0xff);
                bytes[bytes.length - 2] = (byte) (percent - 4);
            }
            String transceive = baseNfc.transceive(bytes);
            if (bytes.length > 7) {
                buffer.append(transceive.substring(2));
            } else {
                buffer.append(transceive);
            }
        }
        return buffer.toString();
    }


    /**
     * 配置标准模式
     *
     * @param baseNfc
     * @param mode     0代表设置为2个小数点 1代表设置3个小数点
     * @param byteList
     * @param callback
     * @throws Exception
     */
    private static void configStandardMode(BaseNfc baseNfc, int mode, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        if (baseNfc instanceof INfcV) {

            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x02, (byte) 0xcf, (byte) 0x1d, (byte) 0x01, 0, 0});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }

        } else {
            //测温流程中不能改变模式
            byte[] bytesFlag = baseNfc.sendCommand(new byte[]{0x40, (byte) 0xcf, (byte) 0x01, (byte) 0x00, 0x00, 0x00, 0x00});
            int flag = bytesFlag[2] & 0x10;
            if (flag == 16) {
                callback.onResult(false);
                return;
            }

        }

        byte[] userCfg = baseNfc.sendCommand(byteList.get(0));
        byte cfg = userCfg[userCfg.length - 4];
        LogUtil.d("binary", cfg);
        int flag;
        if (mode == 0) {
            flag = cfg & 0x7f;
        } else {
            flag = cfg | 0x80;
        }
        if (userCfg.length == 5) {
            byte[] temp = new byte[4];
            System.arraycopy(userCfg, 1, temp, 0, 4);
            userCfg = temp;
        }
        userCfg[userCfg.length - 4] = (byte) flag;
        userCfg[userCfg.length - 3] = (byte) ~flag;
        userCfg[userCfg.length - 1] = (byte) ~userCfg[userCfg.length - 2];
        byte[] instruct = byteList.get(1);
        //        userCfg = new byte[]{0x4c, (byte) 0xb3,0x29, (byte) 0xd6};
        System.arraycopy(userCfg, 0, instruct, instruct.length - 4, 4);
        LogUtil.d(TransUtil.byteToHex(instruct));
        String transceive = baseNfc.transceive(instruct);
        if (transceive.contains("0000")) {
            callback.onResult(true);
        } else {
            //失败
            callback.onResult(false, TransUtil.byteToHex(instruct), transceive);
        }


    }


    private static float detA;
    private static float detB;
    private static float offset = 0.0F;
    private static boolean chipType = false;
    private static boolean standard = false;

    private static void checkChipType(byte[] bytes1, byte[] bytes2, BaseNfc baseNfc) throws Exception {
        byte[] userCfg = baseNfc.sendCommand(bytes1);
        byte cfg = userCfg[userCfg.length - 4];
        LogUtil.d("cfg", Byte.toString(cfg));
        int mode = (cfg >> 7) & 0x01;
        LogUtil.d("binary", mode);
        if (mode == 1) {
            // 三个小数点显示
            standard = false;
        } else {
            //两位小数点小数点显示
            standard = true;
        }
        int flag = (cfg >> 2) & 0x7;
        if (flag == 7) {
            //通过readmemery命令获取eepoom区中的vdeta，vdetb和offset
            chipType = true;
            String vet = baseNfc.transceive(bytes2);

            offset = Integer.parseInt(reverse(vet.substring(vet.length() - 12, vet.length() - 8)), 16);
            detA = Integer.parseInt(reverse(vet.substring(vet.length() - 8, vet.length() - 4)), 16);
            detB = Integer.parseInt(reverse(vet.substring(vet.length() - 4)), 16) - 65536;
            if (offset > 10000) {
                offset = offset - 65536;
            }
        } else {
            chipType = false;
        }
    }


    /**
     * 设置密码
     *
     * @param baseNfc
     * @param pwd      4字节密码
     * @param address  地址值
     * @param send
     * @param callback
     * @throws Exception
     */
    private static void settingPassword(BaseNfc baseNfc, byte[] pwd, byte[] address, byte[] send, OnResultCallback callback) throws Exception {
        for (int i = 3; i >= 0; i--) {
            send[send.length - i - 1] = pwd[i];
        }
        if (send.length < 18) {
            send[send.length - 9] = address[0];
            send[send.length - 8] = address[1];
        } else {
            send[send.length - 7] = address[0];
            send[send.length - 6] = address[1];
        }
        String transceive1 = baseNfc.transceive(send);
        //       baseNfc.transceive(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0x7C, 0, 0, 0});
        //        baseNfc.transceive(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0xBC, 0, 0, 0});
        //       baseNfc.transceive(new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0xFC, 0, 0, 0});
        if (transceive1.contains("0000")) {
            callback.onResult(true);
        } else {
            callback.onResult(false);
        }
    }

    /**
     * 更新密码
     *
     * @param baseNfc
     * @param oldPwd
     * @param newPwd
     * @param address
     * @param byteList
     * @param callback
     * @throws Exception
     */
    private static void updatePassword(BaseNfc baseNfc, byte[] oldPwd, byte[] newPwd, byte[] address, List<byte[]> byteList, OnResultCallback callback) throws Exception {
        byte[] encryptionPwd = encryptionPwd(baseNfc, oldPwd, byteList);
        if (address[1] != (byte) 0x30) {
            byte[] bytes = byteList.get(2);
            bytes[bytes.length - 5] = 0x03;
            byte[] bytes1 = byteList.get(3);
            byteList.add(bytes);
            byteList.add(bytes1);
        }
        boolean pwd = authPwd(baseNfc, encryptionPwd, byteList);
        if (pwd) {
            settingPassword(baseNfc, newPwd, address, byteList.get(3), callback);
        } else {
            callback.onResult(false);
        }

    }

    /**
     * 进行密码乱序操作
     *
     * @param baseNfc
     * @param pwd
     * @param byteList
     * @return
     * @throws Exception
     */
    private static byte[] encryptionPwd(BaseNfc baseNfc, byte[] pwd, List<byte[]> byteList) throws Exception {
        //首先获取随机数
        byte[] hexToByte = baseNfc.sendCommand(byteList.get(0));
        byte[] randomByte = new byte[4];
        randomByte[0] = hexToByte[2];
        randomByte[1] = hexToByte[4];
        randomByte[2] = hexToByte[1];
        randomByte[3] = hexToByte[3];
        String random = TransUtil.byteToHex(randomByte);
        random = TransUtil.hexStringToBinary(random);
        random = random.substring(random.length() - 3) + random.substring(0, random.length() - 3);
        String randomHex = TransUtil.binaryString2hexString(random);
        byte[] bytesRandom = TransUtil.hexStringToBytes(randomHex);
        //获取auth_rb_cfg
        byte[] bytes = baseNfc.sendCommand(byteList.get(1));
        for (int i = 0; i < bytesRandom.length; i++) {
            bytesRandom[i] = (byte) (bytesRandom[i] ^ bytes[bytes.length - 1]);
            pwd[i] = (byte) (pwd[i] ^ bytesRandom[i]);
        }
        return pwd;
    }

    /**
     * 密码认证
     *
     * @param baseNfc
     * @param pwd      乱序密码
     * @param byteList
     * @return
     * @throws Exception
     */
    private static boolean authPwd(BaseNfc baseNfc, byte[] pwd, List<byte[]> byteList) throws Exception {
        byte[] bytes = byteList.get(2);
        for (int i = 3; i >= 0; i--) {
            bytes[bytes.length - i - 1] = pwd[i];
        }
        byte[] transceive = baseNfc.sendCommand(bytes);
        SystemClock.sleep(100);
        if ((transceive[1] & (byte) 0x80) == (byte) 0x80) {
            return true;
        }
        return false;
    }


    private static String reverse(String s) {
        if (s.length() == 4) {
            String s1 = s.substring(0, 2);
            String s2 = s.substring(2, 4);
            return s2 + s1;
        }
        return s;
    }

    public static float calculate(String str) {
        int k = 0;
        String flag = Integer.toBinaryString(Integer.parseInt(str, 16));
        if (Integer.parseInt(flag.substring(0, 1)) != 0) {
            String temp = reverse(str);
            int j = Integer.parseInt(temp, 16);
            k = 0x1fff & j;
        }
        double t = (k / 8192.0) * (detA / 16.0) + detB / 16.0 + offset / 16.0;
        double u = k / 8192.0;
        float temp = new BigDecimal(t).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        return temp;
    }

    private static float strFromat(boolean standard, String str) {
        float resultTem = 0;
        LogUtil.d(str);
        String substring = str.substring(str.length() - 4, str.length() - 2);
        String substring1 = str.substring(str.length() - 2, str.length());
        String newstr = substring1 + substring;

        String stringToBinary = TransUtil.hexStringToBinary(newstr);
        String tempData = stringToBinary.substring(stringToBinary.length() - 10);

        String bStr = tempData.substring(1);
        String hexString = TransUtil.binaryString2hexString("0000000" + bStr);


        char[] chars = substring1.toCharArray();
        char nu = '2';
        float number = (float) 4.00;
        if (standard) {
            number = (float) 4.00;
        } else {
            number = (float) 8.00;
        }
        if (chars[1] >= nu) {

            int i = -((0xffff - Integer.parseInt(newstr, 16)) & 0x03ff) - 1;
            resultTem = (float) (i / number);
        } else {
            int a = Integer.parseInt(hexString, 16);
            resultTem = (float) (a / number);
        }
//        LogUtil.d(resultTem + "");
        return resultTem;
    }

    private static int parseField(String data) {
        byte[] bytes = TransUtil.hexToByte(data);
        int filed = (bytes[1] >> 5) & 0x1;
        LogUtil.d("filed", filed);
        return filed;
    }

    private static float strFromat(String str) {
        return strFromat(standard, str);
    }

    private static float formatTemp(String data) {
        if (chipType) {
            return calculate(data);
        }
        return strFromat(data);
    }

    // 8BE0 3800
    // 小端存储，31位：奇偶校验位，30~16：时间标记，15~12：相关标志flag，9~0:10bit温度数据
    // 解析出30~16位的时间标记
    private static int formatIndex(String data) {
        // 3800 : 0038 : 0     000 0000 0021 1000
        //            奇偶校验位        时间标记

        int index = -1;
        String littleStorageFormat = data.substring(2) + data.substring(0, 2);
        // 转成二进制 并 去掉第一位的校验位
        String binary = TransUtil.hexStringToBinary(littleStorageFormat).substring(1);
        // 二进制转10进制
        return TransUtil.binaryToAlgorism(binary);
    }

    private static void formatCompressTemp(List<Float> floatList, List<String> stringList, String data) {
        LogUtil.d(data);
//        data ="FBDF4FFFF1BBBFFE";
        String reversalData = TransUtil.reversalData(data);
        String toBinary = TransUtil.hexStringToBinary(reversalData);
        LogUtil.d(toBinary);
        for (int i = 0; i <= 20; i += 10) {
            float resultTem = 0;
            String tempData = toBinary.substring(toBinary.length() - 10 - i, toBinary.length() - i);
            String bStr = tempData.substring(1);

            String zHexS = TransUtil.binaryString2hexString("0000000" + bStr.substring(0, 9));
            int x = 0;
            char[] chars = tempData.toCharArray();
            char nu = '1';
            float number = (float) 4.00;
            if (standard) {
                number = (float) 4.00;
//                 zHexS = TransUtil.binaryString2hexString("0000000" + bStr.substring(0,9));
//                  x = Integer.parseInt(bStr.substring(7,8))*2+Integer.parseInt(bStr.substring(8,9));
            } else {
                number = (float) 8.00;
//                 zHexS = TransUtil.binaryString2hexString("0000000000" + bStr.substring(0,6));
//                 x = Integer.parseInt(bStr.substring(6,7))*4+Integer.parseInt(bStr.substring(7,8))*2+Integer.parseInt(bStr.substring(8,9));
            }
            if (chars[0] >= nu) {
                int b = -((0xffff - Integer.parseInt(zHexS, 16)) & 0x01ff) - 1;
//               int b = ~Integer.parseInt(zHexS, 16) +1;

                resultTem = (float) (b / number);
            } else {
                int a = Integer.parseInt(zHexS, 16);
                resultTem = (float) (a / number);
            }
            LogUtil.d(resultTem + "");
            floatList.add(resultTem);
            stringList.add(resultTem + "");

        }


    }

    /**
     * 计算单次体温数据
     *
     * @param data
     * @return
     */
    private static float singleTemp(String data) {
        return strFromat(data);
//        if (!chipType) {
//            return strFromat(data);
//        }
//        String flag = Integer.toBinaryString(Integer.parseInt(reverse(data), 16));
//        LogUtil.d(flag);
//        if (flag.length() < 10) {
//
//            return (float) (Integer.parseInt(flag, 2) / 8.0);
//
//        } else {
//            char[] chars = flag.substring(1).toCharArray();
//            for (int i = 0; i < chars.length; i++) {
//                char temp = 0;
//                if (chars[i] == '1') {
//                    temp = '0';
//                }
//                if (chars[i] == '0') {
//                    temp = '1';
//                }
//                chars[i] = temp;
//            }
//            int result = -(Integer.parseInt(new String(chars), 2) + 1);
//            return (float) (result / 8.0);

//        }


    }

}
