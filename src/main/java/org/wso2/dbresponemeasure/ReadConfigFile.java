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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Read configurations from config.xml file.
 */
public class ReadConfigFile {
    private Properties properties = new Properties();

    private static final Logger log = LoggerFactory.getLogger(ReadConfigFile.class);

    /**
     * Read config.file.
     */
    public ReadConfigFile() {
        try (InputStream input = new FileInputStream(Paths.get(".", "config.properties").toString())) {
            properties.load(input);
        } catch (IOException e) {
            log.error("Can't find/read 'config.properties' file.", e);
        }
    }

    /**
     * get the value related to a property.
     * @param key key of a property.
     * @return value of property.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
