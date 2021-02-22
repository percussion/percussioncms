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
package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.lang.StringUtils;

import com.percussion.sitemanage.service.IPSSitePublishService.PubType;

/**
 * This request object stores the information required to publish a site.
 */
@XmlRootElement(name = "SitePublishRequest")
public class PSSitePublishRequest
{
   /**
    * See {@link #getSiteName()}.
    */
   @NotBlank
   @NotNull
   String siteName;

   /**
    * See {@link #getType()}.
    */
   @NotBlank
   @NotNull
   PubType type;

   /**
    * See {@link #getItems()}.
    */
   String[] items;

   /**
    * @return the name of the site to be published, never blank.
    */
   public String getSiteName()
   {
      return siteName;
   }

   /**
    * @param siteName the name of the site to be published. May not be blank.
    */
   public void setSiteName(String siteName)
   {
      if (StringUtils.isBlank(siteName))
         throw new IllegalArgumentException("siteName may not be blank");

      this.siteName = siteName;
   }

   /**
    * @return the type of publishing to be performed. Never <code>null</code>.
    */
   public PubType getType()
   {
      return type;
   }

   /**
    * @param type the type of publishing to be performed, may not be
    *            <code>null</code>.
    */
   public void setType(PubType type)
   {
      this.type = type;
   }

   /**
    * @return the id's of the items to be published during demand publishing,
    *         {@link PubType#PUBLISH_NOW}. May be <code>null</code>.
    */
   public String[] getItems()
   {
      return items;
   }

   /**
    * @param items the id's of the items to be published during demand
    *            publishing, {@link PubType#PUBLISH_NOW}. May be
    *            <code>null</code>.
    */
   public void setItems(String[] items)
   {
      this.items = items;
   }

}
