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

package com.percussion.apibridge;

import com.percussion.rest.deliverytypes.DeliveryType;
import com.percussion.rest.deliverytypes.IDeliveryTypeAdaptor;
import com.percussion.rest.errors.BackendException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSDeliveryType;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean
public class DeliveryTypeAdaptor implements IDeliveryTypeAdaptor {

    private IPSPublisherService pubService;
    private IPSGuidManager guidMgr;

    public DeliveryTypeAdaptor(){
        pubService = PSPublisherServiceLocator.getPublisherService();
        guidMgr = PSGuidManagerLocator.getGuidMgr();
    }
    /***
     * Gets a delivery type by id
     * @param baseURI
     * @param id
     * @return
     */
    @Override
    public DeliveryType getDeliveryTypeById(URI baseURI, String id) throws BackendException {
        try {
            PSGuid guid = new PSGuid(PSTypeEnum.DELIVERY_TYPE, id);
            IPSDeliveryType type = pubService.loadDeliveryType(guid);
            return copyDeliveryType(type);
        } catch (PSNotFoundException e) {
            throw new BackendException(e);
        }
    }

    /***
     * Creates or updates a delivery type
     * @param baseURI
     * @param type
     * @return
     */
    @Override
    public DeliveryType updateDeliveryType(URI baseURI, DeliveryType type) throws BackendException {
        try {
            if (type.getId() == null || StringUtils.isBlank(type.getId().getStringValue())) {
                //Create
                IPSDeliveryType create = pubService.createDeliveryType();

                create.setUnpublishingRequiresAssembly(type.getUnpublishingRequiresAssembly());
                create.setName(type.getName());
                create.setDescription(type.getDescription());
                create.setBeanName(type.getBeanName());
                pubService.saveDeliveryType(create);
                return copyDeliveryType(create);

            } else {
                IPSDeliveryType update = copyDeliveryType(type);

                //Update the type
                pubService.saveDeliveryType(update);

                //Load after save
                return copyDeliveryType(pubService.loadDeliveryType(update.getGUID()));
            }
        } catch (PSNotFoundException e) {
            throw new BackendException(e);
        }
    }

    /***
     * Deletes a delivery type
     * @param baseURI
     * @param id
     * @return
     */
    @Override
    public void deleteDeliveryTypeById(URI baseURI, String id) throws BackendException {
        try {
            IPSGuid guid = guidMgr.makeGuid(id, PSTypeEnum.DELIVERY_TYPE);
            IPSDeliveryType type = pubService.loadDeliveryType(guid);
            pubService.deleteDeliveryType(type);
        } catch (PSNotFoundException e) {
            throw new BackendException(e);
        }
    }

    /***
     * Get the list of DeliveryTypes available on the system.
     * @param baseURI
     * @return A list of available Delivery Types.
     */
    @Override
    public List<DeliveryType> getDeliveryTypes(URI baseURI) {
       List<IPSDeliveryType> types  = pubService.findAllDeliveryTypes();
       List<DeliveryType> response = new ArrayList<>();
       for(IPSDeliveryType t : types){
           response.add(copyDeliveryType(t));
       }
       return response;
    }

    private DeliveryType copyDeliveryType(IPSDeliveryType t) {
        DeliveryType ret = new DeliveryType();

        ret.setBeanName(t.getBeanName());
        ret.setName(t.getName());
        ret.setDescription(t.getDescription());
        ret.setId(ApiUtils.convertGuid(t.getGUID()));
        ret.setUnpublishingRequiresAssembly(t.isUnpublishingRequiresAssembly());
        return ret;
    }

    private IPSDeliveryType copyDeliveryType(DeliveryType type){
        IPSDeliveryType ret = new PSDeliveryType();

        ret.setBeanName(type.getBeanName());
        ret.setDescription(type.getDescription());
        ret.setName(type.getName());
        ret.setUnpublishingRequiresAssembly(type.getUnpublishingRequiresAssembly());

        ret.setGUID(ApiUtils.convertGuid(type.getId()));
        return ret;
    }
}
