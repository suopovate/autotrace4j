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

/**
 * 功能：增强 Apache HttpComponents clinet, 涵盖Feign、RestTemplate使用Apache HttpClient的情况
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class ApacheHttpClientInterceptor extends Instance {
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
        final String traceId = AutoTraceCtx.getTraceId();
        if (Objects.nonNull(traceId)) {
            ReflectUtils.WrappedMethod method = ReflectUtils.getMethod(allArgs[1]
                , TypePools.SET_HEADER, String.class, String.class);

            // adapt to shop & big data
            method.invoke(AutoTraceCtx.TRACE_HEADER, traceId);
            method.invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);

            final String spanId = AutoTraceCtx.getSpanId();
            if (Objects.nonNull(spanId)) {
                method.invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
            }
        }
    }

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.apache.http.impl.client.AbstractHttpClient")
                .or(ElementMatchers.named("org.apache.http.impl.client.MinimalHttpClient"))
                .or(ElementMatchers.named("org.apache.http.impl.client.InternalHttpClient"));
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("doExecute");
    }
}
