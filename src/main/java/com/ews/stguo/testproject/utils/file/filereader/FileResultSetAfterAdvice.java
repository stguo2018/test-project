package com.ews.stguo.testproject.utils.file.filereader;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterAdvice;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
public class FileResultSetAfterAdvice implements MethodInterceptor, AfterAdvice, Serializable {

    private final String className;
    private static final String STRING_CLASS_NAME = "java.lang.String";

    public FileResultSetAfterAdvice(String className) {
        this.className = className;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (Objects.equals(invocation.getMethod().getName(), "readLine")) {
            Object result = invocation.proceed();
            if (result != null) {
                switch (className) {
                    case STRING_CLASS_NAME:
                        if (result instanceof String) {
                            return result.toString();
                        }
                        break;
                }
                Exception ex = new Exception(String.format("Result type: %s, expected type: %s, Type mismatch!",
                        result.getClass().getName(), className));
                log.error("Handle result data type failed.", ex);
                throw ex;
            }
        } else {
            return invocation.proceed();
        }
        return null;
    }
}
