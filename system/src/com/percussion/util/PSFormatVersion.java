/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.util;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.PSConsole;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * This class provides functionality to get different parts/formats of this
 * JAR version.
 */
////////////////////////////////////////////////////////////////////////////////
public class PSFormatVersion
{
   /**
    * Constructor
    *
    * @param packageName the package from which we use the resources
    */
   //////////////////////////////////////////////////////////////////////////////
   public PSFormatVersion(String packageName)
   {
      this();

      ResourceBundle bundle = getResources(packageName);
      if (!(bundle == null))
      {
         m_buildNumber = bundle.getString(KEY_BUILD_NUMBER);
         m_buildId = Integer.parseInt(bundle.getString(KEY_BUILD_ID));
         m_majorVersion = Integer.parseInt(bundle.getString(KEY_MAJOR_VERSION));
         m_minorVersion = (Integer.parseInt(bundle.getString(KEY_MINOR_VERSION)));
         m_microVersion = Integer.parseInt(bundle.getString(KEY_MICRO_VERSION));
         m_versionString = bundle.getString(KEY_VERSION_STRING);
         m_interfaceVersion = (Integer.parseInt(
            bundle.getString(KEY_INTERFACE_VERSION)));
         m_optionalId = bundle.getString(KEY_OPTIONAL_ID);
         m_displayVersion = bundle.getString(KEY_DISPLAY_VERSION);
      }
   }

    /***
     * Load Version.properties from resource file.
     * @param clazz
     * @param path
     */
   public PSFormatVersion(Class clazz, String path ){

       Properties props = new Properties();
       try {
           props.load(clazz.getResourceAsStream(path));
       } catch (IOException e) {
           e.printStackTrace();
       }

       m_buildNumber = props.getProperty(KEY_BUILD_NUMBER);
       m_buildId = Integer.parseInt(props.getProperty(KEY_BUILD_ID));
       m_majorVersion = Integer.parseInt(props.getProperty(KEY_MAJOR_VERSION));
       m_minorVersion = (Integer.parseInt(props.getProperty(KEY_MINOR_VERSION)));
       m_microVersion = Integer.parseInt(props.getProperty(KEY_MICRO_VERSION));
       m_versionString = props.getProperty(KEY_VERSION_STRING);
       m_interfaceVersion = (Integer.parseInt(
               props.getProperty(KEY_INTERFACE_VERSION)));
       m_optionalId = props.getProperty(KEY_OPTIONAL_ID);
       m_displayVersion = props.getProperty(KEY_DISPLAY_VERSION);
   }

   /**
    * Private Constructor called from static factory method { createFromXml() createFromXml}
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private PSFormatVersion()
   {
   }
   
   /**
    * Returns the plain version in the format major.minor.micro
    * (1.0.0, 2.4.1, ...).
    * 
    * @return String the plain version string in the proper format.
    */
   /////////////////////////////////////////////////////////////////////////////
   public String getVersion()
   {
      return m_displayVersion;
   }

   
   
  /**
   * Returns the plain build identifier. This number uniquely identifies a
   * build. However, most people will use the build date to identify the build.
   *
   * @return String the plain build number
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getBuildId()
  {
      return Integer.toString(m_buildId);
  }

  /**
   * Returns the  build number, in the 8 digit form YYYYMMNN. NN is a counter.
   *
   * @return String the build number
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getBuildNumber()
  {
      return m_buildNumber;
  }

  /**
   * Returns the optional id for the build.
   *
   * @return String the optional id
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getOptionalId()
  {
      return m_optionalId;
  }

  /**
   * Returns the plain build date as a <code>Date</code> object.
   *
   * @return String the plain build date, <code>null</code> if the date is
   * invalid or not set.
   */
  //////////////////////////////////////////////////////////////////////////////
  public Date getBuildDate()
  {
      Date dt = null;
      try
      {
         FastDateFormat fmt = FastDateFormat.getInstance( "yyyyMMDD" );

         dt = fmt.parse( m_buildNumber );
      }
      catch ( ParseException e )
      {
         // ignore, returning null
      }
      return dt;
  }

  /**
   * Returns a complete version string.
   * <p>
   * <pre>
   *  Examples:
   *    Version 1.2  Build 200311R01 (257)
   *    Version 1.2  Build 200311X01 (257)
   *    Version 1.2  Build 200311B01 (257)
   *    Version 1.2  Build 200311Q01 (257)
   *    Version 1.2  Build 200311T01 (257) [Relationships]
   *    Version 1.2  Build 200311P01 (257) [Rx-03-01-0123]
   *
   *  </pre>
   * </p>
   * @return String the version string
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getVersionString()
  {
     StringBuffer sb = new StringBuffer();
     String exportString = "";

     try
     {
        Class.forName("com.percussion.legacy.security.deprecated.PSDESKey");
     }
     catch (ClassNotFoundException e)
     {
        exportString = " E";
     }

        String buildType = m_versionString.toUpperCase();
        sb.append("Version ");
        sb.append(getVersion());
        sb.append(exportString);
        sb.append("  Build ");
        sb.append(getBuildNumber().substring(0, 6));
        sb.append(ms_buildTypeMap.get(buildType));
        sb.append(getBuildNumber().substring(6));
        sb.append(" (");
        sb.append(getBuildId());
        sb.append(")");

        if(buildType.equals("TEST") || buildType.equals("PATCH"))
        {
           sb.append(" [");
           sb.append(getOptionalId());
           sb.append("]");
        }

     return sb.toString();

  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSFormatVersion)) return false;
        PSFormatVersion that = (PSFormatVersion) o;
        return m_buildId == that.m_buildId &&
                m_majorVersion == that.m_majorVersion &&
                m_minorVersion == that.m_minorVersion &&
                m_microVersion == that.m_microVersion &&
                m_interfaceVersion == that.m_interfaceVersion &&
                Objects.equals(m_buildNumber, that.m_buildNumber) &&
                Objects.equals(m_versionString, that.m_versionString) &&
                Objects.equals(m_optionalId, that.m_optionalId) &&
                Objects.equals(m_displayVersion, that.m_displayVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_buildNumber, m_buildId, m_majorVersion, m_minorVersion, m_microVersion, m_versionString, m_optionalId, m_interfaceVersion, m_displayVersion);
    }

    /**
   * Returns the major version number.
   *
   * @return int the major version number
   */
  //////////////////////////////////////////////////////////////////////////////
  public int getMajorVersion()
  {
     return m_majorVersion;
  }

  /**
   * Returns the minor version number.
   *
   * @return int the minor version number
   */
  //////////////////////////////////////////////////////////////////////////////
  public int getMinorVersion()
  {
     return m_minorVersion;
  }

  /**
   * Returns the minor version number.
   *
   * @return int the minor version number
   */
  //////////////////////////////////////////////////////////////////////////////
  public int getMicroVersion()
  {
       return m_microVersion;
  }

   //////////////////////////////////////////////////////////////////////////////
   protected ResourceBundle getResources(String packageName)
   {
      try
      {
         if (m_res == null)
            m_res = ResourceBundle.getBundle(packageName + ".Version",
                                         Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         PSConsole.printMsg("Util", e);
      }

      return m_res;
   }

  /**
   * Returns the interface version number.
   *
   * @return int the interface version number
   * @see #m_interfaceVersion m_interfaceVersion
   */
  //////////////////////////////////////////////////////////////////////////////
  public int getInterfaceVersion()
  {
     return m_interfaceVersion;
  }
  
   /**
    * This method is called to create a PSXFormatVersion XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *    PSXFormatVersion defines all the information about the current version.
    *    --&gt;
    *    &lt;!ELEMENT PSXFormatVersion EMPTY &gt;
    *
    *    &lt;!--
    *       buildId - a monotonically increasing value that changes with every
    *                 compilation of the product
    *
    *       buildNumber - is the current build number, in the form YYYYMMNN (8
    *                     digits) Where NN is a counter.
    *
    *       majorVersion  -is the current major version portion of the version where
    *       the format is <majorVersion.minorVersion>
    *
    *       minorVersion - is the current minor version portion of the version where
    *       the format is <majorVersion.minorVersion>
    *
    *       microVersion - is the current micro version portion of the version where
    *       the format is <majorVersion.minorVersion>
    *
    *       versionString - the prefix descriptor used to build the full version string
    *
    *       optionalId - id used to identify a patch or test build
    *       
    *       displayVersion - is the version number where the format is
    *       <majorVersion.minorVersion.microVersion>
    *
    *      --&gt;
    *
    *    &lt;!ATTLIST PSXFormatVersion
    *       buildId           CDATA          #REQUIRED
    *       buildNumber       CDATA          #REQUIRED
    *       majorVersion      CDATA          #REQUIRED
    *       minorVersion      CDATA          #REQUIRED
    *       microVersion      CDATA          #REQUIRED
    *       versionString     CDATA          #REQUIRED
    *       interfaceVersion  CDATA          #REQUIRED
    *       optionalId        CDATA          #REQUIRED
    *       displayVersion    CDATA          #REQUIRED
    *    &gt;
    * </code></pre>
    *
    * @param doc the Xml document used to create this element.  If null, and
    *          IllegalArgumentException is thrown
    *
    * @return     the newly created PSXFormatVersion XML element node
    *
    * @throws  IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc cannot be null");

      Element  root = doc.createElement(NODE_TYPE);
      root.setAttribute(KEY_BUILD_ID, Integer.toString(m_buildId));
      root.setAttribute(KEY_BUILD_NUMBER, getBuildNumber());
      root.setAttribute(KEY_MAJOR_VERSION, Integer.toString(m_majorVersion));
      root.setAttribute(KEY_MINOR_VERSION, Integer.toString(m_minorVersion));
      root.setAttribute(KEY_MICRO_VERSION, Integer.toString(m_microVersion));
      root.setAttribute(KEY_VERSION_STRING, m_versionString);
      root.setAttribute(KEY_INTERFACE_VERSION,
         Integer.toString(m_interfaceVersion));
      root.setAttribute(KEY_OPTIONAL_ID, m_optionalId);
      root.setAttribute(KEY_DISPLAY_VERSION, m_displayVersion);

      return root;
   }

   /**
    * A static factory method called to create a PSFormatVersion object
    * from a PSXFormatVersion XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode an Xml Element containing the version info
    *
    * @return the newly created PSFormatVersion object initialized from
    *  the Xml Element.  if unable to create the object using the supplied
    *  Element, returns <code>null</code>.
    *
    */
   public static PSFormatVersion createFromXml(Element sourceNode)
   {
      PSFormatVersion version = null;
      try
      {
         version = new PSFormatVersion();
         version.fromXml(sourceNode);
      }
      catch(PSUnknownNodeTypeException e)
      {
         //return null
         version = null;
      }

      return version;
   }

   /**
    * This method is called to populate a PSFormatVersion Java object
    * from a PSXFormatVersion XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode a node containing the version XML Element
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXFormatVersion or is <code>null</code>
    */
   private void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      //make sure we have a source node
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, NODE_TYPE);


      //make sure we got the PSXFormatVersion type node
      if (false == NODE_TYPE.equals (sourceNode.getNodeName()))
      {
         Object[] args = { NODE_TYPE, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
             IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      m_buildNumber = getAttribute(tree, KEY_BUILD_NUMBER, true);
      m_buildId = (new Integer(
         getAttribute(tree, KEY_BUILD_ID, true))).intValue();
      m_majorVersion = (new Integer(
         getAttribute(tree, KEY_MAJOR_VERSION, true))).intValue();
      m_minorVersion = (new Integer(
         getAttribute(tree, KEY_MINOR_VERSION, true))).intValue();
      m_microVersion = (new Integer(
         getAttribute(tree, KEY_MICRO_VERSION, false, "0"))).intValue();
      m_versionString = getAttribute(tree, KEY_VERSION_STRING, true);
      m_optionalId = getAttribute(tree, KEY_OPTIONAL_ID, false);
      
      // we may not have an iterface version
      try
      {
         m_interfaceVersion = (new Integer(
            getAttribute(tree, KEY_INTERFACE_VERSION, true))).intValue();
      }
      catch (PSUnknownNodeTypeException e)
      {
         // in this case set it to zero
         m_interfaceVersion = 0;
      }
      
      // we may not have a display version
      try
      {
         m_displayVersion = getAttribute(tree, KEY_DISPLAY_VERSION, true);
      }
      catch (PSUnknownNodeTypeException e)
      {
         // in this case set it to formatted version
         m_displayVersion = getFormattedVersion();
      }
   }


   /**
    * This method is called to retrieve an attribute value
    * from an XML element node.
    *
    * @param tree   a PSXmlTreeWalker with the current node set to the element
    *               where the attribute is located.  if tree is <code>null</code>
    *               an IllegalArgumentException will be thrown.
    *
    * @param attrName the name of the attribute to search for.  if null or empty,
    *               an IllegalArgumentException will be thrown.
    *
    * @param required <code>true</code> if the attribute is required,
    * <code>false</code> otherwise
    * 
    * @param def default value used if attribute is not present or has no 
    * value, may be <code>null</code>.
    *
    * @return the value of the attribute, may be <code>null</code> or empty
    * if <code>required</code> is <code>false</code>
    *
    * @throws PSUnknownNodeTypeException if <code>required</code> is
    * <code>true</code> and the attribute is not present or is zero length
    *
    * @throws  IllegalArgumentException if tree or attrName is null or empty
    */
   private String getAttribute(PSXmlTreeWalker tree, String attrName,
      boolean required, String def) throws PSUnknownNodeTypeException
   {
      if (tree == null)
         throw new IllegalArgumentException("tree cannot be null");

      if (attrName == null)
         throw new IllegalArgumentException("attrName cannot be null");

      String sTemp = tree.getElementData(attrName, false);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         sTemp = def == null ? "" : def;
         if (required && sTemp.trim().length() == 0)
         {
            Object[] args = { NODE_TYPE, attrName,
                  ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }         
      }
      return sTemp;
   }
   
   /**
    * Convienience method that calls 
    * {@link #getAttribute(PSXmlTreeWalker, String, boolean, String)}
    */
   private String getAttribute(PSXmlTreeWalker tree, String attrName,
      boolean required) throws PSUnknownNodeTypeException
   {
      return getAttribute(tree, attrName, required, null);  
   }
   
   /**
    * Returns version number formatted as majorVersion.minorVersion.microVersion.
    */
   private String getFormattedVersion()
   {
      return m_majorVersion + "." + m_minorVersion + "." + m_microVersion;  
   }

   /**
    * Used for testing.
    */
/*
   public static void main( String [] args )
   {
      try
      {
         if ( null == args || args.length != 1 )
         {
            System.out.println( "Usage: java com.percussion.util.PSFormatVersion versionProperties" );
            System.out.println( " example: java com.percussion.util.PSFormatVersion com.percussion.E2Designer.Version" );
            System.exit(-1);
         }

         PSFormatVersion fv = new PSFormatVersion( args[0] );
         System.out.println( "str= " + fv.getVersionString());
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot( doc, "version" );
         root.appendChild( fv.toXml( doc ));
         PSXmlDocumentBuilder.write(doc, System.out);

         java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
         PSXmlDocumentBuilder.write(doc, os);
         System.out.println( "\r\n########\r\n" );

         java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream( os.toByteArray());
         doc = PSXmlDocumentBuilder.createXmlDocument( is, false );
         PSXmlDocumentBuilder.write( doc, System.out );

         System.out.println( "\r\n########\r\n" );
         System.exit(0);
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }
*/
   //////////////////////////////////////////////////////////////////////////////
   // class storage
   private ResourceBundle m_res = null;

   /**
    * the build date, in the form YYYYMMDD, always 8 digits. never <code>
    * null</code> after construction. This is used as the build number by
    * everyone outside of development.
    */
   private String m_buildNumber = null;

   /**
    * The monotonically increasing # that is different for every release
    * build. If 2 builds were created on the same day, they would have the
    * same build date, but different ids.
    */
   private int m_buildId = 0;

   /** the major version where format is <code>majorversion.minorverion</code>. */
   private int m_majorVersion = 0;

   /** the minor version where format is <code>majorversion.minorverion</code>. */
   private int m_minorVersion = 0;

   /** the micro version where format is <code>majorversion.minorversion[microversion]</code>. */
   private int m_microVersion = 0;

   /** the prefix descriptor used to build the full version string */
   private String m_versionString = null;

   /** optional id used for TEST and PATCH type builds */
   private String m_optionalId = null;

   /**
    * the interface version, monotonically incremented only when the workbench
    * and server are no longer compatible
    */
   private int m_interfaceVersion = 0;
   
   /**
    * the display version seen in dialogs, consoles, etc.
    */
   private String m_displayVersion = null;

   /**
    * Mappings of build type string to build type mnemonic.
    * Never <code>null</code> or empty.
    *<p>Mappings:</p>
    *<table border="1">
    *   <th><td>Build type</td><td>Mnemonic</td></th>
    *   <tr><td>BETA</td><td>B</tr>
    *   <tr><td>INTERNAL</td><td>X</tr>
    *   <tr><td>QA</td><td>Q</tr>
    *   <tr><td>RELEASE</td><td>R</tr>
    *   <tr><td>TEST</td><td>T</tr>
    *</table>
    */
   private static Map<String, String> ms_buildTypeMap = new HashMap<>();
   static
   {
      ms_buildTypeMap.put("ALPHA", "A");
      ms_buildTypeMap.put("BETA", "B");
      ms_buildTypeMap.put("INTERNAL", "X");
      ms_buildTypeMap.put("PATCH", "P");
      ms_buildTypeMap.put("QA", "Q");
      ms_buildTypeMap.put("RELEASE", "R");
      ms_buildTypeMap.put("TEST", "T");
   }

   //Properties keys
   private static final String KEY_BUILD_NUMBER    = "buildNumber";
   private static final String KEY_BUILD_ID        = "buildId";
   private static final String KEY_MAJOR_VERSION   = "majorVersion";
   private static final String KEY_MINOR_VERSION   = "minorVersion";
   private static final String KEY_MICRO_VERSION   = "microVersion";
   private static final String KEY_VERSION_STRING  = "versionString";
   private static final String KEY_INTERFACE_VERSION = "interfaceVersion";
   private static final String KEY_OPTIONAL_ID = "optionalId";
   private static final String KEY_DISPLAY_VERSION = "displayVersion";

    /** the element name used for the root node  */
   public static final String NODE_TYPE = "PSXFormatVersion";
}

