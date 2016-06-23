package ru.qrhandshake.qrpos.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Logging all POST and GET requests
 */
public class PostLoggingInterceptor extends HandlerInterceptorAdapter{
    private static final Logger logger = LoggerFactory.getLogger(PostLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            String keyValue = key + " : " + value;
            sb.append("[").append(keyValue).append("], ");
        }
        if (sb.length() >=2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        logger.debug("Incoming request, url: [{}], params: [{}]", request.getRequestURL(), sb);
        return true;
    }
}

