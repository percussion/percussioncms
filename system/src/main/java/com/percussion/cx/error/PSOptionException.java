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
package com.percussion.cx.error;

import com.percussion.error.PSStandaloneException;

/**
 * This class is used when an error occurs during PSOption handling (loading,
 * persisting, creating etc).
 */
public class PSOptionException extends PSStandaloneException
{

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    */
   public PSOptionException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param singleMessage the sole of argument to use as the arguments in the
    *    error message
    */
   public PSOptionException(int msgCode, String singleMessage)
   {
      super(msgCode,singleMessage);
   }
   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *    error message
    */
   public PSOptionException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode,arrayArgs);
   }

   /**
    * @see #com.percussion.error.PSStandaloneException PSStandaloneException
    */
   protected String getResourceBundleBaseName()
   {
      return getClass().getPackage().getName() + "." + STRING_BUNDLE_NAME;
   }

   /**
    * See {@link PSStandaloneException#getXmlNodeName()}
    */
   protected String getXmlNodeName()
   {
      return "PSXOptionException";
   }

   /**
    * The String bundle used with this Exception.
    */
   public static final String STRING_BUNDLE_NAME =
      "PSContentExplorerErrorStringBundle.properties";
}


