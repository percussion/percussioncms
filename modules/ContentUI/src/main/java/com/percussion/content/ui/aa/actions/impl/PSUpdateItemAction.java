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

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSDetails;
import com.percussion.design.objectstore.PSDisplayError;
import com.percussion.design.objectstore.PSFieldError;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.Map;

/**
 * Action to update the item. Gets the content editor url from ceUrl parameter
 * and creates an internal request to the content editor. Calls the update
 * method to update the item. If succeeds returns success, If there are any
 * validation errors, returns a JSON object with parameter validationError and
 * value as concatenated string of all validation error messages and the
 * ceCachedPageUrl parameter is added with the cached page URL.
 */
public class PSUpdateItemAction extends PSAAActionBase
{

   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      try
      {
         IPSRequestContext req = getRequestContext();
         String ceUrl = (String) getParameter(params, "ceUrl");
         IPSInternalRequest ireq = req.getInternalRequest(ceUrl);
         IPSRequestContext ureq = ireq.getRequestContext();
         // reset validation error object
         ureq.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR, null);
         ureq.setParameter(IPSHtmlParameters.SYS_CE_CACHED_PAGEURL, null);
         ireq.performUpdate();
         String validateError = ureq
               .getParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR);
         if (!StringUtils.isBlank(validateError))
         {
            PSDisplayError de = new PSDisplayError(validateError);
            String validationError = "";
            for (PSDetails dt : de.getDetails())
            {
               for (PSFieldError fe : dt.getFieldErrors())
               {
                  validationError += fe.getErrorText() + "\n";
               }
            }
            String cachedUrl = ureq
                  .getParameter(IPSHtmlParameters.SYS_CE_CACHED_PAGEURL);
            JSONObject errObj = new JSONObject();
            errObj.append("validationError", validationError);
            errObj.append("ceCachedPageUrl", cachedUrl);
            return new PSActionResponse(errObj.toString(),
                  PSActionResponse.RESPONSE_TYPE_JSON);
         }
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
}
