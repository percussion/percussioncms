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

package com.percussion.redirect.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
@JsonRootName("data")
public class PSRedirectValidationData
{
    public String getFromPath()
    {
        return fromPath;
    }
    public void setFromPath(String fromPath)
    {
        this.fromPath = fromPath;
    }
    public String getToPath()
    {
        return toPath;
    }
    public void setToPath(String toPath)
    {
        this.toPath = toPath;
    }
    public RedirectPathType getType()
    {
        return type;
    }
    public void setType(RedirectPathType type)
    {
        this.type = type;
    }
    private String fromPath;
    private String toPath;
    private RedirectPathType type;
    
    public enum RedirectPathType{
        page, folder, section, site
    }
}
