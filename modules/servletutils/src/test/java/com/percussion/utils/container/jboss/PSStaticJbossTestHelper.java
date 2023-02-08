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

package com.percussion.utils.container.jboss;

import java.io.File;

import com.percussion.utils.container.PSContainerUtilsFactory;

import static com.percussion.util.PSResourceUtils.getResourcePath;

public class PSStaticJbossTestHelper
{
   
   public static void setJettyContainerType()
   {

   }

 
   public static File getPropertiesFile(String name)
   {
      File f = new File(getTestRootDir(), name);
      return f;
   }

   public static File getTestRootDir()
   {

      File f = new File(getResourcePath( PSStaticJbossTestHelper.class,"/com/percussion/utils/jboss"));
      return f;
   }

   public static File getTomcatTestDir()
   {
      File f = new File(getResourcePath(PSStaticJbossTestHelper.class,"/com/percussion/utils/tomcat"));
      return f;
   }
   public static File  getSpringConfigDir(){
      File f = new File(getResourcePath(PSStaticJbossTestHelper.class,"/com/percussion/design/objectstore/legacy"));
      return f;
   }

}
