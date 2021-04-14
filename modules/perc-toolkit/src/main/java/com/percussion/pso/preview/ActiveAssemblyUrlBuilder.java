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

import com.percussion.pso.utils.PSOMutableUrl;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   private static Log log = LogFactory.getLog(ActiveAssemblyUrlBuilder.class);

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
      log.debug("new url is  " + url.toString()); 
      return url.toString();
   }
   
   protected static final String BASE_AA_URL = "/Rhythmyx/sys_action/checkoutaapage.xml";
}
