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
package com.percussion.webservices.assembly.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.util.PSBaseBean;
import com.percussion.webservices.assembly.IPSAssemblyWs;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * The public assembly webservice implementations.
 */
@PSBaseBean("sys_assemblyWs")
public class PSAssemblyWs extends PSAssemblyBaseWs implements IPSAssemblyWs
{
   // @see IPSAssemblyWs#loadAssemblyTemplates(String, String)
   public List<PSAssemblyTemplateWs> loadAssemblyTemplates(String name,
      String contentType)
   {
      IPSAssemblyService service = 
         PSAssemblyServiceLocator.getAssemblyService();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      if (StringUtils.isBlank(contentType))
         contentType = "*";
      contentType = StringUtils.replaceChars(contentType, '*', '%');

      List<IPSAssemblyTemplate> rval;
      try
      {
         rval = service.findTemplates(name, contentType, null, null, null,
               null, null);
      }
      catch (PSAssemblyException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException(e);
      }
      
      return getTemplateWs(rval);
   }

   // @see IPSAssemblyWs#loadSlots(String)
   public List<IPSTemplateSlot> loadSlots(String name)
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      List<IPSTemplateSlot> rval = service.findSlotsByName(name);
      return rval;
   }

   private static final Logger log = LogManager.getLogger(IPSConstants.WEBSERVICES_LOG);

}
