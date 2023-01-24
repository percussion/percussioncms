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

package com.percussion.services.publisher;


/**
 * Represents a publisher from the database. A publisher specifies the
 * parameters that are need to communicate with a specific publishing hub.
 * 
 * @author dougrand
 */
public interface IPSPublisher
{
   /**
    * The unique identifier for each publisher in the database, aka the primary
    * key
    * 
    * @return the primary key value, never <code>null</code> for a persisted
    *         publisher
    */
   Integer getId();

   /**
    * Set a new identifier, illegal for a persisted instance
    * 
    * @param publisherid the new publisher id, never <code>null</code>
    */
   void setId(Integer publisherid);

   /**
    * The name of the publisher instance
    * 
    * @return the name of the publisher instance, may be <code>null</code> or
    *         empty
    */
   String getName();

   /**
    * Set the name
    * 
    * @param name the name, may be <code>null</code> or empty
    */
   void setName(String name);

   /**
    * The description of the publisher instance
    * @return the description of the publisher instance, may be <code>null</code> 
    * or empty
    */
   String getDescription();

   /**
    * Set the description
    * 
    * @param description the description, may be <code>null</code> or empty
    */
   void setDescription(String description);

   /**
    * Get the ip address
    * 
    * @return the ip address of the publishing hub, never <code>null</code> or
    * empty
    */
   String getIpAddress();

   /**
    * Set the ip address
    * 
    * @param ipaddress the new ip address, never <code>null</code> or empty
    */
   void setIpAddress(String ipaddress);

   /**
    * Get the port
    * 
    * @return the port for the publishing hub, never <code>null</code>
    */
   Integer getPort();

   /**
    * Set the port
    * 
    * @param port the port, never <code>null</code>
    */
   void setPort(Integer port);

   /**
    * The user id to use with the cms, used by the publishing hub when 
    * requesting the assembled output to publish
    * 
    * @return the user id never <code>null</code> or empty
    */
   String getUserId();

   /**
    * Set the user id
    * @param userid the user id, never <code>null</code> or empty
    */
   void setUserId(String userid);

   /**
    * The password to use with the cms, used by the publishing hub when
    * requesting the assembled output to publish
    * 
    * @return the password, never <code>null</code> or empty
    */
   String getPassword();

   /**
    * Set the password 
    * 
    * @param password the password, never <code>null</code> or empty
    */
   void setPassword(String password);

   /**
    * Get the publishing hub uid. If defined, this will be used when authenticating 
    * requests to the publishing hub.
    * @return the publishing hub uid, may be <code>null</code>
    */
   String getPubuid();
   
   /**
    * Set the publishing hub uid
    * @param pubuid the publishing hub uid, may be <code>null</code>
    */
   void setPubuid(String pubuid);

   /**
    * Get the publishing hub password. If defined, this will be used when authenticating
    * request to the publishing hub
    * @return the publishing hub password, may be <code>null</code>
    */
   String getPubpw();

   /**
    * Set the publishing hub password
    * @param pubpw the publishing hub password, may be <code>null</code>
    */
   void setPubpw(String pubpw);

}
