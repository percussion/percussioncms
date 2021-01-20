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

      List<IPSGuid> rval = new ArrayList<IPSGuid>();
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
