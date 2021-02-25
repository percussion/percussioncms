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
