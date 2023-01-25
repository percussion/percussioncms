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
