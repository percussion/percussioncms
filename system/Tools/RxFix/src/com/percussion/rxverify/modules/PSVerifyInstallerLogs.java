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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSInstallation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author dougrand
 *
 * Check the installer logs for errors
 */
public class PSVerifyInstallerLogs implements IPSVerify
{

   /*
    *  (non-Javadoc)
    * @see com.percussion.rxverify.IPSVerify#generate(java.io.File, com.percussion.rxverify.PSInstallation)
    */
   public void generate(File rxdir, PSInstallation installation) throws IOException
   {
      // Do nothing for log checks

   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.rxverify.IPSVerify#verify(com.percussion.rxverify.PSInstallation, java.io.File)
    */
   public void verify(File rxdir, File originalRxDir, PSInstallation installation) throws IOException
   {
      if (installation == null)
      {
         throw new IllegalArgumentException("installation must never be null");
      }
      if (rxdir == null)
      {
         throw new IllegalArgumentException("rxdir must never be null");
      }
      
      Logger l = LogManager.getLogger(getClass());
      // Check the install log for warnings or errors
      l.info("Checking the installer log for warnings or errors");
      checkLogFile(rxdir);
      
      // Check the install.txt file from InstallShield for errors
      // or exception traces
      File installlog = new File(rxdir, "rxconfig/Installer/installlog.txt");
      if (installlog.exists())
      {
         l.info("Check the installlog.txt file");
         containsError(installlog);
      }
      
      // Tablefactory log files
      File tflog = new File(rxdir, "logs/tableFactory.log");
      File tflog2 = new File(rxdir, "rxInstallUpdate/tablefactoryLog.txt");
      
      l.info("Check the table factory log files");
      if (tflog.exists())
      {
         containsError(tflog);
      }
      if (tflog2.exists())
      {
         containsError(tflog2);
      }

      // Check the upgrade plugin output for errors
      l.info("Check the upgrade plugins for errors");
      checkUpgradeLogs(rxdir);
   }
   

   /**
    * Check through upgrade logs for errors. Just report what logs had errors
    * (if any)
    * 
    * @param rxdir The rhythmyx directory, assumed not <code>null</code> and
    *           that the directory exists
    * @throws IOException if there is a problem opening or reading one of the
    *            upgrade log files
    */
   private void checkUpgradeLogs(File rxdir) throws IOException
   {
      Logger l = LogManager.getLogger(getClass());
      File upgrade = new File(rxdir, "upgrade");

      if (upgrade.exists() == false)
      {
         l.info("Not an upgrade installation");
         return;
      }
      File logs[] = upgrade.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            String fullname = pathname.getName();
            return fullname.startsWith("upgrade") && fullname.endsWith(".log");
         }
      });
      for (int i = 0; i < logs.length; i++)
      {
         File logfile = logs[i];
         if (containsError(logfile))
         {
            l.error("Error in upgrade log {}", logfile);
         }
      }

   }

   /**
    * Read through the given file and return <code>true</code> if the string
    * <q>error</q>
    * occurs in the file.
    * 
    * @param logfile The given logfile to scan, assumed not <code>null</code>
    *           and existent
    * @return <code>true</code> if the string
    *         <q>error</q>
    *         occurs in the file.
    * @throws IOException if there is a problem opening or reading the file
    */
   private boolean containsError(File logfile) throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(logfile));
      String line = null;
      while ((line = reader.readLine()) != null)
      {
         line = line.toLowerCase();
         if (line.indexOf("error") >= 0)
            return true;
         
         if (line.indexOf("exception") >= 0)
            return true;
      }
      return false;
   }

   /**
    * Check the log file for warnings or errors. The format of the install file
    * is assumed to be such that warnings and errors will be at the start of the
    * line and using the strings "ERRORS" and "WARN", which are the convention
    * in log4j.
    * 
    * @param rxdir The Rhythmyx directory, assumed not <code>null</code>
    * @throws IOException if there's a problem opening or reading the installer
    *            log
    */
   private void checkLogFile(File rxdir) throws IOException
   {
      Logger l = LogManager.getLogger(getClass());
      File installlog = new File(rxdir, "rxconfig/Installer/install.log");

      if (installlog.exists() == false)
      {
         l.error("Installer log is missing: {}", installlog);
         return;
      }

      BufferedReader reader = new BufferedReader(new FileReader(installlog));
      String line = null;
      while ((line = reader.readLine()) != null)
      {
         if (line.startsWith("FATAL"))
         {
            l.fatal("{}",line);
         }
         else if (line.startsWith("ERROR"))
         {
            l.error("{}",line);
         }
         else if (line.startsWith("WARN"))
         {
            l.warn("{}",line);
         }
      }
   }   
}
