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
package com.percussion.user.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import net.sf.oval.constraint.NotBlank;

/**
 * @author DavidBenua
 *
 */
@Entity
@Table(name="USERLOGIN")
public class PSUserLogin extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank
    @Column(name="USERID")
    private String userid; 
    
    @Basic
    @Column(name="PASSWORD")
    private String password; 
    
    public PSUserLogin()
    {
        
    }

    /**
     * @return the userid
     */
    public String getUserid()
    {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid)
    {
        this.userid = userid;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
}
