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
package com.percussion.util;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class a set of HTML parameters that can be posted to a Rhythmyx resource
 * The XML representation of the object of  this class has the following simple
 * DTD:
 * &lt;!ELEMENT HtmlParams ANY&gt;
 * The root element can have one or more child elements that are name value
 * pairs. The element name will be the parameter name and the value of the
 * element is the value of the parameter. There can be multiple elelements with
 * the same name so that multi-valued HTML parameter is supported.
 */
public class PSHtmlParamDocument
{
   private static final Logger log = LogManager.getLogger(PSHtmlParamDocument.class);
   /**
    * Default Constructor.
    */
   public PSHtmlParamDocument()
   {
   }

   /**
    * Constructor that takes a map of name-value pairs.
    * @param params must not be <code>null</code>.
    * @see setParams
    * @throws IllegalArgumentException if the paarameter supplied is <code>null</code>.
    */
   public PSHtmlParamDocument(Map params)
   {
      setParams(params);
   }

   /**
    * Builds a fresh object from the given XML document element. The source node
    * must conform to the DTD defined in the class description.
    * @param sourceNode root element of the XML document from which the object
    * is built, mut not be <code>null</code> and name must be {@link #ROOT}.
    * @throws IllegalArgumentException if the source node is <code>null</code> or
    * does not have the correct name (@link #ROOT).
    */
   public void fromXml(Element sourceNode)
   {
      if(sourceNode == null)
      {
         throw new IllegalArgumentException("sourceNode must not be null");
      }
      if(!sourceNode.getNodeName().equals(ROOT))
      {
         throw new IllegalArgumentException(sourceNode.getNodeName()
            + " is not a valid document root element");
      }
      NodeList nl = sourceNode.getChildNodes();
      Set keys = new HashSet();
      Node node = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         node = nl.item(i);
         if(!(node instanceof Element))
            continue;
         keys.add(node.getNodeName());
      }
      Iterator iter = keys.iterator();
      Node child = null;
      String key = null;
      while(iter.hasNext())
      {
         key = iter.next().toString();
         nl = sourceNode.getElementsByTagName(key);
         if(nl.getLength() == 1)
         {
            child = nl.item(0).getFirstChild();
            if(child != null && child instanceof Text)
               m_params.put(key, ((Text)child).getData());
         }
         else
         {
            List list = new ArrayList();
            for(int i=0; i<nl.getLength(); i++)
            {
               child = nl.item(i).getFirstChild();
               if(child != null && child instanceof Text)
                  list.add(((Text)child).getData());
            }
            m_params.put(key, list);
         }
      }
   }

   /**
    * Converts itself to its XML representation based on the DTD given in the
    * class description.
    * @return document element of the XML document representation of this
    * object, never <code>null</code>.
    * @throws IllegalArgumentException if the input document is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
      {
         throw new IllegalArgumentException("doc must not be null");
      }
      Element root = PSXmlDocumentBuilder.createRoot(doc, ROOT);

      Iterator keys = m_params.keySet().iterator();
      String key = null;
      String value = null;
      Object obj = null;
      while(keys.hasNext())
      {
         key = keys.next().toString();
         obj = m_params.get(key);
         if(obj == null)
            continue;
         if(obj instanceof String)
         {
            PSXmlDocumentBuilder.addElement(doc, root, key, obj.toString());
         }
         else if(obj instanceof List)
         {
            List values = (List)obj;
            Object val = null;
            for(int i=0; i<values.size(); i++)
            {
               val = values.get(i);
               PSXmlDocumentBuilder.addElement(doc, root, key, val.toString());
            }
         }
      }
      return root;
   }

   /**
    * Returns the String representation of the XML document representation of
    * this object.
    * @return XML string representation of the object, never <code>null</code>.
    */
   public String getXmlString()
   {
      return PSXmlDocumentBuilder.toString(
         toXml(PSXmlDocumentBuilder.createXmlDocument()));
   }

   /**
    * Set/replace the HTML parameter set.
    * @param params must not be <code>null</code>.
    * @throws IllegalArgumentException if the parameter set is <code>null</code>.
    */
   public void setParams(Map params)
   {
       if(params == null)
         throw new IllegalArgumentException("params must not be null");
     m_params = params;
   }

   /**
    * Add or change the HTML parameter
    * @param name name of the HTML parameter, must not be <code>null</code>.
    * @throws IllegalArgumentException if name of the parameter
    * @param value is the value of the HTML parameter to set. Must not be
    * <code>null</code>. This can either be instance of a String which means it is a
    * single values HTML parameter or instance of List that is a list of strings,
    * which menas the HTML parameter
    * is <code>null</code>.
    */
   public void setParam(String name, Object value)
   {
       if(name == null)
         throw new IllegalArgumentException("name must not be null");
      if(value == null && m_params.containsKey(name))
      {
         m_params.remove(name);
         return;
      }
     m_params.put(name, value);
   }

   /**
    * Remove a named HTML parameter from the object.
    * @param name name of the parameter to remove, must not be <code>null</code>
    * or empty.
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public void removeParam(String name)
   {
       if(name == null || name.length() < 1)
         throw new IllegalArgumentException("name must not be null or empty");
      if(m_params.containsKey(name))
         m_params.remove(name);
   }

   /**
    * Get the value of the named parameter.
    * @param name must not be <code>null</code> or empty.
    * @return value of the named parameter, may be <code>null</code>.
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public Object getParam(String name)
   {
       if(name == null || name.length() < 1)
         throw new IllegalArgumentException("name must not be null or empty");
      if(m_params.containsKey(name))
         return m_params.get(name);
      return null;
   }

   /**
    * Get the value of the named parameter suitable for SQL IN clause. This is
    * useful for a multi-valued HTML parameter that needs to be used in SQL
    * query in the selector of Rhythmyx resource. Does not enclose the string
    * in paranthesis.
    * @param name must not be <code>null</code> or empty.
    * @param encloseInQuotes flag to tell whether to include individual values
    * in quotes or not. Encloding in quotes makes the result useful for SQL
    * string based searches whereas not enclosing makes the result useful in
    * number based searches.
    *
    * @return value as a string than can readily be used in SQL IN clause,
    * never be <code>null</code> may be empty. Not elnclosed in paranthesis.
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public String getParamForInClause(String name, boolean encloseInQuotes)
   {
       if(name == null || name.length() < 1)
         throw new IllegalArgumentException("name must not be null or empty");
      String value = "";
      if(!m_params.containsKey(name))
         return value;

      Object obj = m_params.get(name);
      List list = new ArrayList();
      if(obj instanceof String)
         list.add(obj);
      else if(obj instanceof List)
         list.addAll((List)obj);

      for(int i=0; i<list.size(); i++)
      {
         if(encloseInQuotes)
            value = value + "\'";
         value = value + list.get(i).toString();
         if(encloseInQuotes)
            value = value + "\'";
         if(i!=list.size()-1)
            value = value + ",";
      }
      return value;
   }

   /**
    * Get map of parameters which can be modified, if needed.
    * @return map of parameters, never <code>null</code> may be empty.
    */
   public Map getParams()
   {
      return m_params;
   }

   /**
    * Map of all parameter name-value pairs, never <code>null</code>.
    */
   Map m_params = new HashMap();

   /**
    * main method for testing
    * @param args, none required
    */
   public static void main(String[] args)
   {
      PSHtmlParamDocument htmlDoc = new PSHtmlParamDocument();
      htmlDoc.setParam("testName1", "TestValue1");
      htmlDoc.setParam("testName2", "TestValue2");

      log.info("Test Case1: single values parameters");
      log.info(htmlDoc.getXmlString());

      Map params = new HashMap();
      params.put("testParam1", "testParamValue1");
      params.put("testParam2", "testParamValue2");
      List values = new ArrayList();
      values.add("testParamValue31");
      values.add("testParamValue32");
      values.add("testParamValue33");
      params.put("testParam3", values);
      htmlDoc = new PSHtmlParamDocument(params);
      log.info("Test Case2: Multivalued parameter included");
      log.info(htmlDoc.getXmlString());

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      htmlDoc.fromXml(htmlDoc.toXml(doc));
      htmlDoc.removeParam("testParam2");
      log.info("Test Case3: Roundtripping the params and remove a param");
      log.info(htmlDoc.getXmlString());

      log.info("Test Case4: Get a named parameter");
      log.info("testParam3 = {}",  htmlDoc.getParam("testParam3"));

      log.info("Test Case5: Get a named multi-valued (strings) parameter for SQL IN clause");
      log.info("testParam3 = {} ", htmlDoc.getParamForInClause("testParam3", true));

      log.info("Test Case6: Get a named multi-valued (numbers) parameter for SQL IN clause");
      log.info("testParam3 = {} ", htmlDoc.getParamForInClause("testParam3", false));
   }

   /**
    * Name of the root element of the XML document
    */
   static public final String ROOT = "HtmlParams";

   /**
    * Name of the operand suffix for searchfield operator
    */
   static public final String OPERATOR_SUFFIX = "_operator";
}