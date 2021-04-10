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

package com.percussion.taxonomy.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.taxonomy.TaxonomySecurityHelper;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.domain.Relationship_type;
import com.percussion.taxonomy.domain.Taxonomy;

import java.util.Collections;

@Controller
public class TaxonEditorController extends AbstractTaxonEditorController {

   
   
    // SECURITY: not needed
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Collection taxonomys = taxonomyService.getAllTaxonomys();
        Collection languages = languageService.getAllLanguages();

        TaxonParams tp = new TaxonParams(request, 1, Language.DEFAUL_LANG,taxonomyService); // TODO hard coded values

        Map<String, Object> myModel = new HashMap<String, Object>();
        myModel.put("taxID", tp.getTaxID());
        myModel.put("langID", tp.getLangID());
        myModel.put("taxonomys", taxonomys);
        myModel.put("languages", languages);
        myModel.put("specificNode", false);
        myModel.put("js_timestamp", String.valueOf(System.currentTimeMillis()));
        return new ModelAndView("taxoneditor", "model", myModel);
    }

    // SECURITY: added
    public ModelAndView archiveNode(HttpServletRequest request, HttpServletResponse response) throws Exception {

        TaxonParams tp = new TaxonParams(request,taxonomyService);

        boolean archive = (request.getParameter("archive_node_flag") != null);

        Node node = nodeService.getNode(tp.getNodeID(), Language.DEFAUL_LANG);

        verifyNodeIsEditable(node);

        //(This will have to change if more statuses are ever added)
        if (archive && node.getNode_status().getId() != Node_status.DISABLED) {
            node.setModified_by_id(getUserName(request));
            node.setModified_at(new Timestamp(System.currentTimeMillis()));
        	node.setNode_status(node_statusService.getNode_status(Node_status.DISABLED));
            nodeService.saveNode(node);
        } else if (node.getNode_status().getId() != Node_status.ACTIVE) {
            node.setModified_by_id(getUserName(request));
            node.setModified_at(new Timestamp(System.currentTimeMillis()));
        	node.setNode_status(node_statusService.getNode_status(Node_status.ACTIVE));
            nodeService.saveNode(node);
        }
        return changeNode(request, response);
    }

    // SECURITY: added
    public ModelAndView deleteNode(HttpServletRequest request, HttpServletResponse response) throws Exception {

        TaxonParams tp = new TaxonParams(request,taxonomyService);
        Node node = nodeService.getNode(tp.getNodeID(), Language.DEFAUL_LANG);
        Node node_parent;
        verifyNodeIsEditable(node);
        Map<String, String> errors = null;
        
        if (node.getIn_use() && nodeService.checkDbInUse(node)){
            logger.info("Deleting in use Taxon, removing references in content "+node.getId());
            nodeService.deleteNodeFromContent(node);
        } 

     	errors = nodeService.deleteNodeAndFriends(tp.getNodeID(), Language.DEFAUL_LANG);
     	if(node.getParent()!= null) {
     	   node_parent = nodeService.getNode(node.getParent().getId(), Language.DEFAUL_LANG);
            Collection childNodes = nodeService.getChildNodes(node_parent.getId());
            if(childNodes.size() == 0 && node_parent.getNot_leaf()) {
               node_parent.setNot_leaf(false);
               nodeService.saveNode(node_parent);
            }
     	}
       

        if (errors != null) {
            return changeNodeWithError(request, response, errors);
        }
        return index(request, response);
    }

    // SECURITY: not needed
    // Return same view with special params after a node change
    public ModelAndView changeNode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return changeNodeGeneric(request, response, null);
    }

    // SECURITY: not needed
    private ModelAndView changeNodeWithError(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors) throws Exception {
        //System.out.println("Change Node with Errors ----------------------------------------------------");

        for (String key : errors.keySet()) {
            //System.out.println("Error: " + key + ":" + errors.get(key));
        }

        return changeNodeGeneric(request, response, errors);
    }

    // SECURITY: not needed
    private ModelAndView changeNodeGeneric(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors) throws Exception {
        HashMap<String, Object> myModel = new HashMap<String, Object>();
        Collection taxonomys = taxonomyService.getAllTaxonomys();
        Collection languages = languageService.getAllLanguages();

        TaxonParams tp = new TaxonParams(request,taxonomyService);

        // invalid call... not sure we need this
        if (!tp.hasNodeID()) {
            //System.out.println("Node Missing");
            return index(request, response);
        }

        // node switching... not sure we need this
        Node node = nodeService.getNode(tp.getNodeID(), tp.getLangID());
        if(node.getTaxonomy() == null){
        	node = nodeService.getNode(tp.getNodeID(), Language.DEFAUL_LANG);;
        }
        
        if (node.getTaxonomy().getId() != tp.getTaxID()) {
            //System.out.println("Taxon doesn't match:" + node.getTaxonomy().getId() + " vs. " + tp.getTaxID());
            return index(request, response);
        }

        HashMap<Integer, Collection<String>> nodeValues = getNodeValues(tp.getNodeID(), tp.getLangID());
        HashMap<String, Integer> nodeNames = getNodeNames(tp.getTaxID(), tp.getLangID());

        int which_accordian_to_open = 0;
        if (errors != null) {
        	
        	if (errors.containsKey(ACTION_ERROR)){
        		myModel.put("action_error", errors.get(ACTION_ERROR));
        	}
        	
            myModel.put("errors", errors);
            for (String e : errors.keySet()) {
                which_accordian_to_open = (which_accordian_to_open == 1 || e.startsWith("child")) ? 1 : which_accordian_to_open;
            }
        }
        myModel.put("which_accordian_to_open", which_accordian_to_open);

        myModel.put("isNodeSelectable", node.getIsNodeSelectable());
        myModel.put("not_leaf", node.getNot_leaf());
        myModel.put("status", node.getNode_status().getId());
        myModel.put("taxonomys", taxonomys);
        myModel.put("languages", languages);
        myModel.put("specificNode", true);
        myModel.put("am_i_admin", TaxonomySecurityHelper.amITaxonomyAdmin());
        myModel.put("show_related_taxons", node.getTaxonomy().getHas_related_ui());
        myModel.put("valuesForNode", nodeValues);
        myModel.put("nodes", nodeNames);
        myModel.put("nodeID", node.getId());
        myModel.put("taxID", tp.getTaxID());
        myModel.put("langID", tp.getLangID());
        if(tp.getParentID() != null){
            myModel.put("parentNode", getParentNodeName(tp.getNodeID(), tp.getLangID())); // TODO what the heck is this?
        }
        myModel.put("attributeNames", getAttributeNames(tp.getTaxID(), tp.getLangID()));
        myModel.put("attributeProps", getAttributeProps(tp.getTaxID(), tp.getLangID()));
        myModel.put("editor_RoleNames", TaxonomySecurityHelper.getAllRoles());
        myModel.put("editor_RoleNamesSelected", collection_to_hashmap(getRoleNamesSelected(node)));
        myModel.put("permissionsError", !canEditNode(node));
        myModel.put("selectedRelatedNodeIds", StringUtils.join(getSimilarOrRelatedNodeIds(tp.getNodeID(), Relationship_type.RELATED).keySet().toArray(), ","));
        myModel.put("selectedSimilarNodeIds", StringUtils.join(getSimilarOrRelatedNodeIds(tp.getNodeID(), Relationship_type.SIMILAR).keySet().toArray(), ","));
        myModel.put("js_timestamp", String.valueOf(System.currentTimeMillis()));

        
        // referenced by
        
        Collection<Related_node> referenced_by_relationships = nodeService.getRelatedNodeReferences(tp.getNodeID());
        Collection<Integer> referenced_by_ids = new ArrayList<Integer>();
        for (Related_node rn : referenced_by_relationships){
        	referenced_by_ids.add(rn.getNode().getId());
        }
        
        if (referenced_by_ids.size()>0){
        	Collection<String> referenced_by_node_names = new ArrayList<String>();
        	for (Object[] obj : (Collection<Object[]>)nodeService.getSomeNodeNames(referenced_by_ids, Language.DEFAUL_LANG)){
        		referenced_by_node_names.add(obj[2].toString());
        	}
        	myModel.put("referenced_by_node_names", StringUtils.join(referenced_by_node_names, ",") );
        }
        return new ModelAndView("taxoneditor", "model", myModel);
    }

    // SECURITY: added
    @SuppressWarnings("unchecked")
   public ModelAndView saveChild(HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaxonParams tp = new TaxonParams(request,taxonomyService);

        Node parent_node = null;
        
        Map<String, String> parentErrors;
        Map<String, String> errors;

        boolean move_to_top = request.getParameter("move_to_top")!=null;
        
        if (tp.hasParentID() && !move_to_top) {
            parent_node = nodeService.getNode(tp.getParentID(), tp.getLangID());
            verifyNodeIsEditable(parent_node);
        } else {
            TaxonomySecurityHelper.raise_error_if_cannot_admin();
        }
        
        Node node = new Node();
        Taxonomy taxonomy = this.taxonomyService.getTaxonomy(tp.getTaxID());
        Collection<Attribute> attributes = this.attributeService.getAllAttributes(tp.getTaxID(), tp.getLangID());
        
        node.setTaxonomy(taxonomy);
        node.setCreated_by_id(getUserName(request));
        node.setCreated_at(new Timestamp(System.currentTimeMillis()));
        node.setModified_by_id(getUserName(request));
        node.setModified_at(new Timestamp(System.currentTimeMillis()));
        node.setNode_status(node_statusService.getNode_status(Node_status.ACTIVE));
        
        if (!move_to_top){
        	node.setParent(parent_node);
        	node.setNot_leaf(false);
        }
        
        
        if(parent_node == null || (parent_node != null && parent_node.getNot_leaf())) {
          
           // We do not need to update or change the parent node....
           // JUST UPDATE THE NEW CHILD NODE with the PARAM VALUES
           errors = saveNodeValues(request, attributes, node, tp.getLangID());
        }
        else {
           // Our parent node is giving birth....
           parent_node.setNot_leaf(true);
           
           // Update the parent leaf state
           this.nodeService.saveNode(parent_node);
           
           // Save the child
           errors = saveNodeValues(request, attributes, node, tp.getLangID());
        }
     
        // from parent
        if (parent_node != null && errors == null) {
            // TODO we should have been able to do this (before the node save)!
            // node.setNodeEditors(parent_node.getNodeEditors());
            // instead we do this (after node save)....
            Collection<Node_editor> node_editors = nodeService.getNodeEditors(parent_node.getId());
            for (Node_editor nodeEditor : node_editors) {
                Node_editor obj = new Node_editor();
                obj.setNode(node);
                obj.setRole(nodeEditor.getRole());
                node_editorService.saveNode_editor(obj);
            }
        }

        if (errors == null) {
            return new ModelAndView("redirect:taxoneditor.htm?action=changeNode&nodeID=" + node.getId() + "&taxID=" + tp.getTaxID() + "&langID=" + tp.getLangID());
        } else {
            return changeNodeWithError(request, response, errors);
        }
    }

    // SECURITY: added
    @SuppressWarnings("unchecked")
   public ModelAndView saveNode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaxonParams tp = new TaxonParams(request,taxonomyService);

        Node node = nodeService.getNode(tp.getNodeID(), tp.getLangID());
        Collection<Attribute> attributes = this.attributeService.getAllAttributes(tp.getTaxID(), tp.getLangID());
        
        this.verifyNodeIsEditable(node);

        Map<String, String> errors = saveNodeValues(request, attributes, node, tp.getLangID());
        
        if (errors != null) {
            return changeNodeWithError(request, response, errors);
        } else {
            return changeNode(request, response);
        }
    }

    // SECURITY: added to called function
    public ModelAndView saveSimilar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return saveSimilarOrRelated(request, response, Relationship_type.SIMILAR);
    }

    public ModelAndView saveRelated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return saveSimilarOrRelated(request, response, Relationship_type.RELATED);
    }

    // SECURITY: added
    private ModelAndView saveSimilarOrRelated(HttpServletRequest request, HttpServletResponse response, int relationship_type_id) throws Exception {

        TaxonParams tp = new TaxonParams(request,taxonomyService);
        HashMap<String,String> errors = null;
        
        
        String[] string_node_ids = null;
        if (relationship_type_id == Relationship_type.RELATED) {
            string_node_ids = string_node_ids = StringUtils.split(request.getParameter("relatedtree_ids"), ",");
        } else {
            string_node_ids = string_node_ids = StringUtils.split(request.getParameter("similartree_ids"), ",");
        }

        Node thisNode = nodeService.getNode(tp.getNodeID(), Language.DEFAUL_LANG);
        verifyNodeIsEditable(thisNode);

        // Clear all old ones
        Collection<Related_node> rns = related_nodeService.getAllRelated_nodes();
        for (Related_node rn : rns) {
            if (rn.getNode().getId() == tp.getNodeID()) {
                if (rn.getRelationship().getId() == relationship_type_id) {
                    related_nodeService.removeRelated_node(rn);
                }
            }
        }

        for (String id_string : string_node_ids) {
            int related_node_id = Integer.parseInt(id_string);
            if (related_node_id != tp.getNodeID()) {
                Related_node rn = new Related_node();
                rn.setNode(thisNode);
                rn.setRelated_node(nodeService.getNode(related_node_id, 1));//TO DO: pass lang_id
                rn.setRelationship(relationship_typeService.getRelationship_type(relationship_type_id));
                related_nodeService.saveRelated_node(rn);
            } else {
            	errors = new HashMap<String,String>();
            	errors.put(ACTION_ERROR,"cannot relate taxon to self (other related taxons were saved)");
            }
        }

        return changeNodeGeneric(request, response, errors);
    }

    // SECURITY: added
    public ModelAndView setRoles(HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaxonParams tp = new TaxonParams(request,taxonomyService);

        Node this_node = nodeService.getNode(tp.getNodeID(), Language.DEFAUL_LANG);
        verifyNodeIsEditable(this_node);

        boolean do_children = (TaxonomySecurityHelper.amITaxonomyAdmin() && request.getParameter("apply_to_children")!=null);
        
        String[] roleNames = null;
        if (request.getParameterValues("setRoles") != null) {
            roleNames = request.getParameterValues("setRoles");
        }
        
        set_new_permissions_for_role(roleNames, this_node, do_children);

        return changeNode(request, response);
    }

    // SECURITY: added
    public ModelAndView setNewParent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaxonParams tp = new TaxonParams(request,taxonomyService);

        int fromID = Integer.parseInt(request.getParameter("fromID"));
        
        Node node = nodeService.getNode(fromID, Language.DEFAUL_LANG);
        
        Node orig_node_parent;
        
        
        if (tp.getParentID() == 0) {
            // Check if the original parent of this node still has children? If no, then make that node as a leaf node.
            if(node.getParent() != null) {
               orig_node_parent = nodeService.getNode(node.getParent().getId(), Language.DEFAUL_LANG);
               Collection childNodes = nodeService.getChildNodes(orig_node_parent.getId());
               if(childNodes.size() == 1 && orig_node_parent.getNot_leaf()) {
                  orig_node_parent.setNot_leaf(false);
                  nodeService.saveNode(orig_node_parent);
               }
            }
            
          //Move to root
            node.setParent(null);
            nodeService.saveNode(node);
        } else {
            Node node_parent = nodeService.getNode(tp.getParentID(), Language.DEFAUL_LANG);
            verifyNodeIsEditable(node);
            verifyNodeIsEditable(node_parent);
            nodeService.changeParent(node.getId(), node_parent.getId());
            
            // ADDED TO HANDLE THE LEAF STATUS FOR THE ORIGINAL AND NEW PARENT NODE
            // Find the original parent, check if this parent has any more children. If not, then make this parent as a leaf node.
            if(node.getParent() != null) {
               orig_node_parent = nodeService.getNode(node.getParent().getId(), Language.DEFAUL_LANG);

               Collection children = nodeService.getChildNodes(orig_node_parent.getId());

               if(children.size() == 0 && orig_node_parent.getNot_leaf()) {
                  //save_values_from_params(request, orig_node_parent, tp.getLangID(), true);
                  orig_node_parent.setNot_leaf(false);
                  nodeService.saveNode(orig_node_parent);
               }
            }
            
            // Now check if the parent is set as leaf? If yes, then change it to not leaf
            if(!node_parent.getNot_leaf()) {
               //save_values_from_params(request, node_parent, tp.getLangID(), true);
               node_parent.setNot_leaf(true);
               nodeService.saveNode(node_parent);
            }
            
        }
        
        return new ModelAndView("redirect:taxoneditor.htm?action=changeNode&nodeID=" + node.getId() + "&taxID=" + tp.getTaxID() + "&langID=" + tp.getLangID());
        
    }
    
    

    
    
}