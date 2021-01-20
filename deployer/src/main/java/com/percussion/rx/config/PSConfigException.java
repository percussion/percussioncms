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
package com.percussion.rx.config;

/**
 * This is an unchecked (runtime) exception. It may be used in the Design Object 
 * configuration sub-system.
 *
 * @author YuBingChen
 */
public class PSConfigException extends RuntimeException
{
   /**
    * Constructs an exception with the specified detail message.
    * @param errorMsg the specified detail message.
    */
   public PSConfigException(String errorMsg)
   {
      super(errorMsg);
   }
   
   /**
    * Constructs an exception with the specified cause.
    * @param e the cause of the exception.
    */
   public PSConfigException(Throwable e)
   {
      super(e);
   }

   /**
    * Constructs an exception with the specified detail message and the cause.
    * @param errorMsg the specified detail message.
    * @param e the cause of the exception.
    */
   public PSConfigException(String errorMsg, Throwable e)
   {
      super(errorMsg, e);
   }
   
}
