package com.percussion.soln.p13n.delivery;

import java.util.Collection;

import com.percussion.soln.segment.Segment;

/**
 * A response item that has segments associated to it.
 * @author adamgent
 *
 */
public interface IDeliverySegmentedItem extends IDeliveryResponseItem {
    /**
     * Segments associated to this item.
     * @return never <code>null</code>.
     */
    public Collection<? extends Segment> getSegments();
}
