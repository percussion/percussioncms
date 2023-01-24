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
package com.percussion.services;

import java.io.File;

/**
 * Allows the initialization servlet to store the resource directory in
 * a place the code can access
 * 
 * @author dougrand
 */
public class PSResourceHelper
{
   /**
    * Default value used in unit testing
    */
   public static File ms_resourceDir = new File("ear");
   
   public static File getResourceDir()
   {
      return ms_resourceDir;
   }
   
   public static void setResourceDir(File path)
   {
      ms_resourceDir = path;
   }
}
