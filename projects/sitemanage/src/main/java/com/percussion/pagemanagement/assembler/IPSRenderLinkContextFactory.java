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