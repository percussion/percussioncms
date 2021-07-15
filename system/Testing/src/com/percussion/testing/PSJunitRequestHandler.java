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
package com.percussion.testing;

import com.percussion.conn.PSServerException;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import org.apache.log4j.AppenderSkeleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This loadable handler instantiantes a JUnit class and execute the test suite
 * in the context of the Rx server.
 * 
 * The class can use it's own class loader to instance requested classes so
 * no stopping/starting of the server is needed when a change to the 'testing'
 * class is made. The class loader is given a root path, and root directory 
 * of classes to load fresh each time. This is controlled w/ the 
 * useLocalClassLoader html parameter. By default, this is disable. Set it to
 * true to enable this feature.
 * 
 * The following should be added to the requestHandlers.xml config file. 
 * <pre>
 * &lt;RequestHandlerDef
 *     className="packagexxx.PSDefaultRequestHandler"
 *     configFile="okToNotExistConfig.xml" handlerName="defaultTestHandler"&gt;
 *    &lt;RequestRoots&gt;
 *        &lt;RequestRoot baseName="sys_junitTestHandler"&gt;
 *           &lt;RequestType&gt;POST&lt;/RequestType&gt;
 *           &lt;RequestType&gt;GET&lt;/RequestType&gt;
 *        &lt;/RequestRoot&gt;
 *     &lt;/RequestRoots&gt;
 * &lt;/RequestHandlerDef&gt;
 * 
 * example http request 
 * http://10.10.10.144:9992/Rhythmyx/sys_junitTestHandler?
 * useLocalClassLoader=true&
 * class=com.percussion.cms.dg.DBTest&
 * root=c:/e2/classes&
 * dir=c:/e2/classes/com/percussion/cms/dg
 * </pre>
 */

public class PSJunitRequestHandler implements IPSLoadableRequestHandler
{
   /**
    * see {@link IPSLoadableRequestHandler} interface 
    * for description.
    */
   public void init(Collection requestRoots, InputStream cfgFileIn)
      throws PSServerException
   {
      if (requestRoots == null || requestRoots.size() == 0)
         throw new IllegalArgumentException(
            "must provide at least one request root" );
      
      //suppress eclipse warning
      if (null == cfgFileIn);

      // validate that requestRoots contains only Strings
      for (Iterator iter = requestRoots.iterator(); iter.hasNext();)
      {
         if (!(iter.next() instanceof String))
            throw new IllegalArgumentException(
               "request roots collection may only contain String objects");
      }
      ms_requestRoots = requestRoots;
      
      PSConsole.printMsg(HANDLER, 
         "JUnit testing handler initialized.");
   }

   /**
    * See {@link IPSLoadableRequestHandler} interface
    * for description. 
    * 
    * This method will instantiate a class specified in the request and 
    * execute the specified TestCase by invoking the <code>suite</code>
    * method. 
    * <p>The following parameters are expected.
    * 
    * <table border="0">
    * <tr><td>The class to run:</td><td><code>HTML_PARAM_EXE</code></td></tr>
    * <tr><td>Class Loader will load any classes in or below:</td><td>
    * <code>HTML_PARAM_DIR</code></td></tr>
    * <tr><td>Classes directory:</td><td><code>HTML_PARAM_ROOT</code></td></tr>
    * <tr><td></td></tr>
    * </table>
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      System.out.println("Entered JUnit handler ...");
      
      PSResponse response = request.getResponse();      
      m_responseDoc = null;
      m_responseDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(m_responseDoc, 
         "Response");                
      
      // Appender to craft xml node's to display in the
      // result doc.
      Logger l = LogManager.getLogger();
      
      try
      {                           
         String strClassName = request.getParameter(HTML_PARAM_EXE, "");
         String strDirName = request.getParameter(HTML_PARAM_DIR, "");
         String strRootDev = request.getParameter(HTML_PARAM_ROOT, "");
         boolean useLocalClassLoader = request.getParameter(
               HTML_PARAM_LOCAL_LOADER, "").equalsIgnoreCase("true");
                                                
         if (strClassName.trim().length() == 0)
            throw new IllegalArgumentException(HTML_PARAM_EXE + 
               " html param must be supplied");

         if (useLocalClassLoader && strRootDev.trim().length() == 0)
            throw new IllegalArgumentException(HTML_PARAM_ROOT + 
               " html param must be supplied");

         // If a path for the class loader is not specified
         // use the classes directory
         if (strDirName.trim().length() == 0)
            strDirName = strRootDev;        
         
         ClassLoader classLoader = null;
         if (useLocalClassLoader)
            classLoader = createClassLoader(strDirName, strRootDev);
         else
            classLoader = ClassLoader.getSystemClassLoader();         
        
         try
         {            
            if (strClassName.trim().length() > 0)
            {
               // JUnit class to instantiate and run
               Class junitTest
                  = Class.forName(strClassName, true, 
                        classLoader);    

               Method suite = junitTest.getMethod("suite", (Class[]) null);
               System.out.println("Beginning JUnit test...");
               Test t = (Test) suite.invoke(null, (Object[]) null);
               
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
               PrintStream output = new PrintStream(bos); 
               TestRunner runner = new TestRunner(output);
               TestResult result = runner.doRun(t, false);
               System.out.println("JUnit test finished.");
               output.flush();
               String outputText = new String(bos.toByteArray());
               Element testResultEl = createTestResultResponse(m_responseDoc, 
                     result, strClassName, outputText);
               root.appendChild(testResultEl);
               System.out.println(outputText);
               PSXmlDocumentBuilder.toString(m_responseDoc);
            }
            else 
            {
               PSXmlDocumentBuilder.addElement(m_responseDoc, root, 
                  CLASSNOTFOUNDOREMPTY, 
                  "class name not found or empty: '" + strClassName + "'");
            }               
         }
         catch (Exception e)
         {
            PSXmlDocumentBuilder.addElement(m_responseDoc, root, 
               CLASSNOTFOUNDOREMPTY, 
               e.getMessage());
         }
         finally
         {
            /* In order to re-load a class that uses a dll, the original 
             * class loader must be removed from memory because the class
             * maintains a list of already loaded dlls and throws an 
             * exception if you try to load one again in another instance of
             * the class.
             */
            if (useLocalClassLoader)
            {
               classLoader = null;
               System.gc();
               System.gc();                        
            }
         }
         
         response.setContent(m_responseDoc);
      }
      catch (Exception e)
      {
         PSConsole.printMsg(HANDLER, e.getMessage());
      }
   }
   
   /**
    * Creates a class loader that is responsible for loading 
    * all requested classes.
    * 
    * @param strDir directory string. Never <code>null</code>
    *    or empty. 
    * 
    * @param strRootName directory of classes that may be requested
    *    of the class loader to create. Never <code>null</code>
    *    or empty.
    *  
    * @return class loader that loads a class fresh each time
    * it is requested if it appears in or below <code>strDir</code>.
    * Never <code>null</code>. 
    */
   private RefreshClassLoader createClassLoader(String strDir, 
      String strRootName)
   {
      // Threshold
      if (strDir == null || strRootName == null)
         throw new IllegalArgumentException(
            "strDir and strRootName must not be null");
                               
      // Threshold
      if (strDir.trim().length() == 0 || strRootName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strDir and strRootName must not be empty");

      List<File> l = new ArrayList<File>();

      File f = null;

      if (strDir.indexOf(strRootName) >= 0)
         f = new File(strDir);
      else 
         f = new File(strRootName, strDir);

      File parent = null;

      if (f.isDirectory())
      {
         parent = f;
      }
      else 
      {
         parent = f.getParentFile();
      }
   
      // Recursively add all dir's and sub dirs
      File [] children = parent.listFiles();
      
      for (int i=0; i<children.length; i++)
      {
         // recurse
         addAllFiles(children[i], l);
      }
      
      return new RefreshClassLoader(strRootName, l);
   }

   /**
    * Recursively loads all files and children into 
    * a list provided.
    * 
    * @param f file object to add. Never <code>null</code>.
    * @param l the list to add the <code>f</code> to. Never <code>
    *    null</code>.
    */
   private void addAllFiles(File f, List<File> l)
   {
      if (f == null || l == null)
         throw new IllegalArgumentException(
            "Both f and l must not be null");

      if (f.getName().endsWith("class"))       
         l.add(f);
      
      File [] list = f.listFiles();

      // Break out
      if (list == null)
         return;

      // Break out
      if (list.length == 0)
         return;

      for (int i=0; i<list.length; i++)
      {
         // recurse
         addAllFiles(list[i], l);
      }
   }

   /**
    * Based on a {@link junit.framework.TestResult} it creates a 
    * xml node.
    * 
    * @param doc xml document. Never <code>null</code>.
    * 
    * @param tr Junit test result. Never <code>null</code>.
    * 
    * @param strMethod the method that was run. Never <code>null</code> 
    *    or empty. 
    * 
    * @todo define any xml node names that may be needed in definition ...
    */
   private Element createTestResultResponse(Document doc, TestResult tr, 
      String strMethod, String consoleOutput)
   {
      // Thresholds
      if (doc == null || tr == null || strMethod == null)
         throw new IllegalArgumentException(
            "doc, tr and strMethod must not be null");

      if (strMethod.trim().length() == 0)
         throw new IllegalArgumentException(
            "strMethod must not be empty");

      Element testResultEl = doc.
         createElement(TESTRESULT);
      
      testResultEl.setAttribute("name", strMethod);      
      testResultEl.setAttribute("testCount", ""+tr.runCount());
      testResultEl.setAttribute("errors", ""+tr.errorCount());
      testResultEl.setAttribute("failures", ""+tr.failureCount());

      if (tr.wasSuccessful())
      {
         testResultEl.setAttribute("success", "true");
      }
      else
      {               
         testResultEl.setAttribute("success", "false");

         Enumeration errors = tr.errors();

         while (errors.hasMoreElements())
         {   
            Element errorEl = PSXmlDocumentBuilder.addEmptyElement(doc, 
                  testResultEl, TESTERROR);         
            Object o = errors.nextElement();
                        
            if (o instanceof TestFailure)
            {
               TestFailure tf = (TestFailure) o;
               String testName = tf.failedTest().toString();
               errorEl.setAttribute("testName", testName);
               
               Throwable t = (Throwable) tf.thrownException();
               PSXmlDocumentBuilder.addElement(doc, errorEl, "Message",
                     t.getMessage());

               Writer stackTrace = new StringWriter();
               t.printStackTrace(new PrintWriter(stackTrace));
               PSXmlDocumentBuilder.addElement(doc, errorEl, "Trace",
                     stackTrace.toString());
            }            
         }

         Enumeration failures = tr.failures();
            
         while (failures.hasMoreElements())
         {
            Element failureEl = PSXmlDocumentBuilder.addEmptyElement(doc, 
            testResultEl, TESTFAILURE);            
            Object o = failures.nextElement();
            
            if (o instanceof TestFailure)
            {
               TestFailure tf = (TestFailure) o;
               String testName = tf.failedTest().toString();
               failureEl.setAttribute("testName", testName);
               
               Throwable t = (Throwable) tf.thrownException();
               PSXmlDocumentBuilder.addElement(doc, failureEl, "Message",
                     t.getMessage());

               Writer stackTrace = new StringWriter();
               t.printStackTrace(new PrintWriter(stackTrace));
               PSXmlDocumentBuilder.addElement(doc, failureEl, "Trace",
                     stackTrace.toString());
            }            
         }
      }
      /* The purpose of this line is to extract the time from the beginning
       * of the console output. We don't want the rest of the output because
       * what is printed is equivalent to what we are returning as objects.
       */
      int len = consoleOutput.length() > 25 ? 25 : consoleOutput.length();
      PSXmlDocumentBuilder.addElement(doc, testResultEl, "ConsoleOutput", 
            consoleOutput.substring(0,len));
   
      return testResultEl;
   }

   /**
    * see {@link com.percussion.server.PSLoadableRequestHandler} interface 
    * for description.
    */
   public void shutdown()
   {
   }

   /**
    * see {@link com.percussion.server.PSLoadableRequestHandler} interface 
    * for description.
    */
   public String getName()
   {
      return HANDLER;
   }

   /**
    * see {@link com.percussion.server.PSLoadableRequestHandler} interface 
    * for description.
    */
   public Iterator getRequestRoots()
   {
      return ms_requestRoots.iterator();
   }

   /**
    * Inner class loader class appender to listen for 
    * log events and embed them in an xml response doc.  
    */
   private class XmlDocAppender extends AppenderSkeleton
   {
      //see base class            
      public void append(LoggingEvent event)
      {
          throw new IllegalArgumentException("Need to update for log4j2");
          /*
         // Threshold
         if (m_responseDoc == null)
            return;

         Object o = event.getMessage();      

         // Threshold
         if (o == null)
            return;
         
         Element parent = (Element) m_responseDoc.getFirstChild();

         if (parent == null)
         {
            parent = PSXmlDocumentBuilder.createRoot(m_responseDoc, 
               "Response");
         }
         
         String strMsg = "";
         Element aLogEl = null;
         
         if (o instanceof Exception)
         {
            Exception e = (Exception) o;
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            PrintStream pw = new PrintStream(bStream);
            e.printStackTrace(pw);            
            strMsg = bStream.toString();                        
            aLogEl = PSXmlDocumentBuilder.addElement(m_responseDoc, parent, 
               "LogMessageException", strMsg);                
            aLogEl.setAttribute("message", e.getMessage());
         }
         else
         {
            strMsg = o.toString();
            aLogEl = PSXmlDocumentBuilder.addElement(m_responseDoc, parent, 
               "LogMessage", strMsg);                  
         }
                                    
         aLogEl.setAttribute("level", event.getLevel().toString());
         aLogEl.setAttribute("name", event.getLoggerName());
         aLogEl.setAttribute("thread", event.getThreadName());   
         
         */
      }
      
      //see base class            
      public void close()
      {
         // no-op
      }

      //see base class            
      public boolean requiresLayout()
      {
         return false;
      }
   }

   /**
    * Inner class loader class to instantiate a specific 
    * class.
    */
   private class RefreshClassLoader extends ClassLoader
   {
      /**
       * Ctor that takes the root directory of classes and 
       * a list of file objects such that any file requested in this 
       * list will be 're'-loaded.
       * 
       * @param root. Directory of classes. Never <code>null</code> or
       *    empty.
       * 
       * @param dirs list of files to 'reload'. Never <code>null</code>.
       */
      public RefreshClassLoader(String root, List<File> dirs)
      {
         if (root == null || dirs == null)
            throw new IllegalArgumentException(
               "root and dirs must not be null");

         m_root = root;                 
         m_files = dirs;
      }
            
      //see base class            
      @SuppressWarnings("unchecked")
      protected Class loadClass (String name, boolean resolve) 
         throws ClassNotFoundException 
      {

         // Since all support classes of loaded class use same class loader
         // must check subclass cache of classes for things like Object
         Class c = null;
         
         // Convert class name argument to filename
         // Convert package names into subdirectories
         String filename = name.
            replace('.', File.separatorChar) + ".class";
           
         // Create a file object relative to directory provided 
         // and see if we should load it fresh
         File f = new File (m_root, filename);
         
         // Check if f should be reloaded
         if (!m_files.contains(f))
         {                      
            c = findLoadedClass (name);
            if (c == null) 
            {
               try 
               {
                  c = findSystemClass (name);
               } 
               catch (Exception e) 
               {
               }
            }
         }

         if (c == null) 
         {          
            try 
            {                              
               byte data[] = loadClassData(filename);
               System.out.println("Loading class " + name);      

               c = defineClass(name, data, 0, data.length);
               
               if (c == null)
                  throw new ClassNotFoundException(name);
            } 
            catch (IOException e) 
            {
               throw new ClassNotFoundException (
                  "Error reading file: " + filename);
            }
         }
         if (resolve)
            resolveClass (c);
         return c;
      }
      
      /**
       * Based on the filename reads in the bytes of this file.
       * 
       * @param filename to load. Never <code>null</code>
       * 
       * @throws IOException if filename doesn't specify a valid file or the 
       * data can't be read from the file.
       */
      private byte[] loadClassData (String filename) 
         throws IOException 
      {
         if (filename == null)
            throw new IllegalArgumentException(
               "filename must not be null");

         // Create a file object relative to directory provided
         File f = new File (m_root, filename);
         
         // Get size of class file
         int size = (int)f.length();

         // Reserve space to read
         byte buff[] = new byte[size];

         // Get stream to read from
         FileInputStream fis = new FileInputStream(f);
         DataInputStream dis = new DataInputStream (fis);

         // Read in data
         dis.readFully (buff);

         // close stream
         dis.close();

         // return data
         return buff;
      }
   
      /**
       * Root directory of classes. Passed in ctor, never <code>null</code>.
       */
      private String m_root = null;
      
      /**
       * List of files that should be reloaded. Initialized in definition, 
       * never <code>null</code>.
       */
      private List<File> m_files = new ArrayList<File>(); 

      /* (non-Javadoc)
       * @see java.lang.ClassLoader#findClass(java.lang.String)
       */
      @SuppressWarnings("unchecked")
      protected Class findClass(String name) throws ClassNotFoundException
      {
         Class c = super.findClass(name); 
         return c;
      }
   }

   /**
    * Accessor by inner classes as well ... Generated in <code>
    * processRequest</code>, may be <code>null</code>.
    */
   Document m_responseDoc = null;

   /**
    * Storage for the request roots, initialized in init() call, never
    * <code>null</code> or empty after that. A list of String objects.
    */
   public static Collection ms_requestRoots = null;

   /**
    * Name of the subsystem used to dump messages to server console.
    */
   public static final String HANDLER = "JUnitTestHandler";
   
   public static final String HTML_PARAM_DIR = "dir";
   public static final String HTML_PARAM_EXE = "class";
   public static final String HTML_PARAM_ROOT = "root";
   /**
    * This parameter is used to force the handler to load test classes for
    * the request. Useful when debugging and test cases are changing frequently
    * (i.e. the server doesn't need to be restarted each time).
    */
   public static final String HTML_PARAM_LOCAL_LOADER = "useLocalClassLoader";
   
   // Private defines
   private static final String TESTRESULT = "TestResult";
   private static final String TESTERROR = "TestError";
   private static final String TESTFAILURE = "TestFailure";
   private static final String CLASSNOTFOUNDOREMPTY = "ConfigClassError";
}
