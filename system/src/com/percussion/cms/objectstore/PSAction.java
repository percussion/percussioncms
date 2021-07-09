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
package com.percussion.cms.objectstore;


import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * The class that is used to represent menu actions as defined by
 * 'sys_Action.dtd'.
 */
public class PSAction extends PSVersionableDbComponent
   implements IPSCatalogSummary, IPSCloneTuner
{

   private static final Logger log = LogManager.getLogger(PSAction.class);

   /**
    * Convenience constructor for {@link
    * #PSAction(String, String, String, String, String, int)
    * PSAction(name, label, TYPE_MENUITEM, "", HANDLER_CLIENT, 0) }. See the
    * link for more description.
    */
   public PSAction(String name, String label)
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
   public PSAction(String name, String label, String type, String url,
      String handler, int sortrank)
   {
      super(createKey((String) null));

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      if (!(HANDLER_CLIENT.equals(handler) || HANDLER_SERVER.equals(handler)))
      {
         throw new IllegalArgumentException(
            "handler must be one of the following:" + HANDLER_CLIENT + "," +
            HANDLER_SERVER);
      }

      m_name = name;
      m_label = label;
      setMenuType(type);
      m_handler = handler;
      m_actionURL = (url == null) ? "" : url;
      m_sortrank = sortrank;

      // set the default properties
      m_props.setProperty(PROP_LAUNCH_NEW_WND, NO);
      m_props.setProperty(PSAction.PROP_MUTLI_SELECT, NO);
      m_props.setProperty(PSAction.PROP_REFRESH_HINT, RefreshHint.NONE
            .getValue());
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
   public PSAction(Element element) throws PSUnknownNodeTypeException
   {
      super.fromXml(element);

      fromXml(element);
   }

   /**
    * Creates the correct key for this component.
    */
   public static PSKey createKey(String value)
   {
      PSKey key = null;

      if (null == value || value.trim().length() == 0)
         key = new PSSimpleKey(PRIMARY_KEY);
      else
         key = new PSSimpleKey(PRIMARY_KEY, value);

      return key;
   }

   /**
    * Gets the id of the object.
    *
    * @return the id, it may be <code>-1</code> if the id has not been assigned.
    */
   public int getId()
   {
      return getKeyPartInt(PRIMARY_KEY, -1);
   }

   /**
    * @return the GUID object, never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return getGuidFromId(getId());
   }

   /**
    * Creates a GUID from an id.
    * 
    * @param id the action id, which is saved in the repository.
    * 
    * @return the created GUID, never <code>null</code>.
    */
   public static IPSGuid getGuidFromId(int id)
   {
      return new PSGuid(PSTypeEnum.ACTION, id);
   }

   /**
    * Gets the action id (which is saved in the repository) from a
    * GUID object.
    * 
    * @param guid the guid object, which must be a {@link PSTypeEnum#ACTION}
    *    type.
    * 
    * @return the UUID of the guid.
    */
   public static int getIdFromGuid(IPSGuid guid)
   {
      if (guid.getType() != PSTypeEnum.ACTION.getOrdinal())
         throw new IllegalArgumentException(
               "guid must be PSTypeEnum.ACTION type.");
      
      return guid.getUUID();
   }
   
   /**
    * @param newguid the new GUID, its type must be {@link PSTypeEnum#ACTION},
    *    never <code>null</code>.
    */
   public void setGUID(IPSGuid newguid)
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null.");
      
      String id = String.valueOf(getIdFromGuid(newguid));
      setLocator(createKey(id));
   }
   
   /**
    * Gets the list of mode-uicontexts with the action
    *
    * @return the list of mode-uicontexts with the action, never 
    *    <code>null</code> may be <code>empty</code>.
    */
   public PSDbComponentCollection getModeUIContexts()
   {
      return m_modeUiContexts;
   }

   /**
    * Set the mode-uicontext mapping list.
    *
    * @param modeCtxs the new list, never <code>null</code>, but may be empty.
    */
   public void setModeUIContexts(PSDbComponentCollection modeCtxs)
   {
      if (modeCtxs == null)
         throw new IllegalArgumentException("modeCtxs may not be null.");

      m_modeUiContexts = modeCtxs;
   }

   // implements interface method, see toXml(Document) for the expected format
   //of the xml element.
   @Override
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      super.fromXml(sourceNode);

      //attributes
      m_name = PSComponentUtils.getRequiredAttribute(sourceNode, NAME_ATTR);
      m_label = PSComponentUtils.getRequiredAttribute(sourceNode, LABEL_ATTR);
      m_type = PSComponentUtils.getEnumeratedAttribute(
         sourceNode, TYPE_ATTR, ms_menuTypes);
      String url = sourceNode.getAttribute(URL_ATTR);
      m_actionURL = (url == null) ? "" : (url.equals(URL_PLACEHOLDER) ? ""
            : url);
      setMenuDynamic(!StringUtils.isBlank(m_actionURL));
      m_handler = PSComponentUtils.getEnumeratedAttribute(
         sourceNode, HANDLER_ATTR, ms_handlers);

      m_sortrank = 0;
      String sortrank = sourceNode.getAttribute(SORTRANK_ATTR);
      try
      {
         m_sortrank = Integer.parseInt(sortrank);
      }
      catch(NumberFormatException e)
      {
         //ignore, as default is assigned
      }

      try
      {
         setVersion(Integer.parseInt(PSComponentUtils.getRequiredAttribute(
               sourceNode, VERSION_ATTR)));
      }
      catch(NumberFormatException e)
      {
         Object[] args =
         {
            getNodeName(),
            VERSION_ATTR,
            e.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      m_description = "";
      Element descEl = PSComponentUtils.getChildElement(
         sourceNode, "Description", false);
      if (descEl != null)
      {
         Node descText = descEl.getFirstChild();
         if (descText instanceof Text)
            m_description = ((Text) descText).getData();
      }

      Element propsEl = PSComponentUtils.getChildElement(
         sourceNode, PSActionProperties.XML_NODE_NAME, false);
      if(propsEl != null)
         m_props.fromXml(propsEl);
      else
         m_props.clear();

      Element paramsEl = PSComponentUtils.getChildElement(
         sourceNode, PSActionParameters.XML_NODE_NAME, false);
      if(paramsEl != null)
         m_params.fromXml(paramsEl );
      else
         m_params.clear();

      Element visCtxEl = PSComponentUtils.getChildElement(
         sourceNode, PSActionVisibilityContexts.XML_NODE_NAME, false);
      if(visCtxEl != null)
         m_visContexts.fromXml( visCtxEl );
      else
         m_visContexts.clear();

      Element modeCtxEl = PSComponentUtils.getChildElement(
         sourceNode, PSDbComponentCollection.XML_NODE_NAME, false);
      if(modeCtxEl != null)
         m_modeUiContexts.fromXml( modeCtxEl );
      else
      {
         m_modeUiContexts.clear();
      }

      Element actions = PSComponentUtils.getChildElement(
         sourceNode, PSChildActions.XML_NODE_NAME, false);
      if (actions != null)
         m_children.fromXml(actions);
      else
         m_children.clear();
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
    * Sets the name of the action.
    *
    * @param name never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if the argument is invalid
    */
   public void setName(String name)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name cannot null or empty");

      if (!name.equals(m_name))
         setDirty();

      m_name = name;
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

      if (!label.equals(m_label))
         setDirty();

      m_label = label;
   }

   /**
    * Gte the action menu description.
    *
    * @return the description, never <code>null</code>, may be empty.
    */
   @Override
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Set a new action menu description.
    *
    * @param description the new description, may be <code>null</code> or empty.
    */
   public void setDescription(String description)
   {
      if (description == null)
         description = "";

      // remove whitespace
      //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
      description = description.trim();

      if (!description.equals(m_description))
         setDirty();

      m_description = description;
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
    * @param url the url, may be <code>null</code> or empty. Whitespace is
    * trimmed.
    */
   public void setURL(String url)
   {
      url = StringUtils.defaultString(url).trim();
      if (!url.equals(m_actionURL))
         setDirty();
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
    * Sets sort rank of this in its parent's children actions.
    *
    * @param rank the sort rank.
    */
   public void setSortRank(int rank)
   {
      if (rank != m_sortrank)
         setDirty();

      m_sortrank = rank;
   }

   /**
    * Sets the type for this action.
    *
    * @param type the type, may not be <code>null</code> or empty and must be
    * one of the TYPE_xxx values.
    *
    * @throws IllegalArgumentException if type is not valid.
   public void setType(String type)
   {
      if (validType(type))
         m_type = type;
   }
    */

   /**
    * Validate the specified type, must be one of the following:
    * <ul>
    *    <li>TYPE_MENUITEM</li>
    *    <li>TYPE_CASCADEDMENU</li>
    *    <li>TYPE_CONTEXTMENU</li>
    *    <li>TYPE_DYNAMICMENU</li>
    * </ul>
    *
    * @param type the type of menu, may not be <code>null</code> or empty
    * @return <code>true</code> if the type is one of the required types,
    *    otherwise <code>false</code>.
    */
   private boolean validateType(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");

      for (int i=0; i<ms_menuTypes.size(); i++)
      {
         if (type.equals(ms_menuTypes.get(i)))
            return true;
      }

      throw new IllegalArgumentException("invalid menu type");
   }

   /**
    * Is this a standard menu item?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isMenuItem()
   {
      return m_type.equals(TYPE_MENUITEM);
   }

   /**
    * Is this a cascaded menu?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isCascadedMenu()
   {
      return m_type.equals(TYPE_MENU) && !m_isDynamic;
   }

   /**
    * Is this a context menu?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isContextMenu()
   {
      return m_type.equals(TYPE_CONTEXTMENU);
   }

   /**
    * Is this a dynamic menu?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isDynamicMenu()
   {
      return m_type.equals(TYPE_MENU) && m_isDynamic;
   }

   /**
    * Sets the menu type.
    *
    * @param type the new menu type, never <code>null</code> or empty, must be
    *    a valid menu type. See {@link #validateType(String)} for more info.
    */
   public void setMenuType(String type)
   {
      validateType(type);

      if (!type.equals(m_type))
         setDirty();

      m_type = type;
   }

   /**
    * Gets the menu type.
    *
    * @return the menu type, never <code>null</code> or empty. It is one of
    *   the TYPE_XXX.
    */
   public String getMenuType()
   {
      return m_type;
   }

   /**
    * Finds whether the action to be handled by client or not. An action that
    * can not be handled by client is handled by server.
    *
    * @return <code>true</code> if it is, otherwise <code>false</code>.
    */
   public boolean isClientAction()
   {
      return m_handler.equals(HANDLER_CLIENT);
   }

   /**
    * Sets the flag which indicates whether the action is handled by client or
    * by server.
    *
    * @param isClient <code>true</code> if the action is handled by client;
    *   otherwise the action is handled by server.
    */
   public void setClientAction(boolean isClient)
   {
      if (isClient)
         m_handler = HANDLER_CLIENT;
      else
         m_handler = HANDLER_SERVER;
   }

   /**
    * A property is typically used to add custom behavior.
    *
    * @return The collection of all properties. The caller should edit this
    *    collection directly. Any changes made will be seen by this action.
    *    Never <code>null</code>.
    */
   public PSActionProperties getProperties()
   {
      return m_props;
   }

   /**
    * Gets the value of a given property name.
    *
    * @param propName the name of the property, may not be <code>null</code>
    *   or empty.
    *
    * @return the value of the property, it may be <code>null</code> if cannot
    *   find the specified property.
    */
   public String getProperty(String propName)
   {
      return m_props.getProperty(propName);
   }

   /**
    * Convenience method for getting a property with a boolean value.
    *
    * @param propName the name of the property, may not be <code>null</code>
    *   or empty.
    *
    * @return <code>true</code> if the value of the property is {@link #YES};
    *   otherwise, return <code>false</code>.
    */
   public boolean getPropertyBoolean(String propName)
   {
      String value = m_props.getProperty(propName);
      return value != null && value.equalsIgnoreCase(PSAction.YES);
   }

   /**
    * A visibility context is used to control when this action will be
    * visible.
    *
    * @return The collection of all contexts. The caller should edit this
    *    collection directly. Any changes made will be seen by this action.
    *    Never <code>null</code>.
    */
   public PSActionVisibilityContexts getVisibilityContexts()
   {
      return m_visContexts;
   }

   /**
    * Set the visibility contexts that is used to control when this action will
    * be visible.
    *
    * @param visCtxs the new visibility contexts, never <code>null</code>, may
    *    be empty.
    */
   public void setVisibilityContexts(PSActionVisibilityContexts visCtxs)
   {
      if (visCtxs == null)
         throw new IllegalArgumentException("visCtxs may not be null.");

      m_visContexts = visCtxs;
   }

   /**
    * A collection of action url parameters.
    *
    * @return The collection of all parameters. The caller must edit this
    *    collection directly. Any changes made will be seen by this action.
    *    Never <code>null</code>.
    */
   public PSActionParameters getParameters()
   {
      return m_params;
   }

   /**
    * Gets children actions of this action. Should be called only if the action
    * represents a menu as indicated by {@link #isCascadedMenu()} or
    * {@link #isDynamicMenu()}.
    * 
    * @return If this action represents a menu, then a valid object is returned,
    * otherwise, it may be empty, but never <code>null</code>.
    */
   public PSChildActions getChildren()
   {
      return m_children;
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
    *    version CDATA #REQUIRED
    *    >
    * </code></pre>
    *
    * @return the action element, never <code>null</code>.
    */
   @Override
   public Element toXml(Document doc)
   {
      return toXml(doc, true);
   }


   //see base class for description
   @Override
   protected Element toXml(Document doc, boolean includeChildComps)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      Element root = super.toXml(doc);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(LABEL_ATTR, m_label);
      root.setAttribute(TYPE_ATTR, m_type);

      /* If this is a dynamic menu, but the URL is empty, we set the url w/ a
       * placeholder value so that when it is restored, we know it is a dynamic
       * menu.
       */
      String urlValue = m_actionURL;
      if (m_isDynamic && m_actionURL.length() == 0)
      {
         urlValue = URL_PLACEHOLDER;
      }
      root.setAttribute(URL_ATTR, urlValue);
      root.setAttribute(HANDLER_ATTR, m_handler);
      root.setAttribute(SORTRANK_ATTR, String.valueOf(m_sortrank));
      root.setAttribute(VERSION_ATTR, String.valueOf(m_version));
      
      if (m_description.length() > 0)
      {
         PSXmlDocumentBuilder.addElement(doc, root, "Description",
               m_description);
      }

      if (includeChildComps)
      {
         if (!m_props.isEmpty())
            root.appendChild(m_props.toXml(doc));

         if (!m_params.isEmpty())
            root.appendChild(m_params.toXml(doc));

         if (!m_visContexts.isEmpty())
            root.appendChild(m_visContexts.toXml(doc));

         if (!m_modeUiContexts.isEmpty())
            root.appendChild(m_modeUiContexts.toXml(doc));

         if (!m_children.isEmpty())
            root.appendChild(m_children.toXml(doc));
      }

      return root;
   }

   /**
    * Must be overridden to properly deal with all the children of this
    * component.
    */
   @Override
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException
   {
      int state = getState();
      if (state != DBSTATE_MARKEDFORDELETE)
         super.toDbXml( doc,  root,  keyGen,  parent);
      PSKey key = getLocator();
      if (m_children.getState() != DBSTATE_UNMODIFIED)
         m_children.toDbXml(doc, root, keyGen, key);
      if (m_params.getState() != DBSTATE_UNMODIFIED)
         m_params.toDbXml(doc, root, keyGen, key);
      if (m_props.getState() != DBSTATE_UNMODIFIED)
         m_props.toDbXml(doc, root, keyGen, key);
      if (m_visContexts.getState() != DBSTATE_UNMODIFIED)
         m_visContexts.toDbXml(doc, root, keyGen, key);
      if (m_modeUiContexts.getState() != DBSTATE_UNMODIFIED)
         m_modeUiContexts.toDbXml(doc, root, keyGen, key);
      if (state == DBSTATE_MARKEDFORDELETE)
         super.toDbXml( doc,  root,  keyGen,  parent);
   }

   /**
    * Override to deal with all child components.
    */
   @Override
   public void setPersisted()
      throws PSCmsException
   {
      super.setPersisted();
      m_children.setPersisted();
      m_params.setPersisted();
      m_props.setPersisted();
      m_visContexts.setPersisted();
      m_modeUiContexts.setPersisted();
   }


   /**
    * Override to deal with all child components.
    */
   @Override
   public void markForDeletion()
   {
      super.markForDeletion();
      m_children.markForDeletion();
      m_params.markForDeletion();
      m_props.markForDeletion();
      m_visContexts.markForDeletion();
      m_modeUiContexts.markForDeletion();
   }

   /**
    * Merges PSAction objects based merge rules and returns the merge result
    * as PSAction object.
    *
    * @param newAction may be <code>null</code> in which case merge result will
    * be current action.
    *
    * @return merge result as PSAction object, <code>null</code> only when the
    * two actions to be merged are <code>null</code>.
    */
   public PSAction merge(PSAction newAction)
   {
      // if new action is null, merge result is current action.
      if (newAction == null)
         return this;
      
      // TODO apply complex merge rules. Right now, it filters for common child
      // actions based equality.
/*todo:
      PSAction child = null;
      PSAction newChild = null;

      ArrayList list = new ArrayList();
      Iterator iter = newAction.getChildren();
      if (iter.hasNext())
      {
         while (iter.hasNext())
         {
            child = (PSAction)iter.next();
            newChild = findChildAction(child);
            if (newChild != null)
            {
               child.merge(newChild);
               list.add(child);
            }
         }
         setChildren(list.iterator());
      }
      */
      return this;
   }

   /**
    * Method to find the supplied action as one of its
    * child actions. Test is made using the equals() method.
    *
    * @param action the action to test, must not be <code>null</code>.
    *
    * @return
   public PSAction findChildAction(PSAction action)
   {
      if (null == action)
         throw new IllegalArgumentException("Action cannot be null.");

      PSAction child = null;
      Iterator iter = getChildren();
      while (iter.hasNext())
      {
         child = (PSAction)iter.next();
         if (child.equals(action))
            return child;
      }
      return null;
   }
    */

   /**
    * Method to test if this action contains the supplied action as one of its
    * child actions. Test is made using the equals() method.
    *
    * @param action the action to test, must not be <code>null</code>.
    *
    * @return <code>true</code> if the supplied action is equivalent to one of
    * the child actions, <code>false</code> otherwise.
   public boolean containsChildAction(PSAction action)
   {
      PSAction child = null;
      Iterator iter = getChildren();
      while (iter.hasNext())
      {
         child = (PSAction)iter.next();
         if (child.equals(action))
            return true;
      }
      return false;
   }
    */

   /**
    * Used to compare the sort ranks of 2 <code>PSAction</code> objects. The
    * algorithm for sorting is that if the 2 sort ranks are the same we then
    * sort using the label of the object.
    *
    * @param obj1 the first <code>PSAction</code> object to compare,
    * assumed not <code>null</code>
    *
    * @param obj2 the second <code>PSAction</code> object to compare,
    * assumed not <code>null</code>
    *
    * @return a negative integer, zero, or a positive integer as the
    * first object's <code>sort</code> value is less than, equal to, or
    * greater than that of the second object, if the <code>sort</code> values
    * are equal it does a compare on the <code>label</code> field, the compare
    * is done case insensitive
    */
/**
   public static int compare(Object obj1, Object obj2)
   {
      PSAction a1 = (PSAction)obj1;
      PSAction a2 = (PSAction)obj2;

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
*/
   // see IPSDbComponent
   @Override
   public Object clone()
   {
      PSAction copy = null;
      copy = (PSAction) super.clone();

      copy.setName(m_name);
      copy.setLabel(m_label);
      copy.setDescription(m_description);
      copy.setMenuType(m_type);
      copy.setURL(m_actionURL);
      copy.setSortRank(m_sortrank);
      copy.m_handler = m_handler;
      copy.m_version = m_version;
      
      copy.m_params = (PSActionParameters) m_params.clone();
      copy.m_props = (PSActionProperties) m_props.clone();
      copy.m_visContexts = (PSActionVisibilityContexts) m_visContexts.clone();
      copy.m_modeUiContexts =
            (PSDbComponentCollection) m_modeUiContexts.clone();
      copy.m_children = (PSChildActions) m_children.clone();
      
      return copy;
   }

   //implements interface method.
   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;

      boolean equals = true;
      if( !(obj instanceof PSAction) )
         equals = false;
      else
      {
         if (!super.equals(obj))
            return false;
         PSAction other = (PSAction)obj;
         if(!m_name.equals(other.m_name))
            equals = false;
         else if(!m_label.equals(other.m_label))
            equals = false;
         else if(!m_description.equals(other.m_description))
            equals = false;
         else if(!m_type.equals(other.m_type))
            equals = false;
         else if(!m_actionURL.equals(other.m_actionURL))
            equals = false;
         else if(!m_handler.equals(other.m_handler))
            equals = false;
         else if(m_sortrank != other.m_sortrank)
            equals = false;
         else if(!m_props.equals(other.m_props))
            equals = false;
         else if(!m_params.equals(other.m_params))
            equals = false;
         else if(!m_visContexts.equals(other.m_visContexts))
            equals = false;
         else if(!m_children.equals(other.m_children))
            equals = false;
         else if (!m_modeUiContexts.equals(other.m_modeUiContexts))
            equals = false;
         else if (!m_version.equals(other.m_version))
            equals = false;
      }

      return equals;
   }

   //implements interface method.
   @Override
   public int hashCode()
   {
      int code = 0;

      code =
         super.hashCode() +
         m_name.hashCode() +
         m_label.hashCode() +
         m_description.hashCode() +
         m_type.hashCode() +
         m_handler.hashCode() +
         m_actionURL.hashCode() +
         m_sortrank +
         m_params.hashCode() +
         m_props.hashCode() +
         m_children.hashCode() +
         m_visContexts.hashCode() +
         m_modeUiContexts.hashCode() +
         m_version.hashCode();

      return code;
   }

   /**
    * We must calculate our state because all our children can be edited
    * outside of our knowledge. See base class for more details.
    */
   @Override
   public int getState()
   {
      int state = super.getState();
      if (state != DBSTATE_UNMODIFIED)
         return state;

      int[] states = new int[] {
         m_props.getState(),
         m_params.getState(),
         m_visContexts.getState(),
         m_modeUiContexts.getState(),
         m_children.getState()
      };
      for (int i=0; i < states.length; i++)
      {
         if (states[i] != DBSTATE_UNMODIFIED)
            return DBSTATE_MODIFIED;
      }
      return DBSTATE_UNMODIFIED;
   }

   /**
    * Sets the menu as dynamic or cascading depending on the
    * flag passed in. If cascading then the URL is cleared.
    * Does nothing if this action is not of type TYPE_MENU.
    * @param flag if <code>true</code> then set this menu as dynamic,
    * else if <code>false</code> then set to cascading.
    */
   public void setMenuDynamic(boolean flag)
   {
      if(m_type != null && !m_type.equals(TYPE_MENU))
         return;
      m_isDynamic = flag;
      // If cascading then clear URL
      if(!flag && !StringUtils.isBlank(getURL()))
         setURL("");
   }


   /**
    * See base class for description.
    *
    * @return Always <code>true</code>.
    */
   @Override
   protected boolean requiresActionNode()
   {
      return true;
   }

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
    * An action menu description, initialized while constructed, never
    * <code>null</code> after that, might be empty.
    */
   private String m_description = "";

   /**
    * The menu type of the action, initialized in the constructor and never
    * <code>null</code>, empty or modified after that. Will be one of the
    * TYPE_xxx values.
    */
   private String m_type;

   /**
    * Flag indicating that this menu is dynamic. This only
    *  applies to actions whose type is TYPE_MENU. If
    *  false then this is a cascading menu.
    */
   private boolean m_isDynamic;

   /**
    * The action url, initialized in the constructor and never <code>null</code>
    * or modified after that. May be empty.
    */
   private String m_actionURL = "";

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
    * {@link #PSAction(Element) constructor}. May be <code>null</code> if the
    * action does not have any properties associated with.
    */
   private PSActionProperties m_props = new PSActionProperties();

   /**
    * The list of parameters to apply to the action url, initialized in
    * {@link #PSAction(Element) constructor}. May be <code>null</code> if the
    * action url does not have any parameters associated with.
    */
   private PSActionParameters m_params = new PSActionParameters();

   /**
    * The list of visibility contexts associated with the action, initialized in
    * {@link #PSAction(Element) constructor}. May be <code>null</code> if the
    * action does not have any visibilty contexts associated with.
    */
   private PSActionVisibilityContexts m_visContexts =
         new PSActionVisibilityContexts();

   /**
    * The list of mode-uicontexts with the action, initialized in
    * {@link #PSAction(Element) constructor}. Never <code>null</code> may be
    * <code>empty</code>.
    */
   private PSDbComponentCollection m_modeUiContexts =
         new PSDbComponentCollection(PSMenuModeContextMapping.class);

   /**
    * If an action contains sub-actions (cascading menu item), they are stored
    * in this container. Never <code>null</code>.
    */
   private PSChildActions m_children = new PSChildActions();

   /**
    * The constant to indicate this action as 'menuitem'.
    */
   public static final String TYPE_MENUITEM = "MENUITEM";

   /**
    * The constant to indicate this action as cascaded or dynamic menu.
    */
   public static final String TYPE_MENU = "MENU";

   /**
    * The constant to indicate this action as 'context menu'.
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
   private static final List<String> ms_menuTypes = new ArrayList<>();
   static
   {
      ms_menuTypes.add(TYPE_MENUITEM);
      ms_menuTypes.add(TYPE_MENU);
      ms_menuTypes.add(TYPE_CONTEXTMENU);
   }

   /**
    * The list of action handlers.
    */
   private static final List<String> ms_handlers = new ArrayList<>();
   static
   {
      ms_handlers.add(HANDLER_CLIENT);
      ms_handlers.add(HANDLER_SERVER);
   }

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "PSXAction";

   /**
    * The boolean constant to indicate <code>true</code>.
    */
   public static final String YES = "yes";

   /**
    * The boolean constant to indicate <code>false</code>.
    */
   public static final String NO = "no";

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
    * Specifies whether to launch a new window.
    */
   public static final String PROP_LAUNCH_NEW_WND = "launchesWindow";

   /**
    * Specifies what needs to be refreshed after the action is performed.
    */
   public static final String PROP_REFRESH_HINT = "refreshHint";

   /**
    * Specifies that the attached Command supports batch processing.
    */
   public static final String PROP_MUTLI_SELECT = "SupportsMultiSelect";

   /**
    * The name of the property that defines url of the icon for this action.
    */
   public static final String PROP_SMALL_ICON = "SmallIcon";

   /**
    * The name of the property that defines description of this menu action.
    */
   public static final String PROP_DESCRIPTION = "Description";

   /**
    * The name of the property that defines the name of the target to which to
    * go after the action was executed.
    */
   public static final String PROP_TARGET = "target";

   /**
    * The name of the property that defines the name of the target style.
    */
   public static final String PROP_TARGET_STYLE = "targetStyle";


   /**
    * A list of valid values for the {@link #PROP_REFRESH_HINT} property
    */
   public enum RefreshHint
   {
      NONE("none"),
      PARENT("parent"),
      ROOT("root"),
      SELECTED("selected");
      
      /**
       * The value of the refresh hint, init by ctor, never <code>null</code>
       * or empty.
       */
      private String m_value;
      
      /**
       * Creates an object from the given value.
       * 
       * @param value the value of the object, it may not be <code>null</code>
       *    or empty.
       */
      private RefreshHint(String value)
      {
         if (value == null || value.trim().length() == 0)
            throw new IllegalArgumentException(
                  "value may not be null or empty.");
            
         m_value = value;
      }
      
      /**
       * @return the value of the object, never <code>null</code> or empty.
       */
      public String getValue()
      {
         return m_value;
      }
   }

   //xml constants
   private static final String LABEL_ATTR = "label";
   private static final String NAME_ATTR = "name";
   private static final String TYPE_ATTR = "type";
   private static final String URL_ATTR = "url";
   private static final String HANDLER_ATTR = "handler";
   private static final String SORTRANK_ATTR = "sortrank";
   private static final String VERSION_ATTR = "version";

   /**
    * The name of the table column that is the primary key.
    */
   public static final String PRIMARY_KEY = "ACTIONID";

   //main method for test purpose
   public static void main(String[] args)
   {
      try
      {
         PSAction action = new PSAction("testAction", "Test Action");
         PSActionVisibilityContext vis =
               new PSActionVisibilityContext("visctxname1", "visctxval1");
         PSActionVisibilityContexts visCtx = action.getVisibilityContexts();
         visCtx.add(vis);

         PSActionProperties props = action.getProperties();
         props.setProperty("propname1", "propval1");
         props.setProperty("propname2", "propval2");

         PSActionParameters params = action.getParameters();
         params.setParameter("apname1", "apval1");



         print(action);

         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         PSAction action2 = new PSAction(action.toXml(doc));
         System.out.println("New action from xml");
         print(action2);

         System.out.println("Compare 2 actions: " + action.equals(action2));

         vis.add("visctxval2");
         vis.add("visctxval3");
         print(action);
         System.out.println("Compare 2 actions (should not equal): " + action.equals(action2));

         vis.remove("visctxval2");
         vis.remove("visctxval3");
         System.out.println("Compare 2 actions (should equal): " + action.equals(action2));

      }
      catch (PSUnknownNodeTypeException unte)
      {
         log.error(unte.getLocalizedMessage());
         log.error(unte.getMessage());
         log.debug(unte.getMessage(), unte);
      }

   }

   private static void print(PSAction action)
   {
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(doc, "Actions");
         root.appendChild(action.toXml(doc));
         PSXmlDocumentBuilder.write(doc, System.out);
      }
      catch (java.io.IOException ioe)
      {
         log.error(ioe.getMessage());
         log.debug(ioe.getMessage(), ioe);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(java.lang.Object,
    * long)
    */
   @SuppressWarnings("unchecked")
   public Object tuneClone(long newId)
   {
      PSKey newKey = createKey(newId + "");
      setKey(newKey);
      Iterator cols = m_params.iterator();
      while (cols.hasNext())
      {
         PSActionParameter col = (PSActionParameter) cols.next();
         col.setKey(newKey);
      }
      Iterator props = m_props.iterator();
      while (props.hasNext())
      {
         PSActionProperty prop = (PSActionProperty) props.next();
         prop.setKey(newKey);
      }
      // Clone is always server action.
      setClientAction(false);
      return this;
   }

   /**
    * The value used for the action URL if this is a dynamic menu but the url
    * has not been set yet. It's presence is used as a flag to indicate a 
    * dynamic menu.
    */
   private static final String URL_PLACEHOLDER = "???";
}
