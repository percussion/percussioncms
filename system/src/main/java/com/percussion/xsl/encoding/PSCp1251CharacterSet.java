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
package com.percussion.xsl.encoding;

import java.io.IOException;

/**
 * Defines the Cp1251 character encoding for the Saxon XSLT processor.
 */
public class PSCp1251CharacterSet extends PSGenericCharacterSet
{
   /**
    * Initializes a newly created <code>PSCp1251CharacterSet</code> object by
    * delegating to {@link PSGenericCharacterSet#PSGenericCharacterSet(String,
    * String) <code>super("Cp1251", "java-Cp1251.xml")</code>}
    * 
    * @throws IOException if there are problems reading the resource file.
    */
   public PSCp1251CharacterSet() throws IOException
   {
      super("Cp1251", "java-Cp1251.xml");
   }
}
