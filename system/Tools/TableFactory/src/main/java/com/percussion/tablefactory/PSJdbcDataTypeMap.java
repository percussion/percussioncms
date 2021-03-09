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
package com.percussion.tablefactory;

import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class is used to map a set of Jdbc data types to a set of Native dbms
 * data types.
 */
public class PSJdbcDataTypeMap
{
   /**
    * Creates an instance of this class using the supplied document and
    * attributes.  Loads mappings from the first DataTypeMap element found in
    * the document that matches the attributes supplied.  If a parameter is
    * <code>null</code> or empty, then the corresponding attribute on the
    * DataType element is not considered when locating a match.
    *
    * @param doc An Xml Document conforming to the DataTypeMaps DTD defined in
    *    the datatypemap.dtd.  May not be <code>null</code>.
    * @param dbAlias Attribute on the DataTypeMap specifying the backend
    *    alias and used to determine which DataTypeMap element to load from the
    *    doc.  May be <code>null</code> or empty, but either this param or
    *    driver must not be <code>null</code> or empty.  Comparison on this
    *    value is case insensitive.
    * @param driver Additional attribute on the DataTypeMap to use to load a map
    *    for a particular Jdbc driver. May be <code>null</code> or empty, but
    *    either this param or dbAlias must not be <code>null</code> or empty.
    *    May be a semi-colon delimited list.
    * @param os Additional attribute on the DataTypeMap to use to load a map for
    *    a particular operating system.  May be <code>null</code> or empty, in
    *    which case this attribute is not considered when selecting a
    *    DataTypeMap. May be a semi-colon delimited list.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSJdbcTableFactoryException if any errors occur processing the
    * document.
    */
   public PSJdbcDataTypeMap(Document doc, String dbAlias, String driver,
      String os) throws PSJdbcTableFactoryException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // be sure we have at least dbAlias or driver provided.
      if (dbAlias == null)
         dbAlias = "";
      if (driver == null)
         driver = "";
      if (driver.equals("") && dbAlias.equals(""))
         throw new IllegalArgumentException(
            "either dbAlias or driver must not be null or empty");

      m_driver = driver;
      initData( doc, dbAlias, driver, os );

   }

   /**
    * Provides common initialization functionality for the constructors.
    * Populates the fields of this instance using the supplied document and
    * attributes.  Loads mappings from the first DataTypeMap element found in
    * the document that matches the attributes supplied.  If a parameter is
    * <code>null</code> or empty, then the corresponding attribute on the
    * DataType element is not considered when locating a match.
    * <p>
    * Assumes all parameters are valid.  See {@link #PSJdbcDataTypeMap(Document,
    * String, String, String)} for a description of parameters.
    *
    * @throws PSJdbcTableFactoryException if any errors occur processing the
    * document.
    */
   private void initData(Document doc, String dbAlias, String driver, String os)
      throws PSJdbcTableFactoryException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element root = doc.getDocumentElement();
      tree.setCurrent(root);
      String rootName = root.getNodeName();
      if (!rootName.equals(ROOT_EL))
      {
         Object[] args = {ROOT_EL, rootName};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // Walk the maps and select the first match we find
      Element map = tree.getNextElement(MAP_EL,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (map == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, MAP_EL);

      boolean found = false;
      while (map != null)
      {
         // attempt to match on an attribute only if it is provided.
         boolean isMatch = true;
         if (!dbAlias.equals("") && !dbAlias.equalsIgnoreCase(
            map.getAttribute(DB_ATTR)))
         {
            isMatch = false;
         }

         if (isMatch && !driver.equals(""))
         {
            // this may be a list
            String driverAttr = map.getAttribute(DRIVER_ATTR);
            if (driverAttr.trim().length() == 0)
               isMatch = false;
            else
            {
               List drivers = PSStringOperation.getSplittedList(driverAttr,
                  ';');
               if (!drivers.contains(driver))
                  isMatch = false;
            }
         }

         if (isMatch && os != null && !os.equals(""))
         {
            // this may be a list
            String osAttr = map.getAttribute(OS_ATTR);
            if (osAttr.trim().length() == 0)
               isMatch = false;
            else
            {
               List osList = PSStringOperation.getSplittedList(osAttr, ';');
               if (!osList.contains(os))
                  isMatch = false;
            }
         }

         // if we still match, we're done
         if (isMatch)
         {
            found = true;
            break;
         }

         // didn't match, try the next one
         map = tree.getNextElement(MAP_EL,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);

      }

      // did we find any match?
      if (!found)
      {
         Object[] args = {dbAlias, driver, os};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.DATA_TYPE_MAP_NOT_FOUND, args);
      }
        setMaxIndexColSize(map);
        setCreateForeignKeyIndexes(map);   
       // We have our map, load each type mapping
      Map typeMap = new HashMap();
      Element mapping = tree.getNextElement( PSJdbcDataTypeMapping.NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN );
      if (mapping == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL, 
            PSJdbcDataTypeMapping.NODE_NAME);

      while (mapping != null)
      {
         PSJdbcDataTypeMapping dataType = new PSJdbcDataTypeMapping( mapping );
         typeMap.put( dataType.getJdbc(), dataType );

         // get the next type mapping
         mapping = tree.getNextElement( PSJdbcDataTypeMapping.NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      // now process the map
      processMappings(typeMap);
   }
   
   /**
    * Loads "maxIndexColSize" mapping from PSJdbcDataTypeMaps.xml 
    * 
    *@param map The element from which to get this object's attribute. May be <code>null</code> or empty.
    * 
    * @throws TableFactoryException if error occurs
    */

    private void setMaxIndexColSize(Element map) throws PSJdbcTableFactoryException
   {
      String sTemp = map.getAttribute(MAXINDEXCOLSIZE_EL);
      if (sTemp != null && sTemp.trim().length() > 0)
      {
          try
          {
            m_maxIndexColSize = Long.parseLong(sTemp);
          }
          catch (Exception e)
          {
              Object[] args =
              {ROOT_EL, MAXINDEXCOLSIZE_EL, sTemp};
              throw new PSJdbcTableFactoryException(IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
          }
      }
   }
    
   /**
    * Loads "createForeignKeyIndexes" mapping from PSJdbcDataTypeMaps.xm
    * 
    * @param map The element from which to get this object's attribute. May be <code>null</code> or empty.
    * 
    * @throws TableFactoryException if error occurs
    */

    private void setCreateForeignKeyIndexes(Element map) throws PSJdbcTableFactoryException
   {
      String sTempIndex = map.getAttribute(CREATEFOREIGNKEYINDEXES);
      if (sTempIndex == null || sTempIndex.trim().length() == 0 || !sTempIndex.equalsIgnoreCase("yes"))
          return;
      m_createforeignkeyindexes = true;
   }

   /**
    * Convenience version of {@link #PSJdbcDataTypeMap(Document, String, String,
    * String)} that loads the document from a default mappings Xml document as
    * a resource in this class's package.
    *
    * @throws IOException if resource cannot be loaded.
    * @throws SAXException if the xml document cannot be parsed.
    * @see #DEFAULT_MAP_FILE_NAME
    */
   public PSJdbcDataTypeMap(String dbAlias, String driver, String os)
      throws PSJdbcTableFactoryException, IOException, SAXException
   {
      // be sure we have at least dbAlias or driver provided.
      if (dbAlias == null)
         dbAlias = "";
      if (driver == null)
         driver = "";
      if (driver.equals("") && dbAlias.equals(""))
         throw new IllegalArgumentException(
            "either dbAlias or driver must not be null or empty");

      try(InputStream stream = PSJdbcDataTypeMap.class.getResourceAsStream(
         DEFAULT_MAP_FILE_NAME)) {
         // fix Rx-02-01-0003 by checking for null result
         if (null == stream)
            throw new IOException("Could not load default mapping XML document");

         m_driver = driver;
         Document doc = PSXmlDocumentBuilder.createXmlDocument(stream, false);
         initData(doc, dbAlias, driver, os);
      }
   }

   /**
    * Constructs an instance of this class using the supplied mappings.
    *
    * @param mappings A Map of Jdbc types to Native types.  Both are supplied
    * as Strings.  May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if mappings is <code>null</code> or
    * empty.
    * @throws PSJdbcTableFactoryException if any errors occur processing the
    * Map.
    */
   public PSJdbcDataTypeMap(Map mappings) throws PSJdbcTableFactoryException
   {
      if (mappings == null || mappings.isEmpty())
         throw new IllegalArgumentException(
            "mappings may not be null or empty");

      processMappings(mappings);
   }

   /**
    * Gets the data type mapping information for the specified JDBC type.
    *
    * @param jdbcString the JDBC type, not <code>null</code> or empty.
    * @return the data type mapping or <code>null</code> if no mapping found.
    * @throws IllegalArgumentException if jdbcString is <code>null</code> or
    * empty.
    */
   public PSJdbcDataTypeMapping getMapping(String jdbcString)
   {
      if (jdbcString == null || jdbcString.trim().length() == 0)
         throw new IllegalArgumentException(
            "jdbcString may not be null or empty");

      return (PSJdbcDataTypeMapping)m_strJdbc2NativeMap.get(jdbcString);
   }
   
   /**
    * Gets the data type mapping information for the specified JDBC type.
    *
    * @param jdbcType the JDBC type
    * @return the data type mapping or  <code>null</code> if no mapping found.
    */
   public PSJdbcDataTypeMapping getMapping(int jdbcType)
   {
      return (PSJdbcDataTypeMapping)m_intJdbc2NativeMap.get(new Integer(jdbcType));
   }

   /**
    * Given the Jdbc type as a String, returns the Native type as a String.
    *
    * @param jdbcString The jdbc type as a String.  May not be <code>null</code>
    * or empty.
    *
    * @return The native type as a String, <code>null</code> if no mapping
    * found, never empty.
    *
    * @throws IllegalArgumentException if jdbcString is <code>null</code> or
    * empty.
    */
   public String getNativeString(String jdbcString)
   {
      if (jdbcString == null || jdbcString.trim().length() == 0)
         throw new IllegalArgumentException(
            "jdbcString may not be null or empty");

      String nativeString = null;
      PSJdbcDataTypeMapping dataType =
         (PSJdbcDataTypeMapping) m_strJdbc2NativeMap.get(jdbcString);
      if (dataType != null)
         nativeString = dataType.getNative();
      return nativeString;
   }

   /**
    * Given the Jdbc type as an int, returns the Native type as a String.
    *
    * @param jdbcType the JDBC type
    * @return The native type as a String, <code>null</code> if no mapping
    * found, never empty.
    */
   public String getNativeString(int jdbcType)
   {
      String nativeString = null;
      PSJdbcDataTypeMapping dataType =
         (PSJdbcDataTypeMapping) m_intJdbc2NativeMap.get(new Integer(jdbcType));
      if (dataType != null)
         nativeString = dataType.getNative();
      return nativeString;
   }

   /**
    * Given the Native type as a String, returns the Jdbc type as a String.
    *
    * @param nativeString The native type as a String.  May not be <code>null
    * </code> or empty.
    *
    * @return The jdbc type as a String, <code>null</code> if no mapping
    * found, never empty.
    *
    * @throws IllegalArgumentException if nativeString is <code>null</code> or
    * empty.
    */
   public String getJdbcString(String nativeString)
   {
      if (nativeString == null || nativeString.trim().length() == 0)
         throw new IllegalArgumentException(
            "nativeString may not be null or empty");

      String jdbcString = null;
      
      // take the last one with flag set, else first one found - this is to 
      // preserve the old behavior from when we did not store a list of
      // mappings, but stored only one mapping for each native type, as we
      // are doing our best to avoid breaking any existing code.
      List mappingList = (List)m_native2JdbcMap.get(nativeString.toUpperCase());
      if (mappingList != null)
      {
         PSJdbcDataTypeMapping dataTypeMapping = null;
         Iterator mappings = mappingList.iterator();
         while (mappings.hasNext())
         {
            PSJdbcDataTypeMapping test = (PSJdbcDataTypeMapping)mappings.next();
            if (dataTypeMapping == null)
               dataTypeMapping = test;
            else if (!dataTypeMapping.isNative2Jdbc() && test.isNative2Jdbc())
               dataTypeMapping = test;
         }
         
         if (dataTypeMapping != null)
            jdbcString = dataTypeMapping.getJdbc();
      }
         
      return jdbcString;
   }

   /**
    * Convenience version that calls 
    * {@link #getJdbcType(String, String, String, int) 
    * getJdbcType(nativeString, null, null, defaultJdbcType)}
    */
   public int getJdbcType(String nativeString, int defaultJdbcType)
      throws PSJdbcTableFactoryException
   {
      return getJdbcType(nativeString, null, null, defaultJdbcType);
   }
   
   /**
    * Finds the data type mapping (<code>PSJdbcDataTypeMapping</code>) object
    * for the specified native data type <code>nativeString</code>. If this
    * mapping returns <code>true</code> from <code>isNative2Jdbc()</code>
    * method then this method returns the jdbc data type specified in this 
    * mapping, matching on size and scale if provided.
    * If no data type mapping exists for the specified native data type
    * <code>nativeString</code> or the mapping does not return true from the
    * <code>isNative2Jdbc()</code> method then it returns
    * <code>defaultJdbcType</code>
    * <p>
    * @param nativeString the native data type, may not be <code>null</code>
    * or empty.
    * @param size The size to match on, may be <code>null</code> or empty to
    * exclude it as matching criteria.
    * @param scale The scale to match on, may be <code>null</code> or empty to
    * exclude it as matching criteria.
    * @param defaultJdbcType the jdbc data type to return in case no mapping
    * exists for the specified native data type <code>nativeString</code> or
    * if the mapping does not return <code>true</code> from the
    * <code>isNative2Jdbc()</code> method
    *
    * @return Returns the jdbc data type for the specified native data type
    * if the corresponding <code>PSJdbcDataTypeMapping</code> object returns
    * <code>true</code> from the <code>isNative2Jdbc()</code> method, otherwise
    * returns the default jdbc data type <code>defaultJdbcType</code>
    *
    * @throws IllegalArgumentException if <code>nativeString</code> is
    * <code>null</code> or empty
    * @throws PSJdbcTableFactoryException if an error occurs converting the
    * jdbc data type from the string to integer format
    *
    * @see PSJdbcDataTypeMapping#isNative2Jdbc()
    */
   public int getJdbcType(String nativeString, String size, String scale, 
      int defaultJdbcType) throws PSJdbcTableFactoryException
   {
      if (nativeString == null || nativeString.trim().length() == 0)
         throw new IllegalArgumentException(
            "nativeString may not be null or empty");

      int type = defaultJdbcType;
      
      boolean useSize = size != null && size.trim().length() > 0;
      boolean useScale = scale != null && scale.trim().length() > 0;
      
      // find the mapping with the native2jdbc flag set that is exact match on
      // precision and scale if defined.  If not found, use the last mapping
      // with the flag set - this is to preserve the old behavior from when we 
      // did not store a list of mappings, but stored only one mapping for each 
      // native type.  Yes, this is contrary to the "first one found" documented
      // in the datatypemap.dtd, but the behavior is actually not truly 
      // predicable, and we are doing our best to avoid breaking any existing
      // code.      
      List mappingList = (List)m_native2JdbcMap.get(nativeString.toUpperCase());
      if (mappingList != null)
      {
         PSJdbcDataTypeMapping dataTypeMapping = null;
         Iterator mappings = mappingList.iterator();
         while (mappings.hasNext())
         {
            PSJdbcDataTypeMapping test = (PSJdbcDataTypeMapping)mappings.next();
            if (!test.isNative2Jdbc())
               continue;

            dataTypeMapping = test;
            
            // if not using size and scale, don't match so we get the last one
            // with the flag set
            String testsize = test.getDefaultSize();
            String testscale = test.getDefaultScale();
            boolean sizeMatch = useSize && size.equals(testsize);
            boolean scaleMatch = useScale && scale.equals(testscale);
               
            // keep going to take last one with flag if no exact match
            if (sizeMatch && scaleMatch)
               break;
         }
         
         // if we found one, use it, otherwise allow default to return
         if (dataTypeMapping != null)
         {
            String jdbcString = dataTypeMapping.getJdbc();
            type = convertJdbcString(jdbcString);
         }  
      }
      
      return type;
   }


   /**
    * Converts a jdbc type from a String representation to an integer.
    *
    * @param jdbcType The jdbc type as a String.  May not be <code>null</code>
    * or empty.
    *
    * @return The jdbc type represented as an integer.
    *
    * @throws  PSJdbcTableFactoryException if jdbcString is <code>null</code> or
    * empty, or if the provided string cannot be converted.
    */
   public int convertJdbcString(String jdbcString)
      throws PSJdbcTableFactoryException
   {
      if (jdbcString == null || jdbcString.trim().length() == 0)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.JDBC_INT_DATA_TYPE_CONVERSION,
            jdbcString == null ? "null" : jdbcString);

      Integer jdbcType = (Integer)m_jdbcString2Int.get(jdbcString);
      if (jdbcType == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.JDBC_STRING_DATA_TYPE_CONVERSION, jdbcString);

      return jdbcType.intValue();
   }


   /**
    * Converts a jdbc type from a integer to its String representation.
    *
    * @param jdbcType The jdbc type as an integer.
    *
    * @return The jdbc type represented as an String.  Never <code>null</code>
    * or empty.
    *
    * @throws PSJdbcTableFactoryException if the provided type cannot be
    * converted.
    */
   public String convertJdbcType(int jdbcType)
      throws PSJdbcTableFactoryException
   {
      String jdbcString = (String)m_jdbcInt2String.get(new Integer(jdbcType));
      if (jdbcString == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.JDBC_INT_DATA_TYPE_CONVERSION,
               String.valueOf(jdbcType));

      return jdbcString;
   }

   /**
    * Returns the name of the Jdbc driver.
    *
    * @return The name of the jdbc driver, never <code>null</code>, may be empty.
    */
   public String getDriver()
   {
      return m_driver;
   }
   /**
    * Get the maximum size for any column that may be included in an index 
    * definition, see {@link #setMaxIndexColSize(int)} for more info.
    * 
    * @return The size, either -1 (unlimited) or greater than zero.
    */
   public long getMaxIndexColSize()
   {
      return m_maxIndexColSize;
   }
   
   /**
    * Checks if CreateForeignKeyIndexes is turned on. Returns <code>true</code> if
    * {@link #m_createforeignkeyindexes} returns <code>true</code>, else returns <code>false</code>
    * 
    * @return <code>true<code> if createForeignkeyIndexes="yes" 
    */
   
   boolean isCreateForeignKeyIndexes()
   {
      return m_createforeignkeyindexes;
   }

    /**
     * Set the maximum size for any column that may be included in an index
     * definition. Defaults to -1 (unlimited) if not specified. Used to avoid
     * creating indexes that exceed the backend's maximum maximum allowable size
     * of combined index column values. Applies only to columns that contain
     * character data (), and specifies the number of characters.
     * 
     * @param maxSize The maximum size, pass -1 if the size should not be
     *            limited.
     */
   public void setMaxIndexColSize(long maxSize)
    {
        if (maxSize <= 0 && maxSize != -1)
            throw new IllegalArgumentException("invalid maxSize");

        m_maxIndexColSize = maxSize;
    }

  /**
    * Validates the contents of the Map, and stores the mappings in all internal
    * maps for easy retrieval.
    *
    * @param mappings The map of type mappings. The key is the jdbc type as a
    *    String, and the value is the native type as a String.  Assumed not
    *    <code> null</code> or empty.
    *
    * @throws PSJdbcTableFactoryException if there are any errors processing the
    * map.
    */
   private void processMappings(Map mappings) throws PSJdbcTableFactoryException
   {
      // store the mappings in the string map
      m_strJdbc2NativeMap = new HashMap(mappings);

      // walk the mappings, and add to each of the other maps
      Iterator entries = mappings.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         String jdbcStr = (String)entry.getKey();
         PSJdbcDataTypeMapping dataType = 
            (PSJdbcDataTypeMapping)entry.getValue();

         Integer jdbcType = (Integer)m_jdbcString2Int.get(jdbcStr);
         if (jdbcType == null)
         {
            try
            {
               jdbcType = new Integer(Types.class.getField(jdbcStr).getInt(
                  null));
            }
            catch (Exception e)
            {
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.INVALID_DATA_TYPE_MAPPING,
                  new Object[] {jdbcStr, dataType.getNative()} );
            }
            m_jdbcString2Int.put(jdbcStr, jdbcType);
            m_jdbcInt2String.put(jdbcType, jdbcStr);
         }

         m_intJdbc2NativeMap.put(jdbcType, dataType);

         // build map of native to list of datatypes to use for 
         // native->jdbc calculations.   
         String nativeType = dataType.getNative().toUpperCase();
         List dataTypeList = (List)m_native2JdbcMap.get(nativeType);
         if (dataTypeList == null)
         {
            dataTypeList = new ArrayList();
            m_native2JdbcMap.put(nativeType, dataTypeList);
         }
         dataTypeList.add(dataType);
      }
   }

   /**
    * Name of the default mappings file loaded as a resource from this class's
    * package.
    */
   public static final String DEFAULT_MAP_FILE_NAME = "PSJdbcDataTypeMaps.xml";

   /**
    * Map of jdbc type as a String to native type as a String.  Initialized in
    * the ctor, never <code>null</code>, empty, or modified after that.
    */
   private Map m_strJdbc2NativeMap = null;

   /**
    * Map of jdbc type as an Integer to native type as a String.  Initialized in
    * the ctor, never <code>null</code>, empty, or modified after that.
    */
   private Map m_intJdbc2NativeMap = new HashMap();

   /**
    * Map for storing the native type (<code>String</code>) as key and a 
    * <code>List</code> of <code>PSJdbcDataTypeMapping</code> objects as value. 
    * Initialized in the ctor, never <code>null</code>, empty, or modified after 
    * that.
    */
   private Map m_native2JdbcMap = new HashMap();

   /**
    * Map of Jdbc types as Integers mapped to the String representation.
    * Initialized in the ctor, never <code>null</code>, empty, or modified after
    * that.
    */
   private static Map m_jdbcInt2String = new HashMap();

   /**
    * Map of Jdbc types as Strings mapped to the Integer representation.
    * Initialized in the ctor, never <code>null</code>, empty, or modified after
    * that.
    */
   private static Map m_jdbcString2Int = new HashMap();

   /**
    * The name of the Jdbc driver.  Initialized in the ctor, never <code>null
    * </code>, may be empty.
    */
   private String m_driver = "";

   /** 
    * The maximum column size for indexable columns, see 
    * {@link #setMaxIndexColSize(long)} for more info. Initialized during
    * construction, -1 if not specified. 
    */
   private long m_maxIndexColSize = -1;
   
    /**
     * Flag to check if the condition to create indexes with foreign key columns
     * is set. Look at {@link #setCreateForeignKeyIndexes(map)} for more
     * info. Default value is false.
     */

   private boolean m_createforeignkeyindexes = false;

   // xml constants
   private static final String ROOT_EL = "DataTypeMaps";
   private static final String MAP_EL = "DataTypeMap";
   private static final String DB_ATTR = "for";
   private static final String DRIVER_ATTR = "driver";
   private static final String OS_ATTR = "os";
   private static final String MAXINDEXCOLSIZE_EL = "maxIndexColSize";
   private static final String CREATEFOREIGNKEYINDEXES = "createForeignkeyIndexes";
}

