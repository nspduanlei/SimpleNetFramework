package com.duanlei.simplenet.stack;

/**
 * Author: duanlei
 * Date: 2015-12-03
 * <p/>
 */
public final class HttpStackFactory {

    public static HttpStack createHttpStack() {

        return new HttpUrlConnStack();
    }

}
