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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation for the PSXNullEntry DTD in BasicObjects.dtd.
 */
public class PSNullEntry  extends PSEntry
{
   /**
    * Creates a new null entry for the provided parameters.
    *
    * @param value the value of this entry, not <code>null</code>,May be empty.
    * @param label the label for this entry, not <code>null</code>
    * @throws IllegalArgumentException if value or text is <code>null</code>
    */
   public PSNullEntry(String value, PSDisplayText label)
   {
      super(value, label);
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
   public PSNullEntry(Element sourceNode, IPSDocument parentDoc, 
                      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   /**
    * Get the include when attribute.
    *
    * @return the current include when attribute.
    */
   public int getIncludeWhen()
   {
      return m_includeWhen;
   }
   
   /**
    * Set the include when attribute.
    *
    * @param includeWhen the new include when attribute.
    * @throws IllegalArgumentException if the include when provided is not 
    *    known.
    */
   public void setIncludeWhen(int includeWhen)
   {
      if (includeWhen != INCLUDE_WHEN_ALWAYS &&
          includeWhen != INCLUDE_WHEN_ONLY_IF_NULL)
         throw new IllegalArgumentException("unknown inchlude when attribute");
      
      m_includeWhen = includeWhen;
   }

   /**
    * Get the current sort order.
    *
    * @return the sort order.
    */
   public int getSortOrder()
   {
      return m_sortOrder;
   }
   
   /**
    * Set the new sort order.
    *
    * @param sortOrder the new sort order.
    * @throws IllegalArgumentException if thw sort order provided is not 
    *    known.
    */
   public void setSortOrder(int sortOrder)
   {
      if (sortOrder != SORT_ORDER_FIRST && sortOrder != SORT_ORDER_LAST &&
          sortOrder != SORT_ORDER_SORTED)
         throw new IllegalArgumentException("unknown sort order attribute");
      
      m_sortOrder = sortOrder;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSNullEntry, not <code>null</code>.
    * @throws IllegalArgumentException if c is <code>null</code>.
    */
   public void copyFrom(PSNullEntry c)
   {
      super.copyFrom(c);
      
      setIncludeWhen(c.getIncludeWhen());
      setSortOrder(c.getSortOrder());
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
      if (!(o instanceof PSNullEntry))
         return false;
      
      PSNullEntry t = (PSNullEntry) o;
      
      final boolean equal;
      if (getIncludeWhen() != t.getIncludeWhen())
         equal = false;
      else if (getSortOrder() != t.getSortOrder())
         equal = false;
      else
         equal = super.equals(o);

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
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
      Element node = null;
      try 
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the sortOrder attribute
         data = tree.getElementData(SORT_ORDER_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<SORT_ORDER_ENUM.length; i++)
            {
               if (SORT_ORDER_ENUM[i].equalsIgnoreCase(data))
               {
                  m_sortOrder = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               { 
                  XML_NODE_NAME, 
                  SORT_ORDER_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // OPTIONAL: get the includeWhen attribute
         data = tree.getElementData(INCLUDE_WHEN_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<INCLUDE_WHEN_ENUM.length; i++)
            {
               if (INCLUDE_WHEN_ENUM[i].equalsIgnoreCase(data))
               {
                  m_includeWhen = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               { 
                  XML_NODE_NAME, 
                  INCLUDE_WHEN_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // restore the PSEntry
         node = tree.getNextElement(PSEntry.XML_NODE_NAME, firstFlags);
         super.fromXml(node, parentDoc, parentComponents);
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
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(SORT_ORDER_ATTR, SORT_ORDER_ENUM[m_sortOrder]);
      root.setAttribute(INCLUDE_WHEN_ATTR, INCLUDE_WHEN_ENUM[m_includeWhen]);

      // store the base class info
      root.appendChild(super.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context) 
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_sortOrder != SORT_ORDER_FIRST && 
          m_sortOrder != SORT_ORDER_LAST && 
          m_sortOrder != SORT_ORDER_SORTED)
      {
         Object[] args = { SORT_ORDER_ENUM };
         context.validationError(this, 
            IPSObjectStoreErrors.UNSUPPORTED_SORT_ORDER, args);
      }
      
      if (m_includeWhen != INCLUDE_WHEN_ALWAYS &&
          m_includeWhen != INCLUDE_WHEN_ONLY_IF_NULL)
      {
         Object[] args = { INCLUDE_WHEN_ENUM };
         context.validationError(this, 
            IPSObjectStoreErrors.UNSUPPORTED_INCLUDE_WHEN, args);
      }
      
      super.validate(context);
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXNullEntry";
   
   /**  Always includeWhen specifier. */
   public static final int INCLUDE_WHEN_ALWAYS = 0;

   /** Only if null includeWhen specifier. */
   public static final int INCLUDE_WHEN_ONLY_IF_NULL = 1;

   /**
    * An array of XML attribute values for the includeWhen. They are
    * specified at the index of the specifier.
    */
   private static final String[] INCLUDE_WHEN_ENUM =
   {
      "always", "onlyIfNull"
   };

   /** Ascending sort order specifier */
   public static final int SORT_ORDER_FIRST = 0;

   /** Descending sort order specifier */
   public static final int SORT_ORDER_LAST = 1;

   /** User sort order specifier */
   public static final int SORT_ORDER_SORTED = 2;

   /**
    * An array of XML attribute values for the sortOrder. They are
    * specified at the index of the specifier.
    */
   private static final String[] SORT_ORDER_ENUM =
   {
      "first", "last", "sorted"
   };

   /** The XML attribute describing when to include this null entry. */
   private int m_includeWhen = INCLUDE_WHEN_ONLY_IF_NULL;

   /** The XML attribute describing how this null entry should be sorted. */
   private int m_sortOrder = SORT_ORDER_FIRST;
   
   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String INCLUDE_WHEN_ATTR = "includeWhen";
   private static final String SORT_ORDER_ATTR = "sortOrder";
}

