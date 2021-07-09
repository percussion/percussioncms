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
package com.percussion.services.system.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Instance of a content status history row for an item
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSContentStatusHistory")
@Table(name = "CONTENTSTATUSHISTORY")
public class PSContentStatusHistory implements Serializable, Cloneable
{
   /**
    * Java serial version id 
    */
   private static final long serialVersionUID = 1L;
   
   /**
    * Primary key of this object.
    */
   @Id
   @Column(name = "CONTENTSTATUSHISTORYID", nullable = false)   
   private long id;
   
   /**
    * Content id of item.
    */
   @Column(name = "CONTENTID", nullable = false)
   private int contentId;
   
   /**
    * The revision of the item when this entry was made.
    */
   @Column(name = "REVISIONID", nullable = true)
   private int revisionId;
   
   /**
    * The id of the session which made this entry.
    */
   @Column(name = "SESSIONID", nullable = true)
   private String sessionId;
   
   /**
    * The name of the user that created this entyr.
    */
   @Column(name = "ACTOR", nullable = true)
   private String actor;
   
   /**
    * Determines if the content is in a valid (public) state.  'Y' if public,
    * 'N' if not.
    */
   @Column(name = "VALID", nullable = true)
   private String isValidValue;
   
   /**
    * The id of the state the item was in after the action generating this entry
    * completed.
    */
   @Column(name = "STATEID", nullable = true)
   private int stateId;
   
   /**
    * The transition id, 0 if a creation, checkin, or checkout event.
    */
   @Column(name = "TRANSITIONID", nullable = true)
   private int transitionId;
   
   /**
    * The id of the workflow the item was in.
    */
   @Column(name = "WORKFLOWAPPID", nullable = true)
   private int workflowId;
   
   /**
    * The name of the role the user was acting in.
    */
   @Column(name = "ROLENAME", nullable = true)
   private String roleName;
   
   /**
    * The name of the state the item was in after the action generating this 
    * entry completed.
    */
   @Column(name = "STATENAME", nullable = true)
   private String stateName;
   
   /**
    * The label of the transition executed, or an appropriate label for a 
    * creation or checkin/out.
    */
   @Column(name = "TRANSITIONLABEL", nullable = true)
   private String transitionLabel;
   
   /**
    * The name of the user checking out the item (if a checkout event)
    */
   @Column(name = "CHECKOUTUSERNAME", nullable = true)
   private String checkoutUserName;
   
   /**
    * The name of the user who last modified the item
    */
   @Column(name = "LASTMODIFIERNAME", nullable = true)
   private String lastModifierName;
   
   /**
    * The date the item was last modified.
    */
   @Column(name = "LASTMODIFIEDDATE", nullable = true)
   private Date lastModifiedDate;
   
   /**
    * The date when this event occurred.
    */
   @Column(name = "EVENTTIME", nullable = true)
   private Date eventTime;
   
   /**
    * The title of the item.
    */
   @Column(name = "TITLE", nullable = true)
   private String title;
   
   /**
    * The comment supplied with the event (may be <code>null</code>).
    */
   @Column(name = "TRANSITIONCOMMENT", nullable = true)
   private String transitionComment;

   /**
    * @return Returns the actor.
    */
   public String getActor()
   {
      return actor;
   }

   /**
    * @param name The actor to set.
    */
   public void setActor(String name)
   {
      actor = name;
   }

   /**
    * @return Returns the checkoutUserName.
    */
   public String getCheckoutUserName()
   {
      return checkoutUserName;
   }

   /**
    * @param name The checkoutUserName to set.
    */
   public void setCheckoutUserName(String name)
   {
      checkoutUserName = name;
   }

   /**
    * @return Returns the contentId.
    */
   public int getContentId()
   {
      return contentId;
   }

   /**
    * @param contentid The contentId to set.
    */
   public void setContentId(int contentid)
   {
      contentId = contentid;
   }

   /**
    * @return Returns the eventTime.
    */
   public Date getEventTime()
   {
      return eventTime;
   }

   /**
    * @param time The eventTime to set.
    */
   public void setEventTime(Date time)
   {
      eventTime = time;
   }

   /**
    * @return Returns the id.
    */
   public long getId()
   {
      return id;
   }

   public PSContentStatusHistory clone() throws CloneNotSupportedException
   {
      return (PSContentStatusHistory) super.clone();
   }
   
   /**
    * @param histid The id to set.
    */
   public void setId(long histid)
   {
      id = histid;
   }

   /**
    * Determine if the content valid value indicates <code>true</code>.
    * 
    * @return <code>true</code> if it does, <code>false</code> otherwise.
    */
   public boolean isValid()
   {
      return "Y".equalsIgnoreCase(isValidValue);
   }
   
   /**
    * @return Returns the isValidValue.
    */
   public String getIsValidValue()
   {
      return isValidValue;
   }

   /**
    * @param isValid The isValidValue to set.
    */
   public void setIsValidValue(String isValid)
   {
      isValidValue = isValid;
   }

   /**
    * @return Returns the lastModifiedDate.
    */
   public Date getLastModifiedDate()
   {
      return lastModifiedDate;
   }

   /**
    * @param date The lastModifiedDate to set.
    */
   public void setLastModifiedDate(Date date)
   {
      lastModifiedDate = date;
   }

   /**
    * @return Returns the lastModifierName.
    */
   public String getLastModifierName()
   {
      return lastModifierName;
   }

   /**
    * @param date The lastModifierName to set.
    */
   public void setLastModifierName(String date)
   {
      lastModifierName = date;
   }

   /**
    * @return Returns the revision.
    */
   public int getRevision()
   {
      return revisionId;
   }

   /**
    * @param revision The revision to set.
    */
   public void setRevision(int revision)
   {
      revisionId = revision;
   }

   /**
    * @return Returns the roleName.
    */
   public String getRoleName()
   {
      return roleName;
   }

   /**
    * @param name The roleName to set.
    */
   public void setRoleName(String name)
   {
      roleName = name;
   }

   /**
    * @return Returns the sessionId.
    */
   public String getSessionId()
   {
      return sessionId;
   }

   /**
    * @param sessid The sessionId to set.
    */
   public void setSessionId(String sessid)
   {
      sessionId = sessid;
   }

   /**
    * @return Returns the stateId.
    */
   public int getStateId()
   {
      return stateId;
   }

   /**
    * @param stateid The stateId to set.
    */
   public void setStateId(int stateid)
   {
      stateId = stateid;
   }

   /**
    * @return Returns the stateName.
    */
   public String getStateName()
   {
      return stateName;
   }

   /**
    * @param name The stateName to set.
    */
   public void setStateName(String name)
   {
      stateName = name;
   }

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param str The title to set.
    */
   public void setTitle(String str)
   {
      title = str;
   }

   /**
    * @return Returns the transitionComment.
    */
   public String getTransitionComment()
   {
      return transitionComment;
   }

   /**
    * @param comment The transitionComment to set.
    */
   public void setTransitionComment(String comment)
   {
      transitionComment = comment;
   }

   /**
    * @return Returns the transitionId.
    */
   public int getTransitionId()
   {
      return transitionId;
   }

   /**
    * @param transid The transitionId to set.
    */
   public void setTransitionId(int transid)
   {
      transitionId = transid;
   }

   /**
    * @return Returns the transitionLabel.
    */
   public String getTransitionLabel()
   {
      return transitionLabel;
   }

   /**
    * @param label The transitionLabel to set.
    */
   public void setTransitionLabel(String label)
   {
      transitionLabel = label;
   }

   /**
    * @return Returns the workflowId.
    */
   public int getWorkflowId()
   {
      return workflowId;
   }

   /**
    * @param workflowid The workflowId to set.
    */
   public void setWorkflowId(int workflowid)
   {
      workflowId = workflowid;
   }
}

