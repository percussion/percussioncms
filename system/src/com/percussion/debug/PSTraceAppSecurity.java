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

import com.percussion.design.objectstore.PSAclEntry;

import java.text.MessageFormat;

/**
 * Used to generate trace messages for the Application Security trace message type (0x0010).  Includes the ACL entry name, the access allowed, if the user was required to authenticate, and if so, the User ID(s), and Security provider(s)/instances(s) used for authentication.
 */
public class PSTraceAppSecurity extends PSTraceMessage
{
   
   /**
    * The constructor for this class.
    * 
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD14701F4
    */
   public PSTraceAppSecurity(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceAppSecurity_dispname");
   }
   
   /**
    * Formats the output for the body of the message, extracting the information 
    * required from the source object.
    * 
    * @param source the source of the information to be used in generating the trace
    * message.   May not be <code>null</code>. Object is an Object array with
    * the following objects:
    * - Integer: the access level required
    * - Integer: the User access level
    * @return the message body as a String
    * @roseuid 39FEE2F300BB
    */
   protected String getMessageBody(java.lang.Object source)
   {
      // validate and retrive args
      Object[] args = (Object[])source;

      //FB: RpC_REPEATED_CONDITIONAL_TEST NC 1-17-16
      if ((args.length < 2) || !(args[0] instanceof Integer) ||
         !(args[1] instanceof Integer))
         throw new IllegalArgumentException("invalid source args");

      int reqLevel = ((Integer)args[0]).intValue();
      int userLevel = ((Integer)args[1]).intValue();

      // determine if user has access
      boolean hasAccess = ((reqLevel & userLevel) == reqLevel);

      // get the string for the access required
      String reqAccess = null;
      switch(reqLevel)
      {
         case PSAclEntry.AACE_DATA_CREATE:
            reqAccess = ms_bundle.getString("traceAppSecurity_DataCreate");
            break;

         case PSAclEntry.AACE_DATA_DELETE:
            reqAccess = ms_bundle.getString("traceAppSecurity_DataDelete");
            break;

         case PSAclEntry.AACE_DATA_QUERY:
            reqAccess = ms_bundle.getString("traceAppSecurity_DataQuery");
            break;

         case PSAclEntry.AACE_DATA_UPDATE:
            reqAccess = ms_bundle.getString("traceAppSecurity_DataUpdate");
            break;
            
         default:
            throw new IllegalArgumentException("Unknown Access Level supplied");
      }

      // now return the final message
      String msg = (hasAccess ? "traceAppSecurity_AccessLevelYes" :
                     "traceAppSecurity_AccessLevelNo");
      Object[] formatArgs = {reqAccess};

      return MessageFormat.format(ms_bundle.getString(msg), formatArgs);
   }
}
