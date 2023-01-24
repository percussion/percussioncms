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
