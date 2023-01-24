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
