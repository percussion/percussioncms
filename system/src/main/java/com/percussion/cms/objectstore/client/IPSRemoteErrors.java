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

/**
 * Error codes for the <code>PSRemoteAgent</code> class.
 */
public interface IPSRemoteErrors
{
   /**
    * Got wrong soap response while communicating with remote server
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>expected response element</TD></TR>
    * <TR><TD>1</TD><TD>the unexpected soap response (error)</TD></TR>
    * </TABLE>
    */
   public static final int REMOTE_WRONG_SOAP_RESP = 15001;

   /**
    * Caught unexpected error while communicating with remote server
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>unexpected error</TD></TR>
    * </TABLE>
    */
   public static final int REMOTE_UNEXPECTED_ERROR = 15002;

}
