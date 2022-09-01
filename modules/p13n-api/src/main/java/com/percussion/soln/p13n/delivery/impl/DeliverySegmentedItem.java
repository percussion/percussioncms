package com.percussion.soln.p13n.delivery.impl;

import java.util.Collection;

import com.percussion.soln.segment.Segment;

public abstract class DeliverySegmentedItem extends AbstractDeliveryResponseItem {
    private Collection<? extends Segment> segments;
    
    public Collection<? extends Segment> getSegments() {
        return this.segments;
    }
    
    public DeliverySegmentedItem(Collection<? extends Segment> segments) {
        super();
        this.segments = segments;
    }
    

}
