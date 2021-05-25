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
package com.percussion.pagemanagement.assembler.impl;
import static com.percussion.util.IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN;

import com.percussion.pagemanagement.assembler.IPSRegionAssembler;
import com.percussion.pagemanagement.assembler.IPSRegionsAssembler;
import com.percussion.pagemanagement.assembler.PSMergedRegion;
import com.percussion.pagemanagement.assembler.PSPageAssemblyContext;
import com.percussion.pagemanagement.assembler.PSRegionResult;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A non-concurrent batch regions assembler.
 * The regions will be assembled in order and with the same
 * thread as the caller.
 * 
 * @author adamgent
 *
 */
@PSSiteManageBean
public class PSSerialRegionsAssembler implements IPSRegionsAssembler
{

    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSSerialRegionsAssembler.class);

    public PSSerialRegionsAssembler() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assembleRegions(
            IPSRegionAssembler regionAssembler, 
            IPSAssemblyItem assemblyItem,
            PSPageAssemblyContext context, 
            Collection<PSMergedRegion> mergedRegions) throws IPSTemplateService.PSTemplateException {
        // hack pulled in from PSConcurrentRegionsAssembler
        setPreviewUrlGenerator();
        
        for(PSMergedRegion mr : mergedRegions ) {
            List<PSRegionResult> results = 
                regionAssembler.assembleRegion(assemblyItem, context, mr);
            context.getRegions().put(mr.getRegionId(), results);
        }
    }
    

    /**
     * Set the "custom" preview URL generator on the request, as a parameter to signal location generator, PSGeneratePubLocation, 
     * to use PSGeneratePreviewLink to generate "friendly URL" in the content of a previewed page.
     **/
    private void setPreviewUrlGenerator()
    {
        IPSAssemblyService service = PSAssemblyServiceLocator.getAssemblyService();
        try
        {
            // As we don't have a good way to use "friendly URL" to preview the thumbnail, 
            // so we don't want to generate "friendly URL" if the template is used to render a thumbnail image.  
            IPSAssemblyTemplate template = service.findTemplateByName("perc.imageThumbBinary");
            if (template != null)
            {
                int imageThumbTemplateId = template.getGUID().getUUID();
                String[] values = new String[] { "global/percussion/contentassembler/perc_casGeneratePreviewLink",
                        String.valueOf(imageThumbTemplateId) };
                PSRequest req = (PSRequest) PSRequestInfo
                        .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
                req.setPrivateObject(SYS_OVERWRITE_PREVIEW_URL_GEN,  values);
            }
        }
        catch (PSAssemblyException e)
        {
            log.error("Failed to find \"perc.imageThumbBinary\" template", e);
        }
    }
}
