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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implements the PSXProcessCheck element defined in sys_CloneHandlerConfig.dtd.
 */
public class PSProcessCheck extends PSComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of 
    *    the appropriate type
    */
   public PSProcessCheck(Element sourceNode, IPSDocument parentDoc, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }
   
   /**
    * Gets the process check name.
    * 
    * @return the process check name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Gets the process check context.
    * 
    * @return the process check context, never <code>null</code> or empty.
    */
   public String getContext()
   {
      return m_context;
   }
   
   /**
    * Gets the process check description.
    * 
    * @return the description,  may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return m_description;
   }
   
   /**
    * Gets the process check sequence number.
    * 
    * @return the process check sequence number, can be any integer.
    */
   public int getSequence()
   {
      return m_sequence;
   }
   
   /**
    * Get the current collection of conditions (a collection of {@link PSRule}
    * objects).
    *
    * @return the collection of conditions {@link PSRule}, never 
    *    <code>null</code>, may be empty.
    */
   public Iterator getConditions()
   {
      return m_conditions.iterator();
   }
   
   /**
    * Sets the conditions to be satisfied to execute this process check.
    * 
    * @param conds list of conditions, may not be <code>null</code>, can be
    * empty.
    * 
    * @throws IllegalArgumentException if conds is <code>null</code>.
    */
   public void setConditions(Iterator conds)
   {
      if(conds == null)
         throw new IllegalArgumentException("conds may not be null.");
         
      m_conditions.clear();
      while(conds.hasNext())
         m_conditions.add(conds.next());
   }
   
   /**
    * Gets the name of the process check as the String representation of this
    * object.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public String toString()
   {
      return m_name;
   }
   
   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try 
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: name attribute
         m_name = tree.getElementData(NAME_ATTR);
         if (m_name == null || m_name.trim().length() == 0)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               NAME_ATTR,
               (m_name == null) ? "null" : "empty"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         // REQUIRED: context attribute
         m_context = tree.getElementData(CONTEXT_ATTR);
         if (m_context == null || m_context.trim().length() == 0)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               CONTEXT_ATTR,
               (m_context == null) ? "null" : "empty"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         // REQUIRED: sequence attribute
         data = tree.getElementData(SEQUENCE_ATTR);
         if (data == null)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               SEQUENCE_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         try
         {
            m_sequence = Integer.parseInt(data);
         }
         catch (NumberFormatException e)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               SEQUENCE_ATTR,
               data
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         Node current = tree.getCurrent();
         
         // OPTIONAL: get the conditions
         node = tree.getNextElement(CONDITIONS_ELEM, firstFlags);
         m_conditions.clear();
         if (node != null)
         {
            node = tree.getNextElement(PSRule.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               m_conditions.add(new PSRule(node, parentDoc, parentComponents));
               
               node = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
            }
         }
         
         tree.setCurrent(current);
         
         //OPTIONAL: get the description     
         m_description = null;    
         Element descEl = tree.getNextElement(DESCRIPTION_ELEM, firstFlags);       
         if(descEl != null)
            m_description = tree.getElementData(descEl);
      } 
      finally 
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(CONTEXT_ATTR, m_context);
      root.setAttribute(SEQUENCE_ATTR, Integer.toString(m_sequence));

      // store the conditions
      Iterator conditions = getConditions();
      if (conditions.hasNext())
      {
         Element elem = doc.createElement(CONDITIONS_ELEM);
         while (conditions.hasNext())
            elem.appendChild(((IPSComponent) conditions.next()).toXml(doc));
            
         root.appendChild(elem);
      }
      
      if(m_description != null)
      {
         PSXmlDocumentBuilder.addElement(
            doc, root, DESCRIPTION_ELEM, m_description);
      }

      return root;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      PSProcessCheck clone = (PSProcessCheck) super.clone();
      clone.m_name = m_name;
      clone.m_sequence = m_sequence;
      clone.m_description = m_description;
      clone.m_context = m_context;
      clone.m_conditions = new PSCollection(PSRule.class);
      Iterator iter = m_conditions.iterator();
      while (iter.hasNext())
         clone.m_conditions.add(((PSRule)iter.next()).clone());

      return clone;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXProcessCheck";
   
   /**
    * The name of this process check. Configuration-wide unique. Initialized
    * in ctor, never <code>null</code> or empty after that.
    */
   private String m_name = null;
   
   /**
    * The context in which this process check is used. Initialized in ctor,
    * never <code>null</code> or empty after that.
    */
   private String m_context = null;
   
   /**
    * The processing sequence of this check. Initialized in ctor, never changed
    * after that.
    */
   private int m_sequence = -1;
   
   /**
    * The description of this check. Initialized in ctor, never changed
    * after that. May be <code>null</code> or empty.
    */
   private String m_description = null;
   
   /**
    * A collection of conditions ({@link PSRule} objects) that specify if the 
    * effect is beeing executed or not. Initialized in the ctor, never changed
    * after that. Never <code>null</code>, might be empty.
    */
   private PSCollection m_conditions = new PSCollection(PSRule.class);

   /*
    * The following strings define all elements/attributes used to parse/create 
    * the XML for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String CONTEXT_ATTR = "context";
   private static final String SEQUENCE_ATTR = "sequence";
   private static final String CONDITIONS_ELEM = "Conditions";
   private static final String DESCRIPTION_ELEM = "Description";   
}
