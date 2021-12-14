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
