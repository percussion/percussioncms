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

package com.percussion.utils.testing;

import com.percussion.util.IOTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/***
 * Class of helper functions for working with test resources
 */
@Deprecated //Use PSResourceUtils
public class PSTestResourceUtils {

    /***
     * Given a valid resource path, returns the resource as a temporary file
     * @param clazz The class to use for loading the resource
     * @param resourcePath The resource path
     * @param dir  May be null
     * @return
     */
    public static File getFile(Class clazz, String resourcePath, File dir) throws IOException {
        File ret = File.createTempFile("test","tmp", dir);
        ret.deleteOnExit();

        InputStream is = clazz.getResourceAsStream(resourcePath);

        IOTools.copyStreamToFile(is, ret);
        return ret;
    }

    public static File getFakeRxDir() throws IOException {
        Path p = Files.createTempDirectory("test");

        File ret = p.toFile();
        ret.deleteOnExit();

        return ret;
    }

    public static File getFakeRxDirFile(Class clazz, String resourcePath) throws IOException {
        return getFile(clazz, resourcePath, getFakeRxDir());
    }

}
