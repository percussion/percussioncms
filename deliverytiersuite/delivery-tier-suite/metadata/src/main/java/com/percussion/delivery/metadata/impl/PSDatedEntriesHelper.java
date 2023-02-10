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

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.data.PSMetadataDatedEntries;
import com.percussion.delivery.metadata.data.PSMetadataDatedEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

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
    private FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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
                            event.setStart(formatter.format(prop.getDatevalue()));
                    }
                    
                    if (END_DATE_PROPERTY_NAME.equals(prop.getName()) && prop.getDatevalue() != null)
                    {
                            event.setEnd(formatter.format(prop.getDatevalue()));
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
