// src/main/java/com/example/apigateway/filter/AddProofHeaderFilter.java
package com.eventmanagementisii.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AddProofHeaderFilter extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Passed-By-Gateway", "YES-IT-REALLY-DID")
                .build();
            return chain.filter(exchange.mutate().request(request).build());
        };
    }
}