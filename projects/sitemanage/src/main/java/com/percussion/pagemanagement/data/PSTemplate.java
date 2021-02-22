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
package com.percussion.pagemanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.AssertValid;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notEmpty;

/**
 * A template is an instance of a layout, which may contain one or more widgets.
 * 
 * @author YuBingChen
 */
@XmlRootElement(name = "Template")
@JsonRootName("Template")
public class PSTemplate extends PSTemplateSummary implements IPSHtmlMetadata
{
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the HTML header fragment in the read only Template.
     */
    public final static String HTML_HEADER = "HTML_HEADER";

    /**
     * The name of the CSS mark-up fragment in the read only Template.
     */
    public final static String CSS_MARKUP = "CSS_MARKUP";

    /**
     * The name of the body mark-up fragment in the read only Template.
     */
    public final static String BODY_MARKUP = "BODY_MARKUP";

    /**
     * The bodyMarkup field value, may be <code>null</code> or empty.
     */
    private String bodyMarkup;

    /**
     * The HTML header property, may be <code>null</code> or empty.
     */
    private String htmlHeader;

    /**
     * The CSS styles for regions, may be <code>null</code> or empty.
     */
    private String cssRegion;

    /**
     * The CSS styles that may override the ones defined in else where, such
     * as in theme, it may be <code>null</code> or empty.
     */
    private String cssOverride;
    
    /**
     * The name of the theme that is used by the template. It may be
     * <code>null</code> or empty.
     */
    private String theme;
    
    /**
     * Containes the region tree for the template.
     * Widgets are associated in the region tree.
     */
    @AssertValid
    private PSRegionTree regionTree;
    
    /**
     * Additional HTML that will go in the &lt;head&gt;&lt;/head&gt;
     */
    private String additionalHeadContent;

    /**
     * The block of text intent to be used within the HTML <head> tag.
     */
    private String afterBodyStartContent;

    /**
     * The block of text intent to be used right before the HTML </body> tag.
     */
    private String beforeBodyCloseContent;

    /**
     * The protected region name used to hide content in the delivery published pages
     * Eg: header
     */
    private String protectedRegion;
    
    /**
     * The text to show when instead of the code in the protected region when user is not logged-in in the delivery
     * Eg: You're not authorized to see this content
     */
    private String protectedRegionText;
    
    /**
     * The server version for the exported template
     * <code>null</code> or empty.
     */
    private String serverVersion;
    
    /**
     * The doc type that was entered manually by user. May be <code>null</code> or empty.
     * Eg: <!DOCTYPE html>
     */
    private PSMetadataDocType docType;

    /**
     * The type of template. Eg: NORMAL or UNASSIGNED. It may be
     * <code>null</code> or empty.
     */
    private String type;

    /**
     * Gets the region tree for the template.
     * Widgets are associated in the region tree.
     * 
     * @return maybe <code>null</code>.
     * 
     */
    public PSRegionTree getRegionTree()
    {
        return regionTree;
    }

    public void setRegionTree(PSRegionTree templateRegionTree)
    {
        this.regionTree = templateRegionTree;
    }

    /**
     * Gets the body mark-up content of the Template.
     * 
     * @return the body mark-up content, may be <code>null</code> or empty.
     */
    public String getBodyMarkup()
    {
        return bodyMarkup;
    }

    /**
     * Sets the body mark-up.
     * 
     * @param bodyMarkup the new body mark-up content, may be <code>null</code> or
     *            empty.
     */
    public void setBodyMarkup(String bodyMarkup)
    {
        this.bodyMarkup = bodyMarkup;
    }

    /**
     * Gets the HTML header property. It is used for the head section of an HTML
     * page.
     * 
     * @return the HTML header property, may be <code>null</code> or empty.
     */
    public String getHtmlHeader()
    {
        return htmlHeader;
    }

    /**
     * Sets the HTML header property.
     * 
     * @param htmlHeader the new HTML header, may be <code>null</code> or empty.
     */
    public void setHtmlHeader(String htmlHeader)
    {
        this.htmlHeader = htmlHeader;
    }

    /**
     * Gets the CSS styles for the template regions.
     * 
     * @return the CSS styles of the regions, may be <code>null</code> or empty.
     */
    public String getCssRegion()
    {
        return cssRegion;
    }

    /**
     * Sets the CSS styles for the template regions.
     * 
     * @param cssRegion the new CSS styles for regions defined in the template, 
     * may be <code>null</code> or empty.
     */
    public void setCssRegion(String cssRegion)
    {
        this.cssRegion = cssRegion;
    }

    /**
     * Gets the CSS override property, which contains the CSS styles that may
     * override the ones defined else where, such as in the theme.
     * 
     * @return the CSS override property, may be <code>null</code> or empty.
     */
    public String getCssOverride()
    {
        return this.cssOverride;
    }
    
    /**
     * Sets the CSS override property.
     * 
     * @param cssOverride the new CSS override property, it may be blank.
     */
    public void setCssOverride(String cssOverride)
    {
        this.cssOverride = cssOverride;
    }
    
    /**
     * Gets the name of the theme used by this template.
     * 
     * @return the theme name, may be blank.
     */
    public String getTheme()
    {
        return this.theme;
    }
    
    /**
     * Sets the Theme used by the template.
     * 
     * @param theme the new name of the Theme, may be blank.
     */
    public void setTheme(String theme)
    {
        this.theme = theme;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getAdditionalHeadContent()
    {
        return additionalHeadContent;
    }


    /**
     * {@inheritDoc}
     */
    public void setAdditionalHeadContent(String additionalHeadContent)
    {
        this.additionalHeadContent = additionalHeadContent;
    }

    /**
     * {@inheritDoc}
     */
    public String getAfterBodyStartContent()
    {
        return afterBodyStartContent;
    }

    /**
     * {@inheritDoc}
     */
    public void setAfterBodyStartContent(String header)
    {
        this.afterBodyStartContent = header;
    }


    /**
     * {@inheritDoc}
     */
    public String getBeforeBodyCloseContent()
    {
        return beforeBodyCloseContent;
    }


    /**
     * {@inheritDoc}
     */
    public void setBeforeBodyCloseContent(String footer)
    {
        this.beforeBodyCloseContent = footer;
    }
    
    /**
     * Gets the server version used by this template.
     * 
     * @return the server version, may be blank.
     */
    public String getServerVersion()
    {
        return this.serverVersion;
    }
    
    /**
     * Sets the server version used by the template.
     * 
     * @param serverVersion the server version, may be blank.
     */
    public void setServerVersion(String serverVersion)
    {
        this.serverVersion = serverVersion;
    }

   /**
    * {@inheritDoc}
    */
    public String getProtectedRegion()
    {
        return this.protectedRegion;
    }
    
   /**
    * {@inheritDoc}
    */
    public void setProtectedRegion(String protectedRegion)
    {
        this.protectedRegion = protectedRegion;
    }

   /**
    * {@inheritDoc}
    */
    public String getProtectedRegionText()
    {
        return this.protectedRegionText;
    }
    
   /**
    * {@inheritDoc}
    */
    public void setProtectedRegionText(String protectedRegionText)
    {
        this.protectedRegionText = protectedRegionText;
    }
    
    /**
     * {@inheritDoc}
     */
    public PSMetadataDocType getDocType()
    {
        return docType;
    }

    /**
     * {@inheritDoc}
     */
    public void setDocType(PSMetadataDocType docType)
    {
        this.docType = docType;
    }
    
    @Override
    public PSTemplate clone()
    {
        return (PSTemplate) super.clone();
    }
    
    /**
     * Determines if this template has an instance of the specified widget.
     * 
     * @param definitionId of the widget, never blank.
     * 
     * @return <code>true</code> if the template has an instance of the widget, <code>false</code> otherwise.
     */
    public boolean hasWidget(String definitionId)
    {
        notEmpty(definitionId, "definitionId may not be empty");
        
        List<PSWidgetItem> widgets = getWidgets();
        for (PSWidgetItem widget : widgets)
        {
            if (widget.getDefinitionId().equalsIgnoreCase(definitionId))
            {
                return true;
            }
        }
            
        return false;
    }
    
    /**
     * Gets all widgets contained in this template.
     * 
     * @return list of widget items, never <code>null</code>, may be empty.
     */
    public List<PSWidgetItem> getWidgets()
    {
        ArrayList<PSWidgetItem> widgetList = new ArrayList<>();

        PSRegionTree treeRegion = getRegionTree();
        if (treeRegion != null)
        {
            Map<String, List<PSWidgetItem>> widgetMap = treeRegion.getRegionWidgetsMap();
            Set<String> keys = widgetMap.keySet();

            for (String key : keys)
            {
                widgetList.addAll(widgetMap.get(key));
            }
        }
        
        return widgetList;
    }
    
    /**
     * Gets the type of the template.
     * Possible values are NORMAL or UNASSIGNED.
     * 
     * @return the string with the type of template. May be <code>null</code>.
     */
    public String getType()
    {
        return type;
    }
    
    /**
     * Sets the type of the template.
     * Possible values are NORMAL or UNASSIGNED.
     * 
     * @param type - the type of template
     */
    public void setType(String type)
    {
        this.type = type;
    }
    
    /**
     * Public enum for the templates type.
     * Possible values are NORMAL and UNASSIGNED
     * 
     */
    public enum PSTemplateTypeEnum
    {
        NORMAL("NORMAL"),
        UNASSIGNED("UNASSIGNED");

        private String label;

        private PSTemplateTypeEnum(String label)
        {
            this.label = label;
        }

        public String getLabel()
        {
            return label;
        }

        public static PSTemplateTypeEnum getEnum(String label)
        {
            for (PSTemplateTypeEnum v : values())
            {
                if (v.getLabel().equals(label))
                {
                    return v;
                }
            }

            return null;
        }
    }
}

