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

package com.percussion.pagemanagement.assembler;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;

public class PSBlogYear {

    private Integer year;
    
    private Integer yearCount;

    private List<PSBlogMonth> months;

    /**
     * @param year
     */
    public PSBlogYear(Integer year)
    {
        super();
        this.year = year;
        this.yearCount = 0;
        
        Calendar cal = Calendar.getInstance();
        Integer currentYear = cal.get(Calendar.YEAR);
        Integer currentMonth = cal.get(Calendar.MONTH);
                
        List<PSBlogMonth> emptyMonths = new ArrayList<>();
        String[] localeMonths = new DateFormatSymbols(Locale.getDefault()).getMonths();
        Integer indexMonth = localeMonths.length-2;
        if (currentYear.equals(year))
        {
            indexMonth = currentMonth;
        }
        
        for (int i = indexMonth; i >= 0; i--) {
            PSBlogMonth newMonth = new PSBlogMonth(localeMonths[i], 0);
            emptyMonths.add(newMonth);
        };
        this.months = emptyMonths;
    }

    /**
     * @return the year
     */
    public Integer getYear()
    {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(Integer year)
    {
        this.year = year;
    }
    
    /**
     * @param yearCount the year count to set
     */
    public void setYearCount(Integer yearCount)
    {
        this.yearCount = yearCount;
    }
    
    /**
     * @return the count for the year
     */
    public Integer getYearCount()
    {
        return yearCount;
    }

    /**
     * @return the months
     */
    public List<PSBlogMonth> getMonths()
    {
        return months;
    }

    /**
     * @param months the months to set
     */
    public void setMonths(List<PSBlogMonth> months)
    {
        this.months = months;
    }

    /**
     * @param month the month to be add
     */
    public void addMonth(PSBlogMonth month)
    {
        this.months.add(month);
    }
    

}
