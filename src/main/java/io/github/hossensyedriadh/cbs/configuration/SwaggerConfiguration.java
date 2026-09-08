package io.github.hossensyedriadh.cbs.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Value("${info.application.name}")
    private String title;

    @Value("${info.application.version}")
    private String version;

    @Value("${info.application.description}")
    private String description;

    @Value("${info.developer.name}")
    private String devName;

    @Value("${info.developer.email}")
    private String devEmail;

    @Value("${info.developer.url}")
    private String devUrl;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                        .title("Core Banking Solution").version(this.version)
                        .description(this.description)
                        .contact(new Contact().name(this.devName).email(this.devEmail)
                                .url(this.devUrl)))
                .specVersion(SpecVersion.V31);
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group(this.title).pathsToMatch("/**")
                .displayName("CBS API")
                .build();
    }
}
