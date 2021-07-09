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

package com.percussion.services.security.loginmods;

import com.percussion.services.security.PSJaasUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * A login module that delegates authentication to Rhythmyx security 
 * providers.
 */
public class PSRxLoginModule implements LoginModule
{

   private static final Logger log = LogManager.getLogger(PSRxLoginModule.class);

   /**
    * Default constructor for this module
    */
   public PSRxLoginModule()
   {

   }

   // see base class
   public void initialize(Subject subject, CallbackHandler handler,
      Map sharedState, Map options)
   {
      m_subject = subject;
      m_callbackHandler = handler;
    
      if (sharedState == null);
      if (options == null);
      
      NameCallback ncb = new NameCallback("Name");
      PasswordCallback pwcb = new PasswordCallback("Password", false);

      try
      {
         handler.handle(new Callback[]
         {ncb, pwcb});
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         return;
      }

      m_username = ncb.getName();
      m_password = new String(pwcb.getPassword());      
   }

   /**
    * Attempts to authenticate the subject supplied to
    * {@link #initialize(Subject, CallbackHandler, Map, Map) initialize()} 
    * with each of the Rhythmyx security providers configured.  The subject is
    * considered to be authenticated after the first successful attempt, and the
    * principal and attribute information returned by that security provider is
    * set on the subject when {@link #commit()} is called.
    * 
    * @see javax.security.auth.spi.LoginModule#login() for other details. 
    */
   public boolean login() throws LoginException
   {
      try
      {
         IPSLoginMgr mgr = PSLoginMgrLocator.getLoginManager();
         m_authSubject = mgr.login(m_username, m_password, m_callbackHandler);

         return (m_authSubject != null);
      }
      catch (Exception e)
      {
         throw new LoginException(e.getLocalizedMessage());
      }
   }

   /**
    * If the user was successfully authenticated by this module, copy the
    * principals returned by login() to the subject supplied during inittialize.
    * Regardless of whether the subject was authenticated by this module, roles
    * are added to the subject. If that subject has had roles set on it, only
    * roles defined in the Rhythmyx backend are added, otherwise roles are added
    * from all catalogers.  Finally all saved state is destroyed.
    * 
    * @see javax.security.auth.spi.LoginModule#commit() for other details
    */   
   public boolean commit() throws LoginException
   {
      if (m_authSubject != null)
      {
         m_subject.getPrincipals().addAll(m_authSubject.getPrincipals());
         m_subject.getPrivateCredentials().addAll(
            m_authSubject.getPrivateCredentials());
         m_subject.getPublicCredentials().addAll(
            m_authSubject.getPublicCredentials());
      }
      
      PSJaasUtils.loadSubjectRoles(m_subject, m_username);
      
      return (m_authSubject != null);
   }

   /**
    * Destroy any saved state and return <code>true</code>.
    * 
    * @see javax.security.auth.spi.LoginModule#abort()     
    */
   public boolean abort() throws LoginException
   {
      // On abort, do nothing
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.security.auth.spi.LoginModule#logout()
    */
   public boolean logout() throws LoginException
   {
      try
      {
         IPSLoginMgr mgr = PSLoginMgrLocator.getLoginManager();
         return mgr.logout(m_username);
      }
      catch (Exception e)
      {
         throw new LoginException(e.getLocalizedMessage());
      }
   }

   /**
    * The subject being processed by this instance of the module, provided
    * during {@link #initialize(Subject, CallbackHandler, Map, Map)}.
    */
   private Subject m_subject;
   

   /**
    * The subject returned by the call to login, may be <code>null</code> if 
    * authentication did not succeed.
    */
   private Subject m_authSubject = null;   
   
   /**
    * Username information from the callback handler is stored here
    */
   private String m_username;

   /**
    * Password information from the callback handler is stored here
    */
   private String m_password;   
   
   /**
    * The callback handler provided during {@link #initialize(Subject, 
    * CallbackHandler, Map, Map)}, immutable after that.
    */
   private CallbackHandler m_callbackHandler;
}
