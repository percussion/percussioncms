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

import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Retrieves the assembled html content for the specified snippet. Expects an
 * objectid for the snippet.
 */
public class PSGetSnippetContentAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String isAAMode = (String) getParameter(params, "isaamode");
      String sys_aamode = (String) getParameter(params,
               IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE);
      String selectedtext = (String) getParameter(params,
               PSInlineLinkField.RX_SELECTEDTEXT);
      String result = null;
      try
      {
         Map<String, String[]> assemblyParams = com.percussion.content.ui.aa.actions.impl.PSActionUtil.getAssemblyParams(
                  objectId, getCurrentUser());
         if (StringUtils.isNotBlank(isAAMode))
         {
            if (isAAMode.equalsIgnoreCase("true"))
            {
               com.percussion.content.ui.aa.actions.impl.PSActionUtil.addAssemblyParam(assemblyParams,
                        IPSHtmlParameters.SYS_COMMAND,
                        IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY);
               if (StringUtils.isNotBlank(sys_aamode))
               {
                  com.percussion.content.ui.aa.actions.impl.PSActionUtil.addAssemblyParam(assemblyParams,
                           IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE,
                           sys_aamode);
               }
            }
         }
         if (StringUtils.isNotBlank(selectedtext))
         {
            com.percussion.content.ui.aa.actions.impl.PSActionUtil.addAssemblyParam(assemblyParams,
               PSSingleValueBuilder.INLINE_TEXT, selectedtext);
         }
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = com.percussion.content.ui.aa.actions.impl.PSActionUtil
                  .assemble(assemblyParams);
         result = PSActionUtil.getBodyContent(pair.getSecond());
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_HTML);
   }

}
