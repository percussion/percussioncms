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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the PSXTableSet DTD in BasicObjects.dtd.
 */
public class PSTableSet extends PSComponent
{
   /**
    * Creates a new table set.
    *
    * @param tableLocation the table locator, not <code>null</code>.
    * @param tableRef a table reference, not <code>null</code>.
    */
   public PSTableSet(PSTableLocator tableLocation, PSTableRef tableRef)
   {
      setTableLocation(tableLocation);

      if (tableRef == null)
         throw new IllegalArgumentException("tableRef cannot be null");

      m_tableRefs.add(tableRef);
   }

   /**
    * Creates a new table set.
    *
    * @param tableLocator the table locator, not <code>null</code>.
    * @param tableRefs a collection of PSTableRef objects, not
    *    <code>null</code> or empty.
    */
   public PSTableSet(PSTableLocator tableLocator, PSCollection tableRefs)
   {
      setTableLocation(tableLocator);
      setTableRefs(tableRefs);
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
   public PSTableSet(Element sourceNode, IPSDocument parentDoc,
                     List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Copy constructor, creates a shallow copy.
    *
    * @param source the source to create a copy from, not <code>null</code>.
    */
   public PSTableSet(PSTableSet source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      copyFrom(source);
   }

   /**
    * Needed for serialization.
    */
   protected PSTableSet()
   {
   }

   /**
    * Get the current table location.
    *
    * @return the table locator, never <code>null</code>.
    */
   public PSTableLocator getTableLocation()
   {
      return m_tableLocation;
   }

   /**
    * Set a new table location.
    *
    * @param tableLocation the new table locator, not <code>null</code>.
    */
   public void setTableLocation(PSTableLocator tableLocation)
   {
      if (tableLocation == null)
         throw new IllegalArgumentException("tableLocation cannot be null");

      m_tableLocation = tableLocation;
   }

   /**
    * Get the table references.
    *
    * @return the table references, never <code>null</code> or empty.
    */
   public Iterator getTableRefs()
   {
      return m_tableRefs.iterator();
   }

   /**
    * Get the table definition reference.
    *
    * @return the table definition reference, might be <code>null</code> but
    *    not empty.
    */
   public String getTableDefRef()
   {
      return m_href;
   }

   /**
    * Set a new table definition reference.
    *
    * @param href the new table definition reference, may be <code>null</code>
    *    but not empty.
    */
   public void setTableDefRef(String href)
   {
      if (href != null && href.length() == 0)
         throw new IllegalArgumentException("the href cannot be empty");

      m_href = href;
   }

   /**
    * Set new table references.
    *
    * @param tableRefs the new table references, not <code>null</code>, not
    *    empty.
    */
   public void setTableRefs(PSCollection tableRefs)
   {
      if (tableRefs == null || tableRefs.isEmpty())
         throw new IllegalArgumentException("tableRefs cannot be null or empty");

      if (!tableRefs.getMemberClassName().equals(
          m_tableRefs.getMemberClassName()))
         throw new IllegalArgumentException(
            "PSTableRef collection expected");

      m_tableRefs.clear();
      m_tableRefs.addAll(tableRefs);
   }

   /**
    * Adds the table reference to the existing collection.
    *
    * @param tableRef the table reference, may not be <code>null</code>
    */
   public void addTableRef(PSTableRef tableRef)
   {
      if(tableRef == null)
         throw new IllegalArgumentException("tableRef cannot be null");

      m_tableRefs.add(tableRef);
   }

   /***
    * Remove the specified table ref.
    * @param tableRef
    */
   public void removeTableRef(PSTableRef tableRef){
      if(tableRef==null)
         throw new IllegalArgumentException("tableRef cannot be null");

      m_tableRefs.remove(tableRef);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component, except instead of copying the reference to the tableref
    * collection, the contents are copied to this object's collection. Derived
    * classes should implement this method for their data, calling the base
    * class method first.
    *
    * @param c a valid PSTableSet, not <code>null</code>.
    */
   public void copyFrom(PSTableSet c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_href = c.m_href;
      m_tableLocation = c.getTableLocation();
      m_tableRefs.clear();
      m_tableRefs.addAll( c.m_tableRefs );
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSTableSet)) return false;
        if (!super.equals(o)) return false;
        PSTableSet that = (PSTableSet) o;
        return Objects.equals(m_href, that.m_href) &&
                Objects.equals(m_tableLocation, that.m_tableLocation) &&
                Objects.equals(m_tableRefs, that.m_tableRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), m_href, m_tableLocation, m_tableRefs);
    }

    /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
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

         // OPTIONAL: get the href attribute
         data = tree.getElementData(HREF_ATTR);
         if (data != null && data.trim().length() > 0)
            m_href = data;

         // REQUIRED: get the table location
         node = tree.getNextElement(PSTableLocator.XML_NODE_NAME, firstFlags);
         if (node != null)
         {
            m_tableLocation = new PSTableLocator(
               node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSTableLocator.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // REQUIRED: get the table refs
         node = tree.getNextElement(PSTableRef.XML_NODE_NAME, nextFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSTableRef.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         while(node != null)
         {
            m_tableRefs.add(new PSTableRef(node, parentDoc, parentComponents));
            node = tree.getNextElement(PSTableRef.XML_NODE_NAME, nextFlags);
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
      if (m_href != null)
         root.setAttribute(HREF_ATTR, m_href);

      // REQUIRED: create the table location
      root.appendChild(m_tableLocation.toXml(doc));

      // REQUIRED: create the table references
      Iterator it = getTableRefs();
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

      // do children
      context.pushParent(this);
      try
      {
         if (m_tableLocation != null)
            m_tableLocation.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_TABLE_SET, null);

         Iterator it = getTableRefs();
         if (!it.hasNext())
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_TABLE_SET, null);
         while (it.hasNext())
            ((IPSComponent) it.next()).validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXTableSet";

   /**
    * Specifies the location of the document containing the table defs.
    * If create is yes and the table doesn't exist, it will be created
    * based on this def. If the table already exists, its metadata is
    * validated against the def.
    */
   private String m_href = null;

   /** The table location, never <code>null</code> after construction */
   private PSTableLocator m_tableLocation = null;

   /**
    * A collection of PSTableRef objects. Never <code>null</code> or
    * empty after construction.
    */
   private PSCollection m_tableRefs =
      new PSCollection((new PSTableRef()).getClass());

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String HREF_ATTR = "href";
}

