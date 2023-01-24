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

package com.percussion.rest.pages;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "CalendarInfo")
@Schema(name="CalendarInfo",description="Represents Calendar information.")
public class CalendarInfo
{
    @Schema(name="startDate", required=false,description="Starting Date.")
    private Date startDate;

    @Schema(name="endDate", required=false,description="Ending Date.")
    private Date endDate;

    @Schema(name="calendars", required=false,description="List of calendars.")
    private List<String> calendars;

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public List<String> getCalendars()
    {
        return calendars;
    }

    public void setCalendars(List<String> calendars)
    {
        this.calendars = calendars;
    }

    @Override
    public String toString()
    {
        return "DateInfo [startDate=" + startDate + ", endDate=" + endDate + ", Calendars=" + calendars + "]";
    }

}
