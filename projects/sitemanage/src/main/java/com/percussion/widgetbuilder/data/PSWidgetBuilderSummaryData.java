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
