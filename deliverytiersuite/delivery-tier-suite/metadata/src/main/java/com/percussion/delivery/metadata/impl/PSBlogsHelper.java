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

            List<PSMetadataBlogYear> blogYearsList = new ArrayList<>();

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
