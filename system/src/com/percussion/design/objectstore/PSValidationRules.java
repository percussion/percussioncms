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

import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation for the PSXValidationRules DTD in BasicObjects.dtd.
 */
public class PSValidationRules extends PSCollectionComponent
{
   /**
    * Create a new collection of PSConditionalExit objects.
    */
   public PSValidationRules()
   {
      super((new PSConditionalExit()).getClass());
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSValidationRules(Element sourceNode, IPSDocument parentDoc,
                            ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Get the number of errors after validation should stop.
    *
    * @return the number of errors after validation should stop.
    */
   public int getMaxErrorsToStop()
   {
      return m_maxErrorsToStop;
   }

   /**
    * Set a new number ob errors after validation should stop.
    *
    * @param maxErrorsToStop the new number of errors after validation should
    *    stop. Must be greater than 0.
    */
   public void setMaxErrorsToStop(int maxErrorsToStop)
   {
      if (maxErrorsToStop <= 0)
         throw new IllegalArgumentException(
            "mxaErrorsToStop must be greater than 0");

      m_maxErrorsToStop = maxErrorsToStop;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSValidationRules object, not <code>null</code>.
    */
   public void copyFrom(PSValidationRules c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setMaxErrorsToStop(c.getMaxErrorsToStop());
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSValidationRules))
         return false;

      PSValidationRules t = (PSValidationRules) o;

      boolean equal = true;

      if (size() != t.size())
         equal = false;
      else if (getMaxErrorsToStop() != t.getMaxErrorsToStop())
         equal = false;
      else
      {
         for (int i=0; i<size() && equal; i++)
         {
            IPSComponent c1 = (IPSComponent) get(i);
            IPSComponent c2 = (IPSComponent) t.get(i);
            if (!PSComponent.compare(c1, c2))
               equal = false;
         }
      }

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      //AP: does not feel right - equals() is overwritten, hashCode is not 
      return super.hashCode();
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
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
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the maxErrorsToStop attribute
         data = tree.getElementData(MAX_ERRORS_TO_STOP_ATTR);
         if (data != null) // use the default if not specified
            m_maxErrorsToStop = Integer.parseInt(data);

         // get all validation rules
         Element node = tree.getNextElement(
            PSConditionalExit.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            add(new PSConditionalExit(node, parentDoc, parentComponents));

            node = tree.getNextElement(
               PSConditionalExit.XML_NODE_NAME, nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(
         MAX_ERRORS_TO_STOP_ATTR, Integer.toString(m_maxErrorsToStop));

      // create the rules
      Iterator it = iterator();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXValidationRules";

   /**
    * This attributes specifies the number of errors until the process is
    * stopped and the errors returned to the user.
    */
   private int m_maxErrorsToStop = 10;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String MAX_ERRORS_TO_STOP_ATTR = "maxErrorsToStop";
}

