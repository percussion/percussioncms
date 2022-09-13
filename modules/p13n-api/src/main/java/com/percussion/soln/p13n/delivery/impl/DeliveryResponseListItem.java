package com.percussion.soln.p13n.delivery.impl;

import java.util.Collection;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.data.DeliveryItem;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.segment.Segment;

public class DeliveryResponseListItem extends DeliverySegmentedItem implements IDeliveryResponseListItem {

    private DeliveryListItem data;

    public DeliveryResponseListItem(DeliveryListItem data, Collection<? extends Segment> segments) {
        super(segments);
        this.data = data;
    }

    public List<String> getSnippetFilterIds()  {
        return data.getSnippetFilterIds();
    }

    @Override
    public DeliveryItem getItemData() {
        return data;
    }

    public String getId() {
        return "" + data.getId();
    }




}
