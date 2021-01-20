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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.percussion.delivery.comments.data.IPSDefaultModerationState;


/**
 * Simple entity to store default moderation state for comments service.
 * @author erikserating
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSComments2")
@Table(name = "PERC_DEFAULT_MODERATION_STATE")
public class PSDefaultModerationState implements IPSDefaultModerationState
{
   
   @Id
   private String site;
   
   @Basic
   private String defaultState;
   
   public PSDefaultModerationState()
   {
      
   }
   
   public PSDefaultModerationState(String site, String defaultState)
   {
      if(StringUtils.isBlank(site))
         throw new IllegalArgumentException("site cannot be null or empty.");
      if(StringUtils.isBlank(defaultState))
         throw new IllegalArgumentException("defaultState cannot be null or empty.");
      this.site = site;
      this.defaultState = defaultState;      
   }

   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#getSite()
 */
   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#getSite()
 */
public String getSite()
   {
      return site;
   }

   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#setSite(java.lang.String)
 */
   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#setSite(java.lang.String)
 */
public void setSite(String site)
   {
      this.site = site;
   }

   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#getDefaultState()
 */
   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#getDefaultState()
 */
public String getDefaultState()
   {
      return defaultState;
   }

   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#setDefaultState(java.lang.String)
 */
   /* (non-Javadoc)
 * @see com.percussion.delivery.comments.service.rdbms.IPSDefaultModerationState#setDefaultState(java.lang.String)
 */
public void setDefaultState(String defaultState)
   {
      this.defaultState = defaultState;
   }   
   

}
