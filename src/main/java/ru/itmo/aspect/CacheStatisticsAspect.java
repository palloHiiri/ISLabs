package ru.itmo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.itmo.service.CacheStatisticsService;
import ru.itmo.dto.response.CacheStatisticsDto;

@Aspect
@Component
public class CacheStatisticsAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheStatisticsAspect.class);

    private final CacheStatisticsService cacheStatisticsService;

    public CacheStatisticsAspect(CacheStatisticsService cacheStatisticsService) {
        this.cacheStatisticsService = cacheStatisticsService;
    }

    @Pointcut("execution(public * ru.itmo.repository.*.*(..))")
    public void repositoryMethods() {}

    @Pointcut("execution(public * ru.itmo.service.*.*(..))")
    public void serviceMethods() {}

    @Pointcut("execution(* ru.itmo.service.CacheStatisticsService.*(..))")
    public void cacheStatisticsServiceMethods() {}

    @Around("repositoryMethods()")
    public Object logCacheStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!cacheStatisticsService.isLoggingEnabled()) {
            return joinPoint.proceed();
        }

        CacheStatisticsDto statsBefore = cacheStatisticsService.getCurrentStatistics();

        Object result = joinPoint.proceed();

        CacheStatisticsDto statsAfter = cacheStatisticsService.getCurrentStatistics();

        long hitsDiff = statsAfter.getSecondLevelCacheHits() - statsBefore.getSecondLevelCacheHits();
        long missesDiff = statsAfter.getSecondLevelCacheMisses() - statsBefore.getSecondLevelCacheMisses();
        long putsDiff = statsAfter.getSecondLevelCachePuts() - statsBefore.getSecondLevelCachePuts();

        long queryHitsDiff = statsAfter.getQueryCacheHits() - statsBefore.getQueryCacheHits();
        long queryMissesDiff = statsAfter.getQueryCacheMisses() - statsBefore.getQueryCacheMisses();
        long queryPutsDiff = statsAfter.getQueryCachePuts() - statsBefore.getQueryCachePuts();

        if (hitsDiff > 0 || missesDiff > 0 || putsDiff > 0 ||
            queryHitsDiff > 0 || queryMissesDiff > 0 || queryPutsDiff > 0) {

            String methodName = joinPoint.getSignature().toShortString();

            logger.info("=== L2 Cache Statistics for {} ===", methodName);
            logger.info("Second Level Cache: hits={}, misses={}, puts={}", hitsDiff, missesDiff, putsDiff);
            logger.info("Query Cache: hits={}, misses={}, puts={}", queryHitsDiff, queryMissesDiff, queryPutsDiff);

            logger.info("Total L2 Cache - Hits: {}, Misses: {}, Hit Ratio: {}%",
                    statsAfter.getSecondLevelCacheHits(),
                    statsAfter.getSecondLevelCacheMisses(),
                    String.format("%.2f", statsAfter.getSecondLevelCacheHitRatio()));
            logger.info("Total Query Cache - Hits: {}, Misses: {}, Hit Ratio: {}%",
                    statsAfter.getQueryCacheHits(),
                    statsAfter.getQueryCacheMisses(),
                    String.format("%.2f", statsAfter.getQueryCacheHitRatio()));
            logger.info("=============================================");
        }

        return result;
    }
}

