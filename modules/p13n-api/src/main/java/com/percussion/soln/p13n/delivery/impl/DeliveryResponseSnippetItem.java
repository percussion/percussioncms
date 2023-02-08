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
