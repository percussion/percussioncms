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
package com.percussion.delivery.comments.data;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
