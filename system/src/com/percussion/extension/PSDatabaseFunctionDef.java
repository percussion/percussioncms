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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class represents a database function definition for a particular driver.
 * A database function is uniquely identified by its name (case-insensitive),
 * such as "SUBSTR", and the jdbc subprotocol used by the driver
 * (case-insensitive), such as "inetdae7".
 */
public class PSDatabaseFunctionDef implements Cloneable
{
   /**
    * Constructs this object from its XML representation.
    *
    * @param type of database function definition, should be one of
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    * @param sourceNode the XML element from which to load this object, may
    * not be <code>null</code>
    * @param canBeDefault <code>true</code> if this function definition can
    * be the default definition for this function, <code>false</code> otherwise.
    * If this is a default function definition, and this parameter value
    * is <code>false</code> then <code>PSUnknownNodeTypeException</code>
    * is thrown. Default function defintions have the driver of type
    * <code>*</code>.
    * See {@link#isDefault()}
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code> or if <code>type</code> is invalid
    * @throws PSUnknownNodeTypeException If the specified element content does
    * not conform to the DTD specified in {@link#toXml(Document) toXml()}
    */
   public PSDatabaseFunctionDef(int type, Element sourceNode,
      boolean canBeDefault) throws PSUnknownNodeTypeException
   {
      PSDatabaseFunctionManager.verifyType(type);
      m_type = type;
      fromXml(sourceNode, canBeDefault, false);
   }

   /**
    * Constructs database functions definition using the specified parameters.
    *
    * @param type of database function definition, should be one of
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    *
    * @param name name of the database function, may not be <code>null</code>
    * or empty
    *
    * @param driver database driver, may not be <code>null</code>
    * or empty
    *
    * @param body body of the database function, may not be <code>null</code>
    * or empty
    *
    * @param description description for this database function definition,
    * may be <code>null</code> or empty. If <code>null</code> then set to empty
    *
    * @param params list containing the function parameters
    * (<code>PSDatabaseFunctionDefParam</code> objects), may be
    * <code>null</code> or empty, if <code>null</code> then set to empty
    */
   private PSDatabaseFunctionDef(int type, String name, String driver,
      String body, String description, List params)
   {
      PSDatabaseFunctionManager.verifyType(type);

      if ((name == null) || (name.trim().length() < 1))
         throw new IllegalArgumentException("name may not be null or empty");

      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");

      if ((body == null) || (body.trim().length() < 1))
         throw new IllegalArgumentException("body may not be null or empty");

      if (description == null)
         description = "";

      m_type = type;
      m_name = name;
      m_driver = driver;
      m_body = body;
      m_desc = description;
      m_params = (params == null ? (new ArrayList()) : params);
   }

   /**
    * Loads the database function definition from the supplied element.
    * See {@link#toXml(Document) toXml()} for the expected form of XML.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @param canBeDefault <code>true</code> if this function definition can
    * be the default definition for this function, <code>false</code> otherwise.
    * If this is a default function definition, and this parameter value
    * is <code>false</code> then <code>PSUnknownNodeTypeException</code>
    * is thrown. Default function defintions have the driver of type
    * <code>*</code>.
    * See {@link#isDefault()}
    *
    * @param fromDefault <code>true</code> if this object is being constructed
    * from a default database function definition, <code>false</code> otherwise.
    * The function body, description and parameters is optional if this is
    * <code>true</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException If the specified element does not
    * conform to the DTD specified in {@link#toXml(Document) toXml()}
    */
   void fromXml(Element sourceNode, boolean canBeDefault, boolean fromDefault)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // name - required
      String sTemp = sourceNode.getAttribute(ATTR_FUNCTION_NAME);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         Object[] args =
            {NODE_NAME, ATTR_FUNCTION_NAME, sTemp == null ? "null" : sTemp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_name = sTemp;

      // if canBeDefault is true then driver attribute is optional or its value
      // may be specified as "*". If not specified or specified as "*", then
      // this is the default function definition.
      sTemp = sourceNode.getAttribute(ATTR_DRIVER);
      if ((sTemp == null) || (sTemp.trim().length() == 0) ||
         (sTemp.equals(DRIVER_TYPE_DEFAULT)))
      {
         if (canBeDefault)
         {
            sTemp = DRIVER_TYPE_DEFAULT;
         }
         else
         {
            Object[] args =
               {NODE_NAME, ATTR_DRIVER, sTemp == null ? "null" : sTemp};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      m_driver = sTemp;

      // body - optional if fromDefault is true
      Element el = walker.getNextElement(EL_BODY, firstFlags);
      if (el != null)
         sTemp = PSXmlTreeWalker.getElementData(el);
      if ((el == null) || (sTemp == null) || (sTemp.trim().length() < 1))
      {
         if (!fromDefault)
         {
            Object[] args =
               {NODE_NAME, EL_BODY, sTemp == null ? "null" : sTemp};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }
      else
      {
         m_body = sTemp;
      }

      // params - optional if fromDefault is true
      el = walker.getNextElement(
         PSDatabaseFunctionDefParam.getNodeName(), nextFlags);
      if (el != null)
         m_params.clear();

      while (el != null)
      {
         m_params.add(new PSDatabaseFunctionDefParam(el));
         el = walker.getNextElement(
            PSDatabaseFunctionDefParam.getNodeName(), nextFlags);
      }

      // description - optional if fromDefault is true
      el = null;
      sTemp = null;
      walker.setCurrent(sourceNode);
      Node firstChild = sourceNode.getFirstChild();
      if (firstChild != null)
      {
         walker.setCurrent(firstChild);
         el = walker.getNextElement(EL_DESCRIPTION,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      if (el != null)
         sTemp = PSXmlTreeWalker.getElementData(el);
      if ((el == null) || (sTemp == null) || (sTemp.trim().length() < 1))
      {
         if (!fromDefault)
         {
            Object[] args =
               {NODE_NAME, EL_DESCRIPTION, sTemp == null ? "null" : sTemp};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }
      else
      {
         m_desc = sTemp;
      }
   }

   /**
    * Serializes this object's state to Xml conforming to the DTD of the
    * "PSXDatabaseFunctionDef" element as defined in the
    * "sys_DatabaseFunctionDefs.dtd" file.
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
      root.setAttribute(ATTR_DRIVER, m_driver);

      PSXmlDocumentBuilder.addElement(doc, root, EL_BODY, m_body);

      Iterator it = m_params.iterator();
      while (it.hasNext())
      {
         PSDatabaseFunctionDefParam param =
            (PSDatabaseFunctionDefParam)it.next();
         root.appendChild(param.toXml(doc));
      }

      PSXmlDocumentBuilder.addElement(doc, root, EL_DESCRIPTION, m_desc);
      return root;
   }

   /**
    * Sets the internal members of this database function definition equal to
    * the specified database function definition.
    *
    * @param dbFuncDef the database function definition whose values should be
    * used to set the value of the internal members of this object, may not
    * be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFuncDef</code> is
    * <code>null</code> or if the type or name (case-insensitive) or
    * driver (case-insensitive) of the specified function definition does not
    * match the type of this function definition.
    */
   public void copyFrom(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      if (dbFuncDef.getType() != m_type)
         throw new IllegalArgumentException(
            "Incorrect type of database function definition");

      if (!(dbFuncDef.getName().equalsIgnoreCase(m_name)))
         throw new IllegalArgumentException(
            "Cannot set value of the database function definition (" + m_name +
            ") using the values of the database function definition (" +
            dbFuncDef.getName() + ")");

      if (!(dbFuncDef.getDriver().equalsIgnoreCase(m_driver)))
         throw new IllegalArgumentException(
            "Cannot set value of the database function definition (" + m_name +
            ", driver = " + m_driver +
            ") using the values of the database function definition (" +
            dbFuncDef.getName() + ", driver = " +
            dbFuncDef.getDriver() + ")");

      m_body = dbFuncDef.getBody();
      m_desc = dbFuncDef.getDescription();
      m_params.clear();
      m_params.addAll(dbFuncDef.m_params);
   }

   /**
    * Creates a clone of this object.
    *
    * @return cloned object, never <code>null</code>
    */
   public Object clone()
   {
      List params = new ArrayList();
      params.addAll(m_params);

      return new PSDatabaseFunctionDef(getType(), getName(), getDriver(),
         getBody(), getDescription(), params);
   }

   /**
    * Compares the name (case-insensitive) and type and driver
    * (case-insensitive) of this object with the specified object. This method
    * excludes the body, description and the parameters from the comparison.
    * This is done so that
    * <code>PSDatabaseFunction.contains(PSDatabaseFunctionDef)</code> method
    * can return meaningful result.
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if the name and driver of this object
    * matches that of the specified object, <code>false</code> otherwise.
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
      if (!(obj instanceof PSDatabaseFunctionDef))
         equals = false;
      else
      {
         PSDatabaseFunctionDef other = (PSDatabaseFunctionDef)obj;

         if (!(m_name.equalsIgnoreCase(other.m_name)))
            equals = false;
         else if (m_type != other.m_type)
            equals = false;
         else if (!(m_driver.equalsIgnoreCase(other.m_driver)))
            equals = false;
      }
      return equals;
   }

   /**
    * Compares this object with the specified object. Details of the
    * comparison is shown below:
    *
    * name - case insensitive string comparison
    * type - numeric comparison
    * driver - case insensitive string comparison
    * body - case sensitive string comparison
    * param - <code>List</code> comparison
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if the parameters listed above match that of the
    * specified object, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    *
    * @see equals(Object)
    */
   public boolean equalsFull(Object obj)
   {
      boolean equals = equals(obj);
      if (!equals)
         return false;

      PSDatabaseFunctionDef other = (PSDatabaseFunctionDef)obj;
      if (!(m_body.equals(other.m_body)))
         equals = false;
      else if (!(m_params.equals(other.m_params)))
         equals = false;
      else if (!(m_desc.equals(other.m_desc)))
         equals = false;

      return equals;
   }

   /**
    * Computes the hash code for this object using the name
    * (converted to lowercase) and type and driver (converted to lowercase)
    *
    * @return the hash code for this object
    *
    * @see hashCodeFull()
    */
   public int hashCode()
   {
      return m_name.toLowerCase().hashCode() + m_type +
         m_driver.toLowerCase().hashCode();
   }

   /**
    * Computes the hash code for this object using the name
    * (converted to lowercase) and type and driver (converted to lowercase),
    * body, description and the function parameters
    *
    * @return the hash code for this object
    *
    * @see hashCode()
    */
   public int hashCodeFull()
   {
      return hashCode() + m_body.hashCode() +
         m_params.hashCode() + m_desc.hashCode();
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
    * Returns the type of database function definition,
    * user-defined or system-defined.
    *
    * @return one of <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code>
    * values
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * The database driver for which this database function has been defined.
    * The driver name is case-insensitive.
    *
    * @return the database driver supported by this function, never
    * <code>null</code> or empty
    */
   public String getDriver()
   {
      return m_driver;
   }

   /**
    * Determines whether this database function has been defined for the
    * specified driver. The driver name comparison is case-insensitive.
    *
    * @param driver the database driver to test for support against this
    * database function, may not be <code>null</code> or empty
    *
    * @return <code>true</code> if this database function supports the specified
    * driver, <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if <code>driver</code> is
    * <code>null</code> or empty
    */
   public boolean isDriver(String driver)
   {
      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");
      return m_driver.equalsIgnoreCase(driver);
   }

   /**
    * Determines whether this is the default funcion definition.
    * Default function definition either does not have the
    * driver specified or the driver is specified as "*".
    *
    * @return <code>true</code> if this the default funcion definition,
    * <code>false</code> otherwise.
    */
   public boolean isDefault()
   {
      return m_driver.equals(DRIVER_TYPE_DEFAULT);
   }

   /**
    * Returns the body of the database function. For example, the body of
    * the database function "SUBSTR" would be "SUBSTR({0}, {1}, {2})"
    *
    * @return the body of the database function, never <code>null</code> or
    * empty
    */
   public String getBody()
   {
      return m_body;
   }

   /**
    * Returns the description of the database function.
    *
    * @return the description of the database function, never <code>null</code>,
    * may be empty
    */
   public String getDescription()
   {
      return m_desc;
   }

   /**
    * Returns the number of parameters that this function has.
    *
    * @return the number of parameters specified for this function
    */
   public int getParamsSize()
   {
      return m_params.size();
   }

   /**
    * Returns an iterator over a list of zero or more parameters.
    * (<code>PSDatabaseFunctionDefParam</code> object).
    *
    * @return an iterator over a list of parameters, never
    * <code>null</code>, the list may be empty
    */
   public Iterator getParams()
   {
      return m_params.iterator();
   }

   /**
    * Returns the function parameter at the specified index.
    *
    * @param index the index of the function paramater, must be valid
    * (non-negative and less than the number of parameters)
    *
    * @return the function parameter at the specified index, never
    * <code>null</code>
    *
    * @throws IllegalArgumentException if <code>index</code> is invalid
    */
   public PSDatabaseFunctionDefParam getParamAtIndex(int index)
   {
      if ((index < 0) || (index > m_params.size() - 1))
         throw new IllegalArgumentException(
            "Invalid function parameter index specified");
      return (PSDatabaseFunctionDefParam)m_params.get(index);
   }

   /**
    * Constant to be used for driver for default database function definition
    */
   public static final String DRIVER_TYPE_DEFAULT = "*";

   // Constants for XML element and attributes
   private static final String NODE_NAME = "PSXDatabaseFunctionDef";
   private static final String EL_BODY = "Body";
   private static final String EL_DESCRIPTION = "Description";
   private static final String ATTR_FUNCTION_NAME = "standardFunctionName";
   private static final String ATTR_DRIVER = "driver";

   /**
    * name of the database function (is case-insensitive), initialized in the
    * ctor, never <code>null</code> or empty, modified in the
    * <code>fromXml()</code> and <code>copyFrom()</code> methods.
    */
   private String m_name;

   /**
    * the database driver supported by this database function
    * (is case-insensitive), initialized in the ctor, never <code>null</code>
    * or empty after that, modified in the <code>fromXml()</code> and
    * <code>copyFrom()</code> methods.
    */
   private String m_driver;

   /**
    * body of the database function, initialized in the ctor,
    * never <code>null</code> or empty after that, modified in the
    * <code>fromXml()</code> and <code>copyFrom()</code> methods.
    */
   private String m_body;

   /**
    * description for this database function, initialized to empty string,
    * then set in the ctor, modified in the <code>fromXml()</code> and
    * <code>copyFrom()</code> methods. Never <code>null</code>, may be empty
    */
   private String m_desc = "";

   /**
    * list for storing the function parameters
    * (<code>PSDatabaseFunctionDefParam/code> objects), initialized to empty
    * list, modified in the <code>fromXml()</code> and
    * <code>copyFrom()</code> methods. Never <code>null</code>, may be empty.
    */
   private List m_params = new ArrayList();

   /**
    * type of database function, initialized in the ctor, modified in the
    * <code>fromXml()</code> and <code>copyFrom()</code> methods. Valid values
    * for type are one of the
    * <code>PSDatabaseFunctionManager.FUNCTION_TYPE_XXX</code> values
    */
   private int m_type;

}






