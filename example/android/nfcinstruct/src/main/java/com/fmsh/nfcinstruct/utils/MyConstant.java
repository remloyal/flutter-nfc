package com.fmsh.nfcinstruct.utils;

/**
 * @author wuyajiang
 * @date 2020/8/10
 */
public class MyConstant {
    /**
     * 版本号
     */
    public static final String VERSION_CODE = "1.0.0";

    public static final int UNKNOWN_ERROR = 0;

    public static final int CHIP_ERROR = 1;

    public static final int NFC_ERROR_CODE_TAG_LOST = 18731;
    public static final int NFC_ERROR_CODE_TAG_TRANSCEIVE_EXCEEDED_LENGTH = 18732;
    public static final int NFC_ERROR_CODE_TAG_TRANSCEIVE_FAIL = 18733;
    public static final int NFC_ERROR_CODE_TAG_IS_NULL = 18734;
    public static final int NFC_ERROR_CODE_TAG_IS_OUT_OF_DATE = 18735;
    public static final int NFC_ERROR_CODE_TAG_STOP_AUTH_PWD_ERROR = 18736;
    public static final int NFC_ERROR_CODE_TAG_READ_DATA_TIME_GO_NO_DATA_ERROR = 18737;
    public static final int NFC_ERROR_CODE_TAG_IN_RECORDING_CANNOT_START_ERROR = 18738;
    public static final int NFC_ERROR_CODE_TAG_STANDARD_MODE_CANNOT_LOG_GT_4864_ERROR = 18739;
    public static final int NFC_ERROR_CODE_TAG_NOT_SUPPORT_LARGE_MODE = 18740;

    // data_area_start_pointer index写入 1
    public static final String TAG_INDEX_1_CODE = "40B3B04800000001";
    // data_area_start_pointer index写入 0
    public static final String TAG_INDEX_0_CODE = "40B3B04800000000";


    // 回读start pointer
    public static final String TAG_INDEX_CHECK_CODE = "40B1B048000000";
    // 写入1后回读到的数据
    public static final String TAG_INDEX_1_CALLBACK_CODE = "01000800";
    // 写入0后回读到的数据
    public static final String TAG_INDEX_0_CALLBACK_CODE = "00000800";


    public static final String TAG_OP_MODE_CHECK_CODE = "40CF0100000000";
}
