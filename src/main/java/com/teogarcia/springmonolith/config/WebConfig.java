package com.teogarcia.springmonolith.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.teogarcia.springmonolith.shared.filter.RateLimitFilter;
import com.teogarcia.springmonolith.shared.filter.RequestIdFilter;
import com.teogarcia.springmonolith.shared.filter.SecurityHeadersFilter;
import com.teogarcia.springmonolith.shared.interceptor.LoggingInterceptor;
import com.teogarcia.springmonolith.shared.interceptor.MetricsInterceptor;
import com.teogarcia.springmonolith.shared.interceptor.TransformInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final AppProperties props;
  private final LoggingInterceptor loggingInterceptor;
  private final MetricsInterceptor metricsInterceptor;
  private final TransformInterceptor transformInterceptor;

  public WebConfig(
      AppProperties props,
      LoggingInterceptor loggingInterceptor,
      MetricsInterceptor metricsInterceptor,
      TransformInterceptor transformInterceptor) {
    this.props = props;
    this.loggingInterceptor = loggingInterceptor;
    this.metricsInterceptor = metricsInterceptor;
    this.transformInterceptor = transformInterceptor;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if (props.corsEnabled()) {
      registry
          .addMapping("/**")
          .allowedOrigins(props.corsOrigin())
          .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
          .allowedHeaders("*")
          .allowCredentials(true);
    }
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loggingInterceptor).order(1);
    registry.addInterceptor(metricsInterceptor).order(2);
    // TransformInterceptor is a ResponseBodyAdvice, not a HandlerInterceptor
  }

  @Bean
  public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
    FilterRegistrationBean<RequestIdFilter> bean =
        new FilterRegistrationBean<>(new RequestIdFilter());
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    bean.addUrlPatterns("/*");
    return bean;
  }

  @Bean
  public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
    FilterRegistrationBean<SecurityHeadersFilter> bean =
        new FilterRegistrationBean<>(new SecurityHeadersFilter());
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    bean.addUrlPatterns("/*");
    return bean;
  }

  @Bean
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
      RateLimitFilter filter) {
    FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>(filter);
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
    bean.addUrlPatterns("/api/*");
    return bean;
  }
}
