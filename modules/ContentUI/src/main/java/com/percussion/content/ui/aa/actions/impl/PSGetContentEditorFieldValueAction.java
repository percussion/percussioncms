/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Action to get the content editor field value. Loads the item using
 * webservices and gets the value of the filed with the name mentioned in object
 * id.
 * 
 */
public class PSGetContentEditorFieldValueAction extends PSAAActionBase
{
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid cid = guidMgr.makeGuid(new PSLocator(objectId.getContentId()));

      IPSContentWs ctService = PSContentWsLocator.getContentWebservice();

      List<IPSGuid> ids = Collections.singletonList(cid);
      List<PSCoreItem> items;
      IPSFieldValue value = null;
      String fieldValue = "";
      try
      {
         items = ctService.loadItems(ids, false, false, false, false);
         PSItemField field = items.get(0).getFieldByName(
               objectId.getFieldName());
         if (field != null)
         {
            value = field.getValue();
            if (value != null)
            {
               fieldValue = value.getValueAsString();
            }
         }
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }

      return new PSActionResponse(fieldValue,
            PSActionResponse.RESPONSE_TYPE_HTML);
   }

}
