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

package com.percussion.debug;

import java.text.MessageFormat;

/**
 * Used to generate trace messages for the Conditional Evaluation information trace message 
 * type (0x10000).  Includes any conditional evaluation for selection (i.e. resource selection, mapper, etc.).  Prints out each operand, the operator, and result for every condition checked.
 */
public class PSTraceConditionalEval extends PSTraceMessage
{

   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 3A0323670232
    */
   public PSTraceConditionalEval(int typeFlag)
   {
      super(typeFlag);
   }
   
   /**
    * Formats the output for the body of the message, extracting the information 
    * required from the source object.
    * 
    * @param source an array of objects containing the information required for the 
    * trace message.  One of two cases:
    * - Op1, OpCode, Op2, Result
    * - Op1, OpCode, Result (unary operator)
    * @return the message body
    * @roseuid 3A03237F008C
    */
   protected String getMessageBody(java.lang.Object source)
   {
      Object[] args = (Object[])source;
      String msg;

      if (args.length == 4)
         msg = "traceConditionalEval_binary";
      else if (args.length == 3)
         msg = "traceConditionalEval_unary";
      else
         throw new IllegalArgumentException("Invalid source args");

      return MessageFormat.format(ms_bundle.getString(msg), args);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceConditionalEval_dispname");
   }
}
