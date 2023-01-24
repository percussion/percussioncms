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
package com.percussion.error;

/**
 * This inteface is provided as a convenient mechanism for accessing the
 * various beans related error codes. The error code messages are defined in the
 * PSBeansStringBundle.properties file.  This was created so that it would be
 * completely independent of the other elements in the system and can stand
 * on its own.  There should not be many messages herein.
 *
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>1001 - 2000</TD><TD>MISC- Miscellaneous</TD></TR>
 * </TABLE>
 */
public interface IPSBeansErrors
{
   /**
    * An exception occurred while processing xml.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught,</TD></TR>
    * </TABLE>
    */
   public static final int XML_PROCESSING_ERROR = 1001;


}
