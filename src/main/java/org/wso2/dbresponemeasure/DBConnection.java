/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.dbresponemeasure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Initiate Database connection.
 */
public class DBConnection {

    private static final Logger log = LoggerFactory.getLogger(DBConnection.class);

    /**
     * Static object of SQL connection.
     */
    private static Connection connection = null;

    /**
     * Create database connection if closed. Else return existing connection.
     * @param url url of database.
     * @param username username of database.
     * @param password password of database.
     * @return database connection.
     */
    public static Connection getConnection(String url, String username, String password) {

        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                log.error("Unable to connect to database.", e);
                return null;
            }
            return connection;
        } else {
            return connection;
        }
    }

    /**
     * Load JDBC driver.
     * @param driverLocation location of JAR file.
     * @param jdbcConnectionClass Connection classs name.
     */
    public static void loadDBDriver(String driverLocation, String jdbcConnectionClass) {

        Driver driver = null;
        try {
            driver = (Driver) Class.forName(jdbcConnectionClass).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error("Unable to load driver : " + jdbcConnectionClass , e);
        }
        try {
            DriverManager.registerDriver(new DriverShim(driver));
        } catch (SQLException e) {
            log.error("Unable to register driver.", e);
        }
    }
}
