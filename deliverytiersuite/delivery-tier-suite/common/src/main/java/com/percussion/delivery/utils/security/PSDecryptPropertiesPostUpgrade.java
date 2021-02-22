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

package com.percussion.delivery.utils.security;


import com.percussion.delivery.utils.spring.PSPropertiesFactoryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/***
 * This utility class is intended for use post upgrade
 * to decrypt all property files with the previous version of
 * the JRE so that they can be re-encrypted by the new version
 * of the JRE after upgrade.
 */
public class PSDecryptPropertiesPostUpgrade {

    private static final Logger log = LogManager.getLogger(PSDecryptPropertiesPostUpgrade.class);

    public static String[] CONF_DIRS={"Deployment/Server/conf/perc", "Staging/Deployment/Server/conf/perc"};

    public static void main(String[] args){

        String rootDir = args[0];


        Path prodDTSConf = Paths.get(rootDir,CONF_DIRS[0]);
        Path stageDTSConf = Paths.get(rootDir,CONF_DIRS[1]);

        if(prodDTSConf.toFile().exists() && prodDTSConf.toFile().isDirectory()){
           try (Stream<Path> files = Files.list(prodDTSConf)){

               files.forEach(p -> decryptProperties(p));

           } catch (IOException e) {
               log.error(e.getMessage());
               log.debug(e);
           }
        }

        if(stageDTSConf.toFile().exists() && stageDTSConf.toFile().isDirectory()){
            try (Stream<Path> files = Files.list(stageDTSConf)){

                files.forEach(p -> decryptProperties(p));

            } catch (IOException e) {
                log.error(e.getMessage());
                log.debug(e);
            }
        }


    }

    public static void decryptProperties(Path p){

        if (p.getFileName().endsWith(".properties")) {
            System.out.println("Processing property file: " + p.toString());
            PSSecureProperty.unsecureProperties(p.toFile());

        }
    }
}
