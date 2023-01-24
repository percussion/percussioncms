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
            "secure",
            "debug",
            "timeout"
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

        private String debug;

        private String timeout;

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public String getDebug() {
            return debug;
        }

        public void setDebug(String debug) {
            this.debug = debug;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

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

