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

package com.percussion.apibridge;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.rest.templates.ITemplatesAdaptor;
import com.percussion.rest.templates.TemplateFilter;
import com.percussion.rest.templates.TemplateSummary;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean
public class TemplateAdaptor implements ITemplatesAdaptor {

    private IPSAssemblyService asmSvc = PSAssemblyServiceLocator.getAssemblyService();
    private IPSContentWs contentwsService = PSContentWsLocator.getContentWebservice();

    public TemplateAdaptor(){
        //NOOP
    }


    public List<TemplateSummary> listAllTemplateSummaries(URI baseUri){

        List<TemplateSummary> ret =  new ArrayList<>();
        try {
            List<IPSCatalogSummary> summaries = asmSvc.getSummaries(PSTypeEnum.TEMPLATE);
            for(IPSCatalogSummary sum : summaries){
                ret.add(ApiUtils.convertTemplateSummary(sum));
            }

            return ret;

        } catch (PSCatalogException e) {
            throw new WebApplicationException(e.getMessage(),500);
        } catch (PSNotFoundException e) {
            throw new WebApplicationException("Not Found",404);
        }
    }

    /**
     * Returns all template summaries that match the supplied filter.
     * NOTE: Currently only contentid is implemented.
     * TODO: Implement for all filter options
     * @param baseUri
     * @param filter
     * @return
     */
    @Override
    public List<TemplateSummary> listTemplateSummaries(URI baseUri, TemplateFilter filter) {

        if(filter==null){
            throw new IllegalArgumentException("TemplateFilter cannot be null");
        }

        List<TemplateSummary> ret =  new ArrayList<>();
        int contentID = filter.getContentId();

        try {
            List<IPSGuid> guids = new ArrayList<>();
            guids.add(PSGuidManagerLocator.getGuidMgr().makeGuid(contentID, PSTypeEnum.LEGACY_CONTENT));
            List<PSCoreItem> items = contentwsService.loadItems(guids,false,false,false,false);
            if(items!=null && !items.isEmpty()) {
                PSCoreItem item = items.get(0);
                long contentTypeId = item.getContentTypeId();
                IPSGuid ctypeGuid  = PSGuidManagerLocator.getGuidMgr().makeGuid(contentTypeId,
                        PSTypeEnum.NODEDEF);
                List<IPSAssemblyTemplate> templates = asmSvc.findTemplatesByContentType(ctypeGuid);

                for (IPSAssemblyTemplate t : templates) {
                    ret.add(ApiUtils.convertTemplateSummary(t));
                }

                return ret;
            }else{
                throw new PSNotFoundException("Content Id: " +  contentID + " not found.");
            }

        } catch (PSAssemblyException | PSErrorResultsException e) {
            throw new WebApplicationException(e.getMessage(),500);
        } catch (PSNotFoundException e) {
            throw new WebApplicationException("Not Found",404);
        }
    }
}
