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

package com.percussion.widgets.image.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * Utilities for handling mime types
 */
public class MimeUtils {

    private static final Logger log = LogManager.getLogger(MimeUtils.class);

    private MimeUtils(){
        //Only allow static access
    }
    public static String[] getSupportedMimeTypes(){
        ImageIO.scanForPlugins();

        return ImageIO.getReaderMIMETypes();
    }


    public static String getMimeTypeByExtension(String ext){
        Properties props = new Properties();

        try(InputStream is = MimeUtils.class.getClassLoader().getResourceAsStream(
                    "com/percussion/widgets/image/image-mime-types.properties")){

            props.load(is);
        } catch (IOException e) {
            log.error("Unable to load resource image-mime-types.properties!");
        }
        return props.getProperty(ext);
    }

}
