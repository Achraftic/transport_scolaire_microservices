package com.transport.api_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ApiGatewayApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    @Order(-1)
    public GlobalFilter a() {
        return (exchange, chain) -> {
            LOGGER.info("Request path: " + exchange.getRequest().getPath());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                LOGGER.info("Response status: " + exchange.getResponse().getStatusCode());
            }));
        };
    }
}
