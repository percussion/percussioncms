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
package com.percussion.sitemanage.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * The section node contains summary information of a section and all direct
 * child section nodes. This can be used to construct a tree of sections for
 * a specific site.
 *
 * @author yubingchen
 */
@XmlRootElement(name = "SectionNode")
@JsonRootName("SectionNode")
public class PSSectionNode extends PSAbstractPersistantObject
{
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;


    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.share.data.PSAbstractPersistantObject#getId()
     */
    @Override
    @XmlElement
    public String getId()
    {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.share.data.PSAbstractPersistantObject#setId(java.io.Serializable
     * )
     */
    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Gets the title of the section. It is navigation title of the navigation
     * node and the link title of the landing page of the node.
     * 
     * @return the title of the section. It should not be blank for a properly
     * configured section.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the title of the section.
     * 
     * @param title the new title. It should not be blank for a valid section. 
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the section type
     */
    public PSSectionTypeEnum getSectionType()
    {
       return sectionType;
    }

    /**
     * @param sectionType to set, if <code>null</code> set to {@link PSSectionTypeEnum#section}}
     */
    public void setSectionType(PSSectionTypeEnum sectionType)
    {
       if(sectionType == null)
    	   sectionType = PSSectionTypeEnum.section;
       this.sectionType = sectionType;
    }

    /**
     * Gets all (direct) child nodes.
     * 
     * @return child nodes, never <code>null</code>, but may be empty.
     */
    @XmlElementWrapper(name = "childNodes")
    @XmlElements ({@XmlElement(name="SectionNode", type=PSSectionNode.class)})
    public List<PSSectionNode> getChildNodes()
    {
        return childNodes;
    }
    
    /**
     * Sets direct child nodes.
     * 
     * @param nodes the new list of child nodes, it may be <code>null</code>,
     * which will be treated as empty list.
     */
    public void setChildNodes(List<PSSectionNode> nodes)
    {
        if (nodes == null)
        {
            childNodes.clear();
        }
        else
        {
            childNodes = nodes;
        }
    }

    public boolean isRequiresLogin()
    {
        return requiresLogin;
    }

    public void setRequiresLogin(boolean requiresLogin)
    {
        this.requiresLogin = requiresLogin;
    }

    public String getAllowAccessTo()
    {
        return allowAccessTo;
    }

    public void setAllowAccessTo(String allowAccessTo)
    {
        this.allowAccessTo = allowAccessTo;
    }
    
    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }
    
    /**
     * A list of direct child nodes, may be empty, never <code>null</code>.
     */
    private List<PSSectionNode> childNodes = new ArrayList<>();
    
    /**
     * The navigation title. It is also the link title of the landing page.
     */
    private String title;

    /**
     * The link text of the section.
     */
    private String folderPath;

    /**
     * The string representation of the guid of the navon item of the section.
     */
    private String id;
    
    private PSSectionTypeEnum sectionType = PSSectionTypeEnum.section; 
    
    /**
     * Field to note if the section requires login.
     */
    private boolean requiresLogin;
    
    /**
     * Field to save the groups that are allowed to enter the section.
     */
    private String allowAccessTo;
}
