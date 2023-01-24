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

package com.percussion.filetracker;

import javax.swing.*;

class PSToolBar extends JToolBar
{
   public PSToolBar()
   {
      // floatable doesn't work well unless all other borders are emtpy
      setFloatable(false);
   }   

   public JButton add(PSAction a)
   {
      JButton tbButton = add((Action) a);
      tbButton.setActionCommand(tbButton.getText());
      tbButton.setToolTipText( (String) a.getValue( Action.SHORT_DESCRIPTION ));

      // override things we don't want
      tbButton.setText("");
/*
      // cool button look
      tbButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
      tbButton.addMouseMotionListener(new MouseMotionAdapter()
      {
         public void mouseMoved(MouseEvent e)
         {
            ((JComponent)e.getComponent()).setBorder(
               BorderFactory.createBevelBorder(BevelBorder.RAISED));
         }
      });
*/      
      return(tbButton);
   }
}
