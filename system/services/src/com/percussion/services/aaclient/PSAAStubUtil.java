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
package com.percussion.services.aaclient;

import com.percussion.server.PSServer;
import com.percussion.util.PSStringTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This utility class is used to retrieve various "template" files.
 * The retrieved strings are cached, which can be reseted by calling
 * the reset method. For example, specify the parameter of sys_reinit=true 
 * will cause the assembly servlet to call the reset method, which can be very
 * usefull for debugging purpose.
 */
public class PSAAStubUtil
{
   static public PSStringTemplate getAaPageHeader()
   {
      if (m_aaPageHeader == null)
         m_aaPageHeader = new PSStringTemplate(
            readFileContent(HEADER_FILE_PATH));
      return m_aaPageHeader;
   }

   static public PSStringTemplate getAaPageActionBar()
   {
      if (m_aaPageActionBar == null)
         m_aaPageActionBar = new PSStringTemplate(
            readFileContent(ACTIONBAR_FILE_PATH));
      return m_aaPageActionBar;
   }
   
   static public PSStringTemplate getPageActions()
   {
      if (m_pageActions == null)
         m_pageActions = new PSStringTemplate(
            readFileContent(AB_FILE_PAGE_ACTIONS_PATH));
      return m_pageActions;
   }

   static public PSStringTemplate getSlotActions()
   {
      if (m_slotActions == null)
         m_slotActions = new PSStringTemplate(
            readFileContent(AB_FILE_SLOT_ACTIONS_PATH));
      return m_slotActions;
   }

   static public PSStringTemplate getSnippetActions()
   {
      if (m_snippetActions == null)
         m_snippetActions = new PSStringTemplate(
            readFileContent(AB_FILE_SNIPPET_ACTIONS_PATH));
      return m_snippetActions;
   }

   static public String getAaPageFooter()
   {
      if (m_aaPageFooter == null)
         m_aaPageFooter = readFileContent(PAGEFOOTER_FILE_PATH);
      return m_aaPageFooter;
   }


   static public void reset()
   {
      m_aaPageHeader = null;
      m_aaPageActionBar = null;
      m_pageActions = null;
      m_slotActions = null;
      m_snippetActions = null;
      m_aaPageFooter = null;
   }

   /**
    * @param fname
    */
   static private String readFileContent(String fname)
   {
      InputStreamReader reader = null;
      StringWriter writer = null;
      try
      {
         reader = new InputStreamReader(new FileInputStream(new File(PSServer
            .getRxDir(), fname)), "UTF8");
         writer = new StringWriter();
         IOUtils.copy(reader, writer);
         writer.flush();
         return writer.toString();
      }
      catch (IOException e)
      {
         ms_log.error("Fatal error active assembly will not function");
         ms_log.error(e);
      }
      finally
      {
         IOUtils.closeQuietly(reader);
         IOUtils.closeQuietly(writer);
      }
      return "";
   }

   // Html templates/stubs
   static private PSStringTemplate m_aaPageHeader = null;

   static private PSStringTemplate m_aaPageActionBar = null;

   static private PSStringTemplate m_pageActions = null;

   static private PSStringTemplate m_slotActions = null;

   static private PSStringTemplate m_snippetActions = null;
   
   static private String m_aaPageFooter = null;

   // File paths for these
   static private final String HTMLBASE_PATH = "sys_resources"
      + File.separator + "html" + File.separator;
   
   static private final String HEADER_FILE_PATH = 
      HTMLBASE_PATH + "sys_aaPageHeader.html";

   static private final String ACTIONBAR_FILE_PATH = 
      HTMLBASE_PATH + "sys_aaPageActionBar.html";

   static private final String PAGEFOOTER_FILE_PATH = 
      HTMLBASE_PATH + "sys_aaPageFooter.html";

   static private final String AB_FILE_PAGE_ACTIONS_PATH = 
      HTMLBASE_PATH + "sys_aaPageActions.html";

   static private final String AB_FILE_SLOT_ACTIONS_PATH = 
      HTMLBASE_PATH + "sys_aaSlotActions.html";

   static private final String AB_FILE_SNIPPET_ACTIONS_PATH = 
      HTMLBASE_PATH + "sys_aaSnippetActions.html";

   private static final Logger ms_log = LogManager.getLogger(PSAAStubUtil.class);
}
