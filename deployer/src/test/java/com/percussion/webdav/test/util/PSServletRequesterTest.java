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
package com.percussion.webdav.test.util;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.client.PSBinaryValueEx;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.servlet.PSServletRequester;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.method.PSWebdavUtils;
import org.apache.log4j.Logger;
import org.junit.experimental.categories.Category;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is used to test PSServletRequester class
 */
@Category(IntegrationTest.class)
public class PSServletRequesterTest extends PSWebdavServlet
{

   // see HttpServlet.service(HttpServletRequest req, HttpServletResponse resp)
   protected void service (HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
   {
      resp.addHeader("Content-Type", "text/html");
      m_requester = getRemoteRequester(req, resp);
      m_writer = resp.getWriter();
           
       
      // Run tests
      runTest("testGetCommunities");
      runTest("testGetWorkflows");
      runTest("testGetContentTypes");
      runTest("testGetContextVariables");
      runTest("testGetTransitions");
      runTest("testFolders");
      runTest("testOpenItem");
      runTest("testUpdateBinaryNew");
   
 
      
   }


   /**
    * Test get communities
    *
    * @throws Exception if an error occurs
    */
   public void testGetCommunities() throws Exception
   {
      PSEntry community = getRemoteAgent().getDefaultUserCommunity();
      PSEntry defaultCommunity =
         new PSEntry("10", new PSDisplayText("Default"));
      assertTrue(community.equals(defaultCommunity));

      List communities = getRemoteAgent().getCommunities();
      Iterator it = communities.iterator();
      while(it.hasNext())
         writeln(it.next().toString());
      assertTrue(communities.contains(defaultCommunity));
   }

   /**
    * Test get workflows
    *
    * @throws Exception if an error occurs
    */
   public void testGetWorkflows() throws Exception
   {
      PSEntry defaultCommunity =
         new PSEntry("10", new PSDisplayText("Default"));
      PSEntry wfArticle = new PSEntry("1", new PSDisplayText("Article"));
      PSEntry wfIndex = new PSEntry("2", new PSDisplayText("Index"));
      PSEntry wfImages = new PSEntry("3", new PSDisplayText("Images"));

      List workflows = getRemoteAgent().getWorkflows(defaultCommunity);
      Iterator it = workflows.iterator();
      while(it.hasNext())
         writeln(it.next().toString());
      assertTrue(workflows.contains(wfArticle));
      assertTrue(workflows.contains(wfIndex));
      assertTrue(workflows.contains(wfImages));
   }

   /**
    * Test get content types
    *
    * @throws Exception if an error occurs
    */
   public void testGetContentTypes() throws Exception
   {
      PSEntry defaultCommunity =
         new PSEntry("10", new PSDisplayText("Default"));
      PSEntry article = new PSEntry("1", new PSDisplayText("Article"));

      List contentTypes = getRemoteAgent().getContentTypes(defaultCommunity);
      Iterator it = contentTypes.iterator();
      while(it.hasNext())
         writeln(it.next().toString());
      assertTrue(contentTypes.contains(article));
   }

   /**
    * Test get context variables
    *
    * @throws Exception if an error occurs
    */
   public void testGetContextVariables() throws Exception
   {
      PSEntry rxcss =
         new PSEntry(
            "web_resources/xroads/resources/css",
            new PSDisplayText("rxcss"));

      List ctxVars = getRemoteAgent().getContextVariables();
      Iterator it = ctxVars.iterator();
      while(it.hasNext())
         writeln(it.next().toString());
      assertTrue(ctxVars.contains(rxcss));
   }

   /**
    * Test get transitions for a given workflow
    *
    * @throws Exception if an error occurs
    */
   public void testGetTransitions() throws Exception
   {
      PSEntry wfIndex = new PSEntry("2", new PSDisplayText("Index"));
      PSEntry submit = new PSEntry("SubmitToQA", new PSDisplayText("Submit"));
      PSEntry approve = new PSEntry("Approve", new PSDisplayText("Approve"));

      List transitions = getRemoteAgent().getTransitions(wfIndex);
      Iterator it = transitions.iterator();
      while(it.hasNext())
         writeln(it.next().toString());
      assertTrue(transitions.contains(submit));
      assertTrue(transitions.contains(approve));
   }


   
   /**
    * Test for folders
    * @throws Exception
    */
   public void testFolders()
     throws Exception
   {
      String FOLDER_PATH = "//Folders/TestFolder";
      String FOLDER_NAME = "TestFolder";
      
      
         
      PSComponentSummary folderSummary =
         PSWebdavUtils.getComponentByPath(FOLDER_PATH, m_requester);
      if (folderSummary == null)
      {
         writeln("\"" + FOLDER_PATH + "\" does not exist.");
         PSLocator parent = new PSLocator(3, 1);
         List props = new ArrayList();
         PSWebdavUtils.createFolder(FOLDER_NAME, props.iterator(), parent,
               m_requester);
            
         folderSummary =
            PSWebdavUtils.getComponentByPathRqd(FOLDER_PATH, m_requester);
         writeln("Created folder: " + FOLDER_PATH);
      }
      else
      {
         writeln("\"" + FOLDER_PATH + "\" exists.");
      }
         
      PSRemoteAgent agent = new PSRemoteAgent(m_requester);
      agent.purgeTree(folderSummary);
      writeln("Deleted folder: " + FOLDER_PATH);
               
   }
   
   /**
    * Test opening an item
    * @throws Exception
    */
   public void testOpenItem() throws Exception
   {
      
      PSRemoteAgent agent = getRemoteAgent();
      PSLocator locator = new PSLocator("302", "1");
      agent.openItem(locator, true, true);      
      
   }
   
   /**
    * Test update binary with new item
    * 
    * @throws Exception if an error occurs.
    */
   public void testUpdateBinaryNew() throws Exception
   {
      PSRemoteAgent remoteAgent = getRemoteAgent();
      
      PSClientItem item = remoteAgent.newItem("4");
      
      //Set fields
      PSItemField itemField = null;
      itemField = item.getFieldByName("sys_title");
      itemField.addValue(new PSTextValue("Automated Test Image (Servlet Test)"));
      
      //Get the binary data
      File file = new File("c:/testimage2.gif");
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      fis.read(data);
      fis.close();
      
      PSItemField binaryField = item.getFieldByName("imgbody");
      binaryField.addValue(new PSBinaryValueEx(data, "testItAgain.gif", null));
      
      remoteAgent.updateItem(item, true); 
      
   }
   
   /**
    * Returns the remote agent
    * @return the remote agent, never <code>null</code>.
    */
   private PSRemoteAgent getRemoteAgent()
   {
      return new PSRemoteAgent(m_requester);
   }
   
   /**
    * Get the remote requester, which can be used to communicate with the
    * remote Rhythmyx Server.
    *
    * @param req The request object, assume not <code>null</code>.
    * @param resp The response object, assume not <code>null</code>.
    * 
    * @return The remote requester, never <code>null</code>.
    */
   private IPSRemoteRequester getRemoteRequester(
      HttpServletRequest req,
      HttpServletResponse resp)
   {
      return new PSServletRequester(
         req,
         resp,
         getServletContext().getContext("/Rhythmyx"));
   }
   
   /**
    * Writes a line to the response include
    * an html line break
    * @param s
    */
   private void writeln(String s)
   {
      writeln(s, NORMAL);
   }
   
   /**
    * Writes a line to the response with specified
    * style
    * @param s
    * @param style
    */
   private void writeln(String s, int style)
   {
       
       switch(style)
       {
          case SUCCESS:
             s = "<font color=\"green\">" + s + "</font><br>"; 
             break;
          
          case TITLE:
             s = "<b>" + s + "</b><br>";
             break;
          
          case ERROR:
             s = "<font color=\"red\">" + s + "</font><br>";
             break;
          
          default:
             s += "<br>";
       
       }
       m_writer.println(s);  
   }
   
   /**
    * Writes the exception's stacktrace to the
    * responses writer with the correct html
    * formatting tags
    * @param e
    */
   private void writeStackTrace(Exception e)
   {
       m_writer.println("<pre><font size=\"2\" color=\"blue\">");
       e.printStackTrace(m_writer);
       m_writer.println("</font></pre><br>");  
   }
   
   /**
    * Invokes the test method specified via reflection
    * @param test
    */
   private void runTest(String test)
   {
        
      try
      {
         Class me = this.getClass();
         Method method = me.getMethod(test, null);
         writeln("Starting: " + test, TITLE);         
         method.invoke(this, null);
         writeln(test + " ran successfully", SUCCESS);
         writeln("");
      }
      catch (SecurityException e)
      {
         writeStackTrace(e);
      }
      catch (IllegalArgumentException e)
      {
         writeStackTrace(e);
      }
      catch (NoSuchMethodException e)
      {
         writeStackTrace(e);
      }
      catch (IllegalAccessException e)
      {
         writeStackTrace(e);
      }
      catch (InvocationTargetException e)
      {
                  
         writeln(test + " failed with errors", ERROR);
         writeStackTrace((Exception)e.getTargetException());
         writeln("");
         
         //EC_UNRELATED_TYPES 
         Logger.getLogger(getClass()).error(e.getLocalizedMessage(),e);
          
      }      
   
   }
   
   private void assertTrue(boolean b) throws Exception
   {
       if(!b)
          throw new Exception("assertTrue failed."); 
   }  

   // Using instance fields in a servlet is bad
   // but I don't expect more then one person to
   // be hitting this at one time.
   private PrintWriter m_writer;
   private IPSRemoteRequester m_requester;
   
   
   // Html line display styles
   private final static int NORMAL = 1;
   private final static int TITLE = 2;
   private final static int SUCCESS = 3;
   private final static int ERROR = 4;
         
}
