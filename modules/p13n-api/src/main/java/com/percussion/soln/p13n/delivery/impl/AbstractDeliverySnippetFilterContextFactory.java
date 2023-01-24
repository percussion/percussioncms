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

package com.percussion.soln.p13n.delivery.impl;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContextFactory;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService.DeliveryDataException;
import com.percussion.soln.p13n.delivery.impl.DeliverySegmentUtil.SegmentResults;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;

public abstract class AbstractDeliverySnippetFilterContextFactory implements IDeliverySnippetFilterContextFactory {
    
    private ISegmentService segmentService;
    
    

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(AbstractDeliverySnippetFilterContextFactory.class);
    
    public IDeliverySnippetFilterContext createContext(DeliveryRequest request) 
        throws DeliveryException, DeliveryDataException, DeliveryContextException {
        
        if (getSegmentService() == null) 
            throw new DeliveryContextException("Segment Service is not wired (spring)");
        
        if (log.isDebugEnabled())
            log.debug("Creating context for request: " + request);
        
        DeliveryListItem listItem = getListItem(request);
        
        if (listItem == null) {
            throw new DeliveryContextException(format("No {0} in request or for id: {1}", 
                    DeliveryListItem.class.getSimpleName(), request.getListItemId()));
        }
        
        IDeliveryResponseListItem responseListItem = createResponseListItem(listItem);
        
        List<IDeliveryResponseSnippetItem> contentItems = createResponseSnippetItems(listItem);

        VisitorProfile profile = request.getVisitorProfile();
        
        IDeliverySnippetFilterContext context = createContext(responseListItem, contentItems, profile);
        
        return context;
        

    }
    
    /**
     * Gets or creates the pre-processed list for the request.
     * @param request never <code>null</code>.
     * @return <code>null</code> indicates an item could not created or found from the request.
     * @throws DeliveryException
     */
    protected abstract DeliveryListItem getListItem(DeliveryRequest request) 
        throws DeliveryException;
    
    /**
     * Creates a context used for snippet filtering.
     * @param responseListItem never <code>null</code>.
     * @param snips maybe empty but never <code>null</code>.
     * @param profile never <code>null</code>.
     * @return never <code>null</code>.
     * @throws DeliveryException
     */
    protected IDeliverySnippetFilterContext createContext(
            IDeliveryResponseListItem responseListItem, 
            List<IDeliveryResponseSnippetItem> snips, 
            VisitorProfile profile) throws DeliveryException {
        if (profile == null) {
            return new DeliveryContext(responseListItem, snips);
        }
        return new DeliveryContext(responseListItem, snips, profile, getSegmentService());
    }
    
    protected IDeliveryResponseListItem createResponseListItem(DeliveryListItem listItem) throws DeliveryException {
        ArrayList<String> segmentIds = new ArrayList<String>();
        if (listItem.getSegmentIds() != null)
            segmentIds.addAll(listItem.getSegmentIds());
        SegmentResults sr = DeliverySegmentUtil.findSegments(getSegmentService(), segmentIds);
        sr.logInvalid(listItem, log);
        Collection<? extends Segment> segments = sr.getSegments();
        return createResponseListItem(listItem, segments);
    }

    protected DeliveryResponseListItem createResponseListItem(DeliveryListItem listItem, Collection<? extends Segment> segments) {
        return new DeliveryResponseListItem(listItem, segments);
    }
    
    protected List<IDeliveryResponseSnippetItem> createResponseSnippetItems(DeliveryListItem listItem) 
        throws DeliveryException {
        List<IDeliveryResponseSnippetItem> responseSnippets = new ArrayList<IDeliveryResponseSnippetItem>();
        if ( listItem.getSnippets() != null ) {
            int i = 1;
            for(DeliverySnippetItem snip : listItem.getSnippets()) {
                List<String> segIds = new ArrayList<String>(snip.getSegmentIds());
                SegmentResults sr = DeliverySegmentUtil.findSegments(getSegmentService(), segIds);
                Collection<? extends Segment> segments = sr.getSegments();
                sr.logInvalid(snip, log);
                DeliveryResponseSnippetItem item = createResponseSnippetItem(snip, segments);
                //Set the original sort index.
                item.setSortIndex(i); i++;
                responseSnippets.add(item);
            }
        }
        
        return responseSnippets;
    }
    
    protected DeliveryResponseSnippetItem createResponseSnippetItem(DeliverySnippetItem snip, Collection<? extends Segment> segments) {
        return new DeliveryResponseSnippetItem(snip, segments);
    }

    public ISegmentService getSegmentService() {
        return segmentService;
    }

    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }

}
