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

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSMetaDataCache;
import com.percussion.extension.PSDatabaseFunctionDef;
import com.percussion.extension.PSDatabaseFunctionManager;
import com.percussion.server.PSConsole;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * The PSFunctionCall class defines mappings to database functions.
 * This class is used to define a replacement value that is a database function
 * with zero or more parameter values. These parameters may be HTML parameters,
 * backend column etc.
 * (See the DTD defined in
 * {@link PSFunctionParamValue#toXml(Document) toXml()} method for all
 * possible types of supported parameters).
 */
public class PSFunctionCall
   extends PSNamedReplacementValue
   implements IPSMutatableReplacementValue
{

   /**
    * Constructs this object from its XML representation. See the
    * {@link #toXml(Document) toXml()} method for the DTD of the
    * <code>sourceNode</code> element.
    *
    * @param sourceNode the XML element node to construct this object from,
    * may not be <code>null</code>
    *
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>
    *
    * @param parentComponents   the parent objects of this object, may be
    * <code>null</code> or empty
    *
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is
    * <code>null</code> or the XML element node is not of the appropriate type
    */
   public PSFunctionCall(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      super(sourceNode, parentDoc, parentComponents);
      m_parentComponents = new ArrayList<>();
      if (parentComponents != null)
         m_parentComponents.addAll(parentComponents);
   }

   /**
    * Constructs this object from the specified database function definition
    * and function parameter values.
    *
    * @param dbFuncName the database function to be called, may not be
    * <code>null</code> or empty
    *
    * @param params the parameter values of this database function call, may
    * be <code>null</code> or empty if this database function does not require
    * any parameters.
    *
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>
    *
    * @param parentComponents   the parent objects of this object, may be
    * <code>null</code> or empty
    */
   public PSFunctionCall(
      String dbFuncName, PSFunctionParamValue[] params,
      IPSDocument parentDoc, List parentComponents)
   {
      super(dbFuncName);
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      m_parentComponents = new ArrayList();
      if (parentComponents != null)
         m_parentComponents.addAll(parentComponents);

      m_dbFuncName = dbFuncName;
      setParamValues(params);
      resetParentList(parentComponents, parentSize);
   }

   /**
    * Converts this object into a string (suitable for displaying in a table
    * cell).
    * @return the string representation of the database function used by
    * this function call in the format:
    * <i>FunctionName(param1, param2, ...)</i>.
    */
   public String toString()
   {
      return getValueDisplayText();
   }

   /**
    * Get the parameter values associated with this database function call.
    *
    * @return the parameter values of this database function call,
    * never <code>null</code>, may be empty. All members of the returned
    * array are guaranteed to be non-<code>null</code>.
    *
    * @see #getParameters()
    */
   public PSFunctionParamValue[] getParamValues()
   {
      PSFunctionParamValue[] retArray =
         new PSFunctionParamValue[m_params.size()];
      m_params.toArray(retArray);
      return retArray;
   }

   /**
    * Set the parameters associated with this database function call.
    * This object uses the input array. No further use of the array should
    * be made as it will affect this object as well.
    *
    * @param params the parameter values of this database function call, may
    * be <code>null</code> or empty if this database function does not require
    * any parameters.
    */
   public void setParamValues(PSFunctionParamValue[] params)
   {
      setParamValues(PSIteratorUtils.iterator(params));
   }

   /**
    * Set the parameters associated with this database function call. Only
    * non-<code>null</code> values are added as function parameter values.
    *
    * @param params An Iterator over zero or more
    * <code>PSFunctionParamValue</code> objects, may be <code>null</code> or
    * empty if this database function does not require any parameters.
    */
   public void setParamValues(Iterator params)
   {
      m_params = new ArrayList();
      // build column names which need to be mapped
      List cols = new ArrayList();
      if (params != null)
      {
         // get the back-end column names
         while (params.hasNext())
         {
            PSFunctionParamValue val = (PSFunctionParamValue)(params.next());
            if (val != null)
            {
               if (val.isBackEndColumn())
                  cols.add(val.getValue().getValueText());
               m_params.add(val);
            }
         }
      }
      m_columns = new String[cols.size()];
      cols.toArray(m_columns);
   }

   /**
    * Get the columns which must be selected from the back-end(s) in
    * order to use this mapping. The column name syntax is
    * <code>back-end-table-alias.column-name</code>.
    *
    * @return the columns which must be selected from the back-end(s)
    * in order to use this mapping, array may be empty
    */
   public String[] getColumnsForSelect()
   {
      return m_columns;
   }

   /**
    * Returns the name of the database function used by this object.
    *
    * @return the name of the database function used by this object,
    * non-<code>null</code> and non-empty.
    */
   public String getName()
   {
      return m_dbFuncName;
   }

   /**
    * Returns this database function call's parameter values.
    *
    * @return this database function call's parameter values
    * (<code>PSFunctionParamValue</code> objects),
    * never <code>null</code>, may be empty. All members of the returned
    * collection are guaranteed to be non-<code>null</code>.
    *
    * @see #getParamValues()
    */
   public Collection getParameters()
   {
      return m_params;
   }

   /**
    * @return the constant <code>VALUE_TYPE</code>
    * See {@link #VALUE_TYPE}
    */
   public String getType()
   {
      return VALUE_TYPE;
   }

   /**
    * Creates a clone of this object.
    *
    * @return cloned object, never <code>null</code>
    */
   public Object clone()
   {
      PSFunctionCall copy = (PSFunctionCall) super.clone();
      copy.m_params = new ArrayList(m_params.size());
      for (Iterator iter = m_params.iterator(); iter.hasNext();)
      {
         PSFunctionParamValue value = (PSFunctionParamValue)iter.next();
         copy.m_params.add(value.clone());
      }
      return copy;
   }

   /**
    * See {@link IPSReplacementValue#getValueType()} for details.
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   /**
    * Returns the text which represents this function call. The text includes
    * the function name and its parameter values in the format:
    * FUNCTION_NAME(PARAM1, PARAM2, PARAM3)
    *
    * @param displayText if <code>true</code> then returns the string which
    * can be displayed to represent this function call, otherwise returns
    * implementation specific text for this function call.
    *
    * @return text which represents this function call, never <code>null</code>
    * or empty
    */
   private String getText(boolean displayText)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(m_dbFuncName);
      buffer.append("(");
      Iterator it = m_params.iterator();
      boolean first = true;
      while (it.hasNext())
      {
         if (first)
            first = false;
         else
            buffer.append(", ");
         PSFunctionParamValue paramVal = (PSFunctionParamValue)it.next();
         if (displayText)
            buffer.append(paramVal.getValue().getValueDisplayText());
         else
            buffer.append(paramVal.getValue().getValueText());
      }
      buffer.append(")");
      return buffer.toString();
   }

   /**
    * See {@link IPSReplacementValue#getValueDisplayText()} for details.
    */
   public String getValueDisplayText()
   {
      return getText(true);
   }

   /**
    * See {@link IPSReplacementValue#getValueText()} for details.
    */
   public String getValueText()
   {
      return getText(false);
   }

   /**
    * Returns the name of the database function represented by this function
    * call.
    *
    * @return the database function name, never <code>null</code> or empty
    */
   public String getDatabaseFunctionName()
   {
      return m_dbFuncName;
   }

   /**
    * This method is used to initialize this function call. It should be called
    * once during application startup. This should be called before any call
    * to {@link #getDatabaseFunctionDef()} method is made.
    * <p>
    * In this method the actual definition of the function is obtained from
    * the database function manager. An exception is thrown if the function
    * is not defined or the number of paramaters provided does not match the
    * number of parameters required by the definition. The function definition
    * (<code>PSDatabaseFunctionDef</code> object) is then stored internally
    * and can be obtained using the {@link #getDatabaseFunctionDef()} method.
    * This function definition should always be used when using this function
    * call object. This ensures that the same defintion is used until the
    * application is restarted. Since the user can add/remove/modify user
    * defined funtions during runtime, which is then immediately loaded by the
    * database function manager, but the new function definition should not be
    * used until the application is restarted. Obtaining the function definition
    * at application initialization improves performance and guarantees that any
    * exception related to missing function definition or paramater mismatch
    * exception will only be thrown during application initialization and never
    * after that.
    *
    * @throws PSDataExtractionException if the function definition of the
    * specified function is missing or number of paramaters provided does
    * not match the number of paramaters required by the definition
    *
    * @throws IllegalStateException if this object has already been initialized
    * successfully
    */
   public void initialize() throws PSDataExtractionException
   {
      if (m_dbFuncDef != null)
         throw new IllegalStateException("Function call already initialized.");

      String dbFuncName = getDatabaseFunctionName();
      String driver = getDriver();

      PSDatabaseFunctionDef funcDef =
         PSDatabaseFunctionManager.getInstance().getDatabaseFunctionDef(
               PSDatabaseFunctionManager.FUNCTION_TYPE_SYSTEM |
               PSDatabaseFunctionManager.FUNCTION_TYPE_USER,
               dbFuncName, driver);

      // throw exception if function definition is missing
      if (funcDef == null)
      {
         Object[] args = new Object[] {dbFuncName, driver};
         throw new PSDataExtractionException(
            IPSObjectStoreErrors.DATABASE_FUNCTION_DEFINITION_NOT_FOUND,
            args);
      }

      PSFunctionParamValue[] params = getParamValues();
      int providedParamsCount = params.length;
      int reqParamsCount = funcDef.getParamsSize();

      // throw exception if insufficient number of params provided
      if (reqParamsCount != providedParamsCount)
      {
         Object[] args = new Object[]
            {
               new Integer(providedParamsCount),
               dbFuncName,
               new Integer(reqParamsCount)
            };
         throw new PSDataExtractionException(
            IPSObjectStoreErrors.DATABASE_FUNCTION_CALL_PARAM_COUNT_MISMATCH,
            args);
      }

      m_dbFuncDef = funcDef;
   }

   /**
    * Returns the function definition set during initialization.
    * The <code>initialize()</code> method should have been called before
    * this method is called, otherwise an <code>IllegalStateException</code>
    * is thrown.
    * <p>
    * This method does not retrieve the function definition from the database
    * function manager, only returns the value set during initialization.
    *
    * @return the database function definition set during initialization,
    * never <code>null</code>
    *
    * @throws IllegalStateException if this object has not been initialized
    * successfully
    *
    * @see #initialize()
    */
   public PSDatabaseFunctionDef getDatabaseFunctionDef()
   {
      if (m_dbFuncDef == null)
         throw new IllegalStateException(
            "Function call has not been initialized.");

      return m_dbFuncDef;
   }

   /**
    * Determines whether all the parameters of this function call has
    * static value, (that is the parameter values do not depend upon runtime
    * data).
    *
    * @return <code>true</code> if one or more function parameters depends
    * upon the runtime data for its value, <code>false</code> otherwise.
    *
    * @see PSFunctionParamValue#toXml(Document)
    */
   public boolean hasStaticParamsOnly()
   {
      boolean staticParams = true;
      PSFunctionParamValue[] params = getParamValues();
      for (int i = 0; (i < params.length) && staticParams; i++)
         staticParams = params[i].isStaticValue();
      return staticParams;
   }

   /**
    * This method is called to serialize this object to an XML element.
    * <p>
    * The DTD of the returned XML element is:
    * <pre><code>
    *
    * &lt;!ELEMENT PSXFunctionCall   (name, PSXFunctionParamValue*)>
    * &lt;!ATTLIST PSXFunctionCall
    *   id %UniqueId; #REQUIRED
    * >
    * &lt;!ELEMENT name (#PCDATA)>
    *
    * </code></pre>
    *
    * See {@link PSFunctionParamValue#toXml(Document) toXml()} method for the
    * DTD of the "PSXFunctionParamValue" element.
    *
    * @param doc The document to use when creating elements, may not be
    * <code>null</code>.
    *
    * @return The element containing this object's state, never <code>
    * null</code>.
    */

   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element  root = doc.createElement(getNodeName());
      root.setAttribute(ATTR_ID, String.valueOf(m_id));
      PSXmlDocumentBuilder.addElement(
         doc, root, EL_NAME, m_dbFuncName);

      for (Iterator i = m_params.iterator(); i.hasNext();)
      {
         PSFunctionParamValue val = (PSFunctionParamValue)(i.next());
         if (val != null )
         {
            Element node = val.toXml(doc);
            root.appendChild(node);
         }
      }
      return root;
   }

   /**
    * Loads this object from the supplied element.
    * See {@link #toXml(Document) toXml()} for the expected form of XML.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>
    *
    * @param parentComponents   the parent objects of this object, may be
    * <code>null</code> or empty
    *
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is
    * <code>null</code> or does not conform to the DTD specified in
    * {@link #toXml(Document) toXml()}
    */
   public void fromXml(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
      {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, getNodeName());

         if (false == (getNodeName().equals(sourceNode.getNodeName())))
         {
            Object[] args = { getNodeName(), sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         String sTemp = sourceNode.getAttribute(ATTR_ID);
         try
         {
            m_id = Integer.parseInt(sTemp);
         }
         catch (NumberFormatException e)
         {
            Object[] args =
               { getNodeName(), ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         // get the database function name
         sTemp = null;
         Element nameEl = tree.getNextElement(EL_NAME, firstFlags);
         if (nameEl != null)
            sTemp = PSXmlTreeWalker.getElementData(nameEl);

         if ((sTemp == null) || (sTemp.trim().length() < 1))
         {
            Object[] args = {getNodeName(), EL_NAME,
               ((sTemp == null) ? "null" : sTemp)};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_dbFuncName = sTemp;

         // process "PSXFunctionParamValue" elements
         tree.setCurrent(sourceNode);
         List params = new ArrayList();
         Element paramEl = tree.getNextElement(
            PSFunctionParamValue.NODE_NAME, firstFlags);
         while (paramEl != null)
         {
            params.add(new PSFunctionParamValue(
               paramEl, parentDoc, parentComponents));
            paramEl = tree.getNextElement(
               PSFunctionParamValue.NODE_NAME, nextFlags);
         }

         setParamValues(params.iterator());
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validate this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw exceptions. Instead, it
    * should register any errors with the validation context to
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param cxt the validation context, may not be <code>null</code>
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      if (m_columns == null)
         cxt.validationError(this,
            IPSObjectStoreErrors.EXT_CALL_PARAM_VALUE_NULL, null);

      cxt.pushParent(this);
      try
      {
         for (Iterator i = m_params.iterator(); i.hasNext(); )
         {
            PSFunctionParamValue val = (PSFunctionParamValue)(i.next());
            if (null != val)
               val.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   /**
    * Compares this object with the specified object. The database function
    * name comparison is case-insensitive.
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and the database function names are equal (case-insensitive)
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSFunctionCall))
         return false;

      boolean equals = true;
      PSFunctionCall other = (PSFunctionCall)obj;
      if (!m_dbFuncName.equalsIgnoreCase(other.m_dbFuncName))
         equals = false;
      else if (!compare(m_params, other.m_params))
         equals = false;

      return equals;
   }

   /**
    * Returns the hash code of this object. This includes the hashcode of the
    * database function name (converted to lowercase) and hash code of all
    * function parameters.
    *
    * @return the hashcode of this object.
    */
   public int hashCode()
   {
      return m_dbFuncName.toLowerCase().hashCode() + m_params.hashCode();
   }

   /**
    * Returns the tag name of the root element from which this object can be
    * constructed.
    *
    * @return the name of the root node of the XML document returned by a call
    * to {@link #toXml(Document) toXml()} method.
    *
    * @see #toXml(Document)
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * See {@link PSNamedReplacementValue#getErrorCode()} for details.
    */
   public int getErrorCode()
   {
      return 0;
   }

   /**
    * Returns the driver which should be used to obtain the database function
    * definition of the function which this object represents.
    *
    * @return the database driver for obtaining the database function
    * definition, never <code>null</code> or empty
    */
   public String getDriver()
   {
      String driver = null;

      // if the function param values contains a backend column, then get
      // driver for the database containing the column
      Iterator itParams = m_params.iterator();
      while (itParams.hasNext())
      {
         IPSReplacementValue val =
            ((PSFunctionParamValue)itParams.next()).getValue();
         if (val instanceof PSBackEndColumn)
         {
            PSBackEndTable table = ((PSBackEndColumn)val).getTable();
            driver = driver = getDriverFromTable(table);
            if (driver != null)
               break;
         }
      }

      // try to get the driver from the parent WHERE clause
      if ((driver == null) && (m_parentComponents != null) &&
         (!m_parentComponents.isEmpty()))
      {
         PSWhereClause whereClause = null;
         Object obj = null;
         boolean found = false;
         ListIterator itParent = m_parentComponents.listIterator();
         while ((itParent.hasNext()) && (!found))
         {
            obj = itParent.next();
            if ((obj != null) && (obj == this))
            {
               if (itParent.hasPrevious())
               {
                  found = true;
                  obj = itParent.previous();
               }
            }
         }

         if (found && (obj != null ) &&
            (obj instanceof PSWhereClause))
         {
            whereClause = (PSWhereClause)obj;
         }

         if (whereClause != null)
         {
            // check if the variable or value of the where clause contains a
            // backend column or a function call containing a backend column
            IPSReplacementValue repVar = whereClause.getVariable();
            if (repVar != this)
               driver = getDriver(repVar);

            if (driver == null)
            {
               IPSReplacementValue repVal = whereClause.getValue();
               if (repVal != this)
                  driver = getDriver(repVal);
            }
         }
      }

      // try to get the driver from the data set
      if ((driver == null) && (m_parentComponents != null) &&
         (!m_parentComponents.isEmpty()))
      {
         Object obj = null;
         PSDataSet dataSet = null;
         Iterator itDs = m_parentComponents.iterator();
         boolean foundDataSet = false;

         while (itDs.hasNext() && (!foundDataSet))
         {
            obj = itDs.next();
            if ((obj != null) &&
               (obj instanceof PSDataSet))
            {
               dataSet = (PSDataSet)obj;
               foundDataSet = true;
            }
         }

         if (dataSet != null )
         {
            PSPipe pipe = dataSet.getPipe();
            if (pipe != null)
            {
               PSBackEndDataTank tank = pipe.getBackEndDataTank();
               if (tank != null)
               {
                  Iterator itTbl = tank.getTables().iterator();
                  if (itTbl.hasNext())
                  {
                     PSBackEndTable table = (PSBackEndTable)itTbl.next();
                     driver = driver = getDriverFromTable(table);      
                  }
               }
            }
         }
      }

      if (driver == null)
      {
         // use the default driver
         try
         {
            driver = PSConnectionHelper.getConnectionDetail(null).getDriver();
         }
         catch (NamingException e)
         {
            // this is fatal
            RuntimeException re = new RuntimeException(
               "Cannot determine driver type: " + e.getLocalizedMessage());
            throw (RuntimeException)re.initCause(e);            
         }
         catch (SQLException e)
         {
            // this is fatal
            RuntimeException re = new RuntimeException(
               "Cannot determine driver type: " + e.getLocalizedMessage());
            throw (RuntimeException)re.initCause(e); 
         }
      }
      return driver;
   }

   /**
    * Returns the database driver based on the specified replacement value.
    * If <code>repVal</code> is an instance of <code>PSBackEndColumn</code> or
    * a <code>PSFunctionCall</code> containing backend column as one of the
    * parameter values, then returns the driver for the database containing the
    * table with the column specified in the backend column.
    *
    * @param repVal the value to use in determining the database driver,
    * may be <code>null</code>
    *
    * @return the driver of the database containing the backend column if any
    * in the specified replacement value, <code>null</code> if the value
    * does not contain any backend column, never empty if not <code>null</code>
    */
   private String getDriver(IPSReplacementValue repVal)
   {
      String driver = null;

      if ((repVal!= null) &&
         (repVal instanceof PSFunctionCall))
      {
         PSFunctionCall funcCall = (PSFunctionCall)repVal;
         PSFunctionParamValue[] funcParams = funcCall.getParamValues();
         for (int i = 0; i < funcParams.length; i++)
         {
            IPSReplacementValue paramVal = funcParams[i].getValue();
            if (paramVal instanceof PSBackEndColumn)
            {
               repVal = ((PSBackEndColumn)paramVal);
               break;
            }
         }
      }

      if ((repVal!= null) &&
         (repVal instanceof PSBackEndColumn))
      {
         PSBackEndTable table = ((PSBackEndColumn)repVal).getTable();
         driver = getDriverFromTable(table);
      }
      if ((driver != null) && (driver.trim().length() < 1))
         driver = null;

      return driver;
   }
   
   /**
    * Get the driver for the specified table.
    * 
    * @param table The table, assumed not <code>null</code>.
    *   
    * @return The driver name, may be <code>null</code> if it could not be
    * obtained.
    */
   private String getDriverFromTable(PSBackEndTable table)
   {
      String driver = null;
      try
      {
         PSMetaDataCache.loadConnectionDetail(table);
         driver = table.getConnectionDetail().getDriver();
      }
      catch (SQLException e)
      {
         // return null to preserve existing behavior
         PSConsole.printMsg(getClass().getSimpleName(), e);
      }
      return driver;
   }

   /**
    * The value type associated with this instances of this class.
    */
   public static final String VALUE_TYPE = "FunctionCall";

   /**
    * The name of the database function to be called. Initialized in the ctor,
    * modified in the <code>fromXml()</code> method, never <code>null</code>
    * or empty after initialization
    */
   private String m_dbFuncName;

   /**
    * the function definition of the database function wrapped by this object,
    * initialized to <code>null</code>, then set in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that
    */
   private PSDatabaseFunctionDef m_dbFuncDef = null;

   /**
    * A Collection of zero or more non-<code>null</code>
    * <code>PSFunctionParamValue</code> objects.  Initialized in ctor,
    * modified in the <code>fromXml()</code> and <code>setParamValues()</code>
    * methods, never <code>null</code> after initialization, may be empty.
    */
   private Collection m_params;

   /**
    * An array of zero or more columns which need to be mapped in order for the
    * param values to be bound successfully. There is NOT a 1:1 correspondence
    * between columns and params. Initialized in the ctor,
    * modified in the <code>fromXml()</code> and <code>setParamValues()</code>
    * methods, never <code>null</code> after initialization
    */
   private String[] m_columns;

   /**
    * the parent objects of this object, initialized in the ctor, may be
    * <code>null</code> or empty
    */
   private ArrayList m_parentComponents;

   /**
    * The tag name of the root element from which this object can be
    * constructed.
    * @see #toXml(Document)
    * @see #getNodeName()
    */
   public static final String XML_NODE_NAME = "PSXFunctionCall";

   /**
    * Constants for XML elements and attributes
    */
   private static final String ATTR_ID = "id";
   private static final String EL_NAME = "name";
}

