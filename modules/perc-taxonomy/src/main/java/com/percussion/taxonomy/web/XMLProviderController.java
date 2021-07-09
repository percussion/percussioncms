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

package com.percussion.taxonomy.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.domain.Value;

@Controller
public class XMLProviderController extends AbstractXMLProviderController {

    public ModelAndView getXMLJSTreeNav(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	TaxonParams tp = new TaxonParams(request,taxonomyService);
        String prefix = request.getParameter("prefix");
        String link = "taxoneditor.htm?action=changeNode&nodeID=[NODE_ID]&taxID=" + tp.getTaxID() + "&langID=" + tp.getLangID();
        
        int request_type = -1;
        
        if (request.getParameter("only_expand_children")!=null && request.getParameter("only_expand_children").equals("true") && tp.hasNodeID()){
        	request_type = AbstractXMLProviderController.ONLY_CHILDREN;
        }else{
        	request_type = tp.hasNodeID() ? AbstractXMLProviderController.MINIMAL : AbstractXMLProviderController.TOP_LEVEL_ONLY;
        }
        

        Collection<Integer> already_picked_node_ids = null;
        
        if (request_type == AbstractXMLProviderController.MINIMAL){
        	already_picked_node_ids = new ArrayList<Integer>();
        	already_picked_node_ids.add(tp.getNodeID());
        }
        
        
        return getXML(request, response, prefix, link, false, request_type, -1, already_picked_node_ids);
    }

    public ModelAndView getXMLJSTreeMove(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	TaxonParams tp = new TaxonParams(request,taxonomyService);
        String prefix = request.getParameter("prefix");
        
        int fromID = Integer.parseInt(request.getParameter("fromID"));
        int skip_id = fromID;

        
        String link = "taxoneditor.htm?action=setNewParent&fromID=" + fromID + "&parentID=[NODE_ID]&taxID=" + tp.getTaxID() + "&langID=" + tp.getLangID();

        
        
        int request_type = -1;
        
        if (request.getParameter("only_expand_children")!=null && request.getParameter("only_expand_children").equals("true")){
        	request_type = AbstractXMLProviderController.ONLY_CHILDREN;
        }else{
        	request_type = AbstractXMLProviderController.TOP_LEVEL_ONLY;
        }
        
        return getXML(request, response, prefix, link, true, request_type, skip_id, null);
    }

    public ModelAndView getXMLJSTree(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	TaxonParams tp = new TaxonParams(request,taxonomyService);
        String prefix = request.getParameter("prefix");
        return getXML(request, response, prefix, null, false, AbstractXMLProviderController.NORMAL, -1, null);
    }

    public ModelAndView getXMLJSTreeLazy(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	TaxonParams tp = new TaxonParams(request,taxonomyService);
        String prefix = request.getParameter("prefix");
        
        Collection<Integer> already_picked_node_ids = null;
        if (prefix.indexOf("related")>=0){
        	already_picked_node_ids = new ArrayList<Integer>();
        	for (Related_node related_node : (Collection<Related_node>) nodeService.getRelatedNodes(tp.getNodeID())){
        		already_picked_node_ids.add(related_node.getRelated_node().getId());
        	}
        	
        }else if (prefix.indexOf("similar")>=0){
        	already_picked_node_ids = new ArrayList<Integer>();
        	for (Related_node related_node : (Collection<Related_node>) nodeService.getSimilarNodes(tp.getNodeID())){
        		already_picked_node_ids.add(related_node.getRelated_node().getId());
        	}
        } else if (prefix.indexOf("treenode")>=0){
        	already_picked_node_ids = new ArrayList<Integer>();
        	if (request.getParameter("already_picked_node_ids").length() > 0){
        		for (String id_as_string : StringUtils.split(StringUtils.trimToEmpty(request.getParameter("already_picked_node_ids")),",")){
        			already_picked_node_ids.add(Integer.parseInt(StringUtils.trimToEmpty(id_as_string)));
        		}
        	}
        }
        
        
        
        
        
        int request_type = -1;
        
        if (request.getParameter("only_expand_children")!=null && request.getParameter("only_expand_children").equals("true")){
        	request_type = AbstractXMLProviderController.ONLY_CHILDREN;
        }else{
        	request_type = AbstractXMLProviderController.MINIMAL;
        }        
        return getXML(request, response, prefix, null, false, request_type, -1, already_picked_node_ids);
    }
    
    public ModelAndView getJSTreeSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<String, Object>();
    	TaxonParams tp = new TaxonParams(request,taxonomyService);

    	boolean exclude_disabled = (request.getParameter("exclude_disabled")!=null && request.getParameter("exclude_disabled").equals("true"));

    	String prefix = request.getParameter("prefix");
    	
    	ArrayList<String> a = new ArrayList<String>();
    	
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG; 
    	
    	for (Node node : (Collection <Node>) nodeService.getNodesFromSearch(tp.getTaxID(), langID, request.getParameter("q"),exclude_disabled)){
    		for (Integer node_id : getSelfAndAncestors(node.getId(), langID)){
    			a.add("\"#" + prefix + node_id + '"');
    		}
    	}
    	
    	myModel.put("json", "[" + StringUtils.join(a,",") + "]");
		return new ModelAndView("getjstreesearch", "model", myModel);
    	
    }

    public ModelAndView setInUse(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<String, Object>();
    	
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG;
    	    	
    	// build collection of integer ids
    	Collection<Integer> ids = new ArrayList<Integer>();
    	for (String id_string : StringUtils.split(StringUtils.trimToEmpty(request.getParameter("ids")), ',')){
    		if (NumberUtils.toInt(StringUtils.trimToEmpty(id_string)) > 0){
    			ids.add(NumberUtils.toInt(StringUtils.trimToEmpty(id_string)));
    		}
    	}
    	
    	for (Node node : (Collection<Node>)nodeService.getSomeNodes(ids)){
    		if (!node.getIn_use()){
    			node.setIn_use(true);
    			nodeService.saveNode(node);
    		}
    	}
    	
    	
		return new ModelAndView("getidstostring", "model", myModel);
    }
    
    public ModelAndView getIdsToString(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<String, Object>();
    	
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG;
    	    	
    	// build collection of integer ids
    	Collection<Integer> ids = new ArrayList<Integer>();
    	String param_value = StringUtils.trimToEmpty(request.getParameter("ids"));
    	param_value = StringUtils.remove(param_value, " ");
    	for (String id_string : StringUtils.split(param_value, ',')){
    		if (NumberUtils.toInt(id_string) > 0){
    			ids.add(NumberUtils.toInt(id_string));
    		}
    	}
    	
    	// To avoid any exception in case there is no taxonomy selected for a content item
    	if(ids.size() == 0)
    	   return new ModelAndView("getidstostring", "model", myModel);
    	
    	// build collection of node names
    	Collection<String> node_names = new ArrayList<String>();
    	for (Object[] obj : (Collection<Object[]>)nodeService.getSomeNodeNames(ids, langID)){
    		node_names.add(obj[2].toString());
    	}
    	
    	// joins names and return value
    	myModel.put("idstostring", StringUtils.join(node_names.toArray(),','));
		return new ModelAndView("getidstostring", "model", myModel);
    }
    
    public ModelAndView getAutocompleteSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<String, Object>();
    	TaxonParams tp = new TaxonParams(request,taxonomyService);

    	String prefix = request.getParameter("prefix");
    	String[] exclude_id_strings = StringUtils.split(request.getParameter("exclude_ids"),",");

    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG; 
    	
    	Hashtable<Integer, Hashtable<String,Collection<String>>> titles_hashtable = buildTitlesHashtable(tp.getTaxID(),langID);
    	
		Collection<Object[]> unfilted_nodes = (Collection<Object[]>) nodeService.getAllNodeNames(tp.getTaxID(), langID);
    	
    	ArrayList<String> a = new ArrayList<String>();
    	
    	int z = 0;
    	

    	
    	for (Node node : (Collection <Node>) nodeService.getNodesFromSearch(tp.getTaxID(), langID, request.getParameter("term"),true)){
    			// note this is were we set the max dropdown length
    			// done find ones we have already picked
    			if (z <= 5 && !ArrayUtils.contains(exclude_id_strings, String.valueOf(node.getId()))){
    				Object[] tripletPlusTwo = null;
    				
    				// find matching node name value
    				for (Object[] obj : unfilted_nodes){
    					Integer obj_id = new Integer(String.valueOf(obj[0]));
    					if (obj_id.intValue() == node.getId()){
    						tripletPlusTwo = obj;
    					}
    				}
    				
    				
    				// parent abbr
    				String parent_abbr = "";
    				if (node.getParent()!= null){
        				for (Object[] obj : unfilted_nodes){
        					Integer obj_id = new Integer(String.valueOf(obj[0]));
        					if (obj_id.intValue() == node.getParent().getId()){
        						parent_abbr = obj[2].toString();
        						if (StringUtils.contains(parent_abbr, "(") && StringUtils.contains(parent_abbr, ")")){
        							parent_abbr = StringUtils.split(StringUtils.replace(parent_abbr, ")", "("),"(")[1];
        						}
        						parent_abbr = parent_abbr + " -- ";
        					}
        				}
    				}
    				
    				
    				String title = buildTitle(titles_hashtable.get(node.getId()), null, langID, tripletPlusTwo, false, true);
    				String s = "{";
    				s += "\"id\" : \"#" + prefix + node.getId() + "\", ";
    				s += "\"label\" : \"" + StringUtils.replace(parent_abbr + tripletPlusTwo[2],"\"","\\\"") + "\", ";
    				s += "\"value\" : \"" + StringEscapeUtils.escapeHtml(parent_abbr + tripletPlusTwo[2]) + "\", ";
    				s += "\"title\" : \"" + StringUtils.replace(StringEscapeUtils.escapeHtml(title),"|","&#13;&#10;") + "\"";
    				s +=  "}";
    				
    				a.add(s);
    				z = z + 1;
    			}
    			
 
    	}
    	
    	myModel.put("json", "[" + StringUtils.join(a,",") + "]");
		return new ModelAndView("getautocompletesearch", "model", myModel);
    	
    }
    
    
}
