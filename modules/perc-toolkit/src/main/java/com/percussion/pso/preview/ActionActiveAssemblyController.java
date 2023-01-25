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
package com.percussion.pso.preview;

import com.percussion.error.PSException;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.sitemgr.IPSSite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.mvc.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ActionActiveAssemblyController extends ActionPreviewController
  implements Controller
{

   private static final Logger log = LogManager.getLogger(ActionActiveAssemblyController.class);
   
   private boolean showSnippets = true; 
   /**
    * 
    */
   public ActionActiveAssemblyController()
   {
   }

   /**
    * Finds the eligible templates.  For Active Assembly this is limited to HMTL templates.
    * The setting of the <code>showSnippets</code> flag determines if snippet templates are also 
    * included in the Active Assembly menu.  The default is <code>true</code>.
    *
    * @see ActionPreviewController#findVisibleTemplates(String, Set)
    */
   @Override
   protected List<IPSAssemblyTemplate> findVisibleTemplates(String contentid,
         Set<IPSSite> sites) throws PSException, PSAssemblyException
   {
      initServices();
      if(log.isDebugEnabled());//do nothing for now
      List<IPSAssemblyTemplate>templates = super.findVisibleTemplates(contentid, sites); 
      List<IPSAssemblyTemplate>htmlTemplates = new ArrayList<IPSAssemblyTemplate>(); 
      for(IPSAssemblyTemplate template : templates)
      {
         if(showSnippets
               || template.getOutputFormat() == IPSAssemblyTemplate.OutputFormat.Page)
         {
            if(template.getActiveAssemblyType() == IPSAssemblyTemplate.AAType.Normal)
            {
               htmlTemplates.add(template); 
            }
         }
      }
      
      return filterVisibleTemplates(htmlTemplates, sites);
   }

 

   /**
    * Is the show snippets flag set.
    * @return the showSnippets flag.
    */
   public boolean isShowSnippets()
   {
      return showSnippets;
   }

   /**
    * Sets the show snippets flag.
    * @param showSnippets the showSnippets to set
    */
   public void setShowSnippets(boolean showSnippets)
   {
      this.showSnippets = showSnippets;
   }
   
}
