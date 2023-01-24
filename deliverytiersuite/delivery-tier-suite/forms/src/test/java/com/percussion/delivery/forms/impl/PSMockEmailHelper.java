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
package com.percussion.delivery.forms.impl;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.exceptions.PSEmailException;
import com.percussion.delivery.utils.IPSEmailHelper;
import com.percussion.delivery.utils.PSEmailServiceNotInitializedException;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

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
    public String sendMail(IPSEmailRequest emailRequest) throws PSEmailServiceNotInitializedException, PSEmailException
    {
        Validate.notNull(emailRequest);
        
        if (error != null)
        {
            String errorMsg = error;
            error = null;
            throw new PSEmailException(errorMsg);
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
