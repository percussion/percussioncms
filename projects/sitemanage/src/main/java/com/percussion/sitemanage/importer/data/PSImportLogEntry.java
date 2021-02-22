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
package com.percussion.sitemanage.importer.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author JaySeletz
 *
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSImportLogEntry")
@Table(name = "PSX_IMPORTLOGENTRY")

public class PSImportLogEntry extends PSAbstractDataObject
{
    @Id
    @Column(name = "LOGENTRYID")    
    private long logEntryId = -1L;
    
    @Basic
    @Column(name = "OBJECTID")
    private String objectId;
    
    @Basic
    @Column(name = "OBJECT_TYPE")
    private String objectType;
    
    @Basic
    @Column(name = "LOGENTRY_DATE")
    private Date logEntryDate;
    
    @Basic
    @Column(name = "DATA")
    private String logData;
    
    @Basic
    @Column(name = "CATEGORY")
    private String category;
    
    @Basic
    @Column(name = "DESCRIPTION")
    private String description;
    
    public PSImportLogEntry()
    {
        
    }
    
    public PSImportLogEntry(String objectId, String objectType, Date logEntryDate, String logData)
    {
        this(objectId, objectType, logEntryDate, null, null, logData);
    }
    
    /**
     * @param objectId The id of the object being logged, not <code>null<code/> or empty.
     * @param objectType The type of the object being logged, not <code>null</code> or empty.
     * @param date The date of the log, not <code>null</code>.
     * @param description An optional description of the object, may be <code>null</code>.
     * @param category An optional category, may be <code>null</code>.
     * @param logData The log message, not <code>null<code/> or empty.
     */
    public PSImportLogEntry(String objectId, String objectType, Date logEntryDate, String description, String category, String logData)
    {
        Validate.notEmpty(objectType);
        Validate.notEmpty(objectId);
        Validate.notNull(logEntryDate);
        Validate.notNull(logData);
        
        this.objectId = objectId;
        this.objectType = objectType;
        this.logEntryDate = logEntryDate;
        this.description = description;
        this.category = category;
        this.logData = logData;
    }

    public long getLogEntryId()
    {
        return logEntryId;
    }

    public void setLogEntryId(long logEntryId)
    {
        this.logEntryId = logEntryId;
    }

    public String getObjectId()
    {
        return objectId;
    }

    public void setObjectId(String objectId)
    {
        Validate.notEmpty(objectId);
        this.objectId = objectId;
    }

    public String getType()
    {
        return objectType;        
    }

    public void setType(String type)
    {
        Validate.notEmpty(type);
        this.objectType = type;
    }

    public Date getLogEntryDate()
    {
        return logEntryDate;
    }

    public void setLogEntryDate(Date logEntryDate)
    {
        Validate.notNull(logEntryDate);
        this.logEntryDate = logEntryDate;
    }

    public String getLogData()
    {
        return logData;
    }

    public void setLogData(String logData)
    {
        Validate.notNull(logData);
        this.logData = logData;
    }
    
    /**
     * Get the category, may be <code>null</code>.
     * @return
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * Get the description, may be <code>null</code>.
     * @return
     */
    public String getDescription()
    {
        return description;
    }
}
