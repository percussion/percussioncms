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

import com.icl.saxon.charcode.PluggableCharacterSet;

/**
 * Defines the UTF-16BE (big endian) character encoding for the Saxon XSLT 
 * processor.
 */
public final class PSUTF16BECharacterSet implements PluggableCharacterSet
{
   /**
    * Gets Java's name for the character encoding supported by this class.
    * 
    * @return <code>"UnicodeBig"</code>
    */ 
   public String getEncodingName()
   {
      return "UnicodeBig";
   }


   /**
    * Every Unicode character can be represented in this encoding, so this
    * method always returns <code>true</code>.
    * 
    * @param i the Unicode of the character to be tested
    * @return <code>true</code> unless the character is one half of a surrogate
    * pair.
    */ 
   public boolean inCharset(int i)
   {
      // return true unless the character is one half of a surrogate pair 
      // (D800 to DFFF)
      return (i < 55296 || i > 57343);
   }
}
