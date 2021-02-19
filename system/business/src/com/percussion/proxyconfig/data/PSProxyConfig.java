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
package com.percussion.proxyconfig.data;

import com.percussion.proxyconfig.service.impl.ProxyConfig;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LucasPiccoli
 * 
 *         This class contains proxy configuration information to allow traffic
 *         for certain protocols going through a proxy server.
 * 
 */
public class PSProxyConfig extends PSAbstractDataObject
{

   private static final long serialVersionUID = 1L;

   protected String host;

   protected String port;

   protected String user;
   
   protected String password;

   protected List<String> protocols;

   public PSProxyConfig()
   {
      super();
   }
   
   /**
    * 
    */
   public PSProxyConfig(String host, String port, String user, String password, List<String> protocols)
   {
      super();
      
      this.host = host;
      this.port = port;
      this.user = user;
      this.password = password;

      if (protocols == null)
      {
         this.protocols = new ArrayList<>();
      }
      else
      {
         this.protocols = new ArrayList<>(protocols);
      }
   }
   
   /**
    * 
    * @param proxyConfig
    */
   public PSProxyConfig(ProxyConfig proxyConfig)
   {
      this.host = proxyConfig.getHost();
      if (proxyConfig.getPassword() != null)
         this.password = proxyConfig.getPassword().getValue();
      this.port = proxyConfig.getPort();
      this.user = proxyConfig.getUser();

      if (proxyConfig.getProtocols() == null)
      {
         this.protocols = new ArrayList<>();
      }
      else
      {
         this.protocols = new ArrayList<>();
         for (String protocol : proxyConfig.getProtocols().getProtocols())
         {
            this.protocols.add(protocol);
         }
      }
   }
   
   /**
    * @return the host
    */
   public String getHost()
   {
      return host;
   }

   /**
    * @param host the host to set
    */
   public void setHost(String host)
   {
      this.host = host;
   }

   /**
    * @return the port
    */
   public String getPort()
   {
      return port;
   }

   /**
    * @param port the port to set
    */
   public void setPort(String port)
   {
      this.port = port;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param user the user to set
    */
   public void setUser(String user)
   {
      this.user = user;
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

   /**
    * @return the protocols
    */
   public List<String> getProtocols()
   {
      return protocols;
   }

   /**
    * @param protocols the protocols to set
    */
   public void setProtocols(ArrayList<String> protocols)
   {
      this.protocols = protocols;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((host == null) ? 0 : host.hashCode());
      result = prime * result + ((port == null) ? 0 : port.hashCode());
      result = prime * result + ((protocols == null) ? 0 : protocols.hashCode());
      result = prime * result + ((user == null) ? 0 : user.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (getClass() != obj.getClass())
         return false;
      PSProxyConfig other = (PSProxyConfig) obj;
      if (host == null)
      {
         if (other.host != null)
            return false;
      }
      else if (!host.equals(other.host))
         return false;
      if (password == null)
      {
         if (other.password != null)
            return false;
      }

      if (port == null)
      {
         if (other.port != null)
            return false;
      }
      else if (!port.equals(other.port))
         return false;
      if (user == null)
      {
         if (other.user != null)
            return false;
      }
      else if (!user.equals(other.user))
         return false;
      if (protocols == null)
      {
         if (other.protocols != null)
            return false;
      }
      else if (protocols.size() != other.protocols.size() || !protocols.containsAll(other.protocols))
         return false;

      return true;
   }
   
   
}
