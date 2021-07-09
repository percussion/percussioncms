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
