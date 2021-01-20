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
package com.percussion.rx.services.deployer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class represents a message.
 * @author bjoginipally
 *
 */
@XmlRootElement(name = "Message")
public class PSUninstallMessage
{
   public PSUninstallMessage()
   {
      
   }
   public PSUninstallMessage(String packageName,String type,String body)
   {
      setPackageName(packageName);
      setType(type);
      setBody(body);
   }
   /**
    * @return the packages
    */
   @XmlElement(name = "body")
   public String getBody()
   {
      return body;
   }
   public void setBody(String body)
   {
      this.body = body;
   }
   
   /**
    * @return the package
    */
   @XmlElement(name = "package")
   public String getPackageName()
   {
      return packageName;
   }
   public void setPackageName(String packageName)
   {
      if (packageName == null)
         throw new IllegalArgumentException("packageName must not be null");
      this.packageName = packageName;
   }
   
   /**
    * @return the type
    */
   @XmlElement(name = "type")
   public String getType()
   {
      return type;
   }
   public void setType(String type)
   {
      this.type = type;
   }
   

   private String type;
   private String body = "";
   private String packageName = "";
   
}
