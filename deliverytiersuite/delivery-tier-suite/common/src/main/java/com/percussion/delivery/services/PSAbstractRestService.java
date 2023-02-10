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
package com.percussion.delivery.services;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.percussion.delivery.utils.PSVersionHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a base class for Delivery REST services to extend from 
 * to handle common and shared methods between the services. 
 * 
 * @author natechadwick
 *
 */
public abstract class PSAbstractRestService implements IPSRestService {
	
	private final Logger log = LogManager.getLogger(this.getClass());

	/** 
	 * @see com.percussion.delivery.services.IPSRestService#getVersion()
	 */
	@Override
	public String getVersion() {
		try
	      {
			log.info("Version from PSAbstractRestService.class : {}" , PSVersionHelper.getVersion(this.getClass()));
			
			return PSVersionHelper.getVersion(this.getClass());
			
	      }catch (Exception e){
		         throw new WebApplicationException(e, Response.serverError().build());
		  }
	}

}
