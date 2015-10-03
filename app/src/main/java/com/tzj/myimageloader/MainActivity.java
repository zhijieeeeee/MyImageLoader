package com.tzj.myimageloader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tzj.myimageloader.adapter.ImageGridViewAdapter;
import com.tzj.myimageloader.bean.FolderBean;
import com.tzj.myimageloader.util.CheckSd;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private GridView gv_photo;
    private RelativeLayout rl_bottom;
    private TextView tv_dir_name;
    private TextView tv_dir_count;

    private ImageGridViewAdapter imageGridViewAdapter;

    /**
     * 当前图片列表
     */
    private List<String> mImgList;

    /**
     * 当前的文件夹
     */
    private File mCurrentFile;

    /**
     * 当前文件夹下的图片数量
     */
    private int mCount;

    /**
     * 存放图片的文件夹List
     */
    private List<FolderBean> folderBeanList;

    /**
     * 加载框
     */
    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //当前文件夹下的文件名list
            mImgList.addAll(Arrays.asList(mCurrentFile.list()));
            imageGridViewAdapter = new ImageGridViewAdapter(MainActivity.this,
                    mImgList, mCurrentFile.getAbsolutePath());
            gv_photo.setAdapter(imageGridViewAdapter);
            tv_dir_name.setText(mCurrentFile.getName());
            tv_dir_count.setText(mCount + "张");
            progressDialog.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        gv_photo = (GridView) findViewById(R.id.gv_photo);
        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        tv_dir_name = (TextView) findViewById(R.id.tv_dir_name);
        tv_dir_count = (TextView) findViewById(R.id.tv_dir_count);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("正在加载...");
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mImgList = new ArrayList<>();
        folderBeanList = new ArrayList<>();
        //扫描图片
        scanImg();
    }

    /**
     * 扫描图片
     */
    private void scanImg() {
        //检测sd卡是否挂载
        if (!CheckSd.isSdEnable()) {
            Toast.makeText(MainActivity.this, "未检测到sd卡", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                //通过ContentProvide扫描图片
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(mImgUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "= ? or " +
                                MediaStore.Images.Media.MIME_TYPE + "= ? ",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                //放置已经扫描过的父文件夹的文件夹路径
                Set<String> parentFilePathSet = new HashSet<>();
                while (cursor.moveToNext()) {
                    //获得图片路径
                    String path = cursor.getString(
                            cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //获得图片的父文件夹
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    //获得父文件夹的路径
                    String parentFilePath = parentFile.getAbsolutePath();
                    FolderBean folderBean;
                    //如果该父路径已经被扫描过，执行下次循环
                    if (parentFilePathSet.contains(parentFilePath)) {
                        continue;
                    } else {
                        parentFilePathSet.add(parentFilePath);
                        folderBean = new FolderBean();
                        folderBean.setDirPath(parentFilePath);
                        folderBean.setFirstImgPath(path);
                    }
                    if (parentFile.list() == null) {
                        continue;
                    }
                    //获得父文件夹下的文件的数量
                    int fileCount = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String fileName) {
                            if (fileName.endsWith(".jpeg")
                                    || fileName.endsWith(".jpg")
                                    || fileName.endsWith(".png")) {
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    folderBean.setCount(fileCount);
                    folderBeanList.add(folderBean);
                    //设置图片最多的文件夹为当前显示的文件夹
                    if (fileCount > mCount) {
                        mCount = fileCount;
                        mCurrentFile = parentFile;
                    }
                }
                cursor.close();
                handler.sendEmptyMessage(111);
            }
        }.start();
    }
}
