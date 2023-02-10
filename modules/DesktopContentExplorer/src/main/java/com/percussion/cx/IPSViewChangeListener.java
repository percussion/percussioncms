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
package com.percussion.cx;

/**
 * The interface to represent the change in the data of the view that does not 
 * represent "the change". Interface is implemented by classes that are to be 
 * notified of a views data changes.
 */
public interface IPSViewChangeListener
{    
   /**
    * Notifies the listener as the data is changed/modified and provides the 
    * modified data object. 
    * 
    * @param data the modifed data, must be an instance of object supported by 
    * the view. 
    * 
    * @throws IllegalArgumentException if data is not valid.
    */
   public void viewDataChanged(Object data);   
}

