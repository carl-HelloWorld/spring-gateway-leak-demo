package com.hello.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR;

/**
 * 请求体日志组件
 */
@Component
public class RequestBodyLogGlobalFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(RequestBodyLogGlobalFilter.class);

    private final List<HttpMessageReader<?>> messageReaders;

    public RequestBodyLogGlobalFilter(final ServerCodecConfigurer codecConfigurer) {
        this.messageReaders = codecConfigurer.getReaders();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest cachedRequest = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        if (cachedRequest == null) {
            return ServerWebExchangeUtils.cacheRequestBody(exchange, (serverHttpRequest -> {
                if (exchange.getRequest() == serverHttpRequest) {
                    return chain.filter(exchange);
                }
                return requestBodyStoreToExchange(exchange, chain, serverHttpRequest);
            }));
        }else {
            return requestBodyStoreToExchange(exchange, chain, cachedRequest);
        }
    }

    private Mono<Void> requestBodyStoreToExchange(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest cachedRequest) {
        final ServerRequest serverRequest = ServerRequest
                .create(exchange.mutate().request(cachedRequest).build(), messageReaders);
        // todo serverRequest.bodyToMono(byte[].class) netty leak? when throw LimitedDataBufferList.raiseLimitException

        // io.netty.util.ResourceLeakDetector.reportUntracedLeak will print the memory leak log、

        // invoke track->serverRequest.bodyToMono(byte[].class)
        // -> AbstractDataBufferDecoder.decodeToMono
        // -> ByteArrayDecoder
        return serverRequest.bodyToMono(byte[].class).doOnNext(requestBody -> {
            String requestBodyLog = getRequestBodyLog(requestBody);
            if (log.isDebugEnabled()) {
                log.debug(exchange.getLogPrefix() + "requestBodyLog is:" + requestBodyLog);
            }
            if (StringUtils.isNotEmpty(requestBodyLog)) {
                exchange.getAttributes().put("requestBodyLog", requestBodyLog);
            }
        }).then(Mono.defer(() -> {
            return chain.filter(exchange.mutate().request(cachedRequest).build());
        }));
    }

    @Override
    public int getOrder() {
        // AuthorizationGlobalFilter之前
        return Ordered.HIGHEST_PRECEDENCE;
    }


    public int getRequestBodyLogMaxByteLength() {
        return 500;
    }

    private String getRequestBodyLog(byte[] buf) {
        if (buf != null && buf.length > 0) {
            int length = Math.min(buf.length,getRequestBodyLogMaxByteLength());
            try {
                String requestBodyToUse = new String(buf, 0, length, Charset.forName("utf-8").name());
                if (buf.length > getRequestBodyLogMaxByteLength()) {
                    requestBodyToUse = requestBodyToUse + "......";
                }
                return requestBodyToUse;
            } catch (UnsupportedEncodingException ex) {
                return "[unknown]";
            } catch (Throwable ex) {
                log.error("ex", ex);
                return "[unknown]";
            }
        }
        return null;
    }

}
