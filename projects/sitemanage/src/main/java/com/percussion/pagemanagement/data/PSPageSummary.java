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

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.PSDataItemSummarySingleFolderPath;

import java.util.Collection;

/**
 * The base class for all page related classes. 
 *
 * @author YuBingChen
 * @author adamgent
 */
public class PSPageSummary extends PSDataItemSummarySingleFolderPath
{

    /**
     *  Safe to serialize
     */
    private static final long serialVersionUID = 3197862964060713693L;

    /**
     * The title of the Page.
     */
    @NotNull
    @NotBlank
    private String title;

    /**
     * The ID of the template used to render the Page.
     */
    @NotNull
    @NotBlank
    private String templateId;

    /**
     * The link title for the Page.
     */
    @NotNull
    @NotBlank
    private String linkTitle;
    
    /**
     * The page meta tag noindex.
     */
    private String noindex;

    /**
     * The author of the page.
     */
    private String author;
    
    /**
     * The page tags.
     */
    private Collection<String> tags;
    
    
    /**
     * The migration version of the template this page was last saved with
     */
    private String templateContentMigrationVersion = "0";
    
    private boolean migrationEmptyWidgetFlag = false;
    
    /**
     * Checks the meta tag noindex of the Page.  If set to "true" noindex meta tag 
     * will be added to page.
     * 
     * @return the noindex
     */
    public String getNoindex()
    {
        return noindex;
    }

    /**
     * Sets the meta tag noindex of the Page.  If set to "true" noindex meta tag 
     * will be added to page.
     * 
     * @param noindex
     */
    public void setNoindex(String noindex)
    {
        this.noindex = noindex;
    }
    
    /**
     * The page meta tag description.
     */
    private String description;

    /**
     * Gets the meta tag description of the Page.
     * 
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the meta tag description of the Page.
     * 
     * @param description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Gets the ID of the template used to render the Page.
     * 
     * @return the template ID, never <code>null</code> or empty.
     */
    public String getTemplateId()
    {
        return templateId;
    }

    /**
     * Sets the ID of the template used to render the Page.
     * 
     * @param templateId the template ID, never <code>null</code> or empty.
     */
    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }

    /**
     * Gets the title of the Page.
     *  
     * @return the title, never <code>null</code> or empty.
     */
    public String getTitle()
    {
        return title;
    }
 
    /**
     * @return the link title, never <code>null</code> or empty.
     */
    public String getLinkTitle()
    {
        return linkTitle;
    }

    /**
     * Sets the title of the Page.
     * 
     * @param title of the Page, never <code>null</code> or empty.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Sets the link title for the page.
     * @param linkTitle the link title for the page, never
     * <code>null</code> or empty.
     */
    public void setLinkTitle(String linkTitle)
    {
        this.linkTitle = linkTitle;
    }
 
    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @return the tags for the page
     */
    public Collection<String> getTags()
    {
        return tags;
    }

    /**
     * @param tags the page tags to set
     */
    public void setTags(Collection<String> tags)
    {
        this.tags = tags;
    }

    /**
     * Get the content migration version this page was last saved with
     * 
     * @return the version, "0" if the page has never had 
     * content migration applied to it. 
     */
    public String getTemplateContentMigrationVersion()
    {
        return templateContentMigrationVersion;
    }

    /**
     * Set the content migration version, see {@link #getTemplateContentMigrationVersion()}
     *
     * @param version the version to set, not <code>null</code>, non-numeric values are
     * ignored.
     */
    public void setTemplateContentMigrationVersion(String version)
    {
        
        this.templateContentMigrationVersion = version;
    }

    /**
     * Flag to indicate whether content migration failed to migrate content into all the widgets are not.
     * @return true if the migration leaves an empty widget otherwise false.
     */
    public boolean isMigrationEmptyWidgetFlag()
    {
        return migrationEmptyWidgetFlag;
    }

    /**
     * @param migrationEmptyWidgetFlag Flag to indicate whether content migration failed to migrate content into all the widgets are not.
     */
    public void setMigrationEmptyWidgetFlag(boolean migrationEmptyWidgetFlag)
    {
        this.migrationEmptyWidgetFlag = migrationEmptyWidgetFlag;
    }
    
}
