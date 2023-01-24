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
 * Taxonomy Node_status class which represents its taxon's status such as active or disabled. 
 *  
 */
public class Node_status {

    /**
     * Constant to set active status of the node
     */
    public static int ACTIVE=1;
    /**
     * Constant to set disabled status of the node
     */
    public static int DISABLED=2;
    
    private int id;
    private String name;

    /**
     * Returns unique id of the node_status
     * @return id - unique int value as node id
     */
    public int getId() {
        return id;
    }

    /**
     * Set unique node_status id 
     * @param id - unique int node_status id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Return name of node_status 
     * @return name - String name of node_status
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of node_status
     * @param name - String name of node_status
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
       return this.getName() + " is " + (this.getId() == DISABLED ? "DISABLED" : "ACTIVE");
    }
}
