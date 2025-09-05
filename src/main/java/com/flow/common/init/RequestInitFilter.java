package com.flow.common.init;

import com.flow.common.ApplicationContextTool;
import com.flow.tool.MapTool;
import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;

@WebFilter(urlPatterns = "/*", filterName = "requestInitFilter")
public class RequestInitFilter implements Filter {

    private static final List<String> IGNORE_API_LOG = List.of("");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        RequestInitService requestInitService = ApplicationContextTool.getBean(RequestInitService.class);
        requestInitService.init((HttpServletRequest) servletRequest);
        ContentCachingRequestWrapper httpServletRequest = new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
        ContentCachingResponseWrapper httpServletResponse = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
        byte[] requestByte = httpServletRequest.getContentAsByteArray();
        try {
            String requestBody;
            if (requestByte.length == 0) {
                requestBody = new Gson().toJson(httpServletRequest.getParameterMap());
            }else {
                requestBody = Utf8.decode(requestByte);
            }
            String requestPath = httpServletRequest.getRequestURI();
            if (!IGNORE_API_LOG.contains(requestPath)){
                long endTime = System.currentTimeMillis();
                System.out.println("RequestInitFilter-Filter-log: " + MapTool.Map()
                        .put("uid", requestInitService._uid() == null ? 0 : requestInitService._uid())
                        .put("requestBody", requestBody)
                        .put("requestMetRequestInitToken", httpServletRequest.getMethod())
                        .put("requestPath", requestPath)
                        .put("token", httpServletRequest.getHeader("token"))
                        .put("responseStatus", httpServletResponse.getStatus())
                        .put("responseBody", Utf8.decode(httpServletResponse.getContentAsByteArray()))
                        .put("ip", requestInitService.ip())
                        .put("requestId", requestInitService.requestId())
                        .put("exeTotalTime", (endTime - startTime))
                );
            }
        } catch (IllegalArgumentException ignored) {
        }
        httpServletResponse.copyBodyToResponse();
        requestInitService.destroy();
    }

    @Override
    public void destroy() {
    }
}
