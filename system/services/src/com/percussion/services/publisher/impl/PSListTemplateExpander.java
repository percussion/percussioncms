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
package com.percussion.services.publisher.impl;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * The list template expander takes a list of templates as a parameter and
 * creates output content list items
 * 
 * @author dougrand
 */
public class PSListTemplateExpander extends PSBaseTemplateExpander
{
   @Override
   protected List<IPSGuid> getCandidateTemplates(Map<String, String> parameters)
         throws PSPublisherException
   {
      String templateNameStr = parameters.get("template");
      if (StringUtils.isBlank(templateNameStr))
      {
         throw new IllegalArgumentException(
               "the template parameter may not be null or empty");
      }
      String names[] = templateNameStr.split(",");
      
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();

      List<IPSGuid> rval = new ArrayList<>();
      for (String name : names)
      {
         try
         {
            rval.add(asm.findTemplateByName(name.trim()).getGUID());
         }
         catch (PSAssemblyException e)
         {
            throw new PSPublisherException(
                  IPSPublisherServiceErrors.RUNTIME_ERROR, e,
                     e.getLocalizedMessage());
         }
      }

      return rval;
   }

}
