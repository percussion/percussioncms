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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * This class represents a limited version of design model.
 */
public class PSLimitedDesignModel extends PSDesignModel
{
   @Override
   public void delete(IPSGuid guid)
   {
      throw new UnsupportedOperationException("delete(IPSGuid) is not "
            + "currently implemented for design objects of type "
            + getType().name());
   }

   @Override
   public void delete(String name)
   {
      throw new UnsupportedOperationException(
            "delete(String) is not currently "
                  + "implemented for design objects of type "
                  + getType().name());
   }

   @Override
   public Object load(String name)
   {
      throw new UnsupportedOperationException("load(String) is not currently "
            + "implemented for design objects of type " + getType().name());
   }

   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      throw new UnsupportedOperationException(
            "loadModifiable(IPSGuid) is not currently "
                  + "implemented for design objects of type "
                  + getType().name());
   }

   @Override
   public Object loadModifiable(String name)
   {
      throw new UnsupportedOperationException(
            "loadModifiable(String) is not currently "
                  + "implemented for design objects of type "
                  + getType().name());
   }

   @Override
   public void save(Object obj)
   {
      throw new UnsupportedOperationException("save(Object) is not currently "
            + "implemented for design objects of type " + getType().name());
   }

   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      throw new UnsupportedOperationException(
            "save(Object, List<IPSAssociationSet>) is not currently "
                  + "implemented for design objects of type "
                  + getType().name());
   }

   @Override
   public IPSGuid nameToGuid(String name)
   {
      throw new UnsupportedOperationException("nameToGuid(String) is not "
            + "currently implemented for design objects of type "
            + getType().name());
   }

   @Override
   public String guidToName(IPSGuid guid)
   {
      throw new UnsupportedOperationException("guidToName(IPSGuid) is not "
            + "currently implemented for design objects of type "
            + getType().name());
   }

   @Override
   public List<IPSAssociationSet> getAssociationSets()
   {
      throw new UnsupportedOperationException("getAssociationSets() is not "
            + "currently implemented for design objects of type "
            + getType().name());
   }
}
