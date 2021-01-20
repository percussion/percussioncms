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
package com.percussion.services.assembly;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Generic problem in assembly, the message will indicate what issue occurred.
 * 
 * @author dougrand
 */
public class PSAssemblyException extends PSBaseException
{
   /**
    * 
    */
   private static final long serialVersionUID = 3256726182123680309L;

   /**
    * Ctor
    * @param msgCode message code used to lookup message
    * @param arrayArgs arguments for message, may be <code>null</code>
    */
   public PSAssemblyException(int msgCode, Object... arrayArgs) {
      super(msgCode, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode message code used to lookup message
    * @param cause original exception, may be <code>null</code>
    * @param arrayArgs arguments for message, may be <code>null</code>
    */
   public PSAssemblyException(int msgCode, Throwable cause, Object... arrayArgs) {
      super(msgCode, cause, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode message code used to lookup message
    */
   public PSAssemblyException(int msgCode) {
      super(msgCode);
   }

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.assembly.PSAssemblyErrorStringBundle";
   }



}
