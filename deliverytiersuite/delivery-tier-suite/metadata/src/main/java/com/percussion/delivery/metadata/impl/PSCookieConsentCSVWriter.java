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

package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSCookieConsent;

import java.util.Collection;

import org.apache.log4j.Logger;


/**
 * Merges the contents of a PSCookieConsent object into a
 * .CSV file with each PSCookieConsent obj as a single row.
 * 
 * @author chriswright
 *
 */
public class PSCookieConsentCSVWriter {
    
    private static final Logger MS_LOG = Logger.getLogger(PSCookieConsentCSVWriter.class.getName());

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
