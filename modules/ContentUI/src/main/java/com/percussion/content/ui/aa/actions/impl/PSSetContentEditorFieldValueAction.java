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
import com.percussion.design.objectstore.PSDetails;
import com.percussion.design.objectstore.PSDisplayError;
import com.percussion.design.objectstore.PSFieldError;
import com.percussion.design.objectstore.PSFieldValidationException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sets the field value of a content editor. If there is any validation error
 * while saving the item, returns a JSON object with name as validationError and
 * value as actual validation error.
 */
public class PSSetContentEditorFieldValueAction extends PSAAActionBase
{
   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);

      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid cid = guidMgr.makeGuid(new PSLocator(objectId.getContentId()));

      IPSContentWs ctService = PSContentWsLocator.getContentWebservice();

      List<IPSGuid> ids = Collections.singletonList(cid);
      List<PSCoreItem> items;
      String fieldValue = StringUtils.defaultString((String) getParameter(
            params, "fieldValue"));
      try
      {
         items = ctService.loadItems(ids, false, false, false, false);
         PSItemField field = items.get(0).getFieldByName(
               objectId.getFieldName());
         if (field != null)
         {
            field.clearValues();
            IPSFieldValue fvalue = field.createFieldValue(fieldValue);
            field.addValue(fvalue);
         }
         ctService.saveItems(items, false, false);
      }
      catch (Exception e)
      {
         if(e instanceof PSErrorResultsException)
         {
            return handleValidationException((PSErrorResultsException)e);
         }
         throw new PSAAClientActionException(e);
      }
      // As the Validation error returns a JSONObject,we have to return a
      // dummy JSONObject for the clients to set the response type as JSON. 
      JSONObject dummyResult = new JSONObject();
      try
      {
         dummyResult.append(SUCCESS, SUCCESS);
      }
      catch (JSONException e)
      {
         // This should not happen as we are creating a dummy JSONObject.
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(dummyResult.toString(),
            PSActionResponse.RESPONSE_TYPE_JSON);
   }
   
   /**
    * Utility method to parse the supplied PSErrorResultsException for actual
    * validation exception and create a response using that.
    * @param e assumed not <code>null</code>
    * @return PSActionResponse constructed from the error string.
    * @throws PSAAClientActionException in case of JSON exception, never happen.
    */
   private PSActionResponse handleValidationException(PSErrorResultsException e)
         throws PSAAClientActionException
   {
      JSONObject errObj = new JSONObject();
      String validationError = "";
      String cmsError = "";
      Map<IPSGuid, Object> errors = e.getErrors();
      Iterator iter = errors.keySet().iterator();
      while (iter.hasNext())
      {
         Object obj = errors.get(iter.next());
         if (obj instanceof PSErrorException)
         {
            PSErrorException ee = (PSErrorException) obj;
            Throwable t = ee.getCause();
            if (t instanceof PSFieldValidationException)
            {
               PSFieldValidationException ve = (PSFieldValidationException) t;
               PSDisplayError de = ve.getDisplayError();
               for (PSDetails dt : de.getDetails())
               {
                  for (PSFieldError fe : dt.getFieldErrors())
                  {
                     validationError += fe.getErrorText() + "\n";
                  }
               }
            }
            else
            {
               cmsError += t.getLocalizedMessage() + "\n";
            }
         }
         else if(obj instanceof Exception)
         {
            cmsError += ((Exception)obj).getLocalizedMessage() + "\n";
         }
         else
         {
            //This should not happen if happens just log it
            //@todo log in this case
         }
      }

      try
      {
         if(!StringUtils.isBlank(validationError))
            errObj.append("validationError", validationError);
         else if(!StringUtils.isBlank(cmsError))
            errObj.append("cmsError", cmsError);
      }
      catch (JSONException e1)
      {
         throw new PSAAClientActionException(e1);
      }
      return new PSActionResponse(errObj.toString(),
            PSActionResponse.RESPONSE_TYPE_JSON);
   }
}
