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

import java.util.Objects;


/**
 * The PSUpdateColumn class is used to define columns which will be used
 * when updating the back-end data store. The column may be used as
 * either a key used to locate the back-end row or as an updateable
 * column. At this time, a key value cannot be updated.
 *
 * @see PSDataSynchronizer#getUpdateColumns
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUpdateColumn extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSUpdateColumn(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSUpdateColumn() {
      super();
   }


   // see interface for description
   public Object clone()
   {
      PSUpdateColumn copy = (PSUpdateColumn) super.clone();
      if (m_backEndColumn != null)
         copy.m_backEndColumn = (PSBackEndColumn) m_backEndColumn.clone();
      return copy;
   }


   /**
    * Construct an update column object. The column can be set as being
    * either a key used to locate the back-end row or as an updateable
    * column. At this time, a key value cannot be updated.
    *
    * @param column      the back-end column
    * @param isKey      <code>true</code> to set the column as a key,
    *                   <code>false</code> to set it as being updateable
    */
   public PSUpdateColumn(PSBackEndColumn column, boolean isKey)
   {
      super();
      setColumn(column);
      setKey(isKey);
   }

   /**
    * Get the back-end column.
    *
    * @return      the back-end column
    */
   public PSBackEndColumn getColumn()
   {
      return m_backEndColumn;
   }

   /**
    * Set the back-end column.
    *
    * @param col   the back-end column
    *
    * @see         PSBackEndColumn
    */
   public void setColumn(PSBackEndColumn col)
   {
      IllegalArgumentException ex = validateColumn(col);
      if (ex != null)
         throw ex;

      m_backEndColumn = col;
   }

   private static IllegalArgumentException validateColumn(PSBackEndColumn col)
   {
      if (col == null)
         return new IllegalArgumentException("update col is null");

      return null;
   }

   /**
    * Is this column used in the WHERE clause to locate matching rows?
    *
    * @return      <code>true</code> if the column is used as a key,
    *             <code>false</code> otherwise
    */
   public boolean isKey()
   {
      return m_key;
   }

   /**
    * Set this column as being used in the WHERE clause to locate
    * matching rows.
    */
   public void setKey(boolean key)
   {
      m_key = key;
   }

   /**
    * Is this column used in the SET clause to modify the column's value?
    *
    * @return      <code>true</code> if the column is updateable,
    *             <code>false</code> otherwise
    */
   public boolean isUpdateable()
   {
      return m_updatable;
   }

   /**
    * Set this column as being used in the SET clause to modify the
    * column's value.
    */
   public void setUpdateable(boolean updatable)
   {
      m_updatable = updatable;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXUpdateColumn XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXUpdateColumn is used to define columns which will be used when
    *       updating the back-end data store. The column may be used as
    *       either a key used to locate the back-end row or as an updateable
    *       column. At this time, a key value cannot be updated.
    *
    *       Object References:
    *
    *       PSXBackEndColumn - the back-end column.
    *    --&gt;
    *    &lt;!ELEMENT PSXUpdateColumn   (PSXBackEndColumn)&gt;
    *
    *    &lt;!--
    *       isKey - is this column being used in the WHERE clause to locate
    *       matching rows?
    *
    *       isEditable - is this column used in the SET clause to modify the
    *       column's value?
    *    --&gt;
    *    &lt;!ATTLIST PSXUpdateColumn
    *       isKey         %PSXIsEnabled  #OPTIONAL
    *       isEditable     %PSXIsEnabled  #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXUpdateColumn XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private         boolean            m_key = false;
      root.setAttribute("isKey", m_key ? "yes" : "no");

      //private         boolean            m_updatable = false;
      root.setAttribute("isEditable", m_updatable ? "yes" : "no");

      //private         PSBackEndColumn      m_backEndColumn = null;
      if(null != m_backEndColumn)
         root.appendChild(m_backEndColumn.toXml(doc));

      return root;
   }

   /**
    * This method is called to populate a PSUpdateColumn Java object
    * from a PSXUpdateColumn XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXUpdateColumn
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         if (false == ms_NodeType.equals (sourceNode.getNodeName()))
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

         //private         boolean            m_key = false;
         sTemp = tree.getElementData("isKey");
         m_key = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         //private         boolean            m_updatable = false;
         sTemp = tree.getElementData("isEditable");
         m_updatable = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         //private         PSBackEndColumn      m_backEndColumn = null;
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         if (tree.getNextElement(PSBackEndColumn.ms_NodeType, firstFlags) == null) {
            Object[] args = { ms_NodeType, PSBackEndColumn.ms_NodeType, "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         m_backEndColumn = new PSBackEndColumn(
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

      IllegalArgumentException ex = validateColumn(m_backEndColumn);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_backEndColumn != null)
            m_backEndColumn.validate(cxt);
      }
      finally
      {
         cxt.popParent();
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSUpdateColumn)) return false;
      if (!super.equals(o)) return false;
      PSUpdateColumn that = (PSUpdateColumn) o;
      return m_key == that.m_key &&
              m_updatable == that.m_updatable &&
              Objects.equals(m_backEndColumn, that.m_backEndColumn);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_key, m_updatable, m_backEndColumn);
   }

   private         boolean            m_key = false;
   private         boolean            m_updatable = false;

   /** may be <code>null</code> */
   private         PSBackEndColumn   m_backEndColumn = null;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXUpdateColumn";
}

