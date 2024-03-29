package com.github.yourbootloader.config;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Conditional(YtLiquibaseConfiguration.LiquibaseCondition.class)
public class YtLiquibaseConfiguration {

    private final Environment environment;

    public YtLiquibaseConfiguration(Environment environment) {
        this.environment = environment;
    }

//    @Bean
//    SpringLiquibase liquibase(@Value("${yt.jdbc.schema}") String schema, DataSource dataSource) {
//        SpringLiquibase liquibase = new SpringLiquibase();
//        liquibase.setChangeLog("classpath:liquibase/changelog.xml");
//        liquibase.setDataSource(dataSource);
//        liquibase.setContexts(String.join(",", environment.getActiveProfiles()));
//        if (dataSource instanceof HikariDataSource) {
//            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
//            liquibase.setDefaultSchema(hikariDataSource.getSchema());
//        } else {
//            liquibase.setDefaultSchema(schema);
//        }
//        return liquibase;
//    }

    static class LiquibaseCondition extends AnyNestedCondition {
        public LiquibaseCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(value = "yt.jdbc.schema", havingValue = "public")
        static class DevDataSource {}
    }

}
