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
package com.percussion.services.filter;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Exception for problems in filter rules
 * 
 * @see IPSFilterServiceErrors for message codes, and the corresponding
 * property bundle for the messages. Each message code documents what, if any,
 * arguments need to be passed.
 * 
 * @author dougrand
 */
public class PSFilterException extends PSBaseException
{

   /**
    * Serial ids are required for objects that implement java.io.Serializable
    */
   private static final long serialVersionUID = -1763123318413410377L;

   /**
    * Ctor
    * @param msgCode the message code for the exception
    * @param arrayArgs the arguments for the exception
    */
   public PSFilterException(int msgCode, Object... arrayArgs) {
      super(msgCode, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode the message code for the exception
    * @param cause the original cause
    * @param arrayArgs the arguments for the exception
    */
   public PSFilterException(int msgCode, Throwable cause, Object... arrayArgs) {
      super(msgCode, cause, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode the message code for the exception
    */
   public PSFilterException(int msgCode) {
      super(msgCode);
   }

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.filter.PSFilterErrorStringBundle";
   }


}
