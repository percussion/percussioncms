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
package com.percussion.pagemanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PSResourceDefinitionGroup} is a grouping of 
 * {@link PSResourceDefinition}s.
 * 
 * The grouping is stored in an Xml file where {@link #getId()}
 * is the name of the xml file minus the extension.
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name = "Resources")
@JsonRootName("Resources")
public class PSResourceDefinitionGroup extends PSAbstractPersistantObject
{
    
    private String id;
    
    
    private List<PSFileResource> fileResources;
    
    private List<PSFolderResource> folderResources;
    
    private List<PSAssetResource> assetResources;
    

    @XmlElement(name="folder")
    public List<PSFolderResource> getFolderResources()
    {
        return folderResources;
    }
    
    public void setFolderResources(List<PSFolderResource> folderResources)
    {
        this.folderResources = folderResources;
    }

    @XmlElement(name="asset")
    public List<PSAssetResource> getAssetResources()
    {
        return assetResources;
    }

    public void setAssetResources(List<PSAssetResource> assetResources)
    {
        this.assetResources = assetResources;
    }

    @XmlElement(name="file")
    public List<PSFileResource> getFileResources()
    {
        return fileResources;
    }

    public void setFileResources(List<PSFileResource> fileResources)
    {
        this.fileResources = fileResources;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }
    
    public static class PSResourceDependency {
        
        private String dependeeId;

        @XmlAttribute(name="refid")
        public String getDependeeId()
        {
            return dependeeId;
        }

        public void setDependeeId(String dependeeId)
        {
            this.dependeeId = dependeeId;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("PSResourceDependency{");
            sb.append("dependeeId='").append(dependeeId).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
    
    /**
     * Base class for resource definitions.
     * {@link #getUniqueId()} is the combination of the resources
     * local {@link #getId()} and the {@link PSResourceDefinitionGroup} group id.
     * @author adamgent
     *
     */
    public static abstract class PSResourceDefinition {
        
        private String id;
        /**
         * Transient. Filled by the service.
         */
        private String groupId;
        /**
         * Transient. Filled by the service.
         */
        private String uniqueId;
        
        private List<PSResourceDependency> dependencies = new ArrayList<>();

        @XmlElement(name="dependency")
        public List<PSResourceDependency> getDependencies()
        {
            return dependencies;
        }

        public void setDependencies(List<PSResourceDependency> dependencies)
        {
            this.dependencies = dependencies;
        }

        @XmlTransient
        public abstract PSResourceDefinitionType getResourceType();
        
        @XmlTransient
        public String getGroupId()
        {
            return groupId;
        }

        public void setGroupId(String groupId)
        {
            this.groupId = groupId;
        }

        @XmlTransient
        public String getUniqueId()
        {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId)
        {
            this.uniqueId = uniqueId;
        }

        @XmlAttribute(name="id", required=true)
        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }
        
        
        /**
         * Visitor pattern
         * @param visitor never <code>null</code>.
         */
        public abstract void accept(IPSResourceDefinitionVisitor visitor) throws PSDataServiceException;
        
    }
    
    public static enum PSResourceDefinitionType {
        ASSET,
        FILE,
        FOLDER,
        CSS,
        JAVASCRIPT,
        THEME
    }
    
    
    /**
     * A file resource is a file that needs to be copied to the site.
     * @author adamgent
     *
     */
    public static class PSFileResource extends PSResourceDefinition {
        
        
        private String file;
        
        private PSFileResourceType type;
        
        private PSFileResourceContext context;

        private PSFileResourcePlacement placement;

        @Override
        public PSResourceDefinitionType getResourceType()
        {
            return PSResourceDefinitionType.FILE;
        }


        @XmlAttribute(name="path", required=true)
        public String getFile()
        {
            return file;
        }


        public void setFile(String file)
        {
            this.file = file;
        }


        @XmlAttribute(required=false)
        public PSFileResourceType getType()
        {
            return type;
        }


        public void setType(PSFileResourceType type)
        {
            this.type = type;
        }


        @XmlAttribute(required=false)
        public PSFileResourceContext getContext()
        {
            return context;
        }


        @XmlAttribute(required=false)
        public PSFileResourcePlacement getPlacement(){return placement;}

        public void setPlacement(PSFileResourcePlacement placement){
            this.placement = placement;
        }

        public void setContext(PSFileResourceContext context)
        {
            this.context = context;
        }

        public static enum PSFileResourceType {
            css,javascript
        }


        public static enum PSFileResourceContext {
            PUBLISH,PREVIEW
        }

        public static enum PSFileResourcePlacement{
            head,body_begin,body_end
        }
        
        @Override
        public void accept(IPSResourceDefinitionVisitor visitor)
        {
            visitor.visit(this);
        }

    }
    
    
    /**
     * A folder resource is a static folder that needs
     * to be copied onto the published site.
     * @author adamgent
     *
     */
    public static class PSFolderResource extends PSResourceDefinition {
        
        
        private String path;

        @Override
        public PSResourceDefinitionType getResourceType()
        {
            return PSResourceDefinitionType.FOLDER;
        }
        
        @XmlAttribute(required=true)
        public String getPath()
        {
            return path;
        }

        public void setPath(String path)
        {
            this.path = path;
        }
        
        @Override
        public void accept(IPSResourceDefinitionVisitor visitor)
        {
            visitor.visit(this);
        }
        
    }
    
    /**
     * 
     * An asset resource definition defines
     * how the asset should be published which includes:
     * 
     * <ul>
     * <li>The publish location</li>
     * <li>The publish url</li>
     * <li>The mimetype</li>
     * <li>A binary field if its binary</li>
     * <li>Template code if its text</li>
     * </ul>
     *  
     * @author adamgent
     *
     */
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "", propOrder = {
            "linkAndLocationsScript",
            "modelScript",
            "viewScript"
        })    
    public static class PSAssetResource extends PSResourceDefinition {
        
        private String contentType;
        
        private String legacyTemplate;
        
        private boolean primary = false;

        private PSLinkAndLocationsScript linkAndLocationsScript;
        
        private PSModelScript modelScript;
        
        private PSViewScriptBlock viewScript;
        

        @Override
        public PSResourceDefinitionType getResourceType()
        {
            return PSResourceDefinitionType.ASSET;
        }
        
        @XmlAttribute(required=true)
        public String getContentType()
        {
            return contentType;
        }

        public void setContentType(String contentType)
        {
            this.contentType = contentType;
        }

        @XmlAttribute
        public boolean isPrimary()
        {
            return primary;
        }

        public void setPrimary(boolean primary)
        {
            this.primary = primary;
        }

        @XmlAttribute
        public String getLegacyTemplate()
        {
            return legacyTemplate;
        }

        public void setLegacyTemplate(String legacyTemplate)
        {
            this.legacyTemplate = legacyTemplate;
        }
        
        @XmlElement(name = "linkAndLocations")
        public PSLinkAndLocationsScript getLinkAndLocationsScript()
        {
            return linkAndLocationsScript;
        }

        public void setLinkAndLocationsScript(PSLinkAndLocationsScript linkAndLocations)
        {
            this.linkAndLocationsScript = linkAndLocations;
        }

        @XmlElement(name = "model")
        public PSModelScript getModelScript()
        {
            return modelScript;
        }

        public void setModelScript(PSModelScript code)
        {
            this.modelScript = code;
        }

        @XmlElement(name = "view")
        public PSViewScriptBlock getViewScript()
        {
            return viewScript;
        }

        public void setViewScript(PSViewScriptBlock content)
        {
            this.viewScript = content;
        }
        
        
        @Override
        public void accept(IPSResourceDefinitionVisitor visitor) throws PSDataServiceException {
            visitor.visit(this);
        }   
        
        
    }
    
    /**
     * 
     * A code block usually jexl.
     * This is excuted before the template code.
     * @author adamgent
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "PSAbstractScript", propOrder = {
        "value"
    })
    public static abstract class PSAbstractScript {
        @XmlValue
        protected String value;
        
        @XmlAttribute
        protected String type;

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }
                
    }
    
    public static class PSModelScript extends PSAbstractScript {
    }
    
    public static class PSLinkAndLocationsScript extends PSAbstractScript {
    }
    
    /**
     * 
     * A template code block usually velocity.
     * @author adamgent
     *
     */
    public static class PSViewScriptBlock extends PSAbstractScript {
                
    }

    private static final long serialVersionUID = 3594335939747382278L;

}
