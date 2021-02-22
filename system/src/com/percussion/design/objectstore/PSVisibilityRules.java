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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implementation for the PSXVisibilityRules DTD in BasicObjects.dtd.
 */
public class PSVisibilityRules extends PSCollectionComponent
{
   /**
    * Creates a new collection of PSRule obejcts.
    */
   public PSVisibilityRules()
   {
      super((new PSRule()).getClass());
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
   public PSVisibilityRules(Element sourceNode, IPSDocument parentDoc,
                            ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Get the data hiding setting.
    *
    * @return the data hiding setting.
    */
   public int getDataHiding()
   {
      return m_dataHiding;
   }

   /**
    * Set a new data hiding option.
    *
    * @param dataHiding the new data hiding option.
    */
   public void setDataHiding(int dataHiding)
   {
      if (dataHiding != DATA_HIDING_XML &&
          dataHiding != DATA_HIDING_XSL)
         throw new IllegalArgumentException("dataHiding option not supported");

      m_dataHiding = dataHiding;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSVisibilityRules, not <code>null</code>.
    */
   public void copyFrom(PSVisibilityRules c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_dataHiding = c.getDataHiding();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSVisibilityRules)) return false;
      if (!super.equals(o)) return false;
      PSVisibilityRules that = (PSVisibilityRules) o;
      return m_dataHiding == that.m_dataHiding;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_dataHiding);
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
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: data hiding attribute
         data = tree.getElementData(DATA_HIDING_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<DATA_HIDING_ENUM.length; i++)
            {
               if (DATA_HIDING_ENUM[i].equalsIgnoreCase(data))
               {
                  m_dataHiding = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  DATA_HIDING_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // get all visibility rules
         node = tree.getNextElement(PSRule.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            add(new PSRule(node, parentDoc, parentComponents));

            node = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
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
      root.setAttribute(DATA_HIDING_ATTR, DATA_HIDING_ENUM[m_dataHiding]);

      // create the rules
      Iterator it = iterator();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_dataHiding != DATA_HIDING_XML &&
          m_dataHiding != DATA_HIDING_XSL)
      {
         Object[] args = { DATA_HIDING_ENUM };
         context.validationError(this,
            IPSObjectStoreErrors.UNSUPPORTED_DATA_HIDING, args);
      }

      super.validate(context);
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXVisibilityRules";

   /**
    * XSL dataHiding specifier.
    */
   public static final int DATA_HIDING_XSL = 0;
   /**
    * XML dataHiding specifier.
    */
   public static final int DATA_HIDING_XML = 1;
   /**
    * An array of XML attribute values for the dataHiding. They are
    * specified at the index of the specifier.
    */
   private static final String[] DATA_HIDING_ENUM =
   {
      "xsl", "xml"
   };

   /** The data hiding specification */
   private int m_dataHiding = DATA_HIDING_XSL;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String DATA_HIDING_ATTR = "dataHiding";
}

