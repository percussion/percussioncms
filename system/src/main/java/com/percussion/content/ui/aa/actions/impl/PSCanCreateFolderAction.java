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
