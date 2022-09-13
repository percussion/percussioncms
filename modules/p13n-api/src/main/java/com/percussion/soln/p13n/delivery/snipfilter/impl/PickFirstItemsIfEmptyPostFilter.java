package com.percussion.soln.p13n.delivery.snipfilter.impl;

import java.util.ArrayList;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * A deprecated filter that will make sure there is always one snippet in the list.
 * Please use {@link ListSizeFilter} instead of this filter.
 * @author adamgent
 *
 */
public class PickFirstItemsIfEmptyPostFilter implements IDeliverySnippetFilter {

    public List<IDeliveryResponseSnippetItem> filter(IDeliverySnippetFilterContext context, List<IDeliveryResponseSnippetItem> items) {
        if (items.isEmpty() && ! context.getResponseSnippetItems().isEmpty()) {
            List<IDeliveryResponseSnippetItem> snippets = new ArrayList<IDeliveryResponseSnippetItem>();
            snippets.add(context.getResponseSnippetItems().get(0));
            return snippets;
        }
        return items;
    }

}
