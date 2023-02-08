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

package com.percussion.services.siteimportsummary.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
region = "PSSiteImportSummary")
@Table(name = "PSX_SITEIMPORTSUMMARY")
public class PSSiteImportSummary extends PSAbstractDataObject
{
   @Id
   @Column(name = "SUMMARYID")    
   private long summaryId= -1L;
   
   @Basic
   @Column(name = "SITEID", nullable=false, unique=true)
   private int siteId;
   
   @Basic
   @Column(name = "PAGES")
   private int pages;

   @Basic
   @Column(name = "TEMPLATES")
   private int templates;

   @Basic
   @Column(name = "FILES")
   private int files;

   @Basic
   @Column(name = "STYLESHEETS")
   private int stylesheets;

   @Basic
   @Column(name = "INTERNALLINKS")
   private int internallinks;

   public long getSummaryId()
   {
      return summaryId;
   }

   public void setSummaryId(long summaryId)
   {
      this.summaryId = summaryId;
   }

   public int getSiteId()
   {
      return siteId;
   }

   public void setSiteId(int siteId)
   {
      this.siteId = siteId;
   }

   public int getPages()
   {
      return pages;
   }

   public void setPages(int pages)
   {
      this.pages = pages;
   }

   public int getTemplates()
   {
      return templates;
   }

   public void setTemplates(int templates)
   {
      this.templates = templates;
   }

   public int getFiles()
   {
      return files;
   }

   public void setFiles(int files)
   {
      this.files = files;
   }

   public int getStylesheets()
   {
      return stylesheets;
   }

   public void setStylesheets(int stylesheets)
   {
      this.stylesheets = stylesheets;
   }

   public int getInternallinks()
   {
      return internallinks;
   }

   public void setInternallinks(int internallinks)
   {
      this.internallinks = internallinks;
   }
}
