/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
