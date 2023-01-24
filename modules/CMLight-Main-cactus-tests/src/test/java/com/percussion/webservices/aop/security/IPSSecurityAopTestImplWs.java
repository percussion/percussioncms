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

