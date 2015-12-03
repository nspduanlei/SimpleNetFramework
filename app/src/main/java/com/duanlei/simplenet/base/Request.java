package com.duanlei.simplenet.base;

import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: duanlei
 * Date: 2015-12-03
 *
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    /**
     * http请求方法枚举
     */
    public static enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");
        private String mHttpMethod = "";
        private HttpMethod(String method) {
            mHttpMethod = method;
        }
    }

    /**
     * 优先级枚举
     */
    public static enum Priority {
        LOW,
        NORMAL,
        HIGN,
        IMMEDIATE
    }

    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    public final static String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * 请求序列号
     */
    protected int mSerialNum = 0;

    /**
     * 优先级默认设置为Normal
     */
    protected Priority mPriority = Priority.NORMAL;

    /**
     * 是否取消请求
     */
    protected boolean isCancel = false;

    /**
     * 请求是否应该缓存
     */
    private boolean mShouldCache = true;


    /**
     * 请求Listener
     */
    protected RequestListener<T> mRequestListener;

    /**
     * 请求url
     */
    private String mUrl = "";

    /**
     * 请求方法
     */
    HttpMethod mHttpMethod = HttpMethod.GET;

    /**
     * 请求header
     */
    private Map<String, String> mHeaders = new HashMap<>();

    /**
     * 请求参数
     */
    private Map<String, String> mBodyParams = new HashMap<>();


    public Request(HttpMethod method, String url, RequestListener<T> listener) {
        mHttpMethod = method;
        mUrl = url;
        mRequestListener = listener;
    }


    /**
     * 从原生的网络请求中解析结果，子类覆写
     * @param response
     * @return
     */
    public abstract T parseResponse(Response response);


    /**
     * 处理Response，该方法运行在ui线程
     * @param response
     */
    public final void deliveryResponse(Response response) {
        T result = parseResponse(response);
        if (mRequestListener != null) {
            int stCode = response != null ? response.getStatusCode() : -1;
            String msg = response != null ? response.getMessage() : "unkown error";
            mRequestListener.onComplete(stCode, result, msg);
        }
    }


    public String getUrl() {
        return mUrl;
    }

    public int getSerialNum() {
        return mSerialNum;
    }

    public void setSerialNum(int serialNum) {
        mSerialNum = serialNum;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }


    public HttpMethod getHttpMethod() {
        return mHttpMethod;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public Map<String, String> getParams() {
        return mBodyParams;
    }

    public boolean isHttps() {
        return mUrl.startsWith("https");
    }

    public void setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
    }

    public boolean shouldCache() {
        return mShouldCache;
    }


    public void cancel() {
        isCancel = true;
    }

    public boolean isCanceled() {
        return isCancel;
    }


    /**
     * 返回POST或者PUT请求时的Body参数字节数组
     * @return
     */
    public byte[] getBody() {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * 将参数转换成url编码参数串
     * @param params
     * @param paramsEncoding
     * @return
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();

        try {

            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }

            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }


    @Override
    public int compareTo(@NonNull Request<T> another) {
        Priority myPriority = this.getPriority();
        Priority anotherPriority = another.getPriority();

        //如果优先级相等，那么按照添加到队列的序列号顺序执行
        return myPriority.equals(anotherPriority) ? this.getSerialNum() - another.getSerialNum()
                :myPriority.ordinal() - anotherPriority.ordinal();
    }


    public static interface RequestListener<T> {
        public void onComplete(int stCode, T response, String errMsg);
    }
}
