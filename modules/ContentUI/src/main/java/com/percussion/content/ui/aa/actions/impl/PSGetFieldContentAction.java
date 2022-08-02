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

import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Retrieves the assembled html content for the specified
 * field. Expects an objectid for the snippet.
 */
public class PSGetFieldContentAction extends PSAAActionBase
{

   // see interface for more detail
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String isAAMode = (String)getParameter(params, "isaamode");
      String sys_aamode = (String)getParameter(params,
         IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE);
      String result;
      try
      {
         Map<String, String[]> assemblyParams = 
            com.percussion.content.ui.aa.actions.impl.PSActionUtil.getAssemblyParams(objectId, getCurrentUser());
         com.percussion.content.ui.aa.actions.impl.PSActionUtil.addAssemblyParam(assemblyParams,
            IPSHtmlParameters.SYS_PART, "field:" + objectId.getFieldName());
         if(StringUtils.isNotBlank(isAAMode))
         {
            if(isAAMode.equalsIgnoreCase("true"))
            {
               com.percussion.content.ui.aa.actions.impl.PSActionUtil.addAssemblyParam(assemblyParams,
                  IPSHtmlParameters.SYS_COMMAND, 
                  IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY);
               if(StringUtils.isNotBlank(sys_aamode))
               {
                  com.percussion.content.ui.aa.actions.impl.PSActionUtil.addAssemblyParam(assemblyParams,
                     IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE, 
                     sys_aamode);
               }
            }
         }
         PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = 
            PSActionUtil.assemble(assemblyParams);
         result = new String(pair.getSecond().getResultData(), StandardCharsets.UTF_8);
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result,
               PSActionResponse.RESPONSE_TYPE_HTML);
   }

}
