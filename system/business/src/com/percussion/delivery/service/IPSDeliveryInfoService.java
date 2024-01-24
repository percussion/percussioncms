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
package com.percussion.delivery.service;

import com.percussion.delivery.data.PSDeliveryInfo;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author peterfrontiero
 *
 */
public interface IPSDeliveryInfoService
{
    /**
     * Finds all delivery info objects.
     *
     * @return a list containing all of the located delivery info objects, will
     * be empty if none were found. Never <code>null</code>.  The list will be sorted
     * alphabetically by label.
     */
    public List<PSDeliveryInfo> findAll();

    /**
     * Finds first server by server type
     * @param type
     * @return Delivery Server Info
     */
    public String findBaseByServerType(String type);

    /**
     * Finds first server by server type
     * @param type
     * @return Delivery Server Info
     */
    public String findBaseByServerName(String type);

    /**
     * Finds a server that run the specified service.
     * @param service the service type name. Cannot be <code>null</code>, or
     * empty.
     * Note, if there are more than one servers that run the specified service,
     * this will only return the 1st one it found, ignore the rest.
     *
     * @return the server found that run the specified service.
     * May be <code>null</code> if no server matches were found.
     */
    public PSDeliveryInfo findByService(String service);

    /**
     * This method is preferred to findByService(String service) as it
     * deciphers which delivery server to return instead of the first
     * matched by service.
     * @param service
     * @param type
     * @return Delivery Server Info
     */
    public PSDeliveryInfo findByService(String service, String type);

    public PSDeliveryInfo findByService(String service, String type,String adminURL);


    public PSDeliveryInfo findByURL(String s) throws MalformedURLException;
}
