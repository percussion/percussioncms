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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class holds one clone handler configuration in memory. It creates or
 * reads documents conforming to the sys_CloneHandlerConfig.dtd.  
 */
public class PSCloneHandlerConfig extends PSComponent
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
   public PSCloneHandlerConfig(Element sourceNode, IPSDocument parentDoc, 
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }
   
   /**
    * Get the clone handler name. The name is unique server-wide.
    * 
    * @return the clone handler name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the process check for the supplied name and context.
    * 
    * @param name the name to get the process check for, not <code>null</code>
    *    or empty.
    * @param context the context to get the process check for, not 
    *    <code>null</code> or empty.
    * @return the process check found for the supplied name and context, might 
    *    be <code>null</code> if no process check exits for the provided 
    *    parameters.
    * @throws IllegalArgumentException if name or context is <code>null</code> 
    *    or empty.
    */
   public PSProcessCheck getProcessCheck(String name, String context)
   {
      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null or empty");
        
      if (context == null || context.trim().length() == 0)
        throw new IllegalArgumentException("context cannot be null or empty");
        
      Iterator checks = getProcessChecks();
      while (checks.hasNext())
      {
         PSProcessCheck check = (PSProcessCheck) checks.next();
         if (check.getName().equals(name) && check.getContext().equals(context))
            return check;
      }
      
      return null;
   }
   
   /**
    * Gets all process checks ordered according to the sequence setting.
    * 
    * @return a list in the correct order for processing with all defined 
    *    process checks for this clone handler.
    */
   public Iterator getProcessChecks()
   {
      return m_processChecks.iterator();
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
         if (m_name == null)
         {
            Object[] args =
            { 
               XML_NODE_NAME, 
               NAME_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // OPTIONAL: Process checks
         node = tree.getNextElement(PROCESS_CHECKS_ELEM, firstFlags);
         m_processChecks.clear();
         if (node != null)
         {
            node = tree.getNextElement(
               PSProcessCheck.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               PSProcessCheck check = new PSProcessCheck(node, null, null);
               if (getProcessCheck(check.getName(), check.getContext()) != null)
               {
                  Object[] args =
                  { 
                     XML_NODE_NAME, 
                     PSProcessCheck.XML_NODE_NAME,
                     "Duplicate entry, must be unique configuration wide: " + 
                        check.getName()
                  };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
               }

               // add the new process check sorted in sequence order
               boolean inserted = false;
               for (int i=0; i<m_processChecks.size(); i++)
               {
                  PSProcessCheck c = (PSProcessCheck) m_processChecks.get(i);
                  if (c.getSequence() > check.getSequence())
                  {
                     m_processChecks.add(i, check);
                     inserted = true;
                     break;
                  }
               }
               if (!inserted)
                  m_processChecks.add(check);
               
               node = tree.getNextElement(
                  PSProcessCheck.XML_NODE_NAME, nextFlags);
            }
         }
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
      
      Element elem = null;
      
      // add processs checks
      Iterator checks = getProcessChecks();
      if (checks.hasNext())
      {
         elem = doc.createElement(PROCESS_CHECKS_ELEM);
         root.appendChild(elem);
      }
      while (checks.hasNext())
         elem.appendChild(((IPSComponent) checks.next()).toXml(doc));
         
      return root;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXCloneHandlerConfig";
   
   /**
    * The clone handler name, server-wide unique.  Initialized in ctor, nerver 
    * changed after that, never <code>null</code> or empty.
    */
   private String m_name = null;
   
   /**
    * A collection of process checks. Initialized in ctor, nerver changed 
    * after that. Never <code>null</code>, might be empty. The collection is
    * sorted in order of specified sequence.
    */
   private PSCollection m_processChecks = new PSCollection(PSProcessCheck.class);

   /*
    * The following strings define all elements/attributes used to parse/create 
    * the XML for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String PROCESS_CHECKS_ELEM = "ProcessChecks";
}
