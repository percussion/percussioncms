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

import com.percussion.cms.PSCmsException;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.content.ui.browse.PSContentBrowser;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Implementation of the "canCreateFolder" action.
 */
public class PSCanCreateFolderAction extends PSAAActionBase
{
   /**
    * @param params the parameter expected in the map is a valid
    * {@link IPSHtmlParameters#SYS_FOLDERID parentFolderId}
    * @return String equivalent of boolean value <code>true</code> if user can
    * create a folder in the folder with given folderid, <code>false</code>
    * otherwise.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      Object obj = getParameter(params, IPSHtmlParameters.SYS_FOLDERID);
      if (obj == null || StringUtils.isBlank(obj.toString()))
         throw new IllegalArgumentException("folderid must not be ampty");
      String folderid = obj.toString();
      boolean canCreate = false;
      try
      {
         canCreate = PSContentBrowser.canCreateFolder(getRequestContext(),
            folderid);
      }
      catch (PSCmsException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(Boolean.toString(canCreate),
         PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

}
