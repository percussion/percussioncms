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
import com.percussion.error.PSExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class PSEmailHelper implements IPSEmailHelper
{
    /**
     * Constructor for this class, initializes email client with the supplied
     * properties.
     * 
     * @param emailProps must not be <code>null</code>.
     */
    public PSEmailHelper(Properties emailProps)
    {
        if (emailProps == null)
        {
            throw new IllegalArgumentException("emailProps must not be null");
        }
        this.emailProps = emailProps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.delivery.utils.IPSEmailHelper#sendMail(com.percussion.
     * delivery.email.data.IPSEmailRequest)
     */
    @Override
    public String sendMail(IPSEmailRequest emailRequest) throws PSEmailServiceNotInitializedException, PSEmailException {
        MultiPartEmail commonsMultiPartEmail = createMultiPartEmail();

        if (commonsMultiPartEmail == null)
            throw new PSEmailServiceNotInitializedException();

        commonsMultiPartEmail.setDebug(log.isDebugEnabled());

        if (StringUtils.isNotBlank(emailRequest.getToList()))
        {
            String[] emails = emailRequest.getToList().split(",");
            for (String email : emails)
            {
                try {
                    commonsMultiPartEmail.addTo(email);
                } catch (EmailException e) {
                    log.error("Error adding address: {} to To: for email message. Error: {}",
                            email,
                            e.getMessage());
                }
            }
        }
        if (StringUtils.isNotBlank(emailRequest.getCCList()))
        {
            String[] emails = emailRequest.getCCList().split(",");
            for (String email : emails)
            {
                try {
                    commonsMultiPartEmail.addCc(email);
                } catch (EmailException e) {
                    log.error("Error adding address: {} to CC: for email message. Error: {}",
                            email,
                            e.getMessage());
                }
            }
        }
        if (StringUtils.isNotBlank(emailRequest.getBCCList()))
        {
            String[] emails = emailRequest.getBCCList().split(",");
            for (String email : emails)
            {
                try {
                    commonsMultiPartEmail.addBcc(email);
                } catch (EmailException e) {
                    log.error("Error adding address: {} to BCC: for email message. Error: {}",
                            email,
                            e.getMessage());
                }
            }
        }

        try {
            commonsMultiPartEmail.setMsg(emailRequest.getBody());
        } catch (EmailException e) {
            log.error("Error setting the body: {} for email message. Error: {}",
                    emailRequest.getBody(),
                    e.getMessage());
            //with no body we shouldn't proceed.
            throw new PSEmailException(e);
        }

        commonsMultiPartEmail.setSubject(emailRequest.getSubject());

        try {
            return commonsMultiPartEmail.send();
        } catch (EmailException e) {
            log.error("Error sending email message. Error: {} Cause: {}",
                    e.getMessage(),
                    e.getCause().getMessage());
            logDebugProperties();

            //send failed so we shouldn't proceed.
            throw new PSEmailException(e);
        }
    }

    private void logDebugProperties(){
        log.debug("========== Start Debug Email Properties ================================");
        log.debug("SMTP User Name: {}", emailProps.getProperty(EMAIL_PROPS_SMTP_USERNAME));
        log.debug("SMTP User Password: {}", emailProps.getProperty(EMAIL_PROPS_SMTP_PASSWORD));
        log.debug("SMTP Host: {}", emailProps.getProperty(EMAIL_PROPS_HOSTNAME));
        log.debug("SMTP Port: {}", emailProps.getProperty(EMAIL_PROPS_PORT));
        log.debug("SMTP From Address: {}", emailProps.getProperty(EMAIL_PROPS_FROM_ADDRESS));
        log.debug("SMTP From Name: {}", emailProps.getProperty(EMAIL_PROPS_FROMNAME));
        log.debug("SMTP Bounce Address: {}", emailProps.getProperty(EMAIL_PROPS_BOUNCE_ADDRESS));
        log.debug("SMTP Use TLS: {}", emailProps.getProperty(EMAIL_PROPS_TLS));
        log.debug("SMTP SSL/TLS Port: {}", emailProps.getProperty(EMAIL_PROPS_SSLPORT));
        log.debug("============ End Debug Email Properties  ==============================");
    }
    
    private MultiPartEmail createMultiPartEmail()
    {
        MultiPartEmail commonsMultiPartEmail = null;

        // Initializes the email client.
        try
        {
            Object hostProp = emailProps.get(EMAIL_PROPS_HOSTNAME);
            if (StringUtils.isBlank((String) hostProp))
                return null;
            
            Object portProp = emailProps.get(EMAIL_PROPS_PORT);
            if (StringUtils.isBlank((String) portProp))
                return null;

            Object fromAddrProp = emailProps.get(EMAIL_PROPS_FROM_ADDRESS);
            if (StringUtils.isBlank((String) fromAddrProp))
                return null;
            
            Object bounceProp = emailProps.get(EMAIL_PROPS_BOUNCE_ADDRESS);
            if (StringUtils.isBlank((String) bounceProp))
                return null;
                 	
        	commonsMultiPartEmail = new MultiPartEmail();
            commonsMultiPartEmail.setCharset(EmailConstants.UTF_8);

        	//SMTP host name and port
        	commonsMultiPartEmail.setHostName((String) hostProp);
            commonsMultiPartEmail.setSmtpPort(Integer.parseInt((String) portProp));
            
            //Only apply authentication if it is supplied. 
            if(!StringUtils.isBlank((String) emailProps.get(EMAIL_PROPS_SMTP_USERNAME))){
            	commonsMultiPartEmail.setAuthenticator(new DefaultAuthenticator((String) emailProps.get(EMAIL_PROPS_SMTP_USERNAME),
                    (String) emailProps.get(EMAIL_PROPS_SMTP_PASSWORD)));
            }
            
            //Default TLS to false
            if(!emailProps.containsKey(EMAIL_PROPS_TLS) || StringUtils.isBlank((String) emailProps.get(EMAIL_PROPS_TLS)))
            	commonsMultiPartEmail.setTLS(false);
            else
            	commonsMultiPartEmail.setTLS(Boolean.parseBoolean((String) emailProps.get(EMAIL_PROPS_TLS)));
            
            //Allow for the from name to be set
            if(!emailProps.containsKey(EMAIL_PROPS_FROMNAME) || StringUtils.isBlank((String) emailProps.get(EMAIL_PROPS_FROMNAME)))
            	commonsMultiPartEmail.setFrom((String) fromAddrProp);
            else
            	commonsMultiPartEmail.setFrom((String) fromAddrProp, (String) emailProps.get(EMAIL_PROPS_FROMNAME));

            commonsMultiPartEmail.setBounceAddress((String) emailProps.get(EMAIL_PROPS_BOUNCE_ADDRESS));
           
            if (StringUtils.isNotBlank((String) emailProps.get(EMAIL_PROPS_SSLPORT)))
            {
                commonsMultiPartEmail.setSSL(true);
                commonsMultiPartEmail.setSslSmtpPort((String) emailProps.get(EMAIL_PROPS_SSLPORT));
            }
        }
        catch (EmailException e)
        {
            commonsMultiPartEmail = null;
            log.error("Invalid properties supplied for email client: {}", PSExceptionUtils.getMessageForLog(e));
        }
        return commonsMultiPartEmail;
    }
    
    /**
     * Gets initialized in ctor, may be <code>null</code> if there is any error
     * initializing the email.
     */
    private Properties emailProps;

    /**
     * Log for this class.
     */
    private static final Logger log = LogManager.getLogger(PSEmailHelper.class);
    
    
}
