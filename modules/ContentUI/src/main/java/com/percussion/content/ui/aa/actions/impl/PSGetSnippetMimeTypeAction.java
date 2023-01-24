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

import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.utils.types.PSPair;
import org.json.JSONObject;

import java.util.Map;

/**
 * Retrieves the mime type of the assembled snippet. Expects an
 * objectid for the snippet.
 */
public class PSGetSnippetMimeTypeAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String result = null;
      try
      {
         Map<String, String[]> assemblyParams = com.percussion.content.ui.aa.actions.impl.PSActionUtil.getAssemblyParams(
                  objectId, getCurrentUser());
         
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = PSActionUtil
                  .assemble(assemblyParams);
         JSONObject obj = new JSONObject();
         obj.append("mimetype", pair.getSecond().getMimeType());
        
         result = obj.toString();
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_JSON);
   }

}
