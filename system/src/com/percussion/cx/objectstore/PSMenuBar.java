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
package com.percussion.cx.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The class that is used to represent menu actions as defined by 
 * 'sys_Action.dtd'.
 */
public class PSMenuBar implements IPSComponent
{
   /**
    * Constructs the menu bar with supplied actions and no properties. Use 
    * {@link #setProperties(PSProperties)} to set properties.
    * 
    * @param actions the list of menu actions, may not be <code>null</code> or 
    * empty and each action should represent a menu. See {@link 
    * PSAction#isMenu() } for more info.
    * 
    * @throws IllegalArgumentException if any actions is invalid.
    */
   public PSMenuBar(Iterator actions)
   {
      if(actions == null || !actions.hasNext())
         throw new IllegalArgumentException("actions may not be null or empty.");
         
      while(actions.hasNext())
      {
         Object obj = actions.next();
         if(obj instanceof PSMenuAction)
         {
            PSMenuAction action = (PSMenuAction)obj;
            if(action.isMenu())
               m_actions.add(obj);
            else
               throw new IllegalArgumentException("action must be a menu");
         }
         else
            throw new IllegalArgumentException(
               "Elements of children must be instances of PSAction");
      }
   }
   
   /**
    * Constructs the menu bar object from the supplied element. See {@link 
    * #toXml(Document) } for the expected form of xml.
    * 
    * @param element the element to load from, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public PSMenuBar(Element element) throws PSUnknownNodeTypeException
   {
      if(element == null)
         throw new IllegalArgumentException("element may not be null.");
         
      fromXml(element);
   }
   
   // implements interface method, see toXml(Document) for the expected format 
   //of the xml element.
   public void fromXml(Element sourceNode) 
      throws PSUnknownNodeTypeException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null.");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }         
      
      Element propsEl = PSComponentUtils.getChildElement(
         sourceNode, PSProperties.XML_NODE_NAME, false);
      m_props = null;
      
      if(propsEl != null)
         m_props = new PSProperties( propsEl );
         
      Iterator list = PSComponentUtils.getChildElements(
         sourceNode, PSMenuAction.XML_NODE_NAME);
      if(!list.hasNext())
      {
         Object[] args = { XML_NODE_NAME, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      while(list.hasNext())
      {
         PSMenuAction action = new PSMenuAction((Element)list.next());
         if(action.isMenu())
            m_actions.add(action);
         else
         {
            Object[] args = { XML_NODE_NAME, 
               PSMenuAction.XML_NODE_NAME, PSMenuAction.TYPE_MENUITEM};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }      
   }

   /**
    * Implements the IPSComponent interface method to produce XML representation
    * of this object. See the interface for description of the method and 
    * parameters.
    * <p>
    * The xml format is:
    * <pre><code>
    * &lt;!ELEMENT MenuBar (Props?, Action+)>
    * </code></pre>
    * 
    * @return the action element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null.");
         
      Element root = doc.createElement(XML_NODE_NAME);
      if(m_props != null)
         root.appendChild(m_props.toXml(doc));
         
      Iterator actions = m_actions.iterator();
      while(actions.hasNext())
         root.appendChild( ((PSMenuAction)actions.next()).toXml(doc) );
         
      return root;
   }
   
   /**
    * Sets the properties of this action.
    * 
    * @param props the properties, supply <code>null</code> to clear the 
    * existing properties.
    */
   public void setProperties(PSProperties props)
   {
      m_props = props;
   }
   
   /**
    * Gets the properties of this menu bar.
    * 
    * @return the properties, may be <code>null</code> 
    */
   public PSProperties getProperties()
   {
      return m_props;
   }
   
   /**
    * Gets the menu actions of this menu bar.
    * 
    * @return an iterator over zero or more <code>PSAction</code> objects, never
    * <code>null</code> or empty.
    */
   public Iterator getActions()
   {
      return m_actions.iterator();
   }
   
   //implements interface method.
   public boolean equals(Object obj)
   {
     boolean equals = true;
      
      if( !(obj instanceof PSMenuBar) )
         equals = false;
      else
      {          
         PSMenuBar other = (PSMenuBar)obj;
         if(m_props == null ^ other.m_props == null)
            equals = false;
         else if(m_props != null && !m_props.equals(other.m_props))
            equals = false;
         else if(!m_actions.equals(other.m_actions))
            equals = false;
      }
      
      return equals;
   }
   
   //implements interface method.   
   public int hashCode()
   {
      return (m_props == null ? 0 : m_props.hashCode()) + m_actions.hashCode();
   }
   
   /**
    * The properties of the menu bar, initialized as this object is loaded from 
    * xml, may be <code>null</code> if not specified. Never modified after 
    * initialization.
    */
   private PSProperties m_props = null;
   
   /**
    * List of menu actions that this menu bar contains, initialized to an empty
    * list and gets filled as this object is loaded from xml, never <code>null
    * </code> or empty after that.
    */
   private List m_actions = new ArrayList();
   
   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "MenuBar";
}
