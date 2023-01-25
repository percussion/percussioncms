/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
