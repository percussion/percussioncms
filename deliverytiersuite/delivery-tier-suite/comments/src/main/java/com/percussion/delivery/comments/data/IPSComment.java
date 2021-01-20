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
package com.percussion.delivery.comments.data;

import java.util.Date;
import java.util.Set;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.percussion.delivery.comments.service.rdbms.PSCommentTag;
import com.percussion.delivery.services.PSCustomDateSerializer;

/**
 * @author erikserating
 *
 */
public interface IPSComment
{
   /** 
    * @return the id for this comment, this is assigned by the persistence layer. 
    */
   public String getId();
   
   /**
    * @return the id of the parent comment. Used for comment threading. 
    */
   public String getParent();
   
   /**
    * @return the comment text, never <code>null</code>, may be empty.
    */
   public String getText();
   
   /**
    * @return the comment title, may be <code>null</code> or empty.
    */
   public String getTitle();
   
   /**
    * @return the sitename of the site the comment is in.
    */
   public String getSite();
   
   /**
    * @return the page path, the relative path to the page that this comment is on, not including the site.
    * Never <code>null</code> or empty.  
    */
   public String getPagePath();
   
   /** 
    * @return the user name of the person who wrote the comment. May be <code>null</code> or empty.
    */
   public String getUsername();
   
   /**
    * @return the url that the comment author entered. May be <code>null</code> or empty.
    */
   public String getUrl();
   
   /** 
    * @return the email for the user who wrote the comment. May be <code>null</code> or empty.
    */
   public String getEmail();
   
   /** 
    * @return the created date for this comment. Never <code>null</code> or empty.
    */
   @JsonSerialize(using = PSCustomDateSerializer.class)
   public Date getCreatedDate();
   
   /** 
    * @return set of all unique tag strings for this comment. Never <code>null</code>, may be empty.
    */
   public Set<String> getTags();
   
   /**
    * @return the current approval state for this comment. Never <code>null</code>. 
    * Defaults to <code>APPROVAL_STATE.PENDING</code>.
    */
   public APPROVAL_STATE getApprovalState();
   
   /**
    * Flag indicating that this comment has been moderated. This should only be
    * <code>true</code> if this comment was put into a state by a user action and not
    * programmatically.
    * @return <code>true</code> if the the comment was moderated.
    */
   public boolean isModerated();
   
   /**
    * Flag indicating that this comment was viewed once by a CM1 user and is no longer considered
    * a new comment.
    * @return <code>true</code> if this comment was viewed.
    */
   public boolean isViewed();
   
   /**
    * Set the viewed flag to indicate the comment has been viewed once by a moderator.
    * @param viewed
    */
   public void setViewed(boolean viewed);
   
   /**
    * @param createdDate the createdDate to set
    */
   public void setCreatedDate(Date createdDate);

   /**
    * @param id the id to set
    */
   public void setId(String id);

   /**
    * @param pagePath the pagePath to set
    */
   public void setPagePath(String pagePath);

   /**
    * @param email the email to set
    */
   public void setEmail(String email);

   /**
    * @param username the username to set
    */
   public void setUsername(String username);

   /**
    * @param text the text to set
    */
   public void setText(String text);

   /**
    * @param parent the parent to set
    */
   public void setParent(String parent);   
   
   /**
    * @param approvalState the approvalState to set
    */
   public void setApprovalState(APPROVAL_STATE approvalState);

   /**
    * @param moderated the moderated to set
    */
   public void setModerated(boolean moderated);

    /**
    * @param site the site to set
    */
   public void setSite(String site);

   /**
    * @param url the url to set
    */
   public void setUrl(String url);
   
   /**
    * @param title the title to set
    */
   public void setTitle(String title);

   String getCommentCreatedDate();

   void setCommentCreatedDate(String commentCreatedDate);

   /**
    * Comment approval states.
    */
   public enum APPROVAL_STATE
   {
      APPROVED,
      REJECTED      
   }
   
}
