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

package com.percussion.install;

import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.PSEncryptor;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSFilenameFilter;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.io.PathUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static com.percussion.utils.container.IPSJdbcDbmsDefConstants.PWD_ENCRYPTED_PROPERTY;

/**
 * This class is the main class for upgrade process and its process method is
 * called from InstallShield.
 */
public class RxUpgrade
{
   /**
    * Constructor used to initialize the current upgrade date for writing log
    * files.
    */
   public RxUpgrade()
   {
      initDate();
   }
   
   /**
   * Takes the configuration file and rhythmyx root directory, creates an
   * instance of PSUpgradeConfig by passing the configuration file then loops
   * through all the valid modules and process the Plugins, Transform Files
   * and property files.
   *
   * @param rxRoot The root directory of the installed Rhythmyx, may not be
   * <code>null</code> or empty
   *
   * @param cfgDoc document with the plugins, may not be
   * <code>null</code>.
   */
   public void process(String rxRoot, Document cfgDoc)
   {
      if ((rxRoot == null) || (rxRoot.trim().length() < 1))
         throw new IllegalArgumentException("rxRoot may not be null or empty");
         
      if (cfgDoc == null)
         throw new IllegalArgumentException("cfgDoc may not be null");
      
      if (rxRoot.endsWith("/") || rxRoot.endsWith("\\"))
         rxRoot = rxRoot.substring(0, rxRoot.length()-1);
      
      m_RxRootDir = rxRoot;
      initLogFileDirs();
                
      try
      {
         execute(new PSUpgradeConfig(cfgDoc));
      }
      catch (Exception e)
      {
         RxUpgradeLog.logIt(e);
      }
   }
   
    /**
    * Takes the configuration file and rhythmyx root directory, creates an
    * instance of PSUpgradeConfig by passing the configuration file then loops
    * through all the valid modules and process the Plugins, Transform Files
    * and property files.
    *
    * @param rxRoot The root directory of the installed Rhythmyx, may not be
    * <code>null</code> or empty
    *
    * @param cfgFile Name of the configuration file, may not be
    * <code>null</code> or empty
    */
   public void process(String rxRoot, String cfgFile)
   {
      if ((rxRoot == null) || (rxRoot.trim().length() < 1))
         throw new IllegalArgumentException("rxRoot may not be null or empty");

      if ((cfgFile == null) || (cfgFile.trim().length() < 1))
      {
         throw new IllegalArgumentException(
            "upgrade config file may not be null or empty");
      }
      
      if (rxRoot.endsWith("/") || rxRoot.endsWith("\\"))
         rxRoot = rxRoot.substring(0, rxRoot.length()-1);
      
      m_RxRootDir = rxRoot;
      initLogFileDirs();
            
      try
      {
         execute(new PSUpgradeConfig(cfgFile));
      }
      catch (Exception e)
      {
         RxUpgradeLog.logIt(e);
      }
    }
   
   /**
    * Adds a response object to the response object store
    * 
    * @param response the response object to add
    */
   public static void addResponse(PSPluginResponse response)
   {
      ms_pluginResponses.add(response);
   }
   
   /**
    * This method returns the pre-upgrade plugin response objects
    * 
    * @return the pre-upgrade plugin response objects
    */
   public static ArrayList getResponses()
   {
      return ms_pluginResponses;
   }
   
   /**
    * Executes all plugin modules found in the supplied upgrade config collection.
    * @param upgradeConfig, never <code>null</code>.
    */
   private void execute(IPSUpgradeConfig upgradeConfig)
   {
      if (upgradeConfig == null)
         throw new IllegalArgumentException("upgradeConfig may not be null");
      
      //initialize plugin response object store
      ms_pluginResponses = new ArrayList();
      
      Iterator iter = null;
      IPSUpgradeModule moduleConfig = null;
      try
      {
         iter = upgradeConfig.getModuleList();

         while(iter.hasNext())
         {
            try
            {
               moduleConfig = (IPSUpgradeModule)iter.next();
               //process file transformation data
               handleTransformFiles(moduleConfig);
              
               //check for pre-upgrade
               isPreUpgrade(moduleConfig);
               
               //process file transformation data
               handlePlugins(moduleConfig);

               //process file transformation data
               handlePropertyFiles(moduleConfig);
            }
            catch(Throwable e)
            {
               RxUpgradeLog.logIt(e);
            }
            finally
            {
               if(moduleConfig != null)
               {
                  moduleConfig.close();
               }
            }
         }
      }
      catch(Throwable e)
      {
         RxUpgradeLog.logIt(e);
      }
   }

   /**
    * Handles the propertyfiles files based on the following DTD
    * <!ELEMENT propertyfiles (file*)>
    * <!ELEMENT file (variable*)>
    * <!ATTLIST file path CDATA #REQUIRED>
    * <!ATTLIST file status CDATA #REQUIRED>
    * <!ELEMENT variable (#PCDATA)>
    * <!ATTLIST variable name CDATA #REQUIRED>
    * <!ATTLIST variable action CDATA #REQUIRED>
    * <!ATTLIST variable modifyifexist CDATA #REQUIRED>
    * Accepts three kinds of actions for properties add, delete and modify
    * 1) action delete simply removes the property,
    * 2) action add adds if the property does not exist and if exists changes
    *    its value based on condition modifyifexist.
    *    modifyifexist=yes changes property value
    *    modifyifexist=no does not change property value
    * 3) action modify modifies if exists and if does not exists adds based on
    *     condition addifnotexist
    *    addifnotexist=yes adds property
    *    addifnotexist=no does not add property
    *
    * @param config, the upgrade module
    * May not be <code>null</code>.
    */
   private void handlePropertyFiles(IPSUpgradeModule config)
   {
      config.getLogStream().println("Running property file updates...");
      try
      {
         NodeList nl =
            config.getModuleElement().getElementsByTagName("propertyfiles");
         if(nl == null || nl.getLength() < 1)
         {
            config.getLogStream().println(
               "No property files needed modifications");
            return;
         }

         Element elemTranformFiles = (Element) nl.item(0);
         nl = elemTranformFiles.getElementsByTagName("file");
         Element elemFile = null;
         String targetFile = null;
         Node node = null;
         NodeList nlVariables = null;
         Element elemVariable = null;
         String varName = "";
         String varAction = "";
         String varCondition = "";
         String varValue = "";

         for(int i=0; (nl != null) && (i < nl.getLength()); i++)
         {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try
            {
               elemFile = (Element)nl.item(i);
               targetFile = getRxRoot() + elemFile.getAttribute("path");
               File testTargetFile = new File(targetFile);
               if (!testTargetFile.exists())
                  continue;

               fis = new FileInputStream(targetFile);
               Properties props = new Properties();
               props.load(fis);

               config.getLogStream().println(
                  "Processing file: " + targetFile + "...");
               nlVariables = elemFile.getElementsByTagName("variable");

               for(int j=0; nlVariables!=null && j<nlVariables.getLength(); j++)
               {
                  elemVariable = (Element)nlVariables.item(j);
                  varName = elemVariable.getAttribute("name");
                  varAction = elemVariable.getAttribute("action");
                  varValue = null;
                  node = elemVariable.getFirstChild();
                  if(node != null && node instanceof Text)
                     varValue = ((Text)node).getData();

                  if(varAction.equalsIgnoreCase(PROP_ACTION_DELETE))
                  {
                     if(props.containsKey(varName))
                        props.remove(varName);
                  }
                  else if(varAction.equalsIgnoreCase(PROP_ACTION_ADD))
                  {
                     varCondition = elemVariable.getAttribute(
                        PROP_CONDITION_MODIFYIFEXIST).trim();
                     if(!props.containsKey(varName) ||
                         varCondition.equalsIgnoreCase(YES))
                     {
                        props.put(varName, varValue);
                     }
                  }
                  else if(varAction.equalsIgnoreCase(PROP_ACTION_MODIFY))
                  {
                     varCondition = elemVariable.getAttribute(
                        PROP_CONDITION_ADDIFNOTEXIST).trim();
                     if(props.containsKey(varName) ||
                         varCondition.equalsIgnoreCase(YES))
                     {
                        props.put(varName, varValue);
                     }
                  }
                  else
                  {
                     config.getLogStream().println("Action: " + varAction +
                        " is not valid for a property variable");
                  }
               }

               File tarFile = new File(targetFile);
               if (!tarFile.canWrite())
               {
                  config.getLogStream().println(
                     "Cannot write to file: " + tarFile.getAbsolutePath());
                  config.getLogStream().println(
                     "Deleting and recreating this file");

                  tarFile.delete();
                  tarFile.createNewFile();
               }
               fos = new FileOutputStream(tarFile);
               props.store(fos, "Modified by Rhythmyx upgrader");
               elemFile.setAttribute("status", "success");
            }
            catch(Exception e)
            {
               elemFile.setAttribute("status", "failed");
               config.getLogStream().println("Warning: " + e.getMessage());
               e.printStackTrace(config.getLogStream());
            }
            finally
            {
               if (fis != null)
               {
                  try
                  {
                     fis.close();
                  }
                  catch (Exception e)
                  {
                  }
               }
               if (fos != null)
               {
                  try
                  {
                     fos.close();
                  }
                  catch (Exception e)
                  {
                  }
               }
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
      }
   }
   /**
    * Loops through all the plugins in a module and calls execute method of
    * PSUpgradePluginMgr class by passing the plugin element.
    *
    * @param config, the upgrade module
    * May not be <code>null</code>.
   */
   private void handlePlugins(IPSUpgradeModule config)
   {
      config.getLogStream().println("Running plugins...");
      try
      {
         NodeList nl = config.getModuleElement().getElementsByTagName("plugins");
         if(nl == null || nl.getLength() < 1)
         {
            config.getLogStream().println("No plugins configured");
            return;
         }
         Element elemPlugins = (Element) nl.item(0);
         nl = elemPlugins.getElementsByTagName("plugin");
         for(int i=0; nl != null && i < nl.getLength(); i++)
         {
            try
            {
               PSUpgradePluginMgr plugin =
                  new PSUpgradePluginMgr(config, (Element)nl.item(i));
               plugin.execute();
            }
            catch(Exception e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
   }
   
   /**
    * Checks if pre-upgrade plugins are about to run.  Sets flag accordingly.
    *
    * @param config, the upgrade module.
    */
   private void isPreUpgrade(IPSUpgradeModule config)
   {
      try
      {
         NodeList nl = config.getModuleElement().getElementsByTagName("preupgrade");
         if (nl != null && nl.getLength() >= 1)
            ms_bPreUpgrade = true; 
         else
            ms_bPreUpgrade = false;
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
   }

   /**
    * Handles the Transform Files based on the following DTD
    * <p>
    * &lt;!ELEMENT transformfiles (file*)>
    * &lt;!ELEMENT file (#PCDATA)>
    * &lt;!ATTLIST file path CDATA #REQUIRED>
    * &lt;!ATTLIST file transformxsl CDATA #REQUIRED>
    * &lt;!ATTLIST file status CDATA #REQUIRED>
    * &lt;!ATTLIST file type CDATA #IMPLIED>
    * <p>
    * If the file specified by the <code>path</code> attribute of the
    * <code>file</code> element is a directory then the stylesheet is applied
    * to all the XML files in the directory.
    * <p>
    * The <code>type</code> attribute of the <code>file</code> element
    * specifies the type of XML files to which the stylesheet should be
    * applied. Current only one type "ContentEditor" is supported. This
    * attribute is optional and by default the stylesheet is applied to all
    * the files in the directory. If the type is specified as "ContentEditor"
    * then the stylesheet is applied to only those XML files in the specified
    * path which contain "PSXContentEditor" element. If type is specified then
    * it must either be empty or equal (case-insensitive) "ContentEditor".
    * <p>
    * @param config, the upgrade module, assumed not <code>null</code>
    */
   private void handleTransformFiles(IPSUpgradeModule config)
   {
      config.getLogStream().println("Running transform files...");

      try
      {
         NodeList nl =
            config.getModuleElement().getElementsByTagName("transformfiles");
         if ((nl == null) || (nl.getLength() < 1))
         {
            config.getLogStream().println("No file needs transformation");
            return;
         }

         Element elemTranformFiles = (Element)nl.item(0);
         nl = elemTranformFiles.getElementsByTagName("file");
         if ((nl == null) || (nl.getLength() < 1))
         {
            config.getLogStream().println(
               "transformfiles element contains no file element");
            return;
         }

         int length = nl.getLength();
         for (int i=0; i < length; i++)
         {
            try
            {
               Element elemFile = (Element)nl.item(i);
               String dtd = elemFile.getAttribute("DTD");
               String xslTransform = getUpgradeRoot() +
                  elemFile.getAttribute("transformxsl");

               Iterator it = getXmlFiles(elemFile.getAttribute("path"),
                  elemFile.getAttribute("type"),
                  config.getLogStream());
               while (it.hasNext())
               {
                  String xmlFile = (String)it.next();
                  config.getLogStream().println(
                     "Processing file: " + xmlFile + "...");
                  Document newDoc = transformXML(xmlFile, xslTransform);
                  config.getLogStream().println("Saving the transformed file: "
                     + xmlFile + "...");

                  write(newDoc, xmlFile, dtd);
               }
            }
            catch(Exception e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
   }

   /**
    * Returns an iterator over a list of file paths. All the file paths
    * are valid and point to an existing file on the system.
    *
    * @param file the path of an Xml file or a directory containing xml, may
    * not be <code>null</code> or empty
    *
    * @param type specifies the type of XML file which should be returned,
    * should either be <code>null</code> or empty or equal "ContentEditor"
    *
    * @param ps stream for logging, assumed not <code>null</code>
    *
    * @return an iteror over a list of file paths (<code>String</code>),
    * never <code>null</code>, may be empty
    *
    * @throws IllegalArgumentException if any param is invalid
    */
   private Iterator getXmlFiles(String file, String type, PrintStream ps)
   {
      if ((file == null) || (file.trim().length() < 1))
         throw new IllegalArgumentException("file may not be null or empty");

      boolean ceOnly = false;
      if ((type != null) && (type.trim().length() > 0))
      {
         if (type.equalsIgnoreCase("ContentEditor"))
            ceOnly = true;
         else
            throw new IllegalArgumentException("invalid type specified");
      }

      List xmlFiles = new ArrayList();
      File f = new File(m_RxRootDir, file);
      File[] files = new File[]{f};

      if (f.exists())
      {
         if (f.isDirectory())
         {
            PSFilenameFilter nameFilter =
               new PSFilenameFilter("xml", false);
            files = f.listFiles(nameFilter);
         }

         for (int i = 0; i < files.length; i++)
         {
            File tempFile = files[i];
            if ((!ceOnly) || (ceOnly && isContentEditorApp(tempFile, ps)))
               xmlFiles.add(tempFile.getAbsolutePath());
         }
      }
      else
      {
         ps.print("File does not exist : " + f.getAbsolutePath());
      }
      return xmlFiles.iterator();
   }

   /**
    * Determines whether the specified file is a Rhythmyx Content Editor
    * application file. Returns <code>true</code> if the root element
    * equals "PSXApplication" and contains atleast one "PSXContentEditor"
    * element.
    *
    * @param file the file to test for Content Editor, may not be
    * <code>null</code> or non-empty
    *
    * @param ps stream for logging, may not be <code>null</code>
    *
    * @return <code>true</code> is the specified file is a Rhythmyx Content
    * Editor application file, <code>false</code> otherwise.
    */
   public static boolean isContentEditorApp(File file, PrintStream ps)
    throws IllegalArgumentException
   {
      if (file == null)
         throw new IllegalArgumentException("file must not be null");
      
      if (ps == null)
         throw new IllegalArgumentException("ps must not be null");
            
      boolean isCE = false;
      if (file.exists() && file.isFile())
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(file);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(fis, false);
            Element root = doc.getDocumentElement();
            if (root != null)
            {
               // check if the XML file is a Rhythmyx application file.
               // For Rhythmyx apps the root node is "PSXApplication"
               if (root.getTagName().equals("PSXApplication"))
               {
                  // check if any "PSXContentEditor" element exist
                  NodeList nl = root.getElementsByTagName("PSXContentEditor");
                  if ((nl != null) && (nl.getLength() > 0))
                     isCE = true;
               }
            }
         }
         catch (Exception ex)
         {
            ps.print("Error while parsing file : " + file.getAbsolutePath());
         }
         finally
         {
            if (fis != null)
            {
               try
               {
                  fis.close();
               }
               catch (Exception e)
               {
               }
            }
         }
      }
      return isCE;
   }

   /**
    * Show how to transform a DOM tree into another DOM tree.
    * This uses the javax.xml.parsers to parse an XML file into a
    * DOM, and create an output DOM.
    *
    * @param srcDoc source document, must not be <code>null</code>.
    * @param xslFile xsl file for transforming, must not be <code>null</code>.
    */
   public static Document transformXML(Document srcDoc, String xslFile)
      throws TransformerException, TransformerConfigurationException,
             SAXException, IOException, MalformedURLException
   {
      //FB: EC_UNRELATED_CLASS_AND_INTERFACE NC 1-17-16
      if(srcDoc==null || srcDoc.getTextContent().equals(""))
      {
         throw new IllegalArgumentException("srcDoc must not be null.");
      }
      if(xslFile.equals(""))
      {
         throw new IllegalArgumentException("xslFile must not be null.");
      }
      TransformerFactory tfactory = TransformerFactory.newInstance();
      if(!tfactory.getFeature(DOMSource.FEATURE))
      {
         throw new org.xml.sax.SAXNotSupportedException(
                "DOM node processing not supported!");
      }

      String strFileUrl = new File(xslFile).toURL().toString();
      Templates templates;
      Document outNode = PSXmlDocumentBuilder.createXmlDocument();
      Node doc = PSXmlDocumentBuilder.createXmlDocument(
            new InputSource(strFileUrl), false);
      DOMSource dsource = new DOMSource(doc);
      // If we don't do this, the transformer won't know how to
      // resolve relative URLs in the stylesheet.
      dsource.setSystemId(strFileUrl);
      templates = tfactory.newTemplates(dsource);

      Transformer tr = templates.newTransformer();
      
      tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      tr.setOutputProperty(OutputKeys.METHOD, "xml");
 
      tr.transform(
         new DOMSource(srcDoc), new DOMResult(outNode));

      return outNode;
   }

   /**
    * Show how to transform a DOM tree into another DOM tree.
    * This uses the javax.xml.parsers to parse an XML file into a
    * DOM, and create an output DOM.
    *
    * @param sourceID source XML file, must not be <code>null</code>.
    * @param xslID xsl file for transforming, must not be <code>null</code>.
    * @throws TransformerException
    * @throws TransformerConfigurationException
    * @throws SAXException
    * @throws IOException
    * @throws MalformedURLException
    */
   public static Document transformXML(String sourceID, String xslID)
      throws TransformerException, TransformerConfigurationException,
             SAXException, IOException, MalformedURLException
   {
      if(sourceID == null || sourceID.equals(""))
      {
         throw new IllegalArgumentException("sourceID must not be null.");
      }
       if(xslID == null || xslID.equals(""))
      {
         throw new IllegalArgumentException("xslID must not be null.");
      }
       Document doc =
         PSXmlDocumentBuilder.createXmlDocument(
            new InputSource(new File(sourceID).toURL().toString()), false);
      return transformXML(doc, xslID);
   }

   /**
    * Write the XML document to the specified output file. If
    * <code>dtd</code> is specified (not <code>null</code> and non-empty) then
    * the output document will contain a DOCTYPE with the
    * specified <code>dtd</code>.
    *
    * @param doc the XML document to be written, may not be <code>null</code>.
    * @param outputFile the file to write to, may not be <code>null</code>.
    * @param dtd the path of dtd file, may be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs while writting the document.
    */
   public static void write(Document doc, String outputFile, String dtd)
      throws IOException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc can not be null");

      if ((outputFile == null) || (outputFile.trim().length() < 1))
         throw new IllegalArgumentException(
            "Output file can not be null or empty");

      String newLine = System.getProperty("line.separator", "\r\n");

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      walker.setConvertXmlEntities(true);

      Writer w = null;
      boolean omitXMLDeclaration = false;
      boolean omitDocumentType = false;

      try
      {
         File file = new File(outputFile);
         if (!file.exists())
         {
            file.getParentFile().mkdirs();
            file.createNewFile();
         }

         FileOutputStream os = new FileOutputStream(file);
         w = new OutputStreamWriter(os, PSCharSets.rxJavaEnc());
         
         if ((dtd != null) && (dtd.trim().length() > 0) &&
            (doc.getDocumentElement() != null))
         {
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + newLine);
            w.write("<!DOCTYPE " + doc.getDocumentElement().getTagName() +
               " SYSTEM \"" + dtd + "\">" + newLine);

            omitXMLDeclaration = true;
            omitDocumentType = true;
         }
         walker.write(w, true, omitXMLDeclaration, omitDocumentType);
      }
      finally
      {
         if (w != null)
         {
            try
            {
               w.close();
            }
            catch (Exception ex)
            {
               //no-op
            }
         }
      }
   }

   /**
    * This is a quick way to execute your plugin. It needs two arguments
    * 1. root directory which needs to be upgraded
    * 2. a plugin configuration file that needs to run your plugin
    * Note: Configuration file must reside in the %RX_INSTALL_DIR%/upgrade. If 
    * config file is testUpgrage.xml, pathname is appended as 
    * %RX_INSTALL_DIR%/upgrade/testUpgrade.xml
    * Main method for testing purpose and to run upgrade as batch file if needed.
    */
   public static void main(String[] args)
   {
      try
      {
         if(args.length < 2)
         {
            System.out.println("Usage:");
            System.out.println("java com.percussion.install.RxUpgrade <RxRootDir> <UpgradeXmlFileName>");
            return;
         }
         String RxRoot = null;
         if(args.length > 0)
            RxRoot = args[0];

         String cfgFile = null;
         if(args.length > 1)
            cfgFile = args[1];

         RxUpgrade upgrade = new RxUpgrade();
         upgrade.process(RxRoot, cfgFile);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Returns the full path of upgrade directory
    */
   static public String getUpgradeRoot()
   {
      return m_RxRootDir + File.separator + UPGRADE_DIR + File.separator;
   }

   /**
    * Returns the root directory of Rhythmyx
    */
   static public String getRxRoot()
   {
      if(m_RxRootDir.isEmpty()){
         m_RxRootDir = PathUtils.getRxDir(null).getAbsolutePath();
      }
      return m_RxRootDir + File.separator;
   }

   /**
    * The connection to the backend repository. It uses the connection 
    * information from {@link #getRxRepositoryProps()}.
    * 
    * @return the connection to the backend repository. Note, it is caller's
    *   resposibility to close this connection.
    * 
    * @throws Exception if an error occurs while creating the connection.
    */
   public static Connection getJdbcConnection() throws Exception
   {
      Properties props = getRxRepositoryProps();
      String password;
      try{
         password = PSEncryptor.getInstance("AES",
                 PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
         ).decrypt(props.getProperty("PWD"));
      }catch(Exception e){
         password = PSLegacyEncrypter.getInstance(
                 PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
         ).decrypt(props.getProperty("PWD"),
                 PSJdbcDbmsDef.getPartOneKey(),null);
      }
      Connection conn = InstallUtil.createConnection(props.getProperty("DB_DRIVER_NAME"),
              props.getProperty("DB_SERVER"),
              props.getProperty("DB_NAME"),
              props.getProperty("UID"),
              password);
      return conn;
   }
   
   /**
    * Gets repository properties which contains the information for creating
    * JDBC connections. The location of the properties file is assumed at
    * "rxconfig/Installer/rxrepository.properties" under the installed
    * directory.
    * 
    * @return the repository properties, never <code>null</code>.
    *    
    * @throws IOException if cannot access the properties file.
    * @throws FileNotFoundException if cannot find the properties file 
    *   described above file. 
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public static Properties getRxRepositoryProps()
         throws FileNotFoundException, IOException
   {
      Properties repprops = new Properties();
      String path = getRxRoot() + "rxconfig/Installer/rxrepository.properties";

      try(FileInputStream fin = new FileInputStream(new File(path));){

         repprops.load(fin);
         repprops.setProperty(PWD_ENCRYPTED_PROPERTY, "Y");
      }
      return repprops;
   }
   
   /**
    * Gets the objectstore directory by looking in server.properties for the
    * objectstore properties file, then reading the objectstore directory
    * property in the properties file.
    * 
    * @return The file representing the objectstore directory, never
    * <code>null</code>.  If any errors are encountered, <RxRoot>/ObjectStore
    * will be returned.
    */
   public static File getObjectStoreDir()
   {
      String rxRoot = getRxRoot();
      File objDir = new File(rxRoot + "ObjectStore");
    
      try
      {
         //Get the server properties
         PSProperties serverProps = new PSProperties(
               rxRoot + "rxconfig/Server/server.properties");
         
         //Get the objectstore properties
         PSProperties objProps = new PSProperties(
               rxRoot + serverProps.getProperty(PROPS_OBJECT_STORE_VAR,
                     PROPS_OBJECT_STORE));   
         
         //ObjectStore directory
         objDir = new File(
               rxRoot + objProps.getProperty(PROPS_OBJECT_STORE_DIR));
      }
      catch (Exception e)
      {
         RxUpgradeLog.logIt("Error accessing objectstore directory: "
               + e.getMessage());
      }
      
      return objDir;
   }
   
   /**
    * Gets previous version properties which contains the previous version
    * information. The location of the properties file is assumed at
    * "PreviousVersion.properties" under the installed
    * directory.
    * 
    * @return the previous version properties, never <code>null</code>.
    *    
    * @throws IOException if cannot access the properties file.
    * @throws FileNotFoundException if cannot find the properties file 
    *   described above file. 
    */
   public static Properties getRxPreviousVersionProps()
         throws FileNotFoundException, IOException
   {
      Properties previousprops = new Properties();
      String path = getRxRoot() + "PreviousVersion.properties";
      previousprops.load(new FileInputStream(new File(path)));
      
      return previousprops;
   }
   
   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid and not
    * <code>null</code>.
    * 
    * @throws IOException if an error occurs loading repository properties. 
    * @throws FileNotFoundException if the repository properties can not be
    * found.
    */
   public static String qualifyTableName(String table)
      throws FileNotFoundException, IOException
   {
      Properties dbProps = getRxRepositoryProps();
      String database = dbProps.getProperty("DB_NAME");
      String schema = dbProps.getProperty("DB_SCHEMA");
      String driver = dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }
   
   /**
    * Initializes the date of the current upgrade to the form yyyy-mm-dd only if
    * it has not been initialized.
    */
   private static void initDate()
   {
      if (ms_date == null)
      {
         Date date = new Date();
         java.sql.Date sqlDate = new java.sql.Date(date.getTime());
         ms_date = sqlDate.toString();
      }
   }
   
   /**
    * Creates the pre and post upgrade log file directories if they do not
    * exist.
    */
   private static void initLogFileDirs()
   {
      File preLogFileDir = new File(getPreLogFileDir());
      if (!preLogFileDir.exists())
         preLogFileDir.mkdirs();
      
      File postLogFileDir = new File(getPostLogFileDir());
      if (!postLogFileDir.exists())
         postLogFileDir.mkdirs();
   }
   
   /**
    * Gets the folder for the upgrade plugin log files.  This will be in the
    * form [RxRoot]/upgrade/logs/yyyy-mm-dd/.
    */
   public static String getLogFileDir()
   {
      initDate();
      
      return getUpgradeRoot() + LOGS_DIR + File.separator + ms_date
         + File.separator;
   }
   
   /**
    * Gets the folder for the pre-upgrade log files for the current upgrade.
    * Includes trailing '/'.
    */
   public static String getPreLogFileDir()
   {
      return getLogFileDir() + PRE_DIR + File.separator; 
   }
   
   /**
    * Gets the folder for the post-upgrade log files for the current upgrade.
    * Includes trailing '/'.
    */
   public static String getPostLogFileDir()
   {
      return getLogFileDir() + POST_DIR + File.separator; 
   }
   
   /**
    * Constant for the name of the PROP_ACTION_ADD property.
    */
   static public final String PROP_ACTION_ADD = "add";

   /**
    * Constant for the name of the PROP_ACTION_DELETE property.
    */
   static public final String PROP_ACTION_DELETE = "delete";

   /**
    * Constant for the name of the PROP_ACTION_MODIFY property.
    */
   static public final String PROP_ACTION_MODIFY = "modify";

   /**
    * Constant for the name of the PROP_CONDITION_MODIFYIFEXIST property.
    */
   static public final String PROP_CONDITION_MODIFYIFEXIST = "modifyifexist";

   /**
    * Constant for the name of the PROP_CONDITION_ADDIFNOTEXIST property.
    */
   static public final String PROP_CONDITION_ADDIFNOTEXIST = "addifnotexist";

   /**
    * Constant for the name of the YES property.
    */
   static public final String YES = "yes";

   /**
    * Constant for the name of the NO property.
    */
   static public final String NO = "no";

   /**
    * Name of the Rhythmyx root directory initialized in process function
    */
   static private String m_RxRootDir = "";

   /**
    * Upgrade directory name
    */
   static public final String UPGRADE_DIR = "upgrade";
   
   /**
    * Upgrade plugins log files directory
    */
   static public final String LOGS_DIR = "logs";
   
   /**
    * Pre-upgrade plugins directory
    */
   static public final String PRE_DIR = "preupgrade";
   
   /**
    * Post-upgrade plugins directory
    */
   static public final String POST_DIR = "postupgrade";
   
   /**
    * Flag for pre-upgrade plugins
    */
   static public boolean ms_bPreUpgrade = false;
   
   /**
    * Pre-upgrade plugin response object storage
    */
   static public ArrayList ms_pluginResponses = new ArrayList();
   
   /**
    * The objectstore property name in server.properties
    */
   static private final String PROPS_OBJECT_STORE_VAR = "objectStoreProperties";
   
   /**
    * The default objectstore properties file
    */
   static private final String PROPS_OBJECT_STORE = "rxconfig/Server/objectstore.properties";
   
   /**
    * The objectstore directory property name in objectstore.properties
    */
   static private final String PROPS_OBJECT_STORE_DIR = "objectDirectory";
   
   /**
    * Date of current upgrade in the form yyyy-mm-dd initialized in ctor.
    */
   static private String ms_date = null; 
   
}
