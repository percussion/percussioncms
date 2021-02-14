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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * A simple wrapper around a string class to allow for proper handling of non-ascii characters when used with CXF
 * serialization.
 */
@JsonRootName(value = "psstring")
public class PSStringWrapper
{
    public PSStringWrapper()
    {
        //no arg constructor
    }

    public PSStringWrapper(String value)
    {
        this.value = value;
    }
    /**
     * @return the value of the wrapped string, may be <code>null</code>.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets the value to be wrapped.
     * 
     * @param value of the string. 
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    private String value;

    private static final long serialVersionUID = -7646223863119728949L;
}
