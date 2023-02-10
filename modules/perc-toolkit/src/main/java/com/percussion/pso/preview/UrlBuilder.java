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
