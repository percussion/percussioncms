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
package com.percussion.relationship;

import com.percussion.error.PSException;

/**
 * This excepton is used by ythe relationship processor to reject workflow
 * transitions within relationship effects.
 */
public class PSRejectTransition extends PSException
{
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode - the error string to load. There is no validation on this
    *    value.
    */
   public PSRejectTransition(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Construct an exception from a class derived from PSException.
    *
    * @param ex The exception to use. Its message code and arguments are
    *    stored. May not be <code>null</code>.
    */
   public PSRejectTransition(PSException ex)
   {
      this(ex.getErrorCode(), ex.getErrorArguments());
   }

   /**
    * Construct an exception for messages taking 2 specific arguments.
    *
    * @param msgCode - the error string to load. There is no validation on this
    * value.
    * @param contentTypeName Displayed in the error message. If the name is
    *    not available, the id should be used. Never <code>null</code> or
    *    empty.
    * @param contentId - the contentid on which the error occurred. May be 0.
    * @param revisionId - the revisionid on which the error occurred.  May be 0.
    */
   public PSRejectTransition(int msgCode, int contentId, int revisionId)
   {
      this(msgCode, new Integer[]
      {
         new Integer(contentId), new Integer(revisionId)
      });
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load. There is no validation on this
    *    value.
    * @param singleMessage the sole of argument to use as the arguments in the
    *    error message
    */
   public PSRejectTransition(int msgCode, String singleMessage)
   {
      super(msgCode,singleMessage);
   }
   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load. There is no validation on this
    *    value.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *    error message
    */
   public PSRejectTransition(int msgCode, Object[] arrayArgs)
   {
      super(msgCode,arrayArgs);
   }
}