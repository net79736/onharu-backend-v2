package com.backend.onharu.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.url")
public class ServerUrlProperties {
    private String front; // app.url.front
    private String back; // app.url.back
}
