package com.percussion.soln.p13n.delivery;

import java.util.List;

/**
 * Represents a delivery list that has been or is going to be filtered on.
 * One might expect to get the snippets items from this list but 
 * because the items in the list change as the {@link IDeliverySnippetFilter}s
 * are running this is not the case.
 * To get the current list of snippets in the list see {@link IDeliverySnippetFilterContext}.
 * 
 * @see IDeliverySnippetFilterContext
 * @see IDeliveryResponseItem
 * @author adamgent
 *
 */
public interface IDeliveryResponseListItem extends IDeliverySegmentedItem {
    
 
    /**
     * Snippet filter ids.
     * @return never <code>null</code>.
     */
    List<String> getSnippetFilterIds();
}
