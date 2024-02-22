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

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.PSAbstractPersistantObject;

/**
 * The summary information of a Template. This is an immutable class.
 * 
 * @author YuBingChen
 */
@XmlRootElement(name="TemplateSummary")
@JsonSerialize(as=PSTemplateSummary.class)
@JsonRootName("TemplateSummary")
public class PSTemplateSummary extends PSAbstractPersistantObject {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = -2647068336786632480L;

    protected static final int MAX_DESCRIPTION=-1;
    protected static final int MAX_SOURCE_TEMPLATE=200;
    protected static final int MAX_LABEL = 300;
    protected static final int MAX_THEME = 300;
    protected static final int MAX_PROTECTED_REGION=200;
    protected static final int MAX_DOCTYPE=1000;
    protected static final int MAX_TYPE=100;


    /**
     * The ID of the Template, initialized by constructor, never
     * <code>null</code> or modified after that.
     */
    @NotEmpty
    private String id;

    /**
     * The name of the Template, initialized by constructor, never
     * <code>null</code>, empty or modified after that.
     */
    @NotNull
    @NotEmpty
    private String name;

    /**
     * The label of the Template, initialized by constructor, may be
     * <code>null</code> or empty.
     */
    private String label;

    /**
     * The description of the Template, initialized by constructor, may be
     * <code>null</code> or empty.
     */
    private String description;
    
    /**
     * The path of thumb sized image of the Template, initialized by constructor,
     * never <code>null</code> or empty after that. Refer to
     * {@link #getImageThumbPath()} for more details.
     */
    private String imageThumbPath;
    
    /**
     * Indicates if the Template is ready only, see {@link #isReadOnly()} for 
     * more details.
     */
    private boolean isReadOnly;
    
    /**
     * The name of the source template, which was used to create this template.
     * It may be <code>null</code> or empty if it has not been properly set.
     */
    private String sourceTemplateName;

    /**
     * The type of template. Eg: NORMAL or UNASSIGNED
     * It may be <code>null</code> if it has not been properly set.
     */
    private String type;

    /**
     * See {@link #getContentMigrationVersion()}
     */
    private String contentMigrationVersion = "0";

    public PSTemplateSummary() {
        super();
    }

    public PSTemplateSummary(PSTemplate template) {
        this.contentMigrationVersion = template.getContentMigrationVersion();
        this.description = template.getDescription();
        this.id = template.getId();
        this.imageThumbPath = template.getImageThumbPath();
        this.isReadOnly = template.isReadOnly();
        this.label = template.getLabel();
        this.type = template.getType();
        this.name = template.getName();
        this.sourceTemplateName = template.getSourceTemplateName();
    }

    /**
     * Gets the ID of the Template.
     * 
     * @return the Template ID, never <code>null</code>.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the Template, which is unique in the system.
     * 
     * @return the name of the Template, never <code>null</code> or empty.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the display name of the Template.
     * 
     * @return the label, may be <code>null</code> or empty.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the description of the Template.
     * 
     * @return the description, may be <code>null</code> or empty.
     */
    public String getDescription() {
        return description;
    }

    
    
    public String getImageThumbPath() {
        return imageThumbPath;
    }

    
    public void setImageThumbPath(String imageThumbPath) {
        this.imageThumbPath = imageThumbPath;
    }

    
    public boolean isReadOnly() {
        return isReadOnly;
    }

    
    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    
    @Override
    public void setId(String id) {
        this.id = id;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public void setLabel(String label) {
        if(label != null && label.length()>MAX_LABEL)
            label = label.substring(0,MAX_LABEL);

        this.label = label;
    }

    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the name of the source template. The source template was used to 
     * create this template.
     * 
     * @return the source template name, may be <code>null</code> or empty.
     */
    public String getSourceTemplateName()
    {
       return sourceTemplateName;
    }
    
    /**
     * Sets the name of the source template. The source template was used to
     * create this template.
     * 
     * @param srcTemplate the new source template name, never <code>null</code>
     * or empty.
     */
    public void setSourceTemplateName(String srcTemplate)
    {  
       if(srcTemplate.length()>MAX_SOURCE_TEMPLATE)
           srcTemplate = srcTemplate.substring(0,MAX_SOURCE_TEMPLATE);

        this.sourceTemplateName = srcTemplate;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        if(type.length()>MAX_TYPE)
            type = type.substring(0,MAX_TYPE);

        this.type = type;
    }

    /**
     * Get the version that is incremented each time the template is saved with changes that require content migration for pages
     * using the template.
     * 
     * @return the version, 0 if no such changes have been saved.
     */
    public String getContentMigrationVersion()
    {
        return contentMigrationVersion;
    }

    /**
     * Set the content migration version.  See {@link #getContentMigrationVersion()}.
     * 
     * @param version the version to set.
     */
    public void setContentMigrationVersion(String version)
    {
        this.contentMigrationVersion = version;
    }
    
    


}
