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
