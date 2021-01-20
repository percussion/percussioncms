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

package com.percussion.services.touchitem.impl;

import static com.percussion.relationship.IPSExecutionContext.RS_PRE_CONSTRUCTION;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_DESTRUCTION;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_UPDATE;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.notification.PSMessageQueueListenerAdapter;
import com.percussion.services.touchitem.IPSTouchItemService;
import com.percussion.services.touchitem.PSTouchItemLocator;
import com.percussion.services.touchitem.data.PSTouchItemData;

import org.apache.log4j.Logger;

/**
 * Listens for Touching Messages.
 * 
 * @author adamgent
 *
 */
public class PSTouchItemQueueListener extends PSMessageQueueListenerAdapter<PSTouchItemData>
{

   /**
    * {@inheritDoc}
    */
   public void onMessage(PSTouchItemData object)
   {
      PSRelationship rel = object.getRelationship();
      int action = object.getAction();
      IPSRequestContext req = new PSRequestContext(PSRequest.getContextForRequest());

      switch (action)
      {
         case RS_PRE_CONSTRUCTION :
            ms_logger.debug("Info construction");
            touchItems(req, rel);
            break;
            
         case RS_PRE_DESTRUCTION :
            ms_logger.debug("Info destroy");
            updateSiteItems(req,rel);
            touchItems(req, rel);
            break;
            
         case RS_PRE_UPDATE :
            ms_logger.debug("Info updating");
            updateSiteItems(req,rel);
            touchItems(req, rel);
            break;
      }
   }

   /**
    * Touches items based on the request and relationship.
    * @param req not null.
    * @param rel not null.
    * @see IPSTouchItemService#touchItems(IPSRequestContext, PSRelationship)
    */
   private void touchItems(IPSRequestContext req, PSRelationship rel)
   {
      PSTouchItemLocator.getTouchItemService().touchItems(req, rel);
   }
   
   /**
    * See {@link IPSTouchItemService#updateSiteItems(IPSRequestContext, PSRelationship)}.
    * 
    * @param req not null.
    * @param rel not null.
    * @see IPSTouchItemService#updateSiteItems(IPSRequestContext, PSRelationship)
    */
   private void updateSiteItems(IPSRequestContext req, PSRelationship rel)
   {
      PSTouchItemLocator.getTouchItemService().updateSiteItems(req, rel);
   }
   
   @Override
   public Class<PSTouchItemData> getType()
   {
      return PSTouchItemData.class;
   }
   
   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger(PSTouchItemQueueListener.class);

}
