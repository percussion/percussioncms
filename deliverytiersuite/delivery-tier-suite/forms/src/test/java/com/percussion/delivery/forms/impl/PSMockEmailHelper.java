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
package com.percussion.delivery.forms.impl;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.utils.IPSEmailHelper;
import com.percussion.delivery.utils.PSEmailServiceNotInitializedException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.mail.EmailException;

/**
 * Mock email helper for testing services that send email.  Should be wired into services in the test/beans.xml.
 * Static methods on the impl class can be used to access any email requests sent - note that this is assumed to
 * be used in a single-threaded test context.
 * 
 * @author JaySeletz
 *
 */
public class PSMockEmailHelper implements IPSEmailHelper
{
    private static List<IPSEmailRequest> emailRequests = new ArrayList<IPSEmailRequest>();
    private static int requestId = 1;
    private static boolean configured = true;
    private static String error = null;

    @Override
    public String sendMail(IPSEmailRequest emailRequest) throws PSEmailServiceNotInitializedException, EmailException
    {
        Validate.notNull(emailRequest);
        
        if (error != null)
        {
            String errorMsg = error;
            error = null;
            throw new EmailException(errorMsg);
        }
        
        if (!configured)
        {
            configured = true;
            throw new PSEmailServiceNotInitializedException();
        }
        
        emailRequests.add(emailRequest);
        
        return String.valueOf(requestId++);
    }

    /**
     * Get the list of email requests that have been made, clears the list from memory
     * 
     * @return The list, not <code>null</code>, may be empty.
     */
    public static List<IPSEmailRequest> getEmailRequests()
    {
        List<IPSEmailRequest> result = new ArrayList<IPSEmailRequest>(emailRequests);
        emailRequests.clear();
        return result;
    }

    /**
     * Temporarily sets this helper as not configured.  Reset to <code>true</code>
     * after next call to {@link #sendMail(IPSEmailRequest)}
     * 
     * @param isConfigured <code>true</code> to be configured, <code>false</code> if not.
     */
    public static void setConfigured(boolean isConfigured)
    {
        configured = isConfigured;
    }

    /**
     * Set an error to be thrown on next call to {@link #sendMail(IPSEmailRequest)}.
     * 
     * @param errorMsg The error msg to use
     */
    public static void setError(String errorMsg)
    {
        error = errorMsg;
    }

}
