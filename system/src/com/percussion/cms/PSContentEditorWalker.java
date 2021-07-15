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
package com.percussion.cms;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility to perform operations on documents conforming with the 
 * sys_ContentEditor.dtd.
 */
public class PSContentEditorWalker
{
   /**
    * Get the first 'Control' element for the provided name.
    *
    * @param doc the document to search for the value, not <code>null</code>.
    * @param name the field name of the DisplayField element we want the
    *    value for, not <code>null</code> or empty.
    * @return the DisplayElement or <code>null</code> if not found.
    * @throws IllegalArgumentException if the provided document or name is
    *    <code>null</code> or if the name is empty.
    */
   public static Element getControlElement(Document doc, String name)
   {
      if (doc == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "parameters cannot be null or empty");
      
      NodeList controls = doc.getElementsByTagName(
         PSDisplayFieldElementBuilder.CONTROL_NAME);
      int i = 0;
      Node node;
      while ((node = controls.item(i++)) != null)
      {
         Element control = (Element)node;
         String paramName = control.getAttribute(
            PSDisplayFieldElementBuilder.PARAMNAME_NAME);
         if (paramName != null && paramName.equalsIgnoreCase(name))
            return control;
      }
      
      return null;
   }
   
   /**
    * Get the value of the first 'DisplayField' matching the provided name.
    * The value means here all 'Value' elements text data found either as
    * String or as list of Sting objects.
    *
    * @param doc the document to search for the value, not <code>null</code>.
    * @param name the field name of the 'DisplayField' element we want the
    *    value for, not <code>null</code> or empty.
    * @return the value found, a String object for single values, a 
    *    'DisplayChoices' element for multiple values, 
    *    <code>null</code> if not found.
    * @throws IllegalArgumentException if the provided document or name is
    *    <code>null</code> or the name is empty.
    */
   public static Object getDisplayFieldValue(Document doc, String name)
   {
      if (doc == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "parameters cannot be null or empty");

      Element control = getControlElement(doc, name);
      if (control != null)
      {
         List values = getValues(control);
         
         if (values.size() > 0)
         {
            Element elem = (Element) control.getFirstChild();
            
            /*
             * It must be a single value if the first child of the 'Control'
             * is a 'Value' element.
             */
            if (elem.getTagName().equals(
               PSDisplayFieldElementBuilder.DATA_NAME))
            {
               Object o = values.get(0);
               if (o != null)
                  return values.get(0).toString();
               else
                  return "";
            }
            
            /*
             * This must be a simple child, return the 'DisplayChoices'
             * element which is the first child of the found 'Control'.
             */
            return elem;
         }
      }
      
      return null;
   }
   
   /**
    * Get the text data of all 'Value' elements found in the provided element.
    *
    * @elem the element to get the 'Value' element data from, not
    *    <code>null</code>.
    * @return a list of String objects, might be empty but not
    *    <code>null</code>.
    */
   public static List getValues(Element elem)
   {
      if (elem == null)
         throw new IllegalArgumentException("elem cannot be null");
      
      List list = new ArrayList();
      NodeList values = elem.getElementsByTagName(
         PSDisplayFieldElementBuilder.DATA_NAME);
      for (int i=0; i<values.getLength(); i++)
      {
         Element value = (Element) values.item(i);
         Text text = (Text) value.getFirstChild();
         if (text != null)
            list.add(text.getNodeValue());
      }
      
      return list;
   }
   
   /**
    * Replaces or adds the Value element(s) in the document provided, for 
    * the supplied container element with the supplied value. If no 'Value' 
    * element is found this will add a new one.
    *
    * @param doc the document used, not <code>null</code>.
    * @param container the container to replace the Value elements in, not
    *    <code>null</code>.
    * @param value the new value, the toString method will be used to create
    *    the Sting value, not <code>null</code>.
    * @throws IllegalArgumentException if any provided argument is 
    *    <code>null</code>.
    */
   public static void replaceOrAddValue(Document doc, Element container, 
      Object value)
   {
      if (doc == null || container == null || value == null)
         throw new IllegalArgumentException("parameters cannot be null");
      
      NodeList values = container.getElementsByTagName(
         PSDisplayFieldElementBuilder.DATA_NAME);
      
      Node oldValue = values.item(0);
      Element newValue = createValue(doc, value.toString());

      if (oldValue == null)
         container.appendChild(newValue);
      else
         container.replaceChild(newValue, (Element)oldValue);
   }
   
   /**
    * Creates a new Value element in the provided document with the supplied
    * content.
    *
    * @param doc the document to create the Value element in, assumed not
    *    <code>null</code>.
    * @param content the Value element content, assumed not <code>null</code>.
    * @return the new Value element, never <code>null</code>.
    */
   private static Element createValue(Document doc, String content)
   {
      Element value = doc.createElement(PSDisplayFieldElementBuilder.DATA_NAME);
      value.appendChild(doc.createTextNode(content));
      
      return value;
   }
}
