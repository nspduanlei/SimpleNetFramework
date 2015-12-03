package com.duanlei.simplenet.core;

import android.util.Log;

import com.duanlei.simplenet.base.Request;
import com.duanlei.simplenet.base.Response;
import com.duanlei.simplenet.cache.Cache;
import com.duanlei.simplenet.cache.LruMemCache;
import com.duanlei.simplenet.stack.HttpStack;

import java.util.concurrent.BlockingQueue;

/**
 * Author: duanlei
 * Date: 2015-12-03
 * 网络请求Executor, 继承Thread, 从网络请求队列中循环读取请求并执行
 */
public class NetworkExecutor extends Thread {

    private static final String TAG = "NetworkExecutor";

    /**
     * 网络请求队列
     */
    private BlockingQueue<Request<?>> mRequestQueue;

    /**
     * 网络请求栈
     */
    private HttpStack mHttpStack;

    /**
     * 结果分发器，将结果投递到主线程
     */
    private static ResponseDelivery mResponseDelivery = new ResponseDelivery();

    /**
     * 请求缓存
     */
    private static Cache<String, Response> mReqCache = new LruMemCache();

    /**
     * 是否停止
     */
    private boolean isStop = false;

    public NetworkExecutor(BlockingQueue<Request<?>> queue, HttpStack httpStack) {
        mRequestQueue = queue;
        mHttpStack = httpStack;
    }

    @Override
    public void run() {
        try {
            while (!isStop) {
                final Request<?> request = mRequestQueue.take();

                if (request.isCanceled()) {
                    Log.d(TAG, "### 取消执行了");
                    continue;
                }

                Response response = null;
                if (isUseCache(request)) {
                    //从缓存中取
                    response = mReqCache.get(request.getUrl());
                } else {
                    //从网络上获取数据
                    response = mHttpStack.performRequest(request);
                    if (request.shouldCache() && isSuccess(response)) {
                        mReqCache.put(request.getUrl(), response);
                    }
                }

                mResponseDelivery.deliveryResponse(request, response);
            }
        } catch (InterruptedException e) {
            Log.i(TAG, "### 请求分发器退出");
        }
    }

    private boolean isSuccess(Response response) {
        return response != null && response.getStatusCode() == 200;
    }

    private boolean isUseCache(Request<?> request) {
        return request.shouldCache() && mReqCache.get(request.getUrl()) != null;
    }

    public void quit() {
        isStop = true;
        interrupt();
    }
}
