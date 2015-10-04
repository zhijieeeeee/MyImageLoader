package com.tzj.myimageloader.bean;

/**
 * <p> ProjectName： MyImageLoader</p>
 * <p>
 * Description：存放图片的文件夹实体
 * </p>
 *
 * @author tangzhijie
 * @version 1.0
 * @CreateDate 2015/9/29
 */
public class FolderBean {

    /**
     * 文件夹路径
     */
    private String dirPath;

    /**
     * 文件夹名称
     */
    private String dirName;

    /**
     * 第一张图片的路径
     */
    private String firstImgPath;

    /**
     * 文件夹下图片的数量
     */
    private int count;

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
        //通过路径直接获取名称
        int lastIndexOf = this.dirPath.lastIndexOf("/") + 1;
        this.dirName = this.dirPath.substring(lastIndexOf);
    }

    public String getDirName() {
        return dirName;
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
