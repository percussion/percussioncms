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
package com.percussion.foldermanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Due to some limitations in Apache CXF, this class is used to wrap a {@link List}
 * of {@link PSFolderItem} objects.
 * 
 * @author miltonpividori
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PSFolders extends PSAbstractDataObject
{
    @XmlElement(name = "child")
    private List<PSFolderItem> children;
    
    public PSFolders()
    {
        super();
    }

    /**
     * @param children
     */
    public PSFolders(List<PSFolderItem> children)
    {
        super();
        this.children = children;
    }

    public List<PSFolderItem> getChildren()
    {
        if(children == null)
        {
            return new ArrayList<>();
        }
        
        return children;
    }

    public void setChildren(List<PSFolderItem> children)
    {
        this.children = children;
    }
}
