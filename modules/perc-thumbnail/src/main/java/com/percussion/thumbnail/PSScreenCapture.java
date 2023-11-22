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
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.tools.IPSUtilsConstants;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PSScreenCapture {


    private PSScreenCapture(){
        //Hide public constructor
    }

    protected static final Logger log = LogManager.getLogger(IPSUtilsConstants.SERVER_LOG);

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

    public static void takeCapture(String urlForCapture, String imagePath,
                                   int width, int height) {
     //@TODO Need to implement it
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
