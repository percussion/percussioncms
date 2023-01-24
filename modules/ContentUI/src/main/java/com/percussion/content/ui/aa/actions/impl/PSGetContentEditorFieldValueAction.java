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
