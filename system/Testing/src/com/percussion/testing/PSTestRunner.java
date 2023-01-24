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
