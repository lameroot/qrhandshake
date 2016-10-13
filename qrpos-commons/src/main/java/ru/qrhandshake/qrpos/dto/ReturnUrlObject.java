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

    public ReturnUrlObject setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getAction() {
        return action;
    }

    public ReturnUrlObject setAction(String action) {
        this.action = action;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public ReturnUrlObject setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReturnUrlObject{");
        sb.append("url='").append(url).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", params size=").append(params.size());
        sb.append('}');
        return sb.toString();
    }
}
