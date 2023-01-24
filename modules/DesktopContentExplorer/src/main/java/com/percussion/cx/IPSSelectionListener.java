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
 * The interface to represent the selection change in main view panel of the 
 * applet. Interface is implemented by classes that are to be notified of a 
 * selection change.
 */
public interface IPSSelectionListener
{
   /**
    * Notification event that the current selection has changed in main view.
    * 
    * @param selection the object that encapsulates the selection details, may
    * not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void selectionChanged(PSSelection selection);
}

