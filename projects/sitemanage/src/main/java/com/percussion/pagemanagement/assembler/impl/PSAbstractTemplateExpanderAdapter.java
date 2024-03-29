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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.IPSTemplateExpander;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.Validate.noNullElements;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * An adapter to the Template expander extension point in cm system
 * which makes it easier to implement template expanders.

 * {@link #createTemplateCache()}.
 * @author adamgent
 * @param <CACHE> template cache
 *
 */
public abstract class PSAbstractTemplateExpanderAdapter<CACHE> implements IPSTemplateExpander
{

    private static final Logger log = LogManager.getLogger(PSAbstractTemplateExpanderAdapter.class);

    /**
     * Return a template guid or <code>null</code>.
     * If <code>null</code> the item will not be expanded which means
     *  will not be run and the
     * item will be skipped. 
     * @param parameters the parameters of the expander, never <code>null</code>.
     * @param templateCache never <code>null</code>..
     * @return a template id that can be <code>null</code>.
     */
    protected abstract IPSGuid getTemplateId(Map<String, String> parameters, CACHE templateCache) throws PSDataServiceException, PSAssemblyException;
    
    /**
     * Creates a new template cache that will be used for the current publishing job.
     * Called only once per content list.
     * @return never <code>null</code>.
     */
    protected abstract CACHE createTemplateCache();
    
    /**
     * Should return a list that is inclusive of the original item passed if
     * they want the item passed to be published.
     * To create new content list items from the passed item use
     * {@link #clone(PSContentListItem)}.
     * 
     * @param contentListItem never <code>null</code>.
     * @param parameters the parameters of the expander, never <code>null</code>.
     * 
     * @return never <code>null</code> maybe empty.
     * 
     * @see #clone(PSContentListItem)
     */
    protected abstract List<PSContentListItem> expandContentListItem(PSContentListItem contentListItem, Map<String, String> parameters) throws PSDataServiceException;
    
    @Override
    public List<PSContentListItem> expand(QueryResult results, Map<String, String> parameters,
            Map<Integer, PSComponentSummary> summaryMap) throws PSPublisherException
    {
        notNull(results, "results");
        notEmpty(parameters, "parameters");
        List<PSContentListItem> contentListItems = new ArrayList<>();
        IPSGuid siteId = getSiteId(parameters);
        int context = getContext(parameters);
        

        try
        {

            RowIterator riter = results.getRows();
            CACHE cache = createTemplateCache();
            while (riter.hasNext())
            {
                Row r = riter.nextRow();
                IPSGuid contentId = getContentItemGuid(r, summaryMap);
                IPSGuid folderId = getFolderGuid(r);
                IPSGuid templateId=null;
                try {
                    templateId = getTemplateId(parameters, cache);
                } catch (PSDataServiceException | PSAssemblyException e) {
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                    //Continue processing
                }
                if (templateId != null) {
                    PSContentListItem item = createContentListItem(contentId, folderId, templateId, siteId, context);
                    List<PSContentListItem> items = expandContentListItem(item, parameters);
                    noNullElements(items, "contentListItems from expander");
                    contentListItems.addAll(items);
                }
                
            }
        }
        catch (RepositoryException | PSDataServiceException e)
        {
            throw new PSPublisherException(IPSPublisherServiceErrors.RUNTIME_ERROR, e, e.getLocalizedMessage());
        }

        return contentListItems;
    }
    
    
    protected PSContentListItem createContentListItem(IPSGuid contentId, IPSGuid folderId, IPSGuid templateId, IPSGuid siteId, Integer context) {
        return new PSContentListItem(contentId, folderId, templateId, siteId, context);
    }
    
    protected IPSGuid getSiteId(Map<String, String> parameters) {
        String siteid = parameters.get(IPSHtmlParameters.SYS_SITEID);
        IPSGuid siteg = null;
        if (!StringUtils.isBlank(siteid))
        {
            siteg = new PSGuid(PSTypeEnum.SITE, siteid);
        }
        return siteg;
    }
    
    protected int getContext(Map<String, String> parameters) {
        String ctx = parameters.get(IPSHtmlParameters.SYS_CONTEXT);
        String deliveryctx = parameters.get(IPSHtmlParameters.SYS_DELIVERY_CONTEXT);
        int context = 0;
        if (deliveryctx != null) {
            context = Integer.parseInt(deliveryctx);
        }
        else if (ctx != null) {
            context = Integer.parseInt(ctx);
        }
        else {
            throw new RuntimeException("Either sys_context or sys_delivery_context must be specified");
        }
        return context;
    }
    
    protected IPSGuid getContentItemGuid(Row r, Map<Integer,PSComponentSummary> summaryMap) throws RepositoryException {
        int cid = (int) r.getValue(IPSContentPropertyConstants.RX_SYS_CONTENTID).getLong();
        PSComponentSummary sum = summaryMap.get(cid);
        return new PSLegacyGuid(cid, sum.getPublicOrCurrentRevision());   
    }
    
    protected IPSGuid getFolderGuid(Row r) throws RepositoryException {
        Value folderid = r.getValue("rx:sys_folderid");
        IPSGuid fid = null;
        if (folderid != null)
        {
            fid = new PSLegacyGuid((int) folderid.getLong(), 0);
        }
        return fid;
    }
    
    
    protected PSContentListItem clone(PSContentListItem item) {
        /*
         * Sadly the real clone is broken.
         */
        return new PSContentListItem(
                item.getItemId(), 
                item.getFolderId(), 
                item.getTemplateId(), 
                item.getSiteId(), 
                item.getContext());
    }

}
