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

/**
 * 
 */
package com.percussion.utils;

import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.integritymanagement.data.PSIntegrityTask.TaskStatus;
import com.percussion.utils.types.PSPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 * Check and report on the health status of the DTS and all of its services
 * 
 * @author robertjohansen
 * 
 */
@Component("dtsStatusProvider")
public class PSDTSStatusProvider
{
    //Root of server
    private String serverRoot;

    //external services - services external from CM1 (List - in case more need to be added)
    
    private final Map<String,String> externalServices = new HashMap<String,String>(){{
        put("perc-polls-services","/perc-polls-services/polls/version");
    }};
    
    private final Map<String,String> services = new HashMap<String,String>() {{
        put(PSDeliveryInfo.SERVICE_FEEDS, "/feeds/rss/version");
        put(PSDeliveryInfo.SERVICE_INDEXER, "/perc-metadata-services/metadata/version");
        put(PSDeliveryInfo.SERVICE_COMMENTS,"/perc-comments-services/comment/version");
        put(PSDeliveryInfo.SERVICE_FORMS,"perc-form-processor/form/version");
        put(PSDeliveryInfo.SERVICE_MEMBERSHIP,"/perc-membership-services/membership/version");
    }};

    private IPSDeliveryInfoService deliveryService;

    private PSDeliveryClient deliveryClient;

    /**
     * Default constructor
     */
    @Autowired
    public PSDTSStatusProvider(IPSDeliveryInfoService service)
    {
        deliveryService = service;
        deliveryClient = new PSDeliveryClient();
        serverRoot = deliveryService.findBaseByServerType(null);
    }

    /**
     * Returns Health status of DTS and all services - No Services are
     * represented if DTS is not running Services are represented as key values
     * in a map with a PSPair representing Status (success or failed) in the
     * first element and the response message in the second element. The first
     * element of the PSPair will always represent Status.
     */
    public Map<String, PSPair<TaskStatus, String>> getDTSStatusReport()
    {
        Map<String, PSPair<TaskStatus, String>> statusReport = new HashMap<>();
        
        //Check the status of the DTS - if down return status of dts else continue checking services
        PSPair<Boolean, String> dtsPair = getExternalTomcatServiceStatus(serverRoot);
        if (!dtsPair.getFirst())
        {
            statusReport.put("dts", new PSPair<>(TaskStatus.FAILED, dtsPair.getSecond()));
            return statusReport;
        }
        statusReport.put("dts", new PSPair<>(TaskStatus.SUCCESS, dtsPair.getSecond()));
        
        //check the external services and add the status to the report
        for(Map.Entry<String, String> externalService : externalServices.entrySet())
        {
            if (!getExternalTomcatServiceStatus(serverRoot + externalService.getValue()).getFirst()) {
                statusReport.put(externalService.getKey(), new PSPair<>(TaskStatus.FAILED, dtsPair.getSecond()));
            }
            else {
                statusReport.put(externalService.getKey(), new PSPair<>(TaskStatus.SUCCESS, dtsPair.getSecond()));
            }
        }
        
        for (Map.Entry<String, String> entry : services.entrySet())
        {
            PSPair<TaskStatus,String> pair = getServiceStatus(entry.getKey(),entry.getValue());
            statusReport.put(entry.getKey(),pair);
        }
        
        return statusReport;
    }
     
    /**
     * Get the status of the specified service
     * @param service Name of service
     * @param serviceURL url from service to /version (ping url)
     * @return PSPair where first represents status and second is response message
     */
    private PSPair<TaskStatus,String> getServiceStatus(String service, String serviceURL)
    {
        try
        {
            PSDeliveryInfo server = deliveryService.findByService(service);
            String message = deliveryClient.getString(new PSDeliveryActionOptions(server, serviceURL,
                    HttpMethodType.GET, false));
            return new PSPair<>(TaskStatus.SUCCESS, message);
        }
        catch (RuntimeException e)
        {
            return new PSPair<>(TaskStatus.FAILED, e.getMessage());
        }
    }

    /**
     * Get the status of the external serivce
     * such as polls and or just root tomcat
     * @return PSPair<Status,Message>
     */
    private PSPair<Boolean, String> getExternalTomcatServiceStatus(String surl)
    {
        try
        {
            boolean alive = false;
            String response = "";
            URL url = new URL(surl);
            if(url.getProtocol().equalsIgnoreCase("https")){
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "*/*");
    
                response = conn.getResponseMessage();
                if(response.contains("OK")) {
                    alive = true;
                }
            }
            else{
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "*/*");
    
                response = conn.getResponseMessage();
                if(response.contains("OK")) {
                    alive = true;
                }
                
            }
            return new PSPair<>(alive, response);
        }
        catch (ConnectException e)
        {
            return new PSPair<>(false, e.getMessage());
        }
        catch (IOException e)
        {
            return new PSPair<>(false, e.getMessage());
        }
    }
}
