package com.github.artlibs.autotrace4j;

import com.github.artlibs.autotrace4j.enhance.TraceBuilder;
import com.github.artlibs.autotrace4j.support.ClassInjectUtil;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 功能：Trace 入口
 *
 * @author Fury
 * @since 2023-01-04
 * <p>
 * All rights Reserved.
 */
public final class AutoTrace4j {
    private AutoTrace4j() {}

    /**
     * Java Attach 执行入口
     *
     * @param packagePrefixes - 增强包前缀,多个以英文逗号分隔
     * @param instrument      Instrumentation
     */
    public static void agentmain(
        String packagePrefixes,
        Instrumentation instrument
    ) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, URISyntaxException {
        premain(packagePrefixes, instrument);
    }

    /**
     * Java Agent 方式执行入口
     *
     * @param packagePrefixes - 增强包前缀,多个以英文逗号分隔
     * @param instrument      Instrumentation
     */
    public static void premain(
        String packagePrefixes,
        Instrumentation instrument
    ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        if (Objects.isNull(packagePrefixes) || packagePrefixes.trim().isEmpty()) {
            throw new IllegalArgumentException("请指定业务包名前缀(Agent参数)以指定增强范围; 如：\n" +
                                                   "-javaagent:/dir/to/autotrace4j.jar=com.your-domain.pkg1,com.your-domain.pkg2");
        }
        // 将ctx包下的class注入到bootstrap
        ClassInjectUtil.injectClassToBootStrapClassLoader(instrument, "com.github.artlibs.autotrace4j.ctx");
        TraceBuilder.enhance(packagePrefixes).on(instrument, null);
    }
}