/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.general;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.impl.PSGuidManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;

public class PSPageLinkText extends PSSimpleJavaUdfExtension
implements IPSUdfProcessor
{

   /**
    * Executes the UDF with the specified parameters and request context.
    *
    * @param params The parameter values of the exit, it is not used. It may be
    *    <code>null</code> or empty.
    *
    * @param request The current request context. It may not be 
    *    <code>null</code>.
    *
    * @return it returns the pagelinketext value for the pageid if the pageid exists in the request
    * otherwise returns empty string
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null.");
      
      String pageId = request.getParameter("percpageid");
      if(pageId == null)
      {
         return "";
      }
      String pageLinkText = "";
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      IPSGuid guid = PSGuidManagerLocator.getGuidMgr().makeGuid(pageId);
      ids.add(guid);
      IPSContentDesignWs service = 
         PSContentWsLocator.getContentDesignWebservice();
      
      List<Node> nodes= service.findNodesByIds(ids, true);
      Node node = nodes.get(0);
      try
      {
         pageLinkText = node.getProperty("rx:resource_link_title").getString();
      }
      catch (PathNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (RepositoryException e)
      {
         e.printStackTrace();
      }
      return pageLinkText;
   }
}

