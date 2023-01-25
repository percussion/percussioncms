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
 * Taxonomy Related_node class to represent information about how taxons are related to one another in a given taxonomy.  
 */
public class Related_node {

    private int id;
    private Node node;
    private Node related_node;
    private Relationship_type relationship;

    /**
     * Return unique id of related node
     * @return id - unique int related node id
     */
    public int getId() {
        return id;
    }

    /**
     * Set unique id of related node
     * @param id - unique int value 
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns node object
     * @return node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Set node object
     * @param node
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * Returns related node object
     * @return node
     */
    public Node getRelated_node() {
        return related_node;
    }

    /**
     * Set related node object
     * @param node
     */
    public void setRelated_node(Node related_node) {
        this.related_node = related_node;
    }

    /**
     * Returns relation type of related node
     * @return relationship
     */
    public Relationship_type getRelationship() {
        return relationship;
    }

    /**
     * Set relationship type of related node
     * @param relationship
     */
    public void setRelationship(Relationship_type relationship) {
        this.relationship = relationship;
    }
}
