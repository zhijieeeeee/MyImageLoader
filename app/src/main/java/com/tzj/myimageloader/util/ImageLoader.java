package com.tzj.myimageloader.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * <p> ProjectName： MyImageLoader</p>
 * <p>
 * Description：图片加载工具类
 * </p>
 *
 * @author tangzhijie
 * @version 1.0
 * @CreateDate 2015/9/28
 */
public class ImageLoader {

    /**
     * Imageloader实例
     */
    private static ImageLoader mInstance;

    /**
     * 图片缓存
     */
    private LruCache<String, Bitmap> mLruCache;

    /**
     * 线程池,执行加载图片的任务
     */
    private ExecutorService mThreadPool;
    /**
     * 线程池的信号量
     */
    private Semaphore mThreadPoolSemaphore;

    /**
     * 默认的线程池中的线程数量
     */
    private static final int DEFAULT_THREAD_COUNT = 3;

    /**
     * 默认的队列调度方式
     */
    private Type mType = Type.LIFO;

    /**
     * 队列调度方式
     */
    public enum Type {
        LIFO, FIFO
    }

    /**
     * 任务队列，供线程池取任务
     */
    private LinkedList<Runnable> mTaskQueue;

    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    /**
     * 后台轮询线程绑定的Handler
     */
    private Handler mPoolThreadHandler;
    /**
     * 信号量,初始化0个
     */
    private Semaphore mPoolThreadHandlerSemaphore = new Semaphore(0);

    /**
     * UI线程Handler
     */
    private Handler mUIHandler;

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * 获得ImageLoader实例（单例模式）
     *
     * @return
     */
    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param threadCount 线程池的线程数
     * @param type        队列调度方法
     */
    private void init(int threadCount, Type type) {
        //初始化轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //根据调度方式从任务队列取出任务,由线程池执行
                        mThreadPool.execute(getTask());
                        try {
                            //如果信号量为0，会被阻塞住，在加载图片成功后释放
                            mThreadPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                //释放一个信号量，表示mPoolThreadHandler初始化完毕
                mPoolThreadHandlerSemaphore.release();
                //启动轮询
                Looper.loop();
            }
        };
        //开启轮询线程
        mPoolThread.start();
        //初始化LruCache
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        //初始化线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        //初始化任务队列
        mTaskQueue = new LinkedList<>();
        //队列调度方式
        mType = type;
        //线程池的信号量的个数为线程池中的线程个数
        mThreadPoolSemaphore = new Semaphore(threadCount);
    }

    /**
     * 核心方法，加载图片
     *
     * @param url       地址
     * @param imageView 控件
     */
    public void loadImage(final String url, final ImageView imageView) {
        imageView.setTag(url);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //显示图片
                    ImageBean imageBean = (ImageBean) msg.obj;
                    ImageView iv = imageBean.imageView;
                    String url = imageBean.url;
                    Bitmap bm = imageBean.bitmap;
                    //比对tag和url
                    if (iv.getTag().toString().equals(url)) {
                        iv.setImageBitmap(bm);
                    }
                }
            };
        }
        //从内存中获取图片
        Bitmap bm = getBitmapFromMemory(url);
        if (bm != null) {//内存中存在
            //发送给UI线程显示图片
            Message message = mUIHandler.obtainMessage();
            ImageBean imageBean = new ImageBean();
            imageBean.bitmap = bm;
            imageBean.url = url;
            imageBean.imageView = imageView;
            message.obj = imageBean;
            mUIHandler.sendMessage(message);
        } else {//内存中不存在，就去加载图片
            Runnable runnable = new Runnable() {
                public void run() {
                    //获取ImageView的宽高
                    ImageViewSize imageViewSize = getImageViewSize(imageView);
                    //根据ImageView的宽高对图片进行压缩
                    Bitmap bm = decodeBitmap(url, imageViewSize.width, imageViewSize.height);
                    //将Bitmap加入的LruCache缓存中
                    addToLruCache(url, bm);
                    //发送给UI线程显示图片
                    Message message = mUIHandler.obtainMessage();
                    ImageBean imageBean = new ImageBean();
                    imageBean.bitmap = bm;
                    imageBean.url = url;
                    imageBean.imageView = imageView;
                    message.obj = imageBean;
                    mUIHandler.sendMessage(message);
                    //释放信号量，表示一个任务完成，让线程池继续添加任务执行
                    mThreadPoolSemaphore.release();
                }
            };
            //将任务添加到任务队列中,并通知轮询线程取出并执行任务
            addTask(runnable);
        }
    }

    /**
     * 添加任务到消息队列，并通知轮询线程取出并执行任务
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        //添加任务到消息队列
        mTaskQueue.add(runnable);
        //确保mPoolThreadHandler在子线程中已经初始化完毕
        try {
            if (mPoolThreadHandler == null)
                //如果信号量是0会被阻塞，如果mPoolThreadHandler初始化完毕就会释放一个信号量
                mPoolThreadHandlerSemaphore.acquire();
        } catch (InterruptedException e) {
        }
        //通知轮询线程取出并执行任务
        mPoolThreadHandler.sendEmptyMessage(111);
    }

    /**
     * 根据调度方式从任务队列取出任务
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    /**
     * 从内存中获取图片
     *
     * @param key 地址
     * @return
     */
    private Bitmap getBitmapFromMemory(String key) {
        return mLruCache.get(key);
    }

    /**
     * 添加bitmap到缓存中
     *
     * @param url    地址
     * @param bitmap 图片
     */
    private void addToLruCache(String url, Bitmap bitmap) {
        if (getBitmapFromMemory(url) == null) {
            if (bitmap != null) {
                mLruCache.put(url, bitmap);
            }
        }
    }

    /**
     * 获取ImageView的宽高
     *
     * @param imageView
     * @return
     */
    @SuppressLint("NewApi")
    private ImageViewSize getImageViewSize(ImageView imageView) {
        ImageViewSize imageViewSize = new ImageViewSize();
        DisplayMetrics displayMetrics = imageView.getContext().
                getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        //获取宽度
        int width = imageView.getWidth();
        if (width <= 0) {
            //ImageView在layout中声明的宽度
            width = lp.width;
        }
        if (width <= 0) {
            //ImageView在layout中声明的最大宽度
            width = imageView.getMaxWidth();
        }
        if (width <= 0) {
            //屏幕宽度
            width = displayMetrics.widthPixels;
        }
        //获取高度
        int height = imageView.getHeight();
        if (height <= 0) {
            //ImageView在layout中声明的高度
            height = lp.height;
        }
        if (height <= 0) {
            //ImageView在layout中声明的最大高度
            height = imageView.getMaxHeight();
        }
        if (height <= 0) {
            //屏幕高度
            height = displayMetrics.heightPixels;
        }
        imageViewSize.width = width;
        imageViewSize.height = height;
        return imageViewSize;
    }

    /**
     * 根据ImageView的宽高对图片进行压缩
     *
     * @param url       图片地址
     * @param reqWidth  ImageView宽
     * @param reqHeight ImageView高
     * @return
     */
    private Bitmap decodeBitmap(String url, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //获取宽高，不加载到内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        int width = options.outWidth;
        int height = options.outHeight;
        //压缩比例
        int sampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRatio = Math.round(width * 1.0f / reqWidth);
            int heightRatio = Math.round(height * 1.0f / reqHeight);
            sampleSize = Math.min(widthRatio, heightRatio);
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(url, options);
        return bm;
    }

    /**
     * 图片的实体
     */
    private class ImageBean {
        String url;
        Bitmap bitmap;
        ImageView imageView;
    }

    /**
     * Imageview宽高的实体
     */
    private class ImageViewSize {
        int width;
        int height;
    }


}
