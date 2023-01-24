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

import com.percussion.delivery.comments.data.IPSComment;

import java.util.Date;
import java.util.Set;

/**
 * @author erikserating
 *
 */
public class PSRestComment implements IPSComment
{
   
   private APPROVAL_STATE approvalState = APPROVAL_STATE.APPROVED;
   private Date createdDate;
   private String id;
   private String pagePath;
   private String email;
   private String username;
   private String text;
   private String title;
   private String parent;
   private Set<String> tags;
   private boolean moderated;
   private boolean viewed;
   private String site;
   private String url;
   private String commentCreatedDate;
   
  public PSRestComment()
  {
     
  }
   
   /**
    * Creates a new comment with the same values as the given one.
    * 
    * @param comment A comment to create a copy from.
    */
   public PSRestComment(IPSComment comment)
   {
       this.id = comment.getId();
       this.approvalState = comment.getApprovalState();
       this.createdDate = comment.getCreatedDate();
       this.email = comment.getEmail();
       this.moderated = comment.isModerated();
       this.pagePath = comment.getPagePath();
       this.parent = comment.getParent();
       this.site = comment.getSite();
       setTags(comment.getTags());
       this.text = comment.getText();
       this.title = comment.getTitle();
       this.url = comment.getUrl();
       this.username = comment.getUsername();
       this.viewed = comment.isViewed();
       this.commentCreatedDate = comment.getCommentCreatedDate();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getApprovalState()
    */
   public APPROVAL_STATE getApprovalState()
   {
      return approvalState;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getCreatedDate()
    */
   public Date getCreatedDate()
   {
      return createdDate;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getEmail()
    */
   public String getEmail()
   {
      return email;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getId()
    */
   public String getId()
   {
      return id;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getPagePath()
    */
   public String getPagePath()
   {
      return pagePath;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getParent()
    */
   public String getParent()
   {
      return parent;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getTags()
    */
   public Set<String> getTags()
   {
      return tags;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getText()
    */
   public String getText()
   {
      return text;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getUsername()
    */
   public String getUsername()
   {
      return username;
   }
   
   /**
    * @param createdDate the createdDate to set
    */
   public void setCreatedDate(Date createdDate)
   {
      this.createdDate = createdDate;
   }

   /**
    * @param id the id to set
    */
   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * @param pagePath the pagePath to set
    */
   public void setPagePath(String pagePath)
   {
      this.pagePath = pagePath;
   }

   /**
    * @param email the email to set
    */
   public void setEmail(String email)
   {
      this.email = email;
   }

   /**
    * @param username the username to set
    */
   public void setUsername(String username)
   {
      this.username = username;
   }

   /**
    * @param text the text to set
    */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
    * @param parent the parent to set
    */
   public void setParent(String parent)
   {
      this.parent = parent;
   }

   /**
    * @param tags the tags to set
    */
   public void setTags(Set<String> tags)
   {
      this.tags = tags;
   }

   /**
    * @param approvalState the approvalState to set
    */
   public void setApprovalState(APPROVAL_STATE approvalState)
   {
      this.approvalState = approvalState;
   }

   /**
    * @return the moderated
    */
   public boolean isModerated()
   {
      return moderated;
   }

   /**
    * @param moderated the moderated to set
    */
   public void setModerated(boolean moderated)
   {
      this.moderated = moderated;
   }

   /**
    * @return the viewed
    */
   public boolean isViewed()
   {
      return viewed;
   }

   /**
    * @param viewed the viewed to set
    */
   public void setViewed(boolean viewed)
   {
      this.viewed = viewed;
   }

   /**
    * @return the site
    */
   public String getSite()
   {
      return site;
   }

   /**
    * @param site the site to set
    */
   public void setSite(String site)
   {
      this.site = site;
   }

   /**
    * @return the url
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * @param url the url to set
    */
   public void setUrl(String url)
   {
      this.url = url;
   }

   /**
    * @return the title
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param title the title to set
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

    @Override
    public String getCommentCreatedDate() {
        return commentCreatedDate;
    }

    @Override
    public void setCommentCreatedDate(String commentCreatedDate) {
        this.commentCreatedDate = commentCreatedDate;
    }
}
