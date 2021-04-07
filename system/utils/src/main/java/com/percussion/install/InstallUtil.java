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

package com.percussion.install;

import com.percussion.util.IOTools;
import com.percussion.util.PSOsTool;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSDriverHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.string.PSStringUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * The InstallUtil class contains some utility methods for the installer.
 */
public class InstallUtil
{

   private static final String JETTY_PERC_LIB="jetty/defaults/lib/perc/";
   private static final String JETTY_PERC_LOGGING="jetty/defaults/lib/perc-logging/";
   private static final String JETTY_BASE_LIB="jetty/base/lib/";
   private static final String JETTY_BASE_JDBC="jetty/base/lib/jdbc";
   private static final String JETTY_BASE_PERC="jetty/base/lib/perc";
   private static final String DTS_COMMON_LIB="Deployment/Server/common/lib";
   private static final String STAGING_DTS_COMMON_LIB="Staging/Deployment/Server/common/lib";

   private static ClassLoader expandClasspath(){

      URL[] urlList = new URL[7];


      try {
         urlList[0] = Paths.get(InstallUtil.m_rootDir + File.separator + JETTY_PERC_LIB).toAbsolutePath().toUri().toURL();
         urlList[1] = Paths.get(InstallUtil.m_rootDir + File.separator + JETTY_PERC_LOGGING).toAbsolutePath().toUri().toURL();
         urlList[2] = Paths.get(InstallUtil.m_rootDir + File.separator + JETTY_BASE_LIB).toAbsolutePath().toUri().toURL();
         urlList[3] = Paths.get(InstallUtil.m_rootDir + File.separator + JETTY_BASE_JDBC).toAbsolutePath().toUri().toURL();
         urlList[4] = Paths.get(InstallUtil.m_rootDir + File.separator + JETTY_BASE_PERC).toAbsolutePath().toUri().toURL();
         urlList[5] = Paths.get(InstallUtil.m_rootDir + File.separator + DTS_COMMON_LIB).toAbsolutePath().toUri().toURL();
         urlList[6] = Paths.get(InstallUtil.m_rootDir + File.separator + STAGING_DTS_COMMON_LIB).toAbsolutePath().toUri().toURL();
      } catch (MalformedURLException e) {
         PSLogger.logError(e.getMessage());
      }


      // ClassLoader loader = (ClassLoader) AccessController.doPrivileged(
      return new URLClassLoader(urlList,ClassLoader.getSystemClassLoader());
   }

   /**
    * Find and replace text in the tutorial. If either <code>strFind</code> or
    * <code>strReplace</code> is null, or if <code>strFind</code> is an empty
    * string, then nothing will happen.
    *
    * @param strFind the text to be found
    * @param strReplace the text to replace <code>strFind</code>
    * @param strFile the path name of a file containing the text to be found and
    *           replaced with (<code>null</code> path name is not allowed)
    */
   public static void findReplace(String strFind, String strReplace, String strFile)
   {
      if ((strFind == null) || (strReplace == null) || (strFind.length() == 0))
         return;

      if (strFind.equals(strReplace))
         return;

      System.out.println("find: + " + strFind + " replace: " + strReplace);
      File file = new File(strFile);

      try(FileInputStream in = new FileInputStream(file))
      {
         String strReadData = "";
         String strWriteData = "";
         int iAvail = in.available();
         byte[] bData = new byte[iAvail];
         in.read(bData, 0, iAvail);
         strReadData = new String(bData);

         StringBuilder buffer = new StringBuilder(strReadData);
         int replace = buffer.toString().indexOf(strFind);
         while (replace != -1)
         {
            buffer = buffer.replace(replace, replace + strFind.length(), strReplace);
            replace = buffer.toString().indexOf(strFind);
         }

         strWriteData = buffer.toString();

         try(FileWriter writer = new FileWriter(strFile)) {
            writer.write(strWriteData, 0, strWriteData.length());
         }
      } catch (IOException e)
      {
         PSLogger.logError(e.getMessage());
      }
   }

   /**
    * Convert a <code>project</code> file. Same as calling
    * <code>convertProject(project, null, null)</code>.
    *
    * @param project the path name of a file (not <code>null</code>)
    * @throws IOException if an error occurs converting the project
    */
   public static void convertProject(String project) throws IOException
   {
      convertProject(project, null, null);
   }

   /**
    * Convert a <code>project</code> file. In the file, the possible installer
    * drivers will first be found and replaced with the driver given by
    * <code>strCurDir</code>. Then the E2 directory will be replaced with the
    * directory of <code>strCurDir</code>. Finally, if <code>strDocDir</code> is
    * not <code>null</code>, the Docs\E2\Help\V2.0\ directory will be replaced
    * with the directory of <code>strDocDir</code>. Otherwise, this final step
    * will be ignored. The result is stored in <code>project</code>.
    *
    * @param project the path name of a file (not <code>null</code>)
    * @param strCurDir the current directory, if <code>null</code>, then the
    *           current user directory is assumed
    * @param strDocDir the document directory
    * @throws IOException if an error occurs
    */
   public static void convertProject(String project, String strCurDir, String strDocDir) throws java.io.IOException
   {
      if (strCurDir == null)
      {
         File curDir = new File(System.getProperty("user.dir"));
         strCurDir = curDir.getCanonicalPath();
      }

      // Jian Huang's comment: According to Mark d'Andrea, these back slashes
      // are internally generated so that we do not have to worry about Solaris.
      String[] drives = getPossibleDrives();
      String strReplace = "\"" + strCurDir.charAt(0) + ":\\";
      for (int iDrive = 0; iDrive < drives.length; ++iDrive)
      {
         String strFind = "\"" + drives[iDrive] + ":\\";
         findReplace(strFind, strReplace, project);
      }

      // now replace \\e2
      String strFind = ":\\\\e2\\\\";
      findReplace(strFind, addSlashes(strCurDir).substring(1) + "\\\\", project);

      strFind = ":\\\\E2\\\\";
      findReplace(strFind, addSlashes(strCurDir).substring(1) + "\\\\", project);

      // now replace the doc dir
      if (strDocDir != null)
      {
         strFind = ":\\\\docs\\\\e2\\\\help\\\\V2.0\\\\\\";
         findReplace(strFind, addSlashes(strDocDir).substring(1) + "\\\\", project);
      }
   }

   /**
    * Get the possible drivers, such as "M", "N", "S", etc.
    *
    * @return a string array contains the driver strings
    */
   public static String[] getPossibleDrives()
   {
      String[] drives = new String[12];
      drives[0] = "C";
      drives[1] = "M";
      drives[2] = "N";
      drives[3] = "Q";
      drives[4] = "S";
      drives[5] = "V";
      drives[6] = "c";
      drives[7] = "m";
      drives[8] = "n";
      drives[9] = "q";
      drives[10] = "s";
      drives[11] = "v";
      return (drives);

   }

   /**
    * Add a back slash after each back slash in <code>str</code>.
    *
    * @param str the input string (not <code>null</code>)
    * @return a new string with back slash being added into
    */
   public static String addSlashes(String str)
   {
      StringBuilder retval = new StringBuilder();
      for (int i = 0; i < str.length(); i++)
      {
         switch (str.charAt(i))
         {
            case '\\' :
               retval.append("\\\\");
               continue;
            default :
               retval.append(str.charAt(i));
               continue;
         }
      }
      return retval.toString();
   }

   /**
    * Checks if the database is setup for unicode. This is done by creating a
    * temporary table in the database and calling
    * {@link PSSqlHelper#supportsUnicode(Connection, String, String)} on this
    * table.
    *
    * @param conn The database connection object. May not be <code>null</code>.
    * @param driver The driver used for this connection. May not be
    *           <code>null</code>.
    * @param database The name of the database. May not be <code>null</code>.
    * @param schema The schema for this connection. May not be <code>null</code>
    *           .
    *
    * @return <code>true</code> if the database is setup for unicode,
    *         <code>false</code> otherwise.
    *
    * @throws SQLException
    */
   public static boolean checkForUnicode(Connection conn, String driver, String database, String schema)
           throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (driver == null)
         throw new IllegalArgumentException("driver may not be null");

      if (database == null)
         throw new IllegalArgumentException("database may not be null");

      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");

      boolean unicode = false;

      // use appropriate datatype according to backend
      String datatype = "varchar";

      if (driver.equals(PSJdbcUtils.JTDS_DRIVER) || driver.equals(PSJdbcUtils.SPRINTA))
         datatype = "nvarchar";

      String uniqueTable = "RXDUAL";
      String column = "GB";
      int i = 1;

      // find unique table name
      while (checkTableExists(uniqueTable, conn, database, schema))
      {
         uniqueTable = "RXDUAL" + i;
         i++;
      }

      // qualify table name
      uniqueTable = PSSqlHelper.qualifyTableName(uniqueTable, database, schema, driver);

      // construct create and delete SQL statements
      String create = "create table " + uniqueTable + " (" + column + " " + datatype + "(50))";
      String delete = "drop table " + uniqueTable;

      // create table
      executeStatement(conn, create);

      // check for unicode support
      unicode = PSSqlHelper.supportsUnicode(conn, uniqueTable, column);

      // delete table
      executeStatement(conn, delete);

      return unicode;
   }

   /**
    * Checks if the specified table exists for the specified database and
    * schema.
    *
    * @param table the table to look for, may not be <code>null<code>
    * @param conn the connection to the database, may not be <code>null<code>
    * @param database the name of the database, may not be <code>null<code>
    * @param schema the schema for this connection, may not be <code>null<code>
    *
    * @return <code>true</code> if the database contains the table,
    *         <code>false</code> otherwise.
    *
    * @throws SQLException
    */
   public static boolean checkTableExists(String table, Connection conn, String database, String schema)
           throws SQLException
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (database == null)
         throw new IllegalArgumentException("database may not be null");

      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");

      // check if table exists in the database
      boolean exists = false;
      ResultSet rs = null;
      try
      {
         if (conn != null)
         {
            String db = database;
            if (db.trim().length() < 1)
               db = null;

            DatabaseMetaData dbmd = conn.getMetaData();
            rs = dbmd.getTables(db, schema, table, new String[]
                    {"TABLE"});
            if ((rs != null) && (rs.next()))
               exists = true;
         }
      }
      catch (SQLException se)
      {
         if (rs != null)
            rs.close();
         throw se;
      }

      return exists;
   }

   /**
    * Executes the given SQL statement on the given connection.
    *
    * @param conn connection to the database, may not be <code>null</code>
    * @param statement specifies the action to be taken, may not be
    *           <code>null</code>
    *
    * @throws SQLException
    */
   public static void executeStatement(Connection conn, String statement) throws SQLException
   {

      try(Statement st = conn.createStatement()) {
         st.execute(statement);
      }
   }

   /**
    * Helper function that returns the first DOM child Element of the supplied
    * node.
    *
    * @param node - parent node that can only be of Document ot Element type,
    *           can be <code>null</code>.
    *
    * @param sElemName - the element tag name, can be <code>null</code>.
    *
    * @return - First DOM element with the supplied name, may be
    *         <code>null</code>. <code>null</code> if the parent node element
    *         name tag is <code>null</code>
    *
    */
   static public Element getElement(Node node, String sElemName)
   {
      if (null == node || null == sElemName || sElemName.trim().length() < 1)
         return null;

      NodeList nl = null;
      if (node instanceof Document)
      {
         nl = ((Document) node).getElementsByTagName(sElemName);
      }
      else if (node instanceof Element)
      {
         nl = ((Element) node).getElementsByTagName(sElemName);
      }
      else
      {
         return null;
      }

      if (null == nl || nl.getLength() < 1)
         return null;

      // we consider only the first text child for element value.
      return (Element) nl.item(0);
   }

   /**
    * Helper function that returns the value of the first child DOM Element of
    * the supplied DOM node.
    *
    * @param node - DOM node can be either Document or Element type, can not be
    *           <code>null</code>. If <code>null</code> or other type, the
    *           return value shall be empty string.
    *
    * @param sElemName - the element tag name, can not be <code>null</code>. If
    *           <code>null</code>, the return value shall be empty string.
    *
    * @return String - the value of the element, can be empty string.
    *
    */
   static public String getElemValue(Node node, String sElemName)
   {
      String value = "";

      if (null == node || null == sElemName || sElemName.trim().length() < 1)
         return value;

      NodeList nl = null;
      if (node instanceof Document)
      {
         nl = ((Document) node).getElementsByTagName(sElemName);
      }
      else if (node instanceof Element)
      {
         nl = ((Element) node).getElementsByTagName(sElemName);
      }
      else
      {
         return value;
      }
      if (null == nl || nl.getLength() < 1)
         return value;

      // we consider only the first text child for element value.
      Element elem = (Element) nl.item(0);
      if (elem == null)
         return value;
      Node temp = elem.getFirstChild();
      if (null == temp || Node.TEXT_NODE != temp.getNodeType())
         return value;

      return ((Text) temp).getData().trim();
   }

   /**
    * Helper function that returns the value of the given DOM Element.
    *
    * @param elem - DOM Element, can not be <code>null</code>. If
    *           <code>null</code>, the return value shall be empty string.
    *
    *
    * @return String - the value of the element, can be empty string.
    *
    */
   static public String getElemValue(Element elem)
   {
      String value = "";
      if (elem == null)
         return value;
      Node temp = elem.getFirstChild();
      if (null == temp || Node.TEXT_NODE != temp.getNodeType())
         return value;

      return ((Text) temp).getData().trim();
   }

   /**
    * Helper function that returns the ArrayList of values of the given child
    * element of a given node.
    *
    * @param node - DOM node can be either Document or Element type, can not be
    *           <code>null</code>. If <code>null</code> or other type, the
    *           return value shall be empty arraylist.
    *
    * @param childElemName - the element tag name, can not be <code>null</code>.
    *           If <code>null</code>, the return value shall be empty arraylist.
    *
    * @return - a list of element values, never <code>null</code>.
    *
    */

   static public ArrayList getValueList(Node node, String childElemName)
   {
      ArrayList list = new ArrayList();

      if (null == node || null == childElemName || childElemName.trim().length() < 1)
         return list;

      NodeList nl = null;
      if (node instanceof Document)
      {
         nl = ((Document) node).getElementsByTagName(childElemName);
      }
      else if (node instanceof Element)
      {
         nl = ((Element) node).getElementsByTagName(childElemName);
      }
      else
      {
         return list;
      }
      String value = "";
      for (int i = 0; null != nl && i < nl.getLength(); i++)
      {
         value = getElemValue((Element) nl.item(i));
         if (value != null && value.length() > 0)
         {
            list.add(value);
         }
      }
      return list;
   }

   /**
    * Method to restore version.properties, else in the next installer run,
    * plugins will not run. This is because installer will copy
    * Version.properties to PreviousVersion.properties and install a new
    * Version.properties file. Because of errors such as this one during
    * install, the tree is in a stale state. Next install will copy the failed
    * installation version into PreviousVersion.properties, thus ending up with
    * the upgrade plugin comparison version being the same and no upgrade
    * plugins will run. This method will copy PreviousVersion.properties into
    * version.properties then remove PreviousVersion.properties. This will at
    * least maintain the state of the tree prior to the failed upgrade.
    *
    * @param root The Rhythmyx root directory, may not be <code>null</code> or
    *           empty.
    *
    * @throws IOException if an error occurs during copy.
    * @throws FileNotFoundException if an error occurs during copy.
    */
   public static void restoreVersionPropertyFile(String root) throws IOException, FileNotFoundException
   {
      if (root == null || root.trim().length() == 0)
         throw new IllegalArgumentException("root may not be null or empty");

      File prevFile = new File(root + File.separator + PREVIOUS_VERSION_PROPS_FILE);
      File curFile = new File(root + File.separator + VERSION_PROPS_FILE);
      if (curFile.exists() && prevFile.exists())
      {
         copyFiles(prevFile, curFile);
         prevFile.deleteOnExit();
      }
   }

   /**
    * Method to backup rxrepository.properties as
    * {@link #ORIG_REPOSITORY_PROPS_FILE}.
    *
    * @param root The Rhythmyx root directory, may not be <code>null</code> or
    *           empty.
    *
    * @throws IOException if an error occurs during copy.
    * @throws FileNotFoundException if an error occurs during copy.
    */
   public static void backupRepositoryPropertyFile(String root) throws IOException, FileNotFoundException
   {
      if (root == null || root.trim().length() == 0)
         throw new IllegalArgumentException("root may not be null or empty");

      File prevFile = new File(root, ORIG_REPOSITORY_PROPS_FILE);
      File curFile = new File(root, REPOSITORY_PROPS_FILE);
      if (curFile.exists())
      {
         IOTools.copyFileStreams(curFile, prevFile);
      }
   }

   /**
    * Method to restore rxrepository.properties, else if the current 5.x
    * installation is not compatible for upgrade and the upgrade is halted, the
    * server will be left in a state in which it fails to start. This is because
    * the installer will load the current properties into
    * rxrepository.properties in preparation for upgrade. Part of this includes
    * re-encrypting the database password using the 6.x encryption scheme. This
    * will ensure that the server starts in the event of a failed compatibility
    * scan.
    *
    * @param root The Rhythmyx root directory, may not be <code>null</code> or
    *           empty.
    *
    * @throws IOException if an error occurs during copy.
    * @throws FileNotFoundException if an error occurs during copy.
    */
   public static void restoreRepositoryPropertyFile(String root) throws IOException, FileNotFoundException
   {
      if (root == null || root.trim().length() == 0)
         throw new IllegalArgumentException("root may not be null or empty");

      File prevFile = new File(root, ORIG_REPOSITORY_PROPS_FILE);
      File curFile = new File(root, REPOSITORY_PROPS_FILE);
      if (prevFile.exists())
      {
         IOTools.copyFileStreams(prevFile, curFile);
         prevFile.delete();
      }
   }

   /**
    * Utility method to copy infile to outfile
    *
    * @param in : input file, may not be <code>null</code>.
    * @param out : output file, may not be <code>null</code>.
    *
    * @throws IOException if an error occurs during copy.
    * @throws FileNotFoundException if either of the files cannot be found.
    */
   public static void copyFiles(File in, File out) throws IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in may not be null or empty");

      if (out == null)
         throw new IllegalArgumentException("out may not be null or empty");

      try(FileChannel sourceChannel = new FileInputStream(in).getChannel()) {
         try(FileChannel destinationChannel = new FileOutputStream(out).getChannel()) {
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
         }
      }
   }

   /**
    * Modifies the supplied name as below, according to naming conventions:
    * <p>
    * <ol>
    * <li>Replace all "-" with " "</li>
    * <li>Calls {@link #convertSpaceToUnderscore(String)} to replace spaces with
    * underscores.</li>
    * </ol>
    *
    * @param oldName old name to modify, may be <code>null</code> or empty.
    * @return new name after modifying as described above, may be
    *         <code>null</code> or empty if supplied one <code>null</code> or
    *         empty.
    */
   public static String modifyName(String oldName)
   {
      if (oldName == null)
         return oldName;

      return convertSpaceToUnderscore(oldName.replace("-", " "));
   }

   /**
    * Modifies the supplied string as below:
    * <p>
    * <ol>
    * <li>Replace all " " with "_"</li>
    * <li>Collapse continuous "_"s into one "_"</li>
    * </ol>
    *
    * @param str string to modify, may be <code>null</code> or empty.
    * @return new string after modifying as described above, may be
    *         <code>null</code> or empty if supplied one <code>null</code> or
    *         empty.
    */
   public static String convertSpaceToUnderscore(String str)
   {
      if (str == null)
         return str;

      str = str.replace(" ", "_");
      while (str.indexOf("__") != -1)
         str = str.replace("__", "_");
      return str;
   }

   /**
    * Helper function adds xml processing instruction and DOCTYPE to the given
    * xml string.
    *
    * @param str xml string to be modified, may not be <code>null</code>
    * @param root element to which the DOCTYPE will be applied, may not be
    *           <code>null</code> or empty
    * @param type the type of the DOCTYPE specification, acceptable values are
    *           SYSTEM and PUBLIC
    * @param dtd the dtd reference, may not be <code>null</code> or empty
    * @return the modified string.
    */
   public static String addDocType(String str, String root, String type, String dtd)
   {
      if (str == null)
         throw new IllegalArgumentException("str may not be null");
      if (root == null || root.trim().length() == 0)
         throw new IllegalArgumentException("root may not be null or empty");
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      else
      {
         if (!type.equals("SYSTEM") && !type.equals("PUBLIC"))
            throw new IllegalArgumentException("type must be SYSTEM or PUBLIC");
      }
      if (dtd == null || dtd.trim().length() == 0)
         throw new IllegalArgumentException("dtd may not be null or empty");

      int loc1 = str.indexOf("<?xml");
      if (loc1 == -1)
      {
         str = "<?xml version = \"1.0\" encoding=\"UTF-8\"?>\n" + str;
      }
      int loc2 = str.indexOf("<!DOCTYPE " + root);
      if (loc2 == -1)
      {
         int loc3 = str.indexOf("?>");
         {
            String left = str.substring(0, loc3 + 2);
            String right = str.substring(loc3 + 2);
            str = left + "\n<!DOCTYPE " + root + " " + type + " \"" + dtd + "\">\n" + right;
         }

      }
      return str;
   }

   /**
    * If the installation or upgrade is to a directory that already has the
    * server installed.
    *
    * @param instDir the directory this installation is performed never
    *           <code>null</code> or empty
    * @return <code>true</code> if the installation location specified already
    *         has the server installed, <code>false</code> otherwise.
    */
   public static boolean isServerInstallation(String instDir)
   {
      boolean isServerInstalled = false;

      if (instDir == null || instDir.length() == 0)
         throw new IllegalArgumentException("install location may not be " + "null or empty");

      String serverExe = null;
      if (PSOsTool.isWindowsPlatform())
         serverExe = "PercussionServer.exe";
      else
         serverExe = "StartServer.sh";

      File serverF = new File(instDir + "/" + serverExe);
      if (serverF.exists())
         isServerInstalled = true;

      return isServerInstalled;
   }

   /**
    * Helper method for validation to advise if the server is still running.
    * First checks if <code>dirName</code> is a valid server installation.
    *
    * @param dirName the install location specified either for upgrade or new
    *           install, never <code>null</code> or empty.
    * @return <code>true</code> if the server is running, <code>false</code>
    *         otherwise
    */
   public static boolean isServerRunning(String dirName)
   {
      if (dirName == null || dirName.length() == 0)
         throw new IllegalArgumentException("install location may not be " + "null or empty");
      if (!InstallUtil.isServerInstallation(dirName))
         return false;

      return checkServerRunning(dirName);
   }

   /**
    * Helper method for validation to advise if the server is still running.
    *
    * @param dirName the install location specified either for upgrade or new
    *           install, never <code>null</code> or empty.
    * @return <code>true</code> if the server is running, <code>false</code>
    *         otherwise
    */
   public static boolean checkServerRunning(String dirName)
   {
      boolean isRunning = false;

      if (dirName == null || dirName.length() == 0)
         throw new IllegalArgumentException("install location may not be " + "null or empty");

      Properties prop = new Properties();
      try
      {
         // First Check the Port. We have to read the config file, and then
         // check
         // if the port is active
         prop.load(new FileInputStream(dirName + File.separator + "rxconfig" + File.separator + "server"
                 + File.separator + "server.properties"));
         String bindPort = prop.getProperty("bindPort");
         if (bindPort == null || bindPort.isEmpty())
         {
            bindPort = "9992";
         }
         isRunning = !portAvailable(Integer.parseInt(bindPort));
         if (!isRunning)
         {
            isRunning = isDerbyRunning(dirName);
         }
      }
      catch (IOException ex)
      {
         isRunning = false;
      }

      return isRunning;
   }

   /**
    * THis is a utility method to shutdown Derby Server.
    * CMS-5932
    */
   public static void shutDownDerby(){

      try (Connection cn = DriverManager.getConnection("jdbc:derby:;shutdown=true")){
      } catch (SQLException e) {
         if ("XJ015".equals(e.getSQLState())) {
            PSLogger.logInfo( "Derby shutdown succeeded. SQLState=" + e.getSQLState() );
            return;
         }
      }
   }

   public static boolean isDerbyRunning(String dirName)
   {
      boolean isRunning = false;

      // Derby Check
      String pathToRsDx = dirName + File.separator + "AppServer" + File.separator + "server" + File.separator + "rx"
              + File.separator + "deploy" + File.separator + "rx-ds.xml";
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder;
      try
      {
         docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
         docBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);

         docBuilder = docBuilderFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(new File(pathToRsDx));
         NodeList driverList = doc.getElementsByTagName("driver-class");
         if (driverList.getLength() > 0)
         {
            String driverName = driverList.item(0).getTextContent();
            if (driverName.contains("derby"))
            {
               NodeList urls = doc.getElementsByTagName("connection-url");
               if (urls.getLength() > 0)
               {
                  String uri = urls.item(0).getTextContent();
                  String cleanUri = uri.replace("jdbc:", "");
                  URI uriObject = URI.create(cleanUri);
                  Integer derbyPort = uriObject.getPort();
                  if (derbyPort == null)
                     derbyPort = 1527;
                  isRunning = !portAvailable(derbyPort);
               }
            }
         }
      }
      catch (Exception e)
      {
         isRunning = false;
      }
      return isRunning;
   }

   /**
    * Checks to see if a specific port is available. From Apache Camel, removed
    * max / min port arguments
    *
    * @param port the port to check for availability
    */
   @SuppressFBWarnings("UNENCRYPTED_SERVER_SOCKET") //Is just a port check no TLS required
   public static boolean portAvailable(int port)
   {
      try (ServerSocket ss = new ServerSocket(port)) {
         ss.setReuseAddress(true);
         try (DatagramSocket ds = new DatagramSocket(port)) {
            ds.setReuseAddress(true);
         }
         return true;
      } catch (IOException e) {
         logError("Port Availability Check Failed." + e.getMessage());
      }

      return false;
   }

   public static boolean checkTomcatServerRunning(String dirName)
   {
      boolean isRunning = false;
      String pathToServerConf = dirName + File.separator + "Deployment" + File.separator + "Server" + File.separator
              + "conf" + File.separator + "server.xml";
      if (dirName == null || dirName.length() == 0)
         throw new IllegalArgumentException("install location may not be " + "null or empty");

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder;
      try
      {
         docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
         docBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
         docBuilder = docBuilderFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(new File(pathToServerConf));
         NodeList connectorList = doc.getElementsByTagName("Connector");
         int i = 0;
         while (i < connectorList.getLength()) {
            NamedNodeMap serverAttributes = connectorList.item(i).getAttributes();
            Node portNode = serverAttributes.getNamedItem("port");
            if (portNode != null) {
               String port = portNode.getTextContent();
               isRunning = !portAvailable(Integer.parseInt(port));
               if (isRunning)
               {
                  i = connectorList.getLength();
               }
            }
            i++;
         }
      }
      catch (Exception ex)
      {
         isRunning = false;
      }

      return isRunning;
   }

   /**
    * Locates the Version.properties and copies to PreviousVersion.properties.
    * The order of lookup is as follows: 1) lib/rxserver.jar 2)
    * RxServices/WEB-INF/lib/rxclient.jar 3)
    * rxapp.ear/rxapp.war/WEB-INF/lib/rxserver.jar 4)
    * RxServices.war/WEB-INF/lib/rxclient.jar 5) RxServices.war
    *
    * @param strRootDir The Rhythmyx root installation directory, may not be
    *           <code>null</code> or empty.
    */
   public static void writePreviousVersion(String strRootDir)
   {
      if (strRootDir == null || strRootDir.trim().length() == 0)
         throw new IllegalArgumentException("strRootDir may not be null or " + "empty");

      File rxclientFile = null;
      String jettyJar = null;
      File checkDir =  new File(RxFileManager.JETTY_VERSION_JAR_FILE_DIR);
      if (checkDir.exists()) {
         File[] files = checkDir.listFiles((FileFilter) new PrefixFileFilter("perc-ant-", IOCase.SENSITIVE));
         if (files.length>1)
         {
            PSLogger.logError("Found multiple perc-ant jar files in "+checkDir.getAbsolutePath());
         }
         if (files.length==1)
            jettyJar = files[0].getAbsolutePath();
      }

      String[] jarCheckList = new String[]{ jettyJar,
              RxFileManager.VERSION_JAR_FILE_PUB,RxFileManager.VERSION_JAR_FILE_6X,RxFileManager.VERSION_JAR_FILE_PUB_6X,RxFileManager.RXSERVICES_WAR};
      try {
         JarFile jar = null;
         JarEntry jarEntry = null;
         for (String checkFile : jarCheckList) {
            if (checkFile == null)
               continue;
            // Look in lib/rxserver.jar
            File jarFile = new File(strRootDir + File.separator + checkFile);

            PSLogger.logInfo("Attempting to locate Version.properties in " + jarFile.getPath());


            if (jarFile.exists()) {
               jar = new JarFile(jarFile);
               jarEntry = jar.getJarEntry(RxFileManager.VERSION_FILE);
               break;
            }
         }

         if (jarEntry != null) {
            PSLogger.logInfo("Located Version.properties");

            try (InputStream in = jar.getInputStream(jarEntry)) {
               Properties verProp = new Properties();
               verProp.load(in);
               try (FileOutputStream out = new FileOutputStream(
                       new File(strRootDir + File.separator + RxFileManager.PREVIOUS_VERSION_PROPS_FILE))) {
                  verProp.store(out, "Written by installer for upgrade purpose");


                  try {
                     // Set the major version for this installation
                     ms_majorVersion = Integer.parseInt(verProp.getProperty("majorVersion",
                             String.valueOf(InstallUtil.ms_majorVersion)));

                  } catch (NumberFormatException nfe) {
                     PSLogger.logError("Error parsing majorVersion");
                  }

               }

            }
         }else{
            PSLogger.logInfo("Could not locate Version.properties");
         }
      }
      catch (IOException e)
      {
         PSLogger.logInfo("ERROR : " + e.getMessage());
         PSLogger.logInfo(e);
      }
   }

   /**
    * Returns the brand code, never <code>null</code> since the installation
    * cannot proceed unless a valid brand code is provided by the user.
    *
    * @param rootDir the Rhythmyx root directory, never <code>null</code> or
    *           empty.
    * @return the brand code.
    */
   public static Code fetchBrandCode(String rootDir)
   {
      if (rootDir == null || rootDir.trim().length() == 0)
         throw new IllegalArgumentException("rootDir may not be null or empty");

      Code c = null;
      RxFileManager fileManager = new RxFileManager(rootDir);
      String fileName = fileManager.getInstallationPropertyFile();

      try
      {
         PSProperties props = new PSProperties(fileName);
         String bc = props.getProperty("BRANDCODE");

         c = new Code(bc);
      }
      catch (Exception ex)
      {
         PSLogger.logInfo(ex.getMessage());
      }

      return c;
   }

   /**
    * Obtains connection using the derby connection information. The caller of
    * this function is responsible for closing the connection.
    */
   public static Connection createDerbyConnection()
   {
      return InstallUtil.createConnection("derby", "//localhost:1527/CMDB", "CMDB", "demo");
   }

   /**
    * Obtains connection using the connection information supplied. The caller
    * of this function is responsible for closing the connection.
    *
    * @param strDriver the database driver to connect with, may not be
    *           <code>null</code>.
    * @param strServer the server to connect to, may not be <code>null</code>.
    * @param strUser the user id for the connection, may not be
    *           <code>null</code>.
    * @param strPassword the password for the user, may not be <code>null</code>
    *           .
    *
    * @return the database connection, may be <code>null</code> if database
    *         connection failed.
    */
   public static Connection createConnection(String strDriver, String strServer, String strUser, String strPassword)
   {
      if (strDriver == null)
         throw new IllegalArgumentException("strDriver may not be null");

      if (strServer == null)
         throw new IllegalArgumentException("strServer may not be null");

      if (strUser == null)
         throw new IllegalArgumentException("strUser may not be null");

      if (strPassword == null)
         throw new IllegalArgumentException("strPassword may not be null");

      Connection conn = null;
      try
      {
         conn = createConnection(strDriver, strServer, null, strUser, strPassword);
      }
      catch (SQLException sqle)
      {
         conn = null;
         String msg = "Database Connection failed : " + sqle.getMessage();
         logError(msg);
      }
      return conn;
   }

   private static void logError(String msg)
   {
      if (ms_isSilentInstall)
         PSLogger.logError(msg);
      else
         System.out.println(msg);
   }

   private static void logInfo(String msg)
   {
      if (ms_isSilentInstall)
         PSLogger.logInfo(msg);
      else
         System.out.println(msg);
   }

   /**
    * Adds the jar file URL to the internal list of jdbc driver URLs.
    *
    * @param driverLocation the absolute path of the jar file URL to be added to
    *           the internal list of jdbc driver URLs, may not be blank.
    */
   public static void addJarFileUrl(String driverLocation)
   {
      if (StringUtils.isBlank(driverLocation))
      {
         throw new IllegalArgumentException("driverLocation may not be blank");
      }

      String jarFilePath = driverLocation;
      String strUrl = "jar:file:";
      strUrl += jarFilePath;
      strUrl += "!/";
      List m_jarUrls = null;
      try
      {
         if (m_jarUrls == null)
         {
            m_jarUrls = new ArrayList<URL>();
         }

         m_jarUrls.add(new URL(strUrl));
      }
      catch (MalformedURLException e)
      {
         PSLogger.logError(e.getLocalizedMessage());
      }
   }

   /**
    * Create a new database connection for the provided driver type. Use the
    * given server configuration to get the driver class and loads that driver.
    * The caller of this function is responsible for closing the connection.
    *
    * @param driver the db driver type
    * @param server the database server
    * @param db the database to be used
    * @param uid the user Id
    * @param pw the user password
    * @return the created connection, may be <code>null</code>
    * @throws SQLException if any database related error occurred
    */

   public static Connection createLoadedConnection(String driver, String server, String db, String uid, String pw)
           throws SQLException
   {
      String className = null;
      Class driverClass = null;
      className = RxInstallerProperties.getResources().getString(driver);

      try {
         if (driver.equalsIgnoreCase(PSJdbcUtils.MYSQL)) {
            if (m_extDriver == null) {
               if (m_jarUrls == null || m_jarUrls.isEmpty()) {
                  // Likely an upgrade, set default driver
                  //CMS location
                  File extDriver = new File(m_rootDir + PSJdbcUtils.MYSQL_DRIVER_LOCATION);

                  //DTS Location
                  if (!extDriver.exists()) {
                     extDriver = new File(m_rootDir + PSJdbcUtils.MYSQL_DTS_DRIVER_LOCATION);
                  }

                  //Staging DTS location
                  if (!extDriver.exists()) {
                     extDriver = new File(m_rootDir + PSJdbcUtils.MYSQL_STAGING_DTS_DRIVER_LOCATION);
                  }

                  String extDriverLocation = extDriver.getAbsolutePath();
                  if (!extDriver.exists()) {
                     logError("Cannot find MySQL driver at " + extDriverLocation);
                  } else {
                     try {
                        PSDriverHelper.getDriver(className, extDriverLocation);
                        addJarFileUrl(extDriverLocation);
                     } catch (ClassNotFoundException e) {
                        logError("Cannot find MySQL driver at " + extDriverLocation);
                     }
                  }
               }

               if (m_jarUrls != null) {
                  try {
                     int size = m_jarUrls.size();
                     URL urlList[] = new URL[size];
                     for (int i = 0; i < size; i++) {
                        InstallUtil.logInfo("Loading " + m_jarUrls.get(i));
                        urlList[i] = (URL) m_jarUrls.get(i);
                     }

                     ClassLoader loader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                        @Override
                        public Object run() {
                           return new URLClassLoader(urlList);
                        }
                     });

                     driverClass = Class.forName(className, true, loader);
                     InstallUtil.logInfo("Loaded " + className);
                     if (driverClass != null) {
                        Object objDriver = driverClass.newInstance();
                        if (objDriver != null) {
                           if (objDriver instanceof Driver)
                              m_extDriver = (Driver) objDriver;
                        }
                     }
                  } catch (InstantiationException ie) {
                     logError("InstantiationException : " + ie.getMessage());
                     driverClass = null;
                  } catch (IllegalAccessException iae) {
                     logError("IllegalAccessException : " + iae.getMessage());
                     driverClass = null;
                  } catch (NoSuchFieldError err) {
                     logError("NoSuchFieldError : " + err.getMessage());
                     driverClass = null;
                  } catch (Exception e) {
                     logError("Exception : " + e.getMessage());
                     driverClass = null;
                  }
               }

            }
         } else {
            if (m_extDriver == null) {
               Path dir = Paths.get(m_rootDir, PSJdbcUtils.DEFAULT_JDBC_DRIVER_LOCATION);
               if (Files.exists(dir)) {

               } else if (Files.exists(Paths.get(m_rootDir, PSJdbcUtils.DEFAULT_DTS_DRIVER_LOCATION))) {
                  dir = Paths.get(m_rootDir, PSJdbcUtils.DEFAULT_DTS_DRIVER_LOCATION);
               } else if (Files.exists(Paths.get(m_rootDir, PSJdbcUtils.DEFAULT_STAGING_DTS_DRIVER_LOCATION))) {
                  dir = Paths.get(m_rootDir, PSJdbcUtils.DEFAULT_STAGING_DTS_DRIVER_LOCATION);
               }

               try {
                  List<File> files = Files.list(dir).map(Path::toFile)
                          .collect(Collectors.toList());

                  URL[] urlList = new URL[files.size()];
                  int i = 0;
                  for (File f : files) {
                     urlList[i] = f.toURI().toURL();
                     i++;
                  }
                  ClassLoader loader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                     @Override
                     public Object run() {
                        return new URLClassLoader(urlList);
                     }
                  });

                  System.setProperty("jdbc.drivers", className);
                  driverClass = Class.forName(className, true, loader);
                  Object objDriver = driverClass.newInstance();
                  logInfo("Successfully loaded driver " + className);
                  if (objDriver instanceof Driver) {
                     m_extDriver = (Driver) objDriver;
                      DriverManager.registerDriver((Driver) objDriver);
                   } else {
                     logError(objDriver.getClass().getName() + " is not a valid Driver!");
                  }

               } catch (InstantiationException ie) {
                  logError("InstantiationException : " + ie.getMessage());
                  ie.printStackTrace();
               } catch (IllegalAccessException iae) {
                  logError("IllegalAccessException : " + iae.getMessage());
                  iae.printStackTrace();
               } catch (NoSuchFieldError err) {
                  logError("NoSuchFieldError : " + err.getMessage());
                  err.printStackTrace();
               } catch (IOException e) {
                  e.printStackTrace();
                  logError(e.getMessage());
               }
            }
         }
      }
      catch (ClassNotFoundException cls)
      {
         cls.printStackTrace();
         logError("Could not find the driver class : " + className);
         logError("Exception : " + cls.getMessage());
         throw new SQLException("JDBC driver class not found. " + cls.toString());
      }
      catch (LinkageError link)
      {
         link.printStackTrace();
         logError("linkage error");
         logError("Exception : " + link.getMessage());
         throw new SQLException("JDBC driver could not be loaded. " + link.toString());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         logError("Exception : " + e.getMessage());
         throw new SQLException("Exception. " + e.toString());
      }
      String dbUrl = PSJdbcUtils.getJdbcUrl(driver, server);
      Properties props = PSSqlHelper.makeConnectProperties(dbUrl, db, uid, pw);
      logInfo("Connecting to: URL= " + dbUrl + " UID =" + uid );
      Connection conn;
      if(m_extDriver == null) {
         conn = DriverManager.getConnection(dbUrl, props);
      }else{
         logInfo("Connecting with direct driver.");
         conn = m_extDriver.connect(dbUrl,props);
      }
      if (conn != null)
      {
         if (db != null)
            conn.setCatalog(db);
      }else{
         logError("Unable to establish database connection.");
      }

      return conn;


   }

   /**
    * Create a new database connection for the provided driver type. Use the
    * given server configuration to get the driver class. Determines if drive is
    * a supplied driver or a driver that we have to load. The caller of this
    * function is responsible for closing the connection.
    *
    * @param driver the db driver type, may not be <code>null</code>.
    * @param server the database server, may not be <code>null</code>.
    * @param db the database to be used, may be <code>null</code>.
    * @param uid the user Id, may not be <code>null</code>.
    * @param pw the user password, may not be <code>null</code>.
    *
    * @return the created connection, may be <code>null</code>.
    * @throws SQLException if any database related error occurred
    */
   public static Connection createConnection(String driver, String server, String db, String uid, String pw)
           throws SQLException
   {

      if(driver.equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER)){
         /** Note: Apparently in the 5.4 version an arbitrary decision was made to switch to the
          * embedded Derby jdbc driver as the default.  This of course breaks on upgrade as the
          * previous version deployed Derby in networked mode by default.  So we will switch to the
          * embedded defaults in this case.
          */

         if(!server.equalsIgnoreCase(RxInstallerProperties.getString("embedded.db_server_name"))) {
            logInfo("Switching Apache Derby configuration to embedded mode for upgrade...");
            server = RxInstallerProperties.getString("embedded.db_server_name");
            db = "";
            uid =  RxInstallerProperties.getString("embedded.user_id");
            pw =   RxInstallerProperties.getString("embedded.pwd");
            String derbyHome = Paths.get(InstallUtil.m_rootDir + File.separator + "Repository").toAbsolutePath().toString();
            logInfo("Setting Derby home to: " + derbyHome);
            System.setProperty("derby.system.home", derbyHome);
         }
      }

      return createLoadedConnection(driver, server, db, uid, pw);
   }

   /**
    * Create a new database connection for the provided driver type. Use the
    * given server configuration to get the driver class. The caller of this
    * function is responsible for closing the connection.
    *
    * @param driver the db driver type, may not be <code>null</code>.
    * @param server the database server, may not be <code>null</code>.
    * @param db the database to be used, may be <code>null</code>.
    * @param uid the user Id, may not be <code>null</code>.
    * @param pw the user password, may not be <code>null</code>.
    *
    * @return the created connection, may be <code>null</code>.
    * @throws SQLException if any database related error occurred
    */
   public static Connection createStandardConnection(String driver, String server, String db, String uid, String pw)
           throws SQLException
   {
      if (driver == null)
         throw new IllegalArgumentException("driver may not be null");

      if (server == null)
         throw new IllegalArgumentException("server may not be null");

      if (uid == null)
         throw new IllegalArgumentException("uid may not be null");

      if (pw == null)
         throw new IllegalArgumentException("pw may not be null");

      String className;
      Class driverClass;

      String strJTdsSqlServerDesc = RxInstallerProperties.getResources().getString("jtdssqlserverdesc");

      if (driver.equals(PSJdbcUtils.ORACLE))
         className = RxInstallerProperties.getResources().getString("oracle");
      else if (driver.equals(strJTdsSqlServerDesc))
         className = RxInstallerProperties.getResources().getString("jtds");
      else
         className = RxInstallerProperties.getResources().getString(driver);

      try
      {
         Class.forName(className);
      }
      catch (ClassNotFoundException cls)
      {
         logError("Could not find the driver class : " + className);
         logError("Exception : " + cls.getMessage());
         throw new SQLException("JDBC driver class not found. " + cls.toString());
      }
      catch (LinkageError link)
      {
         logError("linkage error");
         logError("Exception : " + link.getMessage());
         throw new SQLException("JDBC driver could not be loaded. " + link.toString());
      }
      catch (Exception e)
      {
         logError("Exception : " + e.getMessage());
         throw new SQLException("Exception. " + e.toString());
      }

      ClassLoader cl = ClassLoader.getSystemClassLoader();

      URL[] urls = ((URLClassLoader)cl).getURLs();

      logInfo("System class path:");
      for(URL url: urls){
         logInfo(url.getFile());
      }

      ClassLoader clExt = expandClasspath();
      urls = ((URLClassLoader)clExt).getURLs();

      logInfo("Extended class path:");
      for(URL url: urls){
         logInfo(url.getFile());
      }

      String dbUrl = PSSqlHelper.getJdbcUrl(driver, server);
      Properties props = PSSqlHelper.makeConnectProperties(dbUrl, db, uid, pw);

      logInfo("Connecting to: " + dbUrl);

      //Set the jdbc driver classname property to include the driver in case it is not a type 4 driver.
      System.setProperty("jdbc.drivers",className);
      try {
         driverClass = Class.forName(className, true, clExt);

         if (driverClass != null) {
            Object objDriver = driverClass.newInstance();
            if (objDriver != null) {
               logInfo("Successfully loaded driver " + className);
               if (objDriver instanceof Driver) {
                  m_extDriver = (Driver) objDriver;
                  logInfo(objDriver.getClass().getName() + " is a valid driver.");

                  DriverManager.registerDriver((Driver) objDriver);
                  logInfo("Registered driver: " + className);

               } else {
                  logError(objDriver.getClass().getName() + " is not a valid Driver!");
               }
            } else {
               logError("Unable to instantiate driver: " + driverClass);
            }

         } else {
            logError("Unable to load configured driver class:" + className + " from classpath.");
         }
      } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
         logError(e.getMessage());
      }
      Connection conn;
      if(m_extDriver == null) {
         conn = DriverManager.getConnection(dbUrl, props);
      }else{
         logInfo("Connecting with direct driver.");
         conn = m_extDriver.connect(dbUrl,props);
      }
      if (conn != null)
      {
         if (db != null)
            conn.setCatalog(db);
      }else{
         logError("Unable to establish database connection.");
      }

      return conn;
  }

   /**
    * Converts a variable name to the InstallAnywhere format, which is:
    * $VARIABLE$. This value may then be resolved to find the value of the
    * variable.
    *
    * @param var name of the InstallAnywhere variable, may not be
    *           <code>null</code> or empty.
    *
    * @return the InstallAnywhere variable ready for resolution, never
    *         <code>null</code> or empty.
    */
   public static String getSubstituteName(String var)
   {
      if (var == null || var.trim().length() == 0)
      {
         throw new IllegalArgumentException("var may not be null or empty");
      }

      return '$' + var + '$';
   }

   /**
    * Creates an InstallAnywhere variable name for an Rx custom code
    * implementation class.
    *
    * @param classname the name of the custom code class, may not be
    *           <code>null</code> or empty.
    * @param var the variable name, may not be <code>null</code> or empty.
    *
    * @return an InstallAnywhere variable in the form CLASS_VARIABLE, never
    *         <code>null</code> or empty.
    */
   public static String getVariableName(String classname, String var)
   {
      if (classname == null || classname.trim().length() == 0)
      {
         throw new IllegalArgumentException("classname may not be null or " + "empty.");
      }

      if (var == null || var.trim().length() == 0)
      {
         throw new IllegalArgumentException("var may not be null or " + "empty.");
      }

      String cName = classname;
      int index = classname.lastIndexOf('.');
      if (index != -1 && index < classname.length() - 1)
         cName = classname.substring(index + 1);

      return cName + "_" + var;
   }

   /**
    * Determines if a given port is valid and bindable.
    *
    * @param port may be <code>null</code> or <code>empty</code>.
    * @return <code>true</code> if server socket was successfully bound to a
    *         given port, <code>false</code> otherwise.
    */
   @SuppressFBWarnings("UNENCRYPTED_SERVER_SOCKET") //Is just a port check no TLS required
   public static boolean isBindableTcpPort(String port)
   {
      if (port == null || port.trim().length() == 0)
         return false;

      try(ServerSocket  s = new ServerSocket(Integer.parseInt(port))){

         return s.isBound();
      } catch (IOException e) {
         PSLogger.logError("Given Port is in use: "+ e.getMessage() + port );
      } catch (NumberFormatException e)
      {
         PSLogger.logError("Given Invalid port: " + e.getMessage() + port );
      }
      return false;
   }

   /**
    * Returns machine host name.
    *
    * @return machine host name, never <code>null</code>, never
    *         <code>empty</code>. Defaults to "localhost".
    */
   public static String getMyHostName()
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (Exception e)
      {
         String msg = "Failed to get machine host name. " + e.getLocalizedMessage();

         PSLogger.logError(msg);
      }

      return "localhost";
   }

   /**
    * Gets the fully qualified domain name for this IP address.
    *
    * @return machine domain name, never <code>null</code>, may be
    *         <code>empty</code>.
    */
   public static String getMyDomainName()
   {
      try
      {
         String cHostName = InetAddress.getLocalHost().getCanonicalHostName();

         if (cHostName.indexOf('.') > 0)
            return cHostName.substring(cHostName.indexOf('.') + 1);
         else
            return "";
      }
      catch (Exception e)
      {
         String msg = "Failed to get a domain name. " + e.getLocalizedMessage();

         PSLogger.logError(msg);
      }

      return "";
   }



   /**
    * Performs branding of the Rhythmyx installation found at rootDir.
    *
    * @param strRootDir may not be <code>null</code> or <code>empty</code>.
    */
   public static void brandProduct(String strRootDir)
   {
      PSStringUtils.notBlank(strRootDir);

      Code code = fetchBrandCode(strRootDir);
      if (code == null)
      {
         PSLogger.logInfo("InstallUtil#brandProduct : Brand code is null");
         return;
      }
      try
      {
         code.brand(strRootDir);
      }
      catch (CodeException ex)
      {
         PSLogger.logInfo("ERROR : " + ex.getMessage());
         PSLogger.logInfo(ex);
      }
   }

   /**
    * This one is used if table not exist and we are performing operation on it So that this will bypass the operation.
    *
    */
   public static boolean tableNotExists() {
      return false;
   }

   /**
    * This one is used if table not exist and we are performing operation on it So that this will bypass the operation.
    *
    * @param e the SQLException
    */
   public static boolean tableNotExists(SQLException e) {
      boolean notExists;
      if(e.getSQLState().equals("42Y55") || e.getSQLState().equals("42X65") || e.getSQLState().equals("42X86") || e.getSQLState().equals("X0Y32") || e.getSQLState().equals("X0X05")) {
         notExists = true;
      } else {
         notExists = false;
      }
      return notExists;
   }

   /**
    * Set internal path to driver to use
    *
    * @param rootDir
    */
   public static void setRootDir(String rootDir)
   {
      m_rootDir = rootDir;
      if(m_rootDir.equals(".")){
         String test = System.getProperty("rxdeploydir");
         if(test != null)
            m_rootDir = test;
      }
   }

   /**
    * Set if the calling code is performing a silent installation to enable additional
    * error logging.
    *
    * @param isSilent <code>true</code> if performing a silent install, <code>false</code> if not.
    */
   public static void setIsSilentInstall(boolean isSilent)
   {
      ms_isSilentInstall = isSilent;
   }

   /**
    * Determine if additional error logging is enabled for silent install
    *
    * @return <code>true</code> if performing a silent install, <code>false</code> if not.
    */
   public static boolean isSilentInstall()
   {
      return ms_isSilentInstall;
   }

   /**
    * Previous Version file name.
    */
   public static final String PREVIOUS_VERSION_PROPS_FILE = "PreviousVersion.properties";

   /**
    * Version file name.
    */
   public static final String VERSION_PROPS_FILE = "Version.properties";

   /**
    * Repository properties file path relative to Rhythmyx root.
    */
   public static final String REPOSITORY_PROPS_FILE = "rxconfig/Installer/rxrepository.properties";

   /**
    * Original repository properties file path relative to Rhythmyx root.
    */
   public static final String ORIG_REPOSITORY_PROPS_FILE = REPOSITORY_PROPS_FILE + ".orig";

   /**
    * Major version of current rhythmyx installation obtained from
    * PreviousVersion.properties.
    */
   public static int ms_majorVersion = 6;

   /**
    * Key for storing the Rhythmyx Service Name in installation.properties file
    */
   public static final String RHYTHMYX_SVC_NAME = "rhythmyxSvcName";

   /**
    * Key for storing the Rhythmyx Service Description in
    * installation.properties file
    */
   public static final String RHYTHMYX_SVC_DESC = "rhythmyxSvcDesc";

   /**
    * Property which stores the driver type in 5.X server.properties
    */
   public final static String DRIVER_PROPERTY = "driverType";

   /**
    * Property which stores the driver class in 5.X server.properties
    */
   public final static String CLASS_PROPERTY = "loggerClassname";

   /**
    * Property which stores the database server in 5.X server.properties
    */
   public final static String SERVER_PROPERTY = "serverName";

   /**
    * Property which stores the database login user id in 5.X server.properties
    */
   public final static String ID_PROPERTY = "loginId";

   /**
    * Property which stores the database login password in 5.X server.properties
    */
   public final static String PW_PROPERTY = "loginPw";

   /**
    * Property which stores the database name in 5.X server.properties
    */
   public final static String DATABASE_PROPERTY = "databaseName";

   /**
    * Property which stores the database schema in 5.X server.properties
    */
   public final static String SCHEMA_PROPERTY = "schemaName";

   private static String m_rootDir = ".";

   private static boolean ms_isSilentInstall = false;
   private static Driver m_extDriver = null;
   private static List m_jarUrls = null;


}
