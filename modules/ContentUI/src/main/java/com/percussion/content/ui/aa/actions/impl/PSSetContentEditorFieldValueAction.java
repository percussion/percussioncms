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
