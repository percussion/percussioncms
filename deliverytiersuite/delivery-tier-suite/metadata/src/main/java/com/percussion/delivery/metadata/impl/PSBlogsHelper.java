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

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.data.PSMetadataBlogEntry;
import com.percussion.delivery.metadata.data.PSMetadataBlogMonth;
import com.percussion.delivery.metadata.data.PSMetadataBlogYear;
import com.percussion.delivery.metadata.data.PSMetadataRestBlogList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class process the results in order to get the list of the associated
 * posts by date. return a PSMetadataRestBlogList that contains the list
 * organized by year and month.
 * 
 * @author leonardohildt
 * 
 */
public class PSBlogsHelper
{

    public static final String BLOG_PROPERTY_NAME = "dcterms:created";

    /**
     * This method is responsible for returning the list of the associated posts by date.
     * The date is organized by year and month. Each node shows the number of posts for that month.
     * The year shows the aggregate amount for all of the months posted that year.
     * 
     * @param results entries containing the collection of metadata entries.
     * @return PSMetadataRestBlogList contains the list organized by year and month.
     */
    public PSMetadataRestBlogList getProcessedBlogs(List<IPSMetadataEntry> results) throws Exception
    {
        if (results == null)
            throw new IllegalArgumentException("Results can not be null");

        PSMetadataBlogEntry blogs = new PSMetadataBlogEntry();

        try
        {
            for (IPSMetadataEntry entryPage : results)
            {
                for (IPSMetadataProperty prop : entryPage.getProperties())
                {
                    if (BLOG_PROPERTY_NAME.equals(prop.getName()) && !prop.getStringvalue().isEmpty())
                    {
                        Calendar cal = Calendar.getInstance();
                        Date currentDate = cal.getTime(); // used to check whether or not a page is set to publish in the future
                        cal.setTime(prop.getDatevalue());
                        Date pageDate = cal.getTime();
                        
                        // if page date is in future, we don't want to return that value
                        if (pageDate.compareTo(currentDate) > 0) {
                            break;
                        }

                        PSMetadataBlogYear selectedYear = null;
                        PSMetadataBlogMonth selectedMonth = null;
                        Integer currentPostYear = cal.get(Calendar.YEAR);
                        String currentPostMonth = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                        
                        for (PSMetadataBlogYear year : blogs.getYears())
                        {
                            if (year.getYear().equals(currentPostYear))
                            {
                                selectedYear = year;
                                break;
                            }
                        }
                        if (selectedYear == null)
                        {
                            selectedYear = new PSMetadataBlogYear(cal.get(Calendar.YEAR));
                        }
                        if (selectedYear != null)
                        {
                            for (PSMetadataBlogMonth month : selectedYear.getMonths())
                            {
                                if (month.getMonth().equals(currentPostMonth))
                                {
                                    selectedMonth = month;
                                    break;
                                }
                            }
                            if (selectedMonth != null)
                            {
                                selectedYear.setYearCount(selectedYear.getYearCount() + 1);
                                selectedMonth.setCount(selectedMonth.getCount() + 1);
                            }
                        }
                        blogs.getYears().add(selectedYear);
                    }   
                }
            }

            List<PSMetadataBlogYear> blogYearsList = new ArrayList<PSMetadataBlogYear>();

            for (PSMetadataBlogYear year : blogs.getYears())
            {
                blogYearsList.add(year);
            }
            Comparator<PSMetadataBlogYear> comp = new YearOrderBlogsComparator();
            Collections.sort(blogYearsList, comp);

            PSMetadataRestBlogList blogListResults = new PSMetadataRestBlogList();
            blogListResults.setYears(blogYearsList);
            return blogListResults;
        }
        catch (Exception e)
        {
            throw new Exception("Cannot get the list of blogs organized by year and months.");
        }
    }

    class YearOrderBlogsComparator implements Comparator<PSMetadataBlogYear>
    {
        
        public int compare(PSMetadataBlogYear o1, PSMetadataBlogYear o2)
        {
            return o2.getYear().compareTo(o1.getYear());
        }
    }

}
