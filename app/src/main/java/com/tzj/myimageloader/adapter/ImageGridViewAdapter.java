package com.tzj.myimageloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.tzj.myimageloader.R;
import com.tzj.myimageloader.util.ImageLoader;

import java.util.List;

/**
 * <p> ProjectName： MyImageLoader</p>
 * <p>
 * Description：图片显示适配器
 * </p>
 *
 * @author tangzhijie
 * @version 1.0
 * @CreateDate 2015/9/29
 */
public class ImageGridViewAdapter extends BaseAdapter {

    private Context context;

    /**
     * 图片的文件名List
     */
    private List<String> imageNameList;

    /**
     * 图片所在文件夹的路径
     */
    private String parentFilePath;

    private LayoutInflater inflater;

    public ImageGridViewAdapter(Context context, List<String> imageNameList, String parentFilePath) {
        this.context = context;
        this.imageNameList = imageNameList;
        this.parentFilePath = parentFilePath;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.iv = (ImageView) contentView.findViewById(R.id.iv);
            viewHolder.chk = (ImageButton) contentView.findViewById(R.id.chk);
            contentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) contentView.getTag();
        }
        //先设置默认的
        viewHolder.iv.setImageResource(R.mipmap.no_image);
        viewHolder.chk.setImageResource(R.mipmap.chk_photo_normal);
        //显示图片
        ImageLoader.getInstance().loadImage(
                parentFilePath + "/" + imageNameList.get(position), viewHolder.iv);
        return contentView;
    }

    private class ViewHolder {
        ImageView iv;
        ImageButton chk;
    }
}
