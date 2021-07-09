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


package com.percussion.installerbot;

import org.apache.commons.lang.StringUtils;

import expectj.ExpectJ;
import expectj.ExpectJException;
import expectj.SpawnedProcess;

/**
 * {@link IPSConsoleAppDriver} based on ExpectJ.
 * @see <a href="http://expectj.sourceforge.net/">ExpectJ Project</a> 
 * @author Andriy Palamarchuk
 */
public class PSExpectJConsoleAppDriver implements IPSConsoleAppDriver
{
   /**
    * The console application process.
    */
   SpawnedProcess m_spawnedProcess;
   
   /**
    * @see #setLogFile(String)
    */
   private String m_logFile;

   /**
    * @see IPSConsoleAppDriver#launchApplication(String, long)
    */
   public void launchApplication(String command, long defaultTimeoutInSec)
         throws PSConsoleAppDriverException
   {
      assert StringUtils.isNotBlank(getLogFile());
      try
      {
         m_spawnedProcess = new ExpectJ(getLogFile(), defaultTimeoutInSec).spawn(command);
      }
      catch (ExpectJException e)
      {
         throw new PSConsoleAppDriverException(e); 
      }
   }

   /**
    * @see IPSConsoleAppDriver#expect(String)
    */
   public void expect(String pattern) throws PSConsoleAppDriverException
   {
      try
      {
         m_spawnedProcess.expect(pattern);
      }
      catch (ExpectJException e)
      {
         throw new PSConsoleAppDriverException(e);
      }
   }

   // see interface
   public void expect(final String pattern, final long timeoutInSec)
         throws PSConsoleAppDriverException
   {
      try
      {
         m_spawnedProcess.expect(pattern, timeoutInSec);
      }
      catch (ExpectJException e)
      {
         throw new PSConsoleAppDriverException(e);
      }
   }

   /**
    * @see IPSConsoleAppDriver#send(String)
    */
   public void send(String line) throws PSConsoleAppDriverException
   {
      System.out.print(">>>" + line);
      try
      {
         m_spawnedProcess.send(line);
      }
      catch (ExpectJException e)
      {
         throw new PSConsoleAppDriverException(e);
      }
   }

   /**
    * @see IPSConsoleAppDriver#stop()
    */
   public void stop()
   {
      m_spawnedProcess.stop();
   }

   /**
    * @see IPSConsoleAppDriver#isLastExpectTimeOut()
    */
   public boolean isLastExpectTimeOut()
   {
      return m_spawnedProcess.isLastExpectTimeOut();
   }

   /**
    * @see IPSConsoleAppDriver#isClosed()
    */
   public boolean isClosed()
   {
      return m_spawnedProcess.isClosed();
   }

   /**
    * @see IPSConsoleAppDriver#getLogFile()
    */
   public String getLogFile()
   {
      return m_logFile;
   }

   /**
    * @see IPSConsoleAppDriver#setLogFile(String)
    */
   public void setLogFile(final String logFile)
   {
      this.m_logFile = logFile;
   }

   /**
    * @see IPSConsoleAppDriver#getCurrentStandardOutContents()
    */
   public String getCurrentStandardOutContents()
   {
      return m_spawnedProcess.getCurrentStandardOutContents();
   }
}
