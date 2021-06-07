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

package com.percussion.install;

import com.percussion.util.IPSBrandCodeConstants.EvalTypes;
import com.percussion.util.IPSBrandCodeConstants.ServerTypes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The Code class handles Rhythmyx Installer's brand code issue. This class
 * should not use any com.percussion classes in order to avoid circular
 * dependencies. This is the first class to be build during the build
 * procedure.
 */
@SuppressWarnings("unchecked")
public class Code
{

   private static final Logger log = LogManager.getLogger(Code.class);

   /**
    * Construct a Code object. This constructor should only be used by the
    * code generator tool (com.percussion.CodeGenerator.CodeGenerator class).
    * No other class should ever use this constructor.
    *
    * @param brandCodeData contains data for generating the brand code,
    * never <code>null</code>
    * @throws IllegalArgumentException if brandCodeData is <code>null</code>
    * @throws CodeException if any error occurs parsing the Component Mapping
    * Xml file
    */
   public Code(PSBrandCodeData brandCodeData)
      throws CodeException
   {
      if (brandCodeData == null)
         throw new IllegalArgumentException("brandCodeData may not be null");
      m_brandCodeData = brandCodeData;
      init();
   }

   /**
    * Parses the input string and constructs a Code object.
    * @param strCode the brand code in string format, may not be
    * <code>null</code> or empty
    * @throws CodeException if any error occurs parsing the Component Mapping
    * Xml file
    */
   public Code(String strCode)
      throws CodeException
   {
      if ((strCode == null) || (strCode.trim().length() < 1))
         throw new IllegalArgumentException("strCode may not be null");
      init();
      fromString(strCode);
   }

   /**
    * Get the code out of the product.
    * @param rhythmyxRootDir the Rhythmyx root directory, may not be
    * <code>null</code>, this directory should exist and should be a valid
    * Rhythmyx directory
    * @throws IllegalArgumentException if rhythmyxRootDir is <code>null</code>
    * or this directory does not exist and is not a valid Rhythmyx root
    * directory or if the brand file does not exist.
    * @throws CodeException if any error occurs getting the brand code from
    * the brand file, or parsing the Component Mapping Xml file
    */
   public Code(File rhythmyxRootDir)
      throws CodeException
   {
      if (rhythmyxRootDir == null)
         throw new IllegalArgumentException("rhythmyxRootDir may not be null");
      if (!(rhythmyxRootDir.exists() && rhythmyxRootDir.isDirectory()))
         throw new IllegalArgumentException(
            "rhythmyxRootDir is not a valid Rhythmyx root directory");

      init();

      String strJarFile = rhythmyxRootDir.getAbsolutePath() + LIBDIR + BRAND_FILE;

      File jarFile = new File(strJarFile);
      if (!(jarFile.exists() && jarFile.isFile()))
         throw new IllegalArgumentException(
            "Failed to find branding file.");

      try
      {
         JarFile jar = new JarFile(jarFile);
         JarEntry jarEntry = jar.getJarEntry(JAR_ENTRY);
         if (jarEntry == null)
            throw new CodeException(getResources().getString("getbranderr"));
         String strCode = new String(jarEntry.getExtra());
         if ((strCode == null) || (strCode.trim().length() < 1))
            throw new CodeException(getResources().getString("getbranderr"));
         fromString(strCode);
      }
      catch (IOException e)
      {
         throw new CodeException(getResources().getString("unablegetbrand"));
      }
   }

   /**
    * Obtains an instance of brand code map.
    * @throws CodeException if any error occurs parsing the Component mapping
    * Xml file.
    */
   private void init() throws CodeException
   {
      m_brandCodeMap = PSBrandCodeMap.newInstance();
   }

   /**
    * Returns <code>true</code> if the component corresponding to the input
    * componentId parameter is licensed, <code>false</code> otherwise.
    * @param componentId the id of the component, should be one of the constant
    * values from <code>com.percussion.util.IPSBrandCodeConstants</code>
    * interface, should be greater than 0.
    * @return <code>true</code> if the component corresponding to the input
    * componentId parameter is licensed, <code>false</code> otherwise.
    * @throws IllegalArgumentException if componentId is less than 1
    * @throws RuntimeException if any error occurs retrieving the list of
    * licensed components
    */
   public boolean isComponentLicensed(int componentId)
   {
      try
      {
         return m_brandCodeData.isComponentLicensed(componentId);
      }
      catch (CodeException cex)
      {
         throw new RuntimeException(cex.getLocalizedMessage());
      }
   }

   /**
    * Returns the value of the property corresponding to the input
    * propertyId parameter.
    * @param propertyId the id of the property whose value is required,
    * should be one of the constant values from
    * <code>com.percussion.util.IPSBrandCodeConstants</code> interface,
    * should be non-negative.
    * @return the value of the property corresponding to the input
    * propertyId parameter.
    * @throws IllegalArgumentException if componentId is less than 0
    */
   public int getPropertyValue(int propertyId)
   {
      return m_brandCodeData.getPropertyValue(propertyId);
   }

   /**
    * Determine if this code is an evaluation code.
    * 
    * @return <code>true</code> if this code is an evaluation code,
    * <code>false</code> otherwise.
    */
   public boolean isAnEval()
   {
      return !m_brandCodeData.getProductExpires().equals(EvalTypes.NOT_EVAL);
   }

   /**
    * Returns the brand code data object.
    * @return the brand code data object,never <code>null</code>
    */
   public PSBrandCodeData getBrandCodeData()
   {
      return m_brandCodeData;
   }

   /**
    * Converts the brand code represented by this object into string format.
    * @return the brand code in string format, never <code>null</code> and
    * never empty.
    */
   @Override
   public String toString()
   {
      //------------------------------------------------------------------------
      // store the brand code map version in BITS_BRAND_CODE_MAP_VERSION bits
      int bcmv = m_brandCodeData.getBrandCodeMapVersion();
      long lProd = bcmv;
      int shiftCount = BITS_BRAND_CODE_MAP_VERSION;

      // store the server type 
      ServerTypes serverType = m_brandCodeData.getServerType();
      int iServerType = serverType.getValue();
      
      lProd |= iServerType << shiftCount;
      try
      {
         if (m_brandCodeMap.supportsExtendedProductInfo(bcmv))
            shiftCount += BITS_SERVER_TYPE_EXTENDED;
         else
            shiftCount += BITS_SERVER_TYPE;
      }
      catch (CodeException e)
      {
         // this is a bug of some sort
         throw new RuntimeException(e);
      }

      // store the product expiration in BITS_PRODUCT_EXPIRES bits
      lProd |= m_brandCodeData.getProductExpires().getValue() << shiftCount;
      
      String strProd = Long.toString(lProd, 16).toUpperCase();

      //------------------------------------------------------------------------
      // store the license id in BITS_LICENSE_ID bits
      long lLicProps = m_brandCodeData.getLicenseId();
      shiftCount = BITS_LICENSE_ID;

      // store the quantities of properties
      Iterator itProps = m_brandCodeData.getProperties().entrySet().iterator();
      while (itProps.hasNext())
      {
         Map.Entry item = (Map.Entry)itProps.next();
         String strPropId = (String)item.getKey();
         String strPropValue = (String)item.getValue();
         int propId = Integer.parseInt(strPropId);
         int propValue = Integer.parseInt(strPropValue);
         shiftCount = BITS_LICENSE_ID + (propId*BITS_PROPERTIES_ID);
         lLicProps |= propValue << shiftCount;
      }
      String strLicProps = Long.toString(lLicProps, 16).toUpperCase();

      //------------------------------------------------------------------------
      // store the parts id
      long lParts = 0;
      Iterator itParts = m_brandCodeData.getPartsId().iterator();
      while (itParts.hasNext())
      {
         String strPartId = (String)itParts.next();
         int partId = Integer.parseInt(strPartId);
         lParts |= partId;
      }
      String strParts = Long.toString(lParts, 16).toUpperCase();

      //------------------------------------------------------------------------

      GregorianCalendar codeExpire = new GregorianCalendar();
      codeExpire.setTime(m_brandCodeData.getCodeExpires());
      int yr = codeExpire.get(Calendar.YEAR) - BRAND_CODE_EXPIRE_BASE_YR;
      int month = codeExpire.get(Calendar.MONTH);
      int day = codeExpire.get(Calendar.DAY_OF_MONTH);
      int iExpire = month * 10000 + day  * 100 + yr;
      String strExpire = Integer.toString(iExpire, 16).toUpperCase();

      //------------------------------------------------------------------------

      String ret = strProd + "-" + strLicProps + "-" + strParts + "-" + strExpire;

      char[] retarray = ret.toCharArray();
      for (int i = 0 ; i <retarray.length; ++i)
      {
         if (retarray[i] != '-')
         {
            int inew = retarray[i] - '0';
            inew += 'A';
            retarray[i] = (char)inew;
         }
      }

      //------------------------------------------------------------------------

      // add the checksum
      long wCheckSum = 0;
      for (int i = 0;  i < retarray.length ; ++i)
      {
           wCheckSum += retarray[i];
      }
      String checkSum = Long.toString(wCheckSum, 16).toUpperCase();
      checkSum = checkSum.substring(checkSum.length() - 2, checkSum.length());
      String strRealReturn = new String(retarray) + "-" + checkSum;
      return strRealReturn;
   }

   /**
    * Parses the input string nto a brand code.
    * @param strCode the brand code in string format, assumed not
    * <code>null</code> and non-empty
    * @throws CodeException if the brand code is invalid
    */
   private void fromString(String strCode)
      throws CodeException
   {
      //get the 3 parts
      StringTokenizer tokens = new StringTokenizer(strCode, "-");
      if (tokens.countTokens() != 5)
         throw new CodeException(getResources().getString("invalidcode"));

      String strProduct = tokens.nextToken();
      String strLicProps = tokens.nextToken();
      String strPart = tokens.nextToken();
      String strDate = tokens.nextToken();
      String strCheckSum = tokens.nextToken();

      try
      {
         // verify check sum
         String strRet = strProduct + "-" + strLicProps + "-" + strPart + "-" + strDate;
         char retarray[] = strRet.toCharArray();
         long wCheckSum = 0;
         for (int i = 0;  i < retarray.length ; ++i)
            wCheckSum += retarray[i];

         String checkSum = Long.toString(wCheckSum, 16).toUpperCase();
         checkSum = checkSum.substring(checkSum.length() - 2, checkSum.length());
         if (!checkSum.equals(strCheckSum))
            throw new CodeException(getResources().getString("invalidcode"));

         long lProd = convertToLong(strProduct);
         long lLicProps = convertToLong(strLicProps);
         long lParts = convertToLong(strPart);
         int iExpire = (int)convertToLong(strDate);

         int bcmv = 0;
         
         bcmv = getValue(lProd, BITS_BRAND_CODE_MAP_VERSION);
         // verify brand code map version
         if (!m_brandCodeMap.isValidBrandCodeMapVersion(bcmv))
         {
            throw new CodeException(getResources().getString("invalidcode"));
         }

         boolean supportsExtended = m_brandCodeMap.supportsExtendedProductInfo(
            bcmv);
         
         lProd = lProd >> BITS_BRAND_CODE_MAP_VERSION;
         int bitsServerType = supportsExtended ? BITS_SERVER_TYPE_EXTENDED : 
            BITS_SERVER_TYPE;
            
         int serverTypeVal = getValue(lProd, bitsServerType);
         ServerTypes serverType = ServerTypes.valueOf(serverTypeVal);         
         
         lProd = lProd >> bitsServerType;
      
         int bitsEvalType = supportsExtended ? BITS_PRODUCT_EXPIRES_EXTENDED : 
            BITS_PRODUCT_EXPIRES;
         int evalTypeVal = getValue(lProd, bitsEvalType);
         EvalTypes evalType = EvalTypes.valueOf(evalTypeVal); 
         lProd = lProd >> bitsEvalType;
         
         int licenseId = getValue(lLicProps, BITS_LICENSE_ID);
         lLicProps = lLicProps >> BITS_LICENSE_ID;

         // properties
         Map propsMap = getPropertiesMap(lLicProps, 
            m_brandCodeMap.getLicenseProperties(bcmv, licenseId));

         // parts
         List partsIdList = getPartsList(lParts);

         int yr = iExpire%100;
         iExpire = (iExpire - yr)/100;
         int day = iExpire%100;
         iExpire = (iExpire - day)/100;
         int month = iExpire;
         yr += BRAND_CODE_EXPIRE_BASE_YR;

         GregorianCalendar codeExpire = new GregorianCalendar();
         codeExpire.clear(Calendar.HOUR);
         codeExpire.clear(Calendar.MINUTE);
         codeExpire.clear(Calendar.SECOND);
         codeExpire.clear(Calendar.MILLISECOND);
         codeExpire.set(Calendar.YEAR, yr);
         codeExpire.set(Calendar.MONTH, month);
         codeExpire.set(Calendar.DAY_OF_MONTH, day);

         m_brandCodeData = new PSBrandCodeData();
         m_brandCodeData.setBrandCodeMapVersion(bcmv);
         m_brandCodeData.setServerType(serverType);
         m_brandCodeData.setProductExpires(evalType);
         m_brandCodeData.setLicenseId(licenseId);
         m_brandCodeData.setCodeExpires(codeExpire.getTime());
         m_brandCodeData.setPartsId(partsIdList);
         m_brandCodeData.setProperties(propsMap);
      }
      catch (Exception ex)
      {
         throw new CodeException(ex.getLocalizedMessage());
      }
   }

   /**
    * Serializes this object's state to Xml.
    * @param doc The document to use when creating elements, may not be
    * <code>null</code>
    * @return the element containing this object's state,
    * never <code>null</code>
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      // create the root element
      Element   root = null;
      Element   el = null;
      Element   childEl = null;

      try
      {
         root = doc.createElement(EL_BRAND_CODE);
         root.setAttribute(IPSBrandCodeMap.ATTR_VALUE, toString());

         // brand code map version
         el = doc.createElement(IPSBrandCodeMap.EL_MAP);
         el.setAttribute(IPSBrandCodeMap.ATTR_VERSION,
            "" + m_brandCodeData.getBrandCodeMapVersion());
         root.appendChild(el);

         // server type - PRODUCTION or DEVELOPMENT
         el = doc.createElement(EL_SERVER_TYPE);
         String serverType = m_brandCodeData.getServerType().name();

         el.setAttribute(IPSBrandCodeMap.ATTR_VALUE, serverType);
         root.appendChild(el);

         // install type - NON-EVAL, 30 DAY EVAL, 60 DAY EVAL, 90 DAY EVAL
         el = doc.createElement(EL_INSTALL_TYPE);
         String installType = m_brandCodeData.getProductExpires().name();

         el.setAttribute(IPSBrandCodeMap.ATTR_VALUE, installType);
         root.appendChild(el);

         // properties
         el = doc.createElement(IPSBrandCodeMap.EL_PROPERTIES);
         root.appendChild(el);

         Map propsIdValueMap = m_brandCodeData.getProperties();
         Map propsIdNameMap = m_brandCodeMap.getProperties();

         Iterator it = propsIdValueMap.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry item = (Map.Entry)it.next();
            String propId = (String)item.getKey();
            String propValue = (String)item.getValue();
            String propName = (String)propsIdNameMap.get(propId);

            childEl = doc.createElement(IPSBrandCodeMap.EL_PROPERTY);
            childEl.setAttribute(IPSBrandCodeMap.ATTR_NAME, propName);
            childEl.setAttribute(IPSBrandCodeMap.ATTR_VALUE, propValue);
            el.appendChild(childEl);
         }

         // license  ATTR_NAME
         el = doc.createElement(IPSBrandCodeMap.EL_LICENSE);
         String licenseName = m_brandCodeData.getLicenseName();
         el.setAttribute(IPSBrandCodeMap.ATTR_NAME, licenseName);
         root.appendChild(el);

         // parts
         el = doc.createElement(IPSBrandCodeMap.EL_PARTS);
         root.appendChild(el);

         List partsName = m_brandCodeData.getPartsName();
         it = partsName.iterator();
         while (it.hasNext())
         {
            childEl = doc.createElement(IPSBrandCodeMap.EL_PART);
            String partName = (String)it.next();
            childEl.setAttribute(IPSBrandCodeMap.ATTR_NAME, partName);
            el.appendChild(childEl);
         }

         // components
         el = doc.createElement(IPSBrandCodeMap.EL_COMPONENTS);
         root.appendChild(el);

         it = m_brandCodeData.getLicensedComponents().values().iterator();
         while (it.hasNext())
         {
            childEl = doc.createElement(IPSBrandCodeMap.EL_COMPONENT);
            String componentName = (String)it.next();
            childEl.setAttribute(IPSBrandCodeMap.ATTR_NAME, componentName);
            el.appendChild(childEl);
         }
         return root;
      }
      catch (CodeException cex)
      {
         throw new RuntimeException(cex.getLocalizedMessage());
      }
   }

   /**
    * Converts the string to long value.
    * @param str the string to convert, assumed not <code>null</code> and
    * non-empty
    * @return the long value
    */
   private long convertToLong(String str)
   {
      char[] c = str.toCharArray();
      for (int i = 0 ; i < c.length; ++i)
      {
         int iChar = c[i];
         iChar += '0';
         iChar -= 'A';
         c[i] = (char)iChar;
      }
      return Long.parseLong(new String(c), 16);
   }

   /**
    * Returns the value of an integer composed of the last n bits of l.
    * @param l the value whose last n bits will be used to get an integer value
    * @param n the number of bits to use for obtaining the integer value
    * @return the value of an integer composed of the last n bits of l.
    */
   int getValue(long l, int n)
   {
      String str = Long.toBinaryString(l);
      int beginIndex = str.length() - n;
      if (beginIndex < 0)
         beginIndex = 0;
      String ret = str.substring(beginIndex);
      return Integer.parseInt(ret, 2);
   }

   /**
    * Returns a map containing property id as key and quantity as value
    * 
    * @param lProps the value containing the quantity of each property
    * @param propIds A list of property ids as Strings to get values for,
    * assumed not <code>null</code>.
    * @return the map containing property id as key and quantity as value, never
    * <code>null</code>
    */
   private Map getPropertiesMap(long lProps, List propIds)
   {
      Map propsMap = new HashMap();
      
      /*
       * first fill the map with zero values for each propId since if we've
       * stored zero values we'll potentially have nothing stored in the bits
       * for that index since if the higher bits are all zero, there's nothing
       * to shift to
       */
      for (Object object : propIds)
      {
         propsMap.put(object, "0");
      }
      
      int i = 0;
      while (lProps > 0)
      {
         int value = getValue(lProps, BITS_PROPERTIES_ID);
         lProps = lProps >> BITS_PROPERTIES_ID;
         String strPropId = String.valueOf(i);
         if (propIds.contains(strPropId))
            propsMap.put(strPropId, String.valueOf(value));
         i++;
      }
      return propsMap;
   }

   /**
    * Returns a list containing the id of parts.
    * @param lParts the value containing the id of parts supported by this
    * brand code
    * @return a list containing the id of parts, never <code>null</code>
    */
   private List getPartsList(long lParts)
   {
      List partsIdList = new ArrayList();
      int i = 1;
      while (i <= lParts)
      {
         if ((lParts & i) == i)
            partsIdList.add("" + i);
         i = i << 1;
      }
      return partsIdList;
   }

   /**
    * Determine whether the brand code has expired or not.
    * @return <code>true</code> if the brand code has expired,
    * <code>false</code> otherwise.
    */
   public boolean hasExpired()
   {
      return m_brandCodeData.hasExpired();
   }

   /**
    * Brand the product.
    * @param rxDirPath Rhythmyx root directory, may not be <code>null</code>,
    * should be a valid Rhythmyx root directory
    * @throws IllegalArgumentException if directory is <code>null</code>
    * or empty or this directory does not exist and is not a valid Rhythmyx root
    * directory.
    * @throws CodeException
    */
   public void brand(String rxDirPath) throws CodeException
   {
      if ((rxDirPath == null) || (rxDirPath.trim().length() < 1))
         throw new IllegalArgumentException("rxDirPath may not be null or empty");

      File rxDir = new File(rxDirPath);
      if (!(rxDir.exists() && rxDir.isDirectory()))
         throw new IllegalArgumentException(
            "rxDirPath is not a valid Rhythmyx root directory");
      String strInstallJarFile = rxDir.getAbsolutePath() + LIBDIR + JAR_FILE;

      File installJarFile = new File(strInstallJarFile);
      if (!(installJarFile.exists() && installJarFile.isFile()))
         throw new IllegalArgumentException(
            "Failed to find branding file." + strInstallJarFile);

      //we need the installation properties
      //if there are not just use the working directory
      String strCode = toString();
      // write the new brand code to the branding file
      String strBrandFile = rxDirPath  + LIBDIR  + BRAND_FILE;
      try
      {
         //need to write to tmp file then rename
         File jarFile = new File(strInstallJarFile);
         File tempjarFile = new File(strBrandFile);
         if(tempjarFile.exists())
            tempjarFile.delete();

         tempjarFile.createNewFile();

         JarFile jar = new JarFile(jarFile);
         JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(tempjarFile));
         byte[] buffer = new byte[1024];
         int bytesRead;

         JarEntry jarEntry = jar.getJarEntry(JAR_ENTRY);
         if(jarEntry != null)
            jarEntry.setExtra(strCode.getBytes());
         else
            throw new CodeException(getResources().getString("branderr"));

         for (Enumeration entries = jar.entries(); entries.hasMoreElements();)
         {
            JarEntry entry = (JarEntry)entries.nextElement();
            if(entry.getName().equals(JAR_ENTRY))
            {
               entry = jarEntry;
            }
            InputStream entryStream = jar.getInputStream(entry);
            tempJar.putNextEntry(entry);
            while((bytesRead = entryStream.read(buffer)) != -1)
            {
               tempJar.write(buffer, 0, bytesRead);
            }
         }
         tempJar.close();
      }
      catch (IOException e)
      {
         throw new CodeException(getResources().getString("unabletobrand"));
      }
   }


   /**
    * Returns the resource bundle.
    * @return the resource bundle, never <code>null</code>.
    */
   private static ResourceBundle getResources()
   {
      try
      {
         if (ms_res == null)
            ms_res = ResourceBundle.getBundle(
               "com.percussion.install.RxInstaller", Locale.getDefault());
      }
      catch(MissingResourceException mre)
      {
         log.error(mre.getMessage());
         log.debug(mre.getMessage(), mre);
      }
      return ms_res;
   }

   /**
    * Returns the path to the branding file.
    * @param rxRootDir the Rhythmyx root directory, may not be
    * <code>null</code> or empty
    * @return the path to the branding file, never <code>null</code> or empty.
    * @throws IllegalArgumentException if rxRootDir is <code>null</code> or empty
    */
   public static String getBrandFileName(String rxRootDir)
   {
      if ((rxRootDir == null) || (rxRootDir.trim().length() < 1))
         throw new IllegalArgumentException("rxRootDir may not be null or empty");
      return (rxRootDir + LIBDIR + BRAND_FILE);
   }


   /**
    * Constants used while serializing this object's state to Xml.
    */
   public static final String EL_BRAND_CODE = "brandCode";
   public static final String EL_SERVER_TYPE = "serverType";
   public static final String EL_INSTALL_TYPE = "installType";



   public static final String LIBDIR = "/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib/";
   /**
    * base yr used for calculating the yr in which the brand code should expire
    */
   public static final int BRAND_CODE_EXPIRE_BASE_YR = 2003;

   /**
    * Constants for the bits sizes.
    */
   private static final int BITS_BRAND_CODE_MAP_VERSION = 6;
   private static final int BITS_SERVER_TYPE = 2;
   private static final int BITS_SERVER_TYPE_EXTENDED = 6;
   private static final int BITS_PRODUCT_EXPIRES = 2;
   private static final int BITS_PRODUCT_EXPIRES_EXTENDED = 6;
   private static final int BITS_LICENSE_ID = 6;
   private static final int BITS_PROPERTIES_ID = 6;

   /**
    * jar entry used for storing the brand code in the extra part
    */
   private static final String JAR_ENTRY =
      "com/percussion/install/RxDesignerOnlyPostInstaller.class";

   /**
    * installer jar file, required for creating the <code>BRAND_FILE</code>
    */
   private static final String JAR_FILE = "rxinstall.jar";

   /**
    * name of the brand file. the brand code is stored in this file.
    */
   private static final String BRAND_FILE = "psinstaller.exe";

   /**
    * Stores the resource bundle, initialized in the <code>getResources</code>
    * method, never <code>null</code> after initialization.
    */
   private static ResourceBundle ms_res = null;

   /**
    * Stores the data for generating the brand code, initialized in the
    * constructor, never <code>null</code> after initialization.
    */
   private PSBrandCodeData m_brandCodeData = null;

   /**
    * In memory representation of the Component Map Xml, initialized in the
    * constructor, never <code>null</code> after initialization.
    */
   private IPSBrandCodeMap m_brandCodeMap = null;

}
