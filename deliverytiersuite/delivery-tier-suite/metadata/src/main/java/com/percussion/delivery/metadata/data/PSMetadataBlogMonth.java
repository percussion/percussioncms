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

/**
 * Represents a year and the list of months with the number of posts for each month.
 * 
 * @author leonardohildt
 * 
 */
public class PSMetadataBlogMonth
{
    private String month;

    private Integer count;

    public PSMetadataBlogMonth(){

    }

    /**
     * @param month
     * @param count
     */
    public PSMetadataBlogMonth(String month, Integer count)
    {
        super();
        this.month = month;
        this.count = count;
    }

    /**
     * @return the month
     */
    public String getMonth()
    {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(String month)
    {
        this.month = month;
    }

    /**
     * @return the count
     */
    public Integer getCount()
    {
        return count;
    }

    /**
     * @param count the number of counts to set
     */
    public void setCount(Integer count)
    {
        this.count = count;
    }
}
