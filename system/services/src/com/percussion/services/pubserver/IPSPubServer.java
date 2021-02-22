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
package com.percussion.services.pubserver;

import com.percussion.services.pubserver.data.PSPubServerProperty;
import com.percussion.services.pubserver.impl.PSPubServerDao;
import com.percussion.utils.guid.IPSGuid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a publishing server. The server manager performs all CRUD
 * operations on server objects.
 * 
 * @author leonardohildt
 */
public interface IPSPubServer
{
   
   /**
    * The publishing type. Used to indicate which mechanism to be used to
    * publish to the live site.
    */
   public enum PublishType{
       /**
        * Publishing defaults to local
        */
       filesystem,
       /**
        * Publishing will be done via FTP
        */
       ftp,
       /**
        * Publishing will be done via SFTP
        */
       sftp,
       /**
        * publishing will be done to database
        */
       database;
    }
   
   /**
    * Get the unique id for the server.
    * 
    * @return the guid, never <code>null</code>
    */
   IPSGuid getGUID();

   /**
    * Get the server id for this server.
    * 
    * @return the server id, never <code>null</code> or empty.
    */
   long getServerId();

   /**
    * Set the server id.
    * 
    * @param serverId the server id to set
    */
   void setServerId(long serverId);

   /**
    * The server name, never <code>null</code> or empty.
    * 
    * @return Returns the server name.
    */
   String getName();

   /**
    * @param name The name to set, never <code>null</code> or empty
    */
   void setName(String name);

   /**
    * Get the description that describes this server.
    * 
    * @return the description, can be <code>null</code> or empty.
    */
   String getDescription();

   /**
    * Set the description.
    * 
    * @param description the description to set
    */
   void setDescription(String description);

   /**
    * Get the publish type for the server.
    * 
    * @return the publish type, never <code>null</code> or empty.
    */
   String getPublishType();

   /**
    * Set the publish type for this server.
    * 
    * @param publishType the publish type to set
    */
   void setPublishType(String publishType);

   Set<PSPubServerProperty> getProperties();
   
   /**
    * Retrieves the property from this server that has the given name. It
    * returns <code>null</code> if no property was found with that name. The
    * comparison is made ignoring the letter case.
    * 
    * @param propertyName {@link String} can be blank, in which case it will
    *           return <code>null</code>.
    * @return {@link PSPubServerProperty} object if the property was found,
    *         <code>null</code> otherwise.
    */
   PSPubServerProperty getProperty(String propertyName);

   /**
    * Returns the property value without being decoded (see
    * {@link #getDecodedPropertyValue(String)}), or <code>null</code> if that is
    * the property value, or the property does not exist.
    * 
    * @param propertyName {@link String} can be blank, in which case it will
    *           return <code>null</code>.
    * @return {@link String} (that may be <code>null</code>) with the property
    *         value if it exists, or <code>null</code> if it could not be found.
    */
   String getPropertyValue(String propertyName);

   /**
    * Similar to {@link #getPropertyValue(String)} but in case that the returned
    * value is <code>null</code>, the default value is returned.
    * 
    * @param propertyName {@link String} can be blank, in which case it will
    *           return the default value.
    * @param defaultValue {@link String} can be blank, it will be returned if
    *           the property does not exists or its value is blank.
    * @return {@link String} that may be <code>null</code>.
    */
   String getPropertyValue(String propertyName, String defaultValue);

   /**
    * Helper method to find out if the server is ment to publish in XML format
    * or not.
    * 
    * @return <code>true</code> if the server is ment to publish in XML format.
    *         <code>false</code> otherwise.
    */
   boolean isXmlFormat();
   
   /**
    * Helper method to find out if the server is ment to publish to a database
    * or not.
    * 
    * @return <code>true</code> if the server is ment to publish to database.
    *         <code>false</code> otherwise.
    */
   boolean isDatabaseType();
   
   /**
    * Helper method to determine if the server is publishing to ftp or sftp
    * 
    * @return <code>true</code> if ftp, <code>false</code> if not.
    */
   public boolean isFtpType();
   
   /**
    * The site Id
    * 
    * @return Returns the site id, never <code>null</code>
    */
   public long getSiteId();

   /**
    * Property names to be encoded
    */
   static String[] encodedPropertyNames = {PSPubServerDao.PUBLISH_PASSWORD_PROPERTY};

   static final Set<String> encodedPropertyNamesList = new HashSet<>(
         Arrays.asList(encodedPropertyNames));

   String getServerType();
   void setServerType(String serverType);

   /**
    * Determine if this server has been full published since created or configuration has changed.
    * 
    * @return <code>true</code> if it has been full published, <code>false</code> if not.
    */
   boolean hasFullPublished();
   
   /**
    * Set if this server has been full published since created or configuration has changed.
    * 
    * @param hasFullPublished <code>true</code> if it has been full published, <code>false</code> if not.
    */
   void setHasFullPublisehd(boolean hasFullPublished);

}
