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

package com.percussion.error;



/**
 * This interface is used to define all of our exception classes.
 */
public interface IPSException
{
   /**
    * Returns the localized detail message of this exception.
    *
    * @param   locale      the locale to generate the message in
    *
    * @return               the localized detail message
    */
   String getLocalizedMessage(java.util.Locale locale);

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage();

   /**
    * Returns the detail message of this exception.
    *
    * @return               the detail message
    */
   String getMessage();

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return   the error code
    */
   int getErrorCode();

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return   the error arguments
    */
    Object[] getErrorArguments();

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArg         the argument to use as the sole argument in
    *                           the error message
    */
    void setArgs(int msgCode, Object errorArg);

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArgs      the array of arguments to use as the arguments
    *                           in the error message
    */
    void setArgs(int msgCode, Object[] errorArgs);
}

