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
package com.percussion.i18n.tmxdom;

/**
 * This interface defines common methods for the TMX node 'prop'. Refer to the
 * TMX 1.4 DTD for more details of this node at:
 * <p>
 * <a href="http://www.lisa.org/tmx/">Localisation Industry Standards Association</a>
 * </p>
 * @see IPSTmxDtdConstants
 */
public interface IPSTmxProperty
   extends IPSTmxNote
{
   /**
    * Method to get the type attribute of this node.
    * @return value of the 'type' attribute, never <code>null</code>
    * or <code>empty</code>
    */
   public String getType();

   /**
    * Sets type attribute for this node
    * @param    type if <code>null</code>, assumed <code>empty</code>.
    */
   public void setType(String type);
}
