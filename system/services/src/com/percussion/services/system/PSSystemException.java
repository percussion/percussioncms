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
package com.percussion.services.system;

import com.percussion.utils.exceptions.PSBaseException;

public class PSSystemException extends PSBaseException
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -3490163137874339586L;

   /* (non-Javadoc)
    * @see PSBaseException#PSBaseException(int)
    */
   public PSSystemException(int msgCode)
   {
      super(msgCode);
   }

   /* (non-Javadoc)
    * @see PSBaseException#PSBaseException(int, Object...)
    */
   public PSSystemException(int msgCode, Object... arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /* (non-Javadoc)
    * @see PSBaseException#PSBaseException(int, throwable, Object...)
    */
   public PSSystemException(int msgCode, Throwable cause, Object... arrayArgs)
   {
      super(msgCode, cause, arrayArgs);
   }

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.system.PSSystemErrorStringBundle";
   }
}

