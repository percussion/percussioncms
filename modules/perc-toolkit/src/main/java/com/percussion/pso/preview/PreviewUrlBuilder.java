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

import com.percussion.pso.utils.PSOMutableUrl;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a URL for Preview. If the assembly URL is not known, use 
 * the default URL.  
 *
 * @author DavidBenua
 *
 */
public class PreviewUrlBuilder implements UrlBuilder, Cloneable
{

   private static final Logger log = LogManager.getLogger(PreviewUrlBuilder.class);
   
   private String defaultLocationUrl; 
   
   private String multipleLocationUrl;
   /**
    * 
    */
   public PreviewUrlBuilder()
   {
     
   }
   

   /**
    * @see UrlBuilder#buildUrl(IPSAssemblyTemplate, Map, SiteFolderLocation, boolean)
    */
   public String buildUrl(IPSAssemblyTemplate template, Map<String,Object> urlParams, 
         SiteFolderLocation location, boolean useMultiple) throws Exception
   {
      PSOMutableUrl url;
      String defaultLoc = this.getDefaultLocationUrl(); 
      
      String templateid = findTemplateId(template); 
      if(useMultiple)
      {
         url = new PSOMutableUrl(this.getMultipleLocationUrl());
         log.debug("multiple url is : {}", url.toString());
      }
      else
      {  
         url = new PSOMutableUrl(this.getDefaultLocationUrl());
         log.debug("single url is : {}", url.toString());
      }
      Map<String,Object> newParams = new HashMap<String, Object>(urlParams); 
      if(location != null)
      {
         newParams.putAll(location.getParameterMap());
      }
      newParams.put(IPSHtmlParameters.SYS_ITEMFILTER, "preview");
      newParams.put(IPSHtmlParameters.SYS_CONTEXT, "0"); 
      
      newParams.put(IPSHtmlParameters.SYS_TEMPLATE, templateid); 
      
      url.setParamList(newParams); 
      log.debug("new url is {}", url.toString());
      return url.toString();
   }

   /**
    * Find the template id for the given template. 
    * @param template
    * @return the template id. 
    */
   protected String findTemplateId(IPSAssemblyTemplate template)
   {
      String templateid = String.valueOf(template.getGUID().getUUID());
      log.debug("templateid is {}", templateid);
      return templateid; 
   }
   
   /**
    * Fix up the base url.  Replace an initial <code>../</code>
    * with the <code>/Rhythmyx</code>. 
    * @param urlbase the base url. 
    * @return the fixed base url. 
    */
   protected String fixupUrl(String urlbase)
   {
      if(urlbase.startsWith("../"))
      {
         urlbase = urlbase.replace("../", "/Rhythmyx/"); 
      }
      return urlbase;
   }
   
   /**
    * Default Assembler Url
    */
   protected static final String DEFAULT_ASSY_URL = "/Rhythmyx/assembler/render";
   /**
    * @return the defaultLocationUrl
    */
   public String getDefaultLocationUrl()
   {
      return defaultLocationUrl;
   }

   /**
    * @param defaultLocationUrl the defaultLocationUrl to set
    */
   public void setDefaultLocationUrl(String defaultLocationUrl)
   {
      this.defaultLocationUrl = defaultLocationUrl;
   }


   /**
    * @return the multipleLocationUrl
    */
   public String getMultipleLocationUrl()
   {
      return multipleLocationUrl;
   }


   /**
    * @param multipleLocationUrl the multipleLocationUrl to set
    */
   public void setMultipleLocationUrl(String multipleLocationUrl)
   {
      this.multipleLocationUrl = multipleLocationUrl;
   }
   
}
