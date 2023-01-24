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
