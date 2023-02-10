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
package com.percussion.workflow.mail;

/**
 * Rhythmyx workflow engine uses javax mail to send mail notifications for the
 * workflow by default. However, one can use a custom mail program (a plugin)
 * instead of javax API. In that case the plugin needs implement this interface
 * to use a custom mail API. Following are the steps invloved in using custom
 * mail program for Rhythmyx workflow:
 * <ul>
 *<li>Write your own class implementing this interface.
 * e.g. com.percussion.PSMailProgramJavax</li>
 *<li>Register this class with the Rhythmyx workflow engine. This is done by
 * making an entry in rxworkflow.properties file for the variable
 * CUSTOM_MAIL_CLASS, as for example:
 * <br>...<br>
 * CUSTOM_MAIL_CLASS=com.percussion.PSMailProgramJavax
 * <br>...<br>
 * </li>
 *</ul>
 *
 */
public interface IPSMailProgram
{
   /**
    * This method can be used to initialize the mail program. Called by
    * workflow engine only once in the Object's life time.
    *
    * @throws PSMailException when initialization of the mail program fails.
    *
    */
    void init()
      throws PSMailException;

   /**
    * This is the method that must be implemented by the implementing class
    * (plugin) that should actually send the message as per the data supplied
    * via IPSMailMessageContext object.
    *
    * @param mesageContext object storing all the data required for sending the
    * mail. Never be <code>null</code>.
    *
    * @throws PSMailException when message cannnot be sent for any reason.
    *
    */
   void sendMessage(IPSMailMessageContext mesageContext)
      throws PSMailException;

   /**
    * This method can be used to cleanup the resources used by the mail program.
    * Workflow engine calls this method just before this object looses its
    * scope (before its death).
    *
    * @throws PSMailException when termination is smooth.
    *
    */
    void terminate()
      throws PSMailException;

}
