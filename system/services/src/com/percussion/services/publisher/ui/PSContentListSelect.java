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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.publisher.ui;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.rx.publisher.jsf.data.PSParameter;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * JSF managed bean to handle the content list views. This model holds state
 * data, including what contentlist is currently being edited. It presents
 * action methods (like save and newlist) for use by the JSF pages, as well as
 * action methods on the inner Entry class to handle per item actions.
 * <p>
 * This object is instantiated by the JSF framework, and is tied to a user's
 * session. This should make this safe - the exception would be the user
 * modifying two contentlists in different tabs with the same session.
 * <p>
 * Most values in the current content list are accessed through helper methods
 * in this class, rather than directly by the framework. This enables us to
 * validate the values and translate from internal to external forms. If we only
 * needed to do the translations then converters would have also been an option.
 * 
 * @author dougrand
 */
public class PSContentListSelect
{
   private static final String CLIST_URL = "/Rhythmyx/contentlist";

   /**
    * Hold the information for a single content list entry for the summary list.
    */
   public class Entry
   {
      long mi_id;

      String mi_name;

      String mi_url;

      PSEditionType mi_editionType;

      /**
       * Ctor
       * 
       * @param l the content list, never <code>null</code>
       */
      public Entry(IPSContentList l) {
         if (l == null)
         {
            throw new IllegalArgumentException("l may not be null");
         }
         mi_id = l.getGUID().longValue();
         mi_name = l.getName();
         mi_url = l.getUrl();
         mi_editionType = l.getEditionType();
      }

      /**
       * @return Returns the editionType.
       */
      public PSEditionType getEditionType()
      {
         return mi_editionType;
      }

      /**
       * @return Returns the id.
       */
      public long getId()
      {
         return mi_id;
      }

      /**
       * @return Returns the name.
       */
      public String getName()
      {
         return mi_name;
      }

      /**
       * @return Returns the url.
       */
      public String getUrl()
      {
         return StringUtils.abbreviate(mi_url,80);
      }

      /**
       * Calculate the edition type icon to use in the UI
       * 
       * @return the icon name, never <code>null</code> or empty
       */
      public String getEditionTypeIcon()
      {
         int index = mi_editionType.ordinal() + 1;
         return "pubeditiontype" + index + ".gif";
      }

      /**
       * Calculate the alt and title attribute value for the edition type icon
       * to use in the UI.
       * 
       * @return the alt/title value with first character uppercase and the
       * remainder lowercase, never <code>null</code> or empty
       */
      public String getEditionTypeText()
      {
         String text;
         String name = mi_editionType.name().toUpperCase();
         
         if (name.length() > 1)
         {
            String firstChar = name.substring(0, 1);
            String remainingStr = name.substring(1);
            text = firstChar + remainingStr.toLowerCase();
         }
         else
            text = name;
         
         return text;
      }
      
      /**
       * Action listener that is fired if a list is deleted
       * 
       * @return the name of the outcome
       * @throws PSPublisherException
       */
      public String delete() throws PSPublisherException
      {
         IPSPublisherService pub = PSPublisherServiceLocator
               .getPublisherService();
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         ids.add(new PSGuid(PSTypeEnum.CONTENT_LIST, mi_id));
         List<IPSContentList> list = pub.loadContentLists(ids);
         pub.deleteContentLists(list);
         return null;
      }

      /**
       * Action listener that is fired if a list is edited
       * 
       * @return the name of the outcome
       * @throws PSPublisherException
       */
      public String edit() throws PSPublisherException
      {
         IPSPublisherService pub = PSPublisherServiceLocator
               .getPublisherService();
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         ids.add(new PSGuid(PSTypeEnum.CONTENT_LIST, mi_id));
         setCurrent(pub.loadContentLists(ids).get(0));
         return "edit";
      }
   }

   /**
    * Filter to use when retrieving content lists
    */
   private String m_namefilter = "";

   /**
    * Current item being edited
    */
   IPSContentList m_current = null;

   /**
    * Set to <code>true</code> when the params should be rebuilt on the next
    * access
    */
   boolean m_resetGenParams = false;

   /**
    * Set to <code>true</code> when the params should be rebuilt on the next
    * access
    */
   boolean m_resetExpParams = false;

   /**
    * The current generator parameters being edited
    */
   List<PSParameter> m_currentGenParams = null;

   /**
    * The current expander parameters being edited
    */
   List<PSParameter> m_currentExpParams = null;

   /**
    * The current generator
    */
   String m_generator = null;

   /**
    * The current expander
    */
   String m_expander = null;
   
   /**
    * The name of the content list to copy
    */
   String m_copyItemName = null;
   
   /**
    * The name to give the new content list
    */
   String m_copyItemNewName = null;

   /**
    * Publisher ui component, wired by JSF
    */
   private PSPublisherUI m_pubui = null;

   /**
    * Ctor
    */
   public PSContentListSelect() {
   }

   /**
    * @return Returns the entries.
    * @throws PSPublisherException
    */
   public List<Entry> getEntries()
   {
      List<Entry> rval = new ArrayList<Entry>();
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      List<IPSContentList> lists = pub.findAllContentLists(m_namefilter);
      for (IPSContentList list : lists)
      {
         Entry newentry = new Entry(list);
         rval.add(newentry);
      }

      return rval;
   }
   
   /**
    * Get the selection items
    * @return the selection items, never <code>null</code>
    */
   public SelectItem[] getCopyItems() 
   {
     List<Entry> entries = getEntries();
     SelectItem[] rval = new SelectItem[entries.size()];
     for(int i = 0; i < entries.size(); i++)
     {
        Entry e = entries.get(i);
        rval[i] = new SelectItem(e.getName(),e.getName());
     }
     return rval;
   }

   /**
    * @return Returns the namefilter.
    */
   public String getNamefilter()
   {
      return m_namefilter;
   }

   /**
    * @param namefilter The namefilter to set.
    */
   public void setNamefilter(String namefilter)
   {
      m_namefilter = namefilter;
   }

   /**
    * Simple action that just causes the page to be refreshed
    * 
    * @return <code>null</code> all the time
    */
   public String filter()
   {
      return null;
   }

   /**
    * @return Returns the current.
    */
   public IPSContentList getCurrent()
   {
      return m_current;
   }

   /**
    * @param current The current to set.
    */
   public void setCurrent(IPSContentList current)
   {
      m_current = current;

      m_currentGenParams = new ArrayList<PSParameter>();
      if (m_current != null)
      {
         Map<String, String> args = m_current.getGeneratorParams();
         String gen = m_current.getGenerator();
         if (!StringUtils.isBlank(gen))
         {
            PSPair<String, String> params[] = m_pubui.getParameters(gen);
            for (PSPair<String, String> param : params)
            {
               m_currentGenParams.add(new PSParameter(param.getFirst(), param
                     .getSecond(), args.get(param.getFirst())));
            }
         }
      }
      m_currentExpParams = new ArrayList<PSParameter>();
      if (m_current != null)
      {
         Map<String, String> args = m_current.getExpanderParams();
         String exp = m_current.getExpander();
         if (!StringUtils.isBlank(exp))
         {
            PSPair<String, String> params[] = m_pubui.getParameters(exp);
            for (PSPair<String, String> param : params)
            {
               m_currentExpParams.add(new PSParameter(param.getFirst(), param
                     .getSecond(), args.get(param.getFirst())));
            }
         }
      }

      if (m_current != null)
      {
         m_expander = m_current.getExpander();
         m_generator = m_current.getGenerator();
      }
      else
      {
         m_expander = "";
         m_generator = "";
      }
   }

   /**
    * Get a UI value for the edition type
    * 
    * @return the edition type name, never <code>null</code>
    */
   public String getEditiontype()
   {
      if (m_current != null)
      {
         return m_current.getEditionType().name();
      }
      else
      {
         return "";
      }
   }

   /**
    * Set a new UI value for the edition type, which is translated to a new enum
    * value
    * 
    * @param newvalue the new value, never <code>null</code>
    */
   public void setEditiontype(String newvalue)
   {
      if (newvalue == null)
      {
         throw new IllegalArgumentException("newvalue may not be null");
      }
      if (m_current != null)
      {
         if (!StringUtils.isBlank(newvalue))
            m_current.setEditionType(PSEditionType.valueOf(newvalue));
         else
            m_current.setEditionType(PSEditionType.AUTOMATIC);
      }
   }

   /**
    * Get the current editable object's name
    * 
    * @return the name
    */
   public String getName()
   {
      return m_current != null ? m_current.getName() : "";
   }

   /**
    * Set the current editable object's name
    * 
    * @param name the name, never <code>null</code> or empty
    */
   public void setName(String name)
   {
      if (m_current == null)
         return;

      name = name.trim();

      m_current.setName(name);
   }

   /**
    * Get the current editable object's url
    * 
    * @return the url
    */
   public String getUrl()
   {
      return m_current != null ? m_current.getUrl() : "";
   }

   /**
    * Set the current editable object's url. This must obey some specific rules
    * 
    * @param url the url, never <code>null</code> or empty
    */
   public void setUrl(String url)
   {
      if (m_current == null)
         return;
      if (StringUtils.isBlank(url))
      {
         throw new IllegalArgumentException("url may not be null or empty");
      }
      url = url.trim();

      // Fixup the name in the url if this is a "modern" content list
      if (url.contains("/contentlist"))
      {
         boolean hasq = url.contains("?");
         if (!url.contains(IPSHtmlParameters.SYS_DELIVERYTYPE))
         {
            if (hasq)
            {
               url = url + "&";
            }
            else
            {
               url = url + "?";
               hasq = true;
            }
            url = url + IPSHtmlParameters.SYS_DELIVERYTYPE + "=filesystem";
         }
         if (!url.contains(IPSHtmlParameters.SYS_CONTENTLIST))
         {
            if (hasq)
            {
               url = url + "&";
            }
            else
            {
               url = url + "?";
               hasq = true;
            }
            url = url + IPSHtmlParameters.SYS_CONTENTLIST + "=temp";
         }
         url = PSUrlUtils.replaceUrlParameterValue(url,
               IPSHtmlParameters.SYS_CONTENTLIST, m_current.getName());
      }

      m_current.setUrl(url);
   }

   /**
    * @return Returns the expander.
    */
   public String getExpander()
   {
      return m_expander;
   }

   /**
    * @param expander The expander to set.
    */
   public void setExpander(String expander)
   {
      m_expander = expander;
      m_resetExpParams = true;
   }

   /**
    * @return Returns the generator.
    */
   public String getGenerator()
   {
      return m_generator;
   }

   /**
    * @param generator The generator to set.
    */
   public void setGenerator(String generator)
   {
      m_generator = generator;
      m_resetGenParams = true;
   }

   /**
    * @return Returns the copyItemName.
    */
   public String getCopyItemName()
   {
      return m_copyItemName;
   }

   /**
    * @param copyItemName The copyItemName to set.
    */
   public void setCopyItemName(String copyItemName)
   {
      m_copyItemName = copyItemName;
   }

   /**
    * @return Returns the copyItemNewName.
    */
   public String getCopyItemNewName()
   {
      return m_copyItemNewName;
   }

   /**
    * @param copyItemNewName The copyItemNewName to set.
    */
   public void setCopyItemNewName(String copyItemNewName)
   {
      m_copyItemNewName = copyItemNewName;
   }

   /**
    * Accepts events from the generator and expander drop downs. Reset the
    * shown values for both in this case - this is cheap enough. Later we
    * can make this more specific if there's a reason.
    * 
    * @param event the event from JSF
    */
   @SuppressWarnings("unused") 
   public void selectValueChanged(ValueChangeEvent event)
   {
      m_resetGenParams = true;
      m_resetExpParams = true;
   }
   
   /**
    * Adjust the parameters in the lists to be displayed
    * 
    * @param extension the extension name
    * @param params the params to be adjusted, assumed not <code>null</code>
    * @return the adjusted item list
    */
   private List<PSParameter> adjustParameters(String extension, List<PSParameter> params)
   {
      if (StringUtils.isBlank(extension))
      {
         params.clear();
         return params;
      }

      // Store the old data
      Map<String, String> pdata = new HashMap<String, String>();
      for (PSParameter i : params)
      {
         if (i.getValue() == null)
            continue;
         pdata.put(i.getName(), i.getValue());
      }

      // Clear old data
      params.clear();

      PSPair<String, String>[] declared = m_pubui.getParameters(extension);

      // Create new
      for (PSPair<String, String> pdecl : declared)
      {
         String name = pdecl.getFirst();
         String desc = pdecl.getSecond();
         String value = pdata.get(name);
         params.add(new PSParameter(name, desc, value));
      }
      return params;
   }

   /**
    * Gets a list of generator args.
    * 
    * @return the list of args. The elements in each array are: param name,
    *         param value, tag id, description
    */
   public List<PSParameter> getGeneratorArguments()
   {
      if (m_resetGenParams)
      {
         // Adjust the list of parameters
         if (m_currentGenParams == null)
         {
            m_currentGenParams = new ArrayList<PSParameter>();
         }
         m_currentGenParams = adjustParameters(m_generator, m_currentGenParams);
         m_resetGenParams = false;
      }
      return m_currentGenParams;
   }

   /**
    * The parameters to save for the generator, does not remove any unneeded
    * parameters.
    * 
    * @param params the args to save, never <code>null</code>
    */
   public void setGeneratorArguments(List<PSParameter> params)
   {
      if (params == null)
      {
         throw new IllegalArgumentException("params may not be null");
      }
      m_currentGenParams = params;
   }

   /**
    * Gets a list of expander args.
    * 
    * @return the list of args. The elements in each array are: param name,
    *         param value, tag id, description
    */
   public List<PSParameter> getExpanderArguments()
   {
      if (m_resetExpParams)
      {
         // Adjust the list of parameters
         if (m_currentExpParams == null)
         {
            m_currentExpParams = new ArrayList<PSParameter>();
         }
         m_currentExpParams = adjustParameters(m_expander, m_currentExpParams);
         m_resetExpParams = false;
      }

      return m_currentExpParams;
   }

   /**
    * The parameters to save for the expander, does not remove any unneeded
    * parameters.
    * 
    * @param params the args to save, never <code>null</code>
    */
   public void setExpanderArguments(List<PSParameter> params)
   {
      if (params == null)
      {
         throw new IllegalArgumentException("params may not be null");
      }
      m_currentExpParams = params;
   }

   /**
    * Access the current content list's filter name
    * 
    * @return the filter name, never <code>null</code> but may be empty
    */
   public String getFilterName()
   {
      if (m_current == null || m_current.getFilter() == null)
      {
         return "--undefined--";
      }
      else
      {
         return m_current.getFilter().getName();
      }
   }

   /**
    * Set a new filter on the current content list
    * 
    * @param name the name, may be empty
    * @throws PSFilterException
    */
   public void setFilterName(String name) throws PSFilterException
   {
      if (m_current == null)
         return;

      if (StringUtils.isBlank(name) || name.equals("--undefined--"))
      {
         m_current.setFilterId(null);
      }
      else
      {
         if (m_current.getFilter() != null
               && m_current.getFilter().getName().equals(name))
            return;
         IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
         m_current.setFilterId(fsvc.findFilterByName(name).getGUID());
      }
   }
   
   /**
    * Get the type of the current content list
    * @return the type
    */
   public int getType() 
   {
      return m_current.getType().ordinal();
   }

   /**
    * Set the new type of the current content list
    * @param ordinal the new type
    */
   public void setType(int ordinal)
   {
      m_current.setType(IPSContentList.Type.valueOf(ordinal));
   }

   /**
    * @return Returns the pubui.
    */
   public PSPublisherUI getPubui()
   {
      return m_pubui;
   }

   /**
    * @param pubui The pubui to set.
    */
   public void setPubui(PSPublisherUI pubui)
   {
      m_pubui = pubui;
   }

   /**
    * Action that is fired if a new list should be created
    * 
    * @return the name of the outcome
    */
   public String newlist()
   {
      IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      m_current = pub.createContentList("newlist");
      String name = "new_" + m_current.getGUID().toString();
      m_current.setEditionType(PSEditionType.AUTOMATIC);
      m_current.setUrl(CLIST_URL
            + "?sys_deliverytype=filesystem&sys_contentlist=" + name);
      m_current.setName(name);
      try
      {
         IPSItemFilter filter = fsvc.findFilterByName("public");
         m_current.setFilterId(filter.getGUID());
      }
      catch (PSFilterException e)
      {
         // Ignore
      }
      return "edit";
   }

   /**
    * Validate that the item filter value is appropriate for the kind of content
    * list we are editing
    * 
    * @param context the faces context
    * @param component the ui component
    * @param value the value in question
    */
   public void validateItemFilter(@SuppressWarnings("unused")
   FacesContext context, @SuppressWarnings("unused")
   UIComponent component, Object value)
   {
      if (m_current != null)
      {
         String url = m_current.getUrl();
         if (url.startsWith(CLIST_URL) && value.equals("--undefined--"))
         {
            String message = PSI18nUtils.getString("jsf@missing_item_filter");
            FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                  message, message);
            throw new ValidatorException(m);
         }
      }
   }

   /**
    * Action that is fired if a list should be saved
    * 
    * @return the name of the outcome
    * @throws PSPublisherException
    */
   public String save()
   {
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();

      if (m_current != null)
      {
         List<IPSContentList> lists = new ArrayList<IPSContentList>();
         lists.add(m_current);
         // Copy expander and generator params back to the original object
         copyParams();
         m_currentExpParams = null;
         m_currentGenParams = null;
         m_current = null;
         pub.saveContentLists(lists);
      }
      return "save";
   }
   
   /**
    * Copy the selected content list
    * @return the name of the outcome
    * @throws PSPublisherException
    */
   public String copy()
   {
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();

      if (m_copyItemName != null)
      {
         PSContentList clist = (PSContentList) pub.loadContentList(m_copyItemName);
         PSContentList nclist = (PSContentList) pub
               .findContentListByName(m_copyItemNewName);
         if (nclist != null)
         {
            // If we found the new one, then a different name is needed
            String message = PSI18nUtils.getString("jsf@clist_unique_name");
            FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                  "", message);
            FacesContext.getCurrentInstance().addMessage("name", m);        
            return "fail";
         }
         // Clone with new name
         clist = clist.clone();
         if (StringUtils.isNotBlank(m_copyItemNewName))
         {
            clist.setName(m_copyItemNewName);
            String url = clist.getUrl();
            url = PSUrlUtils.replaceUrlParameterValue(url,
                  IPSHtmlParameters.SYS_CONTENTLIST, m_copyItemNewName);
            clist.setUrl(url);
         }
         List<IPSContentList> tosave = new ArrayList<IPSContentList>();
         tosave.add(clist);
         pub.saveContentLists(tosave);
      }
      return "copy";
   }
   
   /**
    * Transition to copylist page
    * @return outcome
    */
   public String copylist() 
   {
      return "copy";
   }

   /**
    * Copy expander parameters back to the original contentlist
    */
   private void copyParams()
   {
      Set<String> allexpkeys = extractKeys(m_currentExpParams);
      Set<String> allgenkeys = extractKeys(m_currentGenParams);

      // Generator parameters
      Map<String, String> params = m_current.getGeneratorParams();
      for (String key : params.keySet())
      {
         if (!allgenkeys.contains(key))
         {
            m_current.removeGeneratorParam(key);
         }
      }

      for (PSParameter i : m_currentGenParams)
      {
         m_current.addGeneratorParam(i.getName(), i.getValue());
      }

      // Expander parameters
      params = m_current.getExpanderParams();
      for (String key : params.keySet())
      {
         if (!allexpkeys.contains(key))
         {
            m_current.removeExpanderParam(key);
         }
      }

      for (PSParameter i : m_currentExpParams)
      {
         m_current.addExpanderParam(i.getName(), i.getValue());
      }

      m_current.setExpander(m_expander);
      m_current.setGenerator(m_generator);
   }

   /**
    * Extract the keys from a list of items
    * 
    * @param currentParams the list to extract from, assumed not
    *           <code>null</code>
    * @return the list of names, never <code>null</code>
    */
   private Set<String> extractKeys(List<PSParameter> currentParams)
   {
      Set<String> rval = new HashSet<String>();

      for (PSParameter i : currentParams)
      {
         rval.add(i.getName());
      }
      return rval;
   }

   /**
    * Action that is fired when an edit is complete, but the results should not
    * be saved
    * 
    * @return the name of the outcome
    */
   public String cancel()
   {
      setCurrent(null);
      m_copyItemName = null;
      m_copyItemNewName = null;
      return "cancel";
   }
   
   /**
    * Are there validation errors? Used to conditionally show certain controls.
    * @return <code>true</code> if there's a message in the context and the
    * severity isn't info, <code>false</code> otherwise
    */
   @SuppressWarnings("unchecked")
   public Boolean getShowErrorControl()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      Iterator<FacesMessage> iter = ctx.getMessages();
      while(iter.hasNext())
      {
         FacesMessage m = iter.next();
         if (! m.getSeverity().equals(FacesMessage.SEVERITY_INFO))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object arg0)
   {
      return EqualsBuilder.reflectionEquals(this, arg0);
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

}
