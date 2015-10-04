package com.tzj.myimageloader.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.tzj.myimageloader.R;
import com.tzj.myimageloader.adapter.FolderAdapter;
import com.tzj.myimageloader.bean.FolderBean;
import com.tzj.myimageloader.util.DensityUtil;

import java.util.List;

/**
 * <p> ProjectName： MyImageLoader</p>
 * <p>
 * Description：文件夹POP
 * </p>
 *
 * @author tangzhijie
 * @version 1.0
 * @CreateDate 2015/10/4
 */
public class FolderPopupWindow extends PopupWindow {

    private View contentView;
    private Context context;
    private List<FolderBean> folderBeanList;
    private ListView lv_folder;
    private FolderAdapter folderAdapter;
    private OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public FolderPopupWindow(Context context, List<FolderBean> folderBeanList) {
        this.context = context;
        this.folderBeanList = folderBeanList;
        contentView = LayoutInflater.from(context).inflate(R.layout.pop, null);
        setWidth(DensityUtil.getScreenWidth(context));
        setHeight((int) (DensityUtil.getScreenHeight(context) * 0.5));
        setContentView(contentView);
        setFocusable(true);
        setOutsideTouchable(true);
        setTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        initView();
        initData();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        lv_folder = (ListView) contentView.findViewById(R.id.lv_folder);
        lv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (onItemClick != null) {
                    onItemClick.onItemClick(folderBeanList.get(i));
                }
            }
        });
    }

    /**
     * 设置数据
     */
    private void initData() {
        folderAdapter = new FolderAdapter(context, folderBeanList);
        lv_folder.setAdapter(folderAdapter);
    }

    public interface OnItemClick {
        void onItemClick(FolderBean folderBean);
    }
}
