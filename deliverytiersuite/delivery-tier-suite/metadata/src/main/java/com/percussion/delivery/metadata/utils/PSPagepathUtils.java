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

package com.percussion.delivery.metadata.utils;

/**
 * Pagepath-related utility methods.
 * 
 * @author miltonpividori
 *
 */
public class PSPagepathUtils
{
    /**
     * Normalizes a file path, replacing '\' character by '/'.
     * 
     * @param path A path to normalize. Should never be <code>null</code>.
     * @return The normalized path. Never <code>null</code>.
     */
    public static String processPath(String path)
    {
        return path.replace("\\", "/");
    }
}
