package ru.qrhandshake.qrpos.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class LoggingFilter extends OncePerRequestFilter {

    protected static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String REQUEST_PREFIX = "Request: ";
    private static final String RESPONSE_PREFIX = "Response: ";

    private final AtomicLong id = new AtomicLong(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            long requestId = id.incrementAndGet();
            request = new RequestWrapper(requestId, request);
            response = new ResponseWrapper(requestId, response);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (logger.isTraceEnabled()) {
                logRequest(request);
                logResponse(response);
            }
        }
    }

    private void logRequest(final HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        msg.append(REQUEST_PREFIX);
        if (request instanceof RequestWrapper) {
            msg.append("id=").append(((RequestWrapper) request).getId()).append("; ");
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            msg.append("session id=").append(session.getId()).append("; ");
        }
        if (request.getContentType() != null) {
            msg.append("content type=").append(request.getContentType()).append("; ");
        }
        if ( request.getRemoteAddr() != null ) {
            msg.append("Remote address=").append(request.getRemoteAddr()).append("; ");
        }
        msg.append("method=").append(request.getMethod()).append("; ");
        msg.append("uri=").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            msg.append('?').append(request.getQueryString());
        }

        if (request instanceof RequestWrapper && !isMultipart(request)) {
            RequestWrapper requestWrapper = (RequestWrapper) request;
            try {
                String charEncoding = Optional.ofNullable(requestWrapper.getCharacterEncoding()).orElse("UTF-8");
                String body = new String(requestWrapper.toByteArray(), charEncoding);
                msg.append("; payload=").append(body);
            } catch (UnsupportedEncodingException e) {
                logger.warn("Failed to parse request payload", e);
            }
        }

        if ( null != request.getParameterMap() && !request.getParameterMap().isEmpty() ) {
            msg.append("; params [");
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                msg.append(entry.getKey()).append("=").append(Arrays.toString(entry.getValue())).append(",");
            }
            msg.append("]");
        }
        if ( null != request.getHeaderNames() ) {
            msg.append("; headers [");
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                String value = request.getHeader(key);
                msg.append(key).append("=").append(value).append(",");
            }
            msg.append("]");
        }
        logger.trace(msg.toString());
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
    }

    private void logResponse(HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        msg.append(RESPONSE_PREFIX);
        if (response instanceof ResponseWrapper) {
            msg.append("request id=").append(((ResponseWrapper) response).getId());
            try {
                msg.append("; payload=").append(new String(((ResponseWrapper) response).toByteArray(), response.getCharacterEncoding()));
            } catch (UnsupportedEncodingException e) {
                logger.warn("Failed to parse response payload", e);
            }
        }
        logger.trace(msg.toString());
    }
}
