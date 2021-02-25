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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.delivery.metadata.impl;

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.data.PSMetadataDatedEntries;
import com.percussion.delivery.metadata.data.PSMetadataDatedEvent;
import com.percussion.utils.date.PSConcurrentDateFormat;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * This class is responsible for process the dates of the page and return
 * the JSONObject with the entries with their properties.
 * 
 * @author rafaelsalis
 * 
 */
public class PSDatedEntriesHelper
{
    
    /**
     * Constants names for the page properties.
     */
    private static final String SUMMARY_PROPERTY_NAME = "dcterms:abstract";
    private static final String START_DATE_PROPERTY_NAME = "perc:start_date";
    private static final String END_DATE_PROPERTY_NAME = "perc:end_date";
    
    /**
     * Constant for the date formater. 
     */
    private PSConcurrentDateFormat formatter = new PSConcurrentDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * This method is responsible for return the list with entries with their
     * properties:
     * <ul>
     *  <li>page title</li>
     *  <li>page summary</li>
     *  <li>page start date</li>
     *  <li>page end date</li>
     *  <li>page url</li>  
     * </ul>
     * 
     * @param results assumed not <code>null</code>.
     * @return a {@link PSMetadataDatedEntries} object containing the entries.
     * @throws Exception
     */
    public PSMetadataDatedEntries getDatedEntries(List<IPSMetadataEntry> results) throws Exception
    {
        if (results == null)
            throw new IllegalArgumentException("Results can not be null");

        PSMetadataDatedEntries datedListResults = new PSMetadataDatedEntries();
        
        try
        {
            for (IPSMetadataEntry entryPage : results)
            {
                PSMetadataDatedEvent event = new PSMetadataDatedEvent();
                event.setTitle(entryPage.getLinktext());
                
                // Strip the site from the url
                String[] paths = entryPage.getPagepath().split("/"); 
                String pageUrl = StringUtils.EMPTY;
                for (int i = 2; i < paths.length; i++)
                {
                    pageUrl = pageUrl + "/" + paths[i];
                }
                event.setUrl(pageUrl);
                
                for (IPSMetadataProperty prop : entryPage.getProperties())
                {
                    if (SUMMARY_PROPERTY_NAME.equals(prop.getName()) && !prop.getStringvalue().isEmpty())
                    {
                        event.setSummary(prop.getStringvalue());
                    }
                    
                    if (START_DATE_PROPERTY_NAME.equals(prop.getName()) && prop.getDatevalue() != null)
                    {
                            event.setStart(formatter.toString(prop.getDatevalue()));
                    }
                    
                    if (END_DATE_PROPERTY_NAME.equals(prop.getName()) && prop.getDatevalue() != null)
                    {
                            event.setEnd(formatter.toString(prop.getDatevalue()));
                    }
                }
                
                datedListResults.add(event);
            }
            return datedListResults;
        }
        catch (Exception e)
        {
            throw new Exception("Cannot get the list of entries within a specific range of dates.");
        }
    }
    
}
