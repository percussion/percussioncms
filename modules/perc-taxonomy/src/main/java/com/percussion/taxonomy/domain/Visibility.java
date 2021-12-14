/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.taxonomy.domain;

/**
 * Taxonomy Visibility class to represent community visibility information of a taxonomy.
 *  
 */
public class Visibility {

    private int id;
    private Taxonomy taxonomy;
    private long community_id;
    
    /**
     * Returns unique id of Visibility
     * @return id - unique int value of visibility id
     */
    public int getId() {
        return id;
    }
    
    /**
     *Set unique id of Visibility 
     * @param id - int value of Visibility
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Returns Taxonomy object
     * @return taxonomy - a Taxonomy object
     */
    public Taxonomy getTaxonomy() {
        return taxonomy;
    }
    
    /**
     * Set Taxonomy object
     * @param taxonomy
     */
    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }
    
    /**
     * Returns visible  community id
     * @return community_id - visibility community id
     */
    public long getCommunity_id() {
        return community_id;
    }
    /**
     * Set visible community id
     * @param communityId
     */
    public void setCommunity_id(long communityId) {
        community_id = communityId;
    }
    
}
