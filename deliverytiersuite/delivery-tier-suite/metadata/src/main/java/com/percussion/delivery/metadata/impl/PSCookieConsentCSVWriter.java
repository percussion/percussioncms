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

package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSCookieConsent;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Merges the contents of a PSCookieConsent object into a
 * .CSV file with each PSCookieConsent obj as a single row.
 * 
 * @author chriswright
 *
 */
public class PSCookieConsentCSVWriter {
    
    private static final Logger MS_LOG = LogManager.getLogger(PSCookieConsentCSVWriter.class.getName());

    private Collection<IPSCookieConsent> entries;
    
    /**
     * Constructor to initialize the list of cookie consent entries.
     * @param consents the list of entries to convert to CSV.
     */
    public PSCookieConsentCSVWriter(Collection<IPSCookieConsent> consents) {
        this.entries = consents;
    }
    
    /**
     * This method loops through each IPSCookieConsent entry
     * and adds its values to a new line in a .CSV file.
     * @return a string representation of the .CSV file.
     */
    public String writeCSVFile() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Site Name,Service Name,Consent Date,IP Address,Opt In\n");
       
        for (IPSCookieConsent consent : entries) {
            try {
                sb.append(consent.getSiteName() + ",");
                sb.append(consent.getService() + ",");
                sb.append(consent.getConsentDate().toString() + ",");
                sb.append(consent.getIP() + ",");
                sb.append(consent.getOptIn() + "\n");
            }        
            catch (NullPointerException e) {
                MS_LOG.error("Error writing cookie consent entry to .CSV file. "
                        + "Check for NULL entries in the DTS database.", e);
                continue;
            }
        }
        
        return sb.toString();
    }
    
}
