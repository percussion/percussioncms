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

package com.percussion.soln.p13n.delivery.data;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;

/**
 * 
 * A delivery list is a content item that contains
 * {@link DeliverySnippetItem snippets} that have not been filtered or processed.
 * <p>
 * These items are very much akin to CM System slots.
 * @author adamgent
 *
 */
public class DeliveryListItem extends DeliveryItem {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 6013965907342218731L;
    
    private List<DeliverySnippetItem> snippets;
    
    private List<String> snippetFilterIds;

    /**
     * The snippets associated with this list.
     * @return maybe <code>null</code>.
     */
    public List<DeliverySnippetItem> getSnippets() {
        return snippets;
    }

    @Override
    public long getId() {
        return getContentId();
    }

    @Override
    public void setId(long id) {
        setContentId((int)id);
    }

    /**
     * See Getter.
     * @param snippets recommended that this be never <code>null</code>.
     * @see #getSnippets()
     */
    public void setSnippets(List<DeliverySnippetItem> snippets) {
        this.snippets = snippets;
    }

    /**
     * The {@link IDeliverySnippetFilter snippet filters} that should process this list.
     * The filters are processed in the order of the list.
     * @return should not be <code>null</code>
     */
    public List<String> getSnippetFilterIds() {
        return snippetFilterIds;
    }

    /**
     * See Getter.
     * @param snippetFilterIds never <code>null</code>.
     * @see #getSnippetFilterIds()
     */
    public void setSnippetFilterIds(List<String> snippetFilterIds) {
        this.snippetFilterIds = snippetFilterIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("snippetFilterIds",snippetFilterIds)
        .append("snippets", snippets)
        .toString();
    }

}
