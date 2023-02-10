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

