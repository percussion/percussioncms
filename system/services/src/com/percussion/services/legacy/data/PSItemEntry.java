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
package com.percussion.services.legacy.data;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.services.legacy.IPSItemEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * This class contains the skeleton information of an item, which is cached
 * by {@link PSItemSummaryCache}.
 */
public class PSItemEntry implements IPSItemEntry
{
   /**
    * Constructs an item entry object from the minimum supplied info. It simply
    * calls
    * {@link PSItemEntry(int, String, int, int, int, String, Date, Date, Date, int, int)
    * PSItemEntry(int, String, int, int, int null, null, null, null, -1, -1)}
    * 
    * @param contentId the content id of the item.
    * @param name the name of the item, which is also the <code>sys_title</code>
    *           of the item. It should not be <code>null</code> or empty. Logs
    *           warning message if it is <code>null</code> or empty.
    * @param communityId the community id of the item.
    * @param contenttypeId the content id of the item.
    * @param objectType the object type number.
    */
   public PSItemEntry(int contentId, String name, int communityId,
         int contenttypeId, int objectType)
   {
      this(contentId, name, communityId, contenttypeId, objectType,
            null, null, null, null, null, -1, -1, -1, -1, -1,null);
   }
   
   /**
    * Constructs an item entry from the given parameters.
    * 
    * @param contentId the content ID of the item.
    * @param name the name of the item, which is also the <code>sys_title</code>
    *           of the item. It should not be <code>null</code> or empty. Logs
    *           warning message if it is <code>null</code> or empty.
    * @param communityId the community id of the item.
    * @param contenttypeId the content id of the item.
    * @param objectType the object type number.
    * @param createdBy the user name that created the item, may be <code>null</code>.
    * @param lastModifiedDate the last modified date, may be <code>null</code>.
    * @param lastModifier The last modifier user name, may be <code>null</code>.
    * @param postedDate the posted date of the item, may be <code>null</code>.
    * @param createdDate the created date of the item, may be <code>null</code>.
    * @param workflowAppId the work-flow ID.
    * @param contentStateId the state ID of the work-flow.
    * @param tipRevision the tip revision of the item.
    * @param checkedOutUsername the user who has the item checked out may be null
    */
   public PSItemEntry(int contentId, String name,
         int communityId, int contenttypeId, int objectType,
         String createdBy, Date lastModifiedDate, String lastModifier, Date postedDate,
         Date createdDate, int workflowAppId, int contentStateId, int tipRevision, int currentRevision, int publicRevision, String checkedOutUsername)
   {
      if (isBlank(name))
      {
         ms_logger.warn(
               "name (or sys_title) must not be null or empty for contentId: '"
                     + contentId + "'");
      }
      
      m_contentId = contentId;
      m_name = name;
      m_communityId = communityId;
      m_contentTypeId = contenttypeId;
      m_objectType = objectType;
      m_createdBy = createdBy;
      m_lastModifiedDate = lastModifiedDate;
      m_lastModifier = lastModifier;
      m_postedDate = postedDate;
      m_createdDate = createdDate;
      m_workflowAppId = workflowAppId;
      m_stateId = contentStateId;
      m_tipRevision = tipRevision;
      m_currentRevision = currentRevision;
      m_publicRevision = publicRevision;
      m_checkedOutUsername = checkedOutUsername;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getContentId()
    */
   public int getContentId()
   {
      return m_contentId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getCreatedBy()
    */
   public String getCreatedBy()
   {
      return m_createdBy;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getContentTypeLabel()
    */
   public String getContentTypeLabel()
   {
      return m_contentTypeLabel;
   }
   
   /**
    * Sets the label of the content type.
    * @param label the label, may be <code>null</code> or empty.
    */
   public void setContentTypeLabel(String label)
   {
      m_contentTypeLabel = label;      
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getStateName()
    */
   public String getStateName()
   {
      return m_stateName;
   }
   
   /**
    * Sets the state name of the item.
    * @param name the state name, may be <code>null</code> or empty.
    */
   public void setStateName(String name)
   {
      m_stateName = name;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getLastModifiedDate()
    */
   public Date getLastModifiedDate()
   {
      return m_lastModifiedDate;
   }

   public String getLastModifier()
   {
      return m_lastModifier;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getPostDate()
    */
   public Date getPostDate()
   {
      return m_postedDate;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getCreatedDate()
    */
   public Date getCreatedDate()
   {
      return m_createdDate;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getWorkflowAppId()
    */
   public int getWorkflowAppId()
   {
      return m_workflowAppId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getContentStateId()
    */
   public int getContentStateId()
   {
      return m_stateId;
   }

   /**
    * @return the xML_NODE_NAME
    */
   public String getXML_NODE_NAME()
   {
      return XML_NODE_NAME;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getName()
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Package protected method. Sets the name of the item.
    * 
    * @param name the new name of the item, never <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null");
         
      m_name = name;
   }
   
   /**
    * Sets the last modified date.
    * @param date the modified date, never <code>null</code>.
    */
   public void setLastModifiedDate(Date date)
   {
      notNull(date);
      
      m_lastModifiedDate = date;
   }
   
   public void setLastModifier(String modifier)
   {
      notEmpty(modifier);
      
      m_lastModifier = modifier;
   }
   
   /**
    * Sets the 1st published date.
    * @param date the published date, may be <code>null</code>.
    */
   public void setPostDate(Date date)
   {
      m_postedDate = date;
   }
   
   /**
    * Sets the work-flow ID
    * @param id the new ID, it may be <code>-1</code> if unknown
    */
   public void setWorkflowAppId(int id)
   {
      m_workflowAppId = id;
   }
   
   /**
    * Sets the state ID
    * @param id the new id, it may be <code>-1</code> if unknown.
    */
   public void setContentStateId(int id)
   {
      m_stateId = id;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getCommunityId()
    */
   public int getCommunityId()
   {
      return m_communityId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getObjectType()
    */
   public int getObjectType()
   {
      return m_objectType;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#getContentTypeId()
    */
   public int getContentTypeId()
   {
      return m_contentTypeId;
   }
   
   public int getTipRevision()
   {
      return m_tipRevision;
   }
   
   /**
    * Set the tip revision
    * 
    * @param revision the new revision, it may be <code>-1</code> if unknown
    */
   public void setTipRevision(int revision)
   {
      m_tipRevision = revision;
   }
   
   public int getCurrentRevision()
   {
      return m_currentRevision;
   }
   
   /**
    * Set the tip revision
    * 
    * @param revision the new revision, it may be <code>-1</code> if unknown
    */
   public void setCurrentRevision(int revision)
   {
      m_currentRevision = revision;
   }
   
   public int getPublicRevision()
   {
      return m_publicRevision;
   }

    /**
     * Get the user that has checked out this item
     *
     * @return The user name if the item is currently checkout. May be null or empty
     */
    @Override
    public String getCheckedOutUsername() {
        return m_checkedOutUsername;
    }

    public void setCheckedOutUsername(String s){
        m_checkedOutUsername = s;
    }
    /**
    * Set the tip revision
    * 
    * @param revision the new revision, it may be <code>-1</code> if unknown
    */
   public void setPublicRevision(int revision)
   {
      m_publicRevision = revision;
   }

   public boolean isCheckedOut()
   {
      return m_currentRevision < m_tipRevision;
   }
   
   public boolean hasOlderPublicRevision()
   {
      return m_publicRevision < m_currentRevision;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#isFolder()
    */
   public boolean isFolder()
   {
      return m_objectType == PSCmsObject.TYPE_FOLDER;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.legacy.IPSItemEntry#toXml(org.w3c.dom.Document)
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(PSComponentSummary.XML_ATTR_NAME, m_name);
      root.setAttribute(PSComponentSummary.XML_ATTR_CONTENT_ID, ""
            + m_contentId);
      root.setAttribute(PSComponentSummary.XML_ATTR_CONTENTTYPE_ID, ""
            + m_contentTypeId);
      root.setAttribute(PSComponentSummary.XML_ATTR_COMMUNITYID, ""
            + m_communityId);
      root.setAttribute(PSComponentSummary.XML_ATTR_TYPE, "" + m_objectType);

      return root;
   }
   
   /**
    * The content id of the item. Init by ctor, never modified after that.
    */
   private final int m_contentId;
   
   private String m_createdBy;
   private Date m_lastModifiedDate;
   private String m_lastModifier;
   private Date m_postedDate;
   private Date m_createdDate;
   private int m_workflowAppId;
   private int m_stateId;
   private String m_contentTypeLabel;
   private String m_stateName;
   private int m_tipRevision;
   private int m_currentRevision;
   private int m_publicRevision;
   
   /**
    * The name of the folder. Initialized by ctor, never <code>null</code>
    * after that.
    */
   protected String m_name;

   /**
    * The content type id of the item. Init by ctor, never modified after
    * that.
    */
   private final int m_contentTypeId;

   /**
    * The community id of the item, init by ctor.
    */
   protected int m_communityId;

   protected String m_checkedOutUsername;
   /**
    * The object type, init by ctor, never modified after that.
    */
   private final int m_objectType;
  
   private final String XML_NODE_NAME = "PSXItemEntry";
   
   /**
    * The log4j logger used for this class. 
    */
   private static final Logger ms_logger = LogManager.getLogger("PSItemEntry");

   
}
