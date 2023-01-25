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
package com.percussion.testing;

import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSServer;
import com.percussion.util.PSRemoteRequester;
import com.percussion.xml.PSXmlTreeWalker;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Run the given test on the server using an HttpRequest. The server must be
 * running the {@link com.percussion.testing.PSJunitRequestHandler} loadable
 * handler.
 * <p>This class does not support the <code>useLocalClassLoader</code> property
 * of the handler.
 */
public class PSRequestHandlerTestSuite extends TestSuite 
{

   private static final Logger log = LogManager.getLogger(PSRequestHandlerTestSuite.class);

   /*
    * This is the necessary request handler to run tests using
    * this class
      <RequestHandlerDef handlerName="JUnitTestHandler"
        className="com.percussion.testing.PSJunitRequestHandler" >
        <RequestRoots>
           <RequestRoot baseName="sys_junitTestHandler">
              <RequestType>POST</RequestType>
              <RequestType>GET</RequestType>
           </RequestRoot>
        </RequestRoots>
      </RequestHandlerDef>
   */
   
   /**
    * Create a new instance of the testsuite.
    * 
    * @param clazz The class to use when constructing the
    * test suite. {@link TestSuite} uses reflection to find all 
    * the methods that start with "test" to construct a suite. 
    * Must never be <code>null</code>.
    */
   public PSRequestHandlerTestSuite(Class clazz)
   {
      super(clazz);
      m_testClass = clazz;
   }

   /**
    * Ctor that can be used if the 
    * {@link TestSuite#addTest(junit.framework.Test) addTest} methods are 
    * needed. 
    */
   public PSRequestHandlerTestSuite() 
   {
      try 
      {
         Exception e = new Exception();
         StackTraceElement[] stackTrace = e.getStackTrace();
         StackTraceElement el = stackTrace[1];
         m_testClass = Class.forName(el.getClassName());
      } 
      catch (ClassNotFoundException e) 
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }
   
   /* (non-Javadoc)
    * @see junit.framework.Test#run(junit.framework.TestResult)
    */
   public void run(TestResult results)
   {
      // If this is outside of Rhythmyx, fire a request to Rhythmyx
      // to invoke this class through the handler. Otherwise run
      // the suite
      if (insideRhythmyx())
      {
         super.run(results);
      }
      else
      {
         try
         {
            invoke(results);
         }
         catch (Exception e)
         {
            results.addError(this, e);
         }
      }

   }

   /**
    * Invoke runs the test inside of Rhythmyx. It does this by using
    * a special loadable request handler. The request handler is available on
    * the specific application sys_junitTestHandler. The results are
    * returned from Rhythmyx and this code then parses the results 
    * and reflects the results onto the testResults object that this
    * is called with.
    * 
    * @param testResults A never <code>null</code> instance of 
    * {@link TestResult} that is supplied by junit and modified 
    * using the results from the run.
    */
   private void invoke(TestResult testResults) throws Exception
   {
      Properties connInfo = m_configHelper.
         getConnectionProps(IPSClientBasedJunitTest.CONN_TYPE_RXSERVER);

      PSRemoteRequester req = new PSRemoteRequester(connInfo);

      String resource = "sys_junitTestHandler";
      Map params = new HashMap();
      params.put("class", m_testClass.getName());
      Document doc = req.getDocument(resource, params);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      Element result = walker.getNextElement("TestResult");
      NodeList results = result.getChildNodes();
      int count = results.getLength();
      Map errors = new HashMap();
      Map failures = new HashMap();
      for (int i = 0; i < count; i++)
      {
         Node el = results.item(i);
         PSXmlTreeWalker eltw = new PSXmlTreeWalker(el);

         if (el.getNodeName().equals("ConsoleOutput"))
         {
            System.out.println(eltw.getElementData());
         }
         else if (el.getNodeName().equals("TestError"))
         {
            String message = eltw.getElementData("Message");
            String trace = eltw.getElementData("Trace");
            Test test = findTest(((Element) el).getAttribute("testName"));
            AssertionFailedError ex =
               new AssertionFailedError(message + " stack " + trace);
            addToMapList(errors, test, ex);
         }
         else if (el.getNodeName().equals("TestFailure"))
         {
            String message = eltw.getElementData("Message");
            String trace = eltw.getElementData("Trace");
            Test test = findTest(((Element) el).getAttribute("testName"));
            AssertionFailedError ex =
               new AssertionFailedError(message + " stack " + trace);
            addToMapList(failures, test, ex);
         }
      }
      for(int i = 0; i < testCount(); i++)
      {
         Test test = testAt(i);
         testResults.startTest(test);
         List elist = (List) errors.get(test);
         if (elist != null)
         {
            for (Iterator iter = elist.iterator(); iter.hasNext();)
            {
               Throwable th = (Throwable) iter.next();
               testResults.addError(test, th);
            }
         }
         List flist = (List) failures.get(test);
         if (flist != null)
         {
            for (Iterator iter = flist.iterator(); iter.hasNext();)
            {
               AssertionFailedError th = (AssertionFailedError) iter.next();
               testResults.addFailure(test, th);
            }
         }         
         testResults.endTest(test);
      }
   }

   /**
    * Adds the given information to a list on the passed map. 
    * @param map A map, assumed to be not <code>null</code>.
    * @param key A key to the map, assumed to be not <code>null</code>.
    * @param value A value to be added, assumed to be not <code>null</code>.
    */
   private void addToMapList(Map map, Object key, Object value)
   {
      List l = (List) map.get(key);
      if (l == null)
      {
         l = new ArrayList();
         map.put(key, l);
      }
      l.add(value);
   }

   /**
    * Look through the test list and return the correct test for the 
    * given name.
    * 
    * @param string A test name, must never be <code>null</code>
    * @return the given {@link Test} object, never <code>null</code>.
    * @throws Exception if test not found
    */
   private Test findTest(String string) throws Exception
   {
      for (int i = 0; i < testCount(); i++)
      {
         Test test = testAt(i);
         if (test.toString().equals(string))
         {
            return test;
         }
      }

      throw new Exception("Didn't find test " + string);
   }

   /**
    * Determine if this is being run inside or outside the server
    * @return <code>true</code> if the code is running inside of
    * the Rhythmyx server.
    */
   private boolean insideRhythmyx()
   {
      return PSServer.getLogHandler() != null;
   }

   /**
    * Record the test class to be passed to the server. Initialized
    * in the constructor and never modified afterward. Never <code>null</code>.
    */
   private Class m_testClass;
   
   /**
    * Config helper test case - a properties factory.
    */
   private PSConfigHelperTestCase m_configHelper =
      new PSConfigHelperTestCase("props");
}
