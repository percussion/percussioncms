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
