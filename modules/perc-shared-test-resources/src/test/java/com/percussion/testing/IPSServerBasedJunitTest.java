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


/**
 * If a JUnit test must run in the same context (in the same process) as the 
 * Rhythmyx server, it should implement this interface in addition to extending 
 * the {@link junit.framework.TestCase}. This is part of a testing 
 * framework that is based on a loadable handler.
 * <p>To enable this test to still be run w/ the JUnit runner, the 
 * <code>suite</code> method should use {@link 
 * com.percussion.testing.PSRequestHandlerTestSuite PSRequestHandlerTestSuite}
 * class as the suite. This class will invoke the Rx server, asking it to
 * execute the tests remotely.
 * 
 * @see com.percussion.testing.PSJunitRequestHandler 
 * @see com.percussion.testing.PSRequestHandlerTestSuite
 *
 * @author paulhoward
 */
public interface IPSServerBasedJunitTest
{
   /**
    * The loadable handler will call this method once before any test method.
    *  
    * @param req The request that was passed to the loadable handler.
    * Never <code>null</code>;
    */
   public void oneTimeSetUp(Object req);

   /**
    * The loadable handler will call this method once after all tests have 
    * been completed.
    */   
   public void oneTimeTearDown();
}
