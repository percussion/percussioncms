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
package com.percussion.extension;

import com.percussion.error.PSException;

/**
 * PSParameterMismatchException is thrown to indicate that an extension
 * or extension was initialized with the incorrect parameter values.
 * <P>
 * Extensions provide their parameter definitions through the
 * {@link IPSExtensionDef#getRuntimeParameter IPSExtensionDef.getRuntimeParameter}
 * method. The corrsponding run-time values must then be set via an
 * unspecified mechanism (usually a sub-interface will define how params are to
 * be set).
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSParameterMismatchException extends PSException
{
   /**
    * Constructs a parameter mismatch exception when an incorrect number
    * of values was specified.
    *
    * @param expected The number of parameters expected by the extension.
    *
    * @param actual The number of values sent to the extension.
    */
   public PSParameterMismatchException(int expected, int actual)
   {
      super(IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH,
      new Object[] { new Integer(expected), new Integer(actual) });
   }
   
   /**
    * Constructs a parameter mismatch exception for the provided message.
    *
    * @param message the complete display message.
    */
   public PSParameterMismatchException(String message)
   {
      super(IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, message);
   }

   /**
    * Constructs a parameter mismatch exception when an incorrect number
    * of values was specified.
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    * @param expected The number of parameters expected by the extension.
    * @param actual The number of values sent to the extension.
    */
   public PSParameterMismatchException(String language, int expected, int actual)
   {
      super(language, IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH,
      new Object[] { new Integer(expected), new Integer(actual) });
   }

   /**
    * Constructs a parameter mismatch exception for the provided message.
    *
    * @param language          language string to use while lookingup for the
    * message text in the resource bundle
    *
    * @param message the complete display message.
    */
   public PSParameterMismatchException(String language, String message)
   {
      super(language, IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, message);
   }

   /**
    * See {@link com.percussion.error.PSException#PSException(int, Object)} 
    * for documentation.
    */
   public PSParameterMismatchException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * See {@link com.percussion.error.PSException#PSException(int, Object)} 
    * for documentation.
    */
   public PSParameterMismatchException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
}
