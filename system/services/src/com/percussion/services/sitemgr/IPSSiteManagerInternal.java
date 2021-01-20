/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.sitemgr;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.assembly.impl.PSAssemblyBaseWs;

import java.util.Collection;
import java.util.Map;

/**
 * This is used by internal code, not exposed to general public. 
 * Hopefully as a temporary interface. It should be removed after we implemented
 * all the TODOs.
 */
public interface IPSSiteManagerInternal extends IPSSiteManager
{
   /**
    * Finds the Site and Templates associations. This is not exposed in
    * {@link IPSSiteManager} because the map key is not consistent with map
    * value, but we need the ID/Name pair in
    * {@link PSAssemblyBaseWs#getTemplateWs}.
    * 
    * @TODO enhance {@link #getSummaries(PSTypeEnum)} to use projection to load
    * the object so that it can be used to result ID/Name mapping.
    * 
    * @return the association map, where the map key is Site ID/Name, which maps
    * to a collection of associated Template IDs. The collection of Template
    * IDs is never <code>null</code>, but may be empty. The returned map can
    * never be <code>null</code>, but may be empty.
    */
   Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> findSiteTemplatesAssociations();
}
