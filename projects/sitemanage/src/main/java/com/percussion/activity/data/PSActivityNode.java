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
package com.percussion.activity.data;

import java.util.Collections;
import java.util.List;

/**
 * Name/path container for activity which also includes the content types of the active items.
 */
public class PSActivityNode
{
    String siteName;
    String name;
    String path;
    List<String> contentTypes;
    
    public PSActivityNode(String siteName, String name, String path, List<String> contentTypes)
    {
        this.siteName = siteName;
        this.name = name;
        this.path = path;
        this.contentTypes = contentTypes;
    }
    
    public PSActivityNode(String siteName, String name, String path, String contentType)
    {
        this(siteName, name, path, Collections.singletonList(contentType));
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return the contentTypes
     */
    public List<String> getContentTypes()
    {
        return contentTypes;
    }

    /**
     * @param contentTypes the contentTypes to set
     */
    public void setContentTypes(List<String> contentTypes)
    {
        this.contentTypes = contentTypes;
    }

    /**
     * @return the siteName
     */
    public String getSiteName()
    {
        return siteName;
    }

    /**
     * @param siteName the siteName to set
     */
    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

}
