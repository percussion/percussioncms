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
package com.percussion.pagemanagement.service;

import com.percussion.pagemanagement.data.PSPageChangeEvent;

/**
 * Implement this interface and register with the page service to get notified
 * when a page changes. When the page is changed the page service calls the
 * {@link #pageChanged(PSPageChangeEvent)} method with the
 * {@link PSPageChangeEvent} object. PSPageChangeEvent object will have pageid, assetid and type
 * of the event. Adds a listener in the page service constructor. Each method for any action
 * needs to get notified needs to create an PSPageChangeEvent object with event type etc and make a call
 * to pageservice.notifyPageChange by passing the PSPageChangeEvent object. notifyPageChange loop through the
 * listener list and makes the call to the method on the listener implementation class. which implements 
 * this interface. It is caller responsibility to provide needed implementation for the passed in event 
 * type on the listener implementation class.    
 * 
 * @author BJoginipally
 */
public interface IPSPageChangeListener
{
   public void pageChanged(PSPageChangeEvent pgEvent);
}
