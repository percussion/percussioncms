/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
