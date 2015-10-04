package com.tzj.myimageloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tzj.myimageloader.R;
import com.tzj.myimageloader.bean.FolderBean;
import com.tzj.myimageloader.util.ImageLoader;

import java.util.List;

/**
 * <p> ProjectName： MyImageLoader</p>
 * <p>
 * Description：文件夹列表适配器
 * </p>
 *
 * @author tangzhijie
 * @version 1.0
 * @CreateDate 2015/10/4
 */
public class FolderAdapter extends BaseAdapter {

    private Context context;
    private List<FolderBean> folderBeanList;
    private LayoutInflater inflater;

    public FolderAdapter(Context context, List<FolderBean> folderBeanList) {
        this.context = context;
        this.folderBeanList = folderBeanList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return folderBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return folderBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.item_folder, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.iv_folder_first_img = (ImageView) contentView.findViewById(R.id.iv_folder_first_img);
            viewHolder.tv_folder_name = (TextView) contentView.findViewById(R.id.tv_folder_name);
            viewHolder.tv_folder_count = (TextView) contentView.findViewById(R.id.tv_folder_count);
            contentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) contentView.getTag();
        }
        FolderBean folderBean = folderBeanList.get(position);
        //重置
        viewHolder.iv_folder_first_img.setImageResource(R.mipmap.no_image);

        ImageLoader.getInstance().loadImage(folderBean.getFirstImgPath(), viewHolder.iv_folder_first_img);
        viewHolder.tv_folder_name.setText(folderBean.getDirName());
        viewHolder.tv_folder_count.setText(folderBean.getCount() + "张");

        return contentView;
    }

    class ViewHolder {
        ImageView iv_folder_first_img;
        TextView tv_folder_name;
        TextView tv_folder_count;
    }
}
