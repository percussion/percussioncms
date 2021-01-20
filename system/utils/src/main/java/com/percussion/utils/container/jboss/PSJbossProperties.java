/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
