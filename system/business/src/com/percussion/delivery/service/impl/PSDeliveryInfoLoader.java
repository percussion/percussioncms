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

package com.percussion.delivery.service.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.impl.DeliveryServer.Password;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class responsible for loading the delivery servers configuration
 * file. It encrypts any plain-text password during initial load.
 * 
 * @author miltonpividori
 *
 */
public class PSDeliveryInfoLoader
{

    /**
     * Logger for this class
     */
    public static Log log = LogFactory.getLog(PSDeliveryInfoLoader.class);

    /**
     * Delivery servers list.
     */
    private List<PSDeliveryInfo> deliveryServers;

    /**
     * Reads the file and encrypts and saves any plain-text password.
     * 
     * @param configFile Configuration file with delivery servers. It does
     *            nothing if it's <code>null</code>.
    * @throws PSServerConfigException 
     */
    public PSDeliveryInfoLoader(File configFile) throws PSServerConfigException
    {
        notNull(configFile);
        
        deliveryServers = new ArrayList<>();

        if (configFile.exists())
            readAndEncryptConfigFile(configFile);
    }

    /**
     * If the configuration file exists, read every delivery server added. Loads
     * every password.
     * 
     * @param configFile the delivery server configuration file.
    * @throws PSServerConfigException 
     */
    private void readAndEncryptConfigFile(File configFile) throws PSServerConfigException
    {
        DeliveryServerConfig config = getDeliveryServerConfig(configFile);
        PSDeliveryInfo serverInfo;
        boolean configChanged = false;
        
        for (DeliveryServer s : config.getDeliveryServer())
        {
            log.debug("Delivery server: " + s.getConnectionUrl());
            
            serverInfo = convertServerToInfo(s);
            addDeliveryServer(serverInfo);
            Password pwd = s.getPassword();
            String pwdVal = pwd.getValue();
            if (pwd.isEncrypted())
            {
                String decryptedPassword = "";
                try{
                    decryptedPassword = PSEncryptor.getInstance("AES",
                            PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                    ).decrypt(pwdVal);
                }catch(Exception e){
                     decryptedPassword = PSLegacyEncrypter.getInstance(
                             PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                     ).decrypt(pwdVal, PSLegacyEncrypter.getInstance(
                             PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                     ).getPartOneKey(),null);
                }
                serverInfo.setPassword(decryptedPassword);
                
                continue;
            }

            String enc = "";
            try {
                enc = PSEncryptor.getInstance("AES",
                        PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                ).encrypt(pwdVal);
            } catch (PSEncryptionException e) {
                log.error("Error encrypting password: " + e.getMessage(),e);
            }
            pwd.setValue(enc);
            pwd.setEncrypted(Boolean.TRUE);
            configChanged = true;
        }

        if (configChanged)
        {
            // Update config file with encrypted passwords
            FileWriter fileWriter = null;
            BufferedWriter bfWriter = null;
            try
            {
                fileWriter = new FileWriter(configFile);
                bfWriter = new BufferedWriter(fileWriter);
                bfWriter.write(PSSerializerUtils.marshal(config));
            }
            catch (IOException e)
            {
                log.error("Error writing the delivery servers file: " +
                        e.getMessage());
            }
            finally
            {
                IOUtils.closeQuietly(bfWriter);
                IOUtils.closeQuietly(fileWriter);
            }
        }
    }

    /**
     * Gets the configured delivery servers.
     * 
     * @return the delivery servers, never <code>null</code>, may be empty.
     */
    public List<PSDeliveryInfo> getDeliveryServers()
    {
        return deliveryServers;
    }
    
    /**
     * Add the deliver server info to deliveryServers
     * Check to make sure each server is identifiable 
     * If two servers appear to share the same role (by
     * comparison of serverType) than a server configuration
     * error is thrown.
     * @param serverInfo
     * @throws PSServerConfigException
     */
    private void addDeliveryServer(PSDeliveryInfo serverInfo) throws PSServerConfigException
    {
       if(!serverInfo.getAvailableServices().contains("perc-thirdparty-services"))
       {
          //if server is of type null then treat it as production
          String serverType = StringUtils.isBlank(serverInfo.getServerType())?com.percussion.services.pubserver.data.PSPubServer.PRODUCTION.toString():serverInfo.getServerType();
          serverInfo.setServerType(serverType);
          
          //check if we have a duplicate server
        /*  for(PSDeliveryInfo server : deliveryServers)
          {
             if(serverType.equalsIgnoreCase(server.getServerType())
                   && !server.getAvailableServices().contains("perc-thirdparty-services"))
             {
                String message = "Could not identify delivery servers - Must include unique <server-type> identifier"
                      + " in delivery-servers.xml";
                PSServerConfigException exception = new PSServerConfigException(IPSDeliveryErrors.BAD_DELIVERY_SERVER_CONFIGURATION, message);
                log.error(message,exception);
                throw exception;
             }
          }*/
       } 
       deliveryServers.add(serverInfo);
    }

    /**
     * Reads the configuration file and returns a DeliveryServerConfig.
     * 
     * @param configFile the configure file, assumed not <code>null</code>.
     * 
     * @return main configuration object. Never <code>null</code>.
     */
    private DeliveryServerConfig getDeliveryServerConfig(File configFile)
    {



        try(InputStream in = new FileInputStream(configFile)){
            DeliveryServerConfig config = PSSerializerUtils.unmarshalWithValidation(in, DeliveryServerConfig.class);
            return config;
        }
        catch (Exception e)
        {
           String msg = "Unknown Exception";
           Throwable cause = e.getCause();
           if(cause != null && StringUtils.isNotBlank(cause.getLocalizedMessage()))
           {
              msg = cause.getLocalizedMessage();
           }
           else if(StringUtils.isNotBlank(e.getLocalizedMessage()))
           {
              msg = e.getLocalizedMessage();
           }
           log.error("Error getting delivery servers from configuration file: " +  msg,e);
           return new DeliveryServerConfig();
        }
    }

    /**
     * Converts the specified server to {@link PSDeliveryInfo}
     * 
     * @param server the server, assumed not <code>null</code>.
     * 
     * @return the converted server info, never <code>null</code>.
     */
    private PSDeliveryInfo convertServerToInfo(DeliveryServer server)
    {
        PSDeliveryInfo info = new PSDeliveryInfo(server.getConnectionUrl());
        
        if (StringUtils.isNotBlank(server.getUser()))
        {
            info.setUsername(server.getUser());
            info.setPassword(server.getPassword().getValue());
            info.setAdminUrl(server.getAdminConnectionUrl());
            info.setAllowSelfSignedCertificate((server.getAllowSelfSignedCertificate()));
            info.setServerType(server.getServerType());
        }
        
        try
        {
            info.setAvailableServices((ArrayList)server.getAvailableServices().get(0).getService());
        }
        catch (IndexOutOfBoundsException e)
        {
            info.setAvailableServices(new ArrayList());
        }
        
        return info;
    }
}
