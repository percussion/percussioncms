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
package com.percussion.rx.publisher.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.jexl.PSStringUtils;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.util.IPSHtmlParameters;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A composite key that represent an unique group of {@link IPSSiteItem}
 * objects, which were published from one item, and this item may be under one
 * or more folders. All IDs are in UUID or raw data format.
 */
class PSSiteItemKey
{
   /**
    * The (UUID) ID of an item. Initialized by constructor, never modified
    * afterwards.
    */
   int contentId;

   /**
    * The (UUID) ID of a template. Initialized by constructor, never modified
    * afterwards. See {@link #getTemplateId()} for more info.
    */
   int templateId = 0;

   /**
    * The delivery type.
    */
   String deliveryType;

   /**
    * The delivery context.
    */
   int deliveryContext;

   /**
    * The page number of the site item. It is <code>0</code> for non-paginated
    * item; otherwise it is <code>1</code> based page number for paginated
    * items.
    */
   int page;

   /**
    * The (UUID) ID of a site. Initialized by constructor, never modified
    * afterwards.
    */
   int siteId;

   /**
    * Logger.
    */
   private static final Log ms_log = LogFactory.getLog(PSSiteItemKey.class);

   /**
    * Constructs an instance from an assembly item.
    * 
    * @param item the assembly item, it may be not <code>null</code>.
    */
   public PSSiteItemKey(IPSAssemblyItem item)
   {
      notNull(item);

      contentId = item.getId().getUUID();
      templateId = getTemplateIdFromItem(item);
      deliveryType = item.getDeliveryType();
      deliveryContext = item.getDeliveryContext();
      siteId = item.getSiteId().getUUID();
      page = item.getPage() == null ? 0 : item.getPage().intValue();
   }

   /**
    * Constructs an instance from a site item.
    * 
    * @param siteItem the site item, not <code>null</code>.
    */
   public PSSiteItemKey(IPSSiteItem siteItem)
   {
      notNull(siteItem);

      contentId = siteItem.getContentId().intValue();
      templateId = siteItem.getTemplateId() == null ? 0 : siteItem
            .getTemplateId().intValue();
      deliveryType = siteItem.getDeliveryType();
      deliveryContext = siteItem.getContext();
      siteId = (int) siteItem.getSiteId();
      page = siteItem.getPage();
   }

   /**
    * Gets the (UUID) ID of the item.
    * 
    * @return the content ID of the item.
    */
   public int getContentId()
   {
      return contentId;
   }

   /**
    * Gets the (UUID) ID of the template that was used to publish the item.
    * 
    * @return the ID of the template, it may be <code>0</code> if the template
    *         is unknown.
    */
   public int getTemplateId()
   {
      return templateId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object other)
   {
      if (!(other instanceof PSSiteItemKey))
         return false;

      PSSiteItemKey otherKey = (PSSiteItemKey) other;
      return otherKey.contentId == contentId
            && otherKey.templateId == templateId
            && otherKey.deliveryContext == deliveryContext
            && otherKey.page == page && otherKey.siteId == siteId
            && StringUtils.equals(deliveryType, otherKey.deliveryType);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return contentId + templateId + deliveryContext + page + siteId
            + (StringUtils.isBlank(deliveryType) ? 0 : deliveryType.hashCode());
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "cid=" + contentId + ", tid=" + templateId + ", cxt="
            + deliveryContext + ", page=" + page + ", siteId=" + siteId
            + ", dtype=" + deliveryType;
   }

   /**
    * Gets the template ID from the specified assembly item.
    * 
    * @param item the assembly item in question, assumed not <code>null</code>.
    * 
    * @return the template ID, it may be <code>0</code> if template is unknown.
    */
   private int getTemplateIdFromItem(IPSAssemblyItem item)
   {
      if (item.getTemplate() != null)
      {
         return item.getTemplate().getGUID().getUUID();
      }

      if (item.getParameters() != null)
      {
         return getTemplateIdFromParameters(item);
      }

      return getTemplateIdFromUrl(item.getAssemblyUrl());
   }

   /**
    * Gets the template ID from the parameters of the specified item
    * 
    * @param item the work item, assumed not <code>null</code>
    * @return the template ID, it may be <code>0</code> if the template ID is
    *         unknown.
    */
   private int getTemplateIdFromParameters(IPSAssemblyItem item)
   {
      try
      {
         String snumber = item.getParameterValue(
               IPSHtmlParameters.SYS_TEMPLATE, null);
         if (isNotBlank(snumber))
            return Integer.parseInt(snumber);

         snumber = item
               .getParameterValue(IPSHtmlParameters.SYS_VARIANTID, null);
         if (isNotBlank(snumber))
            return Integer.parseInt(snumber);

         snumber = item.getParameterValue(IPSHtmlParameters.SYS_VARIANT, null);
         if (isNotBlank(snumber))
            return Integer.parseInt(snumber);

         return 0;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to get template ID from parameters of item id = "
               + item.getId().getUUID(), e);
         return 0;
      }

   }

   /**
    * Gets the template ID from the specified assembly URL
    * 
    * @param url the assembly URL, assumed not <code>null</code>
    * @return the template ID, it may be <code>0</code> if the template ID is
    *         unknown.
    */
   private int getTemplateIdFromUrl(String url)
   {
      // Get the query portion of the url
      int q = url != null ? url.indexOf('?') : 0;
      if (q <= 0)
         return 0;

      String query = url.substring(q + 1);
      try
      {
         PSStringUtils sutils = new PSStringUtils();
         Map<String, String> params = sutils.stringToMap(query);
         String snumber = params.get(IPSHtmlParameters.SYS_TEMPLATE);
         if (isNotBlank(snumber))
            return Integer.parseInt(snumber);

         snumber = params.get(IPSHtmlParameters.SYS_VARIANTID);
         if (isNotBlank(snumber))
            return Integer.parseInt(snumber);

         snumber = params.get(IPSHtmlParameters.SYS_VARIANT);
         if (isNotBlank(snumber))
            return Integer.parseInt(snumber);

         return 0;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to get template ID from assembly URL: " + url, e);
         return 0;
      }
   }
}
