package com.javayh.gateway.util;

/**
 * @author haiji
 */
public class GrayscaleEnvironment {

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * <p>
     * 将变量放入当前线程
     * </p>
     *
     * @param currentEnvironmentVersion 当前按需要使用的版本号
     * @version 1.0.0
     * @author hai ji
     * @since 2023/3/27
     */
    public static void setCurrentEnvironment(String currentEnvironmentVersion) {
        THREAD_LOCAL.set(currentEnvironmentVersion);
    }

    /**
     * <p>
     * 获取当前线程的环境变量
     * </p>
     *
     * @return java.lang.String
     * @version 1.0.0
     * @author hai ji
     * @since 2023/3/27
     */
    public static String getCurrentEnvironment() {
        return THREAD_LOCAL.get();
    }

    /**
     * <p>
     * 删除当前线程的变量
     * </p>
     *
     * @version 1.0.0
     * @author hai ji
     * @since 2023/3/27
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
