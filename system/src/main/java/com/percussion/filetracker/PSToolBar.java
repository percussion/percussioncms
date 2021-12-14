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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
