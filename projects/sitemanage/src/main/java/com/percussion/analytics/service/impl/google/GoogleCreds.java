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

package com.percussion.analytics.service.impl.google;

public class GoogleCreds
{
    private String private_key_id;
    private String private_key;
    private String client_email;
    private String client_id;
    private String type;
	private String auth_uri;
    private String token_uri;
    private String auth_provider_x509_cert_url;
    private String client_x509_cert_url;
    private String project_id;
    
    public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}
	public String getAuth_uri() {
		return auth_uri;
	}
	public void setAuth_uri(String auth_uri) {
		this.auth_uri = auth_uri;
	}
	public String getToken_uri() {
		return token_uri;
	}
	public void setToken_uri(String token_uri) {
		this.token_uri = token_uri;
	}
	public String getAuth_provider_x509_cert_url() {
		return auth_provider_x509_cert_url;
	}
	public void setAuth_provider_x509_cert_url(String auth_provider_x509_cert_url) {
		this.auth_provider_x509_cert_url = auth_provider_x509_cert_url;
	}
	public String getClient_x509_cert_url() {
		return client_x509_cert_url;
	}
	public void setClient_x509_cert_url(String client_x509_cert_url) {
		this.client_x509_cert_url = client_x509_cert_url;
	}
	
    public String getPrivate_key_id()
    {
        return private_key_id;
    }
    public void setPrivate_key_id(String private_key_id)
    {
        this.private_key_id = private_key_id;
    }
    public String getPrivate_key()
    {
        return private_key;
    }
    public void setPrivate_key(String private_key)
    {
        this.private_key = private_key;
    }
    public String getClient_email()
    {
        return client_email;
    }
    public void setClient_email(String client_email)
    {
        this.client_email = client_email;
    }
    public String getClient_id()
    {
        return client_id;
    }
    public void setClient_id(String client_id)
    {
        this.client_id = client_id;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
}
