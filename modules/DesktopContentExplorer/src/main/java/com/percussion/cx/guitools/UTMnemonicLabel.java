/*[ UTMnemonicLabel.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx.guitools;

import javax.swing.JLabel;
import java.awt.Component;
import java.util.ResourceBundle;

/**
 * The UTMnemonicLabel provides the E2 standard mnemonic label.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTMnemonicLabel extends JLabel
{
   /**
   * Construct a label attached to the provided component using the passed
   * character as its mnemonic.
   *
   * @param label         the label
   * @param component   the component to attach the label to
   * @param mnemonic    the mnemonic character
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTMnemonicLabel(String label, Component component, char mnemonic)
   {
     super(label);

    setDisplayedMnemonic(mnemonic);
    setLabelFor(component);
   }

   /**
   * Construct a label attached to the provided component using the passed
   * resource bundle and resource ID. The mnemonic character must be in the
   * provided resource bundle and the ID is <resId>Mnemonic.
   *
   * @param res            the resource bundle
   * @param resId          the resource ID
   * @param component   the component to attach the label to
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTMnemonicLabel(ResourceBundle res, String resId, Component component)
   {
        super();
        String labelStr = res.getString(resId);
     setText(labelStr);
     char mnemonic = res.getString("mn_" + resId).charAt(0);
        int ix = labelStr.indexOf(mnemonic);
     setDisplayedMnemonicIndex(ix);
    setDisplayedMnemonic(res.getString("mn_" + resId).charAt(0));
    setLabelFor(component);
   }
}
