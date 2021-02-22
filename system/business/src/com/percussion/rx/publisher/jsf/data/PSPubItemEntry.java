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
package com.percussion.rx.publisher.jsf.data;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.publisher.jsf.beans.PSPubLogBean;
import com.percussion.rx.publisher.jsf.beans.PSRuntimeNavigation;
import com.percussion.rx.publisher.jsf.nodes.PSPublishingStatusHelper;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;
import com.percussion.services.publisher.data.PSPubItem;
import com.percussion.utils.guid.IPSGuid;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A simple bean that will set the right detail item in the runtime navigation.
 * This bean knows how to navigate back and forth through the current logs.
 * <p>
 * N.B. A subset of properties is made explicitly available through getters. The 
 * names of these properties must correspond exactly to the names in
 * {@link PSPubItem} to allow sorting to work correctly. The property name
 * is used in the JSF, and the name must be the same so the HQL code in
 * {@link PSPubLogBean} correctly sets the order by clauses.
 * 
 * @author dougrand
 */
public class PSPubItemEntry
{
   /**
    * Format for elapsed time in seconds
    */
   private static final DecimalFormat ms_elapsedFormat = new DecimalFormat(
         "###,###.###s");

   /**
    * The index of this log entry.
    */
   private int m_index;
   
   /**
    * Runtime navigation, never <code>null</code> after constructor
    */
   private PSRuntimeNavigation m_nav;
   
   /**
    * The properties, setup in the {@link #initProperties()} method.
    */
   private Map<String, Object> m_properties = new HashMap<>();

   /**
    * The publishing item status, initialized by constructor, never
    * <code>null</code> after that.
    */
   private IPSPubItemStatus m_itemStatus;

   /**
    * The parent backing bean of this item log entry. Initialized by 
    * constructor, never <code>null</code> after that.
    */
   private PSPubLogBean m_parent;
   
   
   public PSPubItemEntry(PSRuntimeNavigation nav, PSPubLogBean parent,
         IPSPubItemStatus status, int index)
   {
      if (nav == null)
         throw new IllegalArgumentException("nav may not be null");
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if (status == null)
         throw new IllegalArgumentException("status may not be null");

      m_parent = parent;
      m_index = index;
      m_nav = nav;
      m_itemStatus = status;
      
      initProperties();
   }

   /**
    * Get the original item log entry.
    * @return the log entry, never <code>null</code>.
    */
   public IPSPubItemStatus getItemStatus()
   {
      return m_itemStatus;
   }

   /**
    * @return the properties, never <code>null</code>.
    */
   public Map<String, Object> getProperties()
   {
      return m_properties;
   }

   /**
    * Initialize this object from the passed data
    * 
    * @param columns the columns to extract, assumed never <code>null</code>.
    * @param results the result data, assumed never <code>null</code>.
    */
   protected void initProperties()
   {
      String value;

      // operation
      int op = m_itemStatus.getOperation().ordinal();
      if (op == Operation.PUBLISH.ordinal())
      {
         value = "publish";
      }
      else 
      {
         value = "unpublish";
      }
      m_properties.put("operation", value);

      // elapsed
      Integer elapsed = m_itemStatus.getElapsed();
      double time = elapsed != null ? elapsed.doubleValue() : 0.0;
      value = ms_elapsedFormat.format(time / 1000.0);
      m_properties.put("elapsed", value);
      
      // status
      int statusId = m_itemStatus.getStatus().ordinal();
      if (statusId == Status.SUCCESS.ordinal())
      {
         value = "success";
      }
      else if (statusId == Status.CANCELLED.ordinal())
      {
         value = "cancelled";
      }
      else if (statusId == Status.FAILURE.ordinal())
      {
         value = "failure";
      }
      else
      {
         value = "";
      }
      m_properties.put("status", value);
      
      // date
      value = DateFormat.getDateTimeInstance().format(m_itemStatus.getDate());
      m_properties.put("date", value);


      // siteFolder
      value = getSiteFolder();
      m_properties.put("siteFolder", value);
      
      // template
      value = getTemplate();
      m_properties.put("template", value);
   }

   /**
    * Gets the site folder path from the folder ID of {@link #m_itemStatus}.
    * @return the site folder if the folder id is set, or an empty string
    * if unknown.
    */
   private String getSiteFolder()
   {
      Integer folder = m_itemStatus.getFolderId();
      if (folder != null && folder != 0)
      {
         PSRequest request = PSRequest.getContextForRequest();
         PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
         try
         {
         String paths[] = proc.getItemPaths(new PSLocator(folder));
         if (paths.length == 1)
            return paths[0];
         else
            return "Error: " + "cannot find folder path for fid = " + folder;
         }
         catch (Exception e)
         {
            return "Error: " + "cannot find folder path for fid = " + folder;
         }
      }
      else
      {
         return "";
      }
   }

   /**
    * @return get the messages, may be empty but not <code>null</code>.
    */
   public List<String> getMessages()
   {
      String msg = m_itemStatus.getMessage();
      return PSPublishingStatusHelper.splitMessages(msg);
   }
   
   /**
    * @return <code>true</code> if there are messages to display
    */
   public boolean getHasMessages()
   {
      String msg = m_itemStatus.getMessage();
      return StringUtils.isNotBlank(msg);
   }

   /**
    * @return the template label for the given template id, it may be 
    *     empty if failed to get the label of the template.
    */
   private String getTemplate()
   {
      Long templateId = m_itemStatus.getTemplateId();
      if (templateId == null)
         return "";
      
      IPSAssemblyService asvc = PSAssemblyServiceLocator.getAssemblyService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid tguid = gmgr.makeGuid(templateId, PSTypeEnum.TEMPLATE);
      try
      {
         IPSAssemblyTemplate template = asvc.loadUnmodifiableTemplate(tguid);
         return template.getLabel();
      }
      catch (Exception e)
      {
         return "";
      }
   }

   /**
    * Setup reference to this bean for further viewing in detail
    * 
    * @return the outcome, never <code>null</code>
    */
   public String perform()
   {
      m_nav.setDetailItem(this);
      return "pub-runtime-log-item";
   }
   
   /**
    * Action to go to the previous item
    * @return the outcome, never <code>null</code>.
    */
   public String previous()
   {
      if (m_index > 0)
      {
         m_parent.setRowIndex(m_index - 1);
         PSPubItemEntry entry = (PSPubItemEntry)m_parent.getRowData();
         m_nav.setDetailItem(entry);
      }
      return "previous";
   }

   /**
    * Action to go to the next item
    * @return the outcome, never <code>null</code>.
    */   
   public String next()
   {

      int count = m_parent.getRowCount();
      if ((count - m_index) > 1)
      {
         m_parent.setRowIndex(m_index + 1);
         PSPubItemEntry entry = (PSPubItemEntry)m_parent.getRowData();
         m_nav.setDetailItem(entry);
      }      
      return "next";
   }
}
