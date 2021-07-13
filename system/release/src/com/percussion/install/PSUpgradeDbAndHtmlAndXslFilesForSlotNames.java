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

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.StringOutputSegment;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.IOTools;
import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.tools.PSPatternMatcher;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Plugin to modify the database table "RXSLOTTYPE", HTML source files, XSL
 * files and Navigation property file {@link #NAVIGATION_PROPERTIES} used in the
 * assembly applications in the system for the slot name references NOT to have
 * blank spaces in between.
 * <p>
 * First we modify the database table "RXSLOTTYPE" for the "SLOTNAME" not to
 * have blank spaces in between. The logic is explained in
 * {@link #modifySlotName(String)}. The modified slot names are now applied to
 * the XSL and HTML files. Since we do not have an easy way of getting the
 * assembly applications, we take all the applications whose names do not start
 * with "sys_" or "psx_" or part of the set {@link #EXCLUDE_APP_SET}.
 * <p>
 * <ol>
 * <li>In each application directory we modify all *.xsl files</li>
 * <li>In each application directory we modify all *.htm* files in "src"
 * subdirectory</li>
 * <li>In each application directory we empty "edit" subdirectory</li>
 * </ol>
 * The slot names are located based on:
 * <ol>
 * <li>in an HTML file, we look for the attribute name "slotname" and if the
 * value consists of a slot name requires modification we replace with new name</li>
 * <li>in an XSL file, we look for the attribute name "select" and if the value
 * consists of the string "@slotname" and a slot name that needs modification we
 * replace with new name</li>
 * </ol>
 */
public class PSUpgradeDbAndHtmlAndXslFilesForSlotNames implements
   IPSUpgradePlugin
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule,
    * org.w3c.dom.Element)
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println();
      config.getLogStream().println(
         "Running plugin to upgrade slot names to remove blank spaces...");
      config.getLogStream().println();
      List fileList = null;

      File root = new File(RxUpgrade.getRxRoot());

      try
      {
         List slotNames = updateLabelsAndLoadSlotNamesFromDatabase(config
            .getLogStream());
         if (slotNames.isEmpty())
         {
            config.getLogStream().println("No slot names in the system");
            return null;
         }
         buildNameMapAndArray(slotNames);
         if (m_oldNewNameModifiedMap.isEmpty())
         {
            config.getLogStream().println(
               "No slot names in the system needed modifications");
            return null;
         }
         updateSlotNamesInDatabase(m_oldNewNameModifiedMap, config
            .getLogStream());
      }
      catch (Exception e)
      {
         config.getLogStream().println(
            "Upgrading slot names failed with the error:");
         config.getLogStream().println(e.getLocalizedMessage());
         e.printStackTrace(config.getLogStream());
         return null;
      }

      upgradeNavSlotNames(config.getLogStream());

      config.getLogStream().println("Upgrading Rhythmyx applications...");
      // search for all .htm* and .xsl files under Rx root
      try
      {
         fileList = getFilesOfType(new File(root, "ObjectStore"), "*.xml");
      }
      catch (IOException e)
      {
         e.printStackTrace(config.getLogStream());
         return null;
      }

      Iterator iter = fileList.iterator();
      while (iter.hasNext())
      {
         File appFile = (File) iter.next();

         String fileName = appFile.getName();
         String appFolder = fileName.substring(0, fileName.length()
            - ".xml".length());
         if (EXCLUDE_APP_SET.contains(appFolder)
            || appFolder.startsWith("sys_") || appFolder.startsWith("psx_"))
         {
            config.getLogStream().println(
               "Skipping system application: " + appFolder);
            continue;
         }
         try
         {
            config.getLogStream().println(
               "Upgrading application '" + appFolder + "' ...");
            upgradeFolder(new File(root, appFolder), false, config
               .getLogStream());
            config.getLogStream().println(
               "Upgrading application '" + appFolder + "' finished");
         }
         catch (MalformedURLException e)
         {
            config.getLogStream().println(e.getLocalizedMessage());
         }
         catch (IOException e)
         {
            config.getLogStream().println(e.getLocalizedMessage());
         }
      }
      config.getLogStream().println("Upgrading Rhythmyx applications finished");
      config.getLogStream().println("leaving the plugin...");
      return null;
   }

   /**
    * Update the slot names in the navigation properties file
    * {@link #NAVIGATION_PROPERTIES}.
    * 
    * @param logStream print stream for logging, assumed not <code>null</code>.
    */
   private void upgradeNavSlotNames(PrintStream logStream)
   {
      logStream.println("Updating '" + NAVIGATION_PROPERTIES + "'...");
      FileInputStream fis = null;
      FileOutputStream fos = null;

      try
      {
         fis = new FileInputStream(new File(RxUpgrade.getRxRoot(),
            NAVIGATION_PROPERTIES));

         Properties navProps = new Properties();
         navProps.load(fis);
         String value = navProps.getProperty(NAVON_PROPERTY_SLOTNAMES, "");
         String[] slotNames;
         boolean modified = false;
         while ((slotNames = getContainedSlot(value)) != null)
         {
            value = value.replace(slotNames[0], slotNames[1]);
            modified = true;
         }
         fis.close();
         fis = null;
         if (modified)
         {
            logStream.println("New value of " + NAVON_PROPERTY_SLOTNAMES
               + " is: " + value);
            fos = new FileOutputStream(new File(RxUpgrade.getRxRoot(),
               NAVIGATION_PROPERTIES));
            navProps.setProperty(NAVON_PROPERTY_SLOTNAMES, value);
            navProps.store(fos, "Modified by upgrade plugin");
            fos.flush();
         }
         else
         {
            logStream.println(NAVIGATION_PROPERTIES
               + " did not slot name upgrade");
         }
         logStream.println("Updating '" + NAVIGATION_PROPERTIES + "' finished");
         logStream.println();
      }
      catch (Exception e)
      {
         logStream.println("Upgrade of " + NAVIGATION_PROPERTIES
            + " is failed. The error is: " + e.getLocalizedMessage()
            + ". You may have to change the slot names manually");
         e.printStackTrace(logStream);
      }
      finally
      {
         if (fis != null)
         {
            try
            {
               fis.close();
            }
            catch (IOException e)
            {
            }
         }
         if (fos != null)
         {
            try
            {
               fos.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }

   /**
    * Builds the slot name map from the supplied list of slot names. The keys in
    * the map will be the old slot names and the values will be the new slot
    * names. The names are made unique by appending "X" to the name if required.
    * Another map is created that holds only the names that are truely modifed.
    * A sorted array (by string length of slot name) is built off of the
    * modified slot names so that the replacement is performed with longest
    * string first.
    * 
    * @param slotNames list of slot names, assumed not null.
    */
   private void buildNameMapAndArray(List slotNames)
   {
      m_oldNewNameMap = new HashMap();
      Set newNames = new HashSet();
      for (int i = 0; i < slotNames.size(); i++)
      {
         String oldName = (String) slotNames.get(i);
         String newName = InstallUtil.modifyName(oldName);

         // Make sure names are unique by appending trialing "X"
         while (newNames.contains(newName))
            newName += "X";
         //
         newNames.add(newName);
         m_oldNewNameMap.put(oldName, newName);
      }

      Iterator iter = m_oldNewNameMap.keySet().iterator();
      while (iter.hasNext())
      {
         String oldName = (String) iter.next();
         String newName = (String) m_oldNewNameMap.get(oldName);
         if (!oldName.equals(newName))
            m_oldNewNameModifiedMap.put(oldName, newName);
      }

      m_sortedSlotNames = (String[]) m_oldNewNameModifiedMap.keySet().toArray(
         new String[0]);
      Arrays.sort(m_sortedSlotNames, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            return (((String) o2).length() - ((String) o1).length());
         }
      });

   }

   /**
    * Removes whitespaces from the slot name provided to the
    * Java/global/percussion/fastforward/managednav/rxs_NavTreeSlotMarker
    * exit for the given application.
    *  
    * @param app the application, may not be <code>null</code>
    * @return the number of slot names which were converted
    */
   public static int convertSlotName(PSApplication app)
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null");
      
      int convertedSlots = 0;      
      Iterator dataSets = app.getDataSets().iterator();
      while (dataSets.hasNext())
      {
         PSDataSet dataSet = (PSDataSet) dataSets.next();
         PSPipe pipe = dataSet.getPipe();
         
         if (pipe != null)
         {
            PSExtensionCallSet resultDataExts = pipe.getResultDataExtensions();
            
            if (resultDataExts != null)
            {
               Iterator callSet = resultDataExts.iterator();
               while (callSet.hasNext())
               {
                  PSExtensionCall call = (PSExtensionCall) callSet.next();
                  
                  if (call.getName().equals("rxs_NavTreeSlotMarker"))
                  {
                     PSExtensionParamValue[] values = call.getParamValues();
                     for (int i=0; i<values.length; i++)
                     {
                        // converting the slot name specified at the ODD index
                        // parameters: 1, 3, 5, ...etc
                        IPSReplacementValue repVal = values[i].getValue();
                        
                        if (((i+1) % 2) == 0 && repVal instanceof PSTextLiteral)
                        {
                           PSTextLiteral value = (PSTextLiteral) repVal;
                           String origSlotName = value.getValueText();
                           String slotName = InstallUtil.modifyName(origSlotName);
                           value.setValueText(slotName);
                           
                           if (!slotName.equals(origSlotName))
                              convertedSlots++;
                        }
                     }
                  }
               }
            }
         }
      }
      
      return convertedSlots;
   }

   /**
    * Get the list files with matching extension pattern. Does not recurse into
    * sub-folders.
    * 
    * @param folder the folder to list the files from, assumed not
    * <code>null</code>.
    * @param extnPattern Only one extensionfor the files to be returned. It can
    * be a simple pattern like ".htm*", if <code>null</code> all files under
    * the folder are returned.
    * @return List of files matching the pattern, may be <code>null</code> or
    * empty.
    * @throws IOException
    */
   private List getFilesOfType(File folder, String extnPattern)
      throws IOException
   {
      if (!folder.isDirectory())
      {
         throw new IOException(folder.getAbsolutePath() + " is not a directory");
      }
      List fileList;
      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', extnPattern);
      PSFileFilter filter = new PSFileFilter(PSFileFilter.IS_FILE);
      filter.setNamePattern(pattern);
      PSFilteredFileList lister = new PSFilteredFileList(filter);
      fileList = lister.getFiles(folder);
      return fileList;
   }

  
   /**
    * Upgrade files as described below:
    * <p>
    * <ol>
    * <li>Modify all HTML files under "src" subfolder of the supplied folder
    * without recursing into subfolders</li>
    * <li>Modify all XSL files under the supplied folder witout recursing into
    * sub folders</li>
    * <li>Delete all files in the "edit" subfolder of the supplied folder
    * without recursing into subfolders</li>
    * </ol>
    * 
    * @param folder folder object, assumed not <code>null</code> and is a
    * folder. Typically a Rhythmyx application directory.
    * @param testMode <code>true</code> to run in test mode, i.e. don't
    * actually modifying the file and just report the changes.
    * @param logStream print stream for logging, assumed not <code>null</code>.
    * @throws MalformedURLException
    * @throws IOException
    */
   private void upgradeFolder(File folder, boolean testMode,
      PrintStream logStream) throws MalformedURLException, IOException
   {
      // Upgrade all source Html files
      File srcDir = new File(folder, "src");
      List htmlFiles = getFilesOfType(folder, "*.htm*");
      if (srcDir.exists())
         upgradeHtmlFiles(srcDir, testMode, logStream);
      else 
         logStream.println("HTML source directory does not exist.");
      // May be some html files are not in the "src" directory ...
      if ( htmlFiles.size() > 0 )
      {
         logStream.println("HTML files found here: " + folder.getPath());
         upgradeHtmlFiles(folder, testMode, logStream);
      }
      // Upgrade Xsl files
      upgradeXslFiles(folder, testMode, logStream);

      // Delete all xsl files under <rxroot>/<app>/edit directory so that they
      // can be regerenerated during assembly
      File editDir = new File(folder, "edit");
      if (!editDir.exists())
         return;

      List xslFilesToDelete = getFilesOfType(editDir, ".xsl");
      for (int i = 0; i < xslFilesToDelete.size(); i++)
      {
         File file = (File) xslFilesToDelete.get(i);
         logStream.println("Deleting file '" + file.getAbsolutePath() + "'...");
         try
         {
            file.delete();
         }
         catch (Exception e)
         {
            logStream
               .println("Delete failed you may have to delete it manually. Error: "
                  + e.getLocalizedMessage());
            e.printStackTrace(logStream);
         }
      }
   }

   /**
    * Modifies XSL files from the supplied folder. Calls
    * {@link #upgradeFile(File, boolean, PrintStream)} for each XSL file in the
    * folder.
    * 
    * @param folder XSl source folder, assumed not <code>null</code> and a
    * folder.
    * @param testMode <code>true</code> to run in test mode, i.e. don't
    * actually modifying the file and just report the changes.
    * @param logStream print stream for logging, assumed not <code>null</code>.
    * @throws IOException
    */
   private void upgradeXslFiles(File folder, boolean testMode,
      PrintStream logStream) throws IOException
   {
      logStream.println("modifying XSL files from: " + folder.getAbsolutePath()
         + "directory...");

      List xslFiles = getFilesOfType(folder, "*.xsl");
      for (int i = 0; i < xslFiles.size(); i++)
      {
         File file = (File) xslFiles.get(i);
         try
         {
            upgradeFile(file, testMode, logStream);
         }
         catch (Exception e)
         {
            logStream.println(e.getLocalizedMessage());
            e.printStackTrace(logStream);
         }
      }
   }

   /**
    * Modifies HTML files from the supplied folder. Calls
    * {@link #upgradeFile(File, boolean, PrintStream)} for each HTML file in the
    * folder.
    * 
    * @param folder HTML source folder, assumed not <code>null</code> and a
    * folder.
    * @param testMode <code>true</code> to run in test mode, i.e. don't
    * actually modifying the file and just report the changes.
    * @param logStream print stream for logging, assumed not <code>null</code>.
    * @throws IOException
    */
   private void upgradeHtmlFiles(File folder, boolean testMode,
      PrintStream logStream) throws IOException
   {
      logStream.println("modifying HTML files from: "
         + folder.getAbsolutePath() + "directory...");

      List htmlFiles = getFilesOfType(folder, "*.htm*");
      for (int i = 0; i < htmlFiles.size(); i++)
      {
         File file = (File) htmlFiles.get(i);
         try
         {
            upgradeFile(file, testMode, logStream);
         }
         catch (Exception e)
         {
            logStream.println(e.getLocalizedMessage());
            e.printStackTrace(logStream);
         }
      }
   }

   /**
    * Modifies a single file to replace the blank spaces in the slot names.
    * 
    * @param file the file to be modified, assumed not <code>null</code>.
    * @param testMode <code>true</code> to run in test mode i.e. do not
    * actally save the file but report the changes.
    * @param logStream print stream to write the log, assume dnot
    * <code>null</code>.
    * @throws MalformedURLException
    * @throws IOException
    */
   private void upgradeFile(File file, boolean testMode, PrintStream logStream)
      throws MalformedURLException, IOException
   {
      logStream.println("Modifying file: " + file.getAbsolutePath() + "...");

      Source source = new Source(IOTools.getFileContent(file));
      OutputDocument outputDocument = new OutputDocument(source);
      source.setLogWriter(new OutputStreamWriter(logStream));
      StringBuilder sbOld = new StringBuilder();
      StringBuilder sbNew = new StringBuilder();
      List allStartTags = source.findAllStartTags();
      for (Iterator i = allStartTags.iterator(); i.hasNext();)
      {
         StartTag sTag = (StartTag) i.next();
         Attributes attributes = sTag.getAttributes();

         if (attributes == null)
            continue;

         Iterator attrs = attributes.iterator();

         sbOld.setLength(0);
         sbNew.setLength(0);

         sbOld.append("<");
         sbNew.append("<");

         sbOld.append(sTag.getName());
         sbNew.append(sTag.getName());

         boolean modifiedAttrs = false;
         while (attrs.hasNext())
         {
            Attribute attr = (Attribute) attrs.next();
            String value = getModifiedValue(attr, sTag);
            if (value == null)
               value = attr.getValue();
            else
               modifiedAttrs = true;

            sbOld.append(" ");
            sbNew.append(" ");

            sbOld.append(attr.getName());
            sbNew.append(attr.getName());

            sbOld.append("=\"");
            sbNew.append("=\"");

            sbOld.append(attr.getValue());
            sbNew.append(value);

            sbOld.append("\"");
            sbNew.append("\"");

         }
         if (sTag.isEmptyElementTag())
         {
            sbOld.append("/");
            sbNew.append("/");
         }
         sbOld.append(">");
         sbNew.append(">");
         if (modifiedAttrs)
         {
            if (testMode)
            {
               logStream.println("Current tag is:");
               logStream.println(sbOld.toString());
               logStream.println("----");
               logStream.println("Replacemenet tag is:");
               logStream.println(sbNew.toString());
               logStream.println("----");
            }
            else
               outputDocument.add(new StringOutputSegment(sTag, sbNew
                  .toString()));
         }
      }
      if (testMode)
      {
         outputDocument.output(new OutputStreamWriter(logStream));
         logStream.flush();
      }
      else
      {
         // Save the file with UTF-8 encoding
         OutputStreamWriter writer = null;
         try
         {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
            outputDocument.output(writer);
         }
         finally
         {
            if (writer != null)
            {
               try
               {
                  writer.close();
               }
               catch (Exception e)
               {
               }
            }
         }
      }
   }

   /**
    * This method does the actual job of modifying the attribute value. Returns
    * modified value or <code>null</code> if not modified.
    * 
    * @param attr the current attribute which may need a modification, assumed
    * not <code>null</code>.
    * @param sTag Start tag that is the owner of the attribute. This can provide
    * additional context information to see whether the attribute value needs to
    * be changed. Assumed not <code>null</code>.
    * @return modified value for the attribute or <code>null</code> if not
    * modified.
    */
   private String getModifiedValue(Attribute attr, StartTag sTag)
   {
      String name = attr.getName();
      String value = attr.getValue();
      value = (value == null) ? "" : value;
      boolean modified = false;
      if (name.equals("slotname"))
      {
         String[] containedSlotNames = getContainedSlot(value);
         if (containedSlotNames != null)
         {
            value = value.replace(containedSlotNames[0], containedSlotNames[1]);
            modified = true;
         }
      }
      else if (name.equals("select"))
      {
         if (value.contains("@slotname"))
         {
            String[] containedSlotNames = getContainedSlot(value);
            if (containedSlotNames != null)
            {
               value = value.replace(containedSlotNames[0],
                     containedSlotNames[1]);
               modified = true;
            }
         }
      }
      if (modified)
         return value;

      return null;
   }

   /**
    * Returns the name of the slot the supplied string contains (anywhere in the
    * string) as well as the newly converted name from
    * {@link #m_oldNewNameModifiedMap}, which will be the replacement value.
    * The slot names are taken from the array {@link #m_sortedSlotNames} so that
    * h the search is done in the order of longest slot names to shortest.  The
    * comparison is case-insensitive.
    *  
    * @param value value to find the slot in, assumed not <code>null</code> or
    * empty.
    * @return array of string values, the first is the matching slot name in the
    * string, the second is the new slot name from
    * {@link #m_oldNewNameModifiedMap}, <code>null</code> if one does not exist.
    */
   private String[] getContainedSlot(String value)
   {
      String[] slotNames = new String[2];
      String valueUpper = value.toUpperCase();
      for (int i = 0; i < m_sortedSlotNames.length; i++)
      {
         String oldName = m_sortedSlotNames[i];
         int oldNameLength = oldName.length();
         int oldNameIndex = valueUpper.indexOf(oldName.toUpperCase());
         if (oldNameIndex != -1)
         {
            slotNames[0] = value.substring(oldNameIndex, oldNameIndex
                  + oldNameLength);
            slotNames[1] = (String) m_oldNewNameModifiedMap.get(oldName);
            return slotNames;
         }
      }
      return null;
   }

   /**
    * Helper function to load slot names from the database.
    * 
    * @param logStream print stream for logging, assumed not null.
    * 
    * @return List of all slot names in the system, never <code>null</code>
    * may be empty.
    */
   private static List updateLabelsAndLoadSlotNamesFromDatabase(
      PrintStream logStream) throws Exception
   {
      logStream.println("Filling empty slot labels...");
      Connection conn = RxUpgrade.getJdbcConnection();
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade
         .getRxRepositoryProps());

      String qualTableName = PSSqlHelper.qualifyTableName("RXSLOTTYPE", dbmsDef
         .getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      // First update the labels to have the values of slot names
      String updateStmt = "UPDATE " + qualTableName + " SET " + qualTableName
         + ".LABEL=" + qualTableName + ".SLOTNAME" + " WHERE " + qualTableName
         + ".LABEL IS NULL";
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(updateStmt);
      logStream.println("Filling empty slot labels finished");
      logStream.println();

      logStream.println("Querying the existing slot names...");
      // Then query the slot names
      String queryStmt = "SELECT " + qualTableName + ".SLOTNAME " + "FROM "
         + qualTableName;

      ResultSet rs = stmt.executeQuery(queryStmt);
      List result = new ArrayList();
      while (rs.next())
         result.add(rs.getString("SLOTNAME"));

      return result;
   }

   /**
    * Helper function to update the old slot names with new ones.
    * 
    * @param oldNewNameMap map of old and new names for the slots, assumed not
    * <code>null</code> may be empty.
    * @param logStream print stream for logging, assumed not null.
    */
   private static void updateSlotNamesInDatabase(Map oldNewNameMap,
      PrintStream logStream) throws Exception
   {
      logStream.println("Updating slot names with new ones...");

      Connection conn = RxUpgrade.getJdbcConnection();
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade
         .getRxRepositoryProps());

      String qualTableName = PSSqlHelper.qualifyTableName("RXSLOTTYPE", dbmsDef
         .getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());
      String updateStmt = "UPDATE " + qualTableName + " SET " + qualTableName
         + ".SLOTNAME=" + "?" + " " + " WHERE " + qualTableName + ".SLOTNAME="
         + "?";

      Iterator iter = oldNewNameMap.keySet().iterator();
      while (iter.hasNext())
      {
         String oldName = (String) iter.next();
         String newName = (String) oldNewNameMap.get(oldName);
         logStream.println("Replacing '" + oldName + "' with '" + newName
            + "'...");
         PreparedStatement stmt = conn.prepareStatement(updateStmt);
         stmt.setString(1, newName);
         stmt.setString(2, oldName);
         stmt.executeUpdate();
      }
      logStream.println("Updating slot names finished");
      logStream.println();
   }

   /**
    * Set of special app folder names to exclude from upgrade.
    */
   static private Set EXCLUDE_APP_SET = new HashSet();
   static
   {
      EXCLUDE_APP_SET.add("Administration");
      EXCLUDE_APP_SET.add("Docs");
      EXCLUDE_APP_SET.add("DTD");
      EXCLUDE_APP_SET.add("rx_reports");
      EXCLUDE_APP_SET.add("web_resources");
   }

   /**
    * Map of old and new slot names, old names being the keys.
    */
   private Map m_oldNewNameMap = null;

   /**
    * Array of old slot names (modified only) sorted by the length of the
    * string.
    */
   private String[] m_sortedSlotNames = null;

   /**
    * This will be the subset of m_oldNewNameMap in which the key and values are
    * different.
    */
   private Map m_oldNewNameModifiedMap = new HashMap();

   /**
    * Name of the property holding the list slot names in the property file
    * {@link #NAVIGATION_PROPERTIES}
    */
   private static final String NAVON_PROPERTY_SLOTNAMES = "navon.slotnames";

   /**
    * Path of the Navigation property files with respect to the Rx root.
    */
   private static final String NAVIGATION_PROPERTIES = "rxconfig/Server/Navigation.properties";

   /**
    * Main method for testing.
    * 
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      PSUpgradeDbAndHtmlAndXslFilesForSlotNames plugin = new PSUpgradeDbAndHtmlAndXslFilesForSlotNames();

      List fileList = null;

      File root = new File("f:/Rhythmyx");

      // search for all .htm* and .xsl files under Rx root
      fileList = plugin.getFilesOfType(new File(root, "ObjectStore"), "*.xml");

      System.out.println("Running in test mode. Files will not be changed");

      Iterator iter = fileList.iterator();
      while (iter.hasNext())
      {
         File appFile = (File) iter.next();

         String fileName = appFile.getName();
         String appFolder = fileName.substring(0, fileName.length()
            - ".xml".length());
         if (EXCLUDE_APP_SET.contains(appFolder)
            || appFolder.startsWith("sys_") || appFolder.startsWith("psx_"))
         {
            System.out.println("Skipping special application: " + appFolder);
            continue;
         }
         try
         {
            plugin.upgradeFolder(new File(root, appFolder), true, System.out);
         }
         catch (MalformedURLException e)
         {
            System.out.println(e.getLocalizedMessage());
         }
         catch (IOException e)
         {
            System.out.println(e.getLocalizedMessage());
         }
      }
   }
}
