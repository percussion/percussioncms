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
