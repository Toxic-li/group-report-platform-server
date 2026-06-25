package com.groupreport.platform.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 安全配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {

            // 登录认证：除登录、注册等接口外，所有接口都需要登录
            SaRouter.match("/**")
                    .notMatch(
                            "/auth/login",
                            "/auth/captcha",
                            "/doc.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/webjars/**",
                            "/favicon.ico"
                    )
                    .check(r -> StpUtil.checkLogin());

            // 权限认证：管理员接口需要admin角色
            SaRouter.match("/admin/**").check(r -> StpUtil.checkRole("SUPER_ADMIN"));

            // 审核接口需要审核员或管理员权限
            SaRouter.match("/report-designer/audit/**")
                    .check(r -> StpUtil.checkRoleOr("AUDITOR", "SUPER_ADMIN", "ADMIN"));

        })).addPathPatterns("/**");
    }
}
