package com.duanlei.simplenet.requests;

import com.duanlei.simplenet.base.Request;
import com.duanlei.simplenet.base.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: duanlei
 * Date: 2015-12-03
 */
public class JsonRequest extends Request<JSONObject> {


    public JsonRequest(HttpMethod method, String url, RequestListener<JSONObject> listener) {
        super(method, url, listener);
    }

    @Override
    public JSONObject parseResponse(Response response) {
        String jsonString = new String(response.getRawData());

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
