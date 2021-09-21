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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;

import java.util.List;

/**
 * Assembles a region.
 * 
 * @author adamgent
 * @see IPSRegionsAssembler
 *
 */
public interface IPSRegionAssembler
{
    
    /**
     * @param assemblyItem never <code>null</code>.
     * @param context never <code>null</code>.
     * @param mr never <code>null</code>.
     * @return never <code>null</code> maybe empty.
     */
    public List<PSRegionResult> assembleRegion(
            IPSAssemblyItem assemblyItem, 
            PSPageAssemblyContext context,
            PSMergedRegion mr) throws IPSTemplateService.PSTemplateException, PSAssemblyException;

}

