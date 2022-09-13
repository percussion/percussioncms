package com.percussion.soln.p13n.delivery.snipfilter.impl;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.snipfilter.AbstractSortingFilter;

/**
 * Sorts the items in descending order of score. The highest score
 * item is first.
 * @author adamgent
 *
 */
public class SortBasedOnScoreFilter extends AbstractSortingFilter {

    @Override
    public int compareItems(
            IDeliverySnippetFilterContext context, 
            IDeliveryResponseSnippetItem itemLeft, 
            IDeliveryResponseSnippetItem itemRight) {
        return Double.compare(itemLeft.getScore(), itemRight.getScore());
    }

}
