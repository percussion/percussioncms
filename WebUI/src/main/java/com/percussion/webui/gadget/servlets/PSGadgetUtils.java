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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.webui.gadget.servlets;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSServer;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions for gadgets
 */
public class PSGadgetUtils {

    private PSGadgetUtils(){
        //Private constructor to force static access
    }

    private static final Logger log = LogManager.getLogger(PSGadgetUtils.class);

    public static final File gadgetsRoot = new File(PSServer.getRxDir() + "/cm/gadgets/repository");



    /**
     * Given an input url will validate against the uri in the gadget registry
     * if the uri is not valid, it will return false. This is intended to block
     * malicious calls to the gadget servlets.
     *
     * @param url a non null url
     * @return true if the url has a valid path in the url, false if not
     */
    public static boolean isValidGadgetPathInUrl(HttpServletRequest request, URI url){

        if(url == null){
           throw new IllegalArgumentException("URL is required.");
       }

        boolean ret = false; //assume invalid.

        List<File> gadgetFiles = getInstalledGadgetFiles();
        for(File f : gadgetFiles) {
            try {
                if (getGadgetFileNameForCompare(f.getCanonicalPath()).endsWith(url.getPath())) {
                    ret = true;
                    break;
                }
            } catch (IOException e) {
                log.error("An invalid gadget path was provided: {} Error: {}", f.getAbsolutePath(),
                       PSExceptionUtils.getMessageForLog(e) );
                break;
            }
        }

        //validate the host name portions
        List<String> allowedHosts = new ArrayList<>();
        String publicCMSHostName = PSServer.getServerProps().getProperty("publicCmsHostname","");
        if(! StringUtils.isEmpty(publicCMSHostName)){
            allowedHosts.add(publicCMSHostName);
        }
        String allowedOrigins = PSServer.getServerProps().getProperty("allowedOrigins","*");
        if(allowedOrigins.equalsIgnoreCase("*")){
            allowedHosts.add("*");
        }else{
            String[] hosts = allowedOrigins.split(",");
            for(String s: hosts){
                s = s.trim();
                if(s.startsWith("http")){
                    s = s.replace("http://","");
                    s = s.replace("https://", "");
                }
                if(s.contains(":")){
                    s = s.substring(0,s.indexOf(":"));
                }
                allowedHosts.add(s);
            }
        }

        if(! SecureStringUtils.hostMatchesRequest(request,url,allowedHosts))
            ret = false;

        return ret;
    }

    @NotNull
    public static List<File> getInstalledGadgetFiles() {

        List<File> ret = new ArrayList<>();

        File root = new File(gadgetsRoot.getPath());

        File[] gadgetFiles = root.listFiles();

        if (gadgetFiles != null) {
            for (File gadgetFile : gadgetFiles) {
                if (!gadgetFile.isDirectory()) {
                    // only concerned with directories
                    continue;
                }

                File[] gadgetConfigFiles = gadgetFile.listFiles();
                if(gadgetConfigFiles != null) {
                    for (File gadgetConfigFile : gadgetConfigFiles) {
                        if (gadgetConfigFile.isDirectory()) {
                            // only concerned with files
                            continue;
                        }

                        if (gadgetConfigFile.getName().endsWith(".xml")) {
                            ret.add(gadgetConfigFile);
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Convert the path to url / format.
     * @param canonicalPath A canonicalPath to be converted. never null.
     * @return A string containing the path in normalized form.
     */
    protected static String getGadgetFileNameForCompare(String canonicalPath){
        if(canonicalPath == null)
            throw new IllegalArgumentException("Gadget path is required");
        return canonicalPath.replace("\\","/");

    }

}
