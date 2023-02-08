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


package com.percussion.util;


/**
 * The IPSUtilErrors inteface is provided as a convenient mechanism
 * for accessing the various util related error codes. The util error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>10001 - 10050</TD><TD>encode/decoder class errors</TD></TR>
 * <TR><TD>10051 - 10100</TD><TD>collection errors</TD></TR>
 * <TR><TD>10101 - 10150</TD><TD>file support class errors</TD></TR>
 * <TR><TD>10151 - 11000</TD><TD>-not assigned-</TD></TR>
 * </TABLE>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSUtilErrors
{
   /**
    * The Base64 encoding processes encountered an unexpected exception
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>input string</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int BASE64_ENCODING_EXCEPTION   = 10001;

   /**
    * The Base64 decoding processes encountered an unexpected exception
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>input string</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int BASE64_DECODING_EXCEPTION   = 10002;

   /**
    * The class to use as the collection content was not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class name</TD></TR>
    * </TABLE>
    */
   public static final int COLLECTION_CLASS_NOT_FOUND   = 10051;

   /**
    * a file exists where we expected the purgable temp directory to be
    * <p>
    * No arguments.
    */
   public static final int PURGABLE_TEMP_DIR_IS_FILE   = 10101;

   /**
    * Received less data than expected when get response from server.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The received number of bytes</TD></TR>
    * <TR><TD>1</TD><TD>The expected number of bytes</TD></TR>
    * </TABLE>
    */
   public static final int RECEIVE_DATA_ERROR = 10202;
   
   /**
    * Fail to post data through HTTP protocol.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>received error code</TD></TR>
    * <TR><TD>1</TD><TD>received error message</TD></TR>
    * </TABLE>
    */
   public static final int POST_DATA_ERROR = 10203;
}
