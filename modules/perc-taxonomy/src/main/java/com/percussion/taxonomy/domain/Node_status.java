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
