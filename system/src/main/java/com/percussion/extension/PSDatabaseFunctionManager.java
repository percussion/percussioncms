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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.PSServer;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;

/**
 * The database function manager is the object through which database
 * functions are cataloged. This is a singleton class. The first call to
 * <code>createInstance()</code> creates the singleton. Any call to
 * <code>createInstance()</code> after that throws an
 * <code>IllegalStateException</code>.
 * A long lived object should call the <code>createInstance()</code> method
 * (during server startup) and store the reference to prevent the singleton
 * from being garbage collected. Other classes should only call
 * <code>getInstance()</code> method.
 */
public class PSDatabaseFunctionManager
{
   /**
    * Private Constructor. Use the <code>createInstance()</code> method to
    * create the single instance of this class. Thereafter, use the
    * <code>getInstance()</code> method to get a handle to the singleton.
    *
    * @param props contains initialization parameters, currently it only
    * supports two properties.
    * (See {@link #SYS_DB_FUNCTIONS_FILE} and {@link #USER_DB_FUNCTIONS_FILE})
    * May not be <code>null</code> and should contain a non-<code>null</code>
    * and non-empty value for the keys <code>SYS_DB_FUNCTIONS_FILE</code> and
    * <code>USER_DB_FUNCTIONS_FILE</code>
    *
    * @throws IOException if system database functions file does not exist or
    * an error occurs reading from the file
    * @throws SAXException if an error parsing the system database functions
    * XML file
    * @throws PSUnknownNodeTypeException if the system database functions  XML
    * file is not valid. See the "sys_DatabaseFunctionDefs.dtd" file for the
    * DTD of the XML file from which database functions can be loaded.
    */
   private PSDatabaseFunctionManager(Properties props)
      throws IOException, SAXException, PSUnknownNodeTypeException
   {
      m_props = props;

      // initialize the system database functions
      String filePath = PSServer.getRxFile(props.getProperty(SYS_DB_FUNCTIONS_FILE));
      m_dbSysFuncColl = new PSDatabaseFunctionsColl(FUNCTION_TYPE_SYSTEM);
      loadDatabaseFunctions(filePath, m_dbSysFuncColl, true, 0, false);

      // initialize the user database functions
      try
      {
         filePath = PSServer.getRxFile(props
               .getProperty(USER_DB_FUNCTIONS_FILE));
      }
      catch (Exception e)
      {
         // Ignore, just means file is not present
      }
      m_dbUserFuncColl = new PSDatabaseFunctionsColl(FUNCTION_TYPE_USER);
      m_dbUserFuncFileLastModified = loadDatabaseFunctions(filePath,
            m_dbUserFuncColl, false, m_dbUserFuncFileLastModified, false);

   }

   /**
    * Loads the database functions from the specified file if the last modified
    * time of the file does not equal <code>lastModifiedTime</code>. If the
    * file is optional (for example user functions file) and the file has been
    * removed since the last time it was loaded, then the functions collection (
    * <code>funcColl</code>) is emptied.
    * 
    * @param filePath the path of the file from which the database functions are
    *           to be loaded, assumed not <code>null</code>, may be empty
    * @param funcColl the database functions collection which has to be loaded
    *           from the specified file, assumed not <code>null</code>
    * @param fileShouldExist if <code>true</code> and the specified file does
    *           not exist, then throws a <code>FileNotFoundException</code>,
    *           otherwise simply returns if the file does not exist.
    * @param lastModifiedTime reload the database functions only if the last
    *           modified time of the specified file is not equal to the value of
    *           this parameter
    * @param updateLastModifiedTime if <code>true</code> and the functions
    *           were reloaded from the specified file, then updates the last
    *           modified time of the specified file to the time it was read,
    *           else does not update the last modified time.
    * 
    * @return the time that the specified file was last modified, returns
    *         <code>lastModifiedTime</code> if the file does not exist.
    * 
    * @throws IOException if <code>fileShouldExist</code> is <code>true</code>
    *            and the specified file does not exist or an error occurs
    *            reading from the file
    * @throws SAXException if an error occurs parsing the specified XML file
    * @throws PSUnknownNodeTypeException if the specified XML file is not valid.
    *            See the "sys_DatabaseFunctionDefs.dtd" file for the DTD of the
    *            XML file from which database functions can be loaded.
    */
   private static long loadDatabaseFunctions(String filePath,
      PSDatabaseFunctionsColl funcColl, boolean fileShouldExist,
      long lastModifiedTime, boolean updateLastModifiedTime)
      throws IOException, SAXException, PSUnknownNodeTypeException
   {
      File file = new File(filePath);
      boolean exists = (file.exists() && file.isFile());
      if ((!exists) && fileShouldExist)
      {
         throw new FileNotFoundException("Database functions file (" +
            filePath + ") does not exist.");
      }

      if (exists && (lastModifiedTime < file.lastModified()))
      {
         synchronized(m_lock)
         {
            File funcFile = new File(filePath);
            if (lastModifiedTime < funcFile.lastModified())
            {

                try(FileInputStream fis = new FileInputStream(funcFile)){
                  long time = Calendar.getInstance().getTime().getTime();

                  Document doc =
                     PSXmlDocumentBuilder.createXmlDocument(fis, false);
                  funcColl.fromXml(doc.getDocumentElement());

                  if (updateLastModifiedTime)
                     funcFile.setLastModified(time);
                  lastModifiedTime = time;
               }
            }
            else
            {
               lastModifiedTime = funcFile.lastModified();
            }
         }
      }
      if (!exists)
      {
         synchronized(m_lock)
         {
            funcColl.clear();
         }
      }
      return lastModifiedTime;
   }

   /**
    * Returns the handle to the singleton object of this class. All classes
    * (except one long lived object which calls <code>createInstance()</code>)
    * should always call this method to get an instance of this class.
    * <p>
    * The <code>createInstance()</code> should be called once before this
    * method is called otherwise an <code>IllegalStateException</code> is
    * thrown.
    * <p>
    * This function checks to see if the user database functions file has
    * been modified since the last time <code>createInstance()</code> or
    * this method was called. If the file has changed, then reloads the user
    * database functions. If the file has been removed, then the user functions
    * collections is emptied.
    * <p>
    * Since the user functions files can be modified or removed at any time,
    * which will be immediately reflected in this object, other classes
    * should store the reference of database function or function definition
    * that they need and not rely on database function manager to return the
    * same reference for a function or function defintion everytime one of
    * the <code>getDatabaseFunctionXXX()</code> methods is called.
    * <p>
    * The system database functions file can only be installed/modified by the
    * Rhythmyx Installer, so there is no need to reload it.
    *
    * @return the handle to the single instance of this class, never
    * <code>null</code>
    *
    * @throws RuntimeException if the user database functions file has been
    * modified since the last time the database functions were loaded from the
    * file and an error occurs reading from the file or an error occurs parsing
    * the XML file or XML file is not valid.
    */
   public static PSDatabaseFunctionManager getInstance()
   {
      if (ms_this == null)
         throw new IllegalStateException(
            "Database function manager not initialized");

      try
      {
         String filePath = m_props.getProperty(USER_DB_FUNCTIONS_FILE);
         m_dbUserFuncFileLastModified = loadDatabaseFunctions(
            filePath, m_dbUserFuncColl, false,
            m_dbUserFuncFileLastModified, true);
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex.getLocalizedMessage());
      }
      return ms_this;
   }

   /**
    * This method should only be called once. This should be called during
    * server startup by a long lived object, which should store the reference
    * to prevent the singleton from being garbage collected.
    * No other class should call this method.
    *
    * @param props contains initialization parameters, currently it only
    * supports two properties (with key <code>SYS_DB_FUNCTIONS_FILE</code>
    * and <code>USER_DB_FUNCTIONS_FILE</code>).
    * May not be <code>null</code> and should contain a non-<code>null</code>
    * and non-empty value for the key <code>SYS_DB_FUNCTIONS_FILE</code> and
    * <code>USER_DB_FUNCTIONS_FILE</code>.
    *
    * @return the handle to the single instance of this class, never
    * <code>null</code>
    *
    * @throws IOException if the system configuration file
    * (See {@link #SYS_DB_FUNCTIONS_FILE}) required by this class
    * does not exist
    * @throws SAXException if any error occurs parsing the configuration files
    * @throws PSExtensionException if the configuration file is not valid, see
    * the "sys_DatabaseFunctionDefs.dtd" file for the DTD of this XML file.
    *
    * @see getInstance()
    */
   public static PSDatabaseFunctionManager createInstance(Properties props)
      throws IOException, SAXException, PSUnknownNodeTypeException
   {
      if ((props == null) || (props.getProperty(SYS_DB_FUNCTIONS_FILE) == null)
         || (props.getProperty(USER_DB_FUNCTIONS_FILE) == null))
      {
         throw new IllegalStateException("Invalid properties specified");
      }

      if (ms_this != null)
         throw new IllegalStateException(
            "Database function manager already initialized");

      ms_this = new PSDatabaseFunctionManager(props);
      return ms_this;
   }

   /**
    * Returns the <code>PSDatabaseFunction</code> object which contains the
    * implementation of the specified database function for various drivers.
    * The database function name is case-insensitive.
    *
    * @param type should be one of <code>FUNCTION_TYPE_XXX</code> values
    * or mulitple <code>FUNCTION_TYPE_XXX</code> values OR'ed together
    *
    * @param the name of the database function, may not be <code>null</code>
    * or empty
    *
    * @return the object containing the implementation of the specified
    * database function for different databases, may be <code>null</code> if
    * the specified collection type does not contain the specified function
    *
    * @throws IllegalArgumentException if <code>dbFuncName</code> is
    * <code>null</code> or empty
    */
   public PSDatabaseFunction getDatabaseFunction(int type, String dbFuncName)
   {
      if ((dbFuncName == null) || (dbFuncName.trim().length() < 1))
         throw new IllegalArgumentException(
            "dbFuncName may not be null or empty");

      PSDatabaseFunction dbFunc = null;

      if ((type & FUNCTION_TYPE_USER) == FUNCTION_TYPE_USER)
         dbFunc = m_dbUserFuncColl.getDatabaseFunction(dbFuncName);

      if ((dbFunc == null) &&
         ((type & FUNCTION_TYPE_SYSTEM) == FUNCTION_TYPE_SYSTEM))
      {
         dbFunc = m_dbSysFuncColl.getDatabaseFunction(dbFuncName);
      }

      return dbFunc;
   }

   /**
    * Returns an iterator over a list of database functions
    * (<code>PSDatabaseFunction</code>) objects of the specified type(s).
    *
    * @param type should be one of <code>FUNCTION_TYPE_XXX</code> values
    * or mulitple <code>FUNCTION_TYPE_XXX</code> values OR'ed together
    *
    * @return an iterator over a list of <code>PSDatabaseFunction</code>
    * objects, never <code>null</code>, may be empty if no database function
    * has been defined of the specified type (for example, if there are no
    * user defined functions).
    */
   public Iterator getDatabaseFunctions(int type)
   {
      Iterator itUserFunc = PSIteratorUtils.emptyIterator();
      Iterator itSysFunc = PSIteratorUtils.emptyIterator();

      if ((type & FUNCTION_TYPE_USER) == FUNCTION_TYPE_USER)
         itUserFunc = m_dbUserFuncColl.iterator();

      if ((type & FUNCTION_TYPE_SYSTEM) == FUNCTION_TYPE_SYSTEM)
         itSysFunc = m_dbSysFuncColl.iterator();

      return PSIteratorUtils.joinedIterator(itUserFunc, itSysFunc);
   }


   /**
    * Returns an iterator over a list of database function defintion
    * (<code>PSDatabaseFunctionDef</code>) objects of the specified type(s)
    * defined for the specified driver (case-insensitive).
    *
    * @param type should be one of <code>FUNCTION_TYPE_XXX</code> values
    * or mulitple <code>FUNCTION_TYPE_XXX</code> values OR'ed together
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
   public Iterator getDatabaseFunctionsDef(int type, String driver)
   {
      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");

      Iterator itUserFuncDef = PSIteratorUtils.emptyIterator();
      Iterator itSysFuncDef = PSIteratorUtils.emptyIterator();

      if ((type & FUNCTION_TYPE_USER) == FUNCTION_TYPE_USER)
         itUserFuncDef = m_dbUserFuncColl.getDatabaseFunctionsDef(driver);

      if ((type & FUNCTION_TYPE_SYSTEM) == FUNCTION_TYPE_SYSTEM)
         itSysFuncDef = m_dbSysFuncColl.getDatabaseFunctionsDef(driver);

      return PSIteratorUtils.joinedIterator(itUserFuncDef, itSysFuncDef);
   }

   /**
    * Convenience method for getting the database function definition for the
    * specified function name (case-insensitive) and driver (case-insensitive).
    *
    * @param type should be one of <code>FUNCTION_TYPE_XXX</code> values
    * or mulitple <code>FUNCTION_TYPE_XXX</code> values OR'ed together
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
      int type, String dbFuncName, String driver)
   {
      if ((dbFuncName == null) || (dbFuncName.trim().length() < 1))
         throw new IllegalArgumentException(
            "dbFuncName may not be null or empty");

      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");

      PSDatabaseFunctionDef dbFuncDef = null;

      if ((type & FUNCTION_TYPE_USER) == FUNCTION_TYPE_USER)
         dbFuncDef =
            m_dbUserFuncColl.getDatabaseFunctionDef(dbFuncName, driver);

      if ((dbFuncDef == null) &&
         ((type & FUNCTION_TYPE_SYSTEM) == FUNCTION_TYPE_SYSTEM))
      {
         dbFuncDef = m_dbSysFuncColl.getDatabaseFunctionDef(dbFuncName, driver);
      }

      return dbFuncDef;
   }

   /**
    * Adds the database function to the collection of the same type as the
    * specified function. If a database function with the same name
    * (case-insensitive) already exists, then it is replaced by the specified
    * function <code>dbFunc</code>
    *
    * @param dbFunc the database function to add to the collection of the same
    * type as the function, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code>
    */
   public void add(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      int type = dbFunc.getType();

      if (type == FUNCTION_TYPE_USER)
         m_dbUserFuncColl.add(dbFunc);
      else if (type == FUNCTION_TYPE_SYSTEM)
         m_dbSysFuncColl.add(dbFunc);
   }

   /**
    * Adds the database function definition to the collection of the same type
    * as the specified function definition. If a database function definition
    * with the same name (case-insensitive) already exists, then it is replaced
    * by the specified function definition <code>dbFuncDef</code>
    *
    * @param dbFuncDef the database function definition to add to the collection
    * of the same type as the function definition, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbFuncDef</code> is
    * <code>null</code>
    */
   public void add(PSDatabaseFunctionDef dbFuncDef)
   {
      if (dbFuncDef == null)
         throw new IllegalArgumentException("dbFuncDef may not be null");

      int type = dbFuncDef.getType();

      if (type == FUNCTION_TYPE_USER)
         m_dbUserFuncColl.add(dbFuncDef);
      else if (type == FUNCTION_TYPE_SYSTEM)
         m_dbSysFuncColl.add(dbFuncDef);
   }

   /**
    * Check if a database function with the same name (case-insensitive) exists
    * in the collection of the same type as the specified database function
    * <code>dbFunc</code>
    *
    * @param dbFunc the function to test for existence, may not be
    * <code>null</code>
    *
    * @return <code>true</code> if the specified function is contained in the
    * collection of the same type as the specified database function,
    * <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code>
    */
   public boolean contains(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      int type = dbFunc.getType();
      boolean contains = false;

      if (type == FUNCTION_TYPE_USER)
         contains = m_dbUserFuncColl.contains(dbFunc);
      else if (type == FUNCTION_TYPE_SYSTEM)
         contains = m_dbSysFuncColl.contains(dbFunc);

      return contains;
   }

   /**
    * Convenience method for checking if a database function definition of the
    * same type and name (case-insensitive) and driver (case-insensitive) as the
    * specified function definition is contained in the corresponding
    * database function.
    * If the corresponding database function definition does not exist,
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

      int type = dbFuncDef.getType();
      boolean contains = false;

      if (type == FUNCTION_TYPE_USER)
         contains = m_dbUserFuncColl.contains(dbFuncDef);
      else if (type == FUNCTION_TYPE_SYSTEM)
         contains = m_dbSysFuncColl.contains(dbFuncDef);

      return contains;
   }

   /**
    * Removes the database function with the same name (case-insensitive) and
    * type as the specified datbase function <code>dbFunc</code> from the
    * collection if it is present.
    *
    * @param dbFunc the database function to remove from the collection, may
    * not be <code>null</code>
    *
    * @return <code>true</code> if the collection contained the specified
    * function and was removed from this collection, <code>false</code> if the
    * collection did not contain the specified function.
    *
    * @throws IllegalArgumentException if <code>dbFunc</code> is
    * <code>null</code>
    */
   public boolean remove(PSDatabaseFunction dbFunc)
   {
      if (dbFunc == null)
         throw new IllegalArgumentException("dbFunc may not be null");

      int type = dbFunc.getType();
      boolean remove = false;

      if (type == FUNCTION_TYPE_USER)
         remove = m_dbUserFuncColl.remove(dbFunc);
      else if (type == FUNCTION_TYPE_SYSTEM)
         remove = m_dbSysFuncColl.remove(dbFunc);

      return remove;
   }

   /**
    * Convenience method for removing the specified database function definition
    * from the corresponding database function. Returns <code>true</code> if
    * this collection contained a function definition with the same name
    * (case-insensitive) and driver (case-insensitive) and type as the specified
    * parameter <code>dbFuncDef</code> and was successfully removed.
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

      int type = dbFuncDef.getType();
      boolean remove = false;

      if (type == FUNCTION_TYPE_USER)
         remove = m_dbUserFuncColl.remove(dbFuncDef);
      else if (type == FUNCTION_TYPE_SYSTEM)
         remove = m_dbSysFuncColl.remove(dbFuncDef);

      return remove;
   }

   /**
    * Verifies that the type of database function is valid.
    *
    * @param type the type of database function, should be one of the
    * <code>FUNCTION_TYPE_XXX</code> values
    *
    * @throws IllegalArgumentException if <code>type</code> is invalid
    */
   public static void verifyType(int type)
   {
      if (!((type == FUNCTION_TYPE_SYSTEM) || (type == FUNCTION_TYPE_USER)))
         throw new IllegalArgumentException(
            "Invalid database function type : " + type);
   }

   /**
    * Constant for the system defined database functions
    */
   public static final int FUNCTION_TYPE_SYSTEM = 1;

   /**
    * Constant for the user defined database functions
    */
   public static final int FUNCTION_TYPE_USER = 2;

   /**
    * The default path of the file containing the
    * system database functions (relative to the Rhythmyx root directory).
    */
   public static final String DEFAULT_SYS_DB_FUNCTIONS_FILE =
      "rxconfig/Server/sys_DatabaseFunctionDefs.xml";

   /**
    * The name of the key used to store the path of the file containing the
    * system database functions (relative to the Rhythmyx root directory).
    */
   public static final String SYS_DB_FUNCTIONS_FILE = "sysDbFunctionsFile";

   /**
    * The default path of the file containing the
    * user database functions (relative to the Rhythmyx root directory).
    */
   public static final String DEFAULT_USER_DB_FUNCTIONS_FILE =
      "rxconfig/Server/DatabaseFunctionDefs.xml";

   /**
    * The name of the key used to store the path of the file containing the
    * user database functions (relative to the Rhythmyx root directory).
    */
   public static final String USER_DB_FUNCTIONS_FILE = "userDbFunctionsFile";

   /**
    * Constant for name of the database function "UPPER"
    */
   public static final String DB_FUNCTION_UPPER = "UPPER";

   /**
    * Stores the system database function definitions, initialized by the first
    * call to the <code>createInstance()</code> method, never <code>null</code>
    * after initialization.
    */
   private static PSDatabaseFunctionsColl m_dbSysFuncColl = null;

   /**
    * Stores the time that the user database functions file was last modified.
    * Initialized to <code>0</code>. Set in the ctor and modified in the
    * <code>getInstance()</code> method if the user database functions file
    * exists.
    */
   private static long m_dbUserFuncFileLastModified = 0;

   /**
    * Stores the user database function definitions, initialized by the first
    * call to the <code>createInstance()</code> method, never <code>null</code>
    * after initialization.
    */
   private static PSDatabaseFunctionsColl m_dbUserFuncColl = null;

   /**
    * The single instance of this class, initialized by the first
    * call to the <code>createInstance()</code> method, never <code>null</code>
    * or modified after initialization.
    */
   private volatile static PSDatabaseFunctionManager ms_this = null;

   /**
    * contains initialization parameters, initialized in the constructor,
    * currently it supports only two properties
    * (with key <code>SYS_DB_FUNCTIONS_FILE</code> and
    * <code>USER_DB_FUNCTIONS_FILE</code>). Never <code>null</code>
    * or modified after initialization.
    */
   private static Properties m_props = null;

   /**
    * Used for synchronization when loading the database functions,
    * initialized in the static initializer, never <code>null</code> or
    * modified after that.
    */
   private static Object m_lock = null;

   static
   {
      m_lock = new Object();
   }

}



















