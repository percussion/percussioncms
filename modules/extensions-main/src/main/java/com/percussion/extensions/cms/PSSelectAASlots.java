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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.extensions.cms;

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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
                     log.error("Problem loading finder, Error: {}", e);
                     log.debug(e.getMessage(), e);
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
         log.debug(e.getMessage(), e);

      }

      return resultDoc;
   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

}
