/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.ant.install;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PSUpgradeSiteConfig extends PSAction {

    Logger log = LogManager.getLogger(PSUpgradeSiteConfig.class);

    /**
     * The relative path (from the root install dir) of the source folder where
     * the default secure sites configurations files are stored.
     */
    public static final String SECURE_FILES_SOURCE_FOLDER = "sys_resources/webapps/secure/WEB-INF";

    /**
     * The relative path (from the root install dir) of the source folder where
     * the default non secure sites configurations files are stored.
     */
    public static final String NON_SECURE_FILES_SOURCE_FOLDER = "sys_resources/webapps/non-secure/WEB-INF";

    // see base class
    @Override
    public void execute()
    {
        String m_strRootDir = getRootDir();
        log.info("Adding Secure Site Config at : " + m_strRootDir.toString());
        File siteConfigDir = new File(m_strRootDir+File.separator  + "rxconfig" + File.separator + "SiteConfigs");
        log.info("Adding Secure Site Config at : " + siteConfigDir);
        if(Files.exists(siteConfigDir.toPath())) {
            log.info("Adding Secure Site Config as SiteConfig Exists : " + siteConfigDir);
            File[] siteNames = siteConfigDir.listFiles(File::isDirectory);
            if (siteNames != null) {
                for (File siteName : siteNames) {
                    try {
                        log.info("Adding Secure Site Config at for Site: " + siteName);
                        FileUtils.copyDirectory(getSourceConfigurationFolder(), siteName, false);
                    } catch (IOException e) {
                        log.error("SiteConfigUpgrade Failed for Site" + siteName.toString() +" , Error: " + e.getMessage());
                        log.debug("SiteConfigUpgrade Failed for Site" + siteName.toString() +" , Error: " + e.getMessage(),e);
                    }
                }
            }else{
                log.info("No Site Folder for Secure Site Config Found at : " + siteConfigDir.toString());
            }
        }else{
            log.info("No Secure Site Config found at : " + siteConfigDir.toString());
        }
    }

    private File getSourceConfigurationFolder()
    {
        String path = getRootDir();

        path += (path.endsWith(File.separator)) ? SECURE_FILES_SOURCE_FOLDER : File.separator + SECURE_FILES_SOURCE_FOLDER;

        return new File(path);
    }
}
