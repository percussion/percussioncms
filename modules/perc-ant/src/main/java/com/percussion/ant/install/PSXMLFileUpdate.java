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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.install.RxFileManager;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;


/**
 * PSXMLFileUpdate is a task that uses the ISMP XSL class to apply an XSL file
 * to an XML file at install time.  The XSL file is bundled with the source tree
 * and it may contain one token to be replaced when processing.  This class uses
 * the ISMP XSL processor that is used for command builds.
 * It bundles this processor into the installer so that this bean will work
 * with any version of Java.
 * The stylesheets processed by this class are supplied the following parameters:
 * <p>
 * 1> majorVersion - major version of the Rhythmyx being upgraded, will be
 * <code>-1</code> for a new install
 * 2> minorVersion - minor version of the Rhythmyx being upgraded, will be
 * <code>-1</code> for a new install
 * 3> buildNumber - build number of the Rhythmyx being upgraded, will be
 * <code>-1</code> for a new install
 * <p>
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="xmlFileUpdate"
 *              class="com.percussion.ant.install.PSXMLFileUpdate"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to update the xml file.
 *
 *  <code>
 *  &lt;xmlFileUpdate backupXMLFile="true" resolveXslFile="true"
 *      token="$P(absoluteInstallLocation)" value="${install.dir}"
 *      xmlFile="${install.dir}/htmlconverter/config/serverPageTags.xml"
 *      xslFile="${install.dir}/installerTemp/source_tree/file.xsl"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSXMLFileUpdate extends PSAction implements EntityResolver
{

   /***************************************************************************
    * EntityResolver implementation
    ***************************************************************************/

   /**
    * Implementation of the <code>EntityResolver</code> interface method.
    * The default Xerces implementation resolves enitity relative to the
    * directory from which Installshield is running. This method is overriden
    * to resolve entity relative to the Rhythmyx root directory.
    *
    * @see org.xml.sax.EntityResolver
    */
   public InputSource resolveEntity (String publicId, String systemId)
   {
      if ((systemId == null) || (systemId.trim().length() < 0))
         return null;

      PSLogger.logInfo("Input System Id : " + systemId);
      String tempSystemId = systemId.toLowerCase();
      if (tempSystemId.startsWith("http:"))
         return null;

      try
      {
         String workDir = System.getProperty("user.dir");
         workDir = workDir.toLowerCase();
         workDir = workDir.replace('\\', '/');

         int index = tempSystemId.indexOf(workDir);
         if (index != -1)
         {
            index += workDir.length();
            index ++;
            if (systemId.length() > index)
               systemId = systemId.substring(index);
         }

         String path = getRootDir();
         if (!path.endsWith(File.separator))
            path += File.separator;

         File retFile = null;
         File f = new File(path + systemId);
         if (f.exists() && f.isFile())
            retFile = f;

         // try the "DTD" directory under Rhythmyx
         if (retFile == null)
         {
            f = new File(path + "DTD" + File.separator + systemId);
            if (f.exists() && f.isFile())
               retFile = f;
         }

         // try the "DTD" directory in the directory containing the Xml file
         String parentFile = new File(m_xmlFile).getParentFile().getAbsolutePath();
         parentFile += File.separator;

         if (retFile == null)
         {
            f = new File(parentFile + "DTD" + File.separator + systemId);
            if (f.exists() && f.isFile())
               retFile = f;
         }

         if (retFile == null)
         {
            f = new File(parentFile + "dtd" + File.separator + systemId);
            if (f.exists() && f.isFile())
               retFile = f;
         }

         if (retFile != null)
         {
            PSLogger.logInfo("Resolved System Id : " +
                  retFile.getAbsolutePath());
            return new InputSource(new FileInputStream(retFile));
         }
      }
      catch (Exception ex)
      {
         PSLogger.logInfo("Exception : " + ex.getLocalizedMessage());
         PSLogger.logInfo(ex);
      }
      return null;
   }

   // see base class
   @Override
   public void execute()
   {
      String installLoc = getRootDir();
      m_xmlFile = installLoc + File.separator + m_xmlFile;

      File fSrcXmlFile = new File(m_xmlFile);
      if (!fSrcXmlFile.exists())
      {
         PSLogger.logInfo(
               "Xml file does not exist : " + fSrcXmlFile.getAbsolutePath());
         return;
      }

      String srcXmlFile = fSrcXmlFile.getAbsolutePath();
      m_xmlFile = srcXmlFile;
      PSLogger.logInfo("Updating Xml File : " + m_xmlFile);

      String destXmlFile = srcXmlFile;
      File fDestXmlFile = new File(destXmlFile);

      boolean deleteSrcFile = false;
      if (fDestXmlFile.exists() && !fDestXmlFile.canWrite())
      {
         m_backupXmlFile = true;
         deleteSrcFile = true;
      }

      try
      {
         // create a backup of the xml file, if specified
         if (m_backupXmlFile)
         {
            File backupFile = IOTools.createBackupFile(fSrcXmlFile);
         }

         if (deleteSrcFile)
            fSrcXmlFile.delete();

         String docType = getDocType(srcXmlFile);
         // apply ISMP xsl file to xml file
         applyXSL(srcXmlFile, destXmlFile, getXslFile(), installLoc);
         setDocType(destXmlFile, docType);
      }
      catch (Exception e)
      {
         PSLogger.logError("PSXMLFileUpdate : " + e.getMessage());
      }
   }

   /***************************************************************************
    * Private Functions
    ***************************************************************************/

   /**
    * Helper method to transform an Xml file using the specified XSL file.
    *
    * @param srcXmlFile the path of the Xml file to which the XSL should be
    * applied, assumed not <code>null</code> and non-empty
    * @param destXmlFile the path of the Xml file to which the transformed Xml
    * document should be serialized, assumed not <code>null</code> and non-empty
    * @param xslResource the path of the resource which the XSL file
    * is stored, assumed not <code>null</code> and non-empty
    * @param rxDir the absolute installation directory, assumed not
    * <code>null</code> and non-empty
    *
    * @throws Exception if any error occurs
    */
   private void applyXSL(String srcXmlFile, String destXmlFile,
         String xslResource, String rxDir) throws Exception
         {
      String xslTempPath = xslResource;
      // Resolve all string resolvers in XSL file
      if (m_resolveXslFileContents)
         xslTempPath = resolveFile(xslTempPath);

      // Apply the XSL file to the xml file
      InputStream xslIn = new BufferedInputStream(
            new FileInputStream(xslTempPath));
      StreamSource xslSource = new StreamSource(xslIn);

      InputStream  xmlIn = new BufferedInputStream(
            new FileInputStream(srcXmlFile));
      StreamSource xmlSource = new StreamSource(xmlIn);

      // Create a transformer for the stylesheet.
      TransformerFactory tfactory  = TransformerFactory.newInstance();
      tfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Transformer transformer = tfactory.newTransformer(xslSource);

      setTransformParams(rxDir, transformer);

      // Transform the source XML
      transformer.transform(xmlSource, new StreamResult(destXmlFile));
         }

   /**
    * Sets the XSL transformation parameters. The following three paramaters
    * are currently set:
    * <p>
    * 1> majorVersion - major version of the Rhythmyx being upgraded, will be
    * <code>-1</code> for a new install
    * 2> minorVersion - minor version of the Rhythmyx being upgraded, will be
    * <code>-1</code> for a new install
    * 3> buildNumber - build number of the Rhythmyx being upgraded, will be
    * <code>-1</code> for a new install
    * <p>
    * @param rxRootDir the Rhythmyx root directory, assumed not
    * <code>null</code> and non-empty.
    * @param transformer the XSL transformer for which the parameters should be
    * set, assumed not <code>null</code>
    */
   private void setTransformParams(String rxRootDir,
         Transformer transformer)
   {
      int majorVersion = -1;
      int minorVersion = -1;
      int buildNumber = -1;

      File verProp = new File(rxRootDir,
            RxFileManager.PREVIOUS_VERSION_PROPS_FILE);

      if (verProp.exists() && verProp.isFile())
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(verProp);
            Properties p = new Properties();
            p.load(fis);

            String strMajor = p.getProperty(
                  RxFileManager.MAJOR_VERSION_PROP_KEY, "-1");

            String strMinor = p.getProperty(
                  RxFileManager.MINOR_VERSION_PROP_KEY, "-1");

            String strBuild = p.getProperty(
                  RxFileManager.BUILD_NUMBER_PROP_KEY, "-1");

            try
            {
               majorVersion = Integer.parseInt(strMajor);
            }
            catch (Exception ex)
            {
            }

            try
            {
               minorVersion = Integer.parseInt(strMinor);
            }
            catch (Exception ex)
            {
            }

            try
            {
               buildNumber = Integer.parseInt(strBuild);
            }
            catch (Exception ex)
            {
            }
         }
         catch (Exception ex)
         {
            PSLogger.logInfo("ERROR : " + ex.getMessage());
            PSLogger.logInfo(ex);
         }
         finally
         {
            if (fis != null)
            {
               try
               {
                  fis.close();
               }
               catch (Exception ex)
               {
               }
            }
         }
      }

      transformer.setParameter(RxFileManager.MAJOR_VERSION_PROP_KEY,
            new Integer(majorVersion));

      transformer.setParameter(RxFileManager.MINOR_VERSION_PROP_KEY,
            new Integer(minorVersion));

      transformer.setParameter(RxFileManager.BUILD_NUMBER_PROP_KEY,
            new Integer(buildNumber));
   }

   /**
    * Helper method to resolve all occurrences of the token in the XSL file
    * stream and return the path of the file with resolved file contents.
    *
    * @param file the path of the XSL whose contents need to be resolved before
    * applying it to the XML file, assumed not <code>null</code> and non-empty
    *
    * @return the path of the file with resolved file contents,
    * never <code>null</code> or empty
    *
    * @throws Exception if any error occurs
    */
   private String resolveFile(String file)
   throws Exception
   {
      BufferedReader in = new BufferedReader(new FileReader(file));
      String xslResolvedFile =
         IOTools.createTempFile(new File(file)).getAbsolutePath();

      BufferedWriter out = null;
      try
      {
         out = new BufferedWriter(new FileWriter(xslResolvedFile));
         // resolve token, line by line.
         String line = null;
         while ((line = in.readLine()) != null)
         {
            out.write(line.replaceAll(getToken(),
                  getValue().replaceAll("\\\\", "\\\\\\\\")));
            out.newLine();
         }
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (Exception ex)
            {
               // no-op
            }
         }
      }

      return xslResolvedFile;
   }

   /**
    * Returns the Document Type Declaration of the Xml document obtained by
    * parsing the Xml file represented by <code>xmlFilePath</code>.
    *
    * @param xmlFilePath the path of the Xml file, assumed not <code>null</code>
    * and non-empty
    *
    * @return Document Type Declaration (see DocumentType) associated with the
    * document obtained by parsing the specified Xml file, may be
    * <code>null</code>, never empty if not-<code>null</code>
    *
    * @throws Exception if any error occurs
    */
   private String getDocType(String xmlFilePath)
   throws Exception
   {
      String strDocType = "";
      FileInputStream fis = null;
      try
      {
         fis = new FileInputStream(xmlFilePath);
         DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
         db.setEntityResolver(this);
         Document doc = db.parse(fis);
         DocumentType docType = doc.getDoctype();
         if (docType != null)
         {
            String name = docType.getName();
            String publicId = docType.getPublicId();
            String systemId = docType.getSystemId();

            strDocType = "<!DOCTYPE ";
            strDocType += name;
            strDocType += " ";
            if (publicId != null)
            {
               strDocType += "PUBLIC \"";
               strDocType += publicId;
               strDocType += "\"";
               if (systemId != null)
               {
                  strDocType += " \"";
                  strDocType += systemId;
                  strDocType += "\"";
               }
            }
            else if (systemId != null)
            {
               strDocType += "SYSTEM \"";
               strDocType += systemId;
               strDocType += "\"";
            }
            strDocType += ">";
         }
      }
      finally
      {
         if (fis != null)
         {
            try
            {
               fis.close();
            }
            catch (Exception ex)
            {
               //no-op
            }
         }
      }
      strDocType = strDocType.trim();
      if (strDocType.length() < 1)
         strDocType = null;
      return strDocType;
   }

   /**
    * Sets the Document Type Declaration of the Xml document obtained by
    * parsing the Xml file represented by <code>xmlFilePath</code>.
    *
    * @param xmlFilePath the path of the Xml file, assumed not <code>null</code>
    * and non-empty
    * @param docType the Document Type Declaration to set in the Xml document,
    * may be <code>null</code> or empty.
    *
    * @throws Exception if any error occurs
    */
   private void setDocType(String xmlFilePath, String docType)
   throws Exception
   {
      if ((docType == null) || (docType.trim().length() < 1))
         return;

      FileInputStream fis = null;
      Document doc = null;
      try
      {
         fis = new FileInputStream(xmlFilePath);
         doc = PSXmlDocumentBuilder.createXmlDocument(fis, false);
      }
      finally
      {
         if (fis != null)
         {
            try
            {
               fis.close();
            }
            catch (Exception ex)
            {
               //no-op
            }
         }
      }

      if (doc == null)
         return;

      Writer sw = null;
      OutputStreamWriter osw = null;
      String lineSep = System.getProperty("line.separator", "\r\n");
      try
      {
         sw = new StringWriter();
         PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
         walker.write(sw, true, true, true);
         String strXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         strXml += lineSep;
         strXml += docType;
         strXml += lineSep;
         strXml += sw.toString();

         osw = new OutputStreamWriter(new FileOutputStream(xmlFilePath), "UTF-8");
         osw.write(strXml, 0 , strXml.length());
      }
      finally
      {
         if (sw != null)
         {
            try
            {
               sw.close();
            }
            catch (Exception ex)
            {
               //no-op
            }
         }
         if (osw != null)
         {
            try
            {
               osw.close();
            }
            catch (Exception ex)
            {
               //no-op
            }
         }
      }
   }


   /***************************************************************************
    * Bean Properties
    ***************************************************************************/

   /**
    * Returns the path of the XML File on the target machine to update.
    *
    * @return relative or absolute path of the XML File on the target machine
    * to update. May be <code>null</code> or empty.
    */
   public String getXmlFile()
   {
      return m_xmlFile;
   }

   /**
    * Sets the path of the XML File on the target machine to update. It can be
    * relative or absolute path, and can use string resolvers.
    *
    * @param xmlFile the path of the XML File on the target machine to update,
    * if <code>null</code> or empty, an error is logged during the build process.
    */
   public void setXmlFile(String xmlFile)
   {
      this.m_xmlFile = xmlFile;
   }

   /**
    * Returns the path of the  XSL File to bundle into the installer and apply
    * at install time.
    *
    * @return the path of the  XSL File to bundle into the installer and apply
    * at install time, if <code>null</code> or empty, an error is logged during
    * the build process.
    */
   public String getXslFile()
   {
      return m_xslFile;
   }

   /**
    * Sets the path of the  XSL File to bundle into the installer and apply
    * at install time.
    *
    * @param xslFile the path of the  XSL File to bundle into the installer and
    * apply at install time, if <code>null</code> or empty, an error is logged
    * during the build process.
    */
   public void setXslFile(String xslFile)
   {
      this.m_xslFile = xslFile;
   }

   /**
    * Returns whether or not a backup of the Xml file should be created before
    * applying the XSL file.
    *
    * @return <code>true</code> if the backup of the Xml file should be created
    * before applying the XSL file, <code>false</code> if no backup of the Xml
    * file should be created.
    */
   public boolean isBackupXMLFile()
   {
      return m_backupXmlFile;
   }

   /**
    * Sets whether or not a backup of the Xml file should be created before
    * applying the XSL file.
    *
    * @param backupXmlFile <code>true</code> if the backup of the Xml file
    * should be created before applying the XSL file, <code>false</code> if no
    * backup of the Xml file should be created.
    */
   public void setBackupXMLFile(boolean backupXmlFile)
   {
      this.m_backupXmlFile = backupXmlFile;
   }

   /**
    * Returns whether the contents of the XSL file should be resolved using
    * Installshield string resolver methods before appling it to the XML file.
    *
    * @return <code>true</code> if the contents of the XSL file should be
    * resolved using Installshield string resolver methods before applying it
    * to the XML file, otherwise <code>false</code>
    */
   public boolean isResolveXslFileContents()
   {
      return m_resolveXslFileContents;
   }

   /**
    * Sets whether the contents of the XSL file should be resolved using
    * Installshield string resolver methods before appling it to the XML file.
    *
    * @param resolveXslFileContents <code>true</code> if the contents of the
    * XSL file should be resolved using Installshield string resolver methods
    * before applying it to the XML file, otherwise <code>false</code>
    */
   public void setResolveXslFileContents(boolean resolveXslFileContents)
   {
      this.m_resolveXslFileContents = resolveXslFileContents;
   }

   /**
    * Returns the name of the token which should be replaced in the XSL file.
    *
    * @return this is the name of the token.
    */
   public String getToken()
   {
      return m_token;
   }

   /**
    * Sets the name of the token which should be replaced in the XSL file.
    *
    * @param token this is the name of the token.
    */
   public void setToken(String token)
   {
      m_token = token;
   }

   /**
    * Returns the value for which the token should be replaced in the XSL file.
    *
    * @return this is the value of the token replacement.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Sets the value for which the token should be replaced in the XSL file.
    *
    * @param value this is the value of the token replacement.
    */
   public void setValue(String value)
   {
      m_value = value;
   }

   /***************************************************************************
    * Properties
    ***************************************************************************/

   /**
    * The relative path of the XML File on the target machine to update.
    * Modified using <code>setXmlFile()</code> method. If <code>null</code> or
    * empty, an error is logged during the build process.
    */
   private String m_xmlFile = "";

   /**
    * The XSL File to bundle into the installer and apply at install time.
    * The file can contain one replacement token which is resolved at install
    * time.  Modified using <code>setXslFile()</code> method. If
    * <code>null</code> or empty, an error is logged during the build process.
    */
   private String m_xslFile = "";

   /**
    * Whether or not a backup of the Xml file should be created before applying
    * the XSL file. If <code>true</code> then a backup of the Xml file is
    * created, otherwise no backup file is created. Modified using
    * <code>setBackupXMLFile()</code> method.
    */
   private boolean m_backupXmlFile = true;

   /**
    * Whether the contents of the XSL file should be resolved using
    * Installshield string resolver methods before applying it to the XML file.
    * If <code>true</code> the contents of the XSL file are resolved otherwise
    * not. Modified using <code>setResolveXslFileContents()</code> method.
    */
   private boolean m_resolveXslFileContents = false;

   /**
    * The XSL file token which needs to be resolved.
    */
   private String m_token;

   /**
    * The value which should replace the XSL file token.
    */
   private String m_value;
}
