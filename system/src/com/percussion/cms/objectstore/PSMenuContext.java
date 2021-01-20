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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import org.w3c.dom.Element;


/**
 * This class provides an object representation of the Context concept for
 * menus. A menu context identifies a logical location within the UI where
 * menus may be displayed.
 * This is a read-only class. It can only be instantiated from xml.
 * <p>It doesn't need to override the clone methods because all its members
 * are immutable.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSMenuContext extends PSName
{
   /**
    * Since this object is read-only, it can only be instantiated from an
    * existing object, obtained from the processor load method.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException See fromXml();
    */
   public PSMenuContext(Element src)
      throws PSUnknownNodeTypeException
   {
      super(getKeyDef(-1));
      fromXml(src);
   }


   /**
    * This ctor should only be used for testing and debugging purposes.
    * @param name
    * @param dname
    * @param desc
    */
   public PSMenuContext(int id, String name, String dname, String desc)
   {
      super(getKeyDef(id), name, dname, desc);
   }

   /**
    * Gets the menu context id (which is saved in the repository) from a
    * GUID object.
    * 
    * @param guid the guid object, which must be a {@link PSTypeEnum#MENU_MODE}
    *    type.
    * 
    * @return the UUID of the guid.
    */
   public static int getIdFromGuid(IPSGuid guid)
   {
      if (guid.getType() != PSTypeEnum.MENU_CONTEXT.getOrdinal())
         throw new IllegalArgumentException(
               "guid must be PSTypeEnum.MENU_CONTEXT type.");
      
      return (int) guid.getUUID();
   }
      
   /**
    * Creates a GUID from an id.
    * 
    * @param id the menu context id, which is saved in the repository.
    * 
    * @return the created GUID, never <code>null</code>.
    */
   public static PSDesignGuid getGuidFromId(int id)
   {
      return new PSDesignGuid(new PSGuid(PSTypeEnum.MENU_CONTEXT, id));
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
   static final String PRIMARY_KEY = "UICONTEXTID";
}
