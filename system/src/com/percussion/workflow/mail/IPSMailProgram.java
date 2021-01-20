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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
