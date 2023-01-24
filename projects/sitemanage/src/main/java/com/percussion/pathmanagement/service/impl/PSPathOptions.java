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
package com.percussion.pathmanagement.service.impl;

import com.percussion.pathmanagement.data.PSPathItem;

/**
 * Holds current options for path retrieval.
 * 
 * @author JaySeletz
 *
 */
public class PSPathOptions
{
    
    /**
     * Track if current operation loading children should check what types of children they have
     */
    private static ThreadLocal<Boolean> checkChildTypes = new ThreadLocal<>();
    
    /**
     * Track if current operation loading children should be restricted to folder children
     */
    private static ThreadLocal<Boolean> folderChildrenOnly = new ThreadLocal<>();
    
    /**
     * Get Thread local setting for checking child types (section, folder, item) when loading path items.
     * See {@link PSPathItem#hasFolderChildren()}, {@link PSPathItem#hasItemChildren()}, and 
     * {@link PSPathItem#hasSectionChildren()}, which will be <code>false</code> if not checked.
     * 
     * @return <code>true</code> to check them, <code>false</code> to skip checking.  Defaults to <code>false</code>
     * if not set.
     */
    public static boolean shouldCheckChildTypes()
    {
        Boolean check = checkChildTypes.get();
        return check == null ? false : checkChildTypes.get();
    }

    /**
     * Set if child types should be checked when loading path items.
     * 
     * @param shouldSkip <code>true</code> to check, <code>false</code> to skip checking. 
     */
    public static void setShouldCheckChildTypes(boolean shouldSkip)
    {
        checkChildTypes.set(shouldSkip);
    }
    
    public static boolean folderChildrenOnly()
    {
        Boolean result = folderChildrenOnly.get();
        return result == null ? false : result;
    }
    
    public static void setFolderChildrenOnly(boolean foldersOnly)
    {
        folderChildrenOnly.set(foldersOnly);
    }
}
