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
package com.percussion.services.schedule;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Is thrown on scheduler service errors.
 *
 * @author Andriy Palamarchuk
 */
public class PSSchedulingException extends PSBaseException
{
   /**
    * Creates a new exception.
    * @param msgCode the message code.
    * @param args error message arguments.
    */
   public PSSchedulingException(int msgCode, Object... args)
   {
      super(msgCode, args);
   }

   /**
    * Creates a new exception.
    * @param msgCode the message code.
    */
   public PSSchedulingException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * Creates a new exception.
    * @param msgCode the message code.
    * @param cause the underlying. Not <code>null</code>.
    * @param args error message arguments.
    */
   public PSSchedulingException(int msgCode, Throwable cause, Object... args)
   {
      super(msgCode, cause, args);
   }

   // see base
   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.schedule.PSSchedulingErrorStringBundle";
   }

   /**
    * Error messages codes.
    * The error messages are stored in the resource bundle indicated by
    * {@link PSSchedulingException#getResourceBundleBaseName()}.
    */
   public static enum Error
   {
      /**
       * A cron expression parsing error.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>The original exception message.</TD></TR>
       * </TABLE>
       */
      SCHEDULER,

      /**
       * A cron expression parsing error.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>The cron expression.</TD></TR>
       * <TR><TD>1</TD><TD>The original exception message.</TD></TR>
       * </TABLE>
       */
      CRON_FORMAT,

      /**
       * Found a job object, but this object does not contain a job schedule.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Schedule id.</TD></TR>
       * </TABLE>
       */
      JOB_WITHOUT_SCHEDULE,
      
      /**
       * Failed to run an Edition due to an exception.
       * <p>
       */
      FAILED_RUN_EDITION,

      /**
       * Failed to run a specified Edition due to an exception.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Edition ID</TD></TR>
       * <TR><TD>1</TD><TD>Edition name</TD></TR>
       * <TR><TD>2</TD><TD>Underline error message</TD></TR>
       * </TABLE>
       */
      FAILED_RUN_SPECIFIED_EDITION,

      /**
       * Failed to run a specified command.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Command</TD></TR>
       * </TABLE>
       */
      FAILED_RUN_COMMAND,

      /**
       * Failed to run a specified command with standard error text.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Command</TD></TR>
       * <TR><TD>1</TD><TD>standard error text</TD></TR>
       * </TABLE>
       */
      FAILED_RUN_COMMAND_WITH_STDERROR,

      /**
       * Edition canceled by user.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Edition ID</TD></TR>
       * <TR><TD>1</TD><TD>Edition name</TD></TR>
       * </TABLE>
       */
      EDITION_CANCELED_BY_USER,

      /**
       * Edition terminated abnormally.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Edition ID</TD></TR>
       * <TR><TD>1</TD><TD>Edition name</TD></TR>
       * </TABLE>
       */
      EDITION_ABORTED,

      /**
       * Skip firing a scheduled task because it is currently running.
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Task ID</TD></TR>
       * <TR><TD>1</TD><TD>Task name</TD></TR>
       * </TABLE>
       */
      SKIP_FIRE_SCHEDULED_TASK,

      /**
       * Skip executing a scheduled task because it is not registered for the
       * current server
       * <p>
       * <TABLE BORDER="1">
       * <TR><TH>Argument</TH><TH>Description</TH></TR>
       * <TR><TD>0</TD><TD>Task ID</TD></TR>
       * <TR><TD>1</TD><TD>Task name</TD></TR>
       * <TR><TD>2</TD><TD>Registered server name or IP</TD></TR>
       * <TR><TD>3</TD><TD>Current server (short) name</TD></TR>
       * <TR><TD>4</TD><TD>Current server (long) name</TD></TR>
       * <TR><TD>5</TD><TD>Current server IP address</TD></TR>
       * <TR><TD>6</TD><TD>HTTP port of the current server</TD></TR>
       * <TR><TD>7</TD><TD>HTTPS port of the current server</TD></TR>
       * </TABLE>
       */
      SKIP_EXE_TASK_NOT_REG_SERVER,
}
   /**
    * Class version id for serialization. 
    */
   private static final long serialVersionUID = 1;
}
