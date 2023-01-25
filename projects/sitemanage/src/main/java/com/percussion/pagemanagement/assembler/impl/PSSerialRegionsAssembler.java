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
package com.percussion.pagemanagement.assembler.impl;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;

import static com.percussion.util.IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN;

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
            Collection<PSMergedRegion> mergedRegions) throws IPSTemplateService.PSTemplateException, PSAssemblyException {
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
