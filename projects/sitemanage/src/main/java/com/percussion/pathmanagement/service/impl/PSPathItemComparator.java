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

import java.util.Comparator;

/**
 * PathItem sorter to case insensitive sort alpha ascending.
 */
public class PSPathItemComparator implements Comparator<PSPathItem>
{
    private static PSPathItemComparator instance;
    
    /**
     * @param psPathItemService
     */
    private PSPathItemComparator()
    {
        
    }
    
    public static Comparator<PSPathItem> getInstance()
    {
        if (instance == null)
            instance = new PSPathItemComparator();
        
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(PSPathItem a, PSPathItem b)
    {
        return a.getName().compareToIgnoreCase(b.getName());
    }
}
