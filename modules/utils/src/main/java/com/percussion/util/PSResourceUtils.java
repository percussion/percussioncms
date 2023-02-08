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

package com.percussion.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class PSResourceUtils {

    public static String getResourcePath(Class clazz, String resourcePath) {
        URL url = clazz.getResource(resourcePath);
        if (url==null)
            throw new IllegalArgumentException("Cannot load resource "+resourcePath);
        return url.getPath();
    }
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
