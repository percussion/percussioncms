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
