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

package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public class PSPageLinkText extends PSSimpleJavaUdfExtension
implements IPSUdfProcessor
{

   private static final Logger log = LogManager.getLogger(PSPageLinkText.class);

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
      List<IPSGuid> ids = new ArrayList<>();
      IPSGuid guid = PSGuidManagerLocator.getGuidMgr().makeGuid(pageId);
      ids.add(guid);
      IPSContentDesignWs service = 
         PSContentWsLocator.getContentDesignWebservice();
      
      List<Node> nodes= service.findNodesByIds(ids, true);
      Node node = nodes.get(0);
      try
      {
         pageLinkText = node.getProperty("rx:resource_link_title").getString();
      } catch (RepositoryException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
      return pageLinkText;
   }
}

