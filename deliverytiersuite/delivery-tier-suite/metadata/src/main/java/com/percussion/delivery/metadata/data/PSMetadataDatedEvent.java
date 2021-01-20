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
package com.percussion.delivery.metadata.data;

import org.apache.commons.lang.StringUtils;

/**
 * This class contains the structure of the event information. The object is
 * composed of the properties:
 * <ul>
 *  <li>page title</li>
 *  <li>page summary</li>
 *  <li>page start date</li>
 *  <li>page end date</li>
 *  <li>page url</li>
 * </ul>
 * 
 * @author rafaelsalis
 */
public class PSMetadataDatedEvent
{
    
    private String title;
    
    private String summary;
    
    private String start;
    
    private String end;
    
    private String url;
    
    private boolean allDay = false;
    
    private String textColor = StringUtils.EMPTY;
    
    private String textBackground = StringUtils.EMPTY;
    
    /**
     * @return the title of the page, never <code>null</code> or empty.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title sets the page title, never <code>null</code> or empty.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the page summary, it may be <code>null</code> or empty if
     * the page summary is unknown. 
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * @param summary the page summary to set, may be <code>null</code> or empty.
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    /**
     * @return the page start date, it may be <code>null</code> or empty if
     * the page start date is unknown.
     */
    public String getStart()
    {
        return start;
    }

    /**
     * @param start sets the page start date, may be <code>null</code> or empty.
     */
    public void setStart(String start)
    {
        this.start = start;
    }

    /**
     * @return the page end date, it may be <code>null</code> or empty if
     * the page end date is unknown.
     */
    public String getEnd()
    {
        return end;
    }

    /**
     * @param end sets the page end date, may be <code>null</code> or empty.
     */
    public void setEnd(String end)
    {
        this.end = end;
    }
    

    /**
     * @return the page url, never <code>null</code> or empty.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url sets the page url, never <code>null</code> or empty.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the all day value.
     */
    public boolean isAllDay()
    {
        return allDay;
    }

    /**
     * @param allDay sets the all day.
     */
    public void setAllDay(boolean allDay)
    {
        this.allDay = allDay;
    }

    /**
     * @return the text color, may be empty but never <code>null</code>.
     */
    public String getTextColor()
    {
        return textColor;
    }

    /**
     * @param sets the text color, maybe empty but never <code>null</code>.
     */
    public void setTextColor(String textColor)
    {
        this.textColor = textColor;
    }

    /**
     * @return the text background, may be empty but never <code>null</code>.
     */
    public String getTextBackground()
    {
        return textBackground;
    }

    /**
     * @param sets the text background, maybe empty but never <code>null</code>.
     */
    public void setTextBackground(String textBackground)
    {
        this.textBackground = textBackground;
    }

}
