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

package com.percussion.tablefactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to represent both a unique index and a unique constraint
 * in a table schema, and the action to perform when that table schema is used
 * to create or modify a table.  Unique indexes and unique constriants are
 * considered to be interchangeable, and in most dbms systems, unique
 * constraints are implemented using unique indexes.  When
 * a table is cataloged, all unique index columns are queried to construct this
 * object.  If this object is being created in the database as a result of
 * processing a table schema,  it is created as a unique constraint.
 */
public class PSJdbcIndex extends PSJdbcKey
{
   /**
    * Convenience ctor that calls the four arg ctor
    * {@link #PSJdbcIndex(String, Iterator, int, int)}
    * with <code>type</code> set to <code>PSJdbcIndex.TYPE_UNIQUE</code>
    */
   public PSJdbcIndex(String name, Iterator colNames,  int action)
      throws PSJdbcTableFactoryException
   {
      this(name, colNames, action, TYPE_UNIQUE);
   }

   /**
    * Basic constructor for this class.
    *
    * @param name The name of the index. May not be <code>null</code> or empty.
    * @param colNames An iterator over one or more column names to include
    *    in the index.  May not be <code>null</code>.  May not contain
    *    <code>null</code>, empty or duplicate names.
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    *    constants.
    *
    * @param type indicates the type of index, should be one of
    * <code>TYPE_XXX</code> value.
    *
    * @throws IllegalArgumentException if <code>name</code> or
    * <code>colNames</code> is <code>null</code> or <code>colNames</code> does
    * not contain at least one element or if <code>action</code> or
    * <code>type</code> is not valid
    *
    * @throws PSJdbcTableFactoryException if <code>colNames</code> contains
    * any <code>null</code>, empty or duplicate column names, or if there are
    * any other errors.
    */
   public PSJdbcIndex(String name, Iterator colNames, int action, int type)
      throws PSJdbcTableFactoryException
   {
      super(name, action, colNames, CONTAINER_NAME);
      setType(type);
   }

   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the index element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
    */
   public PSJdbcIndex(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      super(sourceNode, NODE_NAME, CONTAINER_NAME);
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the index
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      super.fromXml(sourceNode, NODE_NAME);
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = super.toXml(doc, NODE_NAME);
      root.setAttribute(ATTR_IS_UNIQUE,
         m_type == TYPE_UNIQUE ? XML_TRUE : XML_FALSE);
      return root;
   }

   /**
    * See {@link PSJdbcTableComponent#getComponentState(Element)} for details.
    */
   protected void getComponentState(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      super.getComponentState(sourceNode);

      // get the "isUnique" attribute value - defaults to true if not defined
      // for backwards compatibility
      m_type = TYPE_UNIQUE;
      String sTemp = sourceNode.getAttribute(ATTR_IS_UNIQUE);
      if ((sTemp != null) && (sTemp.trim().length() > 0))
         m_type = sTemp.trim().equalsIgnoreCase(XML_FALSE) ?
            TYPE_NON_UNIQUE : TYPE_UNIQUE;
   }

   /**
    * Determine whether this index is of the specified type
    *
    * @param type should be one of <code>TYPE_XXX</code> or multiple
    * <code>TYPE_XXX</code> values OR'ed together.
    *
    * @return <code>true</code> if this index is of the specified type,
    * "<code>false</code> otherwise.
    */
   public boolean isOfType(int type)
   {
      if (m_type != TYPE_NON_UNIQUE)
         m_type = TYPE_UNIQUE;
      return ((m_type & type) == m_type);
   }

   /**
    * Returns the type of index - unique or non-unique.
    *
    * @return one of <code>TYPE_XXX</code> values.
    */
   public int getType()
   {
      return ((m_type == TYPE_NON_UNIQUE) ? TYPE_NON_UNIQUE : TYPE_UNIQUE);
   }

   /**
    * Sets the type of index - unique or non-unique.
    *
    * @param type indicates the type of index, should be one of
    * <code>TYPE_XXX</code> value.
    *
    * @throws IllegalArgumentException if <code>type</code> is invalid
    */
   public void setType(int type)
   {
      if (!((type == TYPE_UNIQUE) || (type == TYPE_NON_UNIQUE)))
          throw new IllegalArgumentException("Invalid index type: " + type);
      m_type = type;
   }

   /**
    * See {@link PSJdbcTableComponent#canAlter()} for details.
    */
   public boolean canAlter()
   {
      boolean canAlter = false;

      switch (getAction())
      {
         case ACTION_CREATE:
         case ACTION_NONE:
            canAlter = true;
            break;

         case ACTION_DELETE:
            if (getType() == TYPE_NON_UNIQUE)
               canAlter = true;
            break;
      }

      return canAlter;
   }

   /**
    * Compares this index to another object.
    * See {@link PSJdbcTableComponent#compare(Object)} for values returned
    * by this method.
    *
    * @param obj the object to compare, may be <code>null</code>
    * @param flags one or more <code>COMPARE_XXX</code> values OR'ed
    * together
    *
    * @return a code indicating the type of match/mismatch between this object
    * and the specified object <code>obj</code>
    */
   public int compare(Object obj, int flags)
   {
      int match = IS_GENERIC_MISMATCH;
      // dummy do...while loop to avoid many return statements
      do
      {
         if (!(obj instanceof PSJdbcIndex))
         {
            match = IS_CLASS_MISMATCH;
            break;
         }
         PSJdbcIndex other = (PSJdbcIndex)obj;
         match = super.compare(other, flags);
         if (match < IS_EXACT_MATCH)
         {
            break;
         }
         if (m_type != other.m_type)
         {
            match = IS_TYPE_MISMATCH;
            break;
         }
      }
      while (false);
      return match;
   }

   /**
    * Takes into account the order of the columns.
    * {@inheritDoc}
    */
   @Override
   protected int compareColumns(List cols1, List cols2)
   {
      return super.compareColumns(numberColumns(cols1), numberColumns(cols2));
   }
   
   /**
    * Returns the list of the column names, which is exactly the same as
    * provided list, but with the string "X " prepended to each column name,
    * where "X" is the index of the column in the list.
    * Is used for comparison of two lists of columns, to make sure that
    * columns with the same name, but on different positions are treated
    * as different.
    * @param columns the columns to process. Assumed not <code>null</code>.
    * @return the new list.
    */
   private List<String> numberColumns(List<String> columns)
   {
      final List<String> numbered = new ArrayList<String>();
      for (int i = 0; i < columns.size(); i++)
      {
         numbered.add(i + " " + columns.get(i));
      }
      return numbered;
   }
   
   /**
    * Gets a list of all column names of the index object.
    * 
    * @return The column names of an Index. Cannot be <code>null</code> or empty.
    */

   List<String> getIndexColumnNames()
   {
      Iterator<?> columnNameIterator = getColumnNames();
      ArrayList<String> columnNameList = new ArrayList<String>();
      while (columnNameIterator.hasNext())
      {
         String columnName = (String) columnNameIterator.next();
         columnNameList.add(columnName);
      }
      return columnNameList;
   }

   /**
    * Returns the hash code for this index. Adds the type of index to the
    * hashcode obtained from the base class.
    *
    * @return the hash code for this index
    */
   public int hashCode()
   {
      return super.hashCode() + m_type;
   }

   /**
    * Constant to indicate a unique index.
    */
   public static final int TYPE_UNIQUE = 1;

   /**
    * Constant to indicate a non-unique index.
    */
   public static final int TYPE_NON_UNIQUE = 2;

   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "index";

   /**
    * name of this container for error messages.
    */
   public static String CONTAINER_NAME = "index";

   /**
    * Constant for "isUnique" attribute of the root element of the index.
    */
   private static final String ATTR_IS_UNIQUE = "isUnique";

   // Constants for XML true and false
   private static final String XML_TRUE = "y";
   private static final String XML_FALSE = "n";

   /**
    * The type of index - unique or not. Defaults to
    * <code>TYPE_UNIQUE</code>, modified using the <code>setType()</code>
    * method.
    */
   private int m_type;

   /**
    * Constants returned by <code>compare()</code> method. Implies that the
    * type of index is different. For example, one may be unique and other
    * non-unique.
    */
   public static final int IS_TYPE_MISMATCH = -3;

}

