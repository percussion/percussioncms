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

package com.percussion.content;

/**
 * This interface contains the error codes for all exceptions thrown by
 * classes in this pkg.
 * The search error code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>17001 - 17100</TD><TD>conversion errors</TD></TR>
 * <TR><TD>17101 - 17500</TD><TD>general errors</TD></TR>
 * </TABLE>
 */
public interface IPSContentErrors
{
   /**
    * The data supplied for conversion contains a file type that is not
    * supported for conversion. There are no args for this message.
    */
   public static final int UNSUPPORTED_FILE_TYPE = 17001;

   /**
    * The content conversion failed with no message available.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The error code from the converter</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>The file type</TD>
    * </TR>
    * </TABLE>
    */
   public static final int CONTENT_CONVERSION_FAILED_NO_MESSAGE = 17002;

   /**
    * The content conversion failed with a message available.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The error code from the converter</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>The file type</TD>
    * </TR>
    * <TR>
    * <TD>2</TD>
    * <TD>The error message</TD>
    * </TR>
    * </TABLE>
    */
   public static final int CONTENT_CONVERSION_FAILED_WITH_MESSAGE = 17003;

   /**
    * The content conversion failed due to an unexpected error.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The exception class</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>The error message</TD>
    * </TR>
    * </TABLE>
    */
   public static final int CONTENT_CONVERSION_UNEXPECTED_ERROR = 17004;

   /**
    * The content conversion cannot proceed due to an invalid search
    * configuration.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The param name</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>The value</TD>
    * </TR>
    * </TABLE>
    */
   public static final int INVALID_SEARCH_CONFIG_PARAM = 17005;

   /**
    * The content conversion failed to complete. There are no args for this
    * message.
    */
   public static final int CONTENT_CONVERSION_INCOMPLETE = 17006;

   /**
    * No system or user converter is registered for the supplied mimetype. There
    * are no args for this message.
    */
   public static final int UNSUPPORTED_MIMETYPE = 17007;

   /**
    * The convert functionality without mimetype has been dropped. Use
    * extractText method instead.
    */
   public static final int UNSUPPORTED_CONVERT_METHOD = 17008;

   /**
    * The functionality of sys_textExtraction exit has been dropped. Update your
    * content editor/ applications with new exit sys_textExtractor.
    */
   public static final int UNSUPPORTED_EXTRACTION_EXIT = 17009;

   /**
    * The constructor with signature PSContentConverter(int , boolean , String ,
    * String , int) has been dropped, instead use PSContentConverter(String).
    */
   public static final int UNSUPPORTED_CONVERT_CONSTRUCTOR = 17010;
}
