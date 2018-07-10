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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class initiate DB conection and mesure the time.
 */
public class DbResponseMeasure {

    private static final Logger log = LoggerFactory.getLogger(DbResponseMeasure.class);

    /**
     * Main class of the programme.
     * @param args List of command line arguments.
     */
    public static void main(String[] args) {

        ReadConfigFile configs = new ReadConfigFile();

        String url = configs.getProperty("CONNECTION.URL");
        String username = configs.getProperty("CONNECTION.USERNAME");
        String password = configs.getProperty("CONNECTION.PASSWORD");
        String driverClass = configs.getProperty("CONNECTION.DRIVERCLASS");
        String driverLocation = configs.getProperty("CONNECTION.JDBCDRIVER");
        String queryToRun = configs.getProperty("SQL.QUERYTOEXECUTE");
        int diagnosticThreshold = Integer.parseInt(configs.getProperty("DIAGNOSTIC.THRESHOLD"));

        int threadSleepTime = Constents.DEFAULT_THREAD_SLEEP_TIME;
        int iterations = Constents.DEFAULT_ITERATION_COUNT;

        if (configs.getProperty("RUN.THREADSLEEPTIME") != null) {
            threadSleepTime = Integer.parseInt(configs.getProperty("RUN.THREADSLEEPTIME"));
        }

        DBConnection.loadDBDriver(driverLocation, driverClass);
        Connection connection = DBConnection.getConnection(url, username, password);

        if (connection != null) {
            log.info("----Connected to database and query execution started----");
        }

        Statement statement;
        long startTime;
        long completedTime;
        long totalTime = 0;

        if (queryToRun == null) {
            queryToRun = Constents.DEFAULT_QUERY_TO_EXECUTE;
        }

        if (configs.getProperty("SQL.ITERATIONS") != null) {
            iterations = Integer.parseInt(configs.getProperty("SQL.ITERATIONS"));
        }

        if (iterations == -1) {
            iterations = Integer.MAX_VALUE;
        }

        for (int index = 0; index < iterations; index++) {
            try {
                startTime = System.currentTimeMillis();
                statement = connection.createStatement();
                boolean ran = statement.execute(queryToRun);
                if (ran) {
                    log.info("Query Executed");
                }
                statement.close();
                completedTime = System.currentTimeMillis();
                totalTime += (completedTime - startTime);
                log.info("Time taken to execute query : " + (completedTime - startTime) + "ms.");
                if ((completedTime - startTime) > diagnosticThreshold) {
                    log.warn("Diagnostic threshold exceeded. Running the self diagnostics.");
                    runDiagnostics(connection);
                }
            } catch (SQLException e) {
                log.error("Unable to execute query.", e);
                try {
                    connection.close();
                } catch (SQLException ex) {
                    log.error("SQL Error occurred.", e);
                }
            }

            try {
                Thread.sleep(threadSleepTime);
            } catch (InterruptedException e) {
                log.error("Error while sleeping the thread.", e);
                try {
                    connection.close();
                } catch (SQLException ex) {
                    log.error("SQL Error occurred.", e);
                }
            }
        }

        log.info("Average time taken to execute query : " + (totalTime * 1.0 / iterations) + "ms.");

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException | NullPointerException e) {
            log.error("Error occurred while running the query.", e);
        }
    }

    private static void runDiagnostics(Connection connection) {

        String diag1 = "SHOW ENGINE INNODB STATUS";
        String diag2 = "SHOW FULL PROCESSLIST";
        String diag3 = "SHOW OPEN TABLES WHERE In_use > 0";
        String diag4 = "SELECT * FROM mysql.slow_log";

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(diag1);
            printDiagnostics("Output for SHOW ENGINE INNODB STATUS. ", resultSet);
        } catch (SQLException e) {
            log.error("Error while executing diagnostic query.", e);
        }

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(diag2);
            printDiagnostics("Output for SHOW FULL PROCESSLIST. ", resultSet);
        } catch (SQLException e) {
            log.error("Error while executing diagnostic query.", e);
        }

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(diag3);
            printDiagnostics("Output for SHOW OPEN TABLES WHERE In_use > 0. ", resultSet);
        } catch (SQLException e) {
            log.error("Error while executing diagnostic query.", e);
        }

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(diag4);
            printDiagnostics("Output for SELECT * FROM mysql.slow_log. ", resultSet);
        } catch (SQLException e) {
            log.error("Error while executing diagnostic query.", e);
        }
    }

    private static void printDiagnostics(String message, ResultSet resultSet) throws SQLException {

        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                stringBuilder.append("\t").append(resultSet.getString(i));
            }
            stringBuilder.append("\n");
        }
        log.info(message + " : \n" + stringBuilder.toString());
    }
}
