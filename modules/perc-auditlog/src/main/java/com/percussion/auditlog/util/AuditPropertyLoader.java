/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.auditlog.util;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AuditPropertyLoader {

    private static final Logger log = LogManager.getLogger(AuditPropertyLoader.class);

    private AuditPropertyLoader(){
        //Don't allow new instances
    }

    public static Properties loadProperties(String filePath){
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            prop.load(input);

        } catch (IOException ex) {
            log.warn("Unable to load Audit Log properties file: {}", PSExceptionUtils.getMessageForLog(ex));
            log.debug(ex);
        }

        return prop;
    }
}
