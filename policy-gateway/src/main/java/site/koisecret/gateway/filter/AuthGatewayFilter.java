package site.koisecret.gateway.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.utils.JWTUtils;
import site.koisecret.gateway.config.IgnoredUrlsProperties;

import java.nio.charset.StandardCharsets;

/**
 * @ClassName: AuthGatewayFilter
 * @author: 燎原
 * @since: 2023/3/30 21:47
 */
@Slf4j
@Component
public class AuthGatewayFilter implements GlobalFilter {

    @Autowired
    IgnoredUrlsProperties ignoredUrlsProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request=exchange.getRequest();
        String access_token=request.getHeaders().getFirst("access_token");

        //TODO 判断当前的url是否需要被拦截
        if(ignoredUrlsProperties.getUrls().contains(request.getURI().getPath())
                || request.getURI().getPath().contains("similar")){
            return chain.filter(exchange);
        }
        if (StringUtils.isEmpty(access_token)) {
            //请求结束
            return onError(exchange,"没有token，无法访问",441);
        }

        try {
            Claims claims = JWTUtils.parseJWT(access_token);
//            log.info("token : {} 验证通过", access_token);
            request.mutate().headers(httpHeaders -> httpHeaders.set("uid", claims.get("uid").toString()))
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(request)
                    .build();
            return chain.filter(mutatedExchange);
        } catch (ExpiredJwtException expiredJwtEx) {
            log.info("token : {} 过期", access_token );
            //请求结束
            return onError(exchange,"toke过期",442);
        } catch (Exception ex) {
            log.info("token : {} 验证失败" , access_token );
            //请求结束
            return onError(exchange,"token验证失败",401);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange,String msg, Integer code){
        ServerHttpResponse response=exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");

        ObjectMapper objectMapper=new ObjectMapper();
        String resStr= null;
        try {
            resStr = objectMapper.writeValueAsString(Result.FAIL(msg,code));
        } catch (JsonProcessingException e) {
            log.error("LoginAuthGatewayFilter Occur Exception:" +e);
        }
        DataBuffer buffer=response.bufferFactory().wrap(resStr.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }
}
