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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSNavigation;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPubStatus.EndingState;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

import java.util.List;

/**
 * Represents a running edition. The running edition jsp uses AJAX to query the
 * runtime status of the edition. The shown log data is built with straight JSF,
 * so it is not dynamic.
 * 
 * @author dougrand
 * 
 */
public class PSRuntimeEditionNode extends PSLogNode
{
   /**
    * The edition represented by this runtime node.
    */
   private IPSEdition m_edition;
   
   /**
    * If this element is selected.
    */
   private boolean m_selected = false;

   /**
    * The business publishing service.
    */
   private IPSRxPublisherService m_rxpub = PSRxPublisherServiceLocator
         .getRxPublisherService();

   /**
    * Constructor.
    * @param e the edition, never <code>null</code> or empty.
    */
   public PSRuntimeEditionNode(IPSEdition e) {
      super(e.getDisplayTitle(), "pub-runtime-edition");
      m_edition = e;
      setOutcome("pub-runtime-edition");
   }

   /**
    * Determines if the site column need to be rendered or not.
    * @return <code>true</code> if the site column need to be rendered.
    */
   @Override
   public boolean isShowSiteColumn()
   {
      return false;
   }

   /**
    * Get the edition id 
    * 
    * @return the edition id
    */
   public long getEditionId()
   {
      return m_edition.getGUID().longValue();
   }
   
   /**
    * @return the job id for the running edition or <code>0</code> if no 
    * edition is running.
    */
   public long getJobId()
   {
      return m_rxpub.getEditionJobId(m_edition.getGUID());
   }

   /**
    * @return <code>true</code> if this element is selected.
    */
   @Override
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
    * @see com.percussion.rx.publisher.jsf.nodes.PSLogNode#getStatusLogs()
    */
   @Override
   public List<IPSPubStatus> getStatusLogs()
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();
      return 
         psvc.findPubStatusByEdition(m_edition.getGUID());
   }

   /**
    * Get the running job of this Edition.
    * @return the currently running job of this Edition. It may be 
    * <code>null</code> if there is no running job for this Edition.
    */
   private IPSPublisherJobStatus getJobStatus()
   {
      long jobid = m_rxpub.getEditionJobId(m_edition.getGUID());
      if (jobid == 0)
         return null;
      try
      {
         IPSPublisherJobStatus stat = m_rxpub.getPublishingJobStatus(jobid);
         return stat;
      }
      catch(Exception e)
      {
         return null;
      }
   }

   /**
    * @return <code>true</code> if this edition is currently running.
    */
   public boolean getRunning()
   {
      IPSPublisherJobStatus stat = getJobStatus();
      return stat != null && (! stat.getState().isTerminal());
   }

   /**
    * Get the state of the job of the Edition.
    * @return the state of the Edition job. It is blank if there is no running
    *    job for this Edition.
    */
   public String getStatus()
   {
      IPSPublisherJobStatus status = getJobStatus();
      if (status == null)
         return "";
      else
         return status.getState().getDisplayName();
   }
   
   /**
    * Gets the image URL of the current status.
    * 
    * @return the image URL. It may be <code>null</code> if there is no 
    *    running job with this edition. 
    */
   public String getStatusImage()
   {
      IPSPublisherJobStatus status = getJobStatus();
      if (status == null)
         return null;
      
      EndingState endState = PSPublishingStatusHelper.getEndingState(status
            .getState());
      String[] imgSrc = PSPublishingStatusHelper.getStatusImage(endState,
            true, true);
      return imgSrc[0];
   }

   @Override
   protected boolean useAnimatedIcon()
   {
      // use animated icon because this page automatically refreshes itself.
      return true;
   }

   /**
    * Get the percent of the progress for the Edition job.
    * @return the progress. It is 100 if there is no running job for this
    *    Edition.
    */
   public String getProgress()
   {
      IPSPublisherJobStatus status = getJobStatus();
      if (status == null)
         return "100";
      else
         return "" + PSPublishingStatusHelper.getJobCompletionPercent(status);
   }
   
   /**
    * @return get the edition, never <code>null</code>.
    */
   public IPSEdition getEdition()
   {
      return m_edition;
   }
   
   /**
    * Start the edition if it isn't running.
    * @return the outcome, <code>null</code>.
    */
   public String start()
   {
      if (! getRunning())
      {
         m_rxpub.startPublishingJob(m_edition.getGUID(), null);
      }
      
      return perform();
   }
   
   /**
    * Start the Edition from its parent node.
    * @return the outcome of the parent node. 
    */
   public String startFromParent()
   {
      start();

      /**
       * Note, assume this is called from the current parent node.
       * it is expected the navigation of the parent page defined
       * the <redirect> to itself. Cannot return null here; otherwise 
       * refresh browser may restart the last started Edition.
       */
      return getParent().perform();
   }
   
   /**
    * Stop the edition if it is running.
    * @return the outcome, <code>null</code>.
    */
   public String stop()
   {
      if (getRunning())
      {
         long jobid = m_rxpub.getEditionJobId(m_edition.getGUID());
         m_rxpub.cancelPublishingJob(jobid);
         try
         {
            /**
             * Give the job some time to change to cancel status
             */
            Thread.sleep(3000);
         }
         catch (InterruptedException ignore){
            Thread.currentThread().interrupt();
         }
      }
      
      return null;
   }
   
   @SuppressWarnings("cast")
   @Override
   public String perform()
   {
      PSNavigation navigator = (PSNavigation) getModel().getNavigator();
      navigator.setCurrentItemGuid(m_edition.getGUID());
      return super.perform();
   }
   
   @Override
   public String getNavLinkClass()
   {
      return "pubruntime-nav-edition-display";
   }
   
   /**
    * Get the name of the current site.
    * @return the site name, never <code>null</code> or empty.
    */
   public String getSiteName()
   {
      return getParent().getParent().getLabel();
   }
   
   @Override
   public String getHelpTopic()
   {
      return "RuntimeEdition";
   }
}
