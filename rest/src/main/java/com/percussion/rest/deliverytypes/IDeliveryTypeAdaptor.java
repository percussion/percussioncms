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

package com.percussion.rest.deliverytypes;

import com.percussion.rest.errors.BackendException;

import java.net.URI;
import java.util.List;


public interface IDeliveryTypeAdaptor {

	 /***
	  * Gets a delivery type by id
	  * @param baseURI
	  * @param id
	  * @return
	  */
	 public DeliveryType getDeliveryTypeById(URI baseURI, String id) throws BackendException;
	 
	 /***
	  * Creates or updates a delivery type
	  * @param baseURI
	  * @param type
	  * @return
	  */
	 public DeliveryType updateDeliveryType(URI baseURI,DeliveryType type) throws BackendException;
	 
	 /***
	  * Deletes a delivery type
	  * @param baseURI
	  * @param id
	  * @return
	  */
	 public void deleteDeliveryTypeById(URI baseURI,String id) throws BackendException;
	 
	 /***
	  * Get the list of DeliveryTypes available on the system.
	  * @param baseURI
	  * @return A list of available Delivery Types.
	  */
	 public List<DeliveryType> getDeliveryTypes(URI baseURI);
	 

}
