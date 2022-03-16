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
package com.percussion.delivery.utils;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.exceptions.PSEmailException;
import org.apache.commons.mail.EmailException;

/**
 * A common helper class to send emails.
 */
public interface IPSEmailHelper
{
    /**
     * Sends an email with the details provided in {@link IPSEmailRequest}
     * 
     * @param emailRequest The request object that has the details of the email,
     *            must not be <code>null</code>.
     * @return The message id of the email sent.
     * @throws EmailException
     * @throws PSEmailServiceNotInitializedException When there is an error
     *             while initializing the email client.
     */
    public String sendMail(IPSEmailRequest emailRequest) throws PSEmailServiceNotInitializedException, PSEmailException;

    public static final String EMAIL_PROPS_HOSTNAME = "email.hostName";
    public static final String EMAIL_PROPS_PORT = "email.portNumber";
    public static final String EMAIL_PROPS_SMTP_USERNAME = "email.userName";
    public static final String EMAIL_PROPS_SMTP_PASSWORD = "email.password";
    public static final String EMAIL_PROPS_FROM_ADDRESS = "email.fromAddress";
    public static final String EMAIL_PROPS_BOUNCE_ADDRESS = "email.bounceAddress";
    public static final String EMAIL_PROPS_TLS = "email.TLS";
    public static final String EMAIL_PROPS_FROMNAME = "email.fromName";
    public static final String EMAIL_PROPS_SSLPORT = "email.sslPort";

}
