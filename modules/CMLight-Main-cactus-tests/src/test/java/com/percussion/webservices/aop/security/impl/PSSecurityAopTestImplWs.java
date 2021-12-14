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
