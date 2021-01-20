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
package com.percussion.content;

import com.percussion.error.PSException;

/**
 * This exception is used to propagate errors that occur during content 
 * conversion.
 */
public class PSContentConversionException extends PSException
{
   // see base class
   public PSContentConversionException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   // see base class
   public PSContentConversionException(
      String language,
      int msgCode,
      Object singleArg)
   {
      super(language, msgCode, singleArg);
   }

   // see base class
   public PSContentConversionException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   // see base class
   public PSContentConversionException(String language, int msgCode, 
      Object[] arrayArgs)
   {
      super(language, msgCode, arrayArgs);
   }

   // see base class
   public PSContentConversionException(int msgCode)
   {
      super(msgCode);
   }

   // see base class
   public PSContentConversionException(String language, int msgCode)
   {
      super(language, msgCode);
   }

}
