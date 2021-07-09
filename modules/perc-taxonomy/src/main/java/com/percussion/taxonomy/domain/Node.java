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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Taxonomy Node class to represent information about its taxons.
 *  
 */
public class Node implements Comparable<Node> {

   ///////////////////////////////////////////////////////////////////////////////////

    private static final String UNKNOWN                     = "UNKNOWN";
    private static final String IRRELEVANT                  = "N/A";
    
    ///////////////////////////////////////////////////////////////////////////////////

    private int id                                          = -1;
    private int default_community_id                        = -1;
    
    ///////////////////////////////////////////////////////////////////////////////////

    private Taxonomy taxonomy                               = null;
    private Node_status node_status                         = null;
    private Node parent                                     = null;
    
    ///////////////////////////////////////////////////////////////////////////////////

    private boolean not_leaf                                = false;
    private boolean isNodeSelectable                        = true;
    private boolean in_use                                  = false;
    
    ///////////////////////////////////////////////////////////////////////////////////

    private String search                                   = StringUtils.EMPTY;
    private String created_by_id                            = StringUtils.EMPTY;
    private String modified_by_id                           = StringUtils.EMPTY;

    ///////////////////////////////////////////////////////////////////////////////////

    private Timestamp created_at                            = null;
    private Timestamp modified_at                           = null;
    
    ///////////////////////////////////////////////////////////////////////////////////
    
    private Collection<Value> values                        = null;
    private Collection<Related_node> relatedNodesForNodeId  = null;
    private Collection<Node_editor> nodeEditors             = null;

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true or false to determine whether node is in use or not
     * @return in_use - true or false to determine whether node is in use or not
     */
    public boolean getIn_use() {
        return in_use;
    }

    /**
     * Set true or false to identify whether node is in use or not
     * @param inUse - boolean true or false to identify whether node is in use or not
     */
    public void setIn_use(boolean inUse) {
        in_use = inUse;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns collection of Value objects
     * @return
     */
    public Collection<Value> getValues() {
        return (values == null ? null : new HashSet<Value>(values));
    }

    /**
     * Set Value objects
     * @param values - Values collection
     */
    public void setValues(Collection<Value> values) {
       this.values = values;
    }

    /**
     * Add value to values collection
     * @param value - Value object
     */
    public void addValue(Value value) {
       if (value != null) {
          value.setNode(this);
          values.add(value);
       }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns unique id of Node 
     * @return id - unique node id
     */
    public int getId() {
        return id;
    }

    /**
     * Set unique node id
     * @param id - int value to set unique node id 
     */
    public void setId(int id) {
        this.id = id;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns status of the  node
     * @return node_status 
     */
    public Node_status getNode_status() {
        return node_status;
    }

    /**
     * Set node status 
     * @param node_status
     */
    public void setNode_status(Node_status node_status) {
        this.node_status = node_status;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns Taxonomy object
     * @return taxonomy - object of Taxonomy
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

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns parent node object of this node
     * @return parent - parent node of this node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Set parent node of this node
     * @param parent - this node's parent
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    ///////////////////////////////////////////////////////////////////////////////////

   /**
    * Returns search String
    * @return search - String to be searched
    */
    public String getSearch() {
        return (this.search == null ? StringUtils.EMPTY : this.search);
    }

    /**
     * Set search String
     * @param search
     */
    public void setSearch(String search) {
        this.search = search;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns default community id with which this node belongs
     * @return default_community_id 
     */
    public int getDefault_community_id() {
        return default_community_id;
    }

    /**
     * Set default community_id of this node
     * @param default_community_id - default community id of a node
     */
    public void setDefault_community_id(int default_community_id) {
        this.default_community_id = default_community_id;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns boolean true or false to determine whether given node is a leaf or not
     * @return not_leaf - ture or false to identify a leaf node
     */
    public boolean getNot_leaf() {
        return not_leaf;
    }

    /**
     * Set true or false to identify whether a node is leaf or not
     * @param not_leaf
     */
    public void setNot_leaf(boolean not_leaf) {
        this.not_leaf = not_leaf;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Method returns flag indicating whether the node is selectable in the content editor
     * @return boolean
     */
    public boolean getIsNodeSelectable() {
       return this.isNodeSelectable;
    }
    
    /**
     * Method sets a flag indicating whether the node is selectable in content editor
     * @param isNodeSelectable
     */
    public void setIsNodeSelectable(boolean isNodeSelectable) {
       this.isNodeSelectable = isNodeSelectable;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns node created by id
     * @return created_by_id - 
     */
    public String getCreated_by_id() {
        return (this.created_by_id == null ? null : this.created_by_id);
    }

    /**
     * Set node created by id
     * @param created_by_id
     */
    public void setCreated_by_id(String created_by_id) {
        this.created_by_id = created_by_id;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns created date and time
     * @return created_at - date and time of created
     */
    public Timestamp getCreated_at() {
        return created_at;
    }

    /**
     * Set created date and time
     * @param created_at - Created date and time
     */
    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns node modified by id 
     * @return modified_by_id - node modified by id
     */
    public String getModified_by_id() {
        return (this.modified_by_id == null ? null : this.modified_by_id);
    }

    /**
     * Set node modified by id
     * @param modified_by_id - node modified by id
     */
    public void setModified_by_id(String modified_by_id) {
        this.modified_by_id = modified_by_id;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns node modified date and time
     * @return modified_at - node modified at date and time
     */
    public Timestamp getModified_at() {
        return modified_at;
    }

    /**
     * Set node modified at date and time
     * @param modified_at - Node modified date and time
     */
    public void setModified_at(Timestamp modified_at) {
        this.modified_at = modified_at;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns collection of related nodes of a node
     * @return relatedNodesForNodeId - collection of related nodes
     */
    public Collection<Related_node> getRelatedNodesForNodeId() {
        return (this.relatedNodesForNodeId == null ? null : 
           new HashSet<Related_node>(relatedNodesForNodeId));
    }

    /**
     * Set related nodes collection of a node
     * @param relatedNodesForNodeId
     */
    public void setRelatedNodesForNodeId(Collection<Related_node> relatedNodesForNodeId) {
       this.relatedNodesForNodeId = relatedNodesForNodeId;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Return collection of node editors
     * @return nodeEditors - editors of the node
     */
    public Collection<Node_editor> getNodeEditors() {
        return (this.nodeEditors == null ? null : 
           new HashSet<Node_editor>(this.nodeEditors));
    }

    /**
     * Set collectin of node editors
     * @param nodeEditors
     */
    public void setNodeEditors(Collection<Node_editor> nodeEditors) {
       this.nodeEditors = nodeEditors;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Compare node to sort them by node id
     */
    public int compareTo(Node node) {
        if (node.getId() > id) {
            return -1;
        } else if (node.getId() < id) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Returns a string representation of the object.
     * 
     * YOU CANT REFERENCE THE OTHER LAZY-LOADED OBJECT ---- THE DOMAIN MODEL IS COMPLETELY JACKED
     * AND YOU WILL GET HIBERNATE EXCEPTIONS IF YOU TRY TO PULL THE TAXONOMY AND OTHER DOMAIN OBJECTS HERE
     */
    public String toString() {
        return "ID: " + getId()
                + "\nSearch: " + (this.getSearch() != null ? this.getSearch() : IRRELEVANT)
                + "\nParent: " + (parent != null ? parent.getId() : IRRELEVANT)
                + "\nDefault Community: " + this.getDefault_community_id()
                + "\nNot Leaf: " + this.getNot_leaf()
                + "\nCreated By: " + this.getCreated_by_id()
                + "\nCreated At: " + (this.getCreated_at() != null ? this.getCreated_at().toString() : IRRELEVANT)
                + "\nModified By: " + this.getModified_by_id()
                + "\nModified At: " + (this.getModified_at() != null ? this.getModified_at().toString() : IRRELEVANT);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////

}
