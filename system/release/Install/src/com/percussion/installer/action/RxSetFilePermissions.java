/******************************************************************************
 *
 * [ RxSetFilePermissions.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.installshield.util.FileAttributes;
import com.installshield.wizard.service.file.FileService;
import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAUtils;

import java.io.File;

/**
 * Sets file permissions on files and folders.
 *
 * Use the following matrix for determining the value of <code>permission</code>
 * property. Add the value corresponding to the permissions that you want
 * to set.
 *
 *          ----------------------------------------
 *          |        |  read  |  write |  execute  |
 *          ----------------------------------------
 *          |owner   |  1     |  2     |  4        |
 *          ----------------------------------------
 *          |group   |  8     |  16    |  32       |
 *          ----------------------------------------
 *          |world   |  64    |  128   |  256      |
 *          ----------------------------------------
 *
 * For example,
 * For owner read + owner execute, permission=5
 * For owner write + group read + world read, permission=74
 */
public class RxSetFilePermissions extends RxIAAction
{
   @Override
   public void execute()
   {
      setExtensions(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), EXTENSIONS_VAR)));
      setFiles(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), FILES_VAR)));
      setPermission(Integer.valueOf(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), PERMISSION_VAR))));
      setRecurse(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), RECURSE_VAR)).equalsIgnoreCase("true"));
           
      // get a handle to the FileService
      FileService fService = getFileService();
      if (fService == null)
         return;

      FileAttributes fa = new FileAttributes();
      fa.setAttributes(permission);

      // set the permission on the files
      for (int i = 0; i < m_files.length; i++)
      {
         String filePath = "";
         try
         {
            filePath = resolveString(m_files[i]);
            if ((filePath == null) || (filePath.trim().length() < 1))
               continue;

            filePath = filePath.trim();
            File file = new File(filePath);

            if (file.exists())
               recurseSetFileAttributes(fService, new File[]{file}, fa);

         }
         catch (Exception e)
         {
            RxLogger.logError("Failed to set permission on file : " + filePath);
            RxLogger.logError("Exception : " + e.getMessage());
         }
      }
   }

   /**
    * Sets supplied set of permissions on a given set of files and/or
    * directories if Recurse property is true recurses into sub-directories as
    * well.
    * @param fService file service, never <code>null</code>.
    * @param files files to set attributes on
    * @param fa set of file attributes, never <code>null</code>.
    */
   private void recurseSetFileAttributes(FileService fService,
       File[] files, FileAttributes fa)
   {
      if (fService == null)
         throw new IllegalArgumentException("fService may not be null");
      if (fa == null)
         throw new IllegalArgumentException("fa may not be null");

      String path = "";
      for (int i = 0; files!=null && i < files.length; i++)
      {
         try
         {
            File f = files[i];
            path = f.getAbsolutePath();

            if (f.exists())
            {
               if (m_extensions.length > 0)
               {
                  String name = f.getName();
                  for (String ext : m_extensions)
                  {
                     if (name.endsWith(ext))           
                     {
                        fService.setFileAttributes(path, fa);
                        break;
                     }
                  }
               }
               else
                  fService.setFileAttributes(path, fa);
            }
            else
               continue;

            if (f.isDirectory() && isRecurse())
               recurseSetFileAttributes(fService, f.listFiles(), fa);
         }
         catch (Throwable e)
         {
            RxLogger.logError("Failed to set permission on: " + path);
            RxLogger.logError("Exception : " + e.getMessage());
         }
      }
   }

   /**************************************************************************
   * Bean property Accessors and Mutators
   **************************************************************************/

   /**
    *  Returns the files on which the permissions are to be set.
    *
    *  @return the absolute path of files on which the permissions are to be set,
    *  never <code>null</code>, may be an empty array.
    */
   public String[] getFiles()
   {
      return m_files;
   }

   /**
    *  Sets the files on which the permissions are to be set.
    *
    *  @param files the absolute path of the files on which the permissions are
    *  to be set, may be <code>null</code> or empty.
    */
   public void setFiles(String files)
   {
      m_files = RxIAUtils.toArray(files);
   }

   /**
    *  Returns the integer value of the desired permissions on the files.
    *
    *  @return the integer value of the desired permissions on the files.
    */
   public int getPermission()
   {
      return permission;
   }

   /**
    *  Sets the integer value of the desired permissions on the files.
    *
    *  @param permission the integer value of the desired permissions on the
    *  files.
    */
   public void setPermission(int permission)
   {
      this.permission = permission;
   }

   /**
    * @return <code>true</code> if the files of a directory should be
    * examined recursively, <code>false</code> otherwise.
    */
   public boolean isRecurse()
   {
      return m_isRecurse;
   }

   /**
    * @param b the recurse value.
    */
   public void setRecurse(boolean b)
   {
      m_isRecurse = b;
   }
   
   /**
    * File extensions for which the permisssions will be updated.
    * 
    * @param extensions comma-separated list of file extensions to be updated,
    * may be <code>null</code> or empty.
    */
   public void setExtensions(String extensions)
   {
      m_extensions = RxIAUtils.toArray(extensions);
   }

   /**
    * The variable name for the files parameter passed in via the IDE.
    */
   private static final String FILES_VAR = "files";
   
   /**
    * The variable name for the recurse parameter passed in via the IDE.
    */
   private static final String RECURSE_VAR = "recurse";
   
   /**
    * The variable name for the permission parameter passed in via the IDE.
    */
   private static final String PERMISSION_VAR = "permission";
   
   /**
    * The variable name for the extensions parameter passed in via the IDE.
    */
   private static final String EXTENSIONS_VAR = "extensions";
   
   /**************************************************************************
   * Bean properties
   **************************************************************************/

   /**
    * Files on which the permissions are to be set, never <code>null</code>,
    * may be an empty array.
    */
   private String[] m_files = new String[0];

   /**
    * Integer value of the desired permissions on the files. Default permission
    * is owner read+write+execute, group and world read only.
    */
   private int permission = 79;

   /**
    * If <code>true</code> indicates that recursion is desired.
    */
   private boolean m_isRecurse = false;
   
   /**
    * Files of these types (extensions) will be updated with new permissions,
    * never <code>null</code>, may be an empty array.
    */
   private String[] m_extensions = new String[0];

}






