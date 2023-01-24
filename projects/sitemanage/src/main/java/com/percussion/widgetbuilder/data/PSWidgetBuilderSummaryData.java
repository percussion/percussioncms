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
package com.percussion.widgetbuilder.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSAbstractPersistantObject;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Lightweight version of {@link PSWidgetBuilderDefinitionData} for serialization of 
 * summary data only.
 * 
 * @author JaySeletz
 *
 */
@XmlRootElement(name="WidgetBuilderSummaryData")
@JsonRootName("WidgetBuilderSummaryData")
public class PSWidgetBuilderSummaryData extends PSAbstractPersistantObject
{

    private long widgetId;
    private String prefix;
    private String author;
    private String label;
    private String publisherUrl;
    private String description;
    private String version;
    private String toolTipMessage;
    private String widgetTrayCustomizedIconPath;
    private boolean responsive;

    private static final long serialVersionUID = -1L;
    
    /**
     * 
     */
    public PSWidgetBuilderSummaryData()
    {
        super();
    }

    /**
     * Copy ctor
     * 
     * @param src The summary to copy from, may not be <code>null</code>.
     */
    public PSWidgetBuilderSummaryData(PSWidgetBuilderSummaryData src)
    {
        Validate.notNull(src);
        
        author = src.author;
        description = src.description;
        label = src.label;
        prefix = src.prefix;
        publisherUrl = src.publisherUrl;
        version = src.version;
        widgetId = src.widgetId;
        responsive = src.responsive;
        toolTipMessage = src.toolTipMessage;
        widgetTrayCustomizedIconPath = src.widgetTrayCustomizedIconPath;

    }
    
    /**
     * Create from dao object
     * 
     * @param dao The dao object to copy from, not <code>null</code>.
     */
    public PSWidgetBuilderSummaryData(PSWidgetBuilderDefinition dao)
    {
        Validate.notNull(dao);
        setAuthor(dao.getAuthor());
        setDescription(dao.getDescription());
        setLabel(dao.getLabel());
        setPrefix(dao.getPrefix());
        setPublisherUrl(dao.getPublisherUrl());
        setVersion(dao.getVersion());
        setId(Long.toString(dao.getWidgetBuilderDefinitionId()));
        setResponsive(dao.isResponsive());
        setWidgetTrayCustomizedIconPath(dao.getWidgetTrayCustomizedIconPath());
        setToolTipMessage(dao.getToolTipMessage());
    }

    public String getToolTipMessage() {
        return toolTipMessage;
    }

    public void setToolTipMessage(String toolTipMessage) {
        this.toolTipMessage = toolTipMessage;
    }

    public String getWidgetTrayCustomizedIconPath() {
        return widgetTrayCustomizedIconPath;
    }

    public void setWidgetTrayCustomizedIconPath(String widgetTrayCustomizedIconPath) {
        this.widgetTrayCustomizedIconPath = widgetTrayCustomizedIconPath;
    }

    /**
     * @return the prefix
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    /**
     * @return the label
     */
    public String getLabel()
    {
        return label;
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
     * @param label the label to set
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * @return the publisherUrl
     */
    public String getPublisherUrl()
    {
        return publisherUrl;
    }

    /**
     * @param publisherUrl the publisherUrl to set
     */
    public void setPublisherUrl(String publisherUrl)
    {
        this.publisherUrl = publisherUrl;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    public boolean isResponsive()
    {
        return responsive;
    }

    public void setResponsive(boolean responsive)
    {
        this.responsive = responsive;
    }

    public long getWidgetId()
    {
        return this.widgetId;
    }

    public void setWidgetId(long id)
    {
        this.widgetId = id;
        
    }

    @Override
    public String getId()
    {
        return Long.toString(getWidgetId());
    }

    @Override
    public void setId(String id)
    {
        setWidgetId(Long.parseLong(id));
    }

}
