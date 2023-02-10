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
import java.util.List;
public interface SiteFolderFinder
{
   /**
    * Find the possible site folder previews for an item.  The set of possible site id / folder id pairs
    * is restricted by the optional folder id and site id parameters.
    * The return value is a List of Maps. Each Map contains the <code>sys_siteid</code> and <code>sys_folderid</code>
    * for the selected folder.  Other keys in the map are "sitename" and "fullpath".    
    * @param contentid the content id of the selected item.  Must not be null or empty. 
    * @param folderid the folder id, if known.  Leave this blank if you do not know the folder id. 
    * @param siteid
    * @return the List of possible site folders. 
    * @throws Exception
    */
   public List<SiteFolderLocation> findSiteFolderLocations(String contentid,
         String folderid, String siteid) throws Exception;
}
