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
