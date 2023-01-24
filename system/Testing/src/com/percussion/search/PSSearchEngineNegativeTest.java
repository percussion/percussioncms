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
package com.percussion.search;

import junit.framework.TestCase;


/**
 * This unit tests runs w/o the Rx server and attempts to do things that are
 * not allowed and verifies the proper exceptions are thrown.
 *
 * @author paulhoward
 */
public class PSSearchEngineNegativeTest extends TestCase 
{
   /**
    * Tries to get the search engine instance w/o properties.
    * 
    * @throws PSSearchException
    */
   public void testInitializeEngine()
      throws PSSearchException
   {
      try
      {
         PSSearchEngine.getInstance();
         fail("Returned engine instance w/o properties.");
      }
      catch (IllegalStateException ise)
      {
         //expected
      }
   }
}
