package ru.itmo.service;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import ru.itmo.dto.response.CacheStatisticsDto;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CacheStatisticsService {

    private final SessionFactory sessionFactory;
    private final AtomicBoolean loggingEnabled = new AtomicBoolean(false);

    public CacheStatisticsService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void enableLogging() {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        loggingEnabled.set(true);
    }

    public void disableLogging() {
        loggingEnabled.set(false);
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled.get();
    }

    public CacheStatisticsDto getCurrentStatistics() {
        Statistics statistics = sessionFactory.getStatistics();
        return new CacheStatisticsDto(
                statistics.getSecondLevelCacheHitCount(),
                statistics.getSecondLevelCacheMissCount(),
                statistics.getSecondLevelCachePutCount(),
                statistics.getQueryCacheHitCount(),
                statistics.getQueryCacheMissCount(),
                statistics.getQueryCachePutCount()
        );
    }

    public void resetStatistics() {
        sessionFactory.getStatistics().clear();
    }
}

