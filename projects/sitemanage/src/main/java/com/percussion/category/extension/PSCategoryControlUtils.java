/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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

package com.percussion.category.extension;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.service.IPSCategoryService;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class PSCategoryControlUtils {
	
    private static volatile IPSCategoryService categoryService = null;
    
	public static final Logger log = LogManager.getLogger(PSCategoryControlUtils.class);
	
	public static PSCategory getCategories(String siteName, String rootPath, boolean includeDeleted, boolean includeNotSelectable) throws PSDataServiceException {
		
	    if (categoryService== null)
	        categoryService = (IPSCategoryService) getWebApplicationContext().getBean("categoryService");
		return categoryService.getCategoryTreeForSite(siteName, rootPath, includeDeleted, includeNotSelectable);
	}
	
	public static PSCategoryNode findCategoryNode(String siteName, String rootPath, boolean includeDeleted, boolean includeNotSelectable) {
        
        if (categoryService== null)
            categoryService = (IPSCategoryService) getWebApplicationContext().getBean("categoryService");
        return categoryService.findCategoryNode(siteName, rootPath, includeDeleted, includeNotSelectable);
    }
	
	/**
	 * Method to convert the category xml into a string. Need this to create the XML Document.
	 * @param category
	 * @return String containing category xml
	 */
	public static String getCategoryXmlInString(PSCategory category) {
		
		StringWriter writer = new StringWriter();
		
		JAXBContext jaxbContext;
	
		try {
		    jaxbContext = JAXBContext.newInstance(PSCategory.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			jaxbMarshaller.marshal(category, writer);
		} catch (JAXBException e) {
			log.error("JAXB Exception occurred while marshalling category object to xml string- PSCategoryControlUtils.getCategoryXmlInString()", e);		
		} catch (Exception e) {
			log.error("Exception occurred while marshalling category object to xml string- PSCategoryControlUtils.getCategoryXmlInString()", e);		
		}

		return writer.toString();
	}
	
	/**
	 * Method to find the parent node (that was set in the property of the control) in the categories.
	 * 
	 * @param nodes - List of nodes to look in
	 * @param parentCategory - parent category value set in the control property.
	 * 
	 * @return - The parent category node
	 */
	private static PSCategoryNode findParentNode(List<PSCategoryNode> nodes, String parentCategory) {
		
		PSCategoryNode parentNode = null;
		
		for(PSCategoryNode node : nodes) {
			
			if(node.getTitle().equals(parentCategory)) {
				parentNode = node;
			} else {
				if(node.getChildNodes() != null && !node.getChildNodes().isEmpty()) {
					parentNode = findParentNode(node.getChildNodes(), parentCategory);
				}
			}
			
			if(parentNode != null)
				break;
		}
		
		return parentNode;
	}
	
	/**
	 * Method to filter the nodes of the parent category based on the selectable property.
	 * 
	 * @param parentNode
	 * @return category node after filtering
	 */
	private static PSCategoryNode filterNode(PSCategoryNode parentNode) {
		
		List<PSCategoryNode> childNodeList = new ArrayList<>();
		
		if(parentNode.getChildNodes() != null && !parentNode.getChildNodes().isEmpty()) {
			for(PSCategoryNode node : parentNode.getChildNodes()) {
				if(node.isSelectable())
					childNodeList.add(node);
			}
			
			parentNode.setChildNodes(null);
			parentNode.setChildNodes(childNodeList);
		}
		
		return parentNode;
		
	}
	
	/**
	 * Method to convert the category xml file in the format that other controls:
	 * checkboxtree and pageautolist can understand and display.
	 * @param doc
	 * @return the category xml Document
	 */
	@SuppressWarnings("unchecked")
	public static Document convertToOldFormatXml(Document doc) {
		
		// create the new Document object. 
		Document document = DocumentHelper.createDocument();
		// preapre the root element of this new Document.
		Element root = document.addElement("Tree");
		List<Attribute> attList = doc.getRootElement().attributes();
		for(Attribute attr : attList) {
			if(attr.getName().equalsIgnoreCase("title"))
				root.addAttribute("label", attr.getStringValue());
		}
		
		Iterator<Element> it = doc.getRootElement().elementIterator();
		
		while(it.hasNext()) {
			Element e = it.next();
			if(e.getName().equalsIgnoreCase("Children")) {
				// cerate a new element with name Node.
				Element newElement = root.addElement("Node");
				
				List<Attribute> attributes = e.attributes();
				for(Attribute attr : attributes) {
					if(attr.getName().equalsIgnoreCase("id"))
						newElement.addAttribute("id", attr.getStringValue());
					if(attr.getName().equalsIgnoreCase("title"))
						newElement.addAttribute("label", attr.getStringValue());
					if(attr.getName().equalsIgnoreCase("selectable")) {
						if(attr.getStringValue().equalsIgnoreCase("true"))
							newElement.addAttribute("selectable", "yes");
						else
							newElement.addAttribute("selectable", "no");
					}
				}
				
				if(e.elements() != null && !e.elements().isEmpty()) {
					Iterator<Element> eleIt = e.elementIterator();
					
					while(eleIt.hasNext()) {
						createChildElement(eleIt.next(), newElement);
					}
					
				}
				
				// then check if this element has child elements create new node element for each child element.
				
			}
		}
		return document;
	}
	
	/**
	 * Supporting method to convert the category xml in the format that is 
	 * understandable by checkboxtree and pageautolist.
	 * @param source
	 * @param targetParent
	 */
	@SuppressWarnings("unchecked")
	private static void createChildElement(Element source, Element targetParent) {
		
		Element newChild = targetParent.addElement("Node");
		
		List<Attribute> attributes = source.attributes();
		for(Attribute attr : attributes) {
			if(attr.getName().equalsIgnoreCase("id"))
				newChild.addAttribute("id", attr.getStringValue());
			if(attr.getName().equalsIgnoreCase("title"))
				newChild.addAttribute("label", attr.getStringValue());
			if(attr.getName().equalsIgnoreCase("selectable")){
				if(attr.getStringValue().equalsIgnoreCase("true"))
					newChild.addAttribute("selectable", "yes");
				else
					newChild.addAttribute("selectable", "no");
			}
				
		}
		
		if(source.elements() != null && !source.elements().isEmpty()) {
			Iterator<Element> eleIt = source.elementIterator();
			
			while(eleIt.hasNext()) {
				createChildElement(eleIt.next(), newChild);
			}
		}
	}
}
