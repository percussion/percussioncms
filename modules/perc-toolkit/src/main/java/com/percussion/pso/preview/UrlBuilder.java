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
import java.util.Map;

import com.percussion.services.assembly.IPSAssemblyTemplate;
/**
 * Interface for the URL builders. The URL Builder builds a preview 
 * or Active Assembly URL for a specific template and location.  
 * 
 * @author DavidBenua
 *
 */
public interface UrlBuilder 
{
   /**
    * Builds the URL
    * @param template the template to preview or assemble.
    * @param urlParams the URL parameters. Must include the 
    * sys_contentid and sys_revision. 
    * @param location the site folder location
    * @param useMultiple does this URL point to the appropriate multisiteresolver? 
    * @return the URL.  Never <code>null</code>
    * @throws Exception
    */
   public String buildUrl(IPSAssemblyTemplate template,
         Map<String, Object> urlParams, SiteFolderLocation location,
         boolean useMultiple) throws Exception;
   
}
