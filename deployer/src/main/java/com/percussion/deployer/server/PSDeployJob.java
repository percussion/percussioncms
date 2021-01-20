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
package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSUserEntry;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSessionManager;
import com.percussion.server.job.PSJobException;
import com.percussion.server.job.PSJobRunner;

import java.util.Iterator;
import java.util.Properties;

/**
 * Base class for all deployment jobs.
 */
public abstract class PSDeployJob extends PSJobRunner implements IPSJobHandle
{
   /**
    * Validates that the user is authorized to perform this job.  Saves the 
    * security token from the request to use for subsequent operations during 
    * the run method.
    * @param id The id used to identify this job. Must be used to then call
    * {@link #setId(int)} to set the job id.
    * @param req The request used to determine the current user's security 
    * permissions.  May not be <code>null</code>.
    * @param initParams Set of name value pairs that this job may require, may 
    * be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException if the user cannot be 
    * authenticated.
    * @throws PSAuthorizationException if user is not authorized to run this 
    * job.
    * @throws PSJobException for any other errors.
    */
   public void init(int id, PSRequest req, Properties initParams) 
      throws PSAuthenticationFailedException, PSAuthorizationException, 
         PSJobException
   {
         
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      m_request = req;
      m_id = id;
      
      // must have server admin access, and be in Admin role
      PSServer.checkAccessLevel(req, PSAclEntry.SACE_ADMINISTER_SERVER);
      
      // check authorization if a role is defined
      if (initParams != null)
      {
         String role = initParams.getProperty(ROLE_PARAM_NAME);
         if (role != null && role.trim().length() > 0 && 
            !req.isUserInRole(role))
         {
            throw new PSAuthorizationException("Deployment", getJobType(), 
               req.getUserSessionId());
         }
      }
      
      initUserInfo(req);
   }

    protected void initUserInfo(PSRequest req)
    {
        m_securityToken = req.getSecurityToken();
          PSUserEntry[] userEntries = 
             PSUserSessionManager.getUserSession(req).getAuthenticatedUserEntries();
          if (userEntries.length > 0)
          {
             // take first user entry name
             m_userId = userEntries[0].getName();
          }
          else
             m_userId = "unknown";
          
          m_userInfoInited = true;
    }
   
   /**
    * See {@link IPSJobHandle#updateStatus(String)} for method param and 
    * exception info.
    * 
    * @throws IllegalStateException if {@link #initDepCount(Iterator)} has not
    * been called.
    */
   public void updateStatus(String message)
   {
      if (message == null || message.trim().length() == 0)
         throw new IllegalArgumentException("message may not be null or empty");
      
      if (m_depTotal == -1)
         throw new IllegalStateException("initDepCount() has not been called");
      
      if (m_curDepCount < m_depTotal) // don't want to go over, shouldn't happen
         m_curDepCount++;
      
      int status = (m_curDepCount * 100) / m_depTotal;
      if (status == 0)
         status = 1; // never 0
      else if (status == 100)
         status = 99; // let job decide when it's finished
      setStatus(status);
      setStatusMessage(message);
   }
   
   /**
    * Walks the supplied packages and initializes the total number of 
    * dependencies that will be processed by this job.
    * 
    * @param pkgs An iterator over one or more <code>PSDependency</code>
    * objects to use to determine the count, may not be <code>null</code>.
    * 
    */
   protected void initDepCount(Iterator pkgs)
   {
     initDepCount(pkgs, true);         
   }
   
   /**
    * Walks the supplied packages and initializes the total number of 
    * dependencies that will be processed by this job.
    * 
    * @param pkgs An iterator over one or more <code>PSDependency</code>
    * objects to use to determine the count, may not be <code>null</code>.
    * @param includedOnly If <code>true</code>, only included dependencies will
    * be counted, otherwise all depedencies will be counted.
    *  
    */
   protected void initDepCount(Iterator pkgs, boolean includedOnly)
   {         
      int count = 0;
      while (pkgs.hasNext())
      {
         PSDependency dep = (PSDependency)pkgs.next();
         int childCount = dep.getChildCount(includedOnly);
         if (childCount == 0)
         {
            String included = includedOnly ? "included " : "";
            throw new IllegalArgumentException("pkg " + dep.getKey() + 
               " has no " + included + "children");
         }
         count += childCount;
      }
      m_depTotal = count;
   }
   
   /**
    * Get the security token extracted from the request supplied to the call to
    * <code>init()</code>.
    * 
    * @return The token, never <code>null</code>.
    * 
    * @throws IllegalStateException If <code>init()</code> has not been called.
    */
   protected PSSecurityToken getSecurityToken()
   {
      if (! m_userInfoInited)
         throw new IllegalStateException("userInfo not initialized");
      
      return m_securityToken;
   }
   
   /**
    * Gets the name of the user that initiated this job.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   protected String getUserId()
   {
      return m_userId;
   }

   /**
    * Get the job type to use in error and console messages.
    * 
    * @return The type of the job, never <code>null</code> or empty.
    */   
   protected abstract String getJobType();
   
   /**
    * Name of the init param to use to get the role required for running this
    * deployment job.
    */
   private static final String ROLE_PARAM_NAME = "role";
   
   /**
    * The security token extracted from the request supplied to the 
    * <code>init</code> call.  <code>null</code> until <code>init()</code> is
    * called, never <code>null</code> or modified after that.
    */
   private PSSecurityToken m_securityToken;
   
   /**
    * Flag to determine if the <code>init()</code> method has been called.
    * <code>false</code> until it is called, <code>true</code> after that.
    */
   private boolean m_userInfoInited = false;
   
   /**
    * The name of the user from the first authenticated user entry found in the
    * user session of the request passed to the <code>init()</code> method, 
    * never <code>null</code> or empty after that.
    */
   private String m_userId;
   
   /**
    * Current number of dependencies having been processed, updated by 
    * calls to <code>updateStatus()</code>.
    */
   protected int m_curDepCount = 0;
   
   /**
    * Total number of dependencies to be processed, initially <code>-1</code>, 
    * set to a non-negative value by a call to the <code>initDepCount()</code> 
    * method, never modified after that.
    */
   private int m_depTotal = -1;
}
