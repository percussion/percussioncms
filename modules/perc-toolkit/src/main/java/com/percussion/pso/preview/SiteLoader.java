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

import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.PSSiteManagerException;

/**
 * Site Loader interface. The site loader finds all sites defined 
 * in the system.  
 * 
 * @author DavidBenua
 *
 */
public interface SiteLoader
{
   /**
    * Finds all sites defined in the system. 
    * @return the list of sites. Never <code>null</code> but may
    * be <code>empty</code>
    * @throws PSSiteManagerException
    */
   public List<IPSSite> findAllSites() throws PSSiteManagerException;
}
