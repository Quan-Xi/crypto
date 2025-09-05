package com.flow.exception;

import com.flow.common.init.RequestInitService;
import com.flow.common.ApplicationContextTool;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.UUID;

public class RequestContext {

    public static LocalDateTime time(){
        RequestInitService reqInitService = getReqInitService();
        if (Objects.isNull(reqInitService)) return LocalDateTime.now();
        return reqInitService.now();
    }

    public static String reqIdWithMethodTrace(){
        return reqId().concat(", methodTrace:").concat(methodTrace());
    }

    public static String reqId(){
        RequestInitService reqInitService = getReqInitService();
        if (Objects.isNull(reqInitService)) return UUID.randomUUID().toString();
        return reqInitService.requestId();
    }

    public static void resetReqId(String reqId){
        if (StringUtils.isBlank(reqId)) return;
        RequestInitService reqInitService = getReqInitService();
        if (Objects.isNull(reqInitService)) return;
        String requestId = reqInitService.requestId();
        reqInitService.setReqId(requestId + "#" + reqId);
    }

    public static String reqIp(){
        RequestInitService reqInitService = getReqInitService();
        if (Objects.isNull(reqInitService)) return "0.0.0.0";
        return reqInitService.get().getIp();
    }


    public static String methodTrace() {
        Exception virtualException = new RuntimeException(StringUtils.EMPTY);
        StackTraceElement[] stackTrace = virtualException.getStackTrace();
        ArrayDeque<String> methodChain = new ArrayDeque<>();
        int loopSize = Math.min(50, stackTrace.length);
        String fileName = "RequestContext.java";
        for (int i = 0; i < loopSize; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            String currFileName;
            if (StringUtils.isNotBlank(currFileName = stackTraceElement.getFileName())
                    && currFileName.endsWith("java")
                    && !StringUtils.equalsIgnoreCase(currFileName, "RequestContext.java")
                    && stackTraceElement.getClassName().contains("tianli")) {
                if (StringUtils.equalsIgnoreCase(currFileName, "LoggerHandle.java")){
                    break;
                }
                if (StringUtils.equals(fileName, currFileName)) {
                    methodChain.addLast("#" + stackTraceElement.getMethodName());
                } else {
                    fileName = currFileName;
                    methodChain.addLast(currFileName + "#" + stackTraceElement.getMethodName());
                }
            }
        }
        return new Gson().toJson(methodChain);
    }

    private static RequestInitService getReqInitService(){
        return ApplicationContextTool.getBean("requestInitService", RequestInitService.class);
    }
}
