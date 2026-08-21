package com.teogarcia.springmonolith.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

  private final AppProperties props;

  public OpenApiConfig(AppProperties props) {
    this.props = props;
  }

  @Bean
  @ConditionalOnProperty(name = "app.docs-enabled", havingValue = "true", matchIfMissing = true)
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title(
                    props.env().equals("test")
                        ? "Spring Monolith Template"
                        : "Spring Monolith Template")
                .version(props.version())
                .description("Production-ready Spring Boot monolith — portfolio parity"))
        .servers(List.of(new Server().url(props.openapiServerUrl())));
  }
}
