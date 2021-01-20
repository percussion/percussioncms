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

package com.percussion.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/**
 * This class is intented to be used as a standalone program that will run a
 * suite of junit tests, print the results, and return <code>0</code> if there
 * were no errors, and <code>1</code> if there were errors.
 */
public class PSTestRunner
{

   /**
    * Runs the specified test suite, returning <code>0</code> if no errors are
    * encountered, and <code>1</code> if there are errors.  All errors are
    * logged to the console.
    *
    * @param args The commandline arguments.  A single argument is expected,
    * the fully qualified class name of the suite to run.  Must specify a class
    * that implements a method matching the following interface:
    * <p><code>
    * public static Test suite();
    * </code><p>
    * which returns the an object implementing the <code>Test</code> interface
    * and defines the test or tests that will be run (typically it returns a
    * <code>TestSuite</code> object).
    */
   public static void main(String[] args)
   {

      try
      {
         if (args.length != 1)
         {
            System.out.println("Usage:");
            System.out.println(
               "java com.percussion.testing.PSTestRunner <suite>");
            System.out.println(
               "where <suite> is the name of the class containing the suite ");
            System.out.println("method to execute.");
            System.exit(1);
         }

         String testCase = args[0];

         Class testClass = null;
         Method suiteMethod = null;
         Test suite = null;

         // load the specified class
         try
         {
            testClass= Class.forName(testCase);
         }
         catch(Exception e)
         {
            System.out.println("Suite class \"" + testCase + "\" not found");
            System.exit(1);
         }

         // get the suite() method
         try
         {
            suiteMethod = testClass.getMethod("suite", new Class[0]);
         }
         catch(Exception e)
         {
            System.out.println(
               "The suite class should have a method named \"suite()\"");
            System.exit(1);
         }

         // invoke the suite method to get the Test to run
         try
         {
            // static method, supply null for the instance object
            suite = (Test)suiteMethod.invoke(null, (Object[]) new Class[0]);
         }
         catch(Exception e)
         {
            System.out.println("Could not invoke the suite() method");
            System.exit(1);
         }

         // we have the test, so now run it and return the result
         System.out.println("Running test suite defined by " + testCase);
         int result = run(suite);
         System.exit(result);
      }
      catch(Exception e)
      {
         System.out.println("Could not create and run test suite");
         System.exit(1);
      }
   }

   /**
    * Runs a single test, collects its results, and prints them to the console.
    * Any console output by the test is supressed.
    *
    * @param suite The test to run, may not be <code>null</code>.
    *
    * @return zero if the test results do not contain any errors, <code>1
    * </code> if the test results contain errors.
    *
    * @throws IllegalArgumentException if suite is <code>null</code>.
    */
   public static int run(Test suite)
   {
      if (suite == null)
         throw new IllegalArgumentException("suite may not be null");

      /* replace std out with a bytearray that we will just throw away as we
       * don't want the output from the individual tests.
       */
      PrintStream stdOut = System.out;
      PrintStream stdErr = System.err;
      System.setOut(new PrintStream(new ByteArrayOutputStream()));
      System.setErr(new PrintStream(new ByteArrayOutputStream()));

      long startTime;
      long endTime;
      long runTime;
      TestResult result = new TestResult();

      try
      {
         startTime = System.currentTimeMillis();
         suite.run(result);
         endTime = System.currentTimeMillis();
         runTime = endTime - startTime;

      }
      finally
      {
         // want to be sure to reset system.out and err if there is an error
         System.setOut(stdOut);
         System.setErr(stdErr);
      }

      // print out result summary
      System.out.println("Results:");
      System.out.println("# Tests: " + suite.countTestCases());
      System.out.println("Total Time: " + runTime/1000 + "." + runTime%1000);

      int numFailures = result.failureCount();
      int numErrors = result.errorCount();
      System.out.println("# Failures: " + numFailures);
      System.out.println("# Errors: " + result.errorCount());

      // print out failures and errors
      if (numFailures > 0)
      {
         System.out.println();
         System.out.println("Failure detail:");
         Enumeration e = result.failures();
         while (e.hasMoreElements())
         {
            TestFailure failure = (TestFailure)e.nextElement();
            System.out.println(failure.toString());
         }
      }

      if (numErrors > 0)
      {
         System.out.println();
         System.out.println("Error detail:");
         Enumeration e = result.errors();
         while (e.hasMoreElements())
         {
            TestFailure error = (TestFailure)e.nextElement();
            System.out.println(error.toString());
         }
      }

      return result.wasSuccessful() ? 0 : 1;
   }
}
