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
