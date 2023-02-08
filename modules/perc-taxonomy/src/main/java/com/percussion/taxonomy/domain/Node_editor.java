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
 * Taxonomy Node_editor class to represent information of taxon editors.
 *  
 */
public class Node_editor {

    private int id;
    private Node node;
    private String role;

    /**
     * Returns unique id of node editor
     * @return id - unique int node editor id
     */
    public int getId() {
        return id;
    }

    /**
     * Set id of node ediot
     * @param id - int unique id of node editor
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
    * Returns node object
    * @return node - Node object
    */
    public Node getNode() {
        return node;
    }

    /**
     * Set node 
     * @param node 
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * Returns role of node editor
     * @return - String role of node editor
     */
    public String getRole() {
        return role;
    }

    /**
     * Set node editor's role
     * @param role - String role of node editor
     */
    public void setRole(String role) {
      this.role = role;
   }
}
