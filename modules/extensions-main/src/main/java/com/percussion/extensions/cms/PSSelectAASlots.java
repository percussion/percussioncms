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
package com.percussion.extensions.cms;

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Filter the set of slots returned by contentvariantslotlist
 * 
 * @author dougrand
 */
public class PSSelectAASlots implements IPSResultDocumentProcessor
{
   private static final Logger log = LogManager.getLogger(PSSelectAASlots.class);

   public boolean canModifyStyleSheet()
   {
      return false;
   }

   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params.length < 1)
         return resultDoc;

      String filter = params[0] == null ? "false" : params[0].toString();
      
      if (!filter.equalsIgnoreCase("true"))
      {
         return resultDoc;
      }

      // Walk the slot elements, removing those whose finder is not null and
      // not in the list of AA finder
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, Boolean> aafinder = new HashMap<>();

      NodeList children = resultDoc.getDocumentElement().getChildNodes();
      Set<Element> nodesToRemove = new HashSet<>();
      int count = children.getLength();
      try
      {
         for (int i = 0; i < count; i++)
         {
            Element slot = (Element) children.item(i);
            String findername = slot.getAttribute("finder");
            if (!StringUtils.isBlank(findername))
            {
               Boolean good = aafinder.get(findername);
               if (good == null)
               {
                  try
                  {
                     IPSSlotContentFinder finder = asm.loadFinder(findername);
                     good = finder.getType().isActivateable();
                     aafinder.put(findername, good);
                  }
                  catch (PSAssemblyException e)
                  {
                     log.error("Problem loading finder, Error: {}",
                             PSExceptionUtils.getMessageForLog(e));
                     log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                  }
               }
               if (!good)
               {
                  nodesToRemove.add(slot);
                  // And remove the next as it is associated if there is any
                  if ((i+1) < count)
                  {
                     Element next = (Element) children.item(i + 1);
                     if (next.getNodeName().equalsIgnoreCase("slotitemsurl"))
                     {
                        nodesToRemove.add((Element) children.item(i + 1));
                     }
                  }
               }
            }
         }
         for(Element slot : nodesToRemove)
         {
            resultDoc.getDocumentElement().removeChild(slot);
         }
      }
      catch (Exception e)
      {
         log.error("Problem while filtering slots, Error: {}", e);
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));

      }

      return resultDoc;
   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

}
