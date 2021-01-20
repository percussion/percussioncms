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
package com.percussion.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class contains the workflow information retrieved from the
 * <code>sys_psxWorkflowCataloger/workflowinfo</code> resource.
 * It is used query states and transitions of a specified workflow.
 */
public class PSWorkflowInfo
{
   /**
    * Constructs the object from its XML representation. Its DTD is in the
    * following format:
    * <pre>
    * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
    * &lt;!ELEMENT transitioninfo (#PCDATA)&gt;
    * &lt;!ATTLIST   transitioninfo
    *             transitionid CDATA #IMPLIED
    *             default CDATA #IMPLIED
    *             workflowid CDATA #IMPLIED
    *             transitionToState CDATA #IMPLIED
    *             transitionFromState CDATA #IMPLIED
    * &gt;
    * &lt;!ELEMENT Transitions (transitioninfo*)&gt;
    * &lt;!ELEMENT stateinfo (#PCDATA)&gt;
    * &lt;!ATTLIST stateinfo
    * stateid CDATA #IMPLIED
    * workflowid CDATA #IMPLIED
    * sortorder CDATA #IMPLIED
    * contentvalid CDATA #IMPLIED
    * &gt;
    * &lt;!ELEMENT States (stateinfo*)&gt;
    * &lt;!ELEMENT StatesTrans (States, Transitions)&gt;
    * </pre>
    *
    * @param doc the document with format described above. It is typically
    *    created by <code>sys_psxWorkflowCataloger/workflowinfo</code>.
    *    Never <code>null</code>.
    */
   public PSWorkflowInfo(Document doc)
   {
      fromXML(doc.getDocumentElement());
   }

   /**
    * Determines if the supplied workflow id and state id matches one of the
    * given valid token.
    *
    * @param workflowId the workflow id.
    * @param stateId the state id in the above workflow.
    * @param validTokens a list of comma delimited valid token.
    *    Never <code>null</code> or empty.
    *
    * @return <code>true</code> if the state matches the valid token;
    *    <code>false</code> otherwise.
    */
   public boolean isValidState(int workflowId, int stateId, String validTokens)
   {
      if (validTokens == null)
         throw new IllegalArgumentException("validTokens may not be null");

      return isValidState(String.valueOf(workflowId),
            String.valueOf(stateId), validTokens);
   }

   /**
    * Returns a transition id in the supplied workflow id. The from-state of
    * the transition matches the supplied state id. The valid-token of the
    * to-state of the transition matches one of the supplied valid-tokens.
    * If there are more than one matched transitions, then the to-state
    * of the returned transition has the least Sort Order value. This state is 
    * the leftmost of the to-states in the preview window of the 
    * workflow. If the desired transition does not exist and the 
    * <code>useDefaults</code> is <code>true</code>, then return one of the 
    * default transitions whose from-state is the supplied state id. Again, if 
    * there are more than one default transitions, the to-state of the returned
    * default transition has the least Sort Order value.
    *
    * @param workflowId the workflow id.
    * @param fromStateId the from state id of the returned trainsition.
    * @param toValidTokens a list of comma delimited valid tokens. The valid
    *    token of the to-state of the returned transition.
    *    Never <code>null</code> or empty.
    * @param useDefaults <code>true</code> if it returns a default transition
    *    when the desired transition not exist.
    *
    * @return the transition described above. Returns <code>null</code> if
    *    there is no such transition exist in the specified workflow.
    */
   public String getTransitionId(int workflowId, int fromStateId,
         String toValidTokens, boolean useDefaults)
   {
      List transitions = getAutoTransitionId(workflowId, fromStateId,
            toValidTokens, useDefaults);

      if (transitions.isEmpty())
      {
         return null;
      }
      else
      {
         SortOrderTransition trans = (SortOrderTransition) findLeast(transitions);
         return trans.getTransitionId();
      }
   }

   /**
    * Returns all states for the supplied workflow id.
    *
    * @param workflowID  workflow id, assume not <code>null</code>
    *
    * @return List of {@link StateInfo} objects in the supplied workflow
    *     may be <code>null</code> or empty if the workflow does not exist.
    */
   private List getStates(String workflowID)
   {
      return (List) m_states.get(workflowID);
   }

   /**
    * Returns all transitions for the supplied workflow id.
    *
    * @param workflowID workflow id, assume not <code>null</code>.
    *
    * @return List of PSTransitionInfo objects in the supplied workflow.
    *     may be <code>null</code> or empty if the workflow does not exist.
    */
   private List getTransitions(String workflowID)
   {
      return (List) m_transitions.get(workflowID);
   }

   /**
    * Checks whether the supplied workflow/stateid has matches one of the
    * supplied char values in the token list.
    *
    * @param workflow
    *           String value workflowid, assume not <code>null</code>.
    * @param stateId
    *           String value stateid, assume not <code>null</code>.
    * @param tokens
    *           comma separated list of char values representing ContentValid
    *           assume not <code>null</code>.
    * @return true if the item's content state char representation is in the
    *         supplied token list. false otherwise.
    */
   private boolean isValidState(String workflow, String stateId, String tokens)
   {
      List stateList = (List) m_states.get(workflow);
      Iterator i = stateList.iterator();
      while (i.hasNext())
      {
         StateInfo info = (StateInfo) i.next();
         if (info.getID().equalsIgnoreCase(stateId))
            return checkValidFlag(info.getValid(), tokens);
      }
      return false;
   }

   /**
    * Performs the char compare
    *
    * @param publishable
    *           char value from ContetValid in states table.
    * @param validTokens
    *           comma separated list of char values representing ContentValid,
    *           assume not <code>null</code>.
    *
    * @return <code>true</code> if publishable is in the validTokens.
    *         <code>false</code> otherwise.
    */
   private boolean checkValidFlag(char publishable, String validTokens)
   {
      StringTokenizer tokens = new StringTokenizer(validTokens, ",");
      while (tokens.hasMoreTokens())
      {
         String token = tokens.nextToken().trim();
         if (publishable == token.charAt(0))
            return true;
      }
      return false;
   }

   /**
    * Find all of the transitions out of the current state in the current
    * workflow
    *
    * @param stateId String value if state id, assume not <code>null</code>.
    * @param workflowId String value workflow id, assume not <code>null</code>
    *
    * @return <code>PSStateInfo</code> object with the supplied state id.
    *          It may be <code>null</code> if cannot find the specified state.
    */
   private StateInfo getState(String stateId, String workflowId)
   {
      List states = getStates(workflowId);
      Iterator it = states.iterator();
      while (it.hasNext())
      {
         StateInfo state = (StateInfo) it.next();
         if (state.getID().equalsIgnoreCase(stateId))
            return state;
      }
      return null;
   }

   /**
    * Finds the PSAutoTransition object from within the list with the lowest
    * sortOrder.
    *
    * @param list
    *           PSAutoTransition objects. Assume not empty.
    *
    * @return PSAutoTransition object with the lowest sortOrder form the
    *         supplied list of PSAutoTransition objects.
    *         may not be <code>null</code>
    */
   private SortOrderTransition findLeast(List list)
   {
      if (list == null)
         throw new IllegalArgumentException(
               "List of autoTransitions may not be null");
      Iterator it = list.iterator();
      SortOrderTransition least = (SortOrderTransition) it.next();
      while (it.hasNext())
      {
         SortOrderTransition trans = (SortOrderTransition) it.next();
         if (trans.getSortOrder() < least.getSortOrder())
            least = trans;
      }
      return least;
   }

   /**
    * Builds the state and transtion information used for ContentValid checks,
    * autoTransition information.
    *
    * @param el the XML representation of the object, never <code>null</code>.
    *    see {@link #PSWorkflowInfo(Document)} for its DTD.
    */
   private void fromXML(Element el)
   {
      List allStates = new ArrayList();
      List allTrans = new ArrayList();

      NodeList snodes = el
            .getElementsByTagName(ELEM_STATEINFO);
      NodeList tnodes = el
            .getElementsByTagName(ELEM_TRANSITIONINFO);

      if (snodes != null && snodes.getLength() > 0)
      {
         for (int i = 0; i < snodes.getLength(); i++)
         {
            Element element = (Element) snodes.item(i);
            String id = element.getAttribute(ATTR_STATEID);
            String sortOrder = element.getAttribute(ATTR_SORT_ORDER);
            String workflow = element.getAttribute(ATTR_WORKFLOWID);
            String cv = element
                  .getAttribute(ATTR_VALID_FLAG);
            if ( (cv == null) || (cv.trim().length() == 0)
                  || ((id == null) || id.trim().length() == 0)
                  || ((sortOrder == null) || sortOrder.trim().length() == 0)
                  || ((workflow == null) || workflow.trim().length() == 0))

               m_logger
                     .error("Cannot create PSTStateInfo recieved empty or null data");
            else
            {
               StateInfo s = new StateInfo(cv.charAt(0), id, sortOrder,
                     workflow);
               allStates.add(s);
            }
         }
      }

      if (tnodes != null && tnodes.getLength() > 0)
      {
         for (int i = 0; i < tnodes.getLength(); i++)
         {
            Element element = (Element) tnodes.item(i);
            String id = element.getAttribute(ATTR_TID);
            String to = element.getAttribute(ATTR_TRANSTO);
            String from = element.getAttribute(ATTR_TRANSFROM);
            String workflow = element.getAttribute(ATTR_WORKFLOWID);
            String flag = element.getAttribute(ATTR_DEFAULT_TRANS);
            if (flag == null || flag.trim().length() == 0)
               flag = "n";
            if (((to == null) || to.trim().length() == 0)
                  || ((id == null) || id.trim().length() == 0)
                  || ((from == null) || from.trim().length() == 0)
                  || ((workflow == null) || workflow.trim().length() == 0))

               m_logger
                     .error("Cannot create PSTransitionInfo recieved empty or null data");
            else
            {
               TransitionInfo t = new TransitionInfo(flag.charAt(0), id,
                     to, from, workflow);
               allTrans.add(t);
            }
         }
      }
      BuildCache(allStates, m_states);
      BuildCache(allTrans, m_transitions);
   }


   /**
    * Builds cache: Map of state info keyed by workflow
    *
    * @param list
    *           of PSWorkflowInfo assume not <code>null</code>
    * @param map
    *           HashMap to fill - either state or transition not
    *           <code>null</code>
    */
   private void BuildCache(List list, HashMap map)
   {
      Iterator it = list.iterator();
      while (it.hasNext())
      {
         IPSWorkflowId info = (IPSWorkflowId) it.next();
         if (map.containsKey(info.getWorkflow()))
         {
            List l = (List) map.get(info.getWorkflow());
            l.add(info);
            map.put(info.getWorkflow(), l);
         }
         else
         {
            List l = new ArrayList();
            l.add(info);
            map.put(info.getWorkflow(), l);
         }
      }
   }

   /**
    * Finds the list of Transition from the current state into a state
    * represented in tokens. If no such state transition is found, the lowest
    * sortOrder default transition is returned.
    *
    * @param workflowId the workflow id of the returned transitions.
    *           assume not <code>null</code>.
    * @param stateId the state id of the returned transitions.
    *           assume not <code>null</code>.
    * @param tokens
    *           String List of char tokens for the state not be
    *           <code>null</code>.
    * @param bUseDefaults
    *           if <code>true</code> will return the List of default
    *           transitions when the desired transition list is empty.
    *
    * @return List PSAutoTransition of the transitions matching the token
    *         criteria may be emtpy but never <code>null</code>
    */
   private List getAutoTransitionId(int workflowId, int stateId,
         String tokens, boolean bUseDefaults)
   {
      String workflow = Integer.toString(workflowId);
      List transitions = getTransitions(workflow);
      List states = getStates(workflow);

      Iterator it = transitions.iterator();
      List entries = new ArrayList();
      List defaults = new ArrayList();
      while (it.hasNext())
      {
         TransitionInfo ti = (TransitionInfo) it.next();
         String stateID = Integer.toString(stateId);
         if (ti.getTransitionFrom().equalsIgnoreCase(stateID))
         {
            StateInfo state = getState(ti.getTransitionTo(), workflow);
            if (isValidState(workflow, state.getID(), tokens))
            {
               SortOrderTransition entry = new SortOrderTransition(state, ti);
               entries.add(entry);
            }
            else
            {
               if (ti.isDefault())
                  defaults.add(new SortOrderTransition(state, ti));
            }
         }
      }

      if (!entries.isEmpty())
         return entries;
      if (bUseDefaults)
         return defaults;
      else
         return new ArrayList();
   }

   /**
    * This object contains a transition and its sort order.
    */
   private class SortOrderTransition
   {
      /**
       * sortOrder for this state
       */
      private int m_sortOrder = 10;

      /**
       * ContentValid column for this state
       */
      private char m_validFlag = 'n';

      /**
       * transitionId column.
       */
      private String m_transitionID = "0";

      /**
       * set to true if this is a default transition
       */
      private boolean m_isDefault = false;

      /**
       * This method called from PSLockMethod/PSUnLockMethod
       *
       * @param state the state info, assume not <code>null</code>. The sort
       *    order of it may not be <code>null</code>.
       *           sets the values of sortOrder and validFlag may not be <code>null</code>
       * @param transition the transition object, assume not <code>null</code>.
       */
      private SortOrderTransition(StateInfo state, TransitionInfo transition)
      {
         if (state.getSortOrder() == null)
            throw new IllegalArgumentException("The sort order of state id \""
                  + state.m_stateId + "\" cannot be null");

         m_isDefault = transition.getDefaultFlag() == 'y' ? true : false;
         m_sortOrder = Integer.parseInt(state.getSortOrder());
         m_transitionID = transition.getTransitionId();
         m_validFlag = state.getValid();
      }

      /**
       *
       * @return sortOrder of this state
       */
      public int getSortOrder()
      {
         return m_sortOrder;
      }

      /**
       *
       * @return transistionID for the transition
       */
      public String getTransitionId()
      {
         return m_transitionID;
      }

      /**
       * Determines if this is a default transition.
       *
       * @return <code>true</code> if it is a default transition.
       */
      public boolean isDefault()
      {
         return m_isDefault;
      }

      /**
       * Gets the char representing the state
       * @return char indicating state
       */
      public char getFlag()
      {
         return m_validFlag;
      }

      /**
       * returns the lower of the PSAutoTransitions in sortOrder
       *
       * @param dat
       *           can be <code>null</code>
       * @return PSAutoTransition with the lower sortOrder never <code>null</code>
       */
      public SortOrderTransition leastSortOrder(SortOrderTransition dat)
      {
         if (dat == null)
            return this;

         if (getSortOrder() < dat.getSortOrder())
            return this;
         else
            return dat;
      }

      /**
       * Given a list of PSAutoTransitions finds the one with the lowest sortOrder.
       * List must have at least one entry.
       *
       * @param list of PSAutoTransition objects may not be <code>null</code>
       * @return PSAutoTransistion with the lowest sortOrder in the list
       *          <code>null</code>
       */
      public SortOrderTransition findLeast(List list)
      {
         Iterator it = list.iterator();
         SortOrderTransition least = (SortOrderTransition) it.next();
         while (it.hasNext())
         {
            SortOrderTransition trans = (SortOrderTransition) it.next();
            if (trans.getSortOrder() < least.getSortOrder())
               least = trans;
         }
         return least;
      }

   }

   /**
    * Interface for used to build the caches in the PSWorkflowInfo
    */
   private interface IPSWorkflowId
   {
      /**
       *
       * @return StateID as a String never <code>null</code>
       */
      public abstract String getID();

      /**
       *
       * @return workflowID as a String never <code>null</code>
       */
      public abstract String getWorkflow();
   }

   /**
    * StateInfo used to encapsulate the information in the States Table. The
    * PSWorkflowInfo keeps a cache of this information. Retrieved from
    * sys_psxWorkflowCataloger/workflowInfo
    */
   private class StateInfo implements IPSWorkflowId
   {
      /**
       * States Table contentValid column char
       */
      private char m_validFlag;

      /**
       * States Table id column never <code>null</code>
       */
      private String m_stateId;

      /**
       * States Table sortOrder column never <code>null</code>
       */
      private String m_sortOrder;

      /**
       * States Table workflow column never <code>null</code>
       */
      private String m_workflowId;

      /**
       * Construct a PSStateInfo with the supplied parameters
       *
       * @param flag
       *           <code>null</code> or empty char containing value in
       *           contentValid column
       * @param state
       *           <code>null</code> or empty String representation of the
       *           stateID
       * @param order
       *           <code>null</code> or empty String valued sortOrder
       * @param workflow
       *           <code>null</code> or empty String valued workflowID
       */
      public StateInfo(char flag, String state, String order, String workflow)
      {
         if ((state == null) || (order == null) && (workflow == null)) {
            throw new IllegalArgumentException("Arguments may not be null");

         }
         m_validFlag = flag;
         m_stateId = state;
         m_sortOrder = order;
         m_workflowId = workflow;
      }

      /**
       *
       * @return StateID as a String never <code>null</code>
       */
      public String getID()
      {
         return m_stateId;
      }

      /**
       *
       * @return the SortOrder as a String never <code>null</code>
       */
      public String getSortOrder()
      {
         return m_sortOrder;
      }

      /**
       *
       * @return workflowID as a String never <code>null</code>
       */
      public String getWorkflow()
      {
         return m_workflowId;
      }

      /**
       *
       * @return char value of the contentValid flag
       */
      public char getValid()
      {
         return m_validFlag;
      }

   }

   /**
    * TransitionInfo used to encapsulate the information in the Transition
    * Table. The PSWorkflowInfo keeps a cache of this information. Retrieved
    * from sys_psxWorkflowCataloger/workflowInfo
    */
   private class TransitionInfo implements IPSWorkflowId
   {

      /**
       * Transition Table default column
       */
      private char m_default;

      /**
       * Transition Table id column never <code>null</code>
       */
      private String m_transitionId;

      /**
       * Transition Table TransitionToState column  never <code>null</code>
       */
      private String m_transitionTo;

      /**
       * Transition TransitionFromState column never <code>null</code>
       */
      private String m_transitionFrom;

      /**
       * Transition Table workflow column never <code>null</code>
       */
      private String m_workflowId;

      /**
       * Creates a transitionInfo object
       *
       * @param defaultTrans
       *           'y' if default transition.
       * @param transitionId
       *           may not be <code>null</code>.
       * @param transitionTo
       *           may not be <code>null</code>.
       * @param transitionFrom
       *           may not be <code>null</code>.
       * @param workflowId
       *           may not be <code>null</code>.
       */
      public TransitionInfo(char defaultTrans, String transitionId,
            String transitionTo, String transitionFrom, String workflowId)
      {
         if ((transitionId == null) || (transitionFrom == null)
               || (transitionTo== null) || (workflowId == null))
            throw new IllegalArgumentException("Arguments may not be null");

         m_default = defaultTrans;
         m_transitionFrom = transitionFrom;
         m_transitionTo = transitionTo;
         m_workflowId = workflowId;
         m_transitionId = transitionId;
      }

      /**
       * Get stateid of To state
       *
       * @return String stateID of the state to transition to
       * never <code>null</code>
       */
      public String getTransitionTo()
      {
         return m_transitionTo;
      }

      /**
       * Get stateid of From state
       *
       * @return String stateID of the originating state never <code>null</code>
       */
      public String getTransitionFrom()
      {
         return m_transitionFrom;
      }

      /**
       * Get id of this transition
       *
       * @return String id of this transition never <code>null</code>
       */
      public String getTransitionId()
      {
         return m_transitionId;
      }

      /**
       * Get id of this transition
       *
       * @return String id of this transition never <code>null</code>
       */
      public String getID()
      {
         return m_transitionId;
      }

      /**
       * Check to see if this is a default transition
       *
       * @return <code>true</code> if default transition
       */
      public boolean isDefault()
      {
         return m_default == 'y';
      }

      /**
       * Get the value of the transition default flag
       *
       * @return char value either 'y' or 'n' if empty its set to 'n'
       */
      public char getDefaultFlag()
      {
         return m_default;
      }

      /**
       * Get the workflowId
       *
       * @return String workflowId never <code>null</code>
       */
      public String getWorkflow()
      {
         return m_workflowId;
      }
   }

   /**
    * Map containing PSStateInfo list indexed by String workflowID
    * never <code>null</code>.
    */
   private HashMap m_states = new HashMap();

   /**
    * Map containing PSTransitionInfo list indexed by String workflowID
    * never <code>null</code>.
    */
   private HashMap m_transitions = new HashMap();

   /**
    * Logger for use with log4j
    */
   private Logger m_logger = Logger.getLogger(getClass());

   /**
    * Constants for XML ELEMENTS
    */
   private static final String ELEM_STATEINFO = "stateinfo";

   private static final String ELEM_TRANSITIONINFO = "transitioninfo";

   private static final String ELEM_HISTORY = "History";

   private static final String EL_COMMENT = "Comment";

   /**
    *  Constants for XML ATTRIBUTES
    */
   private static final String ATTR_SORT_ORDER = "sortorder";

   private static final String ATTR_VALID_FLAG = "contentvalid";

   private static final String ATTR_DEFAULT_TRANS = "default";

   private static final String ATTR_TRANSTO = "transitionToState";

   private static final String ATTR_TRANSFROM = "transitionFromState";

   private static final String ATTR_WORKFLOWID = "workflowid";

   private static final String ATTR_TID = "transitionid";

   private static final String ATTR_STATEID = "stateid";

}
