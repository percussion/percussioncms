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

package com.percussion.server;

import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Singleton class that will manage custom control resources.  This class will
 * handle adding import declarations to the custom controls import file for each
 * control file present in the {@link #CUSTOM_CONTROLS_DIR}.
 */
public class PSCustomControlManager extends PSBaseControlManager
{
   /**
    * Private ctor to enforce singleton pattern.
    */
   private PSCustomControlManager()
   {
   
   }
   
   /**
    * Gets the singleton instance of this class.  
    * 
    * @return The manager, never <code>null</code>.
    */
   public static PSCustomControlManager getInstance()
   {
      if (ms_custCtrlMgr == null)
      {
         ms_custCtrlMgr = new PSCustomControlManager();
      }
      
      return ms_custCtrlMgr;
   }
   
   /**
    * Initializes the control manager.
    * 
    * @param rxRoot The root directory.  May not be <code>null</code> and must
    * exist.
    * 
    * @throws IllegalStateException if the manager has been initialized.
    */
   public void init(File rxRoot)
   {
      if (rxRoot == null || !rxRoot.exists())
      {
         throw new IllegalArgumentException("rxRoot may not be null and must "
               + "exist");
      }
      
      if (m_rxRoot != null)
      {
         throw new IllegalStateException("Custom control manager has already "
               + "been initialized");
      }
      
      m_rxRoot = rxRoot;       
               
      writeImports();
   }
     
   /**
    * Ensures that an import declaration exists in the custom controls file for
    * each control file present in the controls directory.  Also updates the
    * last modified date of system files which need to be re-loaded for changes
    * to take effect.
    */
   public void writeImports()
   {
      if (m_rxRoot == null)
      {
         throw new IllegalStateException("Custom control manager has not been "
               + "initialized");
      }
      
      FileWriter fw = null;
      
      try
      {
         // load current control imports
         File rxTempFile = new File(m_rxRoot, CUSTOM_CONTROL_IMPORTS_FILE);
         String rxTempStr = IOTools.getFileContent(rxTempFile);
         int index = rxTempStr.indexOf(XSL_IMPORT_OPEN_TAG);
         if (index == -1)
         {
            index = rxTempStr.indexOf(XSL_STYLESHEET_CLOSE_TAG);
         }
         
         rxTempStr = rxTempStr.substring(0, index);
         
         // add imports for custom controls 
         String importStr = "";
         List<File> ctrlFiles = getControlFiles();
         for (File ctrlFile : ctrlFiles)
         {
            String relPath = CUSTOM_CONTROLS_DIR + '/' + ctrlFile.getName();
            
            if (importStr.trim().length() > 0)
            {
               importStr += '\n';               
            }
            
            importStr += createImport(relPath);
         }
         
         rxTempStr += importStr + '\n' + XSL_STYLESHEET_CLOSE_TAG;
         
         fw = new FileWriter(rxTempFile);
         fw.write(rxTempStr);
         fw.flush();
         
         // touch necessary files so changes will be picked up
         for (String path : ms_touchFiles)
         {
            Calendar cal = Calendar.getInstance();
            File touchFile = new File(m_rxRoot, path);
            touchFile.setLastModified(cal.getTimeInMillis());
         }
      }
      catch(IOException e)
      {
         PSConsole.printMsg(SUBSYSTEM, e); 
      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (IOException e)
            {
               
            }
         }
      }
   }
   
   /**
    * Gets the set of import declarations from the current custom controls
    * import file.
    * 
    * @return The set of import declarations, as Strings, from the current
    * custom controls import file.  Never <code>null</code>, may be empty.
    */
   public Set<String> getImports()
   {
      Set<String> imports = new HashSet<>();
         
      try
      {
         File rxTempFile = new File(m_rxRoot, CUSTOM_CONTROL_IMPORTS_FILE);
         String rxTempStr = IOTools.getFileContent(rxTempFile);
         Reader r = new StringReader(rxTempStr);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
         NodeList nodes = doc.getElementsByTagName(XSL_IMPORT_TAG_NAME);
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Element elem = (Element) nodes.item(i);
            String href = elem.getAttribute("href");
            if (href.trim().length() > 0 && href.startsWith(HREF_FILE_PREFIX))
            {
               href = href.substring(HREF_FILE_PREFIX.length());
               if (href.startsWith(CUSTOM_CONTROLS_DIR))
               {
                  imports.add(createImport(href));                              
               }
            }
         }
      }
      catch (Exception e)
      {
         PSConsole.printMsg(SUBSYSTEM, e);
      }
      
      return imports;     
   }
   
   /**
    * Creates an import declaration for the specified path.
    * 
    * @param path The file path relative to the Rhythmyx root.  May not be
    * <code>null</code> or empty.
    * 
    * @return An import declaration for the file path.
    */
   public String createImport(String path)
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path may not be blank");
      
      return XSL_IMPORT_OPEN_TAG + " href=\"" + HREF_FILE_PREFIX + path
         + "\"/>";
   }
   
   /**
    * Gets the file which contains the specified custom control.
    * 
    * @param name The control name, may not be blank.
    * 
    * @return The control file or <code>null</code> if the specified control
    * could not be found.
    */
   public File getControlFile(String name)
   {
      File file = null;
      
      for (File ctrlFile : getControlFiles())
      {
         if (getControl(ctrlFile, name) != null)
         {
            file = ctrlFile;
            break;
         }
      }
      
      return file;
   }
   
   /**
    * Gets the set of current custom control files.
    * 
    * @return The set of current custom control files as a list.  Never
    * <code>null</code>, may be empty.
    */
   public List<File> getControlFiles()
   {
      List<File> files = new ArrayList<>();
      
      File ctrlsDir = new File(m_rxRoot, CUSTOM_CONTROLS_DIR);
      if (ctrlsDir.exists() && ctrlsDir.isDirectory())
      {
         File[] ctrlFiles = ctrlsDir.listFiles();
         for (File ctrlFile : ctrlFiles)
         {
            if (isControlFile(ctrlFile))
            {
               files.add(ctrlFile);
            }
         }
      }
      
      return files;
   }
   
   /**
    * Determines if the specified file represents a control file.  A file is
    * considered to be a control file if it is an .xsl file, it contains one
    * and only one control definition, and the name of the file matches that of
    * its control.  Warnings are logged to the console for invalid control
    * files.
    * 
    * @param file The file to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the file is a control file,
    * <code>false</code> otherwise.
    */
   private boolean isControlFile(File file)
   {
      boolean isControlFile = false;
               
      String ext = ".xsl";
      String fileName = file.getName();
      String filePath = file.getAbsolutePath();
      if (!file.getName().endsWith(ext))
      {
         Object[] args =
         {
            filePath
         };
       
         PSConsole.printMsg(SUBSYSTEM,
               IPSServerErrors.INVALID_CTRL_FILE_EXT, args);
      }
      else
      {
         List<PSControlMeta> ctrls = getControls(file);
         if (ctrls.isEmpty())
         {
            Object[] args =
            {
               filePath
            };
            
            PSConsole.printMsg(SUBSYSTEM,
                  IPSServerErrors.INVALID_CTRL_FILE_MISSING_CTRL, args);
         }
         else if (ctrls.size() > 1)
         {
            Object[] args =
            {
               filePath
            };
            
            PSConsole.printMsg(SUBSYSTEM,
                  IPSServerErrors.INVALID_CTRL_FILE_MULT_CTRLS, args);
         }
         else
         {         
            PSControlMeta ctrl = ctrls.get(0);
            String ctrlName = ctrl.getName();

            fileName = fileName.substring(0, fileName.length() - ext.length());
            if (fileName.equals(ctrlName))
            {
               isControlFile = true;
            }
            else
            {
               Object[] args =
               {
                  filePath,
                  ctrlName
               };
               
               PSConsole.printMsg(SUBSYSTEM,
                     IPSServerErrors.INVALID_CTRL_FILE_NAME, args);
            }
         }
      }
         
      return isControlFile;
   }
   
   /**
    * The singleton instance of the control manager, <code>null</code> until
    * a call to {@link #getInstance()}, never <code>null</code> after that.
    */
   private static PSCustomControlManager ms_custCtrlMgr = null;
   
   /**
    * The set of relative system file paths which need to be touched after the
    * import file is updated.
    */
   private static Set<String> ms_touchFiles = new HashSet<>();
   
   /**
    * The Rhythmyx root directory.  May be <code>null</code> until
    * {@link #init(File)} is called.
    */
   private File m_rxRoot = null;
   
   /**
    * The subsystem name.
    */
   private static final String SUBSYSTEM = "CustomControlMgr";
   
   /**
    * The rx_resources stylesheets directory relative to the Rhythmyx root.
    */   
   public static final String RX_STYLESHEETS_DIR =
      "rx_resources/stylesheets";
   
   /**
    * The sys_resources stylesheets directory relative to the Rhythmyx root.
    */   
   public static final String SYS_STYLESHEETS_DIR =
      "sys_resources/stylesheets";
   
   /**
    * The custom controls directory relative to the Rhythmyx root.
    */
   public static final String CUSTOM_CONTROLS_DIR =
      RX_STYLESHEETS_DIR + "/controls";
   
   /**
    * The custom control imports file path relative to the Rhythmyx root.
    */
   public static final String CUSTOM_CONTROL_IMPORTS_FILE =
      SYS_STYLESHEETS_DIR + "/customControlImports.xsl";
   
   /**
    * The href file prefix in an import declaration.
    */
   private static final String HREF_FILE_PREFIX = "file:";
   
   /**
    * The import tag name.
    */
   private static final String XSL_IMPORT_TAG_NAME = "xsl:import";
   
   /**
    * The import open tag (not closed).
    */
   private static final String XSL_IMPORT_OPEN_TAG = '<' + XSL_IMPORT_TAG_NAME;
   
   /**
    * The stylesheet close tag.
    */
   private static final String XSL_STYLESHEET_CLOSE_TAG = "</xsl:stylesheet>";
   
   static
   {
      ms_touchFiles.add(SYS_STYLESHEETS_DIR + "/activeEdit.xsl");
      ms_touchFiles.add(SYS_STYLESHEETS_DIR + "/singleFieldEdit.xsl");
   }

   /* (non-Javadoc)
    * @see com.percussion.server.PSBaseControlManager#getSubSystem()
    */
   @Override
   protected String getSubSystem()
   {
      // TODO Auto-generated method stub
      return SUBSYSTEM;
   }
}
