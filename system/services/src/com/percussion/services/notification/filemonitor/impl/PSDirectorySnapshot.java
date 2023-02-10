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
package com.percussion.services.notification.filemonitor.impl;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 * Utility class for PSDirectoryWatcher
 *
 */

public class PSDirectorySnapshot
{

    private static Map files = new HashMap();

    public static void addFile(String fileName){
        files.put(fileName, fileName);
    }

    public static void removeFile(String fileName){
        files.remove(fileName);
    }

    public static boolean containsFile(String fileName){
        return files.containsKey(fileName);
    }
}
