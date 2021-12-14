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

package com.percussion.server.job;

/**
 * The IPSJobErrors interface is provided as a convenient mechanism
 * for accessing the various deployent related error codes.
 */
public interface IPSJobErrors
{
   /**
    * The specified job definition cannot be located.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The category</TD></TR>
    * <TR><TD>1</TD><TD>The job type</TD></TR>
    * </TABLE>
    */
   public static final int JOB_DEFINITION_NOT_FOUND = 1;
   
   /**
    * Error creating job runner in factory.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The class name</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int FACTORY_GET_RUNNER = 2;

   /**   
    * The server received an invalid request type.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REQUEST_TYPE = 3;
   
   /**
    * Unexpected error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_ERROR = 4;
   
   /**
    * The job handler received a request with a null input document.
    * <p>
    * There are no arguments for this message.
    */
   public static final int NULL_INPUT_DOC = 5;
   
   /**
    * The request recieved by the job handler contained an invalid required 
    * parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The param name</TD></TR>
    * <TR><TD>1</TD><TD>The param value</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_REQUEST_PARAM_INVALID = 6;
   
   /**
    * The Job handler received a request to run a job while another job is still
    * running.
    * <p>
    * There are no arguments for this message.
    */
   public static final int JOB_ALREADY_RUNNING = 7;
   
   /**
    * The request received by the job handler is malformed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request element</TD></TR>
    * <TR><TD>1</TD><TD>The error mesage</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_REQUEST_MALFORMED = 8;
   
   /**
    * The request received by the job handler specifies an invalid job id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The job id</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_JOB_ID = 9;
   
   
   /**
    * The run job request received by the job handler contains an invalid 
    * descriptor document.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_JOB_DESCRIPTOR = 10;
   
   /**
    * The configuration file specified was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Config file path</TD></TR>
    * </TABLE>
    */
   public static final int CONFIG_FILE_NOT_FOUND = 11;
   
   
}
