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

import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Related_node;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
        if (prefix.contains("related")){
        	already_picked_node_ids = new ArrayList<>();
        	for (Related_node related_node : nodeService.getRelatedNodes(tp.getNodeID())){
        		already_picked_node_ids.add(related_node.getRelated_node().getId());
        	}
        	
        }else if (prefix.contains("similar")){
        	already_picked_node_ids = new ArrayList<>();
        	for (Related_node related_node : nodeService.getSimilarNodes(tp.getNodeID())){
        		already_picked_node_ids.add(related_node.getRelated_node().getId());
        	}
        } else if (prefix.contains("treenode")){
        	already_picked_node_ids = new ArrayList<>();
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
    	HashMap<String, Object> myModel = new HashMap<>();
    	TaxonParams tp = new TaxonParams(request,taxonomyService);

    	boolean exclude_disabled = (request.getParameter("exclude_disabled")!=null && request.getParameter("exclude_disabled").equals("true"));

    	String prefix = request.getParameter("prefix");
    	
    	ArrayList<String> a = new ArrayList<>();
    	
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG; 
    	
    	for (Node node : nodeService.getNodesFromSearch(tp.getTaxID(), langID, request.getParameter("q"),exclude_disabled)){
    		for (Integer node_id : getSelfAndAncestors(node.getId(), langID)){
    			a.add("\"#" + prefix + node_id + '"');
    		}
    	}
    	
    	myModel.put("json", "[" + StringUtils.join(a,",") + "]");
		return new ModelAndView("getjstreesearch", "model", myModel);
    	
    }

    public ModelAndView setInUse(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<>();
    	
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG;
    	    	
    	// build collection of integer ids
    	Collection<Integer> ids = new ArrayList<>();
    	for (String id_string : StringUtils.split(StringUtils.trimToEmpty(request.getParameter("ids")), ',')){
    		if (NumberUtils.toInt(StringUtils.trimToEmpty(id_string)) > 0){
    			ids.add(NumberUtils.toInt(StringUtils.trimToEmpty(id_string)));
    		}
    	}
    	
    	for (Node node : nodeService.getSomeNodes(ids)){
    		if (!node.getIn_use()){
    			node.setIn_use(true);
    			nodeService.saveNode(node);
    		}
    	}
    	
    	
		return new ModelAndView("getidstostring", "model", myModel);
    }
    
    public ModelAndView getIdsToString(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<>();
    	
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG;
    	    	
    	// build collection of integer ids
    	Collection<Integer> ids = new ArrayList<>();
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
    	Collection<String> node_names = new ArrayList<>();
    	for (Object[] obj : nodeService.getSomeNodeNames(ids, langID)){
    		node_names.add(obj[2].toString());
    	}
    	
    	// joins names and return value
    	myModel.put("idstostring", StringUtils.join(node_names.toArray(),','));
		return new ModelAndView("getidstostring", "model", myModel);
    }
    
    public ModelAndView getAutocompleteSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap<String, Object> myModel = new HashMap<>();
    	TaxonParams tp = new TaxonParams(request,taxonomyService);

    	String prefix = request.getParameter("prefix");
    	String[] exclude_id_strings = StringUtils.split(request.getParameter("exclude_ids"),",");

    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG; 
    	
    	ConcurrentHashMap<Integer, ConcurrentHashMap<String,Collection<String>>> titles_ConcurrentHashMap = buildTitlesConcurrentHashMap(tp.getTaxID(),langID);
    	
		Collection<Object[]> unfilted_nodes = nodeService.getAllNodeNames(tp.getTaxID(), langID);
    	
    	ArrayList<String> a = new ArrayList<>();
    	
    	int z = 0;
    	

    	
    	for (Node node : nodeService.getNodesFromSearch(tp.getTaxID(), langID, request.getParameter("term"),true)){
    			// note this is were we set the max dropdown length
    			// done find ones we have already picked
    			if (z <= 5 && !ArrayUtils.contains(exclude_id_strings, String.valueOf(node.getId()))){
    				Object[] tripletPlusTwo = null;
    				
    				// find matching node name value
    				for (Object[] obj : unfilted_nodes){
    					Integer obj_id = new Integer(String.valueOf(obj[0]));
    					if (obj_id == node.getId()){
    						tripletPlusTwo = obj;
    					}
    				}
    				
    				
    				// parent abbr
    				String parent_abbr = "";
    				if (node.getParent()!= null){
        				for (Object[] obj : unfilted_nodes){
        					Integer obj_id = new Integer(String.valueOf(obj[0]));
        					if (obj_id == node.getParent().getId()){
        						parent_abbr = obj[2].toString();
        						if (StringUtils.contains(parent_abbr, "(") && StringUtils.contains(parent_abbr, ")")){
        							parent_abbr = StringUtils.split(StringUtils.replace(parent_abbr, ")", "("),"(")[1];
        						}
        						parent_abbr = parent_abbr + " -- ";
        					}
        				}
    				}
    				
    				
    				String title = buildTitle(titles_ConcurrentHashMap.get(node.getId()), null, langID, tripletPlusTwo, false, true);
    				String s = "{";
    				s += "\"id\" : \"#" + prefix + node.getId() + "\", ";
    				s += "\"label\" : \"" + StringUtils.replace(parent_abbr + tripletPlusTwo[2],"\"","\\\"") + "\", ";
    				s += "\"value\" : \"" + StringEscapeUtils.escapeHtml4(parent_abbr + tripletPlusTwo[2]) + "\", ";
    				s += "\"title\" : \"" + StringUtils.replace(StringEscapeUtils.escapeHtml4(title),"|","&#13;&#10;") + "\"";
    				s +=  "}";
    				
    				a.add(s);
    				z = z + 1;
    			}
    			
 
    	}
    	
    	myModel.put("json", "[" + StringUtils.join(a,",") + "]");
		return new ModelAndView("getautocompletesearch", "model", myModel);
    	
    }
    
    
}
