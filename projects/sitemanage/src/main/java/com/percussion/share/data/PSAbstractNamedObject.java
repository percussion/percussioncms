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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.data;

import static org.apache.commons.lang.StringUtils.isBlank;


import java.text.Collator;

/**
 * The base class for all named data objects.  All named data objects should extend this class or some derivative.
 */
public abstract class PSAbstractNamedObject extends PSAbstractDataObject implements Comparable<PSAbstractNamedObject>
{
    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * The name that uniquely identifies the object.
     * 
     * @return should not be <code>null</code> or empty 
     *    unless the object is not finished being processed.
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Determines if the specified name is valid for this object.  By default, a valid name is not blank.
     * 
     * @param name
     * @return <code>true</code> if the name is valid, <code>false</code> otherwise.
     */
    protected boolean isValidName(String name)
    {
        return !isBlank(name);
    }
    
    public int compareTo(PSAbstractNamedObject o)
    {
        return Collator.getInstance().compare(this.getName(), o.getName());
    }
}
