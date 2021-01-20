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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.w3c.dom.Element;

/**
 * Upgrade plugin that will add Ektron ewebeditpro related XSL
 * code into the following stylesheets, if Ektron exists on the installation
 * being upgraded:
 *
 * <ul>
 * <li>activeEdit.xsl</li>
 * <li>singleFieldEdit.xsl</li>
 * <li>sys_I18nUtils.xsl</li>
 * <li>sys_Templates.xsl</li>
 * </ul>
 */
public class PSUpgradePluginAddEktronToXSL implements IPSUpgradePlugin
{

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.install.IPSUpgardePlugin#process(com.percussion.install.IPSUpgradeModule,
    *      org.w3c.dom.Element)
    */
   public PSPluginResponse process(final IPSUpgradeModule config, final Element elemData)
   {
      initLog(config);
      log("Verifying if Sys_Templates should be modified.");
      if(!isWEPInstalled())
      {
         log("Ektron is not installed, this plugin will not be executed.");
         return null;
      }

      try
      {
         log("Adding the sys_eWebEditPro template to sys_Templates.xsl");
         addXSLTemplateToStylesheet(
            SYS_TEMPLATES_FILE, EKTRON_TEMPLATE_FILE);
         log("Adding ewebeditpro related templates to sys_I18nUtils.xsl");
         addXSLTemplateToStylesheet(
            SYS_I18N_UTILS_FILE, EKTRON_I18N_UTILS_TEMPLATE_FILE);
         log("Replacing placeholder token in activeEdit.xsl with Ektron" +
            "i18n function code.");
         replaceToken(
            ACTIVE_EDIT_FILE,
            GET_EKTRON_LANG_SCRIPT_CALL,
            REPLACEMENT_TOKEN);
         log("Replacing placeholder token in singleFieldEdit.xsl with " +
            "Ektron i18n function code.");
         replaceToken(
            SINGLE_FIELD_EDIT_FILE,
            GET_EKTRON_LANG_SCRIPT_CALL,
            REPLACEMENT_TOKEN);

      }
      catch(Exception e)
      {
         e.printStackTrace(m_logStream);
      }
      log("Returning from process.");
      return null;
   }

   /**
    * Adds an XSL template to a specified styleheet
    * file
    * @param XSLFile the path to the XSL file to be modified,
    * assumed not <code>null</code>.
    * @param templateFile the path to the template resource file,
    * assumed not <code>null</code>.
    * @throws IOException on any error
    */
   private void addXSLTemplateToStylesheet(
      final String XSLFile, final String templateFile)
      throws IOException
   {
      final StringBuffer sb = new StringBuffer(getFileContents(XSLFile));
      final String template = getWEPTemplate(templateFile);
      final String marker = "</xsl:template>";
      int pos = sb.lastIndexOf(marker) + marker.length();
      sb.insert(pos, template);
      updateFileContents(XSLFile, sb.toString());
   }

   /**
    * Replace a specified token in a file with replacement content
    * @param XSLFile the path to the XSL file to be modified,
    * assumed not <code>null</code>.
    * @param replacement the replacement text, assumed not
    * <code>null</code>
    * @param token the replacement token to search for
    * @throws IOException on any error
    */
   private static void replaceToken(
      final String XSLFile, String replacement, String token)
      throws IOException
   {
      int pos = 0;
      int idx = 0;
      final StringBuffer sb = new StringBuffer(getFileContents(XSLFile));
      while((pos = sb.indexOf(token, idx)) != -1)
      {
         sb.replace(pos, pos + token.length(), replacement);
         idx = pos + replacement.length();
      }
      updateFileContents(XSLFile, sb.toString());
   }

   /**
    * Helper method to get file contents as a String
    * @param filepath the path to the file to be loaded, assumed
    * not <code>null</code>.
    * @return the contents of the specified file as a String
    * @throws IOException on any error
    */
   private static String getFileContents(final String filepath)
      throws IOException
   {
      final File file = new File(filepath);
      ByteArrayOutputStream bos = null;
      FileInputStream fis = null;
      String contents = null;

      if(!file.exists())
         throw new IOException("Specified file does not exist.");
      try
      {
         fis = new FileInputStream(file);
         bos = new ByteArrayOutputStream();
         copyStream(fis, bos);
         contents = bos.toString();
      }
      finally
      {
         try
         {
            if(bos != null)
               bos.close();
            if(fis != null)
               fis.close();
         }
         catch(IOException ignore){}
      }
      return contents;
   }

   /**
    * Helper method to update file contents (overwrites existing content)
    * @param filepath the path to the file to be written,
    * assumed not <code>null</code>
    * @param contents the contents to be written to the specified file,
    * assumed not <code>null</code>
    * @throws IOException on any error
    */
   private static void updateFileContents(
      final String filepath, final String contents)
      throws IOException
   {
      final File file = new File(filepath);
      PrintWriter pw = null;
      if(!file.exists())
         throw new IOException("Specified file does not exist.");
      try
      {
         pw = new PrintWriter(new FileOutputStream(file));
         pw.write(contents);
         pw.flush();
      }
      finally
      {
         if(pw != null)
            pw.close();
      }
   }

   /**
    * Method to copy Java InputStream to outputStream.
    *
    * @param in
    *           Input stream tp copy from, never <code>null</code>.
    *
    * @param out
    *           Output stream to copy to, never <code>null</code>.
    *
    * @return number bytes copied
    *
    * @throws IOException
    *            in case of any error while copying.
    *
    */
   private static long copyStream(InputStream in, OutputStream out)
       throws IOException
   {
       int nCopied = 0;
       final byte[] buffer = new byte[ DEFAULT_BUFFER_SIZE ];
       int n = 0;
       while( -1 != (n = in.read( buffer )) )
       {
           out.write( buffer, 0, n );
          nCopied += n;
       }
      return nCopied;
   }

   /**
    * Verifies that Ektron ewebEditPro is installed for this installation
    *
    * @return <code>true</code> if WEP is installed
    */
   private static boolean isWEPInstalled()
   {
      final File file = new File(
         RxUpgrade.getRxRoot() + "rx_resources" +
         File.separator + "ewebeditpro" +
         File.separator + "ewebeditpro.js");
      return (file.exists() && file.isFile());
   }

   /**
    * Returns specified template file contents as a string
    * @param template the file name of the template
    * @return ewebeditpro xsl template string
    */
   private String getWEPTemplate(final String template)
   {
      InputStream stream = null;
      ByteArrayOutputStream bos = null;
      try
      {
         bos = new ByteArrayOutputStream();
         stream = getClass().getResourceAsStream(
            template);
         if (stream == null)
         {
            throw new IOException(
               "Failed to open an input stream from file " + template);
         }
         copyStream(stream, bos);

      }
      catch(IOException e)
      {
         e.printStackTrace(m_logStream);
      }
      finally
      {

            try
            {
               if(stream != null)
                  stream.close();
               if(bos != null)
                  bos.close();

            }
            catch (Exception ignore){}

      }
      return bos.toString();
   }

   /**
    * Method to set the log stream, should be called at beginning of
    * {@link #process(IPSUpgradeModule, Element)}method
    *
    * @param config
    *           the IPSUpgradeModule, assumned not <code>null</code>
    */
   private void initLog(IPSUpgradeModule config)
   {
      m_logStream = config.getLogStream();
   }

   /**
    * Convienience method to write a message to the log.
    * {@link #initLog(IPSUpgradeModule)}should be called once before this
    * method is used.
    *
    * @param msg
    *           the message to be logged
    */
   private void log(String msg)
   {
      if(m_logStream != null && msg != null)
         m_logStream.println(msg);
   }

   /**
    * The log stream for writing log messages to, initialized in
    * {@link #initLog(IPSUpgradeModule)}, never <code>null</code> after that.
    */
   private PrintStream m_logStream;

   // Various contants for file paths
   private static final String EKTRON_TEMPLATE_FILE =
      "ewebeditpro_Template.xml";
   private static final String EKTRON_I18N_UTILS_TEMPLATE_FILE =
      "ewebeditpro_i18nUtil_Template.xml";
   private static final String STYLESHEETS_DIR =
      RxUpgrade.getRxRoot() +
      "sys_resources" + File.separator + "stylesheets" +
      File.separator;
   private static final String SYS_TEMPLATES_FILE =
       STYLESHEETS_DIR + "sys_Templates.xsl";
   private static final String ACTIVE_EDIT_FILE =
      STYLESHEETS_DIR + "activeEdit.xsl";
   private static final String SINGLE_FIELD_EDIT_FILE =
      STYLESHEETS_DIR + "singleFieldEdit.xsl";
   private static final String SYS_I18N_UTILS_FILE =
      STYLESHEETS_DIR + "sys_I18nUtils.xsl";

   /**
    * Replacement token for ektron i18n code in activeEdit.xsl and
    * singleFieldEdit.xsl
    */
   private static final String REPLACEMENT_TOKEN =
      "<!-- @@REP WITH WEP I18N FUNC@@ -->";

   /**
    * Default buffer size for stream copying
    */
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

   /**
    * XSL code that adds the Ektron language message file to
    * both activeEdit.xsl and singleFieldEdit.xsl
    */
   private static final String GET_EKTRON_LANG_SCRIPT_CALL =
      "<script language=\"javascript\">\n" +
      "var eWebEditProMsgsFilename = \"<xsl:call-template name=\"" +
      "getEktronLangMsgFile\">" +
      "<xsl:with-param name=\"lang\" select=\"$lang\"/>" +
      "</xsl:call-template>\";\n" +
      "</script>";



}
