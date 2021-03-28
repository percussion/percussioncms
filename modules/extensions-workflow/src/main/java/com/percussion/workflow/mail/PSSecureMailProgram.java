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

import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import com.percussion.extension.IPSExtensionErrors;
import com.percussion.security.PSEncryptProperties;

/**
 * Can be used to send secure mail via workflow transitions. Follows the Mail
 * Program interface: <code>IPSMailProgram</code>. Properties for configuring
 * the secure mail class are found rxconfig/Workflow/rxworkflow.properties.
 *
 * Can be configured with the following fields in the properties file:
 *
 * <ul>
 * <li>SMTP_HOST</li>
 * <li>SMTP_USERNAME</li>
 * <li>SMTP_PASSWORD</li>
 * <li>SMTP_PORT</li>
 * <li>SMTP_TLSENABLED</li>
 * <li>SMTP_SSLPORT</li>
 * </ul>
 *
 * If username, password, sslport, and tlsenabled are not set, they are ignored.
 * If sslport is not set ssl defaults to false.
 *
 * @see IPSMailProgram
 * @author chriswright
 *
 */
public class PSSecureMailProgram implements IPSMailProgram {

    private static final Logger log = Logger.getLogger(PSSecureMailProgram.class.getName());

    /**
     * Default ctor.
     */
    public PSSecureMailProgram() {
        super();
    }

    @Override
    public void init() throws PSMailException {
        // No need for init.
    }

    @Override
    public void sendMessage(IPSMailMessageContext messageContext)
            throws PSMailException {
        validateMessageContext(messageContext);

        try {
            MultiPartEmail commonsMultiPartEmail = createMultiPartEmail(messageContext);
            commonsMultiPartEmail.send();
        } catch (EmailException e) {
            log.error("Error sending workflow mail notification with message: ", e);
        }

    }

    @Override
    public void terminate() throws PSMailException {
        // Method required by interface.
    }

    /**
     * Validates that the required field(s) are present in the
     * <code>messageContext</code>. Required field(s) are SMTP_HOST.
     *
     * @param messageContext
     *            the context which contains the fields to validate.
     * @throws PSMailException
     */
    private void validateMessageContext(IPSMailMessageContext messageContext)
            throws PSMailException {
        String smtpHost = messageContext.getSmtpHost();

        if (StringUtils.isEmpty(smtpHost)) {
            throw new PSMailException(IPSExtensionErrors.SMTP_HOST_EMPTY);
        }
    }

    /**
     * Creates a <code>MultiPartEmail</code> from the
     * <code>messageContext</code> which can be used to send the workflow
     * notification.
     *
     * @param messageContext
     *            the <code>messageContext</code> with fields to add to the
     *            <code>MultiPartEmail</code>.
     * @return the <code>MultiPartEmail</code> used to send the workflow
     *         notification.
     * @throws EmailException
     */
    private MultiPartEmail createMultiPartEmail(
            IPSMailMessageContext messageContext) throws EmailException {
        MultiPartEmail commonsMultiPartEmail = new MultiPartEmail();

        Boolean isTLSEnabled = false; // TLS defaults to false if not set.

        if (!StringUtils.isEmpty(messageContext.getIsTLSEnabled())
                && "true".equalsIgnoreCase(messageContext.getIsTLSEnabled())) {
            isTLSEnabled = true;
        }
        log.debug("Is TLS enabled: " + isTLSEnabled);

        if (!StringUtils.isEmpty(messageContext.getPortNumber())) {
            try {
                int smtpPort = Integer.parseInt(messageContext.getPortNumber());
                commonsMultiPartEmail.setSmtpPort(smtpPort);
                log.debug("SMTP port: " + smtpPort);
            } catch (NumberFormatException e) {
                log.error(
                        "Unable to set smtp port number; defaulting to port 587.  Please check the configuration and"
                                + " verify the port number is a valid port.", e);
            }
        }

        if (!StringUtils.isEmpty(messageContext.getUserName())
                && !StringUtils.isEmpty(messageContext.getPassword())) {
            // decrypt password
            String pwd = PSEncryptProperties.decryptProperty(messageContext.getPassword(),
                    PSLegacyEncrypter.getInstance(
                            PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                    ).getPartOneKey(),
                    PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR),
                    null
            );
            commonsMultiPartEmail.setAuthentication(
                    messageContext.getUserName(), pwd);
        }
        log.debug("Authenticating with user name: " + messageContext.getUserName());

        if (StringUtils.isNotBlank(messageContext.getTo())) {
            String[] emails = messageContext.getTo().split(",");
            for (String email : emails) {
                log.debug("Sending mail to: " + email);
                commonsMultiPartEmail.addTo(makeAddress(email, messageContext.getMailDomain()));
            }
        }

        if (StringUtils.isNotBlank(messageContext.getCc())) {
            String[] emails = messageContext.getCc().split(",");
            for (String email : emails) {
                log.debug("Cc: " + email);
                commonsMultiPartEmail.addCc(makeAddress(email, messageContext.getMailDomain()));
            }
        }
        if (!StringUtils.isEmpty(messageContext.getSSLPortNumber())) {
            commonsMultiPartEmail.setSslSmtpPort(messageContext
                    .getSSLPortNumber());
            commonsMultiPartEmail.setSSLOnConnect(true);
        }
        log.debug("Using SSL port: " + messageContext.getSSLPortNumber());

        commonsMultiPartEmail.setMsg(messageContext.getBody());

        if (!StringUtils.isEmpty(messageContext.getBounceAddr())) {
            commonsMultiPartEmail.setBounceAddress(makeAddress(messageContext.getBounceAddr(),
                    messageContext.getMailDomain()));
            log.debug("Using bounce address: " + messageContext.getBounceAddr());
        }

        commonsMultiPartEmail.setStartTLSEnabled(isTLSEnabled);
        commonsMultiPartEmail.setHostName(messageContext.getSmtpHost());
        commonsMultiPartEmail.setFrom(makeAddress(messageContext.getFrom(),
                messageContext.getMailDomain()));
        commonsMultiPartEmail.setSubject(messageContext.getSubject());

        return commonsMultiPartEmail;
    }

    /**
     * Helper routine that constructs an email address from a
     * user or role, appending the mail domain name to
     * any address that does not contain a "@".  This is used
     * if the user or role does not have the sys_email member
     * property set.
     *
     * @param user  comma-separated list of users.
     * @param mailDomain name of the mail domain. May optionally contain a
     *                   leading "@".
     * @return a String with the appropriate e-mail address and domain name.
     */
    private String makeAddress(String user, String mailDomain) {
        log.debug("Making address with user: " + user + " and domain: " + mailDomain);
        String userAddressString = null;
        if (!mailDomain.startsWith("@")) {
            mailDomain = "@" + mailDomain;
        }
        if (0 != user.length()) {
            if (-1 == user.indexOf('@')) {
                userAddressString = user + mailDomain;
            } else {
                userAddressString = user;
            }
        }
        return userAddressString;
    }
}
