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

package com.percussion.design.objectstore;

import com.percussion.error.PSException;


/**
 * PSLockedException is thrown to indicate that an object is locked.
 * This usually occurs when a request is made to lock an object which is
 * already locked by someone else.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLockedException extends PSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode       the error string to load
    *
    * @param singleArg      the argument to use as the sole argument in
    *                      the error message
    */
   public PSLockedException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode       the error string to load
    *
    * @param arrayArgs      the array of arguments to use as the arguments
    *                      in the error message
    */
   public PSLockedException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }
   
   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode       the error string to load
    */
   public PSLockedException(int msgCode)
   {
      super(msgCode);
   }

   public void setLockingUser(String user)
   {
      m_user = user;
   }

   public void setLockingSession(String sessionId)
   {
      m_sessionId = sessionId;
   }

   public void setExpirationMinutes(long minutes)
   {
      m_minutes = minutes;
   }

   public void setObjectName(String name)
   {
      m_objectName = name;
   }

   public void constructArguments()
   {
      switch (m_code)
      {
      case IPSObjectStoreErrors.LOCK_ALREADY_HELD:
         /**
          * The object was already exclusively locked by someone else.
          * <p>
          * The arguments passed in for this message are:
          * <TABLE BORDER="1">
          * <TR><TH>Arg</TH><TH>Description</TH></TR>
          * <TR><TD>0</TD><TD>The name of the locked object</TD></TR>
          * <TR><TD>1</TD><TD>The name of the user currently holding the lock</TD></TR>
          * <TR><TD>2</TD><TD>How many minutes from now the lock will expire if the
          * user does not renew it</TD></TR>
          * </TABLE>
          */   
         m_args = new Object[] { m_objectName, m_user, "" + m_minutes};
         break;
      case IPSObjectStoreErrors.LOCK_ALREADY_HELD_SAME_USER:
      default:
         /**
          * The object was already exclusively locked by the user requesting
          * the lock, but under a different user session.
          * <p>
          * The arguments passed in for this message are:
          * <TABLE BORDER="1">
          * <TR><TH>Arg</TH><TH>Description</TH></TR>
          * <TR><TD>0</TD><TD>The name of the locked object</TD></TR>
          * <TR><TD>1</TD><TD>The name of the user currently holding the lock</TD></TR>
          * <TR><TD>2</TD><TD>How many minutes from now the lock will expire if the
          * user does not renew it</TD></TR>
          * <TR><TD>3</TD><TD>The session id of the session holding the lock</TD></TR>
          * </TABLE>
          */   
         m_args = new Object[] { m_objectName, m_user, "" + m_minutes, m_sessionId };
      }
   }

   private String m_objectName;
   private String m_user;
   private String m_sessionId;
   private long m_minutes;
}

