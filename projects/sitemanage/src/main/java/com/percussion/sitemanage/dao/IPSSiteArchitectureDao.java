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
package com.percussion.sitemanage.dao;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.data.PSSiteArchitecture;
import com.percussion.sitemanage.data.PSSiteSection;

import java.util.List;

/**
 * Interface for the site architecture Dao. It extends IPSGenericDao and the get
 * method returns the site architecture. The architecture includes the site
 * details and section under it expanded to first level. To get the sub sections
 * of any section, callers should use getSiteSubSections method.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSSiteArchitectureDao extends
      IPSGenericDao<PSSiteArchitecture, String>
{
   /**
    * Returns the subsections of the given item.
    * @param id Must be a valid guid of the navigation type item.
    * @return The sub sections of the given item. 
    * @throws LoadException
    */
   public List<PSSiteSection> getSections(String id) throws LoadException;
}
