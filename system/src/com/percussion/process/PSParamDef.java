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
package com.percussion.process;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a parameter definition used by a process def.  See
 * {@link PSProcessDef} for more info.  This class is immutable.
 */
public class PSParamDef
{
   /**
    * Construct this object from its XML representation.
    *   
    * @param source The element containing the XML represention, may not be
    * <code>null</code>.  See {@link #toXml(Document)} for more info.
    * 
    * @throws PSProcessException if the source element is malformed or if there 
    * are any errors.
    */
   public PSParamDef(Element source)  throws PSProcessException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      try
      {
         PSXMLDomUtil.checkNode(source, XML_NODE_NAME);
         
         m_name = PSXMLDomUtil.getAttributeTrimmed(source, ATTR_NAME);
         m_value = new PSResolvableValue(source);
         m_ifDefinedName = PSXMLDomUtil.getAttributeTrimmed(source, ATTR_IFDEF);
         m_separator = PSXMLDomUtil.getAttributeTrimmed(source, ATTR_SEPARATOR);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSProcessException(e.getLocalizedMessage());
      }
   }
   
   /**
    * Get the name of this param.
    * 
    * @return The name, may be <code>null</code>, never empty. 
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the resolvable value of this param.
    * 
    * @return The value, never <code>null</code>. 
    */
   public PSResolvableValue getValue()
   {
      return m_value;
   }
   
   /**
    * Get the name of the variable that must be defined for this parameter to
    * be included in the process request. 
    * 
    * @return The name, may be <code>null</code>, never empty.
    */
   public String getIfDefinedName()
   {
      return m_ifDefinedName;
   }
   
   /**
    * Get the separator to use when formatting the command arguments from the
    * name and value.  If <code>null</code>, a space is assumed which will
    * result in two separate command arguments for name and value.  If not,
    * then a single command argument is formed by concatenating 
    * name + separator + value.
    * 
    * @return The separator, may be <code>null</code>, never empty.
    */
   public String getSeparator()
   {
      return m_separator;
   }
   
   /**
    * Serializes this object to its XML representation.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    * 
    * @return The XML representation, never <code>null</code>, conforms to the 
    * PSXParam element as defined in sys_processes.dtd.
    */
   public Element toXml(Document doc)
   {
      Element el = doc.createElement(XML_NODE_NAME);
      
      el.setAttribute(ATTR_NAME, m_name);
      
      m_value.toXml(el);
      
      if (m_ifDefinedName != null)
         el.setAttribute(ATTR_IFDEF, m_ifDefinedName);

      if (m_separator != null)
         el.setAttribute(ATTR_SEPARATOR, m_separator);
         
      return el;      
   }
   
   /**
    * Marks this param as a first param of the group.
    */
   public void setBeginGroup()
   {
      m_beginGroup = true;
   }
   
   /**
    * Marks this param as a last param of the group.
    */
   public void setEndGroup()
   {
      m_endGroup = true;
   }
   
   /**
    * Indicates whether this param is the first param of the group.
    * @return <code>true</code> means the group has begun.
    */
   public boolean isBeginGroup()
   {
      return m_beginGroup;
   }
   
   /**
    * Indicates whether this param as the last param of the group.
    * @return <code>true</code> means the group has ended.
    */   
   public boolean isEndGroup()
   {
      return m_endGroup;
   }
   
   /**
    * Indicates whether this param is the first param of the group.
    * Defaults to <code>false</code>. 
    */
   private boolean m_beginGroup = false;
   
   /**
    * Indicates whether this param is the last param of the group. 
    * Defaults to <code>false</code>.
    */
   private boolean m_endGroup = false;
   
   /**
    * Name of the root element of the XML representation of this class.  
    */
   public static final String XML_NODE_NAME = "PSXParam";
   
   /**
    * The name of this parameter, may be <code>null</code>, never empty or 
    * modified after construction.
    */
   private String m_name;
   
   /**
    * The value and its resolver, never <code>null</code> or modified after
    * construction.
    */
   private PSResolvableValue m_value;
   
   /**
    * The variable name used to control the inclusion of this parameter in the
    * process request.  See {@link #getIfDefinedName()} for more info.
    * Initialized during construction, may be <code>null</code>, never emtpy,
    * never modified after that.
    */
   private String m_ifDefinedName;
   
   /**
    * The value to use as a separator between the name and resolved value.
    * See {@link #getSeparator()} for more info.  May be <code>null</code>, 
    * never empty or modified after construction.
    */
   private String m_separator;
   
   // private xml constants   
   private static final String ATTR_NAME = "name";   
   private static final String ATTR_IFDEF = "ifDefined";
   private static final String ATTR_SEPARATOR = "separator";   
}
