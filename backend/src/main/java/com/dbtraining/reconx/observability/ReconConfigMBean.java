package com.dbtraining.reconx.observability;


import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.management.*;
import org.springframework.jmx.export.annotation.*;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@Component
@ManagedResource(
        objectName = "reconx:type=ReconConfig",
        description = "Runtime reconciliation configuration"
)
public class ReconConfigMBean {


    private volatile double priceTolerance = 0.01;

    private volatile boolean cachingEnabled = true;


    private final CacheManager cacheManager;
    private final ReconConfigMBean reconConfigMBean =
        new ReconConfigMBean(
                new ConcurrentMapCacheManager()
        );


    public ReconConfigMBean(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    @ManagedAttribute(
            description = "Price tolerance used during reconciliation"
    )
    public double getPriceTolerance() {
        return priceTolerance;
    }


    @ManagedAttribute(
            description = "Update price tolerance"
    )
    public void setPriceTolerance(double priceTolerance) {

        if(priceTolerance < 0) {
            throw new IllegalArgumentException(
                "Price tolerance cannot be negative"
            );
        }

        this.priceTolerance = priceTolerance;
    }



    @ManagedAttribute(
            description = "Whether caching is enabled"
    )
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }



    @ManagedAttribute(
            description = "Enable or disable caching"
    )
    public void setCachingEnabled(boolean enabled) {
        this.cachingEnabled = enabled;
    }



    @ManagedOperation(
            description = "Clear all application caches"
    )
    public void clearCache() {

        cacheManager.getCacheNames()
                .forEach(cacheName -> {

                    var cache = cacheManager.getCache(cacheName);

                    if(cache != null) {
                        cache.clear();
                    }
                });
    }
}