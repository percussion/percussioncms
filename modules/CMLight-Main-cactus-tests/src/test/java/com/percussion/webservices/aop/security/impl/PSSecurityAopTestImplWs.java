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
package com.percussion.webservices.aop.security.impl;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.webservices.aop.security.IPSSecurityAopTestImplWs;
import com.percussion.webservices.aop.security.data.PSMockDesignObject;

import java.util.List;

/**
 * Concreate implementation of the AOP test service interfaces. 
 */
public class PSSecurityAopTestImplWs  
   extends  PSSecurityAopTestImplBase 
   implements IPSSecurityAopTestImplWs
{
   @Override
   public List<PSMockDesignObject> loadDesignObjects(String name)
   {
      return super.loadDesignObjects(name);
   }

   @Override
   public PSMockDesignObject loadDesignObject()
   {
      return super.loadDesignObject();
   }

   @Override
   public List<PSMockDesignObject> findPublicObjects(String name)
   {
      return super.findPublicObjects(name);
   }

   @Override
   public void savePublicObjects(String name)
   {
      super.savePublicObjects(name);
   }

   @Override
   public void deletePublicObjects(String name)
   {
      super.deletePublicObjects(name);
   }

   @Override
   public PSMockDesignObject loadDesignObjectIgnore()
   {
      return super.loadDesignObjectIgnore();
   }

   @Override
   public List<IPSCatalogSummary> findDesignObjectsPerm(String name)
   {
      return super.findDesignObjectsPerm(name);
   }

   @Override
   public List<PSMockDesignObject> findPublicObjectsCustom(String name)
   {
      return super.findPublicObjectsCustom(name);
   }   
}
