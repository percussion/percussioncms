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

import com.percussion.share.service.IPSLinkableItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotNull;

/**
 * A page is the base item for all site pages.
 */
@XmlRootElement(name = "Page")
public class PSPage extends PSPageSummary implements IPSLinkableItem, IPSHtmlMetadata
{
    /**
     *  Safe to serialize
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Gets all widgets contained in this page, which does not include widgets in the underlying template.
     * @return the widgets, never <code>null</code>, may be empty.
     */
    public List<PSWidgetItem> getWidgets()
    {
        return getWidgets(null);
    }
    
    /**
     * Gets all widgets contained in this page, which does not include widgets
     * in the underlying template.
     * 
     * @param template if not <code>null</code> excludes widgets from page
     *            regions that also have the widgets on template region.
     * @return the widgets, never <code>null</code>, may be empty.
     */
    public List<PSWidgetItem> getWidgets(PSTemplate template)
    {
        List<PSWidgetItem> widgetList = new ArrayList<>();
        Map<String, List<PSWidgetItem>> tplWidgetMap = new HashMap<>();
        Set<String> tplRegIds = new HashSet<>();
        if(template != null){
            tplWidgetMap = template.getRegionTree().getRegionWidgetsMap();
            List<PSRegion> tplRegs = template.getRegionTree().getRootRegion().getAllRegions();
            for (PSRegion reg : tplRegs)
            {
                tplRegIds.add(reg.getRegionId());
            }
        }
        Map<String, List<PSWidgetItem>> widgetMap = getRegionBranches().getRegionWidgetsMap();
        for (PSRegion r : getRegionBranches().getRegions())
        {
            //don't return widgets if template has widgets in a region and page also have widgets in the same region
            //As the page widgets will not get rendered. Also don't return page widgets if the region doesn't exist
            //on the template anymore.
            if(!tplWidgetMap.keySet().contains(r.getRegionId()) && tplRegIds.contains(r.getRegionId())){
                List<PSWidgetItem> widgets = widgetMap.get(r.getRegionId());
                if (widgets != null)
                    widgetList.addAll(widgets);
            }
        }
        return widgetList;
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
    
    //@AssertValid
    public PSRegionBranches getRegionBranches()
    {
        return regionBranches;
    }

    public void setRegionBranches(PSRegionBranches pageRegionBranches)
    {
        this.regionBranches = pageRegionBranches;
    }    

    /**
    * @return the summary may be <code>null</code> or empty.
    */
   public String getSummary()
   {
      return summary;
   }

   public void setSummary(String summary)
   {
      this.summary = summary;
   }
   
   /**
    * The workflow ID associated with the page. Never <code>null</code>.
    * @return
    */
   @XmlTransient
   public Integer getWorkflowId()
   {
       return workflowId;
   }

   public void setWorkflowId(Integer workflowId)
   {
       this.workflowId = workflowId;
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
   
   public boolean isAddToRecent()
{
    return addToRecent;
}

public void setAddToRecent(boolean addToRecent)
{
    this.addToRecent = addToRecent;
}

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
     * Regions of the template that will be overrided.
     * The overrided region is a list of widgets
     * or regions,
     * never <code>null</code>, maybe empty.
     */
    @NotNull
    @AssertValid
    private PSRegionBranches regionBranches = new PSRegionBranches();    
    
    /**
     * The page summary text. May be <code>null</code> or empty.
     */
    private String summary;
    
    /**
     * The workflow ID associated to this page. Never <code>null</code>.
     */
    private Integer workflowId;
    
    /**
     * The doc type that was entered manually by user. May be <code>null</code> or empty.
     * Eg: <!DOCTYPE html>
     */
    private PSMetadataDocType docType;
    
    /**
     * If set to true, the newly created item and its template and folder will be added to the recent list.
     */
    private boolean addToRecent;
    
}
