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
package com.percussion.util;

import com.percussion.utils.tools.IPSUtilsConstants;

/**
 * Holds the server-independent methods that were once in PSCharSets
 */
public class PSCharSetsConstants
{
   /**
    * Get the standard name of the preferred encoding for
    * Rhythmyx. This encoding is guaranteed to be acceptable for
    * XML parsers and HTTP servers, and should be some kind of Unicode
    * so that we can be sure all characters are representable.
    * @return valid String of the standard name for {@link #rxJavaEnc}
    */
   public static String rxStdEnc()
   {
      return IPSUtilsConstants.RX_STANDARD_ENC;
   }


   /**
    * Get the standard name of the preferred encoding for
    * Rhythmyx. This encoding is guaranteed to be acceptable for
    * Sun's Java methods which take a character encoding, and should be
    * some kind of Unicode so that we can be sure all characters are
    * representable.
    * @return valid String of the Java name for {@link #rxStdEnc}
    */
   public static String rxJavaEnc()
   {
      return IPSUtilsConstants.RX_JAVA_ENC;
   }
}
