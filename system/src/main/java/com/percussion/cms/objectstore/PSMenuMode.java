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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import org.w3c.dom.Element;


/**
 * This class provides an object representation of the Mode concept for menus.
 * This is a read-only class. It can only be instantiated from xml.
 * <p>It doesn't need to override the clone methods because all its members
 * are immutable.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSMenuMode extends PSName
{
   /**
    * Since this object is read-only, it can only be instantiated from an
    * existing object, obtained from the processor load method.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException See fromXml();
    */
   public PSMenuMode(Element src)
      throws PSUnknownNodeTypeException
   {
      super(getKeyDef(-1));
      fromXml(src);
   }

   /**
    * This constructor should only be used for testing and debugging 
    * purposes.
    * @param id
    * @param name
    * @param dname
    * @param desc
    */
   public PSMenuMode(int id, String name, String dname, String desc)
   {      
      super(getKeyDef(id), name, dname, desc);
   }   

   /**
    * Gets the menu mode id (which is saved in the repository) from a
    * GUID object.
    * 
    * @param guid the guid object, which must be a {@link PSTypeEnum#MENU_MODE}
    *    type.
    * 
    * @return the UUID of the guid.
    */
   public static int getIdFromGuid(IPSGuid guid)
   {
      if (guid.getType() != PSTypeEnum.MENU_MODE.getOrdinal())
         throw new IllegalArgumentException(
               "guid must be PSTypeEnum.MENU_MODE type.");
      
      return (int) guid.getUUID();
   }
      
   /**
    * Creates a GUID from an id.
    * 
    * @param id the menu mode id, which is saved in the repository.
    * 
    * @return the created GUID, never <code>null</code>.
    */
   public static PSDesignGuid getGuidFromId(int id)
   {
      return new PSDesignGuid( new PSGuid(PSTypeEnum.MENU_MODE, id) );
   }
   
   /**
    * Creates a key containing the proper definition for this object.
    *
    * @return Never <code>null</code>.
    */
   private static PSKey getKeyDef(int id)
   {
      PSKey key = id > 0
      ? new PSKey(new String[] {PRIMARY_KEY}, new int[]{id},  false)
         : new PSKey(new String[] {PRIMARY_KEY}, false);
      return key;
   }

   /**
    * The name of the table column that is the primary key.
    */
   static final String PRIMARY_KEY = "MODEID";
}
