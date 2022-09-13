package com.percussion.soln.p13n.delivery.impl;

import static com.percussion.soln.p13n.delivery.impl.DeliverySegmentUtil.getVisitorProfileSegments;
import static com.percussion.soln.p13n.delivery.impl.DeliverySegmentUtil.weightDescending;
import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;

public class DeliveryContext implements IDeliverySnippetFilterContext {

    private IDeliveryResponseListItem responseListItem;
    private VisitorProfile visitorProfile;
    private Collection<? extends Segment> visitorSegments;
    private ISegmentService segmentService;
    private List<IDeliveryResponseSnippetItem> responseSnippets;
    private boolean safeToExecuteFilters = true;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DeliveryContext.class);
    
    /**
     * For serializers only.
     */
    public DeliveryContext() {
    }
    
    public DeliveryContext(IDeliveryResponseListItem listItem, 
            List<IDeliveryResponseSnippetItem> responseSnippets,
            VisitorProfile visitorProfile,
            ISegmentService segmentService) throws DeliveryException {
        super();
        notNull(listItem, "responseListItem cannot be null");
        notNull(visitorProfile, "visitorProfile cannot be null");
        notNull(segmentService, "segmentService cannot be null");
        notNull(responseSnippets, "responseSnippets cannot be null");
        this.responseListItem = listItem;
        this.visitorProfile = visitorProfile;
        this.segmentService = segmentService;
        this.visitorSegments = getVisitorProfileSegments(segmentService, visitorProfile, log); 
        this.responseSnippets = responseSnippets;
    }
    
    
    public DeliveryContext(IDeliveryResponseListItem listItem, List<IDeliveryResponseSnippetItem> snippets) {
        if (listItem == null) throw new IllegalArgumentException("responseListItem cannot be null");
        if (snippets == null) throw new IllegalArgumentException("responseSnippets cannot be null");
        this.responseListItem = listItem;
        this.responseSnippets = snippets;
        this.safeToExecuteFilters = false;
    }
    
    public IDeliveryResponseListItem getResponseListItem() {
        return responseListItem;
    }

    public List<Segment> getVisitorSegments() {
        if (visitorSegments == null) return Collections.emptyList();
        return weightDescending(this, visitorSegments);    
    }

    public List<IDeliveryResponseSnippetItem> getResponseSnippetItems() {
        return responseSnippets;
    }

    public String getProfileId() {
        return String.valueOf(visitorProfile.getId());
    }

    public int getVisitorSegmentWeight(Segment segment) {
        if (segment == null) return 0;

        if (visitorProfile == null) return 0;
        Map<String, Integer> w = visitorProfile.getSegmentWeights();
        List<String> segIds = new ArrayList<String>();
        segIds.add(segment.getFolderPath());
        segIds.add(segment.getId());
        segIds.add(segment.getName());
        if (segment.getAliases() != null)
            segIds.addAll(segment.getAliases());
        return getWeight(w, segIds);
    }
    
    private int getWeight(Map<String, Integer> weights, Collection<String> identifiers) {
        if (weights == null)
            return 0;
        
        for (String i : identifiers) {
        	//Defend against strange behavior where Map actually contains a String value
        	Object o = weights.get(i);
        	Integer w = null;
        	if(o instanceof Integer)
        		w = (Integer) o;

        	if (w != null)
                return w;
        }
        return 0;
    }

    public String getVisitorUserId() {
        if (visitorProfile == null) return "";
        return visitorProfile.getUserId();
    }
    
    public boolean isSafeToRunFilters() {
        return safeToExecuteFilters;
    }

    public void setSafeToRunFilters(boolean safeToExecuteFilters) {
        this.safeToExecuteFilters = safeToExecuteFilters;
    }

    public ISegmentService getSegmentService() {
        return segmentService;
    }


    public void setSegmentService(ISegmentService segmentService) {
        this.segmentService = segmentService;
    }
    
    
}
