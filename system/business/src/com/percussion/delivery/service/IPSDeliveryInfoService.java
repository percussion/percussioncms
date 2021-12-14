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
