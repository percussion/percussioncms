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
package com.percussion.webservices.system.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Common implementations used with the public and private system 
 * webservices.
 */
public class PSSystemBaseWs
{

   protected static final Logger log = LogManager.getLogger(IPSConstants.WEBSERVICES_LOG);

   @PersistenceContext
   private EntityManager entityManager;

   protected Session getSession(){
      return entityManager.unwrap(Session.class);
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
         log.error(PSExceptionUtils.getMessageForLog(e));
         int code = IPSWebserviceErrors.FAILED_LOAD_REL_CONFIGS;
         throw new PSErrorException(code, PSWebserviceErrors
               .createErrorMessage(code,PSExceptionUtils.getMessageForLog(e)),
                 PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

}

