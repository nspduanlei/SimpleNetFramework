package com.duanlei.simplenet.stack;

import com.duanlei.simplenet.base.Request;
import com.duanlei.simplenet.base.Response;

/**
 * Author: duanlei
 * Date: 2015-12-03
 * 执行网络请求的接口
 */
public interface HttpStack {
    /**
     * 执行http请求
     * @param request
     * @return
     */
    public Response performRequest(Request<?> request);
}
