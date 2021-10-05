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
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Objects;


/**
 * The PSBackEndJoin class is used to define the relationships between
 * back-end tables. When more than one table is defined in a data tank,
 * E2 needs to know how the information will be joined across the tables
 * when querying data from the back-ends.
 * <p>
 * Joins are defined by specifying the column to be used on the left side
 * of the join and the column to be used on the right side of the join. The
 * relationship between these two columns can then be specified. This
 * can be one of the following:
 * <table border="1">
 * <tr><td>Type</td><td>Behavior</td></tr>
 * <tr><td>Inner</td>
 *     <td>only matching rows are returned</td>
 * </tr>
 * <tr><td>Full Outer</td>
 *     <td>all rows from both tables are returned. If a row in the left
 *         table does not have a match in the right table, NULL values are
 *         used as the values of the right table's columns. If a row in the
 *         right table does not have a match in the left table, NULL values
 *         are used as the values of the left table's columns.</td>
 * </tr>
 * <tr><td>Left Outer</td>
 *     <td>all rows from the left table are returned. If a row in the left
 *         table does not have a match in the right table, NULL values are
 *         used as the values of the right table's columns.</td>
 * </tr>
 * <tr><td>Right Outer</td>
 *     <td>all rows from the right table are returned. If a row in the right
 *         table does not have a match in the left table, NULL values are
 *         used as the values of the left table's columns.</td>
 * </tr>
 * </table>
 *
 * @see PSBackEndDataTank
 * @see PSBackEndDataTank#getJoins
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndJoin extends PSComponent
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
   public PSBackEndJoin(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a back-end join object. The object defines the relationship
    * between two tables. An inner join is created by default.
    *
    * @param leftColumn    the column object defining the left side of the
    *                      join
    * @param rightColumn   the column object defining the right side of the
    *                      join
    */
   public PSBackEndJoin( PSBackEndColumn leftColumn,
      PSBackEndColumn rightColumn)
   {
      super();
      m_leftColumn = leftColumn;
      m_rightColumn = rightColumn;
   }

   /**
    * Default constructor for dynamic construction of the object. Used in fromXml()
    */
   PSBackEndJoin()
   {
      super();
   }


   // see interface for description
   public Object clone()
   {
      PSBackEndJoin copy = (PSBackEndJoin) super.clone();
      if (m_leftColumn != null)
         copy.m_leftColumn = (PSBackEndColumn) m_leftColumn.clone();
      if (m_rightColumn != null)
         copy.m_rightColumn = (PSBackEndColumn) m_rightColumn.clone();
      if (m_translator != null)
         copy.m_translator = (PSExtensionCall) m_translator.clone();
      return copy;
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSBackEndJoin)) return false;
      if (!super.equals(o)) return false;
      PSBackEndJoin that = (PSBackEndJoin) o;
      return m_joinType == that.m_joinType &&
              Objects.equals(m_leftColumn, that.m_leftColumn) &&
              Objects.equals(m_rightColumn, that.m_rightColumn) &&
              Objects.equals(m_translator, that.m_translator);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_leftColumn, m_rightColumn, m_joinType, m_translator);
   }

   /**
    * Is this an inner join? Inner joins only return rows where the value
    * in the left column match the value in the right column.
    *
    * @return      <code>true</code> if this is an inner join,
    *             <code>false</code> otherwise
    */
   public boolean isInnerJoin()
   {
      return (BEJ_TYPE_INNER == m_joinType);
   }

   /**
    * Set this as an inner join. Inner joins only return rows where the value
    * in the left column match the value in the right column.
    */
   public void setInnerJoin()
   {
      m_joinType = BEJ_TYPE_INNER;
   }

   /**
    * Is this a full outer join? Full outer joins return all rows from
    * both tables. If a row in the left table does not have a match in the
    * right table, NULL values are used as the values of the right table's
    * columns. If a row in the right table does not have a match in the left
    * table, NULL values are used as the values of the left table's columns.
    *
    * @return      <code>true</code> if this is a full outer join,
    *             <code>false</code> otherwise
    */
   public boolean isFullOuterJoin()
   {
      return (BEJ_TYPE_FULL_OUTER == m_joinType);
   }

   /**
    * Set this as a full outer join? Full outer joins return all rows from
    * both tables. If a row in the left table does not have a match in the
    * right table, NULL values are used as the values of the right table's
    * columns. If a row in the right table does not have a match in the left
    * table, NULL values are used as the values of the left table's columns.
    */
   public void setFullOuterJoin()
   {
      m_joinType = BEJ_TYPE_FULL_OUTER;
   }

   /**
    * Is this a left outer join? Left outer joins return all rows from
    * the left table. If a row in the left table does not have a match
    * in the right table, NULL values are used as the values of the
    * right table's columns.
    *
    * @return      <code>true</code> if this is a left outer join,
    *             <code>false</code> otherwise
    */
   public boolean isLeftOuterJoin()
   {
      return (BEJ_TYPE_LEFT_OUTER == m_joinType);
   }

   /**
    * Set this as a left outer join? Left outer joins return all rows from
    * the left table. If a row in the left table does not have a match
    * in the right table, NULL values are used as the values of the
    * right table's columns.
    */
   public void setLeftOuterJoin()
   {
      m_joinType = BEJ_TYPE_LEFT_OUTER;
   }

   /**
    * Is this a right outer join? Right outer joins return all rows from
    * the right table. If a row in the right table does not have a match
    * in the left table, NULL values are used as the values of the
    * left table's columns.
    *
    * @return      <code>true</code> if this is a right outer join,
    *             <code>false</code> otherwise
    */
   public boolean isRightOuterJoin()
   {
      return (BEJ_TYPE_RIGHT_OUTER == m_joinType);
   }

   /**
    * Set this as a right outer join? Right outer joins return all rows from
    * the right table. If a row in the right table does not have a match
    * in the left table, NULL values are used as the values of the
    * left table's columns.
    */
   public void setRightOuterJoin()
   {
      m_joinType = BEJ_TYPE_RIGHT_OUTER;
   }

   /**
    * Get the column defining the left side of the join.
    *
    * @return      the column defining the left side of the join
    */
   public PSBackEndColumn getLeftColumn()
   {
      return m_leftColumn;
   }

   /**
    * Set the column defining the left side of the join.
    *
    * @param col   the column defining the left side of the join
    *
    * @see         PSBackEndColumn
    */
   public void setLeftColumn(PSBackEndColumn col)
   {
      if (col == null)
         throw new IllegalArgumentException("back-end join lcol null");

      m_leftColumn = col;
   }

   /**
    * Get the column defining the right side of the join.
    *
    * @return      the column defining the right side of the join
    */
   public PSBackEndColumn getRightColumn()
   {
      return m_rightColumn;
   }

   /**
    * Set the column defining the right side of the join.
    *
    * @param col   the column defining the right side of the join
    *
    * @see         PSBackEndColumn
    */
   public void setRightColumn(PSBackEndColumn col)
   {
      if (col == null)
         throw new IllegalArgumentException("back-end join rcol null");

      m_rightColumn = col;
   }

   /**
    * Get the translation which will be applied to the left side of the
    * join before attempting to locate a matching value for the right side
    * column.
    *
    * @return      the translator (may be null)
    */
   public PSExtensionCall getTranslator()
   {
      return m_translator;
   }

   /**
    * Set the translation which will be applied to the left side of the
    * join before attempting to locate a matching value for the right side
    * column.
    *
    * @param translator    the translator (may be null)
    *
    * @see                  PSExtensionCall
    */
   public void setTranslator(PSExtensionCall translator)
   {
      m_translator = translator;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXBackEndJoin XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXBackEndJoin is used to define the relationships between
    *       back-end tables. When more than one table is defined in a data
    *       tank, E2 needs to know how the information will be joined across
    *       the tables when querying data from the back-ends.
    *
    *       Joins are defined by specifying the column to be used on the left
    *       side of the join and the column to be used on the right side of
    *       the join. The relationship between these two columns can then be
    *       specified. This is defined in the type attribute.
    *    --&gt;
    *    &lt;!ELEMENT PSXBackEndJoin   (leftColumn, rightColumn, translator)&gt;
    *
    *    &lt;!--
    *       types of joins supported:
    *
    *       inner - only matching rows are returned
    *
    *       fullOuter - all rows from both tables are returned. If a row in
    *       the left table does not have a match in the right table, NULL
    *       values are used as the values of the right table's columns. If a
    *       row in the right table does not have a match in the left table,
    *       NULL values are used as the values of the left table's columns.
    *
    *       leftOuter - all rows from the left table are returned. If a row
    *       in the left table does not have a match in the right table, NULL
    *       values are used as the values of the right table's columns.
    *
    *       rightOuter - all rows from the right table are returned. If a
    *       row in the right table does not have a match in the left table,
    *       NULL values are used as the values of the left table's columns.
    *    --&gt;
    *    &lt;!ENTITY % PSXBackEndJoinType "(inner | fullOuter | leftOuter | rightOuter)"&gt;
    *    &lt;!ATTLIST PSXBackEndJoin
    *       type    %PSXBackEndJoinType   #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       the column defining the left side of the join.
    *    --&gt;
    *    &lt;!ELEMENT leftColumn       (PSXBackEndColumn)&gt;
    *
    *    &lt;!--
    *       the column defining the right side of the join.
    *    --&gt;
    *    &lt;!ELEMENT rightColumn      (PSXBackEndColumn)&gt;
    *
    *    &lt;!--
    *       the translation which will be applied to the left side of the
    *       join before attempting to locate a matching value for the right
    *       side column.
    *    --&gt;
    *    &lt;!ELEMENT translator       (PSXExtensionCall)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXBackEndJoin XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      if (m_joinType == BEJ_TYPE_FULL_OUTER)
         root.setAttribute("joinType", XML_FLAG_FULL_OUTER);
      else if (m_joinType == BEJ_TYPE_LEFT_OUTER)
         root.setAttribute("joinType", XML_FLAG_LEFT_OUTER);
      else if (m_joinType == BEJ_TYPE_RIGHT_OUTER)
         root.setAttribute("joinType", XML_FLAG_RIGHT_OUTER);
      else
         root.setAttribute("joinType", XML_FLAG_INNER);

      //since left column and right column are both PSBackEndColumn objects
      //add one more level to identify left and right columns

      //left column,        private             PSBackEndColumn         m_leftColumn = null;
      Element   parentNode = doc.createElement ("leftColumn");
      root.appendChild(parentNode);
      parentNode.appendChild (m_leftColumn.toXml(doc));

      //right column    private             PSBackEndColumn         m_rightColumn = null;
      parentNode = doc.createElement ("rightColumn");
      root.appendChild(parentNode);
      parentNode.appendChild (m_rightColumn.toXml(doc));

      //translator    private             PSExtensionCall               m_translator = null;
      if (m_translator != null) {
         parentNode = doc.createElement ("translator");
         root.appendChild(parentNode);
         parentNode.appendChild (m_translator.toXml(doc));
      }

      return root;
   }

   /**
    * Populates this object from its XML representation.  See {@link #toXml}
    * for the format.
    *
    * @param sourceNode   the XML element node to populate from, not <code>null
    * </code>.
    * @param parentDoc may be <code>null</code>
    * @param parentComponents all the parent objects of this object, may be
    * <code>null</code>
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         validateElementName( sourceNode, ms_NodeType );
         PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         sTemp = tree.getElementData("joinType");
         if (sTemp != null) {
            if (sTemp.equals(XML_FLAG_INNER))
               m_joinType = BEJ_TYPE_INNER;
            else if (sTemp.equals(XML_FLAG_FULL_OUTER))
               m_joinType = BEJ_TYPE_FULL_OUTER;
            else if (sTemp.equals(XML_FLAG_LEFT_OUTER))
               m_joinType = BEJ_TYPE_LEFT_OUTER;
            else if (sTemp.equals(XML_FLAG_RIGHT_OUTER))
               m_joinType = BEJ_TYPE_RIGHT_OUTER;
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         Node cur = tree.getCurrent();   // cur = <PSXBackEndJoin>

         //left column, private PSBackEndColumn m_leftColumn = null;
         if (tree.getNextElement("leftColumn", firstFlags) == null) {
            Object[] args = { ms_NodeType, "leftColumn", "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else {
            /* need a new tree rooted on the leftColumn so we don't jump to
            * the right column's data accidentally
            */
            PSXmlTreeWalker t2 = new PSXmlTreeWalker(tree.getCurrent());
            if (t2.getNextElement(PSBackEndColumn.ms_NodeType, firstFlags) == null) {
               Object[] args = { ms_NodeType + "/leftColumn",
                                 PSBackEndColumn.ms_NodeType, "null" };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }

            m_leftColumn = new PSBackEndColumn(
                  (Element)t2.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //right column private PSBackEndColumn m_rightColumn = null;
         if (tree.getNextElement("rightColumn", firstFlags) == null) {
            Object[] args = { ms_NodeType, "rightColumn", "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else {
            /* need a new tree rooted on the rightColumn so we don't jump to
            * the some other data accidentally
            */
            PSXmlTreeWalker t2 = new PSXmlTreeWalker(tree.getCurrent());
            if (t2.getNextElement(PSBackEndColumn.ms_NodeType, firstFlags) == null) {
               Object[] args = { ms_NodeType + "/rightColumn",
                                 PSBackEndColumn.ms_NodeType, "null" };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_rightColumn = new PSBackEndColumn(
                  (Element)t2.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //translator private PSExtensionCall m_translator = null;
         if (tree.getNextElement("translator", firstFlags) != null) {
            for ( Node child = tree.getCurrent().getFirstChild();
                  child != null; child = child.getNextSibling())
            {
               if (child instanceof Element) {
                  m_translator = new PSExtensionCall(
                     (Element)child, parentDoc, parentComponents);
                  break;
               }
            }
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

      switch (m_joinType)
      {
      case BEJ_TYPE_INNER:
         // fall through
      case BEJ_TYPE_FULL_OUTER:
         // fall through
      case BEJ_TYPE_LEFT_OUTER:
         // fall through
      case BEJ_TYPE_RIGHT_OUTER:
         // ok
         break;
      default:
         cxt.validationError(
            this,
            IPSObjectStoreErrors.BE_JOIN_UNKNOWN_TYPE, "" + m_joinType);
      }

      if (m_leftColumn == null)
         cxt.validationError(this, IPSObjectStoreErrors.BE_JOIN_LCOL_NULL, null);

         if (m_rightColumn == null)
            cxt.validationError(this, IPSObjectStoreErrors.BE_JOIN_RCOL_NULL, null);

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_leftColumn != null)
         {
            m_leftColumn.validate(cxt);
         }

         if (m_rightColumn != null)
         {
            m_rightColumn.validate(cxt);
         }

         if (m_translator != null)
            m_translator.validate(cxt);
      }
      finally
      {
         cxt.popParent();
      }
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param selector a valid PSDataSelector. 
    */
   public void copyFrom( PSBackEndJoin join )
   {
      copyFrom((PSComponent) join );

      m_leftColumn   = join.m_leftColumn;
      m_rightColumn  = join.m_rightColumn;
      m_joinType     = join.m_joinType;
      m_translator   = join.m_translator;
   }

   public static final int BEJ_TYPE_INNER = 1;
   public static final int BEJ_TYPE_FULL_OUTER = 2;
   public static final int BEJ_TYPE_LEFT_OUTER = 3;
   public static final int BEJ_TYPE_RIGHT_OUTER = 4;


   private   PSBackEndColumn         m_leftColumn   = null;
   private   PSBackEndColumn         m_rightColumn  = null;
   private   int                     m_joinType     = BEJ_TYPE_INNER;
   private   PSExtensionCall         m_translator   = null;

   private static final String      XML_FLAG_INNER         = "inner";
   private static final String      XML_FLAG_FULL_OUTER   = "fullOuter";
   private static final String      XML_FLAG_LEFT_OUTER   = "leftOuter";
   private static final String      XML_FLAG_RIGHT_OUTER   = "rightOuter";

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXBackEndJoin";
}
