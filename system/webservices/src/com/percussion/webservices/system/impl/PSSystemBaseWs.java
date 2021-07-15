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
package com.percussion.webservices.system.impl;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.error.PSException;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Common implementations used with the public and private system 
 * webservices.
 */
public class PSSystemBaseWs
{

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /**
    * Loads the (cached) relationship config set.
    * 
    * @return the relationship config set, never <code>null</code>.
    * @throws PSErrorException if failed to load.
    */
   protected PSRelationshipConfigSet getRelationshipConfigSet()
      throws PSErrorException
   {
      // make sure the config is cached
      try
      {
         PSRelationshipCommandHandler.loadConfigs();
         // access the cached config
         return PSRelationshipCommandHandler.getConfigurationSet();
      }
      catch (PSException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_LOAD_REL_CONFIGS;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
               .createErrorMessage(code, e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

}

