package com.percussion.soln.p13n.delivery.impl;

import java.util.Collection;

import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;
import com.percussion.soln.segment.Segment;

public class DeliveryResponseSnippetItem extends DeliverySegmentedItem implements IDeliveryResponseSnippetItem {
    
    private DeliverySnippetItem itemData;
    private String style;
    private double rank = 0;
    private int sortIndex = 0;


    public DeliveryResponseSnippetItem(DeliverySnippetItem itemData, Collection<? extends Segment> segments) {
        super(segments);
        this.itemData = itemData;
    }

    public String getRendering() throws IllegalStateException {
        return itemData.getRendering();
    }


    @Override
    public DeliverySnippetItem getItemData() {
        return itemData;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public double getScore() {
        return rank;
    }

    public void setScore(double rank) {
        this.rank = rank;
    }

    public String getId() {
        return "" + this.itemData.getId();
    }

    
    public int getSortIndex() {
        return sortIndex;
    }

    
    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

}
