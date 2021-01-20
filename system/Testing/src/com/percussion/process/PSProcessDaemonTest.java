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
package com.percussion.process;

import com.percussion.testing.IPSCustomJunitTest;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This JUnit based class tests the PSProcessDaemon.java class. It expects the
 * daemon to be listening on port 8888 and to be configured to use the 
 * processes.xml process definition file (which can be found in the junit 
 * resources). The processes.xml file should also
 * be copied to a directory under the virtual directory (as specified in the
 * daemon config file) w/ the following virtual location: 
 * "/foo/bar/processes.xml".
 * <p>You should end up w/ a directory structure like the following:
 * <pre>
 * {root}/
 *    rxconfig/
 *       Server/
 *          procdaemon.properties
 *          processes.xml
 *    virtual/
 *       foo/
 *          bar/
 *             processes.xml
 * </pre>
 * <p>The procdaemon.properties file must be modified for this test by setting 
 * the proper port, virtual root and procDefFilename values. 
 * <p>The 'ls' command must be available in a standard command window for one
 * of the tests to succeed.
 * <p>This test should be executed from the directory setup for the daemon.
 * 
 * @author paulhoward
 */
@Category(IntegrationTest.class)
public class PSProcessDaemonTest extends TestCase implements IPSCustomJunitTest
{

   /**
    * Constructor for PSProcessDaemonTest.
    * @param name required by framework
    */
   public PSProcessDaemonTest(String name)
   {
      super(name);
   }

   /**
    * Builds a totally bogus doc and makes a request.
    * @throws Exception
    */
   public void testBadRootRequest()
      throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Process");
      PSXmlDocumentBuilder.addElement(doc, root, "child", "foo");
      Document resultDoc = sendDoc(doc);
      checkErrorResultDoc(resultDoc);
   }

   /**
    * Builds a doc w/ a valid root, but no name and makes a request.
    * @throws Exception If any unexpected problems.
    */
   public void testMissingNameAttrRequest()
      throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(doc, 
         PSProcessRequest.XML_NODE_NAME);
      Document resultDoc = sendDoc(doc);
      checkErrorResultDoc(resultDoc);
   }

   /**
    * Makes a valid request using a process name that doesn't exist in the
    * process defs.
    * @throws Exception If any unexpected problems.
    */
   public void testInvalidProcNameRequest()
      throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, 
            PSProcessRequest.XML_NODE_NAME);
      root.setAttribute("procName", "foobar");
      Document resultDoc = sendDoc(doc);
      checkErrorResultDoc(resultDoc);
   }

   /**
    * Makes a request for the 'dirlisting' process.
    * @throws Exception If any unexpected problems.
    */
   public void testValidRequest()
      throws Exception
   {
      Map params = new HashMap();
      
      PSProcessRequest req = new PSProcessRequest("dirlisting", 5000,
         true, params);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.replaceRoot(doc, req.toXml(doc));
      Document resultDoc = sendDoc(doc);
      
      try
      {
         PSProcessRequestResult result = new PSProcessRequestResult(
               resultDoc.getDocumentElement());
         assertTrue(
            result.getStatusCode() == PSProcessRequestResult.STATUS_FINISHED);
         assertTrue(result.getResultCode() == 0);
         assertTrue(result.getResultText().trim().length() > 0);
         //a directory listing has the word 'total' in it
         assertTrue(result.getResultText().toLowerCase().indexOf("total") >= 0);
      }
      catch (Exception e)
      {
         fail("Invalid doc returned from daemon.");
      }
   }

   /**
    * Attempt to make a few directories on the remote machine.
    *  
    * @throws Exception If the send fails for any reason.
    */
   public void testMakeDirectories()
      throws Exception
   {
      StringBuffer result = new StringBuffer(1000);
      List param = new ArrayList();
      param.add("/a/b/c/d");
      assertTrue(sendCommand("mkdir", param, result) == 0);
      param.set(0, "a/b/e");
      assertTrue(sendCommand("mkdir", param, result) == 0);
   }

   /**
    * Attempt to make a add and remove some files and directories on the 
    * remote machine. For the results of this test to be valid, the {@link
    * #testMakeDirectories} && {@link #testPut()} methods must pass.
    *  
    * @throws Exception If the send fails for any reason.
    */
   public void testRemove()
      throws Exception
   {
      //first, set up a sample structure
      StringBuffer result = new StringBuffer(1000);
      List param = new ArrayList();
      param.add("/x/y/z");
      assertTrue(sendCommand("mkdir", param, result) == 0);
      
      String sample0Filename = "x/elephant.doc";
      param.set(0, sample0Filename);      
      String fileContent = "Why are elephants afraid of mice?";
      param.add(fileContent);
      assertTrue(sendCommand("put", param, result) == 0);
      
      String sample1Filename = "x/y/z/elephant2.doc";
      param.set(0, sample1Filename);      
      param.add(fileContent);
      assertTrue(sendCommand("put", param, result) == 0);
      
      String sample2Filename = "x/y/z/elephant3.doc";
      param.set(0, "x/y/z/elephant3.doc");      
      param.add(fileContent);
      assertTrue(sendCommand("put", param, result) == 0);
      
      //First delete elephant3.doc and make sure nothing else is gone
      param.clear();
      result.delete(0, result.length());
      param.add(sample1Filename);
      assertTrue(sendCommand("rm", param, result) == 0);
      assertTrue(result.length() == 0);
      param.set(0, sample2Filename);
      assertTrue(sendCommand("get", param, result) == 0);
      
      //now delete from root
      param.set(0, "x");
      assertTrue(sendCommand("rm", param, result) == 0);
      param.set(0, sample0Filename);
      assertTrue(sendCommand("get", param, result) > 0);
      param.set(0, sample2Filename);
      assertTrue(sendCommand("get", param, result) > 0);
   }

   /**
    * Make some directories and files, then check if they exist. For the 
    * results of this test to be valid, the {@link #testMakeDirectories()} && 
    * {@link #testPut()} methods must pass.
    *  
    * @throws Exception If the send fails for any reason.
    */
   public void testCheckExists()
      throws Exception
   {
      //first, set up a sample structure
      StringBuffer result = new StringBuffer(1000);
      List param = new ArrayList();
      String rootDir = "/checkexists";
      String sampleDir = rootDir + "/a";
      param.add(sampleDir);
      assertTrue(
            sendCommand(PSProcessDaemon.CMD_MAKE_DIRS, param, result) == 0);
      
      String sampleFilename = rootDir + "/elephant.doc";
      param.set(0, sampleFilename);      
      String fileContent = "Why are elephants afraid of mice?";
      param.add(fileContent);
      assertTrue(
            sendCommand(PSProcessDaemon.CMD_SAVE_FILE, param, result) == 0);
      
      //test directory existence
      param.clear();
      param.add(sampleDir);
      result.delete(0, result.length());
      assertTrue(
            sendCommand(PSProcessDaemon.CMD_FS_OBJ_EXISTS, param, result) == 0);
      assertTrue(result.charAt(0) == '1');
      
      //test file existence
      param.set(0, sampleFilename);
      result.delete(0, result.length());
      assertTrue(
            sendCommand(PSProcessDaemon.CMD_FS_OBJ_EXISTS, param, result) == 0);
      assertTrue(result.charAt(0) == '1');
      
      //test non-existence
      param.set(0, rootDir + "/b");
      result.delete(0, result.length());
      assertTrue(
            sendCommand(PSProcessDaemon.CMD_FS_OBJ_EXISTS, param, result) == 0);
      assertTrue(result.charAt(0) == '0');

      //clean up
      param.clear();
      param.add(rootDir);
      assertTrue(
            sendCommand(PSProcessDaemon.CMD_REMOVE_FS_OBJ, param, result) == 0);
   }

   /**
    * Attempts to write a file to the remote file system. This test requires 
    * that the {@link #testMakeDirectories()} and {@link #testGet()} tests 
    * succeed for this test to be valid.
    *
    * @throws Exception If the send fails for any reason.
    */
   public void testPut()
      throws Exception
   {
      StringBuffer result = new StringBuffer(1000);
      List param = new ArrayList();
      param.add("/a");
      sendCommand("mkdir", param, result);
      result.delete(0, result.length());

      String relFilename = "a/elephant.doc"; 
      param.set(0, "/" + relFilename);      
      String fileContent = "Why are elephants afraid of mice?";
      param.add(fileContent);
      assertTrue(sendCommand("put", param, result) == 0);
      
      result.delete(0, result.length());
      get(relFilename, result);
      assertTrue(result.indexOf("elephant") > 0);
      
      //try to overwrite the file
      param.set(0, relFilename);
      String fileContent2 = "Lions are big cats.";
      param.set(1, fileContent2);
      assertTrue(sendCommand("put", param, result) == 0);

      result.delete(0, result.length());
      get("/" + relFilename, result);
      assertTrue(result.indexOf("cats") > 0);
   }

   /**
    * Send a command unknown to the daemon and verify it fails.
    * 
    * @throws Exception If the send fails for any reason.
    */
   public void testInvalidCommand()
      throws Exception
   {
      StringBuffer result = new StringBuffer(1000);
      List param = new ArrayList();
      param.add("param1");
      assertTrue(sendCommand("foobar", param, result) == -1);
   }

   /**
    * Executes the 'get' command w/ valid and invalid paths. Assumes that a
    * director called "/foo/bar" exists w/ the processes.xml file.
    * 
    * @throws Exception If the send fails for any reason.
    */
   public void testGet()
      throws Exception
   {
      // supply a non-existent file path
      StringBuffer result = new StringBuffer(1000);
      assertTrue(get("/bad/path/processes.xml", result) > 0);
      assertTrue(result.toString().indexOf("FileNotFoundException") > 0);
      result.delete(0, result.length());

      // supply paths w/ .. in them 
      assertTrue(get("../bad/path/processes.xml", result) > 0);
      assertTrue(result.toString().indexOf("supplied") > 0);
      assertTrue(get("..", result) > 0);

      //perform a valid get
      assertTrue(get("/foo/bar/processes.xml", result) == 0);
      assertTrue(result.toString().indexOf("PSSimpleProcess") > 0);
   }

   /**
    * Creates a {@link PSLocalCommandHandler} and a {@link 
    * PSRemoteCommandHandler} and executes all the methods for each instance,
    * treating them each as {@link IPSCommandHandler}s. 
    */
   public void testCommandInterface()
   {
      try
      {
         IPSCommandHandler[] handlers = new IPSCommandHandler[2];
         handlers[0] = new PSLocalCommandHandler(null, 
               new File("./rxconfig/Server/processes.xml"));
         handlers[1] = new PSRemoteCommandHandler("localhost", 8888, new File("\\"));
         
         for (int i = 0; i < handlers.length; i++)
         {
            IPSCommandHandler handler = handlers[i];
            File rootDir = new File("handlers");
            handler.makeDirectories(new File(rootDir, "n/o"));
            assertTrue(handler.fileSystemObjectExists(rootDir));
            File path = new File(rootDir, "elephant.doc");
            handler.saveTextFile(path, "Elephants");
            assertTrue(handler.getTextFile(path).indexOf("hants") > 0);
            handler.removeFileSystemObject(path);
            try
            {
               handler.getTextFile(path);           
               assertTrue(false);
            }
            catch (IOException e)
            { /* expected */ }
            
            handler.saveTextFile(path, "Elephants");
            assertTrue(handler.fileSystemObjectExists(path));
            assertTrue(!handler.fileSystemObjectExists(new File("/nonexist")));
            handler.removeFileSystemObject(rootDir);
            try
            {
               handler.getTextFile(path);           
               assertTrue(false);
            }
            catch (IOException e)
            { /* expected */ }            
         }
      }
      catch (PSProcessException e)
      {
         assertTrue(false);
      }
      catch (IOException e)
      {
         assertTrue(false);
      } catch (SAXException e)
      {
         assertTrue(false);
      }
   }

   /**
    * Convenience method that packages the supplied path into a list and calls
    * {@link #sendCommand(String, List, StringBuffer) sendCommand(list, 
    * result)}.
    * 
    * @param path Assumed not <code>null</code> or empty.
    *
    * @throws Exception If the send fails for any reason.
    */
   private int get(String path, StringBuffer result)
      throws Exception
   {
      List param = new ArrayList();
      param.add(path);
      return sendCommand("get", param, result);
   }
      
      
   /**
    * Convenience method that calls {@link PSProcessDaemon#sendCommand(String, 
    * int, String, List, StringBuffer) PSProcessDaemon.sendCommand("localhost",
    * 8888, cmdName, params, result)}.
    */
   private int sendCommand(String cmdName, List params, StringBuffer result)
      throws Exception
   {
      return PSProcessDaemon.sendCommand("localhost", 8888, cmdName, params, 
            result);
   }

   /**
    * Checks that the supplied doc is a {@link PSProcessRequestResult} and
    * that the status code indicates error, the result code is -1 and that
    * the word 'java' doesn't appear in the result text.
    * 
    * @param resultDoc Assumed not <code>null</code>.
    */
   private void checkErrorResultDoc(Document resultDoc)
   {
      try
      {
         PSProcessRequestResult result = new PSProcessRequestResult(
               resultDoc.getDocumentElement());
         assertTrue(result.getStatusCode() == PSProcessRequestResult.STATUS_ERROR);
         assertTrue(result.getResultCode() == -1);
         // a simple check looking for exception such as java.lang.NullPtr...
         assertTrue(result.getResultText().toLowerCase().indexOf("java") < 0);
      }
      catch (Exception e)
      {
         fail("Invalid doc returned from daemon.");
      }
   }

   /**
    * Opens a socket to localhost on port 8888 and sends the supplied doc
    * as a UTF-8 stream to the process daemon using its specified protocal.
    * It reads the results from that request, builds and xml doc and returns
    * it.
    * 
    * @param input Assumed not <code>null</code>.
    * @return Never <code>null</code>.
    * @throws Exception If any unexpected problems.
    */
   private Document sendDoc(Document input)
      throws Exception
   {
      String output = PSXmlDocumentBuilder.toString(input);
      System.out.println(output);
      List params = new ArrayList();
      params.add(output);
      StringBuffer result = new StringBuffer(1000);
      int errorCode = sendCommand("execprocess", params, result);
      assertTrue("Error code returned from daemon.", errorCode == 0);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(result.toString()), false);
      System.out.println(PSXmlDocumentBuilder.toString(doc));
      return doc;
   }


   /**
    * Required by framework.
    * @return Never <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite(PSProcessDaemonTest.class);
      return suite;
   }

   /**
    * Used to run the test from the command line.
    * @param args unused
    */
   public static void main(String[] args)
   {
      TestRunner.run(PSProcessDaemonTest.class);
   }

}
