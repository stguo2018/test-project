package com.ews.stguo.testproject.utils.client;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class DatabaseClient {

    private DatabaseClient() {

    }

    public static DataSource getDataSource(String url) {
        return getDataSource(url, "root", "mysql", 100);
    }

    public static DataSource getDataSource(String url, String username, String password, int maxConnections) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxConnections);
        return new HikariDataSource(config);
    }

}
