package com.nepxion.discovery.plugin.strategy.service.aop;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.strategy.constant.StrategyConstant;

public class FeignStrategyInterceptor extends AbstractStrategyInterceptor implements RequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(FeignStrategyInterceptor.class);

    public FeignStrategyInterceptor(String requestHeaders) {
        super(requestHeaders);

        LOG.info("----------- Feign Intercept Information ----------");
        LOG.info("Feign desires to intercept customer headers are {}", requestHeaderList);
        LOG.info("--------------------------------------------------");
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        printInputRouteHeader();

        applyInnerHeader(requestTemplate);
        applyOuterHeader(requestTemplate);

        printOutputRouteHeader(requestTemplate);
    }

    private void applyInnerHeader(RequestTemplate requestTemplate) {
        requestTemplate.header(DiscoveryConstant.N_D_SERVICE_TYPE, pluginAdapter.getServiceType());
        requestTemplate.header(DiscoveryConstant.N_D_SERVICE_ID, pluginAdapter.getServiceId());
        requestTemplate.header(DiscoveryConstant.N_D_SERVICE_ADDRESS, pluginAdapter.getHost() + ":" + pluginAdapter.getPort());
        requestTemplate.header(DiscoveryConstant.N_D_SERVICE_GROUP, pluginAdapter.getGroup());
        requestTemplate.header(DiscoveryConstant.N_D_SERVICE_VERSION, pluginAdapter.getVersion());
        requestTemplate.header(DiscoveryConstant.N_D_SERVICE_REGION, pluginAdapter.getRegion());
    }

    private void applyOuterHeader(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = serviceStrategyContextHolder.getRestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest previousRequest = attributes.getRequest();
        Enumeration<String> headerNames = previousRequest.getHeaderNames();
        if (headerNames == null) {
            return;
        }

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = previousRequest.getHeader(headerName);
            boolean isHeaderContains = isHeaderContainsExcludeInner(headerName.toLowerCase());
            if (isHeaderContains) {
                requestTemplate.header(headerName, headerValue);
            }
        }
    }

    private void printOutputRouteHeader(RequestTemplate requestTemplate) {
        Boolean interceptLogPrint = environment.getProperty(StrategyConstant.SPRING_APPLICATION_STRATEGY_INTERCEPT_LOG_PRINT, Boolean.class, Boolean.FALSE);
        if (!interceptLogPrint) {
            return;
        }

        System.out.println("--------- Output Route Header Information --------");
        Map<String, Collection<String>> headers = requestTemplate.headers();
        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            boolean isHeaderContains = isHeaderContains(headerName.toLowerCase());
            if (isHeaderContains) {
                Collection<String> headerValue = entry.getValue();

                System.out.println(headerName + "=" + headerValue);
            }
        }
        System.out.println("--------------------------------------------------");
    }
}