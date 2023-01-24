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
package com.percussion.services.publisher.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.publisher.IPSContentListGenerator;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.PSPublisherException;

import java.io.File;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;

/**
 * Content list generator based on JSR-170 queries
 * 
 * @author dougrand
 */
public class PSQueryContentListGenerator implements IPSContentListGenerator
{
   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSContentListGenerator#generate(java.util.Map)
    */
   public QueryResult generate(Map<String, String> parameters)
         throws PSPublisherException
   {
      if (parameters == null || parameters.size() == 0)
      {
         throw new IllegalArgumentException(
               "parameters may not be null or empty");
      }
      String query = parameters.get("query");
      if (StringUtils.isBlank(query))
      {
         throw new IllegalArgumentException("query is a required parameter");
      }
      IPSContentMgr cm = PSContentMgrLocator.getContentMgr();
      try
      {
         Query q = cm.createQuery(query, Query.SQL);
         return cm.executeQuery(q, -1, parameters, null);
      }
      catch (InvalidQueryException e)
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.BAD_QUERY, e,
               query);
      }
      catch (RepositoryException e)
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.REPOSITORY,
               e, e.getLocalizedMessage());
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // No init
   }

}
