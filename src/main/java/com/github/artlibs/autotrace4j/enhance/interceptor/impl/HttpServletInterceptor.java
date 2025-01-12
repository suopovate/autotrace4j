package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.support.TypePools;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.enhance.interceptor.Instance;
import com.github.artlibs.autotrace4j.support.ReflectUtils;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * 功能：增强 HttpServlet
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class HttpServletInterceptor extends Instance {
    /**
     * 在原方法刚开始进入时执行
     *
     * @param thiz         增强的对象实例
     * @param allArgs      原方法的参数表
     * @param originMethod 原方法
     * @throws Exception -
     */
    @Override
    public void beforeMethod(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        String traceId = ReflectUtils.getMethod(allArgs[0]
                , TypePools.GET_HEADER, String.class).invoke(AutoTraceCtx.ATO_TRACE_ID);
        String parentSpanId = ReflectUtils.getMethod(allArgs[0]
            , TypePools.GET_HEADER, String.class).invoke(AutoTraceCtx.ATO_SPAN_ID);

        boolean setTraceIdToResponse = true;
        if (Objects.isNull(traceId)) {
            traceId = ReflectUtils.getMethod(allArgs[1]
                    , TypePools.GET_HEADER, String.class).invoke(AutoTraceCtx.ATO_TRACE_ID);
            parentSpanId = ReflectUtils.getMethod(allArgs[1]
                , TypePools.GET_HEADER, String.class).invoke(AutoTraceCtx.ATO_SPAN_ID);

            setTraceIdToResponse = Objects.isNull(traceId);
        }

        if (Objects.isNull(traceId)) {
            traceId = AutoTraceCtx.generate();
        }
        final String spanId = AutoTraceCtx.generate();

        AutoTraceCtx.setSpanId(spanId);
        AutoTraceCtx.setTraceId(traceId);
        AutoTraceCtx.setParentSpanId(parentSpanId);

        if (setTraceIdToResponse) {
            ReflectUtils.getMethod(allArgs[1], TypePools.SET_HEADER, String.class,
                                   String.class).invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
            ReflectUtils.getMethod(allArgs[1], TypePools.SET_HEADER, String.class,
                                   String.class).invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
        }
    }

    /**
     * 在原方法返回前执行
     *
     * @param thiz         增强的对象实例
     * @param allArgs      原方法的参数表
     * @param result       方法执行结果
     * @param originMethod 原方法
     * @return Object - result
     * @throws Exception -
     */
    @Override
    public Object afterMethod(Object thiz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        AutoTraceCtx.removeAll();
        return result;
    }

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("javax.servlet.http.HttpServlet");
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("service")
                .and(takesArgument(0, ElementMatchers.named("javax.servlet.http.HttpServletRequest")))
                .and(takesArgument(1, ElementMatchers.named("javax.servlet.http.HttpServletResponse")));
    }
}
