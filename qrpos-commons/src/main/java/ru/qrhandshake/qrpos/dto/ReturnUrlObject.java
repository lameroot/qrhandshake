package ru.qrhandshake.qrpos.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lameroot on 20.06.16.
 */
public class ReturnUrlObject {

    private String url;
    private String action;
    private Map<String,String> params = new HashMap<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
