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

package com.percussion.widgets.image.extensions;
      
      import com.percussion.xml.PSXmlTreeWalker;
      import java.util.ArrayList;
      import java.util.List;
      import org.apache.commons.lang.StringUtils;
      import org.apache.commons.lang.Validate;
      import org.apache.logging.log4j.LogManager;
      import org.apache.logging.log4j.Logger;
      import org.w3c.dom.Document;
      import org.w3c.dom.Element;
      
      public class PSItemXMLSupport
      {
    	  private static final Logger log = LogManager.getLogger(PSItemXMLSupport.class);
      
        public static Element getFieldElement(Document inputDoc, String fieldName)
        {
        	PSXmlTreeWalker fieldWalker = new PSXmlTreeWalker(inputDoc.getDocumentElement());
        	fieldWalker.getNextElement("ItemContent", 8);
        	Element field = fieldWalker.getNextElement("DisplayField", 8);
        	while (field != null)
          {
        		PSXmlTreeWalker fw = new PSXmlTreeWalker(field);
        		Element control = fw.getNextElement("Control", 8);
        		if (control != null)
            {
        			String fld = control.getAttribute("paramName");
        			if ((StringUtils.isNotBlank(fld)) && (fld.equals(fieldName)))
        			{
        				return field;
        			}
            }
   field = fieldWalker.getNextElement("DisplayField", 4);
          }
        	return null;
        }
      
        public static String getFieldValue(Element field)
        {
        	PSXmlTreeWalker w = new PSXmlTreeWalker(field);
        	Element c = w.getNextElement("Control", 8);
        	if (c != null)
          {
        		c = w.getNextElement("Value", 8);
        		if (c == null)
            {
        			return null;
            }
        		String val = w.getElementData();
        		return val;
          }
        	return null;
        }
      
        public static void setFieldValue(Document doc, String name, String value)
        {
        	Element f = getFieldElement(doc, name);
        	Validate.notNull(f, "Field " + name + " not found");
        	setFieldValue(f, value);
        }
      
        public static void setFieldValue(Element field, String value)
        {
        	PSXmlTreeWalker w = new PSXmlTreeWalker(field);
        	Element c = w.getNextElement("Control", 8);
        	if (c != null)
          {
        		Element v = w.getNextElement("Value", 8);
        		if (v == null)
            {
        			v = c.getOwnerDocument().createElement("Value");
        			c.appendChild(v);
            }
        		v.setTextContent(value);
          }
        }
      
        public static String getFieldLabel(Element field)
        {
        	if (field != null)
          {
        		PSXmlTreeWalker w = new PSXmlTreeWalker(field);
        		Element l = w.getNextElement("DisplayLabel", 8);
      
        	if (l != null)
            {
        		String label = w.getElementData();
        		return StringUtils.chomp(label, ":");
            }
        	log.debug("field has no label");
          }
        	return null;
        }
      
        public static boolean isMultiValue(Element field)
        {
        	PSXmlTreeWalker w = new PSXmlTreeWalker(field);
        	Element c = w.getNextElement("Control", 8);
        	if (c != null)
          {
        		String dim = c.getAttribute("dimension");
        		if ((StringUtils.isNotBlank(dim)) && (dim.equals("array")))
            {
        			return true;
            }
          }
        	return false;
        }
      
        public static List<String> getFieldValues(Element field)
        {
        	List values = new ArrayList();
      
 			PSXmlTreeWalker w = new PSXmlTreeWalker(field);
 			Element entry = w.getNextElement("Control/DisplayChoices/DisplayEntry", 8);
 			while (entry != null)
          {
	 		String selected = w.getElementData("@selected", false);
 			if ((StringUtils.isNotBlank(selected)) && (selected.equals("yes")))
            {
	 		String val = w.getElementData("Value", false);
	 		if (StringUtils.isNotBlank(val))
              {
            values.add(val);
              }
            }
        entry = w.getNextElement("DisplayEntry", 4);
          }
 		return values;
        }
      }
