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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.Validate.notNull;

/**
 * This is the base class for classes used to represent an object in a table
 * schema consisting of a distinct list of column names.
 */
public abstract class PSJdbcKey extends PSJdbcTableComponent
{
   /**
    * Basic constructor for this class.
    *
    * @param name The name of this key, may be <code>null</code>.
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    *    constants.
    * @param names An iterator over one or more column names as Strings to
    *    include in the key.  May not be <code>null</code>.  May not
    *    contain <code>null</code>, empty or duplicate names.
    * @param containerName The name of this key type to use in error messages.  May
    * not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if names is <code>null</code> or does not
    *    contain at least one element or if action is not valid  or
    *    nodeName is <code>null</code> or empty, or if name is required and
    *    name is required.
    * @throws PSJdbcTableFactoryException if names contains any <code>null
    * </code>, empty or duplicate column names, or if there are any other
    * errors.
    */
   protected PSJdbcKey(String name,  int action, Iterator<String> names,
      String containerName)
      throws PSJdbcTableFactoryException
   {
      super(name, action);
      if (names == null)
         throw new IllegalArgumentException("names may not be null");

      if (!names.hasNext())
         throw new IllegalArgumentException("names may not be empty");

      if (containerName == null || containerName.trim().length() == 0)
         throw new IllegalArgumentException(
            "containerName may not be null or empty");

      m_containerName = containerName;

      setColumnNames(names);
   }


   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the primarykey element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    * @param nodeName The expected name of the supplied element.  May not be
    *    <code>null</code> or empty.
    * @param containerName The name of this key type to use in error messages.  May
    *    not be <code>null</code> or empty.
    *
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code> or
    * nodeName is <code>null</code> or empty.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
    */
   protected PSJdbcKey(Element sourceNode, String nodeName,
      String containerName)
         throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (nodeName == null || nodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "nodeName may not be null or empty");

      if (containerName == null || containerName.trim().length() == 0)
         throw new IllegalArgumentException(
            "containerName may not be null or empty");

      m_containerName = containerName;

      fromXml(sourceNode, nodeName);
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    * @param nodeName The name of the root element for this object.  May not be
    * <code>null</code> or empty.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>  or if
    * nodeName is <code>null</code> or empty.
    */
   protected Element toXml(Document doc, String nodeName)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (nodeName == null || nodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "nodeName may not be null or empty");

      // create the root element
      Element   root = doc.createElement(nodeName);

      // first add base class state
      setComponentState(root);

      // now add each name
      for (String m_name : m_names)
         PSXmlDocumentBuilder.addElement(doc, root, NAME_EL,
                 m_name);

      return root;
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the primarykey
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    * @param nodeName The expected name of the supplied element.  May not be
    *    <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code> or
    * nodeName is <code>null</code> or empty.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty column names, or if there are any other errors.
    */
   protected void fromXml(Element sourceNode, String nodeName)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (nodeName == null || nodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "nodeName may not be null or empty");

      if (!sourceNode.getNodeName().equals(nodeName))
      {
         Object[] args = {nodeName, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      List<String> names = new ArrayList<>();

      // allow base class to set its members
      getComponentState(sourceNode);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      Element nameEl = walker.getNextElement(NAME_EL,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      // make sure we get at least one
      do
      {
         if (nameEl == null)
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_NULL, NAME_EL);

         String name = PSXmlTreeWalker.getElementData(nameEl);
         if (name.trim().length() == 0)
         {
            Object[] args = {nodeName, NAME_EL, name};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         names.add(name);

         nameEl = walker.getNextElement(NAME_EL,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      } while (nameEl != null);

      setColumnNames(names.iterator());
   }

   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcKey with
    * the same (case-sensitive) column names. Otherwise returns
    * <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      if(! (obj instanceof  PSJdbcKey))
         return false;

      return (compare(obj, 0) == IS_EXACT_MATCH);
   }

   /**
    * Compares this column to another object.
    * See  for values returned
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
      int match;
      // dummy do...while loop to avoid many return statements
      do
      {
         if (!(obj instanceof PSJdbcKey))
         {
            match = IS_CLASS_MISMATCH;
            break;
         }
         PSJdbcKey other = (PSJdbcKey)obj;
         match = super.compare(other, flags);
         if (match < IS_EXACT_MATCH)
         {
            break;
         }

         if (((flags & COMPARE_IGNORE_NAME) != COMPARE_IGNORE_NAME)
          && (this.m_containerName.equalsIgnoreCase(other.m_containerName)))
         {
            match = IS_CASE_INSENSITIVE_MATCH;
            if (this.m_containerName.equals(other.m_containerName))
            {
               match = IS_EXACT_MATCH;
            }
         }

         int compareColumns = compareColumns(m_names, other.m_names);
         if (!((match == IS_CASE_INSENSITIVE_MATCH) &&
            (compareColumns == IS_EXACT_MATCH)))
         {
            match = compareColumns;
         }
      }
      while (false);
      return match;
   }

   /**
    * Compares the columns names in the specified lists.
    * The default implementation does not consider
    * the order of the names in the lists,
    * only that they contain the same number of items and have the same names.
    *
    * @param cols1 list containing column names.
    * Not <code>null</code>, may be empty.
    *
    * @param cols2 list containing column names.
    * Not <code>null</code>, may be empty.
    *
    * @return
    * <table>
    * <tr>
    * <td> IS_GENERIC_MISMATCH </td>
    * <td> the columns in the specified lists do not match </td>
    * </tr>
    * <tr>
    * <td> IS_CASE_INSENSITIVE_MATCH </td>
    * <td> the columns in the specified lists match if compared in
    * case-insensitive manner </td>
    * </tr>
    * <tr>
    * <td> IS_EXACT_MATCH </td>
    * <td> the columns in the specified lists exactly match </td>
    * </tr>
    * </table>
    */
   protected int compareColumns(List<String> cols1, List<String> cols2)
   {
      notNull(cols1);
      notNull(cols2);
      int match = IS_GENERIC_MISMATCH;
      // dummy do...while loop to avoid many return statements
      do
      {
         if (cols1.size() != cols2.size())
         {
            break;
         }
         // start with an exact match, if anything does not match then
         // change the match type
         match = IS_EXACT_MATCH;

         // start comparing column names
         //We need to match the columns in same order as some Indexes and keys are having same columns but different order of indexing
         //e.g. PSX_ACLENTRIES has 2 indexes
         //       <index name="IX_ENTRY_NAME_TYPE" isUnique="n">
         //                <name>NAME</name>
         //                <name>TYPE</name>
         //            </index>
         //            <index name="IX_ENTRY_TYPE_NAME" isUnique="n">
         //                <name>TYPE</name>
         //                <name>NAME</name>
         //            </index>
        Iterator<String> col1it = cols1.iterator();
        Iterator<String> col2it = cols2.iterator();
         while (col1it.hasNext())
         {
            String col1 = col1it.next();
            String col2 = col2it.next();
            if (!col1.equals(col2))
            {
               // check in the upper-cased list
               if (col1.equalsIgnoreCase(col2))
               {
                  // change the type of match
                  match = IS_CASE_INSENSITIVE_MATCH;
               }
               else
               {
                  // this column does not exist, exit the loop...no more
                  // processing is required
                  match = IS_GENERIC_MISMATCH;
                  break;
               }
            }
         }
      }
      while (false);
      return match;
   }

   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      hash += super.hashCode();
      hash += m_containerName.hashCode();
      hash += m_names.hashCode();
      return hash;
   }


   /**
    * Returns the list of key columns as an Iterator over one or more
    * columns names as Strings.
    *
    * @return The names iterator, never <code>null</code>, will always contain
    * at least one non-<code>null</code> entry.
    */
   public Iterator<String> getColumnNames()
   {
      return m_names.iterator();
   }

   /**
    * Set a list of column names that comprise this key.  Existing list
    * of columns is replaced.
    *
    * @param columnNames An Iterator over one or more column names as non-empty
    * Strings.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if columnNames is <code>null</code>, does
    * not contain at least one entry, or if it contains any entries that are not
    * non-<code>null</code> Strings.
    * @throws PSJdbcTableFactoryException if columnNames contains any empty or
    * duplicate names (case sensitive).
    */
   public void setColumnNames(Iterator<String> columnNames)
      throws PSJdbcTableFactoryException
   {
      if (columnNames == null || !columnNames.hasNext())
         throw new IllegalArgumentException(
            "columnNames may not be null or empty");

      List<String> newCols = new ArrayList<>();

      while (columnNames.hasNext())
      {
         String colName = columnNames.next();
         if (colName == null || colName.trim().length() == 0)
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.INVALID_COLUMN_NAME, m_containerName);

         if (newCols.contains(colName))
         {
            Object[] args = {m_containerName, colName};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.DUPLICATE_COLUMN, args);
         }
         newCols.add(colName);
      }

      m_names = newCols;
   }

   /**
    * name of this container for error messages, not <code>null</code>, empty,
    * or modified after construction.
    */
   private String m_containerName;

   /**
    * List of column names as Strings comprising this primary key.  Never
    * <code>null</code> or empty after ctor, never contains <code>null</code>,
    * empty or duplicate values.
    */
   private List<String> m_names = null;

   // xml constants
   private static final String NAME_EL = "name";
}

