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
package com.percussion.delivery.feeds.services.rdbms;

import com.percussion.delivery.feeds.services.IPSConnectionInfo;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author erikserating
 *
 */
@Entity
@Table(name = "PERC_CONNECTION_INFO")
public class PSConnectionInfo implements IPSConnectionInfo
{
    @Id
    private long id = 1; // this will always be one as there will only be one info entry in table
    
    @Basic
    private String url;
    
    @Basic
    private String username;
    
    @Basic
    private String password;
    
    @Basic
    private String encrypted;
    
    
    public PSConnectionInfo()
    {
        
    }

    /**
     * @param url
     * @param user
     * @param password
     * @param encrypted
     */
    public PSConnectionInfo(String url, String user, String password, boolean encrypted)
    {
        this.url = url;
        this.username = user;
        this.password = password;
        this.encrypted = Boolean.toString(encrypted);
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.services.rdbms.IPSConnectionInfo#setUrl(java.lang.String)
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the user
     */
    public String getUsername()
    {
        return username;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.services.rdbms.IPSConnectionInfo#setUser(java.lang.String)
     */
    public void setUsername(String user)
    {
        this.username = user;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.services.rdbms.IPSConnectionInfo#setPassword(java.lang.String)
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the encrypted
     */
    public String getEncrypted()
    {
        return encrypted;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.services.rdbms.IPSConnectionInfo#setEncrypted(java.lang.String)
     */
    public void setEncrypted(String encrypted)
    {
        this.encrypted = encrypted;
    }

    /**
     * @return the id
     */
    public long getId()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.services.rdbms.IPSConnectionInfo#setId(long)
     */
    public void setId(long id)
    {
        this.id = id;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PSConnectionInfo other = (PSConnectionInfo) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PSConnectionInfo [id=");
		builder.append(id);
		builder.append(", ");
		if (url != null) {
			builder.append("url=");
			builder.append(url);
			builder.append(", ");
		}
		if (username != null) {
			builder.append("username=");
			builder.append(username);
			builder.append(", ");
		}
		if (password != null) {
			builder.append("password=");
			builder.append(password);
			builder.append(", ");
		}
		if (encrypted != null) {
			builder.append("encrypted=");
			builder.append(encrypted);
		}
		builder.append("]");
		return builder.toString();
	}
    
    
    
    
}
