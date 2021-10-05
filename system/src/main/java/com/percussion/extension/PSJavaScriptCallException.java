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
package com.percussion.extension;

import com.percussion.error.PSException;

/**
 * PSJavaScriptCallException is thrown when an error occurs in the native
 * JavaScriptCall handler.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSJavaScriptCallException extends PSException
{
   /**
    * Constructs a failure with the specified message.
    */
   public PSJavaScriptCallException(
      String function, String message)
   {
      super(IPSExtensionErrors.JS_CALL_FAILED,
         new Object[] { function, message } );
   }

   /**
    * Constructs a failure with the specified context information.
    */
   public PSJavaScriptCallException(
      String function, String message, String source)
   {
      super(IPSExtensionErrors.JS_CALL_FAILED_SRC,
         new Object[] { function, message, source } );
   }
}

