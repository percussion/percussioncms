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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * The PSSortedColumn class is used to define columns which define the sort
 * order for a query statement.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSortedColumn extends PSBackEndColumn
{
   /**
    * Construct a PSSortedColumn object from its XML representation. See the
    * {@link #toXml} method for a description of the XML object.
    *
    * @param sourceNode the XML element node to construct this object from;
    * not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object; may
    * be <code>null</code> as this parameter is not currently used.
    * @param parentComponents the parent objects of this object; may be
    * <code>null</code> to treat this object as the root object.
    *
    * @throws PSUnknownNodeTypeException if the XML element node is <code>null
    * </code> or not of the appropriate type.
    */
   public PSSortedColumn(Element sourceNode, IPSDocument parentDoc,
                         List parentComponents)
      throws PSUnknownNodeTypeException
   {
      /*
         Use super's empty constructor, rather than its Element constructor,
         so that this classes field initialization happens before fromXml.

         Good:
         1. Construct super class from empty constructor
         2. Initialize member variables
         3. Execute fromXml method (which executes super.fromXml)

         Bad:
         1. Construct super class from Element constructor, which executes
            this class' fromXml method (which executes super.fromXml)
         2. Initialize member variables (which forces m_isAscending=true no
            matter what the setting in XML)
      */
      fromXml( sourceNode, parentDoc, parentComponents );
   }


   /**
    * Construct a sorted column object. The column can be sorted
    * ascending or descending.
    *
    * @param column      the back-end column
    *
    * @param sortAsc     <code>true</code> to sort the data in ascending
    *                   order; <code>false</code> for descending order
    */
   public PSSortedColumn(PSBackEndColumn column, boolean sortAsc)
   {
      super( column );
      setAscending(sortAsc);
   }

   /**
    * Is the column sorted in ascending order?
    *
    * @return      <code>true</code> for ascending order;
    *             <code>false</code> for descending order
    */
   public boolean isAscending()
   {
      return m_isAscending;
   }

   /**
    * Set the columns sort order to ascending or descending order.
    *
    * @param sortAsc     <code>true</code> to sort the data in ascending
    *                   order; <code>false</code> for descending order
    */
   public void setAscending(boolean sortAsc)
   {
      m_isAscending = sortAsc;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXSortedColumn XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXSortedColumn is used to define columns which define the sort
    *     order for a query statement.
    *
    *       Object References:
    *
    *       PSXBackEndColumn - the back-end column.
    *    --&gt;
    *    &lt;!ELEMENT PSXSortedColumn   (PSXBackEndColumn)&gt;
    *
    *    &lt;!--
    *       isAscending - is this column sorted in ascending or descending
    *        order?
    *    --&gt;
    *    &lt;!ATTLIST PSXSortedColumn
    *       isAscending   %PSXIsEnabled  #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXSortedColumn XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private         boolean            m_isAscending = true;
      root.setAttribute("isAscending", m_isAscending ? "yes" : "no");

      // store the back-end column info (it's our superclass)
      root.appendChild(super.toXml(doc));

      return root;
   }

   /**
    * This method is called to populate a PSSortedColumn Java object
    * from a PSXSortedColumn XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXSortedColumn
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         if (!ms_NodeType.equals(sourceNode.getNodeName()))
         {
            Object[] args = { ms_NodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         //private         boolean            m_isAscending = true;
         sTemp = tree.getElementData("isAscending");
         /* this is the default */
         m_isAscending = (sTemp == null) || !sTemp.equalsIgnoreCase("no");

         // get the back-end column info (it's our superclass)
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         if (tree.getNextElement(PSBackEndColumn.ms_NodeType, firstFlags) == null) {
            Object[] args = { ms_NodeType, PSBackEndColumn.ms_NodeType, "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         super.fromXml(
            (Element)tree.getCurrent(), parentDoc, parentComponents);
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      cxt.pushParent(this);

      try {
         super.validate(cxt);   // validate the column portion of this object
      } finally {
         cxt.popParent();
      }
   }


   /**
    * Tests this object for equality with another object of the
    * same type.
    *
    * @param   o   The other PSSortedColumn object
    *
    * @return boolean true if the objects are equal, false otherwise
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSSortedColumn))
         return false;

      PSSortedColumn other = (PSSortedColumn)o;

      // check the back-end column info from our super class
      if (!super.equals(o))
         return false;

      return (m_isAscending == other.m_isAscending);
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
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param col a valid PSSortedColumn. If null, a IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( PSSortedColumn col )
   {
      super.copyFrom( col );
      // assume object is valid
      m_isAscending = col.m_isAscending;
   }


   private         boolean            m_isAscending = true;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXSortedColumn";
}

