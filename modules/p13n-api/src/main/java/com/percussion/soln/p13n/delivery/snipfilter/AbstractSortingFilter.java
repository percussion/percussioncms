package com.percussion.soln.p13n.delivery.snipfilter;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * 
 * Adapter that requires implementations to {@link #compareItems(IDeliverySnippetFilterContext, IDeliveryResponseSnippetItem, IDeliveryResponseSnippetItem)}.
 * @author adamgent
 *
 */
public abstract class AbstractSortingFilter implements IDeliverySnippetFilter {

    public static final String SNIP_ITEM_ORDER_PROP_NAME = "rx:perc_list_order";

    public enum Order { ASC, DESC };
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(AbstractSortingFilter.class);
    
    public List<IDeliveryResponseSnippetItem> filter(
            final IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) {

        final Order order = getOrder(context.getResponseListItem());
        
        List<IDeliveryResponseSnippetItem> sortedItems = new ArrayList<IDeliveryResponseSnippetItem>(items);
        Comparator<IDeliveryResponseSnippetItem> comp = new Comparator<IDeliveryResponseSnippetItem>() {
            public int compare(IDeliveryResponseSnippetItem lh, IDeliveryResponseSnippetItem rh) {
                int c = Order.DESC == order ? compareItems(context, rh, lh) : compareItems(context, lh, rh);
                if (c != 0) {
                    return c;
                }
                /*
                 * If the items are still equal we will use their original sort index
                 * and disregard descending or ascending. 
                 */
                Integer lhsi = lh.getSortIndex();
                Integer rhsi = rh.getSortIndex();
                
                return lhsi.compareTo(rhsi);
                
            }
        };
        
        sort(sortedItems, comp);
        
        int i = 1;
        for(IDeliveryResponseSnippetItem s : sortedItems) {
            s.setSortIndex(i);
            ++i;
        }
        return sortedItems;
    }
    
    protected Order getOrder(IDeliveryResponseListItem ruleItem) {
        Order order;
        try {
            if (ruleItem.hasProperty(SNIP_ITEM_ORDER_PROP_NAME)) {
                String orderStr = ruleItem.getProperty(SNIP_ITEM_ORDER_PROP_NAME).getString();
                if ( ! (orderStr.equalsIgnoreCase(Order.DESC.toString()) 
                        || orderStr.equalsIgnoreCase(Order.ASC.toString())) ) {
                    final String orderErrorMessage = SNIP_ITEM_ORDER_PROP_NAME 
                        + " should be either "  
                        + Order.ASC.toString() + " or " 
                        + Order.DESC.toString();
                    log.error(orderErrorMessage);
                    throw new IllegalArgumentException(orderErrorMessage);
                }
                order = Order.valueOf(orderStr.toUpperCase());
            }
            else {
                order = Order.DESC;
            }
        } catch (RepositoryException e) {
            log.error("Problem getting field " + SNIP_ITEM_ORDER_PROP_NAME);
            order = Order.DESC;
        }
        
        return order;
        
    }

    /**
     * Analogous to {@link Comparator}.
     * Implementing classes should <strong>NOT</strong> set the sort index:
     * {@link IDeliveryResponseSnippetItem#setSortIndex(int)} as that will be
     * handled elsewhere.
     * @param context
     * @param itemLeft
     * @param itemRight
     * @return see {@link Comparator#compare(Object, Object)}
     */
    public abstract int compareItems(
            IDeliverySnippetFilterContext context, 
            IDeliveryResponseSnippetItem itemLeft, 
            IDeliveryResponseSnippetItem itemRight);

}
