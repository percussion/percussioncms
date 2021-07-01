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
