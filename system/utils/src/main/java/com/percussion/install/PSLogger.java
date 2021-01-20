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

import com.percussion.util.PSProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * PSLogger sets the log file path and initializes log4j logger used
 * by all the custom installer ant tasks.
 */
public class PSLogger
{
   /**
    * Sets the log file path and initializes the log4j logger.
    *
    * @param rootDir the Rhythmyx root directory, may not be <code>null</code>
    * or empty.
    */
   public static void init(String rootDir)
   {
      if (rootDir == null || rootDir.trim().length() == 0)
      {
         throw new IllegalArgumentException("rootDir may not be null or empty");
      }
      
      //get the root dir
      String strLogFile = rootDir;
      
      if (!(strLogFile.endsWith(File.separator)))
         strLogFile += File.separator;
      
      strLogFile += ms_logFile;
      
      //set absolute log file path which is used to initialize log4j
      ms_absoluteLogFilePath = strLogFile;
      
      File f = new File(strLogFile);
      
      if(!f.exists())
      {
         //make sure directory exists
         f.getParentFile().mkdirs();
      }
      
      if (ms_Logger==null)
      {
         //init log4j
         ensureLog4jConfiguration(strLogFile);
         
         logInfo("Started Rx Log");      
      }
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * returns the log file path relative to the install directory
    * @return the log file path relative to the install directory,
    * never <code>null</code> or empty
    */
   public String getLogFile()
   {
      return ms_logFile;
   }
   
   /**
    * Sets the log file path relative to the install directory
    * @param logFile the log file path relative to
    * the install directory, may be <code>null</code> or empty in which the
    * default is used.
    */
   public void setLogFile(String logFile)
   {
      if ((logFile == null) || (logFile.trim().length() == 0))
         logFile = DEFAULT_LOG_FILE;
      
      ms_logFile = logFile;
   }
   
   /*************************************************************************
    * Static Public functions
    *************************************************************************/
   
   /**
    * Logs info level message into the log4j file. Accumulates any log entries 
    * that are queued before a log file name is known and then logs them all
    * once the log file name is set, which should happen right after user choses
    * the installation desination directory.   
    * @param log the string to be logged later in the specified log file,
    * may be <code>null</code> or empty.
    */
   static public void logInfo(Object log)
   {
      if (log == null)
         return;
      
      if (ms_Logger==null)
      {
         //init log4j
         ensureLog4jConfiguration(ms_absoluteLogFilePath);
      }
      
      Collection logList = getLogList("info");
      
      if (ms_Logger!=null)
      {
         if (logList!=null && logList.size()>0)
         {
            //dump whatever has accumulated before we knew where to log
            Iterator it = logList.iterator();
            while (it.hasNext())
            {
               Object obj = it.next();
               ms_Logger.info(obj);
            }
            
            logList.clear();
         }
         
         ms_Logger.info(log);
      }
      else
      {
         //collect all messages from the events that happened before we knew 
         //where the log file is..
         if (logList != null && ms_Logger!=null)
            logList.add(log);
         
         System.out.println(log);
      }
   }
   
   /**
    * Logs warn level message into the log4j file. Accumulates any log entries 
    * that are queued before a log file name is known and then logs them all
    * once the log file name is set, which should happen right after user choses
    * the installation desination directory.   
    * @param log the string to be logged later in the specified log file,
    * may be <code>null</code> or empty.
    */
   static public void logWarn(Object log)
   {
      if (log == null)
         return;
      
      if (ms_Logger==null)
      {
         //init log4j
         ensureLog4jConfiguration(ms_absoluteLogFilePath);
      }
      
      Collection logList = getLogList("warn");
      
      if (ms_Logger!=null)
      {
         if (logList!=null && logList.size()>0)
         {
            //dump whatever has accumulated before we knew where to log
            Iterator it = logList.iterator();
            while (it.hasNext())
            {
               Object obj = it.next();
               ms_Logger.warn(obj);
            }
            
            logList.clear();
         }
         
         ms_Logger.warn(log);
      }
      else
      {
         //collect all messages from the events that happened before we knew 
         //where the log file is..
         if (logList != null && ms_Logger!=null)
            logList.add(log);
         
         System.out.println(log);
      }
   }
   
   /**
    * Logs error level message into the log4j file. Accumulates any log entries 
    * that are queued before a log file name is known and then logs them all
    * once the log file name is set, which should happen right after user choses
    * the installation desination directory.   
    * @param log the string to be logged later in the specified log file,
    * may be <code>null</code> or empty.
    */
   static public void logError(Object log)
   {
      if (log == null)
         return;
      
      if (ms_Logger==null)
      {
         //init log4j
         ensureLog4jConfiguration(ms_absoluteLogFilePath);
      }
      
      Collection logList = getLogList("error");
      
      if (ms_Logger!=null)
      {
         if (logList!=null && logList.size()>0)
         {
            //dump whatever has accumulated before we knew where to log
            Iterator it = logList.iterator();
            while (it.hasNext())
            {
               Object obj = it.next();
               ms_Logger.error(obj);
            }
            
            logList.clear();
         }
         
         ms_Logger.error(log);
      }
      else
      {
         //collect all messages from the events that happened before we knew 
         //where the log file is..
         if (logList != null && ms_Logger!=null)
            logList.add(log);
         
         System.out.println(log);
      }
   }   
   
   
   /**
    * This method makes sure that log4j is configured for use.
    */
   private static synchronized void ensureLog4jConfiguration(String logFilePath)
   {
      if (ms_Logger == null)
      {
         if (logFilePath== null)
            return;
         
         ms_Logger = Logger.getRootLogger();
         
         // Not configured, setup a minimal configuration here that
         // logs to the console only. 
         PSProperties props = new PSProperties();
         
         String log4jFile = System.getProperty("ps.log4j.properties.file");
         
         if (log4jFile==null || log4jFile.trim().length() < 1)
         {
            setDefaultLog4JProps(props, logFilePath);
         }
         else
         {
            try {
               props = new PSProperties(log4jFile);
            }
            catch (Exception e)
            {
               e.printStackTrace();
               
               setDefaultLog4JProps(props, logFilePath);
            }
         }
         
         PropertyConfigurator.configure(props);
         
         ms_Logger = Logger.getLogger("com.percussion.install.PSLogger");
      }
   }
   
   /**
    * Sets default log4j props.
    * @param props <code>null</code>.
    */
   private static void setDefaultLog4JProps(Properties props, String logFilePath)
   {
      if (props == null)
         throw new IllegalArgumentException("props may not be null");
      
      //set console appender for WARN and up at rool level
      props.setProperty("log4j.rootCategory", "ALL, rxISFile");
      
      /*
       props.setProperty("log4j.appender.console",
       "org.apache.log4j.ConsoleAppender");
       props.setProperty("log4j.appender.console.layout", 
       "org.apache.log4j.PatternLayout");
       props.setProperty("log4j.appender.console.layout.ConversionPattern", 
       "%-5p [%c{1}] %d{MM/dd/yy HH:mm:ss}  %m%n");
       */
      
      props.setProperty("log4j.additivity.rxISFile", "false");
      
      props.setProperty("log4j.appender.rxISFile",
      "org.apache.log4j.RollingFileAppender");
      
      props.setProperty("log4j.appender.rxISFile.Threshold", "INFO");
      
      props.setProperty("log4j.appender.rxISFile.File", logFilePath);
      
      props.setProperty("log4j.appender.rxISFile.layout",
      "org.apache.log4j.PatternLayout");
      
      props.setProperty("log4j.appender.rxISFile.layout.ConversionPattern",
      "%-5p [%c{1}] %d{MM/dd/yy HH:mm:ss}  %m%n");
      
      props.setProperty("log4j.appender.rxISFile.MaxFileSize", "1024KB");
      props.setProperty("log4j.appender.rxISFile.MaxBackupIndex", "10");   
   }
   
   /**
    * Returns a llog list for a given log level.
    * @param level one of the Log4j log levels, never <code>null</code>
    * or <code>empty</code>.
    * @return coll of log entries, may be <code>null</code> or <code>empty</code>.
    */
   private static Collection getLogList(String level)
   {
      if (level== null || level.trim().length()==0)
         throw new IllegalArgumentException("level may not be null or empty");
      
      return (Collection) ms_mapLevel2LogList.get(level);
   }     
   
   /*************************************************************************
    * Static Variables
    *************************************************************************/
   /**
    * This reference to a root logger. Initialized in ensureLog4jConfiguration
    * may be <code>null</code> if used before that.
    */
   private static Logger ms_Logger = null;
   
   
   /**
    * Default log file
    */
   public static String DEFAULT_LOG_FILE =
      "rxconfig" + File.separator +
      "Installer" + File.separator +
      "install.log";
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * log file
    */
   private static String ms_logFile = DEFAULT_LOG_FILE;
   
   /**
    * Absolute resolved file path for log4j to use.
    */
   private static String ms_absoluteLogFilePath = null;
   
   /*************************************************************************
    * Private Variables
    *************************************************************************/
   
   /**
    * stores the log strings, grouped by a log level, to be logged later
    * to log file, never <code>null</code>.
    */
   static private Map  ms_mapLevel2LogList = new HashMap();
   
   static
   { 
      ms_mapLevel2LogList.put("trace", new ArrayList());
      ms_mapLevel2LogList.put("info", new ArrayList());
      ms_mapLevel2LogList.put("debug", new ArrayList());
      ms_mapLevel2LogList.put("warn", new ArrayList());
      ms_mapLevel2LogList.put("error", new ArrayList());
   }
   
}


