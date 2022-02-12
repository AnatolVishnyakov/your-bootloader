package com.github.yourbootloader.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.DriverDataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class YtDatasourceConfiguration {
    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${youtube.jdbc.schema}")
    private String schema;
    @Value("${youtube.jdbc.properties}")
    private String properties;
    @Value("${youtube.jdbc.max-total}")
    private int maxTotal;
    @Value("${youtube.jdbc.connection.wait.timeout.ms}")
    private int dbConnectionWaitTimeoutMs;
    @Value("${youtube.jdbc.connection.lifetime.minutes}")
    private int dbConnectionLifetimeMinutes;
    @Value("${youtube.jdbc.query.timeout.ms}")
    private int dbQueryTimeoutMs;
    @Value("${youtube.jdbc.connection.leak-threshold.ms}")
    private int leakThresholdMs;

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("masterHikariDataSource") HikariDataSource masterDataSource) {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(YtRoutingDataSource.Route.MASTER, masterDataSource);

        YtRoutingDataSource routingDataSource = new YtRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.setTargetDataSources(dataSources);
        return routingDataSource;
    }

    @Bean
    public HikariDataSource masterHikariDataSource() {
        return getHikariDataSource(
                driverClassName, url, username, password, schema, properties, maxTotal, dbConnectionWaitTimeoutMs,
                dbConnectionLifetimeMinutes, dbQueryTimeoutMs, leakThresholdMs,
                "Master datasource",
                true
        );
    }

    public HikariDataSource getHikariDataSource(
            String driverName, String url, String username, String password, String schema, String propertiesString,
            int maxTotal, int dbConnectionWaitTimeoutMs, int dbConnectionLifetimeMinutes,
            int dbQueryTimeoutMs, int leakThresholdMs, String poolName, boolean requestLoggingEnabled
    ) {
        Properties properties = parse(propertiesString);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);
        hikariConfig.setMaximumPoolSize(maxTotal);
        hikariConfig.setSchema(schema);
        hikariConfig.setDataSource(getInternalDataSource(url, driverName, properties, username, password, requestLoggingEnabled));
        hikariConfig.setValidationTimeout(dbQueryTimeoutMs);
        hikariConfig.setConnectionTimeout(dbConnectionWaitTimeoutMs);
        hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(dbConnectionLifetimeMinutes));
        hikariConfig.setLeakDetectionThreshold(leakThresholdMs);
        return new HikariDataSource(hikariConfig);
    }

    private DataSource getInternalDataSource(String url, String driverName, Properties properties, String username, String password, boolean requestLoggingEnabled) {
        DriverDataSource dataSource = new DriverDataSource(url, driverName, properties, username, password);
        if (requestLoggingEnabled) {
            return wrapDataSource(dataSource);
        }
        return dataSource;
    }

    private DataSource wrapDataSource(DataSource dataSource) {
        return ProxyDataSourceBuilder.create(dataSource)
                .listener(new TraceQueryExecutionListener())
                .build();
    }

    public static Properties parse(String propertiesString) {
        Properties properties = new Properties();
        Arrays.stream(propertiesString.split("&"))
                .map(p -> p.split("="))
                .forEach(p -> {
                    if (p.length == 2) {
                        properties.put(p[0], p[1]);
                    }
                });
        return properties;
    }

    private interface BaseExecutionListener {
    }

    private static class TraceQueryExecutionListener implements QueryExecutionListener, BaseExecutionListener {
        @Override
        public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
            log.info("before query");
        }

        @Override
        public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
            log.info("after query");
        }
    }
}