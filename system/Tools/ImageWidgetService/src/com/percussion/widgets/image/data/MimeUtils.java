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
