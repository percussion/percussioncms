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
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the url for Active Assembly. Extends the Preview URL 
 * functionality for AA Urls.   
 * 
 *
 * @author DavidBenua
 *
 */
public class ActiveAssemblyUrlBuilder extends PreviewUrlBuilder implements UrlBuilder, Cloneable
{

   private static final Logger log = LogManager.getLogger(ActiveAssemblyUrlBuilder.class);

   /**
    * Builds the active assembly URL. 
    * @see PreviewUrlBuilder#buildUrl(IPSAssemblyTemplate, Map, SiteFolderLocation, boolean)
    */
   @Override
   public String buildUrl(IPSAssemblyTemplate template, Map<String,Object> urlParams, 
         SiteFolderLocation location, boolean useMultiple ) throws Exception
   {
      log.debug("building active assembly url"); 
      PSOMutableUrl url;
      String templateid = findTemplateId(template);
      String defaultLoc = this.getDefaultLocationUrl();
      if(useMultiple)
      {
         url = new PSOMutableUrl(this.getMultipleLocationUrl());
      }
      else 
      {
         url = new PSOMutableUrl(this.getDefaultLocationUrl());
      }
      String assyUrl = template.getAssemblyUrl();
      if(StringUtils.isBlank(assyUrl))
      {
         assyUrl = DEFAULT_ASSY_URL; 
      }

      Map<String,Object> newParams = new HashMap<String, Object>(urlParams); 
      if(location != null)
      {
         newParams.putAll(location.getParameterMap());
      }
      newParams.put(IPSHtmlParameters.SYS_CONTEXT, "0"); 
      newParams.put(IPSHtmlParameters.SYS_COMMAND, "editrc"); 
      newParams.put("parentPage", "yes");
      newParams.put(IPSHtmlParameters.SYS_VARIANTID, templateid); 
      newParams.put("sys_assemblyurl", URLEncoder.encode(fixupUrl(assyUrl), "UTF-8")); 
      
      url.setParamList(newParams); 
      log.debug("new url is {}", url.toString());
      return url.toString();
   }
   
   protected static final String BASE_AA_URL = "/Rhythmyx/sys_action/checkoutaapage.xml";
}
