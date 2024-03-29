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

package com.percussion.taxonomy.web;

import com.percussion.error.PSExceptionUtils;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.service.AttributeService;
import com.percussion.taxonomy.service.LanguageService;
import com.percussion.taxonomy.service.NodeService;
import com.percussion.taxonomy.service.Node_editorService;
import com.percussion.taxonomy.service.Node_statusService;
import com.percussion.taxonomy.service.Related_nodeService;
import com.percussion.taxonomy.service.Relationship_typeService;
import com.percussion.taxonomy.service.TaxonomyService;
import com.percussion.taxonomy.service.ValueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractTaxonEditorController extends AbstractControllerWithSecurityChecks {

    private static final Logger log = LogManager.getLogger(AbstractTaxonEditorController.class);

    protected final boolean DEBUG = false;
    protected final Logger logger = LogManager.getLogger(getClass());
    protected TaxonomyService taxonomyService;
    protected LanguageService languageService;
    protected NodeService nodeService;
    protected ValueService valueService;
    protected Node_editorService node_editorService;
    protected Node_statusService node_statusService;
    protected AttributeService attributeService;
    protected Related_nodeService related_nodeService;
    protected Relationship_typeService relationship_typeService;

    public static final String ACTION_ERROR="action_error";
    
    protected static HashMap collection_to_hashmap(Collection c){
    	HashMap ret = new HashMap();
    	for (Object o : c){
    		ret.put(o, o);
    	}
    	return ret;
    }
        
    protected Collection<String> getRoleNamesSelected(Node node) throws Exception {
    	Collection<String> ret = new ArrayList<String>();
    	if (node==null){
    		throw new Exception("bad name");
    	}
    	for (Node_editor node_editor : node.getNodeEditors()){
    		ret.add(node_editor.getRole());
    	}
    	return ret;
    }

    protected HashMap<Integer, Integer> getSimilarOrRelatedNodeIds(int nodeId, int relationship_type_id) {
        HashMap<Integer, Integer> ids = new HashMap<Integer, Integer>();
        Collection<Related_node> related_nodes = null;
        if (relationship_type_id == 1) {
            related_nodes = nodeService.getRelatedNodes(nodeId);
        } else {
            related_nodes = nodeService.getSimilarNodes(nodeId);
        }
        for (Related_node related_node : related_nodes) {
            ids.put(new Integer(related_node.getRelated_node().getId()), new Integer(related_node.getRelated_node().getId()));
        }

        return ids;
    }

    //Save Vals ---------------------------------------------------------
    public Map<String, String> saveNodeValues(HttpServletRequest request, 
                                              Collection<Attribute> attributes, 
                                              Node node, 
                                              int langID) throws Exception {
        return valueService.saveValuesFromParams(request.getParameterMap(), attributes, node, langID, getUserName(request));
    }

    protected HashMap<Integer, Boolean[]> getAttributeProps(int taxonomy_id, int language_id) {
        HashMap<Integer, Boolean[]> attributeProps = new HashMap<Integer, Boolean[]>();
        Collection<Attribute> all = attributeService.getAllAttributes(taxonomy_id, language_id);
        for (Attribute attribute : all) {
            Boolean[] props = new Boolean[2];
            props[0] = attribute.getIs_required();
            props[1] = attribute.getIs_multiple();
            attributeProps.put(attribute.getId(), props);
        }
        return attributeProps;
    }

    protected ArrayList<Pair> getAttributeNames(int taxonomy_id, int language_id) {
        ArrayList<Pair> attributeNames = new ArrayList<Pair>();
        Collection all = attributeService.getAttributeNames(taxonomy_id, language_id);
        Iterator pairs = all.iterator();
        while (pairs.hasNext()) {
            Object[] pair = (Object[]) pairs.next();
            attributeNames.add(new Pair((String) pair[0], (Integer) pair[1]));
        }
        Collections.sort(attributeNames);
        return attributeNames;
    }

    protected HashMap<String, Integer> getNodeNames(int taxID, int langID) {
        // TODO need to change this from a hard coding to an ajax based one
        HashMap<String, Integer> nodeNames = new HashMap<String, Integer>();
        Collection all = nodeService.getAllNodeNames(taxID, langID);
        Iterator pairs = all.iterator();
        while (pairs.hasNext()) {
            Object[] pair = (Object[]) pairs.next();
            nodeNames.put((String) pair[2], (Integer) pair[0]);
        }
        return nodeNames;
    }

    protected HashMap<String, Integer> getParentNodeName(int nodeId, int langID) {
        Node node = nodeService.getNode(nodeId,1); //TO DO: Pass lang_id
        Node parent = node.getParent();
        HashMap<String, Integer> parentNodeName = new HashMap<String, Integer>();
        if (parent != null) {
            Collection<String> names = nodeService.getNodeName(parent.getId(), langID);
            String full_name = "";
            for (String name : names) {
                full_name += name;
            }
            parentNodeName.put(full_name, node.getId());
        }
        return parentNodeName;
    }

    protected HashMap<Integer, Collection<String>> getNodeValues(int nodeID, int langID) {
        HashMap<Integer, Collection<String>> nodeValues = new HashMap<Integer, Collection<String>>();
        Collection<Value> values = nodeService.getValuesForNode(nodeID, langID);
        for (Value value : values) {
        	Integer key = value.getAttribute().getId();
        	Collection<String> val = new ArrayList<String>();
        	if (nodeValues.containsKey(key)){
            	val.addAll(nodeValues.get(key));
            }
        	val.add(value.getName());
        	nodeValues.put(key, val);
        }
        return nodeValues;
    }

    protected String decode(String in) {
        try {
            return URLDecoder.decode(in, "UTF-8");
        } catch (Exception e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            return null;
        }
    }

    protected void set_new_permissions_for_role(String[] roleNames, Node this_node2, boolean do_children){
    	
    	Node this_node = nodeService.getNode(this_node2.getId(), 1);  // language doesn't matter so we pass 1 for english
    	
    	node_editorService.removeNode_editors(this_node.getNodeEditors());
    	
        if (roleNames != null) {
            if (this_node.getNodeEditors() != null) {
                for (String role_name : roleNames) {
                    Node_editor obj = new Node_editor();
                    obj.setNode(this_node);
                    obj.setRole(role_name);
                    node_editorService.saveNode_editor(obj);
                }
            }

        }
        
        if (do_children){
        	for (Node child_node : (Collection<Node>) nodeService.getChildNodes(this_node.getId())){
        		set_new_permissions_for_role(roleNames, child_node, do_children);
        	}
        }
    	
    }
    
    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

    public void setAttributeService(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    public void setValueService(ValueService valueService) {
        this.valueService = valueService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNode_statusService(Node_statusService node_statusService) {
        this.node_statusService = node_statusService;
    }

    public void setRelationship_typeService(Relationship_typeService relationship_typeService) {
        this.relationship_typeService = relationship_typeService;
    }

    public void setRelated_nodeService(Related_nodeService related_nodeService) {
        this.related_nodeService = related_nodeService;
    }

    public void setNode_editorService(Node_editorService node_editorService) {
        this.node_editorService = node_editorService;
    }

    // Class used for sorting nodes and returning to JSTL front end
    // -------------------------------------------
    private class Pair implements Comparable<Pair>, Map.Entry<String, Integer> {

        private String name;
        private int id;

        public Pair(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public boolean equals(Object o) {
            return true;
        }

        public int hashCode() {
            return 1;
        }

        public String getKey() {
            return name;
        }

        public Integer getValue() {
            return id;
        }

        public Integer setValue(Integer value) {
            this.id = value;
            return id;
        }

        public int compareTo(Pair a) {
            if (id > a.id) {
                return 1;
            } else if (id < a.id) {
                return -1;
            }
            return 0;
        }
    }
}
