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

import java.util.List;
import java.util.Objects;

/**
 * The PSDataSynchronizer class defines what data is being updated
 * through a particular update pipe (PSUpdatePipe). The columns used to
 * locate matching records as well as the columns which may be updated
 * must be defined.
 *
 * @see PSUpdatePipe#getDataSynchronizer
 * @see PSUpdatePipe
 * @see PSUpdateColumn
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataSynchronizer extends PSComponent
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
   public PSDataSynchronizer(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct an empty data synchronizer object. Inserts, updates
    * and deletes are enabled by default.
    */
   public PSDataSynchronizer()
   {
      super();
      m_columns = new PSCollection(
         com.percussion.design.objectstore.PSUpdateColumn.class);
   }

   /**
    * Is inserting data allowed?
    *
    * @return      <code>true</code> if inserts are allowed,
    *             <code>false</code> otherwise
    */
   public boolean isInsertingAllowed()
   {
      return m_insert;
   }

   /**
    * Enable or disable allowing data to be inserted.
    *
    * @param enable   <code>true</code> to allow inserting,
    *                <code>false</code> to disable it
    */
   public void setInsertingAllowed(boolean enable)
   {
      m_insert = enable;
   }

   /**
    * Is updating data allowed?
    *
    * @return      <code>true</code> if updates are allowed,
    *             <code>false</code> otherwise
    */
   public boolean isUpdatingAllowed()
   {
      return m_update;
   }

   /**
    * Enable or disable allowing data to be updated.
    *
    * @param enable   <code>true</code> to allow updating,
    *                <code>false</code> to disable it
    */
   public void setUpdatingAllowed(boolean enable)
   {
      m_update = enable;
   }

   /**
    * Is deleting data allowed?
    *
    * @return      <code>true</code> if deletes are allowed,
    *             <code>false</code> otherwise
    */
   public boolean isDeletingAllowed()
   {
      return m_delete;
   }

   /**
    * Enable or disable allowing data to be deleted.
    *
    * @param enable   <code>true</code> to allow deleting,
    *                <code>false</code> to disable it
    */
   public void setDeletingAllowed(boolean enable)
   {
      m_delete = enable;
   }

   /**
    * Get the columns associated with this synchronizer. The collection
    * includes columns used to locate matching records as well as
    * columns which are editable.
    *
    * @return         a collection containing the update columns
    *                (PSUpdateColumn objects) (may be null)
    */
   public com.percussion.util.PSCollection getUpdateColumns()
   {
      return m_columns;
   }

   /**
    * Overwrite the update columns associated with this synchronizer with the
    * specified collection. If you only want to modify certain columns,
    * add a new column, etc. use getUpdateColumns to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSDataSynchronizer object. Any subsequent changes made to the
    * object by the caller will also effect the synchronizer.
    *
    * @param       cols      the new update columns
    *
    * @see               #getUpdateColumns
    * @see               PSUpdateColumn
    */
   public void setUpdateColumns(com.percussion.util.PSCollection cols)
   {
      IllegalArgumentException ex = validateUpdateColumns(cols);
      if (ex != null)
         throw ex;

      m_columns = cols;
   }

   private static IllegalArgumentException validateUpdateColumns(
      PSCollection cols)
   {
      if (cols != null) {
         if (!com.percussion.design.objectstore.PSUpdateColumn.class.isAssignableFrom(
            cols.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type, Update Column: " +
               cols.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param  sync a valid PSDataSynchronizer. 
    */
   public void copyFrom( PSDataSynchronizer sync )
   {
      copyFrom((PSComponent) sync );
      // assume object is valid
      m_insert = sync.isInsertingAllowed();
      m_update = sync.isUpdatingAllowed();
      m_delete = sync.isDeletingAllowed();
      m_columns = sync.getUpdateColumns();
   }



   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDataSynchronizer XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXDataSynchronizer defines what data is being updated through
    *       a particular update pipe (PSUpdatePipe). The columns used to
    *       locate matching records as well as the columns which may be
    *       updated must be defined.
    *
    *       Object References:
    *
    *       PSXUpdateColumn - the columns associated with this synchronizer.
    *       The collection includes columns used to locate matching records
    *       as well as columns which are editable.
    *    --&gt;
    *    &lt;!ELEMENT PSXDataSynchronizer (PSXUpdateColumn*)&gt;
    *
    *    &lt;!ATTLIST PSXDataSynchronizer
    *       allowInserts    %PSXIsEnabled  #OPTIONAL
    *       allowUpdates    %PSXIsEnabled  #OPTIONAL
    *       allowDeletes    %PSXIsEnabled  #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXDataSynchronizer XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement("PSXDataSynchronizer");
      root.setAttribute("id", String.valueOf(m_id));

      //private      boolean         m_insert = false;
      root.setAttribute("allowInserts", m_insert ? "yes" : "no");
      //private      boolean         m_update = false;
      root.setAttribute("allowUpdates", m_update ? "yes" : "no");
      //private      boolean         m_delete = false;
      root.setAttribute("allowDeletes", m_delete ? "yes" : "no");

      //private      PSCollection    m_columns = null;
      PSUpdateColumn      column;
       if (m_columns != null){
         int size = m_columns.size();
         for (int i=0; i < size; i++){
            column = (PSUpdateColumn)m_columns.get(i);
            root.appendChild(column.toXml(doc));
         }
       }

      return root;
   }

   /**
    * This method is called to populate a PSDataSynchronizer Java object
    * from a PSXDataSynchronizer XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDataSynchronizer
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

         //private      boolean         m_insert = false;
         sTemp = tree.getElementData("allowInserts");
         m_insert = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         //private      boolean         m_update = false;
         sTemp = tree.getElementData("allowUpdates");
         m_update = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         //private      boolean         m_delete = false;
         sTemp = tree.getElementData("allowDeletes");
         m_delete = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         // get the update columns
         m_columns.clear();

         String curNodeType = PSUpdateColumn.ms_NodeType;
         if (tree.getNextElement(curNodeType, firstFlags) != null)
         {
            PSUpdateColumn    column = null;
            do
            {
               column = new PSUpdateColumn(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_columns.add(column);
            } while (tree.getNextElement(curNodeType, nextFlags) != null);
         }
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

      IllegalArgumentException ex = validateUpdateColumns(m_columns);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      int colSize = (m_columns == null) ? 0 : m_columns.size();

      if (colSize == 0 && (m_insert || m_update))
      {
         String dsName = "unknown";
         java.util.List parentList = cxt.getParentList();

         // go up the parent list looking for the data set name
         for (int i = parentList.size() - 1; i >= 0; i--)
         {
            Object pnt = parentList.get(i);
            if (pnt instanceof PSDataSet)
            {
               dsName = ((PSDataSet)pnt).getName();
               break;
            }
         }

         // we require update pipes if insert or update is enabled
         cxt.validationError(
            this, IPSObjectStoreErrors.SYNC_NO_UPDATE_COLUMNS, dsName);
      }

      // validate children
      cxt.pushParent(this);

      try
      {
         for (int i = 0; i < colSize; i++)
         {
            Object o = m_columns.get(i);
            PSUpdateColumn col = (PSUpdateColumn)o;
            col.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDataSynchronizer)) return false;
      if (!super.equals(o)) return false;
      PSDataSynchronizer that = (PSDataSynchronizer) o;
      return m_insert == that.m_insert &&
              m_update == that.m_update &&
              m_delete == that.m_delete &&
              Objects.equals(m_columns, that.m_columns);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_insert, m_update, m_delete, m_columns);
   }

   private      boolean         m_insert = false;
   private      boolean         m_update = false;
   private      boolean         m_delete = false;
   private      PSCollection    m_columns = null;      //PSUpdateColumn objects

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXDataSynchronizer";
}

