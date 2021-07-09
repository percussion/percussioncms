/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.membership.services.impl;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.email.data.PSEmailRequest;
import com.percussion.delivery.utils.IPSEmailHelper;
import com.percussion.delivery.utils.security.PSHttpClient;
import com.percussion.generickey.services.IPSGenericKeyService;
import com.percussion.membership.data.IPSMembership;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;
import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.PSUserSummary;
import com.percussion.membership.services.IPSAuthProvider;
import com.percussion.membership.services.IPSMembershipDao;
import com.percussion.membership.services.IPSMembershipService;
import com.percussion.membership.services.PSAuthenticationFailedException;
import com.percussion.membership.services.PSMemberExistsException;
import com.percussion.membership.services.PSResetPwdException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.jasypt.util.password.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Provides services to create and manage membership accounts, and provides authentication
 * services for those accounts.
 *
 * @author JaySeletz
 *
 */
public class PSMembershipService implements IPSMembershipService
{

    private IPSAuthProvider authProvider;
    private IPSMembershipDao dao;
    private int sessionTimeOut;
    private PSHttpClient client;
    private IPSEmailHelper emailHelper;
    private IPSGenericKeyService genericKeyService;

    @Context HttpServletRequest request;
    /**
     * @return the genericKeyService
     */
    public IPSGenericKeyService getGenericKeyService()
    {
        return genericKeyService;
    }

    /**
     * @param genericKeyService the genericKeyService to set
     */
    public void setGenericKeyService(IPSGenericKeyService genericKeyService)
    {
        this.genericKeyService = genericKeyService;
    }

    @Autowired
    public PSMembershipService(IPSMembershipDao dao)
    {
        Validate.notNull(dao);
        this.dao = dao;
    }

    @Override
    public PSUserSummary getUser(String sessionId) throws Exception
    {
        Validate.notEmpty(sessionId);

        PSUserSummary userSum = null;
        Date now = new Date();
        IPSMembership member = dao.findMemberBySessionId(sessionId);
        if (member != null)
        {
            String currentSession = "";
            if (hasValidSession(member, now))
            {
                currentSession = sessionId;
                userSum = new PSUserSummary(member);
            }

            touchMemberSession(member, currentSession, now);
        }

        return userSum;
    }

    /**
     * Determine if member's session is expired.
     *
     * @param member The member to check
     * @param now The "current" date-time to use
     *
     * @return <code>true</code> if the session is valid, <code>false</code> if it has expired.
     */
    private boolean hasValidSession(IPSMembership member, Date now)
    {
        Date lastAccessed = member.getLastAccessed();
        Date expires = DateUtils.addMinutes(lastAccessed, sessionTimeOut);

        return expires.after(now);
    }

    public String createAccount(String email, String password, boolean confirmationRequired,
                                String confirmationPage, String customerSite) throws Exception
    {
        Validate.notEmpty(email);
        Validate.notEmpty(password);
        Validate.notNull(confirmationRequired);


        // ensure no html elements in the email
        String escapedEmail = StringEscapeUtils.escapeHtml(email);
        if (!email.equals(escapedEmail))
        {
            throw new IllegalArgumentException("Invalid email address");
        }

        // create the account
        String encryptedPassword = encryptPassword(password);
        PSMemberStatus status = confirmationRequired ? PSMemberStatus.Unconfirmed : PSMemberStatus.Active;

        IPSMembership member = dao.findMemberByUserId(email);
        String resetKey = StringUtils.EMPTY;
        if (member == null)
        {
            member = dao.createMember(email, encryptedPassword, status);
            member.setEmailAddress(email);
            member.setCreatedDate(new Date());
            // create the confirmation key if needed
            resetKey = confirmationRequired ? this.genericKeyService.generateKey(
                    IPSGenericKeyService.DAY_IN_MILLISECONDS) : null;
            member.setPwdResetKey(resetKey);
            dao.saveMember(member);
        }
        else if (member.getStatus().equals(PSMemberStatus.Unconfirmed))
        {
            resetKey = member.getPwdResetKey().toString();
        }
        else if (member.getStatus().equals(PSMemberStatus.Active)
                || member.getStatus().equals(PSMemberStatus.Blocked))
        {
            throw new PSMemberExistsException(email);
        }

        if (confirmationRequired)
        {
            //Send the email and set the URL for the confirmation page
            String resetUrl = confirmationPage + "?rvkey=" + resetKey;
            String emailMessage = getConfirmationEmailBodyMessage(email, resetUrl, customerSite);

            IPSEmailRequest emailRequest = new PSEmailRequest();
            emailRequest.setToList(email);
            emailRequest.setSubject("Thank you for registering with " + customerSite);
            emailRequest.setBody(emailMessage);

            emailHelper.sendMail(emailRequest);
        }
        else
        {
            // authenticate the user
            getAuthProvider().authenticate(email, password);
        }
        // create the session
        return createSession(member);
    }

    @Override
    public void changeStateAccount(PSAccountSummary account) throws Exception
    {
        Validate.notEmpty(account.getEmail());
        Validate.notEmpty(account.getAction());

        dao.changeStatusAccount(account);
    }

    @Override
    public void deleteAccount(String email) throws Exception
    {
        Validate.notEmpty(email);

        // ensure no html elements in the email
        String escapedEmail = StringEscapeUtils.escapeHtml(email);
        if (!email.equals(escapedEmail))
        {
            throw new IllegalArgumentException("Invalid email address");
        }

        dao.deleteAccount(email);
    }

    /**
     * Create a session for the supplied member
     *
     * @param member The member, assumed not <code>null</code>.
     *
     * @return The session id, never <code>null</code> or empty.
     *
     * @throws Exception if there are any unexpected errors.
     */
    private String createSession(IPSMembership member) throws Exception
    {
        String sessionId = generateSessionId();
        member.setSessionId(sessionId);
        member.setLastAccessed(new Date());
        dao.saveMember(member);

        return sessionId;
    }


    @Override
    public String login(String email, String password) throws PSAuthenticationFailedException, Exception
    {
        Validate.notEmpty(email);
        Validate.notEmpty(password);

        // authenticate the user
        getAuthProvider().authenticate(email, password);

        // create the session
        IPSMembership member = dao.findMemberByUserId(email);
        if (member == null)
        {
            // should not have been able to authenticate
            throw new PSAuthenticationFailedException("Unable to locate account for email: " + email);
        }

        return createSession(member);
    }

    @Override
    public void logout(String sessionId) throws Exception
    {
        if (sessionId == null || sessionId.isEmpty())
            return;

        IPSMembership member = dao.findMemberBySessionId(sessionId);
        if (member == null)
            return;

        member.setSessionId("");
        member.setLastAccessed(new Date());
        dao.saveMember(member);
    }

    @Override
    public String setResetKey(String email, String resetLinkUrl) throws PSAuthenticationFailedException, Exception
    {
        Validate.notEmpty(email);

        // create the session
        IPSMembership member = dao.findMemberByUserId(email);
        if (member == null || !member.getStatus().equals(PSMemberStatus.Active))
        {
            throw new PSAuthenticationFailedException("Unable to locate account for email: " + email);
        }

        String resetKey = this.genericKeyService.generateKey(IPSGenericKeyService.DAY_IN_MILLISECONDS);

        // set the reset key
        member.setPwdResetKey(resetKey);

        // save the member
        dao.saveMember(member);

        //Send the email and fix the URL for the redirect page
        String resetUrl = resetLinkUrl + "?resetkey=" + resetKey;
        String emailMessage = getResetEmailBodyMessage(email, resetUrl);

        IPSEmailRequest emailRequest = new PSEmailRequest();
        emailRequest.setToList(email);
        emailRequest.setSubject("Request to reset your password");
        emailRequest.setBody(emailMessage);

        emailHelper.sendMail(emailRequest);

        return resetKey;
    }

    @Override
    public PSUserSummary validatePwdResetKey(String resetKey) throws PSResetPwdException, Exception
    {
        Validate.notEmpty(resetKey);

        // Found the member by the reset key.
        IPSMembership member = dao.findMemberByPwdResetKey(resetKey);
        if (member == null || !member.getStatus().equals(PSMemberStatus.Active))
        {
            throw new PSAuthenticationFailedException("Unable to process the reset password request.");
        }
        Boolean isValid = this.genericKeyService.isValidKey(resetKey);

        if(!isValid)
        {
            throw new PSResetPwdException("The reset password token you have provided has timed out. You can request for a new token on the login page.");
        }

        return new PSUserSummary(member);
    }

    /**
     * (non-Javadoc)
     * @see com.percussion.membership.services.IPSMembershipService#resetPwd(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String resetPwd(String resetKey, String email, String password) throws PSResetPwdException,
            PSAuthenticationFailedException, Exception
    {
        Validate.notEmpty(resetKey);
        Validate.notEmpty(email);
        Validate.notEmpty(password);

        // Found the member by the reset key.
        IPSMembership member = dao.findMemberByPwdResetKey(resetKey);
        if (member == null || !member.getStatus().equals(PSMemberStatus.Active))
        {
            throw new PSAuthenticationFailedException("Unable to process the reset password.");
        }
        // Call the key service to validate the reset key
        Boolean isValid = this.genericKeyService.isValidKey(resetKey);

        if(!isValid)
        {
            throw new PSResetPwdException("The reset password token you have provided has timed out. You can request for a new token on the login page.");
        }

        // Retrieves the user email.
        String memberEmail = member.getEmailAddress();
        if (!email.equalsIgnoreCase(memberEmail))
        {
            throw new PSResetPwdException("The email doesn't match.");
        }

        // Resets the user password.
        String encryptedPassword = encryptPassword(password);
        member.setPassword(encryptedPassword);
        member.setPwdResetKey(null);
        dao.saveMember(member);

        // Call the key service to delete the reset key.
        genericKeyService.deleteKey(resetKey);

        // Authenticates the user.
        return login(email, password);
    }

    public void setAuthProvider(IPSAuthProvider authProvider)
    {
        Validate.notNull(authProvider);
        this.authProvider = authProvider;
    }

    @Override
    public String confirmAccount(String confirmKey) throws PSAuthenticationFailedException, Exception
    {
        Validate.notEmpty(confirmKey);
        String memberEmail = StringUtils.EMPTY;

        // Found the member by the reset key.
        IPSMembership member = dao.findMemberByPwdResetKey(confirmKey);
        if (member == null)
        {
            throw new PSAuthenticationFailedException("Unable to find the member by the key provided.");
        }
        // Call the key service to validate the reset key
        Boolean isValid = this.genericKeyService.isValidKey(confirmKey);

        if((!isValid))
        {
            // Key is invalid but the user is Enabled
            if (member.getStatus().equals(PSMemberStatus.Active))
            {
                throw new PSResetPwdException("User has already been confirmed.");
            }
            else
            {
                // Key is invalid and the user is not Enabled
                throw new PSResetPwdException("The confirmation token you have provided has timed out. You can request for a new token on the register page.");
            }
        }

        // Retrieves the user email.
        memberEmail = member.getEmailAddress();

        // Resets the user password.
        member.setPwdResetKey(null);
        member.setStatus(PSMemberStatus.Active);
        dao.saveMember(member);

        // Call the key service to delete the reset key.
        genericKeyService.deleteKey(confirmKey);

        return memberEmail;
    }

    @Override
    public List<PSUserSummary> findUsers() throws Exception
    {
        List<PSUserSummary> users = new ArrayList<>();

        List<IPSMembership> members = dao.findMembers();
        for (IPSMembership member : members)
        {
            users.add(new PSUserSummary(member));
        }

        return users;
    }

    @Override
    public void setUserGroups(String email, String groups) throws PSAuthenticationFailedException, Exception
    {
        Validate.notEmpty(email);

        // create the session
        IPSMembership member = dao.findMemberByUserId(email);
        if (member == null)
        {
            throw new PSAuthenticationFailedException("Unable to locate account for email: " + email);
        }

        // sets the groups
        member.setGroups(groups);

        // save the member
        dao.saveMember(member);
    }

    /**
     * Set the number of minutes to use for session timeout
     *
     * @param mins The number of mins, a value < 1 means no timeout
     */
    public void setSessionTimeoutMinutes(int mins)
    {
        sessionTimeOut = mins;
    }


    /**
     * Generates a unique session id
     *
     * @return The id, never <code>null</code> or empty.
     */
    private String generateSessionId()
    {
        return UUID.randomUUID().toString();
    }


    /**
     * Internal accessor method to enable future plugable strategy for auth providers.
     *
     * @return The currently configured auth provider, never <code>null</code>.
     */
    private IPSAuthProvider getAuthProvider()
    {
        return authProvider;
    }

    /**
     * Internal accessor method to get the http client
     *
     * @return The currently configured http client, never <code>null</code>.
     */
    private PSHttpClient getClient()
    {
        return client;
    }

    /**
     * Sets http client
     * @param client
     */
    public void setClient(PSHttpClient client)
    {
        this.client = client;
    }

    /**
     * Internal accessor method to the email helper
     *
     * @return The currently configured email helper client, never <code>null</code>.
     */
    public IPSEmailHelper getEmailHelper()
    {
        return emailHelper;
    }

    /**
     * Sets the email helper
     * @param emailHelper
     */
    public void setEmailHelper(IPSEmailHelper emailHelper)
    {
        this.emailHelper = emailHelper;
    }
    /**
     * Update the member's session id and last access time
     *
     * @param member The member, assumed not <code>null</code>
     * @param sessionId The session id to use, assumed not <code>null</code>, may be empty.
     * @param lastAccessed The date-time to use for last accessed, assumed not <code>null</code>
     *
     * @throws Exception if there are any unexpected errors.
     */
    private void touchMemberSession(IPSMembership member, String sessionId, Date lastAccessed) throws Exception
    {
        member.setSessionId(sessionId);
        member.setLastAccessed(lastAccessed);
        dao.saveMember(member);
    }

    /**
     * Helper method that returns an encrypted password.
     *
     * @param password the password to encrypt, assumed not <code>null</code> or empty.
     *
     * @return String the encrypted password
     */
    private String encryptPassword(String password)
    {
        String encryptedPassword = StringUtils.EMPTY;
        PasswordEncryptor passwordEncryptor = PSMembershipPasswordEncryptorFactory.getPasswordEncryptor();
        encryptedPassword = passwordEncryptor.encryptPassword(password);

        // return the encrypted password
        return encryptedPassword;
    }

    /**
     * Helper method to build the email message to be sent to the user
     *
     * @param userEmail The email account of the user.
     * @param redirectLink The redirect link to be added into the message.
     *
     * @return The text of the message key, never blank.
     */
    private String getResetEmailBodyMessage(String userEmail, String redirectLink)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("A password reset has been requested for the following account:");
        sb.append("\r\n");
        sb.append(userEmail);
        sb.append("\r\n");
        sb.append("If you did not initiate a password reset, please ignore this email.");
        sb.append("\r\n");
        sb.append("");
        sb.append("\r\n");
        sb.append("To reset the password, click the link below or copy and paste the link into your browser:");
        sb.append("\r\n");
        sb.append("");
        sb.append(redirectLink);

        return sb.toString();
    }

    /**
     * Helper method to build the confirmation email message to be sent to the user
     *
     * @param userEmail The email account of the user.
     * @param redirectLink The redirect link to be added into the message.
     * @param customerSite The customer website host address.
     *
     * @return The text of the message key, never blank.
     */
    private String getConfirmationEmailBodyMessage(String userEmail, String redirectLink, String customerSite)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Welcome and thank you for registering with us.");
        sb.append("\r\n\n");
        sb.append("To complete the registration process and activate your account, simply visit the link below:");
        sb.append("\r\n");
        sb.append("");
        sb.append(redirectLink);
        sb.append("\r\n");
        sb.append("If clicking the link does not work, just copy and paste the entire link into your browser.");
        sb.append("");
        sb.append("\r\n\n");
        sb.append("We're excited to have you on board!");
        sb.append("");
        sb.append("\r\n\n");
        sb.append("Sincerely,");
        sb.append("\r\n");
        sb.append("The " + customerSite + " Team");
        return sb.toString();
    }


}
