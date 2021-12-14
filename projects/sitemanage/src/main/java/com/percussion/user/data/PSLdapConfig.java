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
package com.percussion.user.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * 
 * Directory Service Configuration loaded from a configuration file.
 * 
 * @author adamgent
 *
 */
@XmlRootElement(name="LdapConfig")
@JsonRootName("LdapConfig")
public class PSLdapConfig extends PSAbstractDataObject
{
    

    private static final long serialVersionUID = 1L;
    private PSLdapServer server;
    
    /**
     * A single directory service.
     * If <code>null</code> then directory service is disabled.
     * @return maybe <code>null</code>.
     */
    @XmlElement(name = "LdapServer")
    public PSLdapServer getServer()
    {
        return server;
    }

    public void setServer(PSLdapServer server)
    {
        this.server = server;
    }

    /**
     * A single Directory Server configuration.
     * @author adamgent
     */
    @XmlType(propOrder = {
            "host", "port", "user",
            "password", 
            "catalogType",
            "objectAttributeName",
            "emailAttributeName",
            "organizationalUnits",
            "secure"
            })
    public static class PSLdapServer extends PSAbstractDataObject {

        private static final long serialVersionUID = 1L;

        @NotNull
        @NotBlank
        private String host;
        
        @NotNull
        @NotBlank
        @Min(1)
        private Integer port;
        
        @NotNull
        @NotBlank
        private String user;
        
        private String password;
        
        @NotNull
        private String objectAttributeName;
        
        private String emailAttributeName;
        
        @NotNull
        @NotEmpty
        private Set<String> organizationalUnits;
        
        private boolean secure;        
        
        @NotNull
        private CatalogType catalogType;

        public String getHost()
        {
            return host;
        }
        
        public void setHost(String host)
        {
            this.host = host;
        }

        public Integer getPort()
        {
            return port;
        }

        public void setPort(Integer port)
        {
            this.port = port;
        }


        public String getUser()
        {
            return user;
        }

        public void setUser(String user)
        {
            this.user = user;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getObjectAttributeName()
        {
            return objectAttributeName;
        }

        public void setObjectAttributeName(String objectAttributeName)
        {
            this.objectAttributeName = objectAttributeName;
        }
        
        public String getEmailAttributeName()
        {
            return emailAttributeName;
        }

        public void setEmailAttributeName(String emailAttributeName)
        {
            this.emailAttributeName = emailAttributeName;
        }


        @XmlElement(name="organizationalUnit", required=true)
        public Set<String> getOrganizationalUnits()
        {
            return organizationalUnits;
        }

        public void setOrganizationalUnits(Set<String> organizationalUnits)
        {
            this.organizationalUnits = organizationalUnits;
        }

        public Boolean getSecure()
        {
            return secure;
        }

        public void setSecure(Boolean secure)
        {
            this.secure = secure;
        }
        
        @XmlElement(name="catalog")
        public CatalogType getCatalogType()
        {
            return catalogType;
        }

        public void setCatalogType(CatalogType catalogType)
        {
            this.catalogType = catalogType;
        }
        
        public static enum CatalogType {
            shallow,
            deep;
        }
    
    }
    


}

