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

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

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
    public String sendMail(IPSEmailRequest emailRequest) throws PSEmailServiceNotInitializedException, EmailException
    {
        MultiPartEmail commonsMultiPartEmail = createMultiPartEmail();
        if (commonsMultiPartEmail == null)
            throw new PSEmailServiceNotInitializedException();
        if (StringUtils.isNotBlank(emailRequest.getToList()))
        {
            String[] emails = emailRequest.getToList().split(",");
            for (String email : emails)
            {
                commonsMultiPartEmail.addTo(email);
            }
        }
        if (StringUtils.isNotBlank(emailRequest.getCCList()))
        {
            String[] emails = emailRequest.getCCList().split(",");
            for (String email : emails)
            {
                commonsMultiPartEmail.addCc(email);
            }
        }
        if (StringUtils.isNotBlank(emailRequest.getBCCList()))
        {
            String[] emails = emailRequest.getBCCList().split(",");
            for (String email : emails)
            {
                commonsMultiPartEmail.addBcc(email);
            }
        }
        commonsMultiPartEmail.setMsg(emailRequest.getBody());
        commonsMultiPartEmail.setSubject(emailRequest.getSubject());
        return commonsMultiPartEmail.send();
    }

    private MultiPartEmail createMultiPartEmail()
    {
        MultiPartEmail commonsMultiPartEmail = null;

        // Initializes the email client.
        try
        {
            Object hostProp = emailProps.get("email.hostName");
            if (StringUtils.isBlank((String) hostProp))
                return null;
            
            Object portProp = emailProps.get("email.portNumber");
            if (StringUtils.isBlank((String) portProp))
                return null;

            Object fromAddrProp = emailProps.get("email.fromAddress");
            if (StringUtils.isBlank((String) fromAddrProp))
                return null;
            
            Object bounceProp = emailProps.get("email.bounceAddress");
            if (StringUtils.isBlank((String) bounceProp))
                return null;
                 	
        	commonsMultiPartEmail = new MultiPartEmail();
        
        	//SMTP host name and port 
        	commonsMultiPartEmail.setHostName((String) hostProp);
            commonsMultiPartEmail.setSmtpPort(Integer.parseInt((String) portProp));
            
            //Only apply authentication if it is supplied. 
            if(!StringUtils.isBlank((String) emailProps.get("email.userName"))){
            	commonsMultiPartEmail.setAuthenticator(new DefaultAuthenticator((String) emailProps.get("email.userName"),
                    (String) emailProps.get("email.password")));
            }
            
            //Default TLS to false
            if(!emailProps.containsKey("email.TLS") || StringUtils.isBlank((String) emailProps.get("email.TLS")))
            	commonsMultiPartEmail.setTLS(false);
            else
            	commonsMultiPartEmail.setTLS(Boolean.parseBoolean((String) emailProps.get("email.TLS")));
            
            //Allow for the from name to be set
            if(!emailProps.containsKey("email.fromName") || StringUtils.isBlank((String) emailProps.get("email.fromName")))
            	commonsMultiPartEmail.setFrom((String) fromAddrProp);
            else
            	commonsMultiPartEmail.setFrom((String) fromAddrProp, (String) emailProps.get("email.fromName"));

            commonsMultiPartEmail.setBounceAddress((String) emailProps.get("email.bounceAddress"));
           
            if (StringUtils.isNotBlank((String) emailProps.get("email.sslPort")))
            {
                commonsMultiPartEmail.setSSL(true);
                commonsMultiPartEmail.setSslSmtpPort((String) emailProps.get("email.sslPort"));
            }
        }
        catch (EmailException e)
        {
            commonsMultiPartEmail = null;
            log.error("Invalid properties suplied for email client:", e);
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
