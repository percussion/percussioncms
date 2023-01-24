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
package com.percussion.wizard;

import com.percussion.cx.PSContentExplorerApplet;

/**
 * A standard wizard start or finish panel which does only show user 
 * instructions.
 */
public class PSWizardStartFinishPanel extends PSWizardPanel
{

   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSWizardStartFinishPanel(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(null);
   }

   /**
    * Construct a start or finish wizard panel.
    */

   public PSWizardStartFinishPanel()
   {
      initPanel(null);
   }
}
