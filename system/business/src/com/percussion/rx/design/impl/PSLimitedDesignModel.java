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
