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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.publisher.jsf.data.PSParameter;
import com.percussion.services.assembly.jexl.PSStringUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.services.publisher.ui.PSPublisherUI;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * A content list.
 * 
 * @author dougrand
 */
public class PSContentListNode extends PSDesignNode
{
   /**
    * Log.
    */
   public final static Log ms_log = LogFactory.getLog(PSContentListNode.class);

   /**
    * Utils.
    */
   public final static PSStringUtils ms_strutils = new PSStringUtils();

   /**
    * Pub ui.
    */
   public final static PSPublisherUI ms_pubui = new PSPublisherUI();

   /**
    * These params are skipped for the "extra" params field.
    */
   public static Set<String> ms_omit_params = new HashSet<>();

   /*
    * Initialize
    */
   static
   {
      ms_omit_params.add(IPSHtmlParameters.SYS_DELIVERYTYPE);
      ms_omit_params.add(IPSHtmlParameters.SYS_CONTENTLIST);
   }

   /**
    * The content list, set in ctor.
    */
   private IPSContentList m_clist;

   /**
    * Set to <code>true</code> when the params should be rebuilt on the next
    * access.
    */
   boolean m_resetGenParams = false;

   /**
    * Set to <code>true</code> when the params should be rebuilt on the next
    * access.
    */
   boolean m_resetExpParams = false;

   /**
    * The current generator parameters being edited.
    */
   List<PSParameter> m_currentGenParams = null;

   /**
    * The current expander parameters being edited.
    */
   List<PSParameter> m_currentExpParams = null;

   /**
    * The current generator.
    */
   String m_generator = null;

   /**
    * The current expander.
    */
   String m_expander = null;

   /**
    * The url parameters to allow simpler manipulation.
    */
   Map<String, String> m_urlParams = new HashMap<>();

   /**
    * The base portion of the url, without the parameters.
    */
   String m_urlBase = null;

   /**
    * Ctor.
    * 
    * @param contentlist the content list, never <code>null</code>.
    */
   public PSContentListNode(IPSContentList contentlist) {
      super(contentlist.getName(), contentlist.getGUID());
      Map<String, String> props = new HashMap<>();
      props.put("description", contentlist.getDescription());
      props.put("type", contentlist.getType().name());
      setProperties(props);
      m_clist = contentlist;
   }

   /**
    * Handle the details of adding a new content list which includes persisting
    * the list and starting the editor.
    * 
    * @param parent The container to receive the new child, never
    * <code>null</code>.
    * @param clNode The new node containing the new content list, never
    * <code>null</code>.
    * 
    * @return The outcome used by the navigation system to decide where to go
    * next or <code>null</code> if there's an error.
    */
   static String handleNewContentList(PSContentListViewNode parent,
         PSContentListNode clNode)
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      try
      {
         psvc.saveContentLists(Collections.singletonList(clNode
               .getContentList()));
      }
      catch (Exception e)
      {
         ms_log.error("Content list saving failure", e);
         return null;
      }
      return clNode.editNewNode(parent, clNode);
   }
   
   /**
    * @return Returns the editionType.
    */
   public int getEditionType()
   {
      return m_clist.getEditionType().getTypeId();
   }

   /**
    * Set a new type.
    * 
    * @param type the new edition type
    */
   public void setEditionType(int type)
   {
      m_clist.setEditionType(PSEditionType.valueOf(type));
   }

   /**
    * @return Returns the id.
    */
   public IPSContentList getContentList()
   {
      return m_clist;
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
    * Adjust the parameters in the lists to be displayed.
    * 
    * @param extension the extension name
    * @param params the params to be adjusted, assumed not <code>null</code>
    * @return the adjusted item list
    */
   private List<PSParameter> adjustParameters(String extension,
         List<PSParameter> params)
   {
      if (StringUtils.isBlank(extension))
      {
         params.clear();
         return params;
      }

      // Store the old data
      Map<String, String> pdata = new HashMap<>();
      for (PSParameter i : params)
      {
         if (i.getValue() == null)
            continue;
         pdata.put(i.getName(), i.getValue());
      }

      // Clear old data
      params.clear();

      PSPair<String, String>[] declared = ms_pubui.getParameters(extension);

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
    * Get the publisher ui information for use in the user interface.
    * 
    * @return the pub ui object, never <code>null</code>.
    */
   public PSPublisherUI getPubui()
   {
      return ms_pubui;
   }

   /**
    * Look at the data in the content list and decide if this is an "old"
    * content list. An old content list will have no generator, expander or
    * filter and will have a url that goes down several levels of either the
    * form <code>/Rhythmyx/app/res</code> or <code>../app/res</code>.
    * 
    * @return <code>true</code> if this is a legacy content list
    */
   public boolean getLegacy()
   {
      if (StringUtils.isNotBlank(m_clist.getExpander())
            || StringUtils.isNotBlank(m_clist.getGenerator())
            || m_clist.getFilter() != null)
      {
         return false;
      }

      String url = m_clist.getUrl();

      if (url.startsWith("../"))
      {
         url = "/Rhythmyx" + url.substring(2);
      }

      int i = url.indexOf('/');
      int count = 0;
      while (i >= 0)
      {
         count++;
         i = url.indexOf('/', i + 1);
      }

      return count > 2;
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
            m_currentGenParams = new ArrayList<>();
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
            m_currentExpParams = new ArrayList<>();
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
    * Access the current content list's filter name.
    * 
    * @return the filter name, never <code>null</code> but may be empty
    */
   public String getFilterName()
   {
      if (m_clist == null || m_clist.getFilterId() == null)
      {
         return null;
      }
      else
      {
         if (m_clist.getFilter() != null)
            return m_clist.getFilter().getName();
         
         IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
         try
         {
            IPSItemFilter filter = fsvc.loadFilter(m_clist.getFilterId());
            ((PSContentList) m_clist).setFilter(filter);
            return m_clist.getFilter().getName();
         }
         catch (PSNotFoundException e)
         {
            return null;
         }
      }
   }

   /**
    * Set a new filter on the current content list.
    * 
    * @param name the name, may be empty
    * @throws PSFilterException
    */
   public void setFilterName(String name) throws PSFilterException
   {
      if (m_clist == null)
         return;

      if (StringUtils.isBlank(name))
      {
         m_clist.setFilterId(null);
      }
      else
      {
         if (m_clist.getFilter() != null
               && m_clist.getFilter().getName().equals(name))
            return;
         IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
         m_clist.setFilterId(fsvc.findFilterByName(name).getGUID());
      }
   }

   /**
    * Determines if the type of Content List is incremental. 
    * @return <code>true</code> if the type is incremental.
    */
   public boolean isIncremental()
   {
      return m_clist.getType().ordinal() == IPSContentList.Type.INCREMENTAL.ordinal();
   }
   
   /**
    * Set the type of Content List
    * @param isIncremental it is <code>true</code> if the type is set to
    *    {@link IPSContentList.Type#INCREMENTAL}; otherwise set the type to 
    *    {@link IPSContentList.Type#NORMAL}
    */
   public void setIncremental(boolean isIncremental)
   {
      if (isIncremental)
         m_clist.setType(IPSContentList.Type.INCREMENTAL);
      else
         m_clist.setType(IPSContentList.Type.NORMAL);
   }
   
   /**
    * Get the type of the current content list.
    * 
    * @return the type
    */
   public int getType()
   {
      return m_clist.getType().ordinal();
   }

   /**
    * Set the new type of the current content list.
    * 
    * @param ordinal the new type
    */
   public void setType(int ordinal)
   {
      m_clist.setType(IPSContentList.Type.valueOf(ordinal));
   }

   /**
    * Calculate the alt and title attribute value for the edition type icon to
    * use in the UI.
    * 
    * @return the alt/title value with first character uppercase and the
    *         remainder lowercase, never <code>null</code> or empty
    */
   public String getEditionTypeText()
   {
      String text;
      String name = m_clist.getEditionType().name().toUpperCase();

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
    * Action listener that is fired if a list is deleted.
    * 
    * @return the name of the outcome
    */
   @Override
   public String delete()
   {
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      try
      {
         List<IPSContentList> list = pub.loadContentLists(Collections
               .singletonList(m_clist.getGUID()));
         pub.deleteContentLists(list);
         // Remove from parent
         remove();
      }
      catch (Exception e)
      {
         ms_log.error("Problem deleting content list", e);
      }
      return navigateToList();
   }

   @Override
   public String navigateToList()
   {
      return "pub-design-content-list-views";
   }

   /**
    * @return the urlParams
    */
   public Map<String, String> getUrlParams()
   {
      return m_urlParams;
   }

   /**
    * Get the params that don't have controls.
    * 
    * @return the params, never <code>null</code> but could be empty.
    * @throws UnsupportedEncodingException
    */
   public String getUrlExtraParams() throws UnsupportedEncodingException
   {

      StringBuilder rval = new StringBuilder();
      for (Map.Entry<String, String> pentry : m_urlParams.entrySet())
      {
         String name = pentry.getKey();
         if (ms_omit_params.contains(name))
            continue;
         if (rval.length() > 0)
            rval.append("&");
         rval.append(name);
         rval.append('=');
         rval.append(URLEncoder.encode(pentry.getValue(), "UTF8"));
      }
      return rval.toString();
   }

   /**
    * Set the params that don't have controls.
    * 
    * @param params the params, if there params that have controls then the
    *            value may or may not be overridden given the timing.
    * @throws UnsupportedEncodingException
    */
   public void setUrlExtraParams(String params)
         throws UnsupportedEncodingException
   {
      PSStringUtils sutils = new PSStringUtils();
      Map<String, String> mparams = (StringUtils.isNotBlank(params)
            ? sutils.stringToMap(params, PSCharSets.rxJavaEnc())
            : new HashMap<String, String>());
      for (String key : m_urlParams.keySet())
      {
         if (ms_omit_params.contains(key))
         {
            mparams.put(key, m_urlParams.get(key));
         }
      }
      setUrlParams(mparams);
   }

   /**
    * @param urlParams the urlParams to set
    */
   public void setUrlParams(Map<String, String> urlParams)
   {
      urlParams.remove("sys_assembly_context");
      m_urlParams = urlParams;
   }

   /**
    * @return the urlBase
    */
   public String getUrlBase()
   {
      return m_urlBase;
   }

   /**
    * @param urlBase the urlBase to set
    */
   public void setUrlBase(String urlBase)
   {
      m_urlBase = urlBase;
   }

   /**
    * Get the delivery types for the UI.
    * 
    * @return the delivery types, never <code>null</code>.
    */
   public List<SelectItem> getDeliveryTypes()
   {
      return getSelectionFromContainer(PSDeliveryTypeContainerNode.NODE_TITLE);
   }

   /**
    * Recalculate properties and such from the contained object.
    */
   public void update()
   {
      String url = m_clist.getUrl();
      if (StringUtils.isNotBlank(url))
      {
         int q = url.indexOf('?');
         if (q > -1)
         {
            String query = url.substring(q + 1);
            m_urlBase = url.substring(0, q);

            try
            {
               Map<String, String> urlParams = ms_strutils.stringToMap(query,
                     PSCharSets.rxJavaEnc());
               setUrlParams(urlParams);
            }
            catch (UnsupportedEncodingException e)
            {
               ms_log.error("Problem parsing content list url: " + url, e);
            }
         }
      }
   }

   /**
    * Save the content list.
    * 
    * @return the result outcome
    * @throws Exception
    */
   public String save() throws Exception
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      if (!getLegacy())
      {
         StringBuilder url = new StringBuilder(m_urlBase);
         url.append('?');
         boolean first = true;
         boolean setname = false;
         for (Map.Entry<String, String> entry : m_urlParams.entrySet())
         {
            if (!first)
               url.append('&');
            String name = entry.getKey();
            url.append(name);
            url.append('=');
            if (name.equals(IPSHtmlParameters.SYS_CONTENTLIST))
            {
               url.append(URLEncoder.encode(m_clist.getName(), PSCharSets
                     .rxJavaEnc()));
               setname = true;
            }
            else
               url.append(URLEncoder.encode(entry.getValue(), PSCharSets
                     .rxJavaEnc()));
            first = false;
         }
         if (!setname)
         {
            if (!first)
               url.append('&');
            url.append(IPSHtmlParameters.SYS_CONTENTLIST);
            url.append("=");
            url.append(URLEncoder.encode(m_clist.getName(), PSCharSets
                  .rxJavaEnc()));
         }
         m_clist.setUrl(url.toString());
      }
      updateGeneratorAndExpander();
      m_currentExpParams = null;
      m_currentGenParams = null;
      psvc.saveContentLists(Collections.singletonList(m_clist));
      setTitle(m_clist.getName());
      update();
      return cancel();
   }

   @Override
   public String perform()
   {
      m_resetExpParams = false;
      m_resetGenParams = false;

      m_expander = m_clist.getExpander();
      m_generator = m_clist.getGenerator();

      m_currentExpParams = new ArrayList<>();
      m_currentGenParams = new ArrayList<>();
      // Extract the current parameters
      if (StringUtils.isNotBlank(m_expander))
      {
         Map<String, String> eparams = m_clist.getExpanderParams();
         PSPair<String, String>[] declared = ms_pubui.getParameters(m_expander);
         for (PSPair<String, String> pdecl : declared)
         {
            String name = pdecl.getFirst();
            String desc = pdecl.getSecond();
            String value = eparams.get(name);
            m_currentExpParams.add(new PSParameter(name, desc, value));
         }
      }
      if (StringUtils.isNotBlank(m_generator))
      {
         Map<String, String> gparams = m_clist.getGeneratorParams();
         PSPair<String, String>[] declared = ms_pubui
               .getParameters(m_generator);
         for (PSPair<String, String> pdecl : declared)
         {
            String name = pdecl.getFirst();
            String desc = pdecl.getSecond();
            String value = gparams.get(name);
            m_currentGenParams.add(new PSParameter(name, desc, value));
         }
      }
      return super.perform();
   }

   /**
    * Copy generator and expander data back into the content list.
    */
   private void updateGeneratorAndExpander()
   {
      Set<String> empty = Collections.emptySet();
      Set<String> allexpkeys = 
         m_currentExpParams != null ? extractKeys(m_currentExpParams) : empty;
         
      Set<String> allgenkeys = 
         m_currentGenParams != null ? extractKeys(m_currentGenParams) : empty;

      // Generator parameters
      Map<String, String> params = m_clist.getGeneratorParams();
      for (String key : params.keySet())
      {
         if (!allgenkeys.contains(key))
         {
            m_clist.removeGeneratorParam(key);
         }
      }

      if (m_currentGenParams != null)
      {
         for (PSParameter i : m_currentGenParams)
         {
            m_clist.addGeneratorParam(i.getName(), i.getValue());
         }
      }

      // Expander parameters
      params = m_clist.getExpanderParams();
      for (String key : params.keySet())
      {
         if (!allexpkeys.contains(key))
         {
            m_clist.removeExpanderParam(key);
         }
      }

      if (m_currentExpParams != null)
      {
         for (PSParameter i : m_currentExpParams)
         {
            m_clist.addExpanderParam(i.getName(), i.getValue());
         }
      }

      m_clist.setExpander(m_expander);
      m_clist.setGenerator(m_generator);
   }

   /**
    * Extract the keys from a list of items.
    * 
    * @param currentParams the list to extract from, assumed not
    *            <code>null</code>.
    * @return the list of names, never <code>null</code>.
    */
   private Set<String> extractKeys(List<PSParameter> currentParams)
   {
      Set<String> rval = new HashSet<>();

      for (PSParameter i : currentParams)
      {
         rval.add(i.getName());
      }
      return rval;
   }

   /**
    * Overrides the base class behavior to remove the node from the parent if
    * the object has never been saved.
    * 
    * @return the outcome of the cancel action
    */
   @Override
   public String cancel()
   {
      String result = super.cancel();
      if (m_clist != null && ((PSContentList) m_clist).getVersion() == null)
      {
         // Remove node if content list is unsaved
         remove();
      }      
      return gotoParentNode();
   }

   /**
    * Accepts events from the generator and expander drop downs. Reset the shown
    * values for both in this case - this is cheap enough. Later we can make
    * this more specific if there's a reason.
    * 
    * @param event the event from JSF
    */
   @SuppressWarnings("unused")
   public void selectValueChanged(ValueChangeEvent event)
   {
      m_resetGenParams = true;
      m_resetExpParams = true;
   }

   @Override
   public String copy()
   {
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();

      IPSContentList original = getContentList();
      
      PSContentListViewNode targetParent = ((PSContentListViewNode) getContainer())
            .getUnusedContentListNode();
      String copyName = targetParent.getUniqueName(original.getName(), true);

      // Clone with new name, and add to the tree
      IPSContentList clistCopy = original.clone();
      clistCopy.setName(copyName);
      if (StringUtils.isBlank(clistCopy.getDescription()))
         clistCopy.setDescription("Copy of " + original.getName());
      String url = clistCopy.getUrl();
      url = PSUrlUtils.replaceUrlParameterValue(url,
            IPSHtmlParameters.SYS_CONTENTLIST, copyName);
      clistCopy.setUrl(url);
      try
      {
         pub.saveContentLists(Collections.singletonList(clistCopy));
      }
      catch (Exception e1)
      {
         ms_log.error("Failed to save Content List", e1);
         return null;
      }
      final PSContentListNode newcln = new PSContentListNode(clistCopy);
      newcln.update();
      return editNewNode(targetParent, newcln);
   }
   
   @Override
   public String getHelpTopic()
   {
      return "ContentlistEditor";
   }

}
