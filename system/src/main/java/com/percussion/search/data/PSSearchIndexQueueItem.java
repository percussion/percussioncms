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
package com.percussion.search.data;


import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.search.PSSearchEditorChangeEvent;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.Validate.notNull;

/**
 * This is a entity class to map the PSX_SEARCHINDEXQUEUE table using
 * hibernate
 *
 * @author BillLanglais
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONE)
@Table(name = "PSX_SEARCHINDEXQUEUE")
public class PSSearchIndexQueueItem 
{
   @Id
   @Column(name = "QUEUEID")
   private int m_queueId;
   
   
   @OneToOne(fetch = FetchType.LAZY)
   @JoinColumn(name="CONTENTID", insertable = false, updatable = false, nullable = true)
   private PSComponentSummary summary;
   
   @Temporal(TemporalType.TIMESTAMP)
   @Column(name = "CREATED", nullable = false)
   private Date m_created;
   
  
   /**
    * Constant to indicate the item was inserted.
    */
   public static final int ACTION_INSERT = 0;
   
   /**
    * Constant to indicate the item was modified.
    */
   public static final int ACTION_UPDATE = 1;
   
   /**
    * Constant to indicate the item was deleted.
    */
   public static final int ACTION_DELETE = 2;
   
   /**
    * Constant to indicate the item was checked in.
    */
   public static final int ACTION_CHECKIN = 3;
   
   /**
    * Constant to indicate the item was checked out.
    */
   public static final int ACTION_CHECKOUT = 4;
   
   /**
    * Constant to indicate the item was transitioned.
    */
   public static final int ACTION_TRANSITION = 5;
   
   /**
    * Constant to indicate the item was submitted for reindexing by the search
    * engine.
    */
   public static final int ACTION_REINDEX = 6;
   
   /**
    * Constant to indicate undefined action.
    */
   public static final int ACTION_UNDEFINED = -1;
   
   /**
    * Enumeration of the string representation of each <code>ACTION_xxx</code>
    * constants, where the index into the array is the value of the constant.
    * When a new constant is added, its string representation must be appended
    * to this enumeration. 
    */
   public static final String[] ACTION_ENUM = 
   {
      "insert", 
      "update", 
      "delete", 
      "checkin", 
      "checkout", 
      "transition",
      "reindex"
   };

   /**
    * Constant for default priority of 20.
    */
   public static final int DEFAULT_PRIORTY = 20;
   
   /**
    * The content id of the modified item.  Set during ctor, never modified
    * after that.
    */
   @Column(name = "CONTENTID")
   private int m_contentId;
   
   /**
    * The revision id of the modified item.  Set during ctor, never modified
    * after that.
    */
   @Column(name = "REVISIONID")
   private int m_revisionId;
   
   /**
    * The id representing the complex child modified.  <code>-1</code> if a 
    * parent item was modified.  Set during the ctor, never modified after that.
    */
   @Column(name = "CHILDID")
   private int m_childId;
   
   /**
    * The id of the child row that was modified.  <code>-1</code> if a 
    * parent item was modified.  Set during the ctor, never modified after that.
    */
   @Column(name = "CHILDROWID")
   private int m_childRowId;

   /**
    * The action that was taken when the content item was modified.  One of the
    * <code>ACTION_xxx</code> values, set during ctor, never <code>null</code>
    * or modified after that.
    */
   @Column(name = "ACTION")
   private int m_action;
   
   /**
    * Collection of binary field names that were modified by this event, as 
    * <code>String</code> objects.  Never <code>null</code>, modified by
    * calls to {@link #setBinaryFields(Collection)}.
    */
   @Column(name = "BINARYFIELDS")
   private String m_binaryFields;;
   
   /**
    * Content type id of the item, set during the ctor, never modified after 
    * that.
    */
   @Column(name = "CONTENTTYPEID")
   private long m_contentTypeId;
   
   
   /**
    * The priority of this event, defaults to {@link #DEFAULT_PRIORTY} if not 
    * explicitly modified by {@link #setPriority(int)}.
    */
   @Column(name = "PRIORITY")
   private int m_priority = DEFAULT_PRIORTY;

   /**
    *  Accessor method for QueueId
    * @param id - Primary key for events
    */
   public void setQueueId(int id)
   {
      m_queueId = id; 
   }
   
   /**
    * Accessor method for QueueId
    * @return - Primary key for events
    */
   public int getQueueId()
   {
      return(m_queueId);
   }
   
   
   /**
    * Accessor method for Queue Events
    * 
    * @param event - event in class form. Must not be <code>null</code>
    */
   public void setChangeEvent(PSSearchEditorChangeEvent event)
   {
      notNull(event, "Change Event must not be null!");
      
      this.m_action = event.getActionType();
      this.m_binaryFields = StringUtils.join(event.getBinaryFields(),",");
      this.m_childId = event.getChildId();
      this.m_childRowId = event.getChildRowId();
      this.m_contentId = event.getContentId();
      this.m_contentTypeId = event.getContentTypeId();
      this.m_priority = event.getPriority();
      this.m_revisionId = event.getRevisionId();
      this.m_created = new Date();
   }
   
   
   
   public int getContentId()
   {
      return m_contentId;
   }

   public void setContentId(int contentId)
   {
      m_contentId = contentId;
   }

   public int getRevisionId()
   {
      return m_revisionId;
   }

   public void setRevisionId(int revisionId)
   {
      m_revisionId = revisionId;
   }

   public int getChildId()
   {
      return m_childId;
   }

   public void setChildId(int childId)
   {
      m_childId = childId;
   }

   public int getChildRowId()
   {
      return m_childRowId;
   }

   public void setChildRowId(int childRowId)
   {
      m_childRowId = childRowId;
   }

   public int getAction()
   {
      return m_action;
   }

   public void setAction(int action)
   {
      m_action = action;
   }

   public String getBinaryFields()
   {
      return m_binaryFields;
   }

   public void setBinaryFields(String binaryFields)
   {
      m_binaryFields = binaryFields;
   }

   public long getContentTypeId()
   {
      return m_contentTypeId;
   }

   public void setContentTypeId(long contentTypeId)
   {
      m_contentTypeId = contentTypeId;
   }

   public int getPriority()
   {
      return m_priority;
   }

   public void setPriority(int priority)
   {
      m_priority = priority;
   }

   @PrePersist
   protected void onCreate() {
      if (m_created == null)
            m_created = new Date();
   }    
   
   /**
    * Accessor method for Queue Events
    * 
    * commit - tells the search indexer to do a commit after processing this
    *          event.  
    * 
    * @return - Event data in class form. Will not be <code>null</code>
    */
   public PSSearchEditorChangeEvent getChangeEvent(boolean commit)
   {
      PSSearchEditorChangeEvent ce = new PSSearchEditorChangeEvent(m_action,m_contentId, m_revisionId, 
            m_childId, m_childRowId, m_contentTypeId, commit);
      String[] fieldArray = StringUtils.split(m_binaryFields,",");
      List<String> fieldList = fieldArray == null ? new ArrayList<String>() : Arrays.asList(fieldArray); 
      ce.setBinaryFields(fieldList);
      return ce;
   }

   // Constructors
   
   /**
    * Default Constructor
    */
   public PSSearchIndexQueueItem()
   {
   }

   /**
    * Creates a Search Index Item Queue Event from the passed in XML.
    * 
    * @param event - Search Index Queue Event. Must not be <code>null</code>
    */
   public PSSearchIndexQueueItem(PSSearchEditorChangeEvent event)
   {
      notNull(event, "Change Event must not be null!");
      
      setChangeEvent(event);
   }

   /**
    * @return the summary
    */
   public PSComponentSummary getSummary()
   {
      return summary;
   }

   /**
    * @param summary the summary to set
    */
   public void setSummary(PSComponentSummary summary)
   {
      this.summary = summary;
   }
   
   public Date getCreated()
   {
      return m_created;
   }

   public void setCreated(Date created)
   {
      m_created = created;
   }

}
