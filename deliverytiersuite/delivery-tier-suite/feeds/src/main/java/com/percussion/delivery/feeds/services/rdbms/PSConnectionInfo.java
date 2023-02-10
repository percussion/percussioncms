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
