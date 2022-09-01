package com.percussion.soln.p13n.delivery.snipfilter;

import java.util.Iterator;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * Adapter that requires implementations to {@link #calculateStyle(IDeliverySnippetFilterContext, IDeliveryResponseSnippetItem, int)}.
 * The style is in many cases a CSS selector that is applied on the browser side.
 * @author adamgent
 *
 */
public abstract class AbstractStylingFilter implements IDeliverySnippetFilter {

    public List<IDeliveryResponseSnippetItem> filter(
            IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) {
        Iterator<IDeliveryResponseSnippetItem> it = items.iterator();
        int i = 0;
        while(it.hasNext()) {
            IDeliveryResponseSnippetItem item = it.next();
            item.setStyle(calculateStyle(context, item, i));
        }
        return items;
    }
    
    public abstract String calculateStyle(
            IDeliverySnippetFilterContext context, 
            IDeliveryResponseSnippetItem item, 
            int index);

}
