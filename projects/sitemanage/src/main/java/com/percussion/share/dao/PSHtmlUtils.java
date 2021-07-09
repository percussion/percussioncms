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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.share.dao;

import static com.percussion.xml.PSXmlDocumentBuilder.FLAG_ALLOW_NULL;
import static com.percussion.xml.PSXmlDocumentBuilder.FLAG_OMIT_DOC_TYPE;
import static com.percussion.xml.PSXmlDocumentBuilder.FLAG_OMIT_XML_DECL;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.share.service.exception.PSExtractHTMLException;
import com.percussion.util.PSTidyUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.List;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import net.htmlparser.jericho.Attribute;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

/**
 * Utility class to apply tidy to HTML and/or extract content from HTML string.
 * 
 * @author yubingchen
 */
public class PSHtmlUtils
{
    /**
     * The logger for this class.
     */
    private static final Logger ms_logger = LogManager.getLogger("PSHtmlUtils");
    
    /**
     * The flag used to convert XML to string.
     */
    private static int XML_TO_STRING_FLAGS = FLAG_ALLOW_NULL | FLAG_OMIT_DOC_TYPE | FLAG_OMIT_XML_DECL;

    /**
     * Strip the "script" HTML element from the specified HTML fragment.
     * 
     * @param source the HTML fragment, it may be <code>null</code> or empty.
     * 
     * @return the HTML fragment without the "script" element. It may be empty  
     * if the specified HTML fragment is <code>null</code> or empty. It can never be <code>null</code>.
     */
    public static String stripScriptElement(String source)
    {
        return stripElement(source, HTMLElementName.SCRIPT);
    }
    
    /**
     * Strip the "link" HTML element with attribute "rel='canonical'" from the specified HTML fragment.
     * 
     * @param source the HTML fragment, it may be <code>null</code> or empty.
     * 
     * @return the HTML fragment without the "link" HTML element with attribute "rel='canonical'". It may be empty  
     * if the specified HTML fragment is <code>null</code> or empty. It can never be <code>null</code>.
     */
    public static String stripLinkCanonicalElement(String source)
    {
        return stripElement(source, HTMLElementName.LINK, "rel", "canonical");
    }
    
    /**
     * Check the "link" HTML element with attribute "rel='canonical'" in the specified HTML fragment.
     * 
     * @param source the HTML fragment, it may be <code>null</code> or empty.
     * 
     * @return <code>true</code> if the HTML fragment contains the specified HTML element. Otherwise returns <code>false</code>.
     */
    public static boolean checkLinkCanonicalElement(String source)
    {
        return checkElementExists(source, HTMLElementName.LINK, "rel", "canonical");
    }
    
    /**
     * Strip the specified HTML element from the specified HTML fragment.
     * 
     * @param source the HTML fragment, it may be <code>null</code> or empty.
     * @param tagName the HTML tag name of the specified element, not blank.
     * 
     * @return the HTML fragment without the specified HTML element. It may be empty
     * if the specified HTML fragment is <code>null</code> or empty. It can never be <code>null</code>.
     */
    public static String stripElement(String source, String tagName) {
    	return stripElement(source, tagName, null, null);
    }
    
    
    /**
     * Strip the specified HTML element from the specified HTML fragment.
     * 
     * @param source the HTML fragment, it may be <code>null</code> or empty.
     * @param tagName the HTML tag name of the specified element, not blank.
     * @param attrName the HTML attribute name of the specified element, it may be <code>null</code> or empty. If <code>null</code> attributes will be ignored in match process.
     * @param attrValue the HTML attribute value of the specified element, it may be <code>null</code> or empty.
     * 
     * @return the HTML fragment without the specified HTML element. It may be empty
     * if the specified HTML fragment is <code>null</code> or empty. It can never be <code>null</code>.
     */
    public static String stripElement(String source, String tagName, String attrName, String attrValue)
    {
        notNull(tagName);
        notEmpty(tagName);
        
        if (StringUtils.isBlank(source))
            return source;
        
        Source src = new Source(source);
        OutputDocument outDoc = new OutputDocument(src);

        tagName = tagName.toLowerCase();
        List<StartTag> tags = src.getAllStartTags(tagName);
        for (Tag tag : tags)
        {
        	boolean attrMatch = true;
        	
        	if (!StringUtils.isBlank(attrName)) {
            	Attribute attr;
        		attr = tag.parseAttributes().get(attrName);
        		
        		if (attr != null) {
        			
        			String value;
        			value = attr.getValue();
        			if (value == null && !StringUtils.isBlank(attrValue)) { //if it is attribute like "checked" or "selected"
        				attrMatch = false;
        			} else if (!value.equals(attrValue)) attrMatch = false;
        			
        		} else attrMatch = false; //no such attribute in the element
        	}
        	
        	
        	if (attrMatch)
            {
                Element elem = tag.getElement();
                outDoc.remove(elem);
            }
        }
        
        return outDoc.toString();
    }

    /**
     * Checks if the specified HTML element contained in the specified HTML fragment.
     * 
     * @param source the HTML fragment, it may be <code>null</code> or empty.
     * @param tagName the HTML tag name of the specified element, not blank.
     * @param attrName the HTML attribute name of the specified element, it may be <code>null</code> or empty. If <code>null</code> attributes will be ignored in match process.
     * @param attrValue the HTML attribute value of the specified element, it may be <code>null</code> or empty.
     * 
     * @return <code>true</code> if the HTML fragment contains the specified HTML element. Otherwise returns <code>false</code>.
     */
    public static boolean checkElementExists(String source, String tagName, String attrName, String attrValue)
    {
        notNull(tagName);
        notEmpty(tagName);
        
        boolean attrMatch = false;
        if (StringUtils.isBlank(source))
            return attrMatch;
        
        Source src = new Source(source);
        tagName = tagName.toLowerCase();
        List<StartTag> tags = src.getAllStartTags(tagName);
        for (Tag tag : tags)
        {
        	attrMatch = true;
        	
        	if (!StringUtils.isBlank(attrName)) {
            	Attribute attr;
        		attr = tag.parseAttributes().get(attrName);
        		
        		if (attr != null) {
        			
        			String value;
        			value = attr.getValue();
        			if (value == null && !StringUtils.isBlank(attrValue)) { //if it is attribute like "checked" or "selected"
        				attrMatch = false;
        			} else if (!value.equals(attrValue)) attrMatch = false;
        			
        		} else attrMatch = false; //no such attribute in the element
        	}
        	
        	if (attrMatch) break;
        }
        
        return attrMatch;
    }
    
    
    /**
     * See {@link PSTidyUtils#applyTidy(String, String)}.
     */
    public static String applyTidy(String source, String filename)
    {
        return PSTidyUtils.applyTidy(source, filename);
    }
    
    /**
     * See {@link PSTidyUtils#getTidiedDocument(String, String)}.
     */
    public static Document getTidiedDocument(String source, String filename)
    {
       return PSTidyUtils.getTidiedDocument(source, filename);
    }
    
    /**
     * Extract the content from HTML string by the specified CSS-Selector.
     * The HTML string will be cleaned up (by tidy) before the extraction process.
     * 
     * @param cssSelector the CSS Selector, not <code>null</code> or empty. It supports all (CSS) Selector level 3, see http://www.w3.org/TR/css3-selectors. 
     * @param source the HTML string, not <code>null</code>. 
     * @param filename the file name that contains the source. It may be <code>null</code> or empty.
     * @param outerHTML determines extract outer or inner HTML. It is <code>true</code> if extracting outer HTML.
     * 
     * @return the extracted content, never <code>null</code>. 
     * 
     * @throws PSExtractHTMLException if a tidy error occurs.
     * no error will be logged here, but expecting caller to log the error as needed.
     */
    public static String extractHtml(String cssSelector, String source, String filename, boolean outerHTML)
    {
        Document tidiedSource = getTidiedDocument(source, filename);
        return extractHtmlOnly(cssSelector, tidiedSource, filename, outerHTML);
    }
    
    /**
     * This is the same as {@link #extractHtml(String, String, boolean)}, except the HTML source is already tidied.
     * 
     * @param cssSelector the CSS Selector, not <code>null</code>.
     * @param source the tidied HTML content, assumed not <code>null</code> or empty.
     * @param outerHTML determines extract outer or inner HTML. It is <code>true</code> if extracting outer HTML.
     * 
     * @return the extracted content, never <code>null</code>. 
     */
    private static String extractHtmlOnly(String cssSelector, Document sourceDoc, String filename, boolean outerHTML)
    {
        try
        {
            DOMNodeSelector selector = new DOMNodeSelector(sourceDoc);
            Set<Node> nodes = selector.querySelectorAll(cssSelector);
            return convertNodesToString(nodes, outerHTML);
        }
        catch (NodeSelectorException e)
        {
            String msg;
            if (StringUtils.isBlank(filename))
                msg = "Failed to extract HTML with CSS Selector, \"" + cssSelector + "\".";
            else
                msg = "Failed to extract HTML with CSS Selector \"" + cssSelector + "\" from file '" + filename + "'.";
            PSExtractHTMLException ex = new PSExtractHTMLException(msg, e);
            throw ex;
        }
    }

    /**
     * Converts the specified nodes to string.
     * 
     * @param nodes the nodes in question, assumed not <code>null</code>.
     * @param outerHTML determines extract outer or inner HTML. It is <code>true</code> if extracting outer HTML.
     * 
     * @return the converted string, not <code>null</code>.
     */
    private static String convertNodesToString(Set<Node> nodes, boolean outerHTML)
    {
        StringBuffer buffer = new StringBuffer();
        for (Node node : nodes)
        {
            String s = convertNodeToString(node, outerHTML);
            if (buffer.length() > 0)
                buffer.append('\n');
            buffer.append(s);
        }
        
        return buffer.toString();
    }
    
    /**
     * Converts a specified node to string.
     * 
     * @param node the node in question, assumed not <code>null</code>.
     * @param outerHTML determines extract outer or inner HTML. It is <code>true</code> if extracting outer HTML.
     * 
     * @return the converted string, not <code>null</code>.
     */
    private static String convertNodeToString(Node node, boolean outerHTML)
    {
        if (outerHTML)
            return PSXmlDocumentBuilder.toString(node, XML_TO_STRING_FLAGS);
        
        StringBuffer buffer = new StringBuffer();
        NodeList nodes = node.getChildNodes();
        int len = nodes.getLength();
        for (int i=0; i < len; i++)
        {
            Node n = nodes.item(i);
            String s = PSXmlDocumentBuilder.toString(n, XML_TO_STRING_FLAGS);
            if (buffer.length() > 0)
                buffer.append('\n');
            buffer.append(s);
        }
        return buffer.toString();
    }
}
