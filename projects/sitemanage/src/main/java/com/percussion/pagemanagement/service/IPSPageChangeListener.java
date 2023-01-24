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
