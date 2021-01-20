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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.content.PSAaRelationshipFolder;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.PSAaRelationship} and
 * {@link com.percussion.webservices.content.PSAaRelationship}
 */
public class PSAaRelationshipConverter extends PSRelationshipConverter
{
   /*
    * (non-Javadoc)
    *
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSAaRelationshipConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /*
    * (non-Javadoc)
    *
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("deprecation")
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value) {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         // convert ID properties without reference info since the reference
         // info is transient data and will not be needed in the server 
         // envirenment.
         com.percussion.webservices.content.PSAaRelationship source =
            (com.percussion.webservices.content.PSAaRelationship) value;

         PSRelationship origRel = getRelationshipFromClient(source);
         
         // set required ID properties
         PSGuid slotId = new PSGuid(PSTypeEnum.SLOT, source.getSlot().getId());
         origRel.setProperty(IPSHtmlParameters.SYS_SLOTID, String
               .valueOf(slotId.longValue()));
         PSGuid templetId = new PSGuid(PSTypeEnum.TEMPLATE, source.getTemplate().getId());
         origRel.setProperty(IPSHtmlParameters.SYS_VARIANTID, String
               .valueOf(templetId.longValue()));
         
         PSAaRelationship target = new PSAaRelationship(origRel);

         // set known properties
         if (source.getSortRank() != null)
            target.setSortRank(source.getSortRank().intValue());
         else
            target.setSortRank(0);
         if (source.getSite() != null)
         {
            PSGuid siteId = new PSGuid(PSTypeEnum.SITE, source.getSite().getId());
            target.setSiteId(siteId);
         }
         if (source.getFolder() != null)
         {
            PSLegacyGuid folderId = new PSLegacyGuid(source.getFolder().getId());
            target.setFolderId(folderId.getContentId());
         }

         return target;
      }
      else // convert from server to webservice
      {
         PSAaRelationship source = (PSAaRelationship) value;

         com.percussion.webservices.content.PSAaRelationship target =
            new com.percussion.webservices.content.PSAaRelationship();
         setRelationshipFromServer(source, target);

         // set known properties
         PSDesignGuid slotId = new PSDesignGuid(source.getSlotId());
         Reference slot = new Reference(slotId.getValue(), source.getSlotName());
         target.setSlot(slot);
         
         PSDesignGuid templateId = new PSDesignGuid(source.getTemplateId());
         Reference template = new Reference(templateId.getValue(),
               source.getTemplateName());
         target.setTemplate(template);

         NonNegativeInteger sortrank =
            new NonNegativeInteger(String.valueOf(source.getSortRank()));
         target.setSortRank(sortrank);

         Reference site = null;
         if (source.getSiteId() != null)
         {
            site = new Reference(source.getSiteId().longValue(),
                  source.getSiteName());
            target.setSite(site);
         }
         PSAaRelationshipFolder folder = null;
         if (source.getFolderId() != -1)
         {
            PSLegacyGuid folderGuid = new PSLegacyGuid(source.getFolderId(),-1);
            folder = new PSAaRelationshipFolder(
                  new PSDesignGuid(folderGuid).longValue(),
                  source.getFolderName(),
                  source.getFolderPath());
            target.setFolder(folder);
         }

         return target;
      }
   }
}
