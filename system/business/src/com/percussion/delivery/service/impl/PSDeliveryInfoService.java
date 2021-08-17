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

package com.percussion.delivery.service.impl;

import com.percussion.cms.objectstore.PSSite;
import com.percussion.delivery.client.IPSDeliveryClient;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.delivery.service.PSDeliveryInfoServiceLocator;
import com.percussion.security.PSEncryptor;
import com.percussion.server.PSServer;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteHelper;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.io.PathUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author peterfrontiero
 *
 */
public class PSDeliveryInfoService implements IPSDeliveryInfoService
{

    private static final Logger log = LogManager.getLogger(PSDeliveryInfoService.class);

    public static final String DTS_CONFIG_FILENAME="rxconfig/DeliveryServer/delivery-servers.xml";

    /**
     * The configuration file path, never <code>null</code>.
     */
    private static File CONFIG_FILE = new File(PSServer.getRxDir(),
            DTS_CONFIG_FILENAME);

    private boolean isConfigured=false;

    /**
     * @return the isConfigured
     */
    public boolean isConfigured()
    {
        return isConfigured;
    }

    /**
     * @param isConfigured the isConfigured to set
     */
    public void setConfigured(boolean isConfigured)
    {
        this.isConfigured = isConfigured;
    }

    /**
     * A list of delivery servers specified in the configure file.
     */
    private List<PSDeliveryInfo> servers = new ArrayList<>();

    public PSDeliveryInfoService() throws PSServerConfigException
    {
        if (!CONFIG_FILE.exists()){
            log.warn("DTS configuration file not found at :" + DTS_CONFIG_FILENAME + " skipping DTS configuration.");
            isConfigured = false;
            return;
        }


        PSDeliveryInfoLoader deliveryInfoLoader = new PSDeliveryInfoLoader(CONFIG_FILE);
        servers = deliveryInfoLoader.getDeliveryServers();
        isConfigured = true;
    }

    /*
     *  This constructor is for JUnit Testing purposes
     */
    public PSDeliveryInfoService(File file) throws PSServerConfigException
    {
        PSDeliveryInfoLoader deliveryInfoLoader = new PSDeliveryInfoLoader(file);
        servers = deliveryInfoLoader.getDeliveryServers();
    }

    /* (non-Javadoc)
     * @see com.percussion.delivery.service.IPSDeliveryService#findAll()
     */
    public List<PSDeliveryInfo> findAll()
    {
        return servers;
    }

    /**
     * find delivery server by server type
     * return first server found with type
     * if none found return null
     */
    public String findBaseByServerType(String type)
    {


        String baseUrl = "";
        if(StringUtils.isBlank(type))
        {
            type = PSPubServer.PRODUCTION;
        }
        PSDeliveryInfo inf = findServerByType(type);
        if(inf != null)
        {
            baseUrl = inf.getUrl();
        }
        return baseUrl;
    }

    private PSDeliveryInfo findServerByType(String type)
    {
        PSDeliveryInfo delInfo = null;
        if(StringUtils.isBlank(type))
        {
            type = PSPubServer.PRODUCTION;
        }
        for (PSDeliveryInfo inf : servers)
        {
            String dtype = StringUtils.isBlank(inf.getServerType())?PSPubServer.PRODUCTION:inf.getServerType();
            //If the servertype is blank then treats it as PRODUCTION type
            if (dtype.equalsIgnoreCase(type))
            {
                delInfo = inf;
                break;
            }
        }
        return delInfo;
    }
    public static void copySecureKeyToDeliveryServer(IPSGuid edition) throws PSNotFoundException {
        if(edition != null) {
            IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
            IPSEdition editionObject = pubService.loadEdition(edition);
            PSPubServer pubServer = PSPubServerDaoLocator.getPubServerManager()
                    .loadPubServer(editionObject.getPubServerId());
        }

        PSDeliveryInfoService psDeliveryInfoService = (PSDeliveryInfoService) PSDeliveryInfoServiceLocator.getDeliveryInfoService();
        List<PSDeliveryInfo> psDeliveryInfoServiceList = psDeliveryInfoService.findAll();
        String secureKey= getSecureKey();
        if(secureKey == null){
            return;
        }
        //TODO: Sony Do Copy only for passed in ServerID
        for(PSDeliveryInfo info : psDeliveryInfoServiceList) {
            if (info.getAvailableServices().contains(PSDeliveryInfo.SERVICE_FEEDS)) {
                PSDeliveryClient deliveryClient = new PSDeliveryClient();
                try {
                    Set<Integer> successfullHttpStatusCodes = new HashSet<>();
                    successfullHttpStatusCodes.add(204);
                    deliveryClient.push(
                            new IPSDeliveryClient.PSDeliveryActionOptions()
                                    .setActionUrl("/feeds/rss/rotateKey")
                                    .setDeliveryInfo(info)
                                    .setHttpMethod(IPSDeliveryClient.HttpMethodType.PUT)
                                    .setSuccessfullHttpStatusCodes(successfullHttpStatusCodes)
                                    .setAdminOperation(true),
                            secureKey);
                    log.info("Updated security key pushed to DTS server: " + info.getAdminUrl());
                } catch (Exception ex) {
                    log.warn("Unable to push updated security key to DTS server:{} ",info.getAdminUrl());
                    log.debug( "Unable to push updated security key to DTS server:{}  ERROR: {} ",info.getAdminUrl(), ex.getMessage(),ex);
                }
            }
        }
    }

    private static String getSecureKey() {

        String keyLocation = PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR);
        String SECURE_KEY_FILE = ".key";
        String keyStr = null;
        Path secureKeyFile = Paths.get(keyLocation + SECURE_KEY_FILE);
        if (Files.exists(secureKeyFile)) {
            //load key
            try {
                byte[] key = Files.readAllBytes(secureKeyFile);
                keyStr = Base64.getEncoder().encodeToString(key);
            } catch (IOException e) {
                log.error("Error reading instance secure key file");
                log.debug(e);
            }
        }
        return keyStr;
    }
    /* (non-Javadoc)
     * @see com.percussion.delivery.service.IPSDeliveryService#findByService()
     */
    public PSDeliveryInfo findByService(String service)
    {
        return findByService(service,null);
    }

    public PSDeliveryInfo findByService(String service, String type)
    {
        PSDeliveryInfo delInfo = null;
        if(StringUtils.isBlank(type))
        {
            type = service.equalsIgnoreCase("perc-thirdparty-services")?
                    PSPubServer.LICENSE:PSPubServer.PRODUCTION;
        }
        for (PSDeliveryInfo inf : servers)
        {
            String dtype = inf.getServerType();

            //If the servertype is blank then treats it as LICENSE if service is license service
            //else assume servertype PRODUCTION
            if(StringUtils.isBlank(dtype))
            {
                dtype = service.equalsIgnoreCase("perc-thirdparty-services")?
                        PSPubServer.LICENSE:PSPubServer.PRODUCTION;
            }

            if (inf.getAvailableServices().contains(service) && dtype.equalsIgnoreCase(type))
            {
                delInfo = inf;
                break;
            }
        }
        return delInfo;
    }



    public PSDeliveryInfo findByService(String service, String type,String adminURL){

        PSDeliveryInfo delInfo = null;
        if(StringUtils.isBlank(type))
        {
            type = service.equalsIgnoreCase("perc-thirdparty-services")?
                    PSPubServer.LICENSE:PSPubServer.PRODUCTION;
        }
        for (PSDeliveryInfo inf : servers)
        {
            String dtype = inf.getServerType();

            //If the servertype is blank then treats it as LICENSE if service is license service
            //else assume servertype PRODUCTION
            if(StringUtils.isBlank(dtype))
            {
                dtype = service.equalsIgnoreCase("perc-thirdparty-services")?
                        PSPubServer.LICENSE:PSPubServer.PRODUCTION;
            }

            if(adminURL!=null){
                if (inf.getAvailableServices().contains(service) && dtype.equalsIgnoreCase(type) && inf.getAdminUrl().equalsIgnoreCase(adminURL))
                {
                    delInfo = inf;
                    break;
                }
            }else{
                if (inf.getAvailableServices().contains(service) && dtype.equalsIgnoreCase(type))
                {
                    delInfo = inf;
                    break;
                }
            }

        }
        return delInfo;
    }



    public PSDeliveryInfo findByURL(String s)
            throws MalformedURLException
    {
        PSDeliveryInfo psdeliveryinfo = null;
        URL url = new URL(s);
        String as[] = url.getPath().split("/");
        if(as.length > 0)
            psdeliveryinfo = findByService(as[0]);
        return psdeliveryinfo;
    }

}
