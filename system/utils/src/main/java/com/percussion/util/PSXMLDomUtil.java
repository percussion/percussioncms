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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlUtil;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Helper class to facilitate common XML tasks. That is available to clients
 * (rxclient.jar).
 */
public class PSXMLDomUtil
{
   /**
    * Static helper function to retrieve the first child element of type
    * <code>Node.ELEMENT_NODE</code>. The standard DOM supports
    * getFirstChild() but does not allow for the type checking.
    * 
    * @param node the node to start from, must not be <code>null</code>
    * 
    * @return the first element child of the supplied node, or <code>null</code>
    *         if none were found
    */
   public static Element getFirstElementChild(Node node)
   {
      if (node == null)
         return null;

      Node child = node.getFirstChild();
      while (child != null)
      {
         if (child.getNodeType() == Node.ELEMENT_NODE)
            return (Element) child;

         child = child.getNextSibling();
      }
      return null;
   }

   /**
    * Static helper function to retrieve the first child element of type
    * <code>Node.ELEMENT_NODE</code> and check it against the supplied name,
    * if it is not the name, then throw an error. The standard DOM supports
    * getFirstChild() but does not allow for the type checking.
    * 
    * @param node the node to start from, must not be <code>null</code>
    * 
    * @param name the name of the node to check against, must not be <code>null
    * </code>
    *           or empty, is compared to this name case sensitive
    * 
    * @return the first element child of the supplied node compared case, never
    *         <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if a node was not found, or the name
    *            does not match the found node
    */
   public static Element getFirstElementChild(Node node, String name)
         throws PSUnknownNodeTypeException
   {
      Element el = getFirstElementChild(node);
      if (el == null)
         return null;

      checkNode(el, name);

      return el;
   }

   /**
    * Static helper function to retrieve only the <code>Node.ELEMENT_NODE</code>
    * type sibling based from the specified node. The standard DOM supports
    * getNextSibling(), but does not allow for node type checking.
    * 
    * @param node the specified node to start from, must not be
    *           <code>null</code>
    * 
    * @return the next sibling element as an Element, or <code>null</code> if
    *         there is no sibling of the supplied node
    */
   public static Element getNextElementSibling(Node node)
   {
      if (node == null)
         return null;

      Node sibling = node.getNextSibling();
      while (sibling != null)
      {
         if (sibling.getNodeType() == Node.ELEMENT_NODE)
            return (Element) sibling;

         sibling = sibling.getNextSibling();
      }
      return null;
   }

   /**
    * Static helper function to retrieve only the <code>Node.ELEMENT_NODE</code>
    * type sibling based from the specified node. The standard DOM supports
    * getNextSibling(), but does not allow for node type checking. This routine
    * also checks if the next element sibling is a specified named node.
    * 
    * @param node the specified node to start from, must not be
    *           <code>null</code>
    * 
    * @param name the name to check the node against to be sure the sibling is
    *           the one expected, must not be <code>null</code> or empty
    * 
    * @return the next sibling element as an Element, or <code>null</code> if
    *         there is no sibling of the supplied node and compared with name
    *         case sensitive,
    *         
    * @see #checkNode(Element, String) for more info
    * 
    * @throws PSUnknownNodeTypeException
    */
   public static Element getNextElementSibling(Node node, String name)
         throws PSUnknownNodeTypeException
   {
      Element el = getNextElementSibling(node);
      if (el == null)
         return null;

      if (el != null)
         checkNode(el, name);

      return el;
   }

   /**
    * Static helper function to get the element data of the specified node.
    * 
    * @param node the Node where the text data resides, may be <code>null</code>
    *           in which case this funtion will return ""
    * 
    * @return the complete text of the specified node or empty string if the
    *         node has no text or is <code>null</code>
    */
   public static String getElementData(Node node)
   {
      StringBuffer ret = new StringBuffer();

      if (node != null)
      {
         Node text;
         for (text = node.getFirstChild(); text != null; text = text
               .getNextSibling())
         {
            /**
             * the item's value is in one or more text nodes which are its
             * immediate children
             */
            if (text.getNodeType() == Node.TEXT_NODE)
            {
               ret.append(text.getNodeValue());
            }
            else
            {
               if (text.getNodeType() == Node.ENTITY_REFERENCE_NODE)
               {
                  ret.append(getElementData(text));
               }
            }
         }
      }
      return ret.toString();
   }

   /**
    * Static helper method to check a specific node for existance based on a
    * node name.
    * 
    * @param el the specific node to test, may be <code>null</code>
    * 
    * @param name the name of the node we are checking against, it does a case
    *           sensitive compare, must not be <code>null</code> or empty
    * 
    * @throws PSUnknownNodeTypeException if the node is <code>null</code> or
    *            is not the node we are checking for
    */
   public static void checkNode(Element el, String name)
         throws PSUnknownNodeTypeException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String nodeName = (el.getNamespaceURI() != null) ? el.getLocalName() : el
            .getNodeName();

      if (nodeName != null && !nodeName.equals(name))
      {
         Object[] args =
         {name, nodeName};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
   }

   /**
    * Static helper method to check an attribute of a specific element.
    * 
    * @param el the element to get the attribute from, must not be <code>null
    * </code>
    * 
    * @param name the name of the attribute to retrieve, must not be <code>
    * null</code>
    *           or empty
    * 
    * @param required a boolean flag to determine if we throw an error if it
    *           does not exist or just return blank, if true, we throw an error,
    *           otherwise we just return "" if not found
    * 
    * @return the value of the specified attribute or blank if not found and not
    *         required
    * 
    * @throws PSUnknownNodeTypeException if the attribute was not found and was
    *            required
    */
   public static String checkAttribute(Element el, String name, boolean required)
         throws PSUnknownNodeTypeException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val = el.getAttribute(name);
      if (required && (val == null || val.trim().length() == 0))
      {
         Object[] args =
         {el.getNodeName(), name, val};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      return (val == null) ? "" : val;
   }

   /**
    * Static helper method to check an attribute of a specific element and
    * returns an integer of that value.
    * 
    * @param el the element to get the attribute from, must not be <code>null
    * </code>
    * 
    * @param name the name of the attribute to retrieve, must not be <code>
    * null</code>
    *           or empty
    * 
    * @param required a boolean flag to determine if we throw an error if it
    *           does not exist or just return -1, if true, we throw an error,
    *           otherwise we just return -1 if not found
    * 
    * @return the value of the specified attribute or -1 if not found and not
    *         required
    * 
    * @throws PSUnknownNodeTypeException if the attribute was not found and was
    *            required
    * 
    * @throws NumberFormatException if the value does not contain a parsable
    *            integer
    */
   public static int checkAttributeInt(Element el, String name, boolean required)
         throws PSUnknownNodeTypeException, NumberFormatException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val = checkAttribute(el, name, required);

      return (val == null || val.trim().length() == 0) ? -1 : Integer
            .parseInt(val);
   }

   /**
    * Convenience method that calls {@link #checkAttributeBool(Element, String, 
    * boolean, String) checkAttributeBool(el, name, <code>true</code>,
    * "yes")}.
    */
   public static boolean checkAttributeBool(Element el, String name)
   {
      return checkAttributeBool(el, name, true, "yes");
   }

   /**
    * Static helper method to check an attribute of a specific element for a
    * <code>true</code>/<code>false</code> flag.
    * 
    * @param el the element to get the attribute from, must not be <code>null
    * </code>
    * 
    * @param name the name of the attribute to retrieve, must not be <code>
    * null</code>
    *           or empty
    * 
    * @param defaultValue If the attribute is not present or has no value, this
    *           value will be returned.
    * 
    * @param yesValue If the attribute has a non-empty value, it is compared
    *           against this case-insensitive. If it matches, <code>true</code>
    *           is returned. If <code>null</code> or empty, 'yes' is used.
    * 
    * @return <code>true</code> if the attributes value is present and matches
    *         the <code>yesValue</code>, otherwise the supplied <code>
    * defaultValue</code>
    *         is returned.
    */
   public static boolean checkAttributeBool(Element el, String name,
         boolean defaultValue, String yesValue)
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val;
      try
      {
         val = checkAttribute(el, name, false);
         val = val.trim();
      }
      catch (PSUnknownNodeTypeException e)
      {
         // never thrown when called like we do
         throw new RuntimeException("Unexpected exception: "
               + e.getLocalizedMessage());
      }

      boolean result = defaultValue;
      if (val.length() > 0)
      {
         if (yesValue == null || yesValue.trim().length() == 0)
            yesValue = "yes";
         result = val.equalsIgnoreCase(yesValue);
      }
      return result;
   }

   /**
    * Just like {@link #checkAttributeInt(Element, String, boolean)}, except
    * this method will return a <code>long</code>.
    */
   public static long checkAttributeLong(Element el, String name,
         boolean required) throws PSUnknownNodeTypeException,
         NumberFormatException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val = checkAttribute(el, name, required);

      return (val == null || val.trim().length() == 0) ? -1 : Long
            .parseLong(val);
   }

   /**
    * Just like {@link #checkAttributeInt(Element, String, boolean)}, except
    * this method will return a <code>Date</code>.
    * 
    * @return a date or <code>null</code> if the value is empty
    */
   public static Date checkAttributeDate(Element el, String name,
         boolean required) throws PSUnknownNodeTypeException,
         NumberFormatException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val = checkAttribute(el, name, required);

      if (StringUtils.isBlank(val))
      {
         return null;
      }
      else
      {
         return PSDataTypeConverter.parseStringToDate(val, new StringBuffer(), null);
      }
   }

   /**
    * Gets the element data from an attribute and validates that the data is a
    * legal value, returning the index of that value into the supplied array of
    * values.
    * 
    * @param el the element to get the attribute from, must not be <code>null
    * </code>
    * @param attrName the name of the attribute to retrieve data from; not
    *           <code>null</code>, or empty.
    * @param legalValues the array of permitted values, not <code>null</code>,
    *           with a default value at index 0 (must have at least one value).
    * @param required <code>true</code> to throw an exception if the attribute
    *           is not found or has no data, <code>false</code> to return 0 in
    *           this case.
    * 
    * @return The index into the supplied array that matches the attribute
    *         value. If the data is <code>null</code> or empty, index 0 of the
    *         legal value array is returned.
    * 
    * @throws PSUnknownNodeTypeException if the node is found and has an illegal
    *            value, regardless of if it is required. If required and not
    *            found, then this exception is thrown as well.
    */
   public static int checkAttributeEnumerated(Element el, String attrName,
         String[] legalValues, boolean required)
         throws PSUnknownNodeTypeException
   {
      if (el == null)
         throw new IllegalArgumentException("el may not be null");
      if (null == attrName || attrName.trim().length() == 0)
         throw new IllegalArgumentException("attrName cannot be null or empty");
      if (null == legalValues || legalValues.length == 0)
         throw new IllegalArgumentException("legalValues");

      String data = checkAttribute(el, attrName, required);
      int index = 0;
      // no value means use the default
      if (data != null && data.trim().length() > 0)
      {
         // make sure the value is legal
         boolean found = false;
         for (int i = 0; i < legalValues.length; i++)
         {
            if (legalValues[i] != null && legalValues[i].equals(data))
            {
               found = true;
               index = i;
               break;
            }
         }

         if (!found)
         {
            String parentName = el.getNodeName();
            Object[] args =
            {parentName, attrName, data};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      return index;
   }

   /**
    * Gets the trimmed value of the attribute.
    * 
    * @param source The element to check, may not be <code>null</code>.
    * @param name The name of the attribute, may not be <code>null</code> or
    *           empty.
    * 
    * @return The trimmed value, never empty, will be <code>null</code> if not
    *         found or found to be empty.
    */
   public static String getAttributeTrimmed(Element source, String name)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      String val = source.getAttribute(name);
      if (val != null && val.trim().length() == 0)
         val = null;
      else
         val = val.trim();

      return val;
   }

   /**
    * Return the node's name without any namespace qualifier
    * 
    * @param node Node may not be <code>null</code>
    * 
    * @return The unqualified name
    */
   public static String getUnqualifiedNodeName(Node node)
   {
      String unqualifiedName = node.getLocalName();
      return (unqualifiedName == null) ? node.getNodeName() : unqualifiedName;
   }

   /**
    * Makes a valid XML name and returns it.
    * 
    * @param str the string to make valid, never <code>null</code>.
    * @return the valid XML name.
    */
   public static String makeXmlName(String str)
   {
      if (str == null)
         throw new IllegalArgumentException("makeXmlName, null is not allowed");

      StringBuffer buf = new StringBuffer(str.length());

      if (str.length() == 0)
      {
         buf.append("root");
         return buf.toString();
      }

      char c = str.charAt(0);
      if (!(Character.isLetter(c) || '_' == c /* || ':' == c */
      ))
      {
         buf.append('_');
      }
      else
      {
         buf.append(c);
      }

      for (int i = 1; i < str.length(); i++)
      {
         c = str.charAt(i);
         if (!(Character.isLetter(c) || Character.isDigit(c) || '.' == c
               || '-' == c || '_' == c /* || ':' == c */
         ))
         {
            buf.append('_');
         }
         else
         {
            if (0x20DD <= c && c <= 0x20E0)
            {
               buf.append('_');
            }
            else
            {
               buf.append(c);
            }
         }
      }

      return buf.toString();
   }

   /**
    * Return the first child of the given element with the given tagname. Note
    * that the underlying DOM function allows the special value '*' to match
    * arbitrary tagnames.
    * 
    * @param parent Must be an element and must not be <code>null</code>.
    * @param tag Provides the tagname and may not be <code>null</code>.
    * @return the child if found and <code>null</code> if not found
    */
   public static Node findFirstNamedChildNode(Element parent, String tag)
   {
      NodeList children = parent.getElementsByTagName(tag);

      if (children.getLength() > 0)
      {
         return children.item(0);
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Retrieves all elements of a certain name that exist under the passed
    * in element. Will include all nested elements of the same name if the
    * flag is set to <code>true</code>.
    * @param root the element under which to locate the specified elements.
    * Cannot be <code>null</code>.
    * @param name the element name, cannot be <code>null</code> or empty.
    * @param includeNested flag indicating that all nested occurrences of the
    * element should be retrieved.
    * @return a list of all the located nodes, never <code>null</code>, may
    * be empty.
    */
   public static List<Element> getAllElementsByName(
      Element root, String name, boolean includeNested)
   {
      if(root == null)
         throw new IllegalArgumentException("root cannot be null.");
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");
      NodeList nl = root.getChildNodes();
      List<Element> nodes = new ArrayList<Element>();
      int len = nl.getLength();
      for(int i = 0; i < len; i++)
      {
         if(nl.item(i) instanceof Element)
         {
            Element el = (Element)nl.item(i);
            if(el.getNodeName().equalsIgnoreCase(name))
            {
               nodes.add(el);
            }
            if(includeNested && el.hasChildNodes())
            {
               nodes.addAll(getAllElementsByName(el, name, true));
            }
         }
      }
      return nodes;
   }

   /**
    * Returns the provided document as XML formatted, UTF8 encoded string with
    * indentations.
    *
    * @deprecated Use PSXmlUtil or base TransformerFactory
    *
    * @param node the {@link Node} to be returned as string, may be
    *           <code>null</code>.
    * @return the provided document as string, the error message in case of
    *         IOExceptions, an empty String if the provided document is
    *         <code>null</code>.
    */
   @Deprecated
   public static String toString(Node node)
   {
      return PSXmlUtil.toString(node);
   }
   
  

   /**
    * Get the boolean value from the element text data of the specified node.
    * Note: this method only check the values of <code>true</code> and/or
    * <code>1</code>, which is compliant with W3C. However, this method does
    * not check <code>yes</code>.
    * 
    * @param node the Node where the text data resides, may be <code>null</code>
    *           or empty.
    * 
    * @return <code>true</code> if the text of the specified node is
    *         <code>1</code> or <code>true</code>; otherwise return
    *         <code>false</code>. It is case insensitive.
    */
   public static boolean getBooleanElementData(Element node)
   {
      String sValue = PSXMLDomUtil.getElementData(node);
      return getBooleanData(sValue);
   }

   /**
    * Get the boolean value from the specified string / text. Note: this method
    * only check the values of <code>true</code> and/or <code>1</code>,
    * which is compliant with W3C. However, this method does not check
    * <code>yes</code>.
    * 
    * @param sValue the text data resides, may be <code>null</code> or empty.
    * 
    * @return <code>true</code> if the specified text is <code>1</code> or
    *         <code>true</code>; otherwise return <code>false</code>. It
    *         is case insensitive.
    */
   public static boolean getBooleanData(String sValue)
   {
      return "true".equalsIgnoreCase(sValue) || "1".equals(sValue);
   }

   /**
    * Removes the stylesheet processing instruction from the supplied document.
    * If the document is <code>null</code> or if the processing instruction is
    * not found then does nothing.
    * 
    * @param doc The Document from which stylesheet processing instruction needs
    *           to be removed, may be <code>null</code>.
    */
   public static void removeStyleSheetPiFromDoc(Document doc)
   {
      if (doc == null)
         return;
      NodeList nl = doc.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node node = nl.item(i);
         if (isNodeStyleSheetProcessingInstruction(node))
         {
            node.getParentNode().removeChild(node);
            return;
         }
      }
   }

   /**
    * Check the supplied Document for a stylesheet processing instruction.
    * 
    * @param doc The Document to check, may be <code>null</code>.
    * 
    * @return <code>true</code> if a stylesheet processing instruction is
    *         found, <code>false</code> if not or if the supplied doc is
    *         <code>null</code>.
    */
   public static boolean hasStyleSheetPiInDoc(Document doc)
   {
      if (doc == null)
         return false;
      NodeList nl = doc.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         if (isNodeStyleSheetProcessingInstruction(nl.item(i)))
            return true;
      }
      return false;
   }

   /**
    * Check the supplied node for a stylesheet processing instruction.
    * 
    * @param node The node to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the node is stylesheet processing
    *         instruction, <code>false</code> if not.
    */

   private static boolean isNodeStyleSheetProcessingInstruction(Node node)
   {
      if (node instanceof ProcessingInstruction)
      {
         if (((ProcessingInstruction) node).getTarget()
               .equals("xml-stylesheet"))
         {
            return true;
         }
      }
      return false;
   }
}
