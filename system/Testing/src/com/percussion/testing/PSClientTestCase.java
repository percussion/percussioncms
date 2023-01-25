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

import java.io.File;

import com.percussion.utils.xml.PSEntityResolver;

/**
 * The utility class to provide the default connection properties for the 
 * remote Rhythmyx Server. This should be used by all Junit tests that are 
 * invoked as a remote client.
 */
public class PSClientTestCase extends PSConfigHelperTestCase implements
      IPSClientBasedJunitTest 
{
   /**
    * Default constructor.
    */
   public PSClientTestCase()
   {
	   File homeDir = new File(System.getProperty("rxdeploydir"));
	   
	   if(homeDir.exists()){
		   PSEntityResolver.setResolutionHome(homeDir);
	   }else{
		   homeDir = new File(System.getenv("RHYTHMYX_HOME"));
		   if(homeDir.exists()){
			   PSEntityResolver.setResolutionHome(homeDir);
		   }	   
	   }
   }
   
   /**
    * Simply call super(String).
    * 
    * @param arg0 the name of the TestCase.
    */
   public PSClientTestCase(String arg0) 
   {
      super(arg0);
   }
}
