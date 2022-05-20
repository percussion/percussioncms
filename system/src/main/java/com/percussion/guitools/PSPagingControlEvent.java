/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.guitools;

import java.awt.*;

/**
 * Event fired when an action occurs on the paging control.
 * @author erikserating
 */
public class PSPagingControlEvent extends AWTEvent
{

   /**
    * Ctor
    * @param source the source object cannot be <code>null</code>
    * @param currentPage the current page indicated by the paging
    * control or -1 if no page selected.
    */
   public PSPagingControlEvent(Object source, int currentPage)
   {
      super(source, 1);
      m_currentPage = currentPage;
   }
   
   /**
    * The value of the current page as indicted by the paging
    * control.
    * @return may be -1 if no page selected.
    */
   public int getCurrentPage()
   {
      return m_currentPage;
   }

   /**
    * The current page value. Defaults to -1.
    */
   private int m_currentPage = -1;

}
