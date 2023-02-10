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

package com.percussion.soln.p13n.delivery.snipfilter.impl;

import static com.percussion.soln.p13n.delivery.snipfilter.DeliverySnippetFilterUtil.*;
import static org.apache.commons.lang.StringUtils.*;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;

/**
 * Filters snippets so that the size of the list is between
 * a minimum and maximum size inclusive.
 * <p>
 * This filter will add back snippets from the original pre-processed list if
 * the current list of snippets is below the minimum count.
 * <p>
 * The filter will simply truncate the list if the list is above the maximum.
 * <p>
 * The property names of the min and max property can be configured as 
 * java bean properties.
 *   
 * @author adamgent
 *
 */
public class ListSizeFilter implements IDeliverySnippetFilter {

    /**
     * The default property name for the max count of the list.
     */
    protected static final String DEFAULT_FILTER_MAX_PROPERTY_NAME = "rx:soln_p13n_filterMax";
    /**
     * The default property name for the min count of the list.
     */
    protected static final String DEFAULT_FILTER_MIN_PROPERTY_NAME = "rx:soln_p13n_filterMin";
    
    private String minCountPropertyName = DEFAULT_FILTER_MIN_PROPERTY_NAME;
    private String maxCountPropertyName = DEFAULT_FILTER_MAX_PROPERTY_NAME;

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(ListSizeFilter.class);
    
    public List<IDeliveryResponseSnippetItem> filter(IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) throws DeliverySnippetFilterException {
        
        int min = getMinCount(context.getResponseListItem());
        int max = getMaxCount(context.getResponseListItem());
        
        List<IDeliveryResponseSnippetItem> originals = context.getResponseSnippetItems();
        /*
         * Lets re-sort the original snippet list based on their current sort index.
         * Sort snippets clones the list.
         */
        originals = sortSnippets(originals);
        int originalSize = originals.size();
        int currentSize = items.size();
        
        //Normalize min and max in case max is above min.
        if (min > max) {
            max = min;
        }
        //Optimal case we do nothing
        if ( (min < 0 && max < 0)  || (min <= currentSize && currentSize <= max) ) 
            return items;
        //Do we need to add items?
        else if (currentSize < min ) {
            //We don't have any items to add.
            if (originalSize <= currentSize) return items;
            //Get items from the original
            List<IDeliveryResponseSnippetItem> filler = new ArrayList<IDeliveryResponseSnippetItem>();
            //Find snippets in the original list that are not in the current.
            int i = currentSize;
            for(IDeliveryResponseSnippetItem snippet : originals) {
                if (i >= min ) break;
                if ( ! containsSnippet(snippet, items)) {
                    filler.add(snippet);
                    ++i;
                }
            }
            if(log.isDebugEnabled())
                log.debug("Adding snippets " + filler);
            items.addAll(filler);
            return items;
        }
        //Do we need to remove items?
        else if (currentSize > max) {
            return new ArrayList<IDeliveryResponseSnippetItem>(items).subList(0, max);
        }
        
        throw new DeliverySnippetFilterException("Programming error", true);
    }
    
   

    /**
     * Gets the min count property from the list item.
     * @param listItem never <code>null</code>.
     * @return -1 if not set.
     */
    protected int getMinCount(IDeliveryResponseListItem listItem) {
        return getNumber(listItem, getMinCountPropertyName());
    }
    
    /**
     * Gets the max count property from the list item.
     * @param listItem never <code>null</code>.
     * @return -1 if not set.
     */
    protected int getMaxCount(IDeliveryResponseListItem listItem) {
        return getNumber(listItem, getMaxCountPropertyName());
    }
    
    private int getNumber(IDeliveryResponseListItem listItem, String prop) throws DeliverySnippetFilterException {
        Number i = -1;
        try {
            if (listItem.hasProperty(prop) && isNotBlank(listItem.getProperty(prop).getString())) {
                i = listItem.getProperty(prop).getLong();
            }
        } catch (RepositoryException e) {
            throw new DeliverySnippetFilterException("Error getting property: " + prop, e);
        }
        return i.intValue();
    }

    
    /**
     * Default is {@value #DEFAULT_FILTER_MIN_PROPERTY_NAME}.
     * @return never <code>null</code>.
     */
    public String getMinCountPropertyName() {
        return minCountPropertyName;
    }

    
    public void setMinCountPropertyName(String filterMinPropertyName) {
        this.minCountPropertyName = filterMinPropertyName;
    }

    /**
     * Default is {@value #DEFAULT_FILTER_MAX_PROPERTY_NAME}
     * @return never <code>null</code>.
     */
    public String getMaxCountPropertyName() {
        return maxCountPropertyName;
    }

    
    public void setMaxCountPropertyName(String filterMaxPropertyName) {
        this.maxCountPropertyName = filterMaxPropertyName;
    }



    

}
