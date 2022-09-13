package com.percussion.soln.p13n.delivery.snipfilter;

import java.util.Iterator;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliverySegmentedItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * Adapter that requires implementations to calculate the score of a {@link IDeliveryResponseSnippetItem snippet item} to be used by 
 * other filters in the pipeline. 
 * The score can retrieved from other filters later on through:
 * {@link IDeliveryResponseSnippetItem#getScore()}
 * @author adamgent
 *
 */
public abstract class AbstractScoringFilter implements IDeliverySnippetFilter {

    
    public List<IDeliveryResponseSnippetItem> filter(IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) {
        Iterator<IDeliveryResponseSnippetItem> it = items.iterator();
        int index = 0;
        while (it.hasNext()) {
            IDeliveryResponseSnippetItem item = it.next();
            double score = item.getScore() + calculateScore(context, item, index);
            item.setScore(score);
            ++index;
        }
        return items;
    }


    /**
     * Calculates the score of a snippet usually based on segmentation.
     * @param context never <code>null</code>.
     * @param item never <code>null</code>.
     * @param index <code>0</code> or greater
     * @return usually a number <code>&gt; 0</code>. Zero will indicate no scoring. 
     */
    public abstract double calculateScore(IDeliverySnippetFilterContext context, IDeliverySegmentedItem item, int index);

}
