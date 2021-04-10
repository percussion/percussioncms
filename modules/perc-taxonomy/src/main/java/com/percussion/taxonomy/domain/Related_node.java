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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
