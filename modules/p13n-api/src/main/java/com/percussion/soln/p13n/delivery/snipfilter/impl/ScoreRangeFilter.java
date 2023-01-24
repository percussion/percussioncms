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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.snipfilter.AbstractRemovalFilter;
import com.percussion.soln.p13n.delivery.snipfilter.AbstractScoringFilter;

/**
 * Filters snippets so that the list has only snippets with in a
 * certain score range. 
 * This filter should be run after a {@link AbstractScoringFilter scoring filter}.
 * <p>
 * The min and max score can be configured as java bean properties.
 * At some point in the future these properties might come from the
 * {@link IDeliverySnippetFilterContext#getResponseListItem() list item} but
 * right now they come from the Java bean properties (configured through spring).
 *   
 * @author adamgent
 *
 */
public class ScoreRangeFilter implements IDeliverySnippetFilter {
    
    /**
     * Default property name.
     */
    private static final String DEFAULT_MAX_SCORE_PROPERTY_NAME = "rx:soln_p13n_maxScore";
    /**
     * Default property name.
     */
    private static final String DEFAULT_MIN_SCORE_PROPERTY_NAME = "rx:soln_p13n_minScore";
    
    private String enablePropertyName = "rx:soln_p13n_enableScoreFilter";
    //TODO use this property.
    private String minScorePropertyName = DEFAULT_MIN_SCORE_PROPERTY_NAME;
    //TODO use this property.
    private String maxScorePropertyName = DEFAULT_MAX_SCORE_PROPERTY_NAME;
    private Double defaultMinScore = 1.0;
    private Double defaultMaxScore = Double.MAX_VALUE;

    public List<IDeliveryResponseSnippetItem> filter(
            IDeliverySnippetFilterContext context, 
            List<IDeliveryResponseSnippetItem> items) throws DeliverySnippetFilterException {
        IDeliveryResponseListItem listItem = context.getResponseListItem();
        if( ! isScoreFilterEnabled(listItem) ) return items;
        
        final Double min = getMinScore(listItem);
        final Double max = getMaxScore(listItem);
        final DoubleRange range = new DoubleRange(min,max);
        //Removes items whose score is not in the range.
        return new AbstractRemovalFilter() {
            @Override
            public boolean removeItem(
                    IDeliverySnippetFilterContext context, 
                    IDeliveryResponseSnippetItem item,
                    int index) {
                return  ! /* NOT */ range.containsDouble(item.getScore());
            }
        }.filter(context, items);
        
    }
    
    protected boolean isScoreFilterEnabled(IDeliveryResponseListItem listItem) {
        String propName = getEnablePropertyName();
        try {
            if (listItem.hasProperty(propName) && isNotBlank(listItem.getProperty(propName).getString())){
                return listItem.getProperty(propName).getBoolean();
            }
        } catch (RepositoryException e) {
            log.warn(getEnablePropertyName() + " does not exist or is messed up", e);
        }
        return false;
    }
    
    //TODO get this from the list item.
    protected Double getMinScore(IDeliveryResponseListItem listItem) {
        return getDefaultMinScore();
    }
    
    //TODO get this from the list item.
    protected Double getMaxScore(IDeliveryResponseListItem listItem) {
        return getDefaultMaxScore();
    }
    

    
    public String getEnablePropertyName() {
        return enablePropertyName;
    }

    
    public void setEnablePropertyName(String enablePropertyName) {
        this.enablePropertyName = enablePropertyName;
    }

    
    public String getMinScorePropertyName() {
        return minScorePropertyName;
    }

    
    public void setMinScorePropertyName(String minScorePropertyName) {
        this.minScorePropertyName = minScorePropertyName;
    }

    
    public String getMaxScorePropertyName() {
        return maxScorePropertyName;
    }

    
    public void setMaxScorePropertyName(String maxScorePropertyName) {
        this.maxScorePropertyName = maxScorePropertyName;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(ScoreRangeFilter.class);

    
    public Double getDefaultMinScore() {
        return defaultMinScore;
    }

    
    public void setDefaultMinScore(Double defaultMinScore) {
        this.defaultMinScore = defaultMinScore;
    }

    
    public Double getDefaultMaxScore() {
        return defaultMaxScore;
    }

    
    public void setDefaultMaxScore(Double defaultMaxScore) {
        this.defaultMaxScore = defaultMaxScore;
    }

}
