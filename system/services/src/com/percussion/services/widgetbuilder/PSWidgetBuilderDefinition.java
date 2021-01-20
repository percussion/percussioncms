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

package com.percussion.services.widgetbuilder;

import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 
 * @author matthewernewein
 * 
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
region = "PSWidgetBuilderDefinition")
@Table(name = "PSX_WIDGETBUILDERDEFINITION")
public class PSWidgetBuilderDefinition extends PSAbstractDataObject

{
    @Id
    @Column(name = "WIDGETBUILDERDEFINITIONID")  
    private long widgetBuilderDefinitionId = -1L;

    @Basic
    @Column(name = "PREFIX", nullable=true)
    private String prefix;

    @Basic
    @Column(name="AUTHOR", nullable=true)
    private String author;

   @Basic
    @Column(name = "LABEL", nullable=true)
    private String label;

    @Basic
    @Column(name = "PUBLISHERURL", nullable=true)
    private String publisherUrl;

    @Basic
    @Column(name = "DESCRIPTION", nullable=true)
    private String description;

    @Basic
    @Column(name = "VERSION", nullable=true)
    private String version;
    
    @Basic
    @Column(name = "FIELDS", nullable=true)
    private String fields = "";    
    
    @Basic
    @Column(name = "WIDGET_HTML", nullable=true)
    private String widgetHtml = "";    

    @Basic
    @Column(name = "CSS_FILES", nullable=true)
    private String cssFiles = "";    
    
    @Basic
    @Column(name = "JS_FILES", nullable=true)
    private String jsFiles = "";    
    
    @Basic
    @Column(name="IS_RESPONSIVE", nullable=true)
    private String isResponsive;

    @Basic
    @Column(name="WIDGET_TRAY_CUSTOMIZED_ICON_PATH", nullable=true)
    private String widgetTrayCustomizedIconPath;

    @Basic
    @Column(name="TOOLTIP_MESSAGE", nullable=true)
    private String toolTipMessage;

    public String getWidgetTrayCustomizedIconPath() {
        return widgetTrayCustomizedIconPath;
    }

    public void setWidgetTrayCustomizedIconPath(String widgetTrayCustomizedIconPath) {
        this.widgetTrayCustomizedIconPath = widgetTrayCustomizedIconPath;
    }

    public String getToolTipMessage() {
        return toolTipMessage;
    }

    public void setToolTipMessage(String toolTipMessage) {
        this.toolTipMessage = toolTipMessage;
    }

    /**
     * @return the widgetBuilderDefinitionId
     */
    public long getWidgetBuilderDefinitionId()
    {
        return widgetBuilderDefinitionId;
    }

    /**
     * @param widgetBuilderDefinitionId the widgetBuilderDefinitionId to set
     */
    public void setWidgetBuilderDefinitionId(long widgetBuilderDefinitionId)
    {
        this.widgetBuilderDefinitionId = widgetBuilderDefinitionId;
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
      return "y".equals(isResponsive);
   }

   public void setResponsive(boolean responsive)
   {
      this.isResponsive = responsive ? "y" : "n";
   }

   /**
    * Get the string that represents the collection of fields 
    * 
    * @return The field data, not <code>null</code>, may be empty.
    */
   public String getFields()
   {
      return fields == null ? "" : fields;
   }

   /**
    * Set the string that represents the collection of fields
    * 
    * @param fields The field data, not <code>null</code>, may be empty.
    */
   public void setFields(String fields)
   {
      Validate.notEmpty(fields);
      this.fields = fields;
   }

   /**
    * Get the html that renders the widget
    * 
    * @return The html, may be <code>null<code/> or empty
    */
   public String getWidgetHtml()
   {
      return widgetHtml;
   }

   /**
    * Set the html that renders the widget.
    * 
    * @param widgetHtml The html, may be <code>null<code/> or empty
    */
   public void setWidgetHtml(String widgetHtml)
   {
      this.widgetHtml = widgetHtml;
   }

   /**
    * Get the string that represents the collection of css files 
    * 
    * @return The file data, not <code>null</code>, may be empty.
    */
   public String getCssFiles()
   {
      return cssFiles == null ? "" : cssFiles;
   }

   /**
    * Set the string that represents the collection of css files
    * 
    * @param cssFiles The file data, not <code>null</code>, may be empty.
    */
   public void setCssFiles(String cssFiles)
   {
      Validate.notEmpty(cssFiles);
      this.cssFiles = cssFiles;
   }

   /**
    * Get the string that represents the collection of js files 
    * 
    * @return The file data, not <code>null</code>, may be empty.
    */
   public String getJsFiles()
   {
      return jsFiles == null ? "" : jsFiles;
   }

   /**
    * Set the string that represents the collection of js files
    * 
    * @param jsFiles The file data, not <code>null</code>, may be empty.
    */
   public void setJsFiles(String jsFiles)
   {
      Validate.notEmpty(jsFiles);
      this.jsFiles = jsFiles;
   }
    
}
