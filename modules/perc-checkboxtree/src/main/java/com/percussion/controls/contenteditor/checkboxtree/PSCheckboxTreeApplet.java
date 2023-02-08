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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.controls.contenteditor.checkboxtree;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * An xml driven tree applet designed for use as Rhythmyx custom control.
 * <p>
 * This applet is intended to be extensible by any of the following:
 * <ol>
 *    <li>
 *    Creating a new <code>IPSCheckboxTreeListener</code> implementation that 
 *    handles special behavior. To use a custom listerner one must override
 *    {@link #makeListener(JTree)}.
 *    </li> 
 *    <li>
 *    Creating a new <code>IPSCheckboxTreeRenderer</code> implementation that 
 *    renders different. To use a custom renderer one must
 *    override {@link #makeRenderer()}.
 *    </li>
 *    <li>
 *    Change the applets look and feel, which defaults to the system look and 
 *    feel. To use a custom look and feel one must override 
 *    {@link #initializeLookAndFeel()}.
 *    </li>
 *    <li>
 *    Addition of extra parameters that will be passed to the renderer and 
 *    listener implementations. Subclasses with extra parameters should also 
 *    override the {@link #getParameterInfo()} and {@link #getExtraParameters()} 
 *    methods. 
 *    </li> 
 * </ol>   
 */
public class PSCheckboxTreeApplet extends JApplet implements Runnable
{
   /**
    * Initialize the applet and load the tree.
    */
   @Override
   public void init()
   {
      super.init();
      
      try
      {
         initializeLookAndFeel();
         
         // read standard parameters
         m_selectedItems = getParameter("selectedItems", "");
         String readParam = getParameter("readOnly", "no");
         String psSessionId = getParameter("pssessionid", "");
         
         if (readParam.toLowerCase().startsWith("yes"))
            m_isReadonly = true;
         
         URL baseDocument = null;
         if (m_isStandalone)
         {
            // testing, load test resources
            m_treeURL = TREEDEF_FILE;
            File resourceBase = new File(RESOURCE_BASE);
            baseDocument = resourceBase.toURL();
         }
         else
         {
            m_treeURL = getParameter("treeXML", "");
            baseDocument = getDocumentBase();
         }

         PSCheckboxTreeModel model = 
            new PSCheckboxTreeModel(baseDocument, m_treeURL, psSessionId);
         m_tree.setModel(model);
         m_renderer = makeRenderer();
         m_tree.setCellRenderer(m_renderer);
         m_tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
         m_tree.setToggleClickCount(2);

         setSelectedNodes(m_selectedItems);
         if (!m_isReadonly)
         {
            m_listener = makeListener(m_tree);
            m_tree.addMouseListener(m_listener);
         }
         else
         {
            m_tree.setEditable(false);
         }

         m_tree.setVisible(true);

         getContentPane().add(m_treeScrollpane, BorderLayout.CENTER);

      }
      catch (Exception e)
      {
         System.err.println("Error: " + e.getLocalizedMessage());
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Convenience method for obtaining parameters.
    * 
    * @param key name of the system property, not <code>null</code> or empty.
    * @param def the default value is returned if the specified key is not 
    *    found, may be <code>null</code> or empty.
    */
   protected String getParameter(String key, String def)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key cannot be null or empty");
      
      return m_isStandalone ? System.getProperty(key, def)
         : (getParameter(key) != null ? getParameter(key) : def);
   }

   /**
    * Creates the listener for this tree. Subclasses of this applet that 
    * implement special behavior must override this method to create a 
    * different <code>IPSCheckboxTreeListener</code> implementation.
    * 
    * @param tree the tree to monitor, not <code>null</code>.
    * @return the listener for this tree, never <code>null</code>. 
    */
   protected IPSCheckboxTreeListener makeListener(JTree tree)
   {
      if (tree == null)
         throw new IllegalArgumentException("tree cannot be null");

      PSCheckboxTreeToggle listener = new PSCheckboxTreeToggle(tree);
      listener.setParameters(getExtraParameters());
      
      return listener;
   }

   /**
    * Creates the renderer for this tree. Subclasses of this applet that 
    * implement a different look and feel must override this method to create 
    * a different <code>IPSCheckboxTreeRender</code> implementation. 
    *
    * @return the tree renderer, never <code>null</code>.
    */
   protected IPSCheckboxTreeRenderer makeRenderer()
   {
      PSCheckboxTreeRenderer renderer = new PSCheckboxTreeRenderer();
      renderer.setParameters(getExtraParameters());
      
      return renderer;
   }

   /**
    * Process any extra parameters. The base implementation does not use any 
    * extra parameters. Subclasses must override this if extra parameters are
    * required.
    * 
    * @return a map of extra parameters, never <code>null</code>, may be empty.
    */
   protected Map<String, String> getExtraParameters()
   {
      if (m_parameters == null)
         m_parameters = new HashMap<>();
      
      return m_parameters;
   }

   /**
    * Gets the list of selected nodes, as a semicolon delimited list of node
    * ids. The String may be empty, but never <code>null</code>. This method 
    * is intended to be called from <code>JavaScript</code> when the page 
    * submit button is pushed.
    * 
    * @return the list of selected node ids as semicolon delimited string,
    *    never <code>null</code>, may be empty.
    */
   public String getSelected()
   {
      List<String> selected = getSelectedNodes();
      return listToString(selected);
   }
   
   /**
    * Test if the checkbox tree control has changed since its initialization.
    * 
    * @return <code>true</code> if it has changed, <code>false</code> otherwise.
    */
   public boolean isDirty()
   {
      return !m_selectedItems.equals(getSelected());
   }

   /**
    * Start the applet.
    */
   @Override
   public void start()
   {
      /*
       * Must initialize the look and feel on each start to make sure the
       * specified look and feel is used in web browsers after each update.
       */
      initializeLookAndFeel();
      SwingUtilities.invokeLater(this);
   }

   /**
    * Stop the applet.
    */
   @Override
   public void stop()
   {
   }

   /**
    * Destroy the applet.
    */
   @Override
   public void destroy()
   {
   }

   /**
    * Get Applet information.
    */
   @Override
   public String getAppletInfo()
   {
      return "Percussion Rhythmyx CheckboxTree Control";
   }

   /**
    * Get the parameter info in a printable form. Subclasses which use extra
    * parameters should also override this method.
    */
   @Override
   public String[][] getParameterInfo()
   {
      String[][] pinfo = 
      {
         { "selectedItems", "String", "list of selected leaf nodes" },
         { "treeXML", "String", "URL for tree XML" },
         { "readOnly", "String", "read only? yes or no default is no" }
      };
      
      return pinfo;
   }

   /**
    * Sets the selected nodes. All nodes whose <code>id</code> matches any
    * element of this list will be selected.
    * 
    * @param selectList the node ids to select, assumed not <code>null</code>.
    */
   private void setSelectedNodes(String selectList)
   {
      List selectedNodes = getTokens(selectList);

      PSCheckboxTreeRootNode rootNode = 
         (PSCheckboxTreeRootNode) m_tree.getModel().getRoot();

      PSCheckboxTreeNode nodewalker = 
         (PSCheckboxTreeNode) rootNode.getNextNode();
      while (nodewalker != null)
      {
         if (nodewalker.isSelectable() && 
            selectedNodes.contains(nodewalker.getNodeId()))
         {
            nodewalker.setSelected(true);
            TreePath path = new TreePath(nodewalker.getPath());
            m_tree.makeVisible(path);
         }
         
         nodewalker = (PSCheckboxTreeNode) nodewalker.getNextNode();
      }
   }

   /**
    * Walk the node tree listing all selected node ids.
    * 
    * @return all selected node ids, may be <code>empty</code> but not 
    *    <code>null</code>.
    */
   private List<String> getSelectedNodes()
   {
      List<String> selectedList = new ArrayList<>();
      
      PSCheckboxTreeRootNode rootNode = 
         (PSCheckboxTreeRootNode) m_tree.getModel().getRoot();
      PSCheckboxTreeNode nodewalker = 
         (PSCheckboxTreeNode) rootNode.getNextNode();
      while (nodewalker != null)
      {
         if (nodewalker.isSelectable() && nodewalker.isSelected())
            selectedList.add(nodewalker.getNodeId());

         nodewalker = (PSCheckboxTreeNode) nodewalker.getNextNode();
      }
      
      return selectedList;
   }

   /**
    * Get a list of tokens for the supplied comma or semicolon delimited string.
    *  
    * @param delimitedString the comma or semicolon delimited string, 
    *    not <code>null</code>, may be empty.
    * @return a list of tokens, may be <code>empty</code> but never 
    *    <code>null</code>.
    */
   public static List<String> getTokens(String delimitedString)
   {
      if (delimitedString == null)
         throw new IllegalArgumentException("delimitedString cannot be null");
      
      List<String> tokenList = new ArrayList<>();

      StringTokenizer tokens = new StringTokenizer(delimitedString, ",;");
      while (tokens.hasMoreTokens())
         tokenList.add(tokens.nextToken());

      return tokenList;
   }

   /**
    * Produce a semicolon delimited string from the supplied tokens.
    * 
    * @param tokens the tokens from which to create a semicolon delimited 
    *    string, may be <code>null</code>.
    * @return the values concatenated into a single string, never 
    *    <code>null</code>, may be empty.
    */
   public static String listToString(List<String> tokens)
   {
      StringBuilder delimitedString = new StringBuilder();
      
      if (tokens != null)
      {
         Iterator it = tokens.iterator();
         boolean first = true;
         while (it.hasNext())
         {
            if (first)
               first = false;
            else
               delimitedString.append(";");
   
            delimitedString.append(it.next().toString());
         }
      }
      
      return delimitedString.toString();
   }

   /**
    * Update the component tree UI with the look and feel of this applet.
    */
   public void run()
   {
      SwingUtilities.updateComponentTreeUI(this);
   }
   
   /**
    * Initialize the look and feel for this applet. Uses the look and feel of 
    * the current system.
    */
   protected void initializeLookAndFeel()
   {
      UIManager.put("ClassLoader", PSCheckboxTreeApplet.class.getClassLoader());
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         // proceed with default look and feel
         System.err.println("Warning: " + e.getLocalizedMessage());
      }
   }

   /**
    * Main method for testing in standalone mode.
    * 
    * @param args command line arguments.
    */
   public static void main(String[] args)
   {
      PSCheckboxTreeApplet applet = new PSCheckboxTreeApplet();
      applet.m_isStandalone = true;
      
      applet.init();
      applet.start();
      
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(3);
      frame.setTitle("PSCheckboxTreeApplet");
      frame.getContentPane().add(applet, BorderLayout.CENTER);
      frame.setSize(400, 320);
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setLocation((d.width - frame.getSize().width) / 2,
         (d.height - frame.getSize().height) / 2);
      frame.setVisible(true);
   }

   /**
    * Extra Parameters for this tree applet. Not used in the base 
    * implementation, but may be used by subclasses. Initialized by
    * {@link #getExtraParameters()}, never <code>null</code> or changed after 
    * that.
    */
   protected Map<String, String> m_parameters = null;

   /**
    * Is this a standalone invocation, used for testing only.
    */
   private boolean m_isStandalone = false;

   /**
    * The list of selected items, never <code>null</code>, may be empty after
    * applet initialization.
    */
   private String m_selectedItems;

   /**
    * The URL of the definition tree. The xml file at this URL is loaded during
    * applet initialzation, never <code>null</code>, may be empty after that.
    */
   private String m_treeURL;

   /**
    * Is this applet read only. The Applet will still expand and collapse, but
    * the selection cannot be changed if this is <code>true</code>.
    */
   private boolean m_isReadonly = false;

   /**
    * The tree that controls the main behavior, never <code>null</code>.
    */
   private JTree m_tree = new JTree();

   /**
    * This pane holds the optional scrollbars. The scrollbars are not normally
    * visible unless the xml supplied specifies a larger amount of data than the
    * allocated size of the applet.
    */
   private JScrollPane m_treeScrollpane = new JScrollPane(m_tree);

   /**
    * Listener for this applet, initialized wit the applet initialization. May
    * be <code>null</code> if started in readonly mode.
    */
   private IPSCheckboxTreeListener m_listener = null;

   /**
    * Renderer for this applet, initialized with the applet initialization, 
    * never <code>null</code> after that.
    */
   private IPSCheckboxTreeRenderer m_renderer = null;

   /**
    * Generated serial version id.
    */
   private static final long serialVersionUID = -2234401999372713865L;

   /**
    * Base resource file location, used for testing in standalone mode.
    */
   private static final String RESOURCE_BASE = 
      "UnitTestResources/com/percussion/controls/contenteditor/checkboxtree/";
   
   /**
    * The tree definition file used for testing in standalone mode.
    */
   private static final String TREEDEF_FILE = "treedef.xml";
}
