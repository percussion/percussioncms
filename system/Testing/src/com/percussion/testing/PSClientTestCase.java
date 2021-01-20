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
