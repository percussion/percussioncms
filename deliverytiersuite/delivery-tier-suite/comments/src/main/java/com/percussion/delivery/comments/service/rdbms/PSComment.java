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
package com.percussion.delivery.comments.service.rdbms;

import com.percussion.delivery.comments.data.IPSComment;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author erikserating
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSComments1")
@Table(name = "PERC_PAGE_COMMENTS")
public class PSComment implements IPSComment, Serializable
{
    @TableGenerator(
        name="commentId", 
        table="PERC_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="commentId", 
        allocationSize=1)
    
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="commentId")
    private long id;
    
   @Basic
   private String approvalState = APPROVAL_STATE.APPROVED.toString();
   
   @Basic
   private Date createdDate;
   
   @Basic
   private String pagePath;
   
   @Basic
   @Column(length = 4000)
   private String email;
   
   @Basic
   @Column(length = 4000)
   private String username;
   
   @Lob
   @Column(length = Integer.MAX_VALUE)
   private String text;
   
   @Basic
   @Column(length = 4000)
   private String title;
   
   @Basic
   private long parent;


   @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL,
           orphanRemoval=true, mappedBy="comment" , targetEntity = PSCommentTag.class)
   @Fetch(FetchMode.SUBSELECT)
   @OnDelete(action = OnDeleteAction.CASCADE)
   private Set<PSCommentTag> commentTags = new HashSet<PSCommentTag>();
   
   @Basic 
   private boolean moderated;
   
   @Basic 
   private boolean viewed;
   
   @Basic
   private String site;
   
   @Basic
   @Column(length = 2000)
   private String url;

   @Transient
   private String commentCreatedDate;
   
   public PSComment()
   {
       
   }
   
   /**
    * Creates a new comment with the same values as the given one,
    * except for the id.
    * 
    * @param comment A comment to create a copy from.
    */
   public PSComment(IPSComment comment)
   {
       this.approvalState = comment.getApprovalState().toString();
       this.createdDate = comment.getCreatedDate();
       this.email = comment.getEmail();
       this.moderated = comment.isModerated();
       this.pagePath = comment.getPagePath();
       this.parent = comment.getParent() == null ? 0 : Long.valueOf(comment.getParent());
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
      return APPROVAL_STATE.valueOf(approvalState);
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
      return String.valueOf(id);
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
      return String.valueOf(parent);
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#getTags()
    */
   public Set<String> getTags()
   {
      Set<String> tagsAsString = new HashSet<String>();
      
      for (PSCommentTag tag : this.commentTags)
          tagsAsString.add(tag.getName());
      
      return tagsAsString;
   }

   public Set<PSCommentTag> getCommentTags()
   {
      return commentTags;
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
      this.id = id == null ? 0 : Long.valueOf(id);
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
      this.parent = Long.valueOf(parent);
   }
   
   public void setTags(Set<String> tags)
   {
       if(tags == null)
          return;
       PSCommentTag commentTag;
       
       for (String aTag : tags)
       {
           commentTag = new PSCommentTag(aTag);
           commentTag.setComment(this);
           this.commentTags.add(commentTag);
       }
   }
   
   public void setCommentTags(Set<PSCommentTag> commentTags)
   {
       this.commentTags = commentTags;
   }

   /**
    * @param approvalState the approvalState to set
    */
   public void setApprovalState(APPROVAL_STATE approvalState)
   {
      this.approvalState = approvalState.toString();
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#isModerated()
    */
   public boolean isModerated()
   {
      return moderated;
   }

   /* (non-Javadoc)
    * @see com.percussion.comments.data.IPSComment#isViewed()
    */
   public boolean isViewed()
   {
      return viewed;
   }

   /**
    * @param moderated the moderated to set
    */
   public void setModerated(boolean moderated)
   {
      this.moderated = moderated;
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


   public String getCommentCreatedDate() {
      return commentCreatedDate;
   }

   public void setCommentCreatedDate(String commentCreatedDate) {
      this.commentCreatedDate = commentCreatedDate;
   }
}
