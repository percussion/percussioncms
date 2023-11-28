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

package com.percussion.thumbnail;


import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSProperties;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.tools.IPSUtilsConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PSScreenCapture {


    private PSScreenCapture(){
        //Hide public constructor
    }

    protected static final Logger log = LogManager.getLogger(IPSUtilsConstants.SERVER_LOG);

    private static final String PROPS_SERVER = "server.properties";
    private static final String ENTRY_NAME = "server_config_base_dir";
    private static final String BASE_CONFIG_DIR = "rxconfig";
    private static final String SERVER_DIR = BASE_CONFIG_DIR + "/Server";
    private static final String SCREEN_CAPTURE_CMD_PROP="screenshotCommandLine";
    private static final String IMAGE_PATH_TOKEN="@@file@@";
    private static final String IMAGE_URL_TOKEN="@@url@@";

    private static PSProperties ms_serverProps = new PSProperties();


    public static final String EMPTY_THUMB_RESOURCE = "META-INF/resources/sys_resources/images/thumbnail/empty-thumb.jpg";
    public static final String WEB_CAP_JS_RESOURCE = "META-INF/resources/sys_resources/js/webcap.js";
     private static final String OS = System.getProperty("os.name")
            .toLowerCase();

    private static volatile Boolean install = true;

    private static final File WEB_CAP_PATH = new File(PathUtils.getRxDir(null), "sys_resources/js/webcap.js");


    public static void generateEmptyThumb(String imagePathForGeneration) {
        copyResource(EMPTY_THUMB_RESOURCE, new File(imagePathForGeneration));

    }

    private static synchronized void extractExecutable() {

        if (install)
        {
            copyWebCapResource();
        }
    }


    private static PSProperties getServerProperties() {
        if(ms_serverProps != null){
            return  ms_serverProps;
        }
        File propFile = PSProperties.getConfig(ENTRY_NAME, PROPS_SERVER,
                getRxConfigDir(SERVER_DIR));

        try {
            ms_serverProps = new PSProperties(propFile.getPath());
        } catch ( IOException e) {
            log.error("File not found in PSProperties", e);
        }

        return ms_serverProps;
    }

    public static String getRxConfigDir(String path) {

        File item = new File(PathUtils.getRxDir(null), path);
        if (item.exists() == false)
        {
            throw new IllegalArgumentException("file does not exist: " + item.getAbsolutePath());
        }
        return item.getAbsolutePath();
    }

    public static void takeCapture(String urlForCapture, String imagePath,
                                    int width, int height) {
        try{
            Properties serverProps = getServerProperties();
            if(serverProps == null){
                log.debug("Screenshot capture command not found as Server properties missing. ");
                return;
            }
            String command = serverProps.getProperty(SCREEN_CAPTURE_CMD_PROP);
            if(StringUtils.isEmpty(command)){
                log.debug("Screenshot capture command not found in Server properties.");
                return;
            }
            command = command.replace(IMAGE_PATH_TOKEN,imagePath);
            command = command.replace(IMAGE_URL_TOKEN, urlForCapture);

            // Execute the command
            Process process = Runtime.getRuntime().exec(command);

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check if the process exited successfully
            if (exitCode == 0) {
                log.debug("Screenshot captured successfully and saved to:{} ",imagePath);
            } else {
                log.error("Failed to capture screenshot. Exit code: {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed to capture screenshot : {}",e.getLocalizedMessage());
        }

    }

    private static void copyWebCapResource() {
        if (!WEB_CAP_PATH.exists()) {
                copyResource(WEB_CAP_JS_RESOURCE, WEB_CAP_PATH);
        }
    }

    private static void copyResource(String resourceName, File destination) {
        ClassLoader classLoader = PSScreenCapture.class.getClassLoader();

        try ( InputStream stream = classLoader.getResourceAsStream(resourceName)){

            if(stream==null){
                log.warn("Unable to locate thumbnail resource: {}" , resourceName);
            }else{
                FileUtils.copyInputStreamToFile(stream,destination);
            }
        } catch (IOException e) {
            log.warn("Unable to copy thumbnail resource: {}" , PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
}
