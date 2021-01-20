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
package com.percussion.servlets;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.utils.jspel.PSItemUtilities;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.assembly.IPSAssemblyWs;
import com.percussion.webservices.assembly.PSAssemblyWsLocator;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;

/**
 * This servlet is now misnamed. A better name would be PSLinkDispatchServlet.
 * However, that would require creating an upgrade plugin to fixup web.xml.
 * <p>
 * Initially, dispatches to 1 or 2 pages, the action panel or active assembly,
 * based on a 'flag' in server.properties. In order to reach the AA page, a
 * site, folder and template must be obtained. If a site is not specified, a
 * chooser page is returned listing all instances of the item in a folder. A
 * hard-coded algorithm attempts to determine a template to use for AA.
 * 
 * @author dougrand
 */
@SuppressWarnings("serial")
public class PSActionPanelServlet extends HttpServlet
{
   private static final String RXAP = "RxAP_";

   /**
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void service(HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException
   {

      // If folder and site are defined, we can just display. Otherwise we
      // need to present a chooser
      HttpSession session = request.getSession(true);
      String contentid = request.getParameter("sys_contentid");
      String folderid = request.getParameter("sys_folderid");
      String siteid = request.getParameter("sys_siteid");
      
      TargetType targetType = getDispatchTargetType();
      String url = getTargetUrl(targetType, contentid);
      Integer cid = new Integer(contentid);
      PSItemUtilities iutils = new PSItemUtilities();
      MultiMap siteinfo = iutils.getItemSiteInfo(cid);
      boolean showpanel = false;
      Integer fid = StringUtils.isNotBlank(folderid)
            ? new Integer(folderid)
            : null;
      Long sid = StringUtils.isNotBlank(siteid) ? new Long(siteid) : null;

      if (fid != null)
      {
         session.setAttribute(RXAP + contentid + "_folderid", fid);
      }
      if (sid != null)
      {
         session.setAttribute(RXAP + contentid + "_siteid", sid);
      }

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = cms.loadComponentSummary(cid);

      // Check access
      boolean accessible = false;
      IPSBackEndRoleMgr rmgr = PSRoleMgrLocator.getBackEndRoleManager();
      try
      {
         PSCommunity com = rmgr.loadCommunity(new PSGuid(PSTypeEnum.COMMUNITY_DEF,
               sum.getCommunityId()));
         for(IPSGuid roleid : com.getRoleAssociations())
         {
            PSBackEndRole roles[] = rmgr.loadRoles(new IPSGuid[] { roleid });
            String rolename = roles[0].getNormalizedName();
            if (request.isUserInRole(rolename))
            {
               accessible = true;
               break;
            }
         }
      }
      catch (PSSecurityException e)
      {
         // If we can't load the community, then what? Just assume no access
      }

      if (!accessible)
      {
         request.setAttribute("title", sum.getName());
         RequestDispatcher disp = request
            .getRequestDispatcher("/ui/actionpage/noaccess.jsp");
         disp.forward(request, response);
         return;
      }
      
      String title = sum.getName();

      if (siteinfo.size() == 0)
      {
         // No folders at all, just do action panel
         showpanel = true;
      }
      else if (fid != null && sid != null)
      {
         // We have the info
         showpanel = true;
      }
      else
      {
         if (siteinfo.size() == 1)
         {
            String sitename = (String) siteinfo.keySet().iterator().next();
            List<String> folders = (List<String>) siteinfo.get(sitename);
            if (folders.size() == 1)
            {
               sid = iutils.getSiteIdFromName(sitename);
               fid = iutils.getFolderIdFromPath(folders.get(0));
               showpanel = true;
            }
            else if (fid != null)
            {
               // One site (or no site) but the folder is specified
               showpanel = true;
            }
         }
         else
         {
            fid = (Integer) session
                  .getAttribute(RXAP + contentid + "_folderid");
            sid = (Long) session.getAttribute(RXAP + contentid + "_siteid");

            showpanel = fid != null;
         }
      }

      if (fid != null)
      {
         url += "&sys_folderid=" + fid.toString();
      }
      if (sid != null)
      {
         url += "&sys_siteid=" + sid.toString();
      }

      request.setAttribute("title", title);
      StringBuffer requrl = request.getRequestURL();
      requrl.append("?");
      requrl.append(request.getQueryString());
      String redirect = URLEncoder.encode(requrl.toString(), "UTF-8");
      request.setAttribute("sys_redirecturl", redirect);

      if (showpanel)
      {
         if (targetType == TargetType.ACTIVE_ASSEMBLY 
               && sid != null && fid != null)
         {
            IPSGuid tguid = calculateDefaultTemplate(sum.getContentTypeId(),sid);
            if (tguid != null)
            {
               url += "&sys_variantid=" + tguid.getUUID();
               response.sendRedirect(response.encodeRedirectURL(url));
               return;
            }
            //fall thru to default page if template can't be found
         }

         request.setAttribute("url", url);
         RequestDispatcher disp = request
               .getRequestDispatcher("/ui/actionpage/panel.jsp");
         disp.forward(request, response);
      }
      else
      {
         request.setAttribute("siteinfo", siteinfo);
         RequestDispatcher disp = request
               .getRequestDispatcher("/ui/actionpage/choose.jsp");
         disp.forward(request, response);
      }
   }

   /**
    * Filters the templates associated with the site and then looks for a
    * template whose <code>PublishWhen</code> is <code>Default</code>. If
    * there isn't one, take the first <code>Always</code> template in
    * ascending alpha order by name. If there isn't one, take the first
    * <code>Page</code> in ascending alpha order by name. If there isn't one,
    * take the first <code>Snippet</code> in ascending alpha order by name.
    * 
    * @param ctypeId Assumed to be a valid content type UUID.
    * @param siteId Assumed to be a valid site id.
    * 
    * @return The id of the template to use, or <code>null</code> if a
    * template cannot be found.
    */
   private IPSGuid calculateDefaultTemplate(long ctypeId, long siteId)
   {
      IPSAssemblyWs mgr = PSAssemblyWsLocator.getAssemblyWebservice();
      IPSGuid tguid = null;
      try
      {
         String ctypeName = PSItemDefManager.getInstance()
               .contentTypeIdToName(ctypeId);
         List<PSAssemblyTemplateWs> templates = 
            mgr.loadAssemblyTemplates(null, ctypeName);
         List<PSAssemblyTemplateWs> siteTemplates = 
            new ArrayList<PSAssemblyTemplateWs>();
         IPSGuid siteGuid = PSGuidManagerLocator.getGuidMgr()
         .makeGuid(siteId, PSTypeEnum.SITE);
         //Filter the templates by site first
         for (PSAssemblyTemplateWs t : templates)
         {
            if(t.getSites().containsKey(siteGuid))
            {
               siteTemplates.add(t);
            }
         }
         if(siteTemplates.isEmpty())
         {
            return null;
         }
         for (PSAssemblyTemplateWs t : siteTemplates)
         {
            if (t.getTemplate().getPublishWhen() 
                  == IPSAssemblyTemplate.PublishWhen.Default)
            {
               tguid = t.getTemplate().getGUID();
               break;
            }
         }
         if (tguid == null)
         {
            tguid = findTemplate(siteTemplates, new Filter()
            {
               @Override
               public boolean accept(IPSAssemblyTemplate t)
               {
                  return t.getPublishWhen() 
                     == IPSAssemblyTemplate.PublishWhen.Always;
               }
            });
         }
         if (tguid == null)
         {
            tguid = findTemplate(siteTemplates, new Filter()
            {
               @Override
               public boolean accept(IPSAssemblyTemplate t)
               {
                  return t.getOutputFormat() 
                     == IPSAssemblyTemplate.OutputFormat.Page;
               }
            });
         }
         if (tguid == null)
         {
            tguid = findTemplate(siteTemplates, new Filter()
            {
               @Override
               public boolean accept(IPSAssemblyTemplate t)
               {
                  return t.getOutputFormat() 
                     == IPSAssemblyTemplate.OutputFormat.Snippet;
               }
            });
         }
      }
      catch (PSInvalidContentTypeException e)
      {
         //ignore
      }
      return tguid;
   }
   
   /**
    * For each template, calls the {@link Filter#accept(IPSAssemblyTemplate) accept} 
    * method and for all those that match, put them in a list. When done, sort
    * the list and return the first entry in the list.
    * 
    * @param templates Assumed not <code>null</code>.
    * 
    * @param filter Assumed not <code>null</code>.
    * 
    * @return The guid from one of the supplied templates, or <code>null</code>
    * if none of them match the supplied filter.
    */
   private IPSGuid findTemplate(List<PSAssemblyTemplateWs> templates, 
         Filter filter)
   {
      List<IPSAssemblyTemplate> possibilities = 
         new ArrayList<IPSAssemblyTemplate>();
      for (PSAssemblyTemplateWs t : templates)
      {
         if (filter.accept(t.getTemplate()))
         {
            possibilities.add(t.getTemplate());
         }
      }
      sort(possibilities);
      IPSGuid tguid = null;
      if (possibilities.size() > 0)
      {
         tguid = possibilities.get(0).getGUID();
      }
      return tguid;
   }
   
   private abstract class Filter
   {
      public abstract boolean accept(IPSAssemblyTemplate t);
   }
   
   /**
    * Order by name, ascending alpha, case-insensitive.
    * 
    * @param templates Assumed not <code>null</code>. May be empty.
    */
   private void sort(List<IPSAssemblyTemplate> templates)
   {
      Collections.sort(templates, new Comparator<IPSAssemblyTemplate>()
      {
         public int compare(IPSAssemblyTemplate t1, IPSAssemblyTemplate t2)
         {
            return t1.getName().compareToIgnoreCase(t2.getName());
         }
      });
   }

   /**
    * Builds the target URL based on the supplied type. This is determined by
    * reading a server property.
    * 
    * @param targetType Assumed not <code>null</code>.
    * 
    * @param contentid Used as the value of the sys_contentid param in the
    * generated url. Assumed not <code>null</code>.
    * 
    * @return A fully qualified string of the form
    * <pre>
    *   /Rhythmyx/path?[optional params&amp;]sys_contentid=<code>contentid</code>
    * </pre>
    */
   private String getTargetUrl(TargetType targetType, String contentid)
   {
      StringBuffer url = new StringBuffer();
      url.append("/Rhythmyx/");
      if (targetType == TargetType.ACTIVE_ASSEMBLY)
      {
         url.append(
            "assembler/render?sys_authtype=0&sys_command=editrc&sys_context=0");
      }
      else
         url.append("sys_ActionPage/Entries.html");

      if (!url.toString().contains("?"))
         url.append("?");
      else
      {
         if (url.charAt(url.length()-1) != '&')
            url.append('&');
      }
      url.append("sys_contentid=");
      url.append(contentid);
      return url.toString();
   }
   
   /**
    * Looks up a certain property in server.properties and converts it to the
    * proper enum. If not found, or the value is not recognized, the default
    * type is returned as specified by {@link TargetType#getDefault()}.
    * 
    * @return Never <code>null</code>.
    */
   private TargetType getDispatchTargetType()
   {
      Properties props = PSServer.getServerProps();
      String tmp = props.getProperty("notificationUrlTarget");
      TargetType result = TargetType.findMatch(tmp);
      if (result == null)
         result = TargetType.getDefault();
      return result;
   }

   /**
    * The types of pages that this dispatcher can process. It is expected that
    * the server properties file will contain a constant known to the containing
    * class with one of the text values below.
    *
    * @author paulhoward
    */
   private enum TargetType
   {
      /**
       * Launch what is known as the Action Panel - a page that has the menu
       * actions available for a specified item.
       */
      ACTION_PANEL("actionPanel"),

      /**
       * If a default template can be found, redirect to the AA page for that
       * template.
       */
      ACTIVE_ASSEMBLY("activeAssembly");
      
      /**
       * The text that represents this enum.
       * 
       * @param key Never blank.
       */
      private TargetType(String key)
      {
         if (StringUtils.isBlank(key))
         {
            throw new IllegalArgumentException("key cannot be blank");  
         }
         mi_key = key.toLowerCase();
      }
      
      /**
       * @return The text that represents the enum. Never blank.
       */
      public String getKey()
      {
         return mi_key;
      }

      /**
       * Compare the supplied string against the keys of all known
       * <code>TargetType</code>s.
       * 
       * @param key May be <code>null</code> or empty. Case-insensitive.
       * 
       * @return <code>null</code> if <code>key</code> is blank or doesn't
       * match any of the keys of the enums.
       */
      public static TargetType findMatch(String key)
      {
         TargetType result = null;
         if (StringUtils.isNotBlank(key))
         {
            key = key.toLowerCase();
            for (TargetType t : TargetType.values())
            {
               if (t.getKey().equals(key))
               {
                  result = t;
                  break;
               }
            }
         }
         return result;
      }
      
      /**
       * @return One of the values in this enum.
       */
      public static TargetType getDefault()
      {
         return ACTION_PANEL;
      }
      
      /**
       * Set in ctor, then never blank or modified.
       */
      private String mi_key;
   }
}
