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

import com.percussion.error.PSException;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * The PSBackEndColumn class is used to define a mapping to a back-end
 * column.
 *
 * @see IPSBackEndMapping
 * @see PSDataMapping#getBackEndMapping
 *
 * @author       Tas Giakouminakis
 * @version  1.0
 * @since       1.0
 */

public class PSBackEndColumn
   extends PSComponent
   implements IPSBackEndMapping, IPSReplacementValue
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode the XML element node to construct this object from
    * @param parentDoc the Java object which is the parent of this object
    * @param parentComponents the parent objects of this object
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *   appropriate type
    */
   public PSBackEndColumn(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      // allow subclasses to override (don't use "this")
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs a back-end column mapping.
    *
    * @param table the back-end table containing the column; may not be
    * <code>null</code>
    * @param column the name of the back-end column; may not be empty or
    * <code>null</code> or greater than 128 characters.
    */
   public PSBackEndColumn(PSBackEndTable table, String column)
   {
      setTable(table);
      setColumn(column);
   }

   /**
    * Empty constructor for derived classes that want to initialize their state
    * before initializing their parent's state.
    */
   protected PSBackEndColumn()
   {

   }

   /**
    * Constructs a new <code>PSBackEndColumn</code> as a (shallow) copy of
    * <code>source</code>.
    * @param source provides the initial state for this object, not <code>null
    * </code>.
    */
   protected PSBackEndColumn(PSBackEndColumn source)
   {
      if (null == source)
         throw new IllegalArgumentException("source may not be null");
      copyFrom(source); // don't want to be overridden
   }

   // see interface for description
   public Object clone()
   {
      PSBackEndColumn copy = (PSBackEndColumn) super.clone();
      copy.m_table = (PSBackEndTable) m_table.clone();
      return copy;
   }

   /**
    * Get the back-end table containing the column.
    *
    * @return       the back-end table containing the column
    */
   public PSBackEndTable getTable()
   {
      return m_table;
   }

   /**
    * Set the back-end table containing the column.
    *
    * @param    table       the back-end table containing the column
    */
   public void setTable(PSBackEndTable table)
   {
      IllegalArgumentException ex = validateTable(table);
      if (ex != null)
         throw ex;

      m_table = table;
   }

   private static IllegalArgumentException validateTable(PSBackEndTable table)
   {
      if (table == null)
         return new IllegalArgumentException("back end table is null");

      return null;
   }

   /**
    * Get the name of the back-end column.
    *
    * @return       the name of the name of the back-end column
    */
   public java.lang.String getColumn()
   {
      return m_column;
   }

   /**
    * Set the name of the back-end column.
    *
    * @param    name          the name of the name of the back-end column
    */
   public void setColumn(String name)
   {
      IllegalArgumentException ex = validateColumn(name);
      if (ex != null)
         throw ex;

      m_column = name;
   }

   /**
    * Private utility method to validate a column name. Returns 0 if
    * the column name is valid, otherwise the return value is the
    * error code.
    *
    * @author   chadloder
    *
    * @version 1.18 1999/06/18
    *
    * @param   fileName The column name
    */
   private static IllegalArgumentException validateColumn(String name)
   {
      if ((null == name) || (name.length() == 0))
      {
         return new IllegalArgumentException("back end column name is empty");
      }
      else if (name.length() > MAX_COL_NAME_LEN)
      {
         return new IllegalArgumentException(
            "back end column name is too big: "
               + MAX_COL_NAME_LEN
               + " "
               + name.length());
      }

      return null;
   }

   /**
    * Get the alias associated with the back-end column.
    *
    * @return            the back-end column alias
    */
   public java.lang.String getAlias()
   {
      return m_alias;
   }

   /**
    * Set the alias associated with the back-end column.
    *
    * @param   alias      the back-end column alias; use null to use no
    *                     alias
    */
   public void setAlias(String alias)
   {
      IllegalArgumentException ex = validateAlias(alias);
      if (ex != null)
         throw ex;
      m_alias = alias;
   }

   /**
    * Private utility method to validate a column alias. Returns 0 if
    * the column alias is valid, otherwise the return value is the
    * error code.
    *
    * @param   name The column alias name
    */
   private static IllegalArgumentException validateAlias(String name)
   {
      if ((null != name) && (name.length() > MAX_COL_NAME_LEN))
      {
         return new IllegalArgumentException(
            "back end column name is too big "
               + MAX_COL_NAME_LEN
               + " "
               + name.length());
      }

      return null;
   }

   /**
    * Tests this object for equality with another object of the
    * same type.
    *
    * @param   o   The other PSBackEndColumn object
    *
    * @return boolean true if the objects are equal, false otherwise
    *
    * @since 1.14 1999/5/5
    *
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSBackEndColumn))
         return false;

      PSBackEndColumn other = (PSBackEndColumn) o;
      boolean bEqual = true;
      if (!doesMatch(other))
         bEqual = false;
      else if (!compare(m_table, other.m_table))
         bEqual = false;

      return bEqual;
   }

   /**
    * Checks if the specified object is the same backend column as this object.
    * The backend table and column name is compared, but column alias is
    * ignored.
    *
    * @param o the object need to be compared with this object,
    * may be <code>null</code> in which case <code>false</code> is returned.
    *
    * @return <code>true</code> if the backend table is the same and the
    * column name matches in case-sensitive manner, otherwise <code>false</code>
    */
   public boolean isSameColumn(Object o)
   {
      if (!(o instanceof PSBackEndColumn))
         return false;

      PSBackEndColumn other = (PSBackEndColumn)o;
      boolean bEqual = false;

      if ((m_table.equals(other.getTable()))
         && (m_column.equals(other.getColumn())))
      {
         bEqual = true;
      }
      return bEqual;
   }

   /**
    * Checks that the provided object matches this object. Differs from {@link
    * #equals(Object)} by checking only the table alias name instead of the
    * <code>PSBackEndTable</code> details. It checks the following. The check is
    * case sensitive.
    * <ol>
    * <li>column names must match</li>
    * <li>table aliases must match</li>
    * <li>column aliases must match if both have them or should match the
    * column name if either one has</li>
    * </ol>
    *
    * @param o the object need to be checked, may be <code>null</code>
    *
    * @return <code>true</code> if the above specified conditions matches,
    * otherwise <code>false</code>
    */
   public boolean doesMatch(Object o)
   {
      if (!(o instanceof PSBackEndColumn))
         return false;

      PSBackEndColumn other = (PSBackEndColumn) o;
      boolean bEqual = true;
      if (!compare(m_column, other.m_column))
         bEqual = false;
      else if (!compare(m_table.getAlias(), other.m_table.getAlias()))
         bEqual = false;
      else if (!compare(m_alias, other.m_alias))
      {
         /* If one column does not have an alias and the other
            column's alias and column name match, we have a match
            if the 2 column names are equal (proved above)
         */
         if ((other.m_alias == null || other.m_alias.equals(""))
            && (this.m_alias == null
               || this.m_alias.equals("")
               || this.m_alias.equals(this.m_column)))
            bEqual = true;
         else if (
            (this.m_alias == null || this.m_alias.equals(""))
               && (other.m_alias.equals(other.m_column)))
            bEqual = true;
         else
            bEqual = false;
      }

      return bEqual;

   }

   // *********** IPSReplacementValue Interface Implementation ***********

   /**
    * Get the type of replacement value this object represents.
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   /**
    * Get the text which can be displayed to represent this value.
    */
   public String getValueDisplayText()
   {
      return getValueText();
   }

   /**
    * Return a string formatted as if with getValueDisplayText for use
    * in translating a dragged path value into a proper display value.
    *
    * Modify this static method if getValueDisplayText changes
    *
    * @param pathtext may be <code>null</code> or empty
    * @return a string in the format of getValueDisplayText
    */
   public static String translatePathToDisplay(String pathtext)
   {
      return pathtext;
   }

   /**
    * Parse the display representation of a back-end column into an
    * array of two items. The first item in the array is the table
    * name, which will never be empty, and the second is the column, which
    * may be <code>null</code>.
    *
    * @param display
    * @return An array of exactly two <code>String</code> elements
    */
   public static String[] parseValueText(String display)
   {
      String rval[] = new String[2];

      // Format is defined by the getValueDisplayText method of
      // PSBackEndColumn
      StringTokenizer tokens = new StringTokenizer(display, ".");
      String strAlias = tokens.nextToken();
      String strColumn = null;
      if (tokens.hasMoreTokens())
      {
         strColumn = tokens.nextToken();
      }

      rval[0] = strAlias;
      rval[1] = strColumn;

      return rval;
   }

   /**
    * Static method to lookup a back end column using an input string that
    * came from the {@link #getValueDisplayText()} method above.
    * @param display Must be a string in the format used by
    * {@link #getValueDisplayText()} and must not be <code>null</code>
    * @param tables Must be a collection of <code>PSBackEndTable</code>
    * objects to search for a match
    * @return the <code>PSBackEndColumn</code> from the matching table
    */
   public static PSBackEndColumn findColumnFromDisplay(String display, PSCollection tables)
   {
      if (display == null || display.trim().length() == 0)
      {
         throw new IllegalArgumentException("The passed column display name must be non-empty");
      }
      if (tables == null)
      {
         throw new IllegalArgumentException("The passed table collection must not be null");
      }

      String arr[] = parseValueText(display);
      String strAlias = arr[0];
      String strColumn = arr[1];

      if(strAlias == null || strAlias.trim().length() == 0)
         return null;

      PSBackEndTable beTable = null;
      Iterator iter = tables.iterator();

      while (iter.hasNext())
      {
         Object t = iter.next();

         if (t instanceof PSBackEndTable)
         {
            beTable = (PSBackEndTable) t;
            String testAlias = beTable.getAlias();
            if (testAlias != null)
            {
               if (strAlias.equals(testAlias)) break; // Done
            }
         }
      }

      return new PSBackEndColumn(beTable, strColumn);
   }

   /**
    * Get the implementation specific text which for this value.
    */
   public String getValueText()
   {
      String tableName = m_table.getAlias();
      if ((tableName == null) || (tableName.length() == 0))
      {
         tableName = m_table.getTable();
      }

      if (tableName == null)
         return m_column;

      return tableName + "." + m_column;
   }

   // ************ IPSBackEndMapping Interface Implementation ************

   /**
    * Get the columns which must be selected from the back-end(s) in
    * order to use this mapping. The column name syntax is
    * <code>back-end-table-alias.column-name</code>.
    * <p>
    * This is retrieved by calling
    * <code>getTable.getAlias() + "." + getColumn()</code>
    *
    * @return       the columns which must be selected from the back-end(s)
    *             in order to use this mapping
    */
   public String[] getColumnsForSelect()
   {
      return new String[] { getValueText()};
   }

   // **************  IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXBackEndColumn XML element node
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXBackEndColumn is used to define a mapping to a back-end column.
    *    --&gt;
    *    &lt;!ELEMENT PSXBackEndColumn (tableAlias, column)&gt;
    *
    *    &lt;!--
    *       the alias of the back-end table containing the column. This alias
    *       can be used to locate the corresponding PSXBackEndTable object
    *       (contained in the PSXBackEndDataTank object).
    *    --&gt;
    *    &lt;!ELEMENT tableAlias       (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the name of the back-end column.
    *    --&gt;
    *    &lt;!ELEMENT column          (#PCDATA)&gt;
    * </code></pre>
    *
    * @return      the newly created PSXBackEndColumn XML element node
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      // create PSXBackEndTable element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "tableAlias",
         m_table.getAlias());

      // create column element
      PSXmlDocumentBuilder.addElement(doc, root, "column", m_column);

      // create column element
      PSXmlDocumentBuilder.addElement(doc, root, "columnAlias", m_alias);

      return root;
   }

   /**
    * This method is called to populate a PSBackEndColumn Java object from a
    * PSXBackEndColumn XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception    PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXBackEndColumn
    */
   public void fromXml(
      Element sourceNode,
      IPSDocument parentDoc,
      java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
            ms_NodeType);

      // make sure we got the ACL type node
      if (false == ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
            args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      }
      catch (Exception e)
      {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp)};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID,
            args);
      }

      String tableAlias = tree.getElementData("tableAlias");
      m_table = null;

      /* the link from the table alias to the actual PSBackEndTable object
       * must be fixed up. Let's locate the pipe which contains use and
       * then go through the PSBackEndDataTank objects within it.
       */
      IPSComponent parent;
      if (parentComponents != null)
      {
         PSBackEndDataTank dt;

         for (int i = parentComponents.size(); i != 0;)
         {
            i--; // decrement right away as arraylist's are 0-based

            parent = (IPSComponent) parentComponents.get(i);
            if (parent
               instanceof com.percussion.design.objectstore.PSBackEndDataTank)
               dt = (PSBackEndDataTank) parent;
            else if (
               parent instanceof com.percussion.design.objectstore.PSPipe)
               dt = ((PSPipe) parent).getBackEndDataTank();
            else if (
               parent instanceof com.percussion.design.objectstore.PSDataSet)
            {
               PSDataSet ds;

               ds = (PSDataSet) parent;
               /* Get pipe from ds */
               PSPipe pipe = ds.getPipe();
               if (pipe != null)
                  dt = pipe.getBackEndDataTank();
               else
                  continue;
            }
            else
               continue; // keep looking

            if (dt == null) // guess this wasn't set up yet
               break;

            PSCollection tabs = dt.getTables();
            PSBackEndTable onTab;
            for (int j = 0; j < tabs.size(); j++)
            {
               onTab = (PSBackEndTable) tabs.get(j);
               if (onTab.getAlias().equals(tableAlias))
               {
                  m_table = onTab;
                  break;
               }
            }

            break;
         }
      }

      // if we didn't find the table, create the place holder
      if (m_table == null)
      {
         try
         {
            m_table = new PSBackEndTable(tableAlias);
         }
         catch (IllegalArgumentException e)
         {
            throw new PSUnknownNodeTypeException(
               ms_NodeType,
               "tableAlias",
               new PSException(e.getLocalizedMessage()));
         }
      }

      // read backend column from XML node
      try
      {
         setColumn(tree.getElementData("column"));
      }
      catch (IllegalArgumentException e)
      {
         throw new PSUnknownNodeTypeException(
            ms_NodeType,
            "column",
            new PSException(e.getLocalizedMessage()));
      }

      // and the backend column alias
      try
      {
         setAlias(tree.getElementData("columnAlias"));
      }
      catch (IllegalArgumentException e)
      {
         throw new PSUnknownNodeTypeException(
            ms_NodeType,
            "columnAlias",
            new PSException(e.getLocalizedMessage()));
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

      Object[] args = null;
      IllegalArgumentException ex = validateColumn(m_column);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateTable(m_table);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateAlias(m_alias);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      // do children
      if (m_table != null)
      {
         cxt.pushParent(this);
         try
         {
            m_table.validate(cxt);
         }
         finally
         {
            cxt.popParent();
         }
      }
   }

   /**
    * Returns the display text. This is implemented to assist with
    * storing objects in the GUI controls. The control automatically
    * does a toString() on the object to display the object.
    */
   public String toString()
   {
      return getValueDisplayText();
   }

   /**
    * @param c a valid <code>PSBackEndColumn</code>, may not be
    * <code>null</code>.
    */
   public void copyFrom(PSComponent c)
   {
      if (c instanceof PSBackEndColumn)
      {
         PSBackEndColumn source = (PSBackEndColumn) c;
         try
         {
            super.copyFrom(source);
         }
         catch (IllegalArgumentException e)
         { // cannot happen
         }
         m_column = source.m_column;
         m_alias = source.m_alias;
         m_table = source.m_table;
      }
      else
         throw new IllegalArgumentException("INVALID_OBJECT_FOR_COPY");
   }

   /**
    * The value type associated with this instances of this class.
    */
   public static final String VALUE_TYPE = "BackEndColumn";

   private String m_alias; // back end column alias
   private String m_column; // back end column name
   private PSBackEndTable m_table = null; // back end table object

   private static final int MAX_COL_NAME_LEN = 128;

   // package access on this so they may reference each other in fromXml
   static final String ms_NodeType = "PSXBackEndColumn";

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_alias, m_column, m_table);
   }
}
