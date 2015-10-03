package com.tzj.myimageloader.util;

import android.content.Context;
import android.os.Environment;

/**
 * <p> ProjectName： MyImageLoader</p>
 * <p>
 * Description：检查Sd卡是否挂载
 * </p>
 *
 * @author tangzhijie
 * @version 1.0
 * @CreateDate 2015/9/29
 */
public class CheckSd {

    /**
     * 检查Sd卡是否挂载
     *
     * @return
     */
    public static boolean isSdEnable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}
