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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.xsl.encoding;

import com.icl.saxon.charcode.PluggableCharacterSet;

/**
 * Defines the UTF-16LE (little endian) character encoding for the Saxon XSLT 
 * processor.
 */
public final class PSUTF16LECharacterSet implements PluggableCharacterSet
{
   /**
    * Gets Java's name for the character encoding supported by this class.
    * 
    * @return <code>"UnicodeLittle"</code>
    */ 
   public String getEncodingName()
   {
      return "UnicodeLittle";
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
