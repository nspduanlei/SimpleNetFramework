package com.duanlei.simplenet.core;

import android.util.Log;

import com.duanlei.simplenet.base.Request;
import com.duanlei.simplenet.stack.HttpStack;
import com.duanlei.simplenet.stack.HttpStackFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: duanlei
 * Date: 2015-12-03
 * 请求队列，使用优先队列，使得请求可以按照优先级进行处理
 */
public class RequestQueue {

    private static final String TAG = "RequestQueue";

    /**
     * 请求队列
     */
    private BlockingQueue<Request<?>> mRequestQueue = new PriorityBlockingQueue<>();

    /**
     * 请求的序列化生成器
     */
    private AtomicInteger mSerialNumGenerator = new AtomicInteger(0);

    /**
     * 默认核心数
     */
    public static int DEFAULT_CORE_NUMS = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * CPU核心数 + 1 个分发线程数
     */
    private int mDispatcherNums = DEFAULT_CORE_NUMS;

    /**
     * 执行网络请求的线程
     */
    private NetworkExecutor[] mDispatchers = null;

    /**
     * http请求的真正执行者
     */
    private HttpStack mHttpStack;

    /**
     *
     * @param coreNums  线程核心数
     * @param httpStack
     */
    protected RequestQueue(int coreNums, HttpStack httpStack) {
        mDispatcherNums = coreNums;
        mHttpStack = httpStack != null ? httpStack : HttpStackFactory.createHttpStack();
    }

    /**
     * 启动NetworkExecutor
     */
    private void startNetworkExecutors() {
        mDispatchers = new NetworkExecutor[mDispatcherNums];
        for (int i = 0; i < mDispatcherNums; i++) {
            mDispatchers[i] = new NetworkExecutor(mRequestQueue, mHttpStack);
            mDispatchers[i].start();
        }
    }

    public void start() {
        stop();
        startNetworkExecutors();
    }

    /**
     * 停止NetworkExecutor
     */
    public void stop() {
        if (mDispatchers != null && mDispatchers.length > 0) {
            for (int i = 0; i < mDispatchers.length; i ++) {
                mDispatchers[i].quit();
            }
        }
    }

    /**
     * 不能重复添加请求
     * @param request
     */
    public void addRequest(Request<?> request) {
        if (!mRequestQueue.contains(request)) {
            request.setSerialNum(this.generateSerialNumber());
            mRequestQueue.add(request);
        } else {
            Log.d(TAG, "###请求队列中已经含有");
        }
    }

    public void clear() {
        mRequestQueue.clear();
    }

    public BlockingQueue<Request<?>> getAllRequests() {
        return mRequestQueue;
    }


    /**
     * 为每个请求生成一个序列号
     * @return
     */
    private int generateSerialNumber() {
        return mSerialNumGenerator.incrementAndGet();
    }

}
