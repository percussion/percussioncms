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
package com.percussion.extension;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a database function. It contains one or more
 * <code>PSDatabaseFunctionDef</code> objects which represents the
 * implementation of this database function for a particular driver.
 * This class does not implement the database function itself. It only acts as
 * a container for grouping the implementation of this database function for
 * different drivers. The implementation of database functions is driver
 * specific and is done by the <code>PSDatabaseFunctionDef</code> class.
 * <p>
 * A database function is uniquely identified by its name (such as "SUBSTR",
 * name is case-insensitive).
 *
 * @see com.percussion.extension.PSDatabaseFunctionDef
 */
public class PSDatabaseFunction
{
   /**
    * Constructs this object from its XML representation.
    *
    * @param type of database function, should be one of
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    * @param sourceNode the XML element from which to load this object, may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code> or if <code>type</code> is invalid
    * @throws PSUnknownNodeTypeException If the specified element content does
    * not conform to the DTD specified in {@link#toXml(Document) toXml()}
    */
   public PSDatabaseFunction(int type, Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      PSDatabaseFunctionManager.verifyType(type);
      m_type = type;
      fromXml(sourceNode);
   }

   /**
    * Contructs the database function from the specified name and containing
    * no <code>PSDatabaseFunctionDef</code> object. Use the
    * {@link #add(PSDatabaseFunctionDef) add()} method to add
    * <code>PSDatabaseFunctionDef</code> objects to this collection.
    *
    * @param type of database function, should be one of
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    * @param name the name of the database function, may not be
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty
    * or if <code>type</code> is invalid
    */
   public PSDatabaseFunction(int type, String name)
   {
      if ((name == null) || (name.trim().length() < 1))
         throw new IllegalArgumentException("name may not be null or empty");
      PSDatabaseFunctionManager.verifyType(type);

      m_type = type;
      m_name = name;
   }

   /**
    * Adds the database function definition to this collection. Replaces
    * previous database function definition if one already exists with the same
    * type and driver (case-insensitive).
    *
    * @param dbFuncDef the database function definition to add to this
    * collection, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFuncDef</code> is
    * <code>null</code> or if the type of the specified database function
    * definition does not match the type of this collection or does not same
    * name as this function
    */
   public void add(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      if (dbFuncDef.getType() != m_type)
         throw new IllegalArgumentException(
            "Incorrect type of database function definition");

      if (!(dbFuncDef.getName().equalsIgnoreCase(m_name)))
         throw new IllegalArgumentException(
            "Database function definition ("
            + dbFuncDef.getName() + ") cannot be added to database function ("
            + m_name + ")" );

      m_dbFuncDefs.put(dbFuncDef.getDriver().toLowerCase(), dbFuncDef);
   }

   /**
    * Sets the internal members of this database function equal to the specified
    * database function.
    *
    * @param dbFunc the database function whose values should be used to set
    * the values of the internal members of this database function.
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code> or if the type or name (case-insensitive) of the
    * specified function does not match the type of this function
    */
   public void copyFrom(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      if (dbFunc.getType() != m_type)
         throw new IllegalArgumentException(
            "Incorrect type of database function");

      if (!(dbFunc.getName().equalsIgnoreCase(m_name)))
         throw new IllegalArgumentException(
            "Cannot set value of the database function (" + m_name +
            ") using the values of the database function (" +
            dbFunc.getName() + ")");

      Iterator it = dbFunc.iterator();
      while (it.hasNext())
      {
         PSDatabaseFunctionDef funcDef = (PSDatabaseFunctionDef)it.next();
         if (contains(funcDef))
            getDatabaseFunctionDef(funcDef.getDriver()).copyFrom(funcDef);
         else
            add(funcDef);
      }

      it = iterator();
      while (it.hasNext())
      {
         PSDatabaseFunctionDef funcDef = (PSDatabaseFunctionDef)it.next();
         if (!(dbFunc.contains(funcDef)))
            remove(funcDef);
      }
   }

   /**
    * Loads the database function from the supplied element.
    * See {@link#toXml(Document) toXml()} for the expected form of XML.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException If the specified element does not
    * conform to the DTD specified in {@link#toXml(Document) toXml()}
    */
   private void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      String sTemp = sourceNode.getAttribute(ATTR_FUNCTION_NAME);
      if ((sTemp == null) || (sTemp.trim().length() == 0))
      {
         Object[] args =
            {NODE_NAME, ATTR_FUNCTION_NAME, sTemp == null ? "null" : sTemp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_name = sTemp;

      // construct the PSDatabaseFunctionDef objects
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      m_dbFuncDefs.clear();
      PSDatabaseFunctionDef dbDefaultFuncDef = null;

      Element el = walker.getNextElement(
         PSDatabaseFunctionDef.getNodeName(), firstFlags);
      if (el != null)
      {
         PSDatabaseFunctionDef dbFirstFuncDef =
            new PSDatabaseFunctionDef(m_type, el, true);
         m_dbFuncDefs.put(
            dbFirstFuncDef.getDriver().toLowerCase(), dbFirstFuncDef);
         if (dbFirstFuncDef.isDefault())
            dbDefaultFuncDef = dbFirstFuncDef;

         el = walker.getNextElement(
            PSDatabaseFunctionDef.getNodeName(), nextFlags);
         while (el != null)
         {
            PSDatabaseFunctionDef dbFuncDef = null;
            if (dbDefaultFuncDef != null)
            {
               dbFuncDef = (PSDatabaseFunctionDef)dbDefaultFuncDef.clone();
               dbFuncDef.fromXml(el, false, true);
            }
            else
            {
               dbFuncDef = new PSDatabaseFunctionDef(m_type, el, false);
            }
            m_dbFuncDefs.put(dbFuncDef.getDriver().toLowerCase(), dbFuncDef);
            el = walker.getNextElement(
               PSDatabaseFunctionDef.getNodeName(), nextFlags);
         }
      }
   }

   /**
    * Serializes this object's state to Xml conforming to the DTD of the
    * "PSXDatabaseFunction" element as defined in the
    * "sys_DatabaseFunctionDefs.dtd" file.
    *
    * See {@link com.percussion.extension.PSDatabaseFunctionDef#toXml(Document)}
    * for the DTD of the PSXDatabaseFunctionDef element.
    *
    * @param doc The document to use when creating elements, may not be <code>
    *  null</code>.
    *
    * @return The element containing this object's state, never <code>
    * null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element   root = doc.createElement(NODE_NAME);
      root.setAttribute(ATTR_FUNCTION_NAME, m_name);

      Iterator it = m_dbFuncDefs.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         PSDatabaseFunctionDef dbFuncDef =
            (PSDatabaseFunctionDef)item.getValue();
         Element el = dbFuncDef.toXml(doc);
         root.appendChild(el);
      }
      return root;
   }

   /**
    * Compares the name (case-insensitive) and type of this object with the
    * specified object.
    * Compares the contained <code>PSDatabaseFunctionDef</code> objects
    * using its <code>equals()</code> method.
    * See {@link com.percussion.extension.PSDatabaseFunctionDef#equals(Object)}
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if this object and the contained
    * <code>PSDatabaseFunctionDef</code> objects match that of the specified
    * object, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    *
    * @see equalsFull(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException(
            "Cannot compare with a null object");

      boolean equals = true;
      if (!(obj instanceof PSDatabaseFunction))
         equals = false;
      else
      {
         PSDatabaseFunction other = (PSDatabaseFunction)obj;

         if (!(m_name.equalsIgnoreCase(other.m_name)))
            equals = false;
         else if (m_type != other.m_type)
            equals = false;
         else if (!(compareDatabaseFunctionDefs(other, false)))
            equals = false;
      }
      return equals;
   }

   /**
    * Compares the name (case-insensitive) and type of this object with the
    * specified object.
    * Compares the contained <code>PSDatabaseFunctionDef</code> objects
    * using its <code>equalsFull()</code> method.
    * See {@link com.percussion.extension.PSDatabaseFunctionDef#equalsFull(Object)}
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if this object and the contained
    * <code>PSDatabaseFunctionDef</code> objects match that of the specified
    * object, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    *
    * @see equals(Object)
    */
   public boolean equalsFull(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException(
            "Cannot compare with a null object");

      boolean equals = true;
      if (!(obj instanceof PSDatabaseFunction))
         equals = false;
      else
      {
         PSDatabaseFunction other = (PSDatabaseFunction)obj;

         if (!(m_name.equalsIgnoreCase(other.m_name)))
            equals = false;
         else if (m_type != other.m_type)
            equals = false;
         else if (!(compareDatabaseFunctionDefs(other, true)))
            equals = false;
      }
      return equals;
   }

   /**
    * Compares the contained database function implementation objects
    * (<code>PSDatabaseFunctionDef</code>) with that of the specified object
    * <code>funcDef</code>.
    *
    * @param funcDef the object with which to compare the contained database
    * function implementation objects (<code>PSDatabaseFunctionDef</code>),
    * assumed not <code>null</code>
    *
    * @param full if <code>true</code> then uses
    * <code>PSDatabaseFunctionDef.equalsFull()</code> method for comparing the
    * contained database function implementation objects, otherwise uses
    *  <code>PSDatabaseFunctionDef.equals()</code> method for comparison.
    *
    * @return <code>true</code> if this object and the object to compare
    * <code>funcDef</code> contain the same database function
    * implementation objects, <code>false</code> otherwise.
    */
   private boolean compareDatabaseFunctionDefs(
      PSDatabaseFunction funcDef, boolean full)
   {
      boolean equals = true;
      Iterator it = m_dbFuncDefs.entrySet().iterator();
      while (it.hasNext() && equals)
      {
         Map.Entry item = (Map.Entry)it.next();
         String driver = (String)item.getKey();
         PSDatabaseFunctionDef dbFuncDef =
            (PSDatabaseFunctionDef)item.getValue();
         PSDatabaseFunctionDef dbFuncDefOther =
            funcDef.getDatabaseFunctionDef(driver);
         if (dbFuncDefOther == null)
            equals = false;
         else if (full && (!(dbFuncDef.equalsFull(dbFuncDefOther))))
            equals = false;
         else if ((!full) && (!(dbFuncDef.equals(dbFuncDefOther))))
            equals = false;
      }
      return equals;
   }

   /**
    * Calculates the hashcode of the contained database function implementation
    * objects.
    *
    * @param full if <code>true</code> then uses the
    * <code>PSDatabaseFunctionDef.hashCodeFull()</code> method for obtaining the
    * hashcode of the contained database function implementation objects,
    * otherwise uses <code>PSDatabaseFunctionDef.hashCode()</code> method.
    *
    * @return the sum of the hashcodes of the
    * <code>PSDatabaseFunctionDef</code> objects contained within this object
    */
   private int getDatabaseFunctionDefsHashCode(boolean full)
   {
      int code = 0;
      Iterator it = m_dbFuncDefs.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         PSDatabaseFunctionDef dbFuncDef =
            (PSDatabaseFunctionDef)item.getValue();
         if (full)
            code += dbFuncDef.hashCodeFull();
         else
            code += dbFuncDef.hashCode();
      }
      return code;
   }

   /**
    * Computes the hash code for this object using the name (lowercase) and
    * type and the sum of the value returned by
    * <code>PSDatabaseFunctionDef.hashCode()</code> method for all the
    * contained database function definition objects.
    *
    * @return the hash code for this object
    *
    * @see hashCodeFull()
    */
   public int hashCode()
   {
      int code = m_name.toLowerCase().hashCode() + m_type;
      code += getDatabaseFunctionDefsHashCode(false);
      return code;
   }

   /**
    * Computes the hash code for this object using the name (lowercase) and
    * type and the sum of the value returned by
    * <code>PSDatabaseFunctionDef.hashCodeFull()</code> method for all the
    * contained database function definition objects.
    *
    * @return the hash code for this object
    *
    * @see hashCode()
    */
   public int hashCodeFull()
   {
      int code = m_name.toLowerCase().hashCode() + m_type;
      code += getDatabaseFunctionDefsHashCode(true);
      return code;
   }

   /**
    * Returns the tag name of the root element from which this object can be
    * constructed.
    *
    * @return the name of the root node of the XML document returned by a call
    * to {@link#toXml(Document) toXml()} method.
    *
    * @see toXml(Document)
    */
   public static String getNodeName()
   {
      return NODE_NAME;
   }

   /**
    * Returns the name of this database function. The database function name
    * is case-insensitive.
    *
    * @return the name of this database function, never <code>null</code> or
    * empty
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns the type of database function, user-defined or system-defined.
    *
    * @return one of <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code>
    * values
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Returns the implementation of this database function for the specified
    * driver. The driver name is case-insensitive.
    *
    * @param driver the database driver corresponding to which the database
    * specific implementaion object is to be returned, may not be
    * <code>null</code> or empty
    *
    * @return the object which provides the implentation of this database
    * function for the specified driver, may be <code>null</code> if no
    * implementation has been provided for the specified driver
    *
    * @throws IllegalArgumentException if <code>driver</code> is
    * <code>null</code> or empty
    */
   public PSDatabaseFunctionDef getDatabaseFunctionDef(String driver)
   {
      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");

      PSDatabaseFunctionDef funcDef =
         (PSDatabaseFunctionDef)m_dbFuncDefs.get(driver.toLowerCase());

      if (funcDef == null)
          funcDef = getDefaultDatabaseFunctionDef();

      return funcDef;
   }

   /**
    * Returns the default function definition if one is specified for this
    * function. Default function definition either does not have the
    * driver specified or the driver is specified as "*".
    *
    * @return default function definition if one is specified for this function,
    * otherwise <code>null</code>
    */
   public PSDatabaseFunctionDef getDefaultDatabaseFunctionDef()
   {
      return (PSDatabaseFunctionDef)m_dbFuncDefs.get(
         PSDatabaseFunctionDef.DRIVER_TYPE_DEFAULT);
   }

   /**
    * Determines if this database function contains a default function
    * definition. Default function definition either does not have the
    * driver specified or the driver is specified as "*".
    *
    * @return <code>true</code> if this database function contains a default
    * function definition, <code>false</code> otherwise.
    */
   public boolean hasDefaultDatabaseFunctionDef()
   {
      return m_dbFuncDefs.containsKey(
         PSDatabaseFunctionDef.DRIVER_TYPE_DEFAULT);
   }

   /**
    * Returns the number of database function definitions in this collection.
    *
    * @return the total number of database function definitions
    */
   public int size()
   {
      return m_dbFuncDefs.size();
   }

   /**
    * Check if the specified database function definition of the same type
    * and name (case-insensitive) exists in this collection.
    *
    * @param dbFuncDef the function definition to test for existence in this
    * collection, may not be <code>null</code> or empty
    *
    * @return <code>true</code> if the specified function is contained in this
    * collection, <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if <code>dbFuncDef</code> is
    * <code>null</code>
    */
   public boolean contains(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      boolean contains = false;
      if (dbFuncDef.getType() == m_type)
      {
         contains = m_dbFuncDefs.containsKey(
            dbFuncDef.getDriver().toLowerCase());

         if (!contains)
            contains = hasDefaultDatabaseFunctionDef();
      }
      return contains;
   }

   /**
    * Removes all of the database function definitions from this collection.
    */
   public void clear()
   {
      m_dbFuncDefs.clear();
   }

   /**
    * Check if this collection is empty
    *
    * @return <code>true</code> if this collection does not contain any
    * database function definition, <code>false</code> otherwise
    */
   public boolean isEmpty()
   {
      return m_dbFuncDefs.isEmpty();
   }

   /**
    * Removes the specified database definition from this collection if it
    * is present. Matching database function definition is obtained using
    * the type and name (case-insensitive) of the specified function definition.
    *
    * @param dbFuncDef the database function definition to remove from this
    * collection, may not be <code>null</code>
    *
    * @return <code>true</code> if this collection contained the specified
    * function definition and was removed from this collection,
    * <code>false</code> if this collection did not contain the specified
    * function definition.
    *
    * @throws IllegalArgumentException if <code>dbFuncDef</code> is
    * <code>null</code>
    */
   public boolean remove(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      boolean remove = false;
      if (dbFuncDef.getType() == m_type)
         remove =
            (m_dbFuncDefs.remove(dbFuncDef.getDriver().toLowerCase()) != null);
      return remove;
   }

   /**
    * Returns an iterator over the database function definitions in this
    * collection.
    *
    * @return an iterator over over a collection of
    * <code>PSDatabaseFunctionDef</code> objects, never <code>null</code>, the
    * collection may be empty.
    */
   public Iterator iterator()
   {
      return m_dbFuncDefs.values().iterator();
   }

   /**
    * Constants for XML element and attributes
    */
   private static final String NODE_NAME = "PSXDatabaseFunction";
   private static final String ATTR_FUNCTION_NAME = "standardFunctionName";

   /**
    * name of the database function, initialized in the ctor,
    * never <code>null</code> or empty, modified in the <code>fromXml()</code>
    * and <code>setValue()</code> methods.
    */
   private String m_name;

   /**
    * Map for storing the database function definition for a particular driver.
    * The driver name (<code>String</code> converted to lowercase) is used as
    * key and the <code>PSDatabaseFunctionDef</code> object which represents the
    * implementation of the database function for a particular driver is stored
    * as the value. Initialized to an empty map, modified in the
    * <code>fromXml</code> and <code>setValue()</code> methods.
    */
   private Map m_dbFuncDefs = new HashMap();

   /**
    * type of database function, initialized in the ctor, modified in the
    * <code>fromXml</code> and <code>setValue()</code> methods. Valid values
    * for type are one of the
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    */
   private int m_type;

}






