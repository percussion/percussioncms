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

package com.percussion.delivery.comments.service.rdbms;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * 
 * @author miltonpividori
 * 
 */
@Entity
@Table(name = "PERC_COMMENT_TAGS")
public class PSCommentTag
{

    @TableGenerator(
        name="commentTagId", 
        table="PERC_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="commentTagId", 
        allocationSize=1)
    
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="commentTagId")
    private long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name="COMMENT_ID")
    private PSComment comment;
    
    @Basic
    private String name;
    
    public PSCommentTag()
    {
        
    }
    
    public PSCommentTag(String name)
    {
        this.name = name;
    }

    public long getId()
    {
        return id;
    }

    public PSComment getComment()
    {
        return comment;
    }

    public void setComment(PSComment comment)
    {
        this.comment = comment;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
