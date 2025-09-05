package com.flow.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
public class ExceptionUtils {

    public static void printStackTrace(Throwable e) {
        try {
            printStackTrace(e, RequestContext.reqId());
        } catch (Throwable ignored) {
        }
    }

    public static void printStackTrace(Throwable e, String reqId) {
        printStackTrace(null, e, reqId);
    }

    public static void printStackTrace(String errMsg, Throwable e, String reqId) {
        StringBuilder errorMsg = new StringBuilder();
        String methodTrace = RequestContext.reqIdWithMethodTrace();

        errorMsg.append("Method Trace: ").append(methodTrace).append("; ");

        if (StringUtils.isNotBlank(errMsg)) {
            errorMsg.append("System msg: ").append(errMsg).append("; ");
        }

        errorMsg.append("RequestId: ").append(reqId).append("; ");
        // Print cause, if any
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        errorMsg.append("Exception msg: ").append(errors.toString().replaceAll("\\n", ""));
        log.error(errorMsg.toString());
    }
}
