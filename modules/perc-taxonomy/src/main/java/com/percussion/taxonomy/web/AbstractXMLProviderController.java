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

import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.service.NodeService;
import com.percussion.taxonomy.service.TaxonomyService;
import com.percussion.taxonomy.web.xmlGeneration.Attr;
import com.percussion.taxonomy.web.xmlGeneration.Item;
import com.percussion.taxonomy.web.xmlGeneration.RootTag;
import com.percussion.taxonomy.web.xmlGeneration.Value;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.xml.core.Persister;
import org.springframework.web.servlet.ModelAndView;

public class AbstractXMLProviderController extends AbstractControllerWithSecurityChecks {

	private static final Logger log = LogManager.getLogger(AbstractXMLProviderController.class);

	public static final int NORMAL = 0;
	public static final int NO_CHILDREN = 1;
	public static final int ONLY_CHILDREN = 2;
	public static final int TOP_LEVEL_ONLY = 3;
	public static final int MINIMAL = 4;

	protected NodeService nodeService;
	
	protected TaxonomyService taxonomyService;

	protected class SortNodeName implements Comparator<Object[]>{

	    public int compare(Object[] o1, Object[] o2) {
	        String s1 = (o1[1] == null ? "0" : o1[1].toString()) + o1[2].toString();
	        String s2 = (o2[1] == null ? "0" : o2[1].toString()) + o2[2].toString();
	        return s1.compareTo(s2);
	    }
	}
	
	protected ModelAndView getXML(HttpServletRequest request, HttpServletResponse response, String html_id_prefix, String link, boolean confirm_link, int request_type, int skip_id, Collection<Integer> already_picked_node_ids) throws Exception {
		TaxonParams tp = new TaxonParams(request,taxonomyService);
		RootTag root = new RootTag();
		
    	// TODO in the future admins might be able to browse / search in a different language but for now we assume editors are editing in English (even if they are entering data for different language)
    	// int langID = tp.getLangID();
    	int langID = Language.DEFAUL_LANG; 
		
    	boolean exclude_disabled = (request.getParameter("exclude_disabled")!=null && request.getParameter("exclude_disabled").equals("true"));		
		
    	Hashtable<Integer, Hashtable<String,Collection<String>>> titles_hashtable = buildTitlesHashtable(tp.getTaxID(),langID);
		
		
		List<Object[]> the_nodes = null;
		Collection<Object[]> unfilted_nodes = (Collection<Object[]>) nodeService.getAllNodeNames(tp.getTaxID(), langID);

		// create helper objects
		ArrayList<Integer> nodes_with_children = new ArrayList<Integer>();
		ArrayList<Integer> root_nodes = new ArrayList<Integer>();
		for (Node node : (Collection<Node>) nodeService.getAllNodes(tp.getTaxID(), langID)) {
			if (node.getParent() != null) {
				nodes_with_children.add(node.getParent().getId());
			} else {
				root_nodes.add(node.getId());
			}

		}

		if (request_type == NO_CHILDREN) {
			the_nodes = new ArrayList<Object[]>();
			Collection<Integer> child_ids = children_and_sub_ids(tp.getNodeID(), false);
			for (Object[] obj : unfilted_nodes) {
				if (!child_ids.contains(new Integer(String.valueOf(obj[0])))) {
					the_nodes.add(obj);
				}
			}
		} else if (request_type == ONLY_CHILDREN) {
			the_nodes = new ArrayList<Object[]>();
			Collection<Integer> child_ids = children_and_sub_ids(tp.getNodeID(), true); // firstlevelonly
			for (Object[] obj : unfilted_nodes) {
				Integer obj_id = new Integer(String.valueOf(obj[0]));
				if (child_ids.contains(obj_id) && (obj_id.intValue() != tp.getNodeID()) && obj_id.intValue() != skip_id) {
					the_nodes.add(obj);
				}
			}
		}else if (request_type == TOP_LEVEL_ONLY) {
			the_nodes = new ArrayList<Object[]>();
			for (Object[] obj : unfilted_nodes) {
				Integer obj_id = new Integer(String.valueOf(obj[0]));
				if (root_nodes.contains(obj_id) && (obj_id.intValue() != skip_id)) {
					the_nodes.add(obj);
				}
			}

		}else if (request_type == MINIMAL) {
			the_nodes = new ArrayList<Object[]>();
			Collection<Integer> ids_we_must_have = new ArrayList<Integer>();
			for (Integer id_we_must_have : already_picked_node_ids) {
				ids_we_must_have.addAll(getSelfAndElders(id_we_must_have.intValue(), langID));
				ids_we_must_have.addAll(children_and_sub_ids(id_we_must_have.intValue(), true)); // firstlevelonly

			}
			ids_we_must_have.addAll(root_nodes);
			for (Object[] obj : unfilted_nodes) {
				if (ids_we_must_have.contains(new Integer(String.valueOf(obj[0])))) {
					the_nodes.add(obj);
				}
			}

		} else {
			the_nodes = new ArrayList<Object[]>(unfilted_nodes);
		}

		Collections.sort(the_nodes, new SortNodeName());
		
		int index = 0;
		for (Object[] tripletPlusTwo : the_nodes) {
			int nodeID = (Integer) tripletPlusTwo[0];
			
			ArrayList<Attr> attributes = null;

			if (tp.getForJEXL()) {
				attributes = new ArrayList();
			}

			String title = buildTitle(titles_hashtable.get(nodeID), attributes, langID, tripletPlusTwo, tp.getForJEXL(), exclude_disabled);

			String xml_link = null;
			String xml_onclick = null;
			if (link != null) {
				xml_link = StringUtils.replace(link, "[NODE_ID]", String.valueOf(nodeID));
				if (confirm_link){
					xml_onclick="return confirm('Are you sure?');";
				}
			}else {
				xml_link = "#";
				xml_onclick = "return false;";
			}

			String node_name = tripletPlusTwo[2].toString();
			if (((Integer) tripletPlusTwo[4]).intValue() != Node_status.ACTIVE){
				node_name += " [archived]";
			}
			
			Item item = new Item(html_id_prefix + nodeID, (tripletPlusTwo[1] == null) ? null : (html_id_prefix + tripletPlusTwo[1].toString()), title, node_name, xml_link,xml_onclick);
			if ((request_type == ONLY_CHILDREN || request_type == TOP_LEVEL_ONLY || request_type == MINIMAL) && nodes_with_children.contains(nodeID)) {
				item.state = "closed";
			}

			// For JEXL - additional XML
			if (tp.getForJEXL()) {
				for (Attr attribute : attributes) {
					item.addAttribute(attribute);
					// System.out.println("Adding "+attribute.name);
				}
			}
			root.addItem(item);
		}

		Persister serializer = new Persister();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			serializer.write(root, baos);
		} catch (Exception e) {
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}

		HashMap<String, Object> myModel = new HashMap<String, Object>();
		myModel.put("xml", baos.toString());
		return new ModelAndView("xmlprovider", "model", myModel);
	}

	private int nextIndex(int currentNodeID, int currentIndex, ArrayList<Object[]> list) {
		for (int i = currentIndex; i < list.size(); i++) {
			if (((Integer) list.get(i)[0]).intValue() != currentNodeID) {
				return i;
			}
		}
		return list.size();
	}

	// TODO move to DAO
	protected Collection<Integer> getSelfAndAncestors(int nodeID, int langID) {
		Collection<Integer> ret = new ArrayList<Integer>();
		Node bottom_node = nodeService.getNode(nodeID, langID);
		if (bottom_node.getParent() != null) {
			ret.addAll(getSelfAndAncestors(bottom_node.getParent().getId(), langID));
		}
		ret.add(bottom_node.getId());
		return ret;
	}

	
	// TODO move to DAO

	protected Collection<Integer> getSelfAndElders(int nodeID, int langID) {
		Collection<Integer> ret = new ArrayList<Integer>();
		Node bottom_node = nodeService.getNode(nodeID, langID);
		if (bottom_node.getParent() != null) {
			for (Node sibling : (Collection<Node>) nodeService.getChildNodes(bottom_node.getParent().getId())) {
				ret.add(new Integer(sibling.getId()));
			}
			ret.addAll(getSelfAndElders(bottom_node.getParent().getId(), langID));
		}
		ret.add(nodeID);
		return ret;
	}

	// TODO move to DAO

	protected Collection<Integer> children_and_sub_ids(int nodeID, boolean first_level_only) {
		Collection<Integer> ret = new ArrayList<Integer>();
		for (Node node : (Collection<Node>) nodeService.getChildNodes(nodeID)) {
			ret.add(new Integer(node.getId()));
			if (!first_level_only) {
				ret.addAll(children_and_sub_ids(node.getId(), first_level_only));
			}
		}
		ret.add(new Integer(nodeID)); // ADD self (required for top level)
		return ret;
	}

	protected Hashtable<Integer, Hashtable<String,Collection<String>>> buildTitlesHashtable(int taxID, int langID) {
		// build titles
		ArrayList<Object[]> raw_titles = new ArrayList(nodeService.getTitlesForNodes(taxID, langID));
		Hashtable<Integer, Hashtable<String,Collection<String>>> titles_hashtable = new Hashtable<Integer, Hashtable<String,Collection<String>>>();
		for (Object[] title : (Collection<Object[]>) raw_titles){
			Integer key = (Integer) title[0]; // node_id
			String name = (String) title[1]; // attr name
			String value = (String) title[2]; // attr value
			Hashtable<String,Collection<String>> title_pairs = null;
			if (titles_hashtable.containsKey(key)){
				title_pairs = titles_hashtable.get(key);
			}else{
				title_pairs = new Hashtable<String,Collection<String>>();
			}
			Collection<String> values = null;
			if (title_pairs.containsKey(name)){
				values = title_pairs.get(name);
			}else{
				values = new ArrayList<String>();
			}
			values.add(value);
			title_pairs.put(name,values);
			titles_hashtable.put(key,title_pairs);
		}
		return titles_hashtable;
	}
	
	
	protected String buildTitle(Hashtable<String,Collection<String>> title_pairs, ArrayList<Attr> attributes, int langID, Object[] tripletPlusTwo, boolean is_for_jexl, boolean exclude_disabled){
		
		ArrayList<String> title_string_array = new ArrayList<String>();

		for (String key : title_pairs.keySet()){
			title_string_array.add(key + ": " + StringUtils.join(title_pairs.get(key),","));

			if (is_for_jexl) {
				Attr attribute = new Attr();
				attribute.langID = langID;
				attribute.name = key;
				attribute.addValue(new Value(langID, StringUtils.join(title_pairs.get(key),",")));
				attributes.add(attribute);
			}
		
		}
		String ret  = StringUtils.join(title_string_array, " | ");

		if (tripletPlusTwo.length > 3) {
			boolean isSelectable = ((Boolean) tripletPlusTwo[3]).booleanValue();
			int status_id = ((Integer) tripletPlusTwo[4]).intValue();

			// Disable Nodes that are marked as "not selectable"
			if (exclude_disabled && (!isSelectable || (status_id != Node_status.ACTIVE))) {
				ret = "Disabled Taxon";
			}
		}
		
		return ret;
	}
	

	// wired by spring properties
	
	public void setNodeService(NodeService nodeService) {
	   this.nodeService = nodeService;
	}
	   
    public void setTaxonomyService(TaxonomyService taxonomyService)
    {
        this.taxonomyService = taxonomyService;
    }

}
