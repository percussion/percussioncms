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
package com.percussion.rx.publisher.jsf.data;

import com.percussion.rx.publisher.PSPublisherUtils;
import com.percussion.rx.publisher.jsf.nodes.PSDesignNode;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Presentation wrapper to aid in the displaying of an edition content list.
 * 
 * @author dougrand
 */
public class PSEditionContentListWrapper implements Comparable<PSEditionContentListWrapper>
{
   /**
    * Logger
    */
   public static final Logger ms_log = LogManager.getLogger(PSEditionContentListWrapper.class);
   
   /**
    * The edition content list
    */
   IPSEditionContentList m_eclist;
   
   /**
    * The name of the associated content list
    */
   private String m_contentlistname;

   /**
    * The assembly context name
    */
   private String m_assemblycontext;

   /**
    * The delivery context name
    */
   private String m_deliverycontext;

   /**
    * The sequence, used to order the content list associations
    */
   private Integer m_sequence;

   /**
    * Is this particular wrapper selected
    */
   private boolean m_selected;
   
   /**
    * The ID of the site that the Edition belongs to. Never <code>null</code>
    * after the constructor.
    */
   private IPSGuid m_siteId;
   
   /**
    * Constructor.
    * @param eclist the edition content list, never <code>null</code>.
    * @param siteId The ID of the site that the Edition belongs to, never
    *    <code>null</code>.
    */
   public PSEditionContentListWrapper(IPSEditionContentList eclist, 
         IPSGuid siteId)
   {
      if (eclist == null)
         throw new IllegalArgumentException("eclist may not be null");
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null");

      m_eclist = eclist;
      m_siteId = siteId;
      
      init();
   }

   /**
    * Initialize or reinitialize wrapper from contained content list. 
    */
   public void init()
   {
      IPSPublisherService psvc = PSPublisherServiceLocator.getPublisherService();
      IPSGuid clistid = m_eclist.getContentListId();
      try
      {
         List<IPSContentList> lists 
            = psvc.loadContentLists(Collections.singletonList(clistid));
         m_contentlistname = lists.get(0).getName();
      }
      catch (PSPublisherException e)
      {
         ms_log.error("Problem loading content list", e);
         m_contentlistname = "unknown";
      }
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      Map<Integer,String> cmap = smgr.getContextNameMap();
      m_assemblycontext = null;
      if (m_eclist.getAssemblyContextId() != null)
         m_assemblycontext = cmap.get(m_eclist.getAssemblyContextId().getUUID());
      m_deliverycontext = cmap.get(m_eclist.getDeliveryContextId().getUUID());
      m_sequence = m_eclist.getSequence();
      m_selected = false;
   }

   /**
    * @return the eclist
    */
   public IPSEditionContentList getEclist()
   {
      return m_eclist;
   }

   public String getClistURL()
   {
      IPSContentList clist = PSPublisherUtils.getContentList(m_eclist);
      String url = PSPublisherUtils.getCListDocumentURL(m_siteId, m_eclist, clist);
      return url;
   }
   
   /**
    * @param eclist the eclist to set
    */
   public void setEclist(IPSEditionContentList eclist)
   {
      m_eclist = eclist;
   }

   /**
    * @return the contentlistname
    */
   public String getContentlistname()
   {
      return m_contentlistname;
   }

   /**
    * A convenience method that calls
    * {@link PSDesignNode#getNameWithId(String, long)}.
    * 
    * @return Name formatted with the id, never <code>null</code> or empty.
    */
   public String getContentListNameWithId()
   {
      return PSDesignNode.getNameWithId(m_contentlistname, m_eclist
            .getContentListId().longValue());
   }

   /**
    * @param contentlistname the contentlistname to set
    */
   public void setContentlistname(String contentlistname)
   {
      m_contentlistname = contentlistname;
   }

   /**
    * @return the assemblycontext
    */
   public String getAssemblycontext()
   {
      return m_assemblycontext;
   }

   /**
    * @param assemblycontext the assemblycontext to set
    */
   public void setAssemblycontext(String assemblycontext)
   {
      m_assemblycontext = assemblycontext;
   }

   /**
    * @return the deliverycontext
    */
   public String getDeliverycontext()
   {
      return m_deliverycontext;
   }

   /**
    * @param deliverycontext the deliverycontext to set
    */
   public void setDeliverycontext(String deliverycontext)
   {
      m_deliverycontext = deliverycontext;
   }

   /**
    * @return the sequence
    */
   public Integer getSequence()
   {
      return m_sequence;
   }

   /**
    * @param sequence the sequence to set
    */
   public void setSequence(Integer sequence)
   {
      m_sequence = sequence;
   }

   /**
    * @return the selected
    */
   public boolean getSelected()
   {
      return m_selected;
   }

   /**
    * @param selected the selected to set
    */
   public void setSelected(boolean selected)
   {
      m_selected = selected;
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(PSEditionContentListWrapper o)
   {
      int mySeq = m_sequence == null ? 0 : m_sequence;
      int otherSeq = o.m_sequence == null ? 0 : o.m_sequence;
      return mySeq - otherSeq;
   }

   /**
    * @return the authtype, never <code>null</code>, may be empty.
    */
   public String getAuthtype()
   {
      if (m_eclist.getAuthtype() != null)
         return Integer.toString(m_eclist.getAuthtype());
      else
         return "";
   }
   
   /**
    * Set new authtype
    * @param authtype the new authtype value, never <code>null</code> or empty
    * and must be numeric.
    */
   public void setAuthtype(String authtype)
   {
      if (StringUtils.isBlank(authtype))
      {
         m_eclist.setAuthtype(null);
         return;
      }
      if (!StringUtils.isNumeric(authtype))
      {
         throw new IllegalArgumentException(
            "authtype must be numeric");
      }
      m_eclist.setAuthtype(new Integer(authtype));
   }
   
   
}
