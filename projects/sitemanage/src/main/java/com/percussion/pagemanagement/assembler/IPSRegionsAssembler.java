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

import java.util.Collection;

/**
 * Batch Assembles a collection of regions.
 * 
 * @author adamgent
 * @see IPSRegionAssembler
 *
 */
public interface IPSRegionsAssembler
{

    /**
     * Assembles a collection of regions
     * by using the given {@link IPSRegionAssembler regionAssembler} and
     * then stores the {@link PSRegionResult results} into the 
     * {@link PSPageAssemblyContext#getRegions() context region results map}.
     * 
     * @param regionAssembler never <code>null</code>.
     * @param assemblyItem never <code>null</code>.
     * @param context The results of given {@link IPSRegionAssembler} should be stored in the context. Never <code>null</code>.
     * @param mergedRegions never <code>null</code>.
     * @see PSPageAssemblyContext#getRegions()
     */
    public void assembleRegions(
            IPSRegionAssembler regionAssembler,
            IPSAssemblyItem assemblyItem, 
            PSPageAssemblyContext context,
            Collection<PSMergedRegion> mergedRegions) throws IPSTemplateService.PSTemplateException, PSAssemblyException;

}

