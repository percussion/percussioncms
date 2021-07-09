/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.percussion.tomcat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PSTomcatPropertySource implements org.apache.tomcat.util.IntrospectionUtils.PropertySource {

    private static final Logger logger = LogManager.getLogger(PSTomcatPropertySource.class);

    /***
     * Default constructor
     */
    public PSTomcatPropertySource() {
        logger.debug("Initialized..");
       /* System.out.println("----Environment ----");
        for(String key : System.getenv().keySet()){
            System.out.println(key + "=" + System.getenv().get(key));
        }
        System.out.println("----End Environment----");

        System.out.println("----System ----");
        System.getProperties().list(System.out);
        System.out.println("----End System----");
        */
    }

    @Override
    public String getProperty(String s) {
        return getProperties().getProperty(s);
    }

    private Properties getProperties() {
        String catalinaBase = System.getProperty("catalina.home");


        if(catalinaBase == null){
            logger.error("Unable to determine catalina.home!  Is the environment set?");
            catalinaBase="";
        }
        logger.debug("Got catalina.home:{}", catalinaBase);
        Properties props = new Properties();

        Path p = Paths.get(catalinaBase, "conf/perc");
        p = p.resolve("perc-catalina.properties");

        try (FileInputStream fs = new FileInputStream(p.toFile())) {
            props.load(fs);
        } catch (IOException exception) {
            logger.error("Error reading:{} got error {}", p.toAbsolutePath(), exception.getMessage());
        }

        return props;
    }
}
