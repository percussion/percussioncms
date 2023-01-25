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

package com.percussion.webui.gadget.servlets;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSServer;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
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
