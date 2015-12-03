package com.duanlei.simplenet.core;

import android.os.Handler;
import android.os.Looper;

import com.duanlei.simplenet.base.Request;
import com.duanlei.simplenet.base.Response;

import java.util.concurrent.Executor;

/**
 * Author: duanlei
 * Date: 2015-12-03
 * 请求结果投递类， 将请求结果投递给UI 线程
 */
public class ResponseDelivery implements Executor {

    /**
     * 主线程的handler
     */
    Handler mResponseHandler = new Handler(Looper.getMainLooper());


    public void deliveryResponse(final Request<?> request, final Response response) {
        Runnable respRunnable = new Runnable() {
            @Override
            public void run() {
                request.deliveryResponse(response);
            }
        };

        execute(respRunnable);
    }

    @Override
    public void execute(Runnable command) {
        mResponseHandler.post(command);
    }
}
