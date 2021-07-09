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
package com.percussion.rx.publisher;

import com.percussion.utils.guid.IPSGuid;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * The public (read-only) interface to the job status. A job can be in a number
 * of states, and the job keeps track of items queued for assembly, items
 * assembled, items awaiting delivery and items delivered. The caller must 
 * request the status each time to be assured of an update.
 * 
 * @author dougrand
 */
public interface IPSPublisherJobStatus
{
   /**
    * The state of the job. Each state may be either a terminal or non-terminal
    * state. Terminal states "latch", which means that once a job enters a 
    * terminal state, no further changes will be recorded.
    * <p>
    * N.B. It may be necessary to implement something more than 
    * {@link #isTerminal()} to ensure that the job status proceeds in an 
    * orderly fashion. Right now it seems sufficient.
    */
   enum State {
      /**
       * The job has been spawned, but has not started to run yet. Pre edition
       * tasks are run in the initial state.
       */
      INITIAL(0, false, "Initializing"),
      
      /**
       * The job has not been run and did not pass the pubserver connection check.
       */
      BADCONFIG(0,true,"Could not connect to publishing server, please check publishing server configuration."),

      /**
       * The pubserver DB Connection is New, needs jetty restart.
       */
      PUBSERVERNEWDBCONFIG(0,true,"Please restart jetty service for initializing new datasource created."),
      
      /**
       * The job has not been run and some of the publishing servers from the publish
       * request (most likely a publish now on a resource) could not be connected to.
       */
      BADCONFIGMULTIPLESITES(0,true,"Could not connect to publishing server for sites: "),
      
      /**
       * The job is running any registered tasks to be run before the edition.
       */
      PRETASKS(0, false, "Running pre edition tasks"),
      
      /**
       * The job is running, queuing information from the content lists. The job
       * continues to queue as long as there are more content lists to execute.
       */
      QUEUEING(20, false, "Queuing content"),
      
      /**
       * The job is running, queuing is complete, and the system is assembling
       * items, delivering assembled items, etc. This is the longest running
       * state for a normal job.
       */
      WORKING(90, false, null),
      
      /**
       * The job is committing any transactional work. The job remains in this
       * state until the commit has been acknowledged.
       */
      COMMITTING(100, false, null),
      
      /**
       * The job is running any registered tasks to be run after the edition.
       */
      POSTTASKS(100, false, "Running post edition tasks"),
      
      /**
       * The job has been cancelled by user. All queued assembly items and items 
       * for delivery will be tossed on receipt.
       */
      CANCELLED(100, true, "Cancelled by user"),

      /**
       * The job has been terminated abnormally. All queued assembly items and 
       * items for delivery will be tossed on receipt.
       */
      ABORTED(100, true, "Terminated abnormally"),

      /**
       * The job has been terminated abnormally. All queued assembly items and
       * items for delivery will be tossed on receipt.
       */
      RESTARTNEEDED(100, true, "Please restart jetty for new database initialization"),

      /**
       * The job has not been executed. Publication was stopped because of
       * licensing problems.
       */
      FORBIDDEN(0, true, "Publication stopped because of licensing issues"),
      
      /**
       * The job has completed, but with failed items, edition tasks, 
       * Content List, commit or un-publishing.
       */
      COMPLETED_W_FAILURE(100, true, "Edition completed with failures"),
      
      /**
       * The job has completed, there will be no items waiting for assembly or
       * delivery. There is no failed item and no failed edition task.
       */
      COMPLETED(100, true, "Edition completed"),
      
      /**
       * Any job that is not running is inactive.
       */
      INACTIVE(0, true, "Not running"),
      
      /**
       * The publish server cannot be used for the specified edition type.
       */
      INVALID(0, true, "The specified publishing server is not valid for the specified edition."),
      
      /**
       * No staging server available to publish or unpublish the selected item.
       */
      NOSTAGING_SERVERS(0, true, "No staging servers available for the item to publish/unpublish.");
      
      
      
      /**
       * Hold the maximum percentage to show.
       */
      private int m_maxpercent;
      
      /**
       * Is this state terminal?
       */
      private boolean m_terminal;
      
      /**
       * The display name, may be <code>null</code> if not set in the ctor.
       */
      private String m_displayName;
      
      /**
       * Ctor.
       * @param maxpercent maximum completion percentage to show in the UI.
       * @param terminal is this state a terminal state?
       * @param displayname is the string to display for this state, may be 
       * <code>null</code>.
       */
      State(int maxpercent, boolean terminal, String displayname)
      {
         m_maxpercent = maxpercent;
         m_terminal = terminal;
         m_displayName = displayname;
      }

      /**
       * @return the maximum percentage to show in the user interface. This is 
       * provided since until the queuing is completed, we don't know what
       * the total count is. Without the maximum percentage value, the UI can
       * show absurd things like the job being at 90% followed by the job being
       * at 50% and then 20% as the queued count increases.
       */
      public int getMaxpercent()
      {
         return m_maxpercent;
      }

      /**
       * @return <code>true</code> if this state is a terminal state. Terminal
       * states imply to the caller that no further updates should occur to the 
       * state. This is needed to prevent any out of order messages from 
       * ruining the job state. For example, the cancellation message sets the
       * state to {@link State#CANCELLED}, but an in process message may then
       * cause the state to change to {@link State#WORKING}. By using the 
       * terminal value, we can latch the terminal state and ignore the 
       * spurious update.
       */
      public boolean isTerminal()
      {
         return m_terminal;
      }
      
      /**
       * Get a user friendly display string to use for the state. By default
       * this is derived from the state name, but can be overriden by supplying
       * a display name to the ctor.
       * 
       * @return the display name, never <code>null</code> or empty.
       */
      public String getDisplayName()
      {
         if (StringUtils.isNotBlank(m_displayName))
            return m_displayName;
         else
            return StringUtils.capitalize(name().toLowerCase());
      }
   }

   /**
    * The state of a given item. The job holds information for every outstanding
    * item being assembled and delivered. The state transitions are simpler than
    * for the job. As for the job states, track terminal states.
    */
   enum ItemState {
      /**
       * The item has been queued for assembly.
       */
      QUEUED(false, false),
      /**
       * The item has been assembled with a successful result.
       */
      ASSEMBLED(false, false),
      /**
       * The item has been assembled with a failure result.
       */
      FAILED(true, true),
      /**
       * The item has been assembled and was cached for the delivery.
       * Used only for transactional delivery.
       */
      PREPARED_FOR_DELIVERY(false, true),
      /**
       * The item has been delivered to the delivery server.
       */
      DELIVERED(true, true),
      /**
       * The item has been delivered to the delivery server.
       */
      CANCELLED(true, true),
      /**
       * The item has been turned into one or more child pages.
       */
      PAGED(false, false),
      
      /**
       * The item has been sent to the delivery handler which 
       * will respond asynchronously when is is complete
       */
      DELIVERY_QUEUED(false,false);
      
      /**
       * If <code>true</code> this is a terminal state.
       */
      private final boolean m_terminal;
      
      /**
       * @see #isPersistable()
       */
      private boolean m_persistable;

      /**
       * Private ctor.
       * @param terminal this state is a terminal state.
       */
      ItemState(boolean terminal, boolean persistable)
      {
         m_terminal = terminal;
         m_persistable = persistable;
      }
      
      /**
       * Is this a terminal state?
       * @return <code>true</code> if the state is terminal.
       */
      public boolean isTerminal()
      {
         return m_terminal;
      }
      
      /**
       * When <code>true</code> the item status needs to be persisted
       * to the database.
       * @return <code>true</code> if the item status needs to be persisted.
       */
      public boolean isPersistable()
      {
         return m_persistable;
      }
   }

   /**
    * Get the current state of the job.
    * 
    * @return the current state, never <code>null</code>, see the description
    *         of the enum {@link State} for details of the returned values.
    */
   State getState();

   /**
    * The total number of items.
    * @return the total number of items in any states.
    */
   int countTotalItems();

   /**
    * As items are transferred from the content list into the assembly queue,
    * this number will increase.
    * 
    * @return the number of items that have been queued by the job for assembly.
    * This count will reduce as items are assembled and delivered and will be 
    * zero when the job is ready for commit.
    */
   int countItemsQueuedForAssembly();

   /**
    * As items are assembled successfully, this number will increase.
    * 
    * @return the number of items that have been assembled and are waiting for
    * delivery. This count will reduce as items are delivered and when there
    * are no items waiting for assembly. It will be zero when the job is 
    * complete.
    */
   int countAssembledItems();

   /**
    * As items fail assembly or delivery, this number increases.
    * 
    * @return the number of items that have failed as of the time when this
    * object was created. An item may be failed based on errors in either
    * assembly or delivery.
    */
   int countFailedItems();


   /**
    * For transactional delivery, as items are submitted for delivery,
    * this number increases. It decreases as the items are actually delivered.
    * 
    * @return the number of items that has been submitted for transactional
    * delivery. These items will be delivered during a job commit and 
    * may still fail on that stage.
    * For non-transactional delivery always returns 0.
    */
   int countItemsPreparedForDelivery();   

   /**
    * As items are delivered to the publishing server, this number increases.
    * 
    * @return the number of items that have been delivered to the delivery 
    * handler specified by the delivery location.
    */
   int countItemsDelivered();
   
   /**
    * @return when this job was started, never <code>null</code>.
    */
   Date getStartTime();
   
   /**
    * @return how much time has gone by where the edition is doing useful 
    * work, this time will cease increasing when the edition is finished.
    */
   long getElapsed();

   /**
    * @return the edition id for the given job
    */
   IPSGuid getEditionId();
   
   /**
    * @return the job id
    */
   long getJobId();
}
