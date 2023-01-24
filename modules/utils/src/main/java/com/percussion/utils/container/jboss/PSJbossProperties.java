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

package com.percussion.utils.container.jboss;

public class PSJbossProperties {

    /**
     * The name of the directory below the JBoss home directory that contains
     * various configuration files.
     */
    public  final static String CONF_DIR = "conf";

    /**
     * The path to the rx JBoss configuration dir relative to the rx root.
     */
    public  static  final String RX_CONF_DIR = "AppServer/server/rx";

    /**
     * The name of the file in which JBoss security constraints are
     * defined, currently the "login-config.xml" file located in the
     * {@link #CONF_DIR} directory.
     */
    public  final static String LOGIN_CONFIG_FILE_NAME = "login-config.xml";

    /**
     * The login configuration file location relative to rx root.
     */
    public  final static String LOGIN_CONFIG_FILE = RX_CONF_DIR + "/conf/"
            + LOGIN_CONFIG_FILE_NAME;



    /**
     * The name of the JBoss service config file located in the
     * {@link #CONF_DIR} directory.
     */
    public  final static String JBOSS_SERVICE_FILE_NAME = "jboss-service.xml";



    /**
     * The JBoss service configuration file location relative to the rx root.
     */
    public  final String JBOSS_SERVICE_FILE = RX_CONF_DIR + "/" +
            CONF_DIR + "/" + JBOSS_SERVICE_FILE_NAME;

    /**
     * The path to the rxapp.war directory relative to the rx root.
     */
    public  final static String RX_APP_DIR = RX_CONF_DIR +
            "/deploy/rxapp.ear/rxapp.war";

    /**
     * The name of the UIL2 service config file located in the
     * {@link #JMS_DIR} directory.
     */
    public  final static String UIL2_SERVICE_FILE_NAME = "uil2-service.xml";



    /**
     * The spring configuration file location relative to rx root.
     */
    public  final static String SPRING_CONFIG_FILE = RX_APP_DIR +
            "/WEB-INF/config/spring/server-beans.xml";






}
