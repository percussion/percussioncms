/******************************************************************************
 *
 * [ RxUpdateLaxFiles.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;


/**
 * This action will preserve java arguments specified in .lax files for CM System launchers on upgrade.  Also, the
 * server launcher's .lax file will be modified to point to the 64-bit JRE folder on new installs and upgrades of 64-bit
 * systems.  The server launcher's memory settings will be increased on new installs to 64-bit systems.
 */
public class RxUpdateLaxFiles extends RxIAAction
{
    /**
     * See this method of <code>RxIAAction</code> for detailed
     * information.
     */
    @Override
    public void execute()
    {
        File rootDir = new File(getInstallValue(RxVariables.INSTALL_DIR));
        
        File linuxServerLax = new File(rootDir, RX_SERVER_BIN_LAX);
        File jre64Dir = new File(rootDir, JRE_64_DIR);
        
        /*  Currently 64 Bit Linux is not implemented... 
            so don't do 64bit JVM Switch on Linux */
        if (jre64Dir.exists() && !linuxServerLax.exists())
        { 
            updateServerLaxFor64Bit();
            updateCmLaxFor64Bit();
        }
        else{
           updateServerLaxFor32Bit();
        }
    }
 

    /**
     * Finds the value of the specified property in the specified .lax file.
     * 
     * @param f .lax file.
     * @param property
     * 
     * @return the value of the property if found and non-empty, otherwise, <code>null</code> will
     * be returned.
     */
    private String getProperty(File f, String property)
    {
        FileInputStream in = null;
        BufferedReader bReader = null;
        try
        {
            String line;
            in = new FileInputStream(f);
            bReader = new BufferedReader(new InputStreamReader(in));
            while ((line = bReader.readLine()) != null)
            {
                if (StringUtils.isEmpty(line) || line.startsWith("#"))
                {
                    continue;
                }

                String[] lineArr = line.split("=");
                String key = lineArr[0];
                if (key.equals(property))
                {
                    int index = line.indexOf('=');
                    if (index > -1 && index < (line.length() - 1))
                    {
                        return line.substring(index + 1);
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            RxLogger.logError("RxUpdateLaxFiles#getProperty : " + ioe.getMessage());
        }
        finally
        {
            if (bReader != null)
            {
                try
                {
                    bReader.close();
                }
                catch (IOException e)
                {
                }
            }

            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        return null;
    }
    
    /**
     * Updates the Rhythmyx Server .lax file to point to the 64-bit java executable.  Also updates
     * the memory settings for a new install.
     */
    private void updateServerLaxFor64Bit()
    {
        String rootDir = getInstallValue(RxVariables.INSTALL_DIR);
        
        File serverLax = new File(rootDir, RX_SERVER_LAX);
        if (!serverLax.exists())
        {
            serverLax = new File(rootDir, RX_SERVER_BIN_LAX);
        }

        if (serverLax.exists())
        {
            String classPath = getProperty(serverLax, LAUNCHER_CLASS_PATH);
            if (classPath != null && !classPath.contains("JRE64"))
            {
                classPath = classPath.replaceAll(JRE_32_DIR, JRE_64_DIR);
                setProperty(serverLax, LAUNCHER_CLASS_PATH, classPath);
            }
            
            String javaExe = getProperty(serverLax, JAVA_EXE_PROP);
            if (javaExe != null  && !javaExe.contains("JRE64"))
            {
                javaExe = javaExe.replaceFirst(JRE_32_DIR, JRE_64_DIR);
                setProperty(serverLax, JAVA_EXE_PROP, javaExe);
            }
            
            String javaArgs = getProperty(serverLax, JAVA_ARGS_PROP);
            if (javaArgs != null)
            {
                javaArgs = javaArgs.replaceFirst(SERVER_JVM_MEM_32, SERVER_JVM_MEM_64);
                javaArgs = javaArgs.replaceFirst(SERVER_JVM_MEM_32_newInstalls, SERVER_JVM_MEM_64);
                javaArgs = javaArgs.replaceFirst(SERVER_JVM_PERMGEN_32, SERVER_JVM_PERMGEN_64);
                setProperty(serverLax, JAVA_ARGS_PROP, javaArgs);
            }    
            
        }
    }
    
    private void updateServerLaxFor32Bit()
    {
       String rootDir = getInstallValue(RxVariables.INSTALL_DIR);
       
       boolean isWindows = true;
       File serverLax = new File(rootDir, RX_SERVER_LAX);
       if (!serverLax.exists())
       {
           serverLax = new File(rootDir, RX_SERVER_BIN_LAX);
           isWindows = false;
       }

       if (serverLax.exists())
       {     
           String javaArgs = getProperty(serverLax, JAVA_ARGS_PROP);
           if (javaArgs != null)
           {
              javaArgs = javaArgs.replaceFirst(SERVER_JVM_PERMGEN_32, SERVER_JVM_PERMGEN_32_UPDATE);
              if (!isWindows)
              {
                 // 32-bit linux can use higher memory settings than windows 
                 javaArgs = javaArgs.replaceFirst(SERVER_JVM_MEM_32_newInstalls, SERVER_JVM_MEM_LINUX_32);
              }
              
              setProperty(serverLax, JAVA_ARGS_PROP, javaArgs);
           }       
       }
    }

    
    /**
     * Updates the PercussuionCM.lax file to point to the 64-bit java executable.  Also updates
     * the memory settings for a new install.
     */
    private void updateCmLaxFor64Bit()
    {
        String rootDir = getInstallValue(RxVariables.INSTALL_DIR);
        
        File CMLax = new File(rootDir, CM_SERVER_LAX);
        
        if (CMLax.exists())
        {
            String classPath = getProperty(CMLax, LAUNCHER_CLASS_PATH);
            if (classPath != null && !classPath.contains("JRE64"))
            {
                classPath = classPath.replaceAll(JRE_32_DIR, JRE_64_DIR);
                setProperty(CMLax, LAUNCHER_CLASS_PATH, classPath);
            }
            
            String javaExe = getProperty(CMLax, JAVA_EXE_PROP);
            if (javaExe != null  && !javaExe.contains("JRE64"))
            {
                javaExe = javaExe.replaceFirst(JRE_32_DIR, JRE_64_DIR);
                setProperty(CMLax, JAVA_EXE_PROP, javaExe);
            }            
        }
    }
    
    /**
     * Updates/adds the specified property of the
     * specified .lax file with the given value.
     * 
     * @param f .lax file.
     * @param property
     * @param value
     */
    private void setProperty(File f, String property, String value)
    {
        FileInputStream in = null;
        BufferedReader bReader = null;
        FileWriter fw = null;
        try
        {
            boolean propertyExists = false;
            String line = "";
            String output = "";
            in = new FileInputStream(f);
            bReader = new BufferedReader(new InputStreamReader(in));
            while ((line = bReader.readLine()) != null)
            {
                if (StringUtils.isEmpty(line))
                {
                    output += "\n";
                    continue;
                }

                if (StringUtils.isNotEmpty(output))
                    output += "\n";

                if (line.startsWith("#") || line.indexOf('=') == -1)
                    output += line;
                else
                {
                    String[] lineArr = line.split("=");
                    String key = lineArr[0];
                    if (key.equals(property))
                    {
                        output += key + '=' + value;
                        propertyExists = true;
                    }
                    else
                    {
                        output += line; 
                    }
                }
            }

            if (!propertyExists)
            {
                if (StringUtils.isNotEmpty(output))
                    output += "\n";

                output += property + '=' + value;
            }

            bReader.close();
            in.close();

            //Write the .lax file
            fw = new FileWriter(f);
            fw.write(output);
        }
        catch (IOException ioe)
        {
            RxLogger.logError("RxUpdateLaxFiles#setProperty : " + ioe.getMessage());
        }
        finally
        {
            if (bReader != null)
            {
                try
                {
                    bReader.close();
                }
                catch (IOException e)
                {
                }
            }

            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                }
            }

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
     * The name of the .lax property which contains the java arguments.
     */
    private static String JAVA_ARGS_PROP = "lax.nl.java.option.additional";

    /**
     * The name of the .lax property which contains the pointer to the java executable used by the launcher.
     */
    private static String JAVA_EXE_PROP = "lax.nl.current.vm";
    
    /**
     * The name of the .lax property which contains the classpath of the launcher.
     */
    private static String LAUNCHER_CLASS_PATH = "lax.class.path"; 
    
    /**
     * The name of the .lax file of the Percussion Server launcher (windows).
     */
    private static String RX_SERVER_LAX = "PercussionServer.lax";
    
    /**
     * The name of the .lax file of the Percussion Server launcher (linux, solaris).
     */
    private static String RX_SERVER_BIN_LAX = "PercussionServer.bin.lax";
    
    /**
     * The name of the .lax file of the PercussionCM launcher (windows).
     */
    private static String CM_SERVER_LAX = "PercussionCM.lax";
    
    /**
     * The name of the 32-bit JRE folder.
     */
    private static String JRE_32_DIR = "JRE";
    
    /**
     * The name of the 64-bit JRE folder.
     */
    private static String JRE_64_DIR = "JRE64";    
    
    /**
     * The memory settings for the 32-bit server launcher.
     */
    private static String SERVER_JVM_MEM_32 = "-Xms128m -Xmx512m";
    
    /**
     * The memory settings for the 32-bit server launcher for newer installs.
     */
    private static String SERVER_JVM_MEM_32_newInstalls = "-Xms128m -Xmx1024m";
    
    /**
     * The memory settings for the 64-bit server launcher.
     */
    private static String SERVER_JVM_MEM_64 = "-Xms256m -Xmx2048m";
    
    /**
     * The memory settings for the 32-bit linux server launcher.
     */
    private static String SERVER_JVM_MEM_LINUX_32 = "-Xms256m -Xmx2048m";
    
    /**
     * The permgen settings for the 32-bit server launcher.
     */
    private static String SERVER_JVM_PERMGEN_32 = "XX:MaxPermSize=128m";
    
    /**
     * The updated permgen settings for the 32-bit server launcher.
     */
    private static String SERVER_JVM_PERMGEN_32_UPDATE = "XX:MaxPermSize=256m";
    
    /**
     * The permgen settings for the 32-bit server launcher.
     */
    private static String SERVER_JVM_PERMGEN_64 = "XX:MaxPermSize=256m";
    
    }


