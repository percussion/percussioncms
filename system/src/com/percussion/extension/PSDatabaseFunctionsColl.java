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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a collection of database functions. It acts as a
 * container of <code>PSDatabaseFunction</code> objects which represents a
 * database function.
 *
 * @see com.percussion.extension.PSDatabaseFunctionDef
 * @see com.percussion.extension.PSDatabaseFunction
 */
public class PSDatabaseFunctionsColl
{
   /**
    * Constructs this object from its XML representation.
    *
    * @param type of database functions, should be one of
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    * @param sourceNode the XML element from which to load this object, may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code> or <code>type</code> is invalid
    * @throws PSUnknownNodeTypeException If the specified element content does
    * not conform to the DTD specified in {@link#toXml(Document) toXml()}
    */
   public PSDatabaseFunctionsColl(int type, Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      PSDatabaseFunctionManager.verifyType(type);
      m_type = type;
      fromXml(sourceNode);
   }

   /**
    * Constructs this collection using the specified type and containing
    * no <code>PSDatabaseFunction</code> objects. Use the
    * {@link#add(PSDatabaseFunction) add()} method to add
    * <code>PSDatabaseFunction</code> objects to this collection.
    *
    * @param type of database functions, should be one of
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    *
    * @throws IllegalArgumentException if <code>type</code> is invalid
    */
   public PSDatabaseFunctionsColl(int type)
   {
      PSDatabaseFunctionManager.verifyType(type);
      m_type = type;
   }


   /**
    * Loads the database functions from the supplied element.
    * See {@link#toXml(Document) toXml()} for the expected form of XML.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException If the specified element does not
    * conform to the DTD specified in {@link#toXml(Document) toXml()}
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // construct the PSDatabaseFunction objects
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      m_dbFuncs.clear();
      Element el = walker.getNextElement(
         PSDatabaseFunction.getNodeName(), firstFlags);
      while (el != null)
      {
         PSDatabaseFunction dbFunc = new PSDatabaseFunction(m_type, el);
         m_dbFuncs.put(dbFunc.getName().toLowerCase(), dbFunc);
         el = walker.getNextElement(
            PSDatabaseFunction.getNodeName(), nextFlags);
      }
   }

   /**
    * Serializes this object's state to Xml conforming to the DTD of the
    * "PSXDatabaseFunctionsColl" element as defined in the
    * "sys_DatabaseFunctionDefs.dtd" file.
    *
    * See {@link com.percussion.extension.PSDatabaseFunction#toXml(Document)}
    * for the DTD of the FunctionDef element.
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

      Iterator it = m_dbFuncs.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         PSDatabaseFunction dbFunc = (PSDatabaseFunction)item.getValue();
         Element el = dbFunc.toXml(doc);
         root.appendChild(el);
      }
      return root;
   }

   /**
    * Compares this object with the specified object. Compares the type and
    * the contained <code>PSDatabaseFunction</code> objects
    * (using its <code>equals()</code> method).
    * See {@link com.percussion.extension.PSDatabaseFunction#equals(Object)}
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if this object and the contained
    * <code>PSDatabaseFunction</code> objects match that of the specified
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
      if (!(obj instanceof PSDatabaseFunctionsColl))
         equals = false;
      else
      {
         PSDatabaseFunctionsColl other = (PSDatabaseFunctionsColl)obj;
         if (m_type != other.m_type)
            equals = false;
         else if (!(compareDatabaseFunctions(other, false)))
            equals = false;
      }
      return equals;
   }

   /**
    * Compares this object with the specified object.
    * Compares the type and the contained <code>PSDatabaseFunction</code>
    * objects (using its <code>equalsFull()</code> method).
    * See {@link com.percussion.extension.PSDatabaseFunction#equalsFull(Object)}
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if this object and the contained
    * <code>PSDatabaseFunction</code> objects match that of the specified
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
      if (!(obj instanceof PSDatabaseFunctionsColl))
         equals = false;
      else
      {
         PSDatabaseFunctionsColl other = (PSDatabaseFunctionsColl)obj;
         if (m_type != other.m_type)
            equals = false;
         else if (!(compareDatabaseFunctions(other, true)))
            equals = false;
      }
      return equals;
   }

   /**
    * Compares the contained database function objects
    * (<code>PSDatabaseFunction</code>) with that of the specified object
    * <code>dbFuncsColl</code>.
    *
    * @param dbFuncsColl the object with which to compare the contained database
    * function objects (<code>PSDatabaseFunction</code>),
    * assumed not <code>null</code>
    *
    * @param full if <code>true</code> then uses
    * <code>PSDatabaseFunction.equalsFull()</code> method for comparing the
    * contained database function objects, otherwise uses
    *  <code>PSDatabaseFunction.equals()</code> method for comparison.
    *
    * @return <code>true</code> if this object and the object to compare
    * <code>dbFuncsColl</code> contain the same database function
    * objects, <code>false</code> otherwise.
    */
   private boolean compareDatabaseFunctions(
      PSDatabaseFunctionsColl dbFuncsColl, boolean full)
   {
      boolean equals = true;
      Iterator it = m_dbFuncs.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         String dbFuncName = (String)item.getKey();
         PSDatabaseFunction dbFunc = (PSDatabaseFunction)item.getValue();
         PSDatabaseFunction dbFuncOther =
            dbFuncsColl.getDatabaseFunction(dbFuncName);
         if (dbFuncOther == null)
            equals = false;
         else if (full && (!(dbFunc.equalsFull(dbFuncOther))))
            equals = false;
         else if ((!full) && (!(dbFunc.equals(dbFuncOther))))
            equals = false;

         if (!equals)
            break;
      }
      return equals;
   }

   /**
    * Calculates the hashcode of the contained database function objects.
    *
    * @param full if <code>true</code> then uses the
    * <code>PSDatabaseFunction.hashCodeFull()</code> method for obtaining the
    * hashcode of the contained database function objects,
    * otherwise uses <code>PSDatabaseFunction.hashCode()</code> method.
    *
    * @return the sum of the hashcodes of the
    * <code>PSDatabaseFunction</code> objects contained within this object
    */
   private int getDatabaseFunctionDefsHashCode(boolean full)
   {
      int code = 0;
      Iterator it = m_dbFuncs.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         PSDatabaseFunction dbFunc = (PSDatabaseFunction)item.getValue();
         if (full)
            code += dbFunc.hashCodeFull();
         else
            code += dbFunc.hashCode();
      }
      return code;
   }

   /**
    * Computes the hash code for this object using the type and the sum of the
    * value returned by <code>PSDatabaseFunction.hashCode()</code> method
    * for all the contained database functions.
    *
    * @return the hash code for this object
    *
    * @see hashCodeFull()
    */
   public int hashCode()
   {
      return (m_type + getDatabaseFunctionDefsHashCode(false));
   }

   /**
    * Computes the hash code for this object using the type and the sum of the
    * value returned by <code>PSDatabaseFunction.hashCodeFull()</code> method
    * for all the contained database functions.
    *
    * @return the hash code for this object
    *
    * @see hashCode()
    */
   public int hashCodeFull()
   {
      return (m_type + getDatabaseFunctionDefsHashCode(true));
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
    * Returns the type of database functions, user-defined or system-defined.
    *
    * @return one of <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code>
    * values
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Returns the <code>PSDatabaseFunction</code> object which contains the
    * implementation of the specified database function for various drivers.
    * The database function name is case-insensitive.
    *
    * @param the name of the database function, may not be <code>null</code>
    * or empty
    *
    * @return the object containing the implementation of the specified
    * database function for different databases, may be <code>null</code> if
    * this collection does not contain the specified function
    *
    * @throws IllegalArgumentException if <code>dbFuncName</code> is
    * <code>null</code> or empty
    */
   public PSDatabaseFunction getDatabaseFunction(String dbFuncName)
   {
      if ((dbFuncName == null) || (dbFuncName.trim().length() < 1))
         throw new IllegalArgumentException(
            "database function name may not be null or empty");
      return (PSDatabaseFunction)m_dbFuncs.get(dbFuncName.toLowerCase());
   }

   /**
    * Convenience method for getting the database function definition for the
    * specified function name (case-insensitive) and driver (case-insensitive).
    *
    * @param the name of the database function, may not be <code>null</code>
    * or empty
    *
    * @param driver type of driver for which the database function definition
    * is to be obtained, may not be <code>null</code> or empty
    *
    * @return the database function definition, may be <code>null</code> if
    * the database function definition does not exist
    *
    * @throws IllegalArgumentException if <code>dbFuncName</code> or
    * <code>driver</code> is <code>null</code> or empty
    */
   public PSDatabaseFunctionDef getDatabaseFunctionDef(
      String dbFuncName, String driver)
   {
      if ((dbFuncName == null) || (dbFuncName.trim().length() < 1))
         throw new IllegalArgumentException(
            "database function name may not be null or empty");

      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException(
            "driver may not be null or empty");

      PSDatabaseFunctionDef dbFuncDef = null;
      PSDatabaseFunction dbFunc = getDatabaseFunction(dbFuncName);
      if (dbFunc != null)
         dbFuncDef = dbFunc.getDatabaseFunctionDef(driver);
      return dbFuncDef;
   }

   /**
    * Returns an iterator over a list of database function defintion
    * (<code>PSDatabaseFunctionDef</code>) objects defined for the specified
    * driver (case-insensitive).
    *
    * @param driver the driver for which the database function definitions is
    * to be returned, may not be <code>null</code> or empty
    *
    * @return an iterator over a list of <code>PSDatabaseFunctionDef</code>
    * objects, never <code>null</code>, may be empty if no database function
    * has been defined for the specified driver.
    *
    * @throws IllegalArgumentException if <code>driver</code> is
    * <code>null</code> or empty
    */
   public Iterator getDatabaseFunctionsDef(String driver)
   {
      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");

      PSDatabaseFunction dbFunc = null;
      PSDatabaseFunctionDef dbFuncDef = null;
      List funcList = new ArrayList();
      Iterator it = m_dbFuncs.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         dbFunc = (PSDatabaseFunction)item.getValue();
         dbFuncDef = dbFunc.getDatabaseFunctionDef(driver);
         if (dbFuncDef != null)
            funcList.add(dbFuncDef);
      }
      return funcList.iterator();
   }

   /**
    * Adds the database function to the collection. If a database function
    * with the same name (case-insensitive) already exists, then it is replaced
    * by the specified function <code>dbFunc</code>
    *
    * @param dbFunc the database function to add to the collection, may not be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code> or if the type of the specified database function does
    * not match the type of this collection
    */
   public void add(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      if (dbFunc.getType() != m_type)
         throw new IllegalArgumentException(
            "Incorrect type of database function");

      m_dbFuncs.put(dbFunc.getName().toLowerCase(), dbFunc);
   }

   /**
    * Convenience method for adding the database function definition to the
    * corresponding database function. If the corresponding database function
    * does not exist, it is created and then this definition added. The database
    * function definition <code>dbFuncDef</code> replaces previous database
    * function definition if one already exists.
    *
    * @param dbFuncDef the database function definition to add to the database
    * function, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFuncDef</code> is
    * <code>null</code> or if the type of the specified database function
    * definition does not match the type of this collection.
    */
   public void add(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      if (dbFuncDef.getType() != m_type)
         throw new IllegalArgumentException(
            "Incorrect type of database function definition");

      PSDatabaseFunction dbFunc = getDatabaseFunction(dbFuncDef.getName());
      if (dbFunc == null)
      {
         dbFunc = new PSDatabaseFunction(m_type, dbFuncDef.getName());
         add(dbFunc);
      }
      dbFunc.add(dbFuncDef);
   }

   /**
    * Returns the number of database functions in this collection.
    *
    * @return the total number of database functions
    */
   public int size()
   {
      return m_dbFuncs.size();
   }

   /**
    * Check if a database function with the same name (case-insensitive)
    * exists in this collection.
    *
    * @param dbFunc the function to test for existence in this collection,
    * may not be <code>null</code>
    *
    * @return <code>true</code> if the specified function is contained in this
    * collection, <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code>
    */
   public boolean contains(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      boolean contains = false;
      if (dbFunc.getType() == m_type)
         contains = m_dbFuncs.containsKey(dbFunc.getName().toLowerCase());
      return contains;
   }

   /**
    * Convenience method for checking if a database function definition of the
    * same type and name (case-insensitive) is contained in the corresponding
    * database function. If the corresponding database function definition does
    * not exist, <code>false</code> is returned. If the type of
    * <code>dbFuncDef</code> does not match the type of this collection,
    * <code>false</code> is returned.
    *
    * @param dbFuncDef the database function definition to test for existence
    * in the corresponding database function, may not be <code>null</code>
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
         PSDatabaseFunction dbFunc = getDatabaseFunction(dbFuncDef.getName());
         if (dbFunc != null)
            contains = dbFunc.contains(dbFuncDef);
      }
      return contains;
   }

   /**
    * Removes all of the database functions from this collection.
    */
   public void clear()
   {
      m_dbFuncs.clear();
   }

   /**
    * Check if this collection is empty
    *
    * @return <code>true</code> if this collection does not contain any
    * database function, <code>false</code> otherwise
    */
   public boolean isEmpty()
   {
      return m_dbFuncs.isEmpty();
   }

   /**
    * Removes the database function with the same name (case-insensitive) as
    * the specified database function from this collection if it is present.
    *
    * @param dbFunc the database function to remove from this collection, may
    * not be <code>null</code>
    *
    * @return <code>true</code> if this collection contained the specified
    * function and was removed from this collection, <code>false</code> if this
    * collection did not contain the specified function.
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code>
    */
   public boolean remove(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      boolean remove = false;
      if (dbFunc.getType() == m_type)
         remove = (m_dbFuncs.remove(dbFunc.getName().toLowerCase()) != null);
      return remove;
   }

   /**
    * Convenience method for removing the specified database function definition
    * from the corresponding database function.
    *
    * @param dbFuncDef the database function definition to remove, may not be
    * <code>null</code>
    *
    * @return <code>true</code> if the specified database function definition
    * existed and was removed, <code>false</code> otherwise
    */
   public boolean remove(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      boolean remove = false;
      PSDatabaseFunction dbFunc = getDatabaseFunction(dbFuncDef.getName());
      if (dbFunc != null)
         remove = dbFunc.remove(dbFuncDef);
      return remove;
   }

   /**
    * Returns an iterator over the database functions in this collection.
    *
    * @return an iterator over over a collection of
    * <code>PSDatabaseFunction</code> objects, never <code>null</code>, the
    * collection may be empty.
    */
   public Iterator iterator()
   {
      return m_dbFuncs.values().iterator();
   }

   /**
    * Constants for XML element and attributes
    */
   private static final String NODE_NAME = "PSXDatabaseFunctionsColl";

   /**
    * Map for storing the database functions.
    * The function name (<code>String</code> converted to lowercase) is used as
    * key and the <code>PSDatabaseFunction</code> object which represents the
    * database function as the value. Initialized to an empty map, modified in
    * the <code>fromXml</code> and <code>add()</code> and <code>remove()</code>
    * and <code>clear()</code> methods. Never <code>null</code>, may be empty.
    */
   private Map m_dbFuncs = new HashMap();

   /**
    * type of database functions, initialized in the ctor, never modified
    * after initialization, valid values for type are one of the
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    */
   private int m_type;

}






