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

package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages saving and retrieving <code>PSIdMap</code> objects to and from
 * memory.
 */
public class PSIdMapManager
{
   /**
    * Constructs the object.
    */
   public PSIdMapManager()
   {
   }

   /**
    * Get the ID Map of the <code>sourceServer</code> from memory.
    *
    * @param sourceServer The string used to identify the source repository.
    * It may not be <code>null</code> or empty.
    *
    * @return The <code>PSIdMap</code> for the <code>sourceServer</code>, it
    * will never be <code>null</code>, but the <code>PSIdMap</code> may not have
    * any <code>PSIdMapping</code> objects. The object will not have any
    * <code>PSIdMapping</code> objects if it does not exist in memory.
    */
   public PSIdMap getIdmap(String sourceServer)
   {
      if ( sourceServer == null || sourceServer.trim().length() == 0 )
         throw new IllegalArgumentException("map may not be null or empty");

      PSIdMap idmapResult = m_repToIdMap.get(sourceServer);
      
      return idmapResult != null ? idmapResult : new PSIdMap(sourceServer);
   }

   /**
    * Save the a <code>PSIdMap</code> object into memory.  If an id map exists
    * for the source server, it will be replaced by the new id map. 
    *
    * @param map The <code>PSIdMap</code> object to be saved into memory.
    * It may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>map</code> is <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public void saveIdMap(PSIdMap map) throws PSDeployException
   {
      if ( map == null )
         throw new IllegalArgumentException("map may not be null");

      validateSavedIdMap(map);

      m_repToIdMap.put(map.getSourceServer(), map);
   }

   /**
    * Validating the given <code>PSIdMap</code> object, which will be saved
    * to memory.
    *
    * @param map The to be validated <code>PSIdMap</code> object. Assuming
    * it is not <code>null</code>.
    *
    * @throws PSDeployException if the given <code>PSIdMap</code> object is not
    * in the saved state.
    */
   private void validateSavedIdMap(PSIdMap map) throws PSDeployException
   {
      Iterator mappingList = map.getMappings();
      while (mappingList.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping) mappingList.next();
         if ((mapping.getTargetId() == null) && (!mapping.isNewObject()))
         {
           Object[] args = {map.getSourceServer() , mapping.getSourceId(),
               mapping.getSourceName()};
            throw new PSDeployException(
               IPSDeploymentErrors.INVALID_SAVED_ID_MAP, args);
         }
      }
   }

   /**
    * The map of id maps by source repository, with source repository as key
    * (<code>String</code>) and the <code>PSIdMap</code> as value.  Initialized
    * to an empty map and entries get added/updated by calls to
    * {@link #saveIdMap(PSIdMap)}. Never <code>null</code>.
    */
   private Map<String, PSIdMap> m_repToIdMap =
      new HashMap<>();
   
}
