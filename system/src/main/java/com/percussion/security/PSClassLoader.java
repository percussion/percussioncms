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

package com.percussion.security;

/**
 * The PSClassLoader class lets the sandbox allow class loading for data transformation
 * through the creation and calling of user defined functions (UDFs).
 *
 * @author      Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSClassLoader extends ClassLoader
{
   /**
    * Construct a PSClassLoader object.
    */
   PSClassLoader()
   {
      super();
   }
}
