package com.training.gateway.gateway;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Service
public class SimplePreFilter extends ZuulFilter {


    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private RedisTemplate< String, Object > template;

    public Object getValue( final String key ) {
        return template.opsForValue().get( key );
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String requestUri = ctx.getRequest().getRequestURI();
        System.out.println(requestUri);
        String requestTokenHeader = ctx.getRequest().getHeader(AUTHORIZATION);
        System.out.println(requestTokenHeader);
        return (requestUri.startsWith("/order"));
    }


    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        String requestTokenHeader = request.getHeader(AUTHORIZATION);

        if(requestTokenHeader == null) {
            try {
                ctx.getResponse().sendError(400);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctx.setSendZuulResponse(false);
        }


        String jwtToken = requestTokenHeader.substring(7);


        String redisResponse = (String)getValue(jwtToken);

        if(redisResponse == null) {
            try {
                ctx.getResponse().sendError(401);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctx.setSendZuulResponse(false);
        }

        return true;

    }
}
