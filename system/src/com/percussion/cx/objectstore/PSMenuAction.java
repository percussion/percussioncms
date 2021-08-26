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
package com.percussion.cx.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The class that is used to represent menu actions as defined by
 * 'sys_Action.dtd'.
 */
public class PSMenuAction implements IPSComponent, Cloneable
{
   /**
    * Convenience constructor for {@link
    * #PSMenuAction(String, String, String, String, String, int)
    * PSMenuAction(name, label, TYPE_MENUITEM, "", HANDLER_CLIENT, 0) }. See the
    * link for more description.
    */
   public PSMenuAction(String name, String label)
   {
      this(name, label, TYPE_MENUITEM, "", HANDLER_CLIENT, 0);
   }

   /**
    * Constructs the object with supplied parameters.
    *
    * @param name name of the action, may not be <code>null</code> or empty.
    * @param label label to show as a menu/menu item, may not be <code>null
    * </code> or empty.
    * @param type type of the action, must be one of the TYPE_XXX values.
    * @param url relative url to the applet to execute for the action, may be
    * <code>null</code> or empty.
    * @param handler handler of the action, must be one of the HANDLER_xxx
    * values.
    * @param sortrank the sort rank of the action.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSMenuAction(String name, String label, String type, String url,
      String handler, int sortrank)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if(label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      validType(type);

      if( !(HANDLER_CLIENT.equals(handler) || HANDLER_SERVER.equals(handler)) )
      {
         throw new IllegalArgumentException(
            "handler must be one of the following:" + HANDLER_CLIENT + "," +
            HANDLER_SERVER);
      }

      m_actionid = -1;
      m_name = name;
      m_label = label;
      m_type = type;
      m_handler = handler;
      m_actionURL = (url == null) ? "" : url;
      m_sortrank = sortrank;
   }

   /**
    * Constructs the action object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public PSMenuAction(Element element) throws PSUnknownNodeTypeException
   {
      if(element == null)
         throw new IllegalArgumentException("element may not be null.");

      fromXml(element);
   }

   // implements interface method, see toXml(Document) for the expected format
   //of the xml element.
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null.");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      //attributes
      m_actionid = PSXMLDomUtil.checkAttributeInt(sourceNode, ACTIONID_ATTR, false);
      m_name = PSComponentUtils.getRequiredAttribute(sourceNode, NAME_ATTR);
      m_label = PSComponentUtils.getAttribute(sourceNode, LABEL_ATTR, m_name);
      m_type = PSComponentUtils.getEnumeratedAttribute(
         sourceNode, TYPE_ATTR, ms_menuTypes);
      String url = sourceNode.getAttribute(URL_ATTR);
      m_actionURL = (url == null) ? "" : url;
      m_handler = PSComponentUtils.getEnumeratedAttribute(
         sourceNode, HANDLER_ATTR, ms_handlers);
      m_sortrank = 0;

      //#IMPLIED attribute
      String sortrank = sourceNode.getAttribute(SORTRANK_ATTR);
      try
      {
         m_sortrank = Integer.parseInt(sortrank);
      }
      catch(NumberFormatException e)
      {
         //ignore, as default is assigned
      }

      Element propsEl = PSComponentUtils.getChildElement(
         sourceNode, PSProperties.XML_NODE_NAME, false);
      m_props = null;

      if(propsEl != null)
         m_props = new PSProperties( propsEl );

      Element paramsEl = PSComponentUtils.getChildElement(
         sourceNode, PSParameters.XML_NODE_NAME, false);
      m_params = null;

      if(paramsEl != null)
         m_params = new PSParameters( paramsEl );

      m_children.clear();
      Iterator actions = PSComponentUtils.getChildElements(
         sourceNode, XML_NODE_NAME);
      if(isMenuItem() && actions.hasNext())
      {
         Object[] args = { sourceNode.getTagName(),
            "has child for menuitem" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      else if(isMenu())
      {
         while(actions.hasNext())
            m_children.add(new PSMenuAction((Element)actions.next()));
      }
   }

   /**
    * Sets the action id of the action.
    */
   public void setActionId(int actionid)
   {
      m_actionid = actionid;
   }

   /**
    * Gets the action id of the action.
    *
    * @return the action id
    */
   public int getActionId()
   {
      return m_actionid;
   }

   /**
    * Sets the name of the action.
    *
    * @param name the name, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if name is not valid.
    */
   public void setName(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      m_name = name;
   }

   /**
    * Gets the name of the action.
    *
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Gets the label of the action.
    *
    * @return the label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * Sets display label for this action. Can be used to set the label for
    * dynamic context menu.
    *
    * @param label the label, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if label is <code>null</code>.
    */
   public void setLabel(String label)
   {
      if(label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      m_label = label;
   }

   /**
    * Gets the action url that is relative to the applet's document base.
    *
    * @return the url, never <code>null</code>, may be empty.
    */
   public String getURL()
   {
      return m_actionURL;
   }


   /**
    * Sets the action url that is relative to the applet's document base.
    * 
    * @param url the url, never <code>null</code>, may be empty.
    */
   public void setURL(String url)
   {
      m_actionURL = url;
   }
   
   /**
    * Gets sort rank of this in its parent's children actions.
    *
    * @return the sort rank.
    */
   public int getSortRank()
   {
      return m_sortrank;
   }

   /**
    * Sets the type for this action.
    *
    * @param type the type, may not be <code>null</code> or empty and must be
    * one of the TYPE_xxx values.
    *
    * @throws IllegalArgumentException if type is not valid.
    */
   public void setType(String type)
   {
      if (validType(type))
         m_type = type;
   }

   /**
    * Gets the action type.
    *
    * @return the action, one of the action types defined in this class
    * including SPECIAL_ACTION_PASTE, never <code>null</code>, or <code>empty
    */
   public String getType()
   {
      return m_type;
   }

   /**
    * Validate the specified type, must be one of the following:
    *
    * <ul>
    *    <li>TYPE_MENU</li>
    *    <li>TYPE_MENUITEM</li>
    *    <li>TYPE_CONTEXTMENU</li>
    * </ul>
    *
    * @param type the type of menu, may not be <code>null</code> or empty
    *
    * @return true if the type is one of the required types, otherwise false
    */
   private boolean validType(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");

      if( !(TYPE_MENU.equals(type) || TYPE_MENUITEM.equals(type) ||
          TYPE_CONTEXTMENU.equals(type)) )
         throw new IllegalArgumentException(
            "type must be one of the following:" + TYPE_MENU + "," +
            TYPE_MENUITEM + "," + TYPE_CONTEXTMENU);

      return true;
   }

   /**
    * Checks whether this action represents a menu.
    *
    * @return <code>true</code> if it is a menu or context menu, otherwise
    * <code>false</code>
    */
   public boolean isMenu()
   {
      if(TYPE_MENU.equals(m_type) || TYPE_CONTEXTMENU.equals(m_type))
         return true;

      return false;
   }

   /**
    * Checks whether this action represents a context menu.
    *
    * @return <code>true</code> if it is a context menu, otherwise <code>false
    * </code>
    */
   public boolean isContextMenu()
   {
      if(TYPE_CONTEXTMENU.equals(m_type))
         return true;

      return false;
   }

   /**
    * Checks whether this action represents a menu item.
    *
    * @return <code>true</code> if it is a menu item, otherwise <code>false
    * </code>
    */
   public boolean isMenuItem()
   {
      if(TYPE_MENUITEM.equals(m_type))
         return true;

      return false;
   }

   /**
    * Finds whether the action to be handled by client or not. An action that
    * can not be handled by client is handled by server.
    *
    * @return <code>true</code> if it is, otherwise <code>false</code>.
    */
   public boolean isClientAction()
   {
      if(HANDLER_CLIENT.equals(m_handler))
         return true;

      return false;
   }

   /**
    * Finds out whether the action supports batch processing or not. An action
    * supports batch processing if the action's handler is 'SERVER' and the
    * the property <code>PROP_BATCH_PROCESS</code> is defined as <code>true
    * </code> or <code>yes</code>.
    *
    * @return <code>true</code> if it supports batch processing, otherwise
    * <code>false</code>
    */
   public boolean supportsBatchProcessing()
   {
      boolean supports = false;
      if(!isClientAction() && m_props != null)
      {
         String propValue = m_props.getProperty(PROP_BATCH_PROCESS);
         if( propValue != null &&
            (propValue.equalsIgnoreCase("true") ||
            propValue.equalsIgnoreCase("yes")) )
         {
            supports = true;
         }
      }

      return supports;
   }

   /**
    * Gets properties associated with this action.
    *
    * @return the properties, may be <code>null</code>
    */
   public PSProperties getProperties()
   {
      return m_props;
   }
   
   /**
    * Gets the value of the supplied property name of this action.
    * 
    * @param name the property name, may not be <code>null</code> or empty.
    * 
    * @return the value, may be <code>null</code> or empty.
    */
   public String getProperty(String name)
   {
      String value = null;
      
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if(m_props != null)
         value = m_props.getProperty(name);
         
      return value;
   }

   /**
    * Gets parameters to set with the action url.
    *
    * @return the parameters, may be <code>null</code>
    */
   public PSParameters getParameters()
   {
      return m_params;
   }

   /**
    * Gets the value of the supplied parameter name of this action.
    * 
    * @param name the parameter name, may not be <code>null</code> or empty.
    * 
    * @return the value, may be <code>null</code> or empty.
    */
   public String getParameter(String name)
   {
      String value = null;
      
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if(m_params != null)
         value = m_params.getParameter(name);
         
      return value;
   }

   /**
    * Sets the parameters associated with the action url.
    *
    * @param params the params, supply <code>null</code> to clear the existing
    * parameters.
    */
   public void setParameters(PSParameters params)
   {
      m_params = params;
   }
   
   /**
    * Adds or replaces the parameter identified by supplied name.
    * 
    * @param name the name of the parameter, may not be <code>null</code> or 
    * empty. 
    * @param value the value of the parameter, may be <code>null</code>
    * 
    * @throws IllegalArgumentException if name is invalid.
    */
   public void setParameter(String name, String value)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if(m_params == null)
         m_params = new PSParameters();

      m_params.setParameter(name, value);
   }  
   

   /**
    * Sets the properties of this action.
    *
    * @param props the properties, supply <code>null</code> to clear the
    * existing properties.
    */
   public void setProperties(PSProperties props)
   {
      m_props = props;
   }

   /**
    * Gets children actions of this action. Should be called only if the action
    * represents a {@link #isMenu() menu}.
    *
    * @return the iterator over zero or more <code>PSMenuAction</code> objects.
    */
   public Iterator getChildren()
   {
      return m_children.iterator();
   }

   /**
    * Sets child actions of this action. Should be called only if the action
    * represents a {@link #isMenu() menu}.
    *
    * @param children the list of <code>PSMenuAction</code> children, may not be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if children is <code>null</code>
    * @throws IllegalStateException if this action does not represent a menu.
    */
   public void setChildren(Iterator children)
   {
      if(isMenuItem())
         throw new IllegalStateException(
            "Can not set children to " + TYPE_MENUITEM + "action");

      if(children == null)
         throw new IllegalArgumentException("children may not be null.");

      m_children.clear();
      while(children.hasNext())
      {
         Object obj = children.next();
         if(obj instanceof PSMenuAction)
            m_children.add(obj);
         else
            throw new IllegalArgumentException(
               "Elements of children must be instances of PSMenuAction");
      }
   }

   /**
    * Implements the IPSComponent interface method to produce XML representation
    * of this object. See the interface for description of the method and
    * parameters.
    * <p>
    * The xml format is:
    * <pre><code>
    * &lt;!ELEMENT Action (Props?, Params?, Action*)>
    * &lt;!ATTLIST Action
    *    label CDATA #REQUIRED
    *    name CDATA #REQUIRED
    *    type ( MENU | CONTEXTMENU | MENUITEM) "MENU"
    *    url CDATA #IMPLIED
    *    handler (CLIENT | SERVER) "CLIENT"
    *    sortrank CDATA #IMPLIED
    *    >
    * </code></pre>
    *
    * @return the action element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ACTIONID_ATTR, "" + m_actionid);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(LABEL_ATTR, m_label);
      root.setAttribute(TYPE_ATTR, m_type);
      root.setAttribute(URL_ATTR, m_actionURL);
      root.setAttribute(HANDLER_ATTR, m_handler);
      root.setAttribute(SORTRANK_ATTR, String.valueOf(m_sortrank));

      if(m_props != null)
         root.appendChild(m_props.toXml(doc));

      if(m_params != null)
         root.appendChild(m_params.toXml(doc));

      if(isMenu())
      {
         Iterator childActions = m_children.iterator();
         while(childActions.hasNext())
            root.appendChild(((PSMenuAction)childActions.next()).toXml(doc));
      }

      return root;
   }

   /**
    * Merges PSMenuAction objects based merge rules and returns the merge result
    * as PSMenuAction object.
    *
    * @param newAction may be <code>null</code> in which case merge result will
    * be current action.
    *
    * @return merge result as PSMenuAction object, <code>null</code> only when the
    * two actions to be merged are <code>null</code>.
    *
    * actions based equality.
    */
   public PSMenuAction merge(PSMenuAction newAction)
   {
      // if new action is null, merge result is current action.
      if (newAction == null)
         return this;

      PSMenuAction child = null;
      PSMenuAction newChild = null;

      ArrayList list = new ArrayList();
      Iterator iter = newAction.getChildren();
      if (iter.hasNext())
      {
         while (iter.hasNext())
         {
            child = (PSMenuAction)iter.next();
            newChild = findChildAction(child);
            if (newChild != null)
            {
               child.merge(newChild);
               list.add(child);
            }
         }
         setChildren(list.iterator());
      }
      return this;
   }

   /**
    * Method to find the supplied action as one of its
    * child actions. Test is made using the equals() method.
    *
    * @param action the action to test, must not be <code>null</code>.
    *
    * @return
    */
   public PSMenuAction findChildAction(PSMenuAction action)
   {
      PSMenuAction child = null;
      Iterator iter = getChildren();
      while (iter.hasNext())
      {
         child = (PSMenuAction)iter.next();
         if (child.equals(action))
            return child;
      }
      return null;
   }

   /**
    * Method to test if this action contains the supplied action as one of its
    * child actions. Test is made using the equals() method.
    *
    * @param action the action to test, must not be <code>null</code>.
    *
    * @return <code>true</code> if the supplied action is equivalent to one of
    * the child actions, <code>false</code> otherwise.
    */
   public boolean containsChildAction(PSMenuAction action)
   {
      PSMenuAction child = null;
      Iterator iter = getChildren();
      while (iter.hasNext())
      {
         child = (PSMenuAction)iter.next();
         if (child.equals(action))
            return true;
      }
      return false;
   }

   /**
    * Adds a property with name {@see #SHOW_ADHOC} and value as
    * {@see #VAL_BOOLEAN_TRUE} if the supplied value is <code>true</code>.
    * Otherwise sets the parameter to <code>null</code>.
    * 
    * @param value The value to be set to {@see #SHOW_ADHOC} param.
    */
   public void setAdhocParam(boolean value)
   {
      if (value)
         setParameter(SHOW_ADHOC, VAL_BOOLEAN_TRUE);
      else
         setParameter(SHOW_ADHOC, null);
   }
   
   /**
    * Convenient method to get the adhoc parameter.
    * 
    * @return <code>true</code> if a parameter with name {@see #SHOW_ADHOC}
    *         exists with a value of {@see #VAL_BOOLEAN_TRUE} otherwise returns
    *         <code>false</code>.
    */
   public boolean isAdhoc()
   {
      return StringUtils.defaultString(getParameter(SHOW_ADHOC))
            .equalsIgnoreCase(VAL_BOOLEAN_TRUE);
   }
   
   /**
    * Convenient method to set the {@see COMMENT_REQUIRED} parameter and property.
    * @param commentRequired must not be blank.
    */
   public void setCommentRequired(String commentRequired)
   {
      if(StringUtils.isBlank(commentRequired))
         throw new IllegalArgumentException("commentRequired must not be blank");
      setParameter(COMMENT_REQUIRED, commentRequired);
      m_props.setProperty(COMMENT_REQUIRED, commentRequired);
   }
   
   /**
    * Convenient method to get the commentRequired parameter.
    * 
    * @return the value of parameter with name {@see #COMMENT_REQUIRED}
    *         if exists otherwise empty String.
    */
   public String getCommentRequired()
   {
      return StringUtils.defaultString(getParameter(COMMENT_REQUIRED));
   }
   
   /**
    * Used to compare the sort ranks of 2 <code>PSMenuAction</code> objects. The
    * algorithm for sorting is that if the 2 sort ranks are the same we then
    * sort using the label of the object.
    *
    * @param obj1 the first <code>PSMenuAction</code> object to compare,
    * assumed not <code>null</code>
    *
    * @param obj2 the second <code>PSMenuAction</code> object to compare,
    * assumed not <code>null</code>
    *
    * @return a negative integer, zero, or a positive integer as the
    * first object's <code>sort</code> value is less than, equal to, or
    * greater than that of the second object, if the <code>sort</code> values
    * are equal it does a compare on the <code>label</code> field, the compare
    * is done case insensitive
    */
   public int compare(Object obj1, Object obj2)
   {
      PSMenuAction a1 = (PSMenuAction)obj1;
      PSMenuAction a2 = (PSMenuAction)obj2;

      // if the sort numbers are the same
      // check the labels for the proper order
      int ret = a1.getSortRank() - a2.getSortRank();
      if (ret == 0)
      {
         return a1.getLabel().toUpperCase().compareTo(
            a2.getLabel().toUpperCase());
      }
      return (ret);
   }

   // see IPSDataComponent
   public Object clone()
   {
      PSMenuAction copy = null;
      try
      {
         copy = (PSMenuAction)super.clone();
         copy.m_children = new ArrayList();

         Iterator i = m_children.iterator();
         while (i.hasNext())
         {
            PSMenuAction obj = (PSMenuAction)i.next();
            copy.m_children.add((PSMenuAction)obj.clone());
         }
      }
      catch (CloneNotSupportedException ex) { /* ignore */ }

      return copy;
   }

   //implements interface method.
   public boolean equals(Object obj)
   {
      boolean equals = true;
      if( !(obj instanceof PSMenuAction) )
         equals = false;
      else
      {
         PSMenuAction other = (PSMenuAction)obj;
         if(m_actionid != other.m_actionid)
            equals = false;
         else if(!m_name.equals(other.m_name))
            equals = false;
         else if(!m_label.equals(other.m_label))
            equals = false;
         else if(!m_type.equals(other.m_type))
            equals = false;
         else if(!m_actionURL.equals(other.m_actionURL))
            equals = false;
         else if(!m_handler.equals(other.m_handler))
            equals = false;
         else if(m_sortrank != other.m_sortrank)
            equals = false;
         else if(m_props == null ^ other.m_props == null)
            equals = false;
         else if(m_props != null && !m_props.equals(other.m_props))
            equals = false;
         else if(m_params == null ^ other.m_params == null)
            equals = false;
         else if(m_params != null && !m_params.equals(other.m_params))
            equals = false;
         else if(!m_children.equals(other.m_children))
            equals = false;
      }

      return equals;
   }

   //implements interface method.
   public int hashCode()
   {
      int code = 0;

      code =
         m_actionid +
         m_name.hashCode() +
         m_label.hashCode() +
         m_type.hashCode() +
         m_handler.hashCode() +
         m_actionURL.hashCode() +
         m_sortrank +
         (m_params == null ? 0 : m_params.hashCode()) +
         (m_props == null ? 0 : m_props.hashCode()) +
         (m_children.hashCode()) ;

      return code;
   }

   /**
    * The id of the action, initialized in the constructor.
    */
   private int m_actionid = -1;

   /**
    * The name of the action, initialized in the constructor and never <code>
    * null</code>, empty or modified after that.
    */
   private String m_name;

   /**
    * The label of the action, initialized in the constructor and never <code>
    * null</code>, empty or modified after that.
    */
   private String m_label;

   /**
    * The menu type of the action, initialized in the constructor and never
    * <code>null</code>, empty or modified after that. Will be one of the
    * TYPE_xxx values.
    */
   private String m_type;

   /**
    * The action url, initialized in the constructor and never <code>null</code>
    * or modified after that. May be empty.
    */
   private String m_actionURL;

   /**
    * The handler of the action, initialized in the constructor and never
    * <code>null</code>, empty or modified after that. Will be one of the
    * HANDLER_xxx values.
    */
   private String m_handler;

   /**
    * The sortrank with in its siblings, initialized in the constructor and
    * never modified after that.
    */
   private int m_sortrank;

   /**
    * The list of properties associated with the action, initialized in
    * {@link #PSMenuAction(Element) constructor} and never <code>null</code> or
    * modified after that.
    */
   private PSProperties m_props = null;

   /**
    * The list of parameters to apply to the action url, initialized in
    * {@link #PSMenuAction(Element) constructor} and never <code>null</code> or
    * modified after that.
    */
   private PSParameters m_params = null;

   /**
    * The list of child actions, never <code>null</code>, may be empty.
    * Initialized at construction time and may be modified through calls to
    * <code>setChildren(Iterator)</code>.
    */
   private List m_children = new ArrayList();

   /**
    * The constant to indicate this action as 'menu'.
    */
   public static final String TYPE_MENU = "MENU";

   /**
    * The constant to indicate this action as 'menuitem'.
    */
   public static final String TYPE_MENUITEM = "MENUITEM";

   /**
    * The constant to indicate this action as 'contextmenu'.
    */
   public static final String TYPE_CONTEXTMENU = "CONTEXTMENU";

   /**
    * The constant to indicate 'client' as the handler for the action.
    */
   public static final String HANDLER_CLIENT = "CLIENT";

   /**
    * The constant to indicate 'server' as the handler for the action.
    */
   public static final String HANDLER_SERVER = "SERVER";

   /**
    * The list of action/menu types.
    */
   private static final List ms_menuTypes = new ArrayList();

   /**
    * The list of action handlers.
    */
   private static final List ms_handlers = new ArrayList();
   static
   {
      ms_menuTypes.add(TYPE_MENU);
      ms_menuTypes.add(TYPE_CONTEXTMENU);
      ms_menuTypes.add(TYPE_MENUITEM);

      ms_handlers.add(HANDLER_CLIENT);
      ms_handlers.add(HANDLER_SERVER);
   }

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "Action";

   /**
    * The name of the property that defines accelerator key for this action.
    */
   public static final String PROP_ACCEL_KEY = "AcceleratorKey";

   /**
    * The name of the property that defines mnemonic key for this action.
    */
   public static final String PROP_MNEM_KEY = "MnemonicKey";

   /**
    * The name of the property that defines tooltip text for this action.
    */
   public static final String PROP_SHORT_DESC = "ShortDescription";

   /**
    * The name of the property that defines url of the icon for this action.
    */
   public static final String PROP_SMALL_ICON = "SmallIcon";
   
   /**
    * The property that defines the batch processing support.
    */
   public static final String PROP_BATCH_PROCESS = "batchProcessing";

   /**
    * The prefix for copy paste action.
    */
   public static final String PREFIX_COPY_PASTE = "Copy-";

   /**
    * The prefix for drag-drop paste action.
    */
   public static final String PREFIX_DROP_PASTE = "Drop-";
   
   /**
    * The name attribute 
    */
   public static final String NAME_ATTR = "name";

   /**
    * Constant for the action param indicating if a comment is required.
    */
   public static final String COMMENT_REQUIRED = "commentRequired";

   /**
    * Constant for the action param indicating if the adhoc selection should be 
    * shown.
    */
   public static final String SHOW_ADHOC = "showAdhoc";

   /**
    * String value that represents true for an action param
    */
   public static final String VAL_BOOLEAN_TRUE = "yes";

   /**
    * String value that represents "hide" for an action param
    */
   public static final String VAL_HIDE = "hide";   
   
   /**
    * The internal name of the Check-in action. Will not change. The name is
    * 'checkin'. 
    */
   static public final String CHECKIN_ACTION_NAME = "checkin";
   
   /**
    * The internal name of the Check-out action. Will not change. The name is
    * 'checkout'. 
    */
   static public final String CHECKOUT_ACTION_NAME = "checkout";
   
   /**
    * The internal name of the Force Check-in action. Will not change.
    * The name is 'forcecheckin'. 
    */
   static public final String FORCE_CHECKIN_ACTION_NAME = "forcecheckin";
   
   
   
   //xml constants
   private static final String LABEL_ATTR = "label";
   private static final String ACTIONID_ATTR = "actionid";
   private static final String TYPE_ATTR = "type";
   private static final String URL_ATTR = "url";
   private static final String HANDLER_ATTR = "handler";
   private static final String SORTRANK_ATTR = "sortrank";
}
