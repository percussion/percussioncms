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
package com.percussion.webservices.aop.security;

import com.percussion.webservices.aop.security.data.PSMockDesignObject;
import com.percussion.webservices.aop.security.strategy.PSTestSecurityStrategy;

import java.util.List;

/**
 * A mock webservice service manager to test the public method patterns for AOP 
 * based security processing.
 */
public interface IPSSecurityAopTestImplWs
{
   /**
    * Attempts to load all design objects specified by 
    * {@link PSSecurityAopTest#getTestAcls()}
    * 
    * @param name a placeholder argument, may be <code>null</code>.
    * 
    * @return The list of objects with guids specified by that acl, never 
    * <code>null</code>.
    */
   public List<PSMockDesignObject> loadDesignObjects(String name);
   
   /**
    * Attempts to load the first design object specified by 
    * {@link PSSecurityAopTest#getTestAcls()}
    * 
    * @return An object with the guid specified by that acl, never 
    * <code>null</code>.
    */
   public PSMockDesignObject loadDesignObject();
   
   /**
    * Returns all design objects specified by 
    * {@link PSSecurityAopTest#getTestAcls()}, used to test that public find
    * results aren't filtered.
    *  
    * @param name Placeholder arg, if <code>null</code>, a runtime exception is
    * thrown, otherwise the list is returned unmodified.
    * 
    * @return The list, never <code>null</code>.
    */
   public List<PSMockDesignObject> findPublicObjects(String name);
   
   /**
    * A noop method used to test that public save methods aren't protected.
    * 
    * @param name Placeholder arg, should not be <code>null</code>.
    */
   public void savePublicObjects(String name);
   
   /**
    * A noop method used to test that public delete methods aren't protected.
    * 
    * @param name Placeholder arg, should not be <code>null</code>.
    */
   public void deletePublicObjects(String name);   
   
   /**
    * Attempts to load the first design object specified by 
    * {@link PSSecurityAopTest#getTestAcls()}
    * 
    * @return An object with the guid specified by that acl, never 
    * <code>null</code>.
    */
   @IPSWsMethod(ignore=true)
   public PSMockDesignObject loadDesignObjectIgnore();
   
   /**
    * Same as {@link #findPublicObjects(String)} but with custom strategy.
    *  
    * @param name Placeholder arg, if <code>null</code>, a runtime exception is
    * thrown, otherwise the list is returned unmodified.
    * 
    * @return The list, never <code>null</code>.
    */
   @IPSWsStrategy(PSTestSecurityStrategy.class)
   public List<PSMockDesignObject> findPublicObjectsCustom(String name);    
}

