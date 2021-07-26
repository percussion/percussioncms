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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.itemmanagement.data;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.Date;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;


/**
 * Encapsulates start and end dates for an item.  
 */
@XmlRootElement(name="ItemDates")
public class PSItemDates extends PSAbstractDataObject
{
    /**
     * Default constructor. For serializers.
     */
    public PSItemDates()
    {
    }

    /**
     * Constructs an instance of the class.
     * 
     * @param itemId never null or blank.
     * @param startDate
     * @param endDate
     */
    public PSItemDates(String itemId, String startDate, String endDate,String comments)
    {
        notEmpty(itemId, "itemId");
        notNull(itemId, "itemId");
        
        this.itemId = itemId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.comments = comments;
    }

    public PSItemDates(String itemId, String startDate, String endDate)
    {
       this(itemId,startDate,endDate,null);
    }

    /**
     * @return itemId - content id
     */
    public String getItemId()
    {
        return itemId;
    }

    /**
     * @param itemId - Set the content id
     */
    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }

    /**
     * @return startDate - could be <null> or empty.
     */
    public String getStartDate()
    {
        return startDate;
    }

    /**
     * @param startDate - set the start date.
     */
    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    /**
     * @return endDate - could be <null> or empty.
     */
    public String getEndDate()
    {
        return endDate;
    }

    /**
     * @param endDate - set the end date
     */
    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }


    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    @NotNull
    @NotEmpty
    private String itemId;
    private String startDate;
    private String endDate;


    private String comments;
}
