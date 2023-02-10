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

import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyRenderLinkContext;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSValidationException;

/**
 * 
 * Creates a Link context from legacy assembly items and new {@link IPSLinkableItem}s.
 * <p>
 * Factory pattern.
 * <p>
 * <em>This interface is not public</em>
 * @author adamgent
 *
 */
public interface IPSRenderLinkContextFactory
{

    /**
     * Create a link content for a content list item
     * this is used by the template expander during content list generation.
     * 
     * @param listItem never <code>null</code>.
     * @param item never <code>null</code>.
     * @return never <code>null</code>.
     */
    public PSRenderLinkContext create(PSContentListItem listItem, IPSLinkableItem item) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException, PSValidationException;
    /**
     * 
     * Creates a link context that has not been validated yet.
     * 
     * @param assemblyItem never <code>null</code>.
     * @param item never <code>null</code>.
     * @return never <code>null</code>.
     */
    public abstract PSRenderLinkContext create(IPSAssemblyItem assemblyItem, IPSLinkableItem item) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException, PSFilterException, PSValidationException;

    /**
     * 
     * Creates a link context that has not been validated yet.
     * 
     * @param page never <code>null</code>.
     * @return never <code>null</code>.
     */
    public abstract PSRenderLinkContext createPreview(PSPage page) throws IPSDataService.DataServiceNotFoundException, PSValidationException;
    
    /**
     * 
     * Creates a link context for an image that has not been validated yet.
     * @param folderPath never <code>null</code>.
     * @param asset never <code>null</code>.
     * @return never <code>null</code>.
     */
    public abstract PSRenderLinkContext createAssetPreview(String folderPath, PSAssetSummary asset);
}
