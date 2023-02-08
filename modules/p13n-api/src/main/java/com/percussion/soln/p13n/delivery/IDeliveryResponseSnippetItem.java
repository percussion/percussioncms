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

package com.percussion.soln.p13n.delivery;

import java.io.IOException;

/**
 * Represents an XML/HTML chunk.
 * @see IDeliveryResponseListItem
 * @author adamgent
 *
 */
public interface IDeliveryResponseSnippetItem extends IDeliverySegmentedItem {
    
    /**
     * The snippets rendering usually an HTML chunk.
     * @return should never be <code>null</code>.
     * @throws IOException
     */
    public String getRendering() throws IOException;
    
    /**
     * See setter.
     * @return maybe <code>null</code>.
     * @see #setStyle(String)
     */
    public String getStyle();

    /**
     * Set by styling/decorating filters.
     * @param style style usually CSS.
     */
    public void setStyle(String style);
    
    /**
     * See setter.
     * @return should be zero or greater but is not guaranteed to be.
     * @see #setScore(double)
     */
    public int getSortIndex();
    
    /**
     * Set by sorting filters to indicate what the order SHOULD be.
     * @param index sorting index.
     */
    public void setSortIndex(int index);
    
    /**
     * See setter.
     * @return a zero usually indicates no score.
     * @see #setScore(double)
     */
    public double getScore();
    
    /**
     * Set by scoring/ranking filters.
     * Scoring is very much similar to search engine relevance ranking
     * and in fact could be powered by a search engine.
     * 
     * @param score never <code>null</code>.
     * 
     */
    public void setScore(double score);
}
