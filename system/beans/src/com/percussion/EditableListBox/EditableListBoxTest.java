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

package com.percussion.EditableListBox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;



public class EditableListBoxTest implements ActionListener, ItemListener
{
  private EditableListBox data;
  private TestImageObject[] listData = {  new TestImageObject("list1"),
                                 new TestImageObject("list2"),
                                 new TestImageObject("list3"),
                                 new TestImageObject("list4"),
                                 new TestImageObject("list5"),
                                 new TestImageObject("list6"),
                                 new TestImageObject("list7"),
                                 new TestImageObject("list8"),
                                 new TestImageObject("list9"),
                                 new TestImageObject("list10")
                                 };
  private String[] rendererData = {"render1", "render2", "render3", "render4", "render5"};
  private String     m_title = new String("Title");
  
  private JPanel     m_testPanel;
  private JCheckBox  m_leftButtonSelection, m_lastCellSelection;
  private JTextField m_titleSelection;
  private JButton    m_titleButton;
  private JRadioButton    m_textField, m_comboBox, m_dropDownList, m_browseEdit,
                          m_browseList;

  private JDialog        m_browseDialog;

  private ImageIcon m_icon = new ImageIcon(getClass().getResource("images/celltest.gif"));

  private JFrame frame;

  private int        m_editorType;
  private String     m_buttonType;
  
  public EditableListBoxTest()
  {
    frame = new JFrame();

    m_editorType = EditableListBox.COMBOBOX;
    m_buttonType = new String(EditableListBox.INSERTBUTTON);

    createBrowseDialog();

    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    data = new EditableListBox(m_title, m_browseDialog, listData, rendererData,
               m_editorType, m_buttonType);

    /*
    data.addColumn("column ?");
    */

    data.setCellSelectionEnabled(false);
    data.setCellEditorEnabled(true);

    data.setPreferredSize(new Dimension(200, 200));
    data.getRightButton().addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        data.deleteRows();
      }
    });
    System.out.println(data.getPreferredSize().toString());

    // added code for testing ICellImageHelper interface
    listData[0].setImage(m_icon);
    listData[2].setImage(m_icon);
    listData[4].setImage(m_icon);
    listData[6].setImage(m_icon);
    listData[8].setImage(m_icon);

    /*
    Object[] nameArray = new Object[2];
    nameArray[0] = "name";
    nameArray[1] = "Sec. Prov.";

    data.setColumnIdentifiers(nameArray);
    */

    initializeEastPanel();

    frame.getContentPane().setLayout(new FlowLayout());
    frame.getContentPane().add(data); //, BorderLayout.CENTER);
    frame.getContentPane().add(m_testPanel); //, BorderLayout.EAST);
    frame.addWindowListener(new WindowAdapter()
       { public void windowClosing(WindowEvent e) { System.exit(0); } }
       );
    frame.setSize(500, 250);
    frame.setVisible(true);

  }

  private void initializeEastPanel()
  {
     m_testPanel = new JPanel();
     m_testPanel.setLayout(new BoxLayout(m_testPanel, BoxLayout.Y_AXIS));

     m_titleSelection = new JTextField(data.getTitle(), 12);
     m_titleButton    = new JButton("OK!");
     m_titleButton.setActionCommand("titleOK");
     m_titleButton.addActionListener(this);
     JPanel title = new JPanel(new FlowLayout());
     title.add(m_titleSelection);
     title.add(m_titleButton);

     m_lastCellSelection = new JCheckBox("Enabled empty last cell", false);
     m_lastCellSelection.addItemListener(this);

     m_leftButtonSelection = new JCheckBox("Browse Button", false);
     m_leftButtonSelection.addItemListener(this);

     m_textField = new JRadioButton("TextField");  m_textField.setActionCommand("text");
     m_comboBox  = new JRadioButton("ComboBox", true);   m_comboBox.setActionCommand("combo");
     m_dropDownList = new JRadioButton("DropDownList"); m_dropDownList.setActionCommand("drop");
     m_browseEdit = new JRadioButton("BrowseBoxEdit");  m_browseEdit.setActionCommand("edit");
     m_browseList = new JRadioButton("BrowseBoxList");  m_browseList.setActionCommand("list");
     
     final ButtonGroup group = new ButtonGroup();
     group.add(m_textField);        m_textField.addActionListener(this);
     group.add(m_comboBox);         m_comboBox.addActionListener(this);
     group.add(m_dropDownList);     m_dropDownList.addActionListener(this);
     group.add(m_browseEdit);       m_browseEdit.addActionListener(this);
     group.add(m_browseList);       m_browseList.addActionListener(this);

     m_testPanel.add(title);
     m_testPanel.add(m_lastCellSelection);
     m_testPanel.add(m_leftButtonSelection);
     m_testPanel.add(m_textField);
     m_testPanel.add(m_comboBox);
     m_testPanel.add(m_dropDownList);
     m_testPanel.add(m_browseEdit);
     m_testPanel.add(m_browseList);
     m_testPanel.setBorder(new LineBorder(Color.darkGray));
  }

  public void actionPerformed(ActionEvent e)
  {
     if ("titleOK".equals(e.getActionCommand()))
     {
        m_title = m_titleSelection.getText();
        data.setTitle(m_title);
     }
     else if ("text".equals(e.getActionCommand()))
     {
        m_editorType = EditableListBox.TEXTFIELD;
        frame.getContentPane().remove(data);
        data = new EditableListBox(m_title, m_browseDialog, listData, null, m_editorType, m_buttonType);
        frame.getContentPane().add(data, BorderLayout.CENTER);
        data.revalidate();

        data.setCellSelectionEnabled(false);
        data.setCellEditorEnabled(true);
        data.addEmptyEndCell();
     }
     else if ("combo".equals(e.getActionCommand()))
     {
        m_editorType = EditableListBox.COMBOBOX;
        frame.getContentPane().remove(data);
        data = new EditableListBox(m_title, m_browseDialog, listData, rendererData, m_editorType, m_buttonType);
        frame.getContentPane().add(data, BorderLayout.CENTER);
        data.revalidate();

        data.setCellSelectionEnabled(false);
        data.setCellEditorEnabled(true);
        data.addEmptyEndCell();
     }
     else if ("drop".equals(e.getActionCommand()))
     {
        m_editorType = EditableListBox.DROPDOWNLIST;
        frame.getContentPane().remove(data);
        data = new EditableListBox(m_title, m_browseDialog, listData, rendererData, m_editorType, m_buttonType);
        frame.getContentPane().add(data, BorderLayout.CENTER);
        data.revalidate();

        data.setCellSelectionEnabled(false);
        data.setCellEditorEnabled(true);
        data.addEmptyEndCell();
     }
     else if ("edit".equals(e.getActionCommand()))
     {
        m_editorType = EditableListBox.BROWSEBOXEDIT;
        frame.getContentPane().remove(data);
        data = new EditableListBox(m_title, m_browseDialog, listData, null, m_editorType, m_buttonType);
        frame.getContentPane().add(data, BorderLayout.CENTER);
        data.revalidate();

        data.setCellSelectionEnabled(false);
        data.setCellEditorEnabled(true);
        data.addEmptyEndCell();
     }
     else if ("list".equals(e.getActionCommand()))
     {
        m_editorType = EditableListBox.BROWSEBOXLIST;
        frame.getContentPane().remove(data);
        data = new EditableListBox(m_title, m_browseDialog, listData, rendererData, m_editorType, m_buttonType);
        frame.getContentPane().add(data, BorderLayout.CENTER);
        data.revalidate();

        data.setCellSelectionEnabled(false);
        data.setCellEditorEnabled(true);
        data.addEmptyEndCell();
     }
  }

  public void itemStateChanged(ItemEvent e)
  {
     if (e.getItemSelectable() instanceof JCheckBox)
     {
        if (e.getSource() == m_leftButtonSelection)
        {
          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            m_buttonType = EditableListBox.BROWSEBUTTON;
            frame.getContentPane().remove(data);
            data = new EditableListBox(m_title, null, listData, rendererData, m_editorType, m_buttonType);
            data.addEmptyEndCell();
          }
          else if (e.getStateChange() == ItemEvent.DESELECTED)
          {
            m_buttonType = EditableListBox.INSERTBUTTON;
            frame.getContentPane().remove(data);
            data = new EditableListBox(m_title, null, listData, rendererData, m_editorType, m_buttonType);
            frame.getContentPane().add(data, BorderLayout.CENTER);
          }

          data.revalidate();
        }
        else if (e.getSource() == m_lastCellSelection)
        {
          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            frame.getContentPane().remove(data);
            data = new EditableListBox(m_title, m_browseDialog, listData, rendererData, m_editorType, m_buttonType);
            data.addEmptyEndCell();
            frame.getContentPane().add(data, BorderLayout.CENTER);
          }
          else if (e.getStateChange() == ItemEvent.DESELECTED)
          {
            frame.getContentPane().remove(data);
            data = new EditableListBox(m_title, null, listData, rendererData, m_editorType, m_buttonType);
            frame.getContentPane().add(data, BorderLayout.CENTER);
          }

          data.revalidate();

        }
     }
  }

  public void createBrowseDialog()
  {
     m_browseDialog = new JDialog(frame, "Dialog");
     m_browseDialog.setSize(100, 100);
  }


//Main method
  
  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

    }
    catch (Exception e)
    {
    }
    new EditableListBoxTest();
  }
}

 
