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
package com.percussion.cms.objectstore.client;

import com.percussion.error.PSException;



/**
 * Exceptions of this type will be thrown from the Remote Agent
 */
public class PSRemoteException extends PSException
{
   /*
    * @see {@link com.percussion.error.PSException(int, Object)}
    */
   public PSRemoteException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /*
    * @see {@link com.percussion.error.PSException(int, Object[])}
    */
   public PSRemoteException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /*
    * @see {@link com.percussion.error.PSException(int)}
    */
   public PSRemoteException(int msgCode)
   {
      super(msgCode);
   }
   
   /**
    * Construct an exception from a class derived from PSException.  The name of
    * the original exception class is saved.
    *
    * @param ex The exception to use.  Its message code and arguments are stored
    * along with the original exception class name.  May not be
    * <code>null</code>.
    */
   public PSRemoteException(PSException ex)
   {
      super(ex);
   }
}
