/******************************************************************************
 *
 * [ RxIAUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.system.SystemUtilService;
import com.percussion.install.InstallUtil;
import com.percussion.installer.RxVariables;
import com.zerog.ia.api.pub.CustomError;
import com.zerog.ia.api.pub.ResourceAccess;
import com.zerog.ia.api.pub.ServiceAccess;
import com.zerog.ia.api.pub.VariableAccess;

import java.net.URL;


/**
 * This class contains static utility methods to be used by Rx IA custom code
 * implementation classes.
 * 
 * @author peterfrontiero
 */
public class RxIAUtils
{
   /**
    * Accesses the value of the specified InstallAnywhere variable.
    * 
    * @param va the {@link VariableAccess} object used to find the value,
    * may not be <code>null</code>.
    * @param var the InstallAnywhere variable name, may not be <code>null</code>
    * or empty.
    * 
    * @return the String value of the InstallAnywhere variable, never
    * <code>null</code>, empty if the variable could not be found.
    */
   public static String getValue(VariableAccess va, String var)
   {
      if (va == null)
      {
         throw new IllegalArgumentException("va may not be null.");
      }
      
      if (var == null || var.trim().length() == 0)
      {
         throw new IllegalArgumentException("var may not be null or " +
               "empty.");
      }
      
      return va.substitute(InstallUtil.getSubstituteName(var));
   }
   
   /**
    * Convenience method to get the value of a boolean InstallAnywhere variable.
    * 
    * @param va the {@link VariableAccess} object used to find the value,
    *  may not be <code>null</code>.
    * @param var the InstallAnywhere variable name, may not be <code>null</code>
    * or empty.
    * 
    * @return the boolean value of the InstallAnywhere variable, never
    * <code>null</code>.
    * @throws IllegalArgumentException if the variable could not be found. 
    */   
   public static boolean getBooleanValue(VariableAccess va, String var)
   {
      Boolean value = (Boolean) va.getVariable(InstallUtil.getSubstituteName(
            var));
      if (value == null)
      {
         throw new IllegalArgumentException("could not find variable " + var
               + ".");
      }
               
      return value.booleanValue();
   }
   
   /**
    * Sets the value of the specified InstallAnywhere variable.
    * 
    * @param va the {@link VariableAccess} object used to store the value,
    * may not be <code>null</code>.
    * @param var the InstallAnywhere variable, may not be <code>null</code> or
    * empty.
    * @param val the InstallAnywhere variable value.
    */
   public static void setValue(VariableAccess va, String var, String val)
   {
      if (va == null)
      {
         throw new IllegalArgumentException("va may not be null.");
      }
      
      if (var == null || var.trim().length() == 0)
      {
         throw new IllegalArgumentException("var may not be null or " +
               "empty.");
      }
      
      va.setVariable(var, val);
   }
   
   /**
    * Determines the root install directory.
    * 
    * @param va the {@link VariableAccess} object used to query
    * InstallAnywhere variables, may not be <code>null</code>.
    * 
    * @return the Rhythmyx root install directory or empty if it has not been
    * set.
    */
   public static String getRootDir(VariableAccess va)
   {
      return getValue(va, RxVariables.INSTALL_DIR);
   }
   
   /**
    * Sets the root install directory.
    * 
    * @param va the {@link VariableAccess} object used to query
    * InstallAnywhere variables, may not be <code>null</code>.
    * @param dir the Rhythmyx root install directory, may not be 
    * <code>null</code> or empty.
    */
   public static void setRootDir(VariableAccess va, String dir)
   {
      setValue(va, RxVariables.INSTALL_DIR, dir);
   }
   
   /**
    * Resolves all InstallAnywhere variables in the given string.
    * 
    * @param va the {@link VariableAccess} object used to query
    * InstallAnywhere variables, may not be <code>null</code>.
    * @param str the string to resolve, may not be <code>null</code>.
    * 
    * @return the resolved string.  All InstallAnywhere variables are replaced
    * by their corresponding values.
    */
   public static String resolve(VariableAccess va, String str)
   {
      if (va == null)
      {
         throw new IllegalArgumentException("va may not be null.");
      }
      
      if (str == null)
      {
         throw new IllegalArgumentException("str may not be null");
      }
      
      return va.substitute(str);
   }
   
   /**
    * Returns a URL to a resource within the archive. During runtime (e.g. when
    * the installer or the uninstaller is executing), calling this method will
    * return a URL to a resource in the archive. That URL can be used like any
    * other URL in Java.
    * 
    * @param ra the <code>ResourceAccess</code> object used to retrieve the 
    * resource, may not be <code>null</code>.
    * 
    * @param archivePath a forward-slash ('/') delimited path relative to the
    * root of the archive. For example "com/acme/picture.gif". If the resource
    * is put into the installer then the path is the absolute path. For example,
    * "C:\foo\bar\picture.gif". If the path contains a source path variable then
    * that should also be put into the archive path
    * (ie. "$IA_PROJECT_DIR$\foo\bar\test.txt"). 
    * 
    * @return an instance of {@link URL} that refers to a resource located in
    * the installer archive.
    */
   public static URL getResource(ResourceAccess ra, String archivePath)
   {
      if (ra == null)
         throw new IllegalArgumentException("ra may not be null");
      
      return ra.getResource(archivePath);
   }
   
   /**
    * The {@link FileService} is used to performed various file system
    * manipulation operations.
    * 
    * @param sa the {@link ServiceAccess} object used to retrieve the file
    * service, may not be <code>null</code>.
    * 
    * @return the {@link FileService} for the current install.
    */
   public static FileService getFileService(ServiceAccess sa)
   {
      if (sa == null)
         throw new IllegalArgumentException("sa may not be null");
      
      return (FileService) sa.getService(FileService.class);
   }
   
   /**
    * The {@link SystemUtilService} is used to performed various system
    * environment manipulation operations.
    * 
    * @param sa the {@link SystemUtilService} object used to retrieve the 
    * system util service, may not be <code>null</code>.
    * 
    * @return the {@link SystemUtilService} for the current install.
    */
   public static SystemUtilService getSystemUtilService(ServiceAccess sa)
   {
      if (sa == null)
         throw new IllegalArgumentException("sa may not be null");
      
      return (SystemUtilService) sa.getService(SystemUtilService.class);
   }
   
   /**
    * Converts the given str to an array.
    * 
    * @param str assumed to be a comma-separated list of values.
    * 
    * @return an array of string values corresponding to the list, never
    * <code>null</code>, may be empty.
    */
   public static String[] toArray(String str)
   {
      if (str == null || str.trim().length() == 0)
         return new String[0];
      
      String[] strArr = new String[]{str.trim()};
      
      if (str.indexOf(',') != -1)
      {
         strArr = str.split(",");
         for (int i = 0; i < strArr.length; i++)
            strArr[i] = strArr[i].trim();
      }
     
      return strArr;
   }
   
   /**
    * Fixes up a given file's installer archive path by placing '$' characters
    * around the source path.
    * 
    * @param path the installer archive path of the file, assumed to begin with
    * a source path variable, may not be <code>null</code>.
    * 
    * @return the modified path.
    */
   public static String fixupSourcePath(String path)
   {
      if (path == null)
         throw new IllegalArgumentException("path may not be null");
      
      int idx = path.indexOf('\\');
      if (idx == -1)
         idx = path.indexOf('/');
      
      if (idx == -1)
         return path;
      
      String srcPath = path.substring(0, idx);
      String remainder = path.substring(idx);
         
      return '$' + srcPath + '$' + remainder;
   }
   
   /**
    * Truncates a text message which is presumed to describe a file operation,
    * i.e., copy or expansion.  The expected message format is:
    * [action] [path] [name] to [file path].  The file path portion will be
    * shortened to produce a message of 50 characters.  A ".../" will be added
    * between shortened path and file name.
    * 
    * @param msg the logging message to be truncated.
    * 
    * @return a message which has been truncated to 50 characters if possible.
    */
   public static String truncateMsg(String msg)
   {
      if (msg == null || msg.trim().length() == 0)
         return "";
      
      int toIndex = msg.indexOf(" to ");
      if (toIndex == -1 || msg.length() <= 50)
         return msg;
      
      String truncatedMsg = msg.substring(0, toIndex);
      int length = truncatedMsg.length();
      if (length <= 50)
         return truncatedMsg;
         
      String actionStr = "";
      int spaceIndex = truncatedMsg.indexOf(' ');
      if (spaceIndex != -1)
         actionStr = truncatedMsg.substring(0, spaceIndex);
                
      int sepIndex = truncatedMsg.lastIndexOf('\\');
      if (sepIndex == -1)
         sepIndex = truncatedMsg.lastIndexOf('/');
      
      String name = truncatedMsg.substring(sepIndex + 1);
      String path = truncatedMsg.substring(spaceIndex + 1, sepIndex + 1);     
      
      int i = path.length() - 1;
      while (truncatedMsg.length() > 50)
      {
         if (i <= 0)
         {
            truncatedMsg = actionStr + ' ' + name;
            break;
         }
         
         path = path.substring(0, i);        
         truncatedMsg = actionStr + ' ' + path + ".../" + name;
         i--;
      }
           
      return truncatedMsg;
   }
   
   /**
    * Type constant for logging error events.
    */
   public static final int ERROR = CustomError.ERROR;
   
   /**
    * Type constant for logging error events.
    */
   public static final int WARNING = CustomError.WARNING;
}
