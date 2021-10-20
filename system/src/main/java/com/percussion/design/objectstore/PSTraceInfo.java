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

package com.percussion.design.objectstore;

import com.percussion.debug.IPSTraceStateListener;
import com.percussion.debug.PSTraceFlag;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Encapsulates all trace options contained in the application.
 */
public class PSTraceInfo extends PSComponent
{

   /**
    * Constructor for this class.  Calls fromXml to initialize itself using the
    * supplied xml.  The default locale will be used for the names and descriptions
    * of trace messages
    * @param sourceNode the source node containing all info required to create this
    * object
    * @param parentDoc the Java object which is the parent of this Object
    * @param parentComponents the parent objects of this object
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    * appropriate type
    * @roseuid 39F46A7B02BF
    */
   public PSTraceInfo(Element sourceNode, IPSDocument parentDoc, List parentComponents) throws PSUnknownNodeTypeException
   {
      this(sourceNode, parentDoc, parentComponents, Locale.getDefault());
   }

   /**
    * Returns the composite flag object indicating which options are currently
    * enabled for the application.
    * @return a flag object representing all currently enabled trace options.
    * Will never be <code>null</code>.
    * @roseuid 39F46AE60261
    */
   public PSTraceFlag getTraceOptionsFlag()
   {
      return m_traceFlag;
   }

   /**
    * Returns a flag object with the options enabled that were originally
    * enabled when this object was first instantiated.
    * @return the flag object with the initial options set.  Should not be
    * <code>null</code>.
    */
   public PSTraceFlag getInitialOptionsFlag()
   {
      if (m_initialTraceFlag == null)
      {
         // this will only happen if a bug is introduced at some point
         throw new IllegalStateException("Default options not initialized");
      }
      return m_initialTraceFlag;
   }


   /**
    * Resets the trace flags with the options enabled that were originally
    * enabled when this object was first instantiated.
    */
   public void restoreInitialOptions()
   {

      /* Be sure we have the initial options - this will throw an
       * exception if it is null (this would be a bug)
       */
      PSTraceFlag initial = getInitialOptionsFlag();

      /* need to use copy or else the regular flag and the initial
       * flag end up referencing the same object
       */
      try
      {
         PSTraceFlag flag = new PSTraceFlag();
         flag.copyFrom(initial);

         // need to use the set method to trigger changes to enabled status
         setTraceOptionsFlag(flag, true);
      }
      catch (IllegalArgumentException e)
      {
         /* this means initial is null - but that was already checked above
          * when we got the intial options
          */
      }

   }

   /**
    * Sets the options to enable for the application.  All options specified by
    * the flag will be enabled.  All that are not enabled by the flag will be
    * disabled. If any bits in the flag are enabled, this will set
    * tracing to enabled overall.  If none of the bits are enabled, this will
    * diable tracing overall.  Also, all listeners must be notified of a
    * start or stop event.
    * @param optionsFlag a composite flag representing the trace options to
    * enable for the application.  May not be <code>null</code>.
    * @roseuid 39F46B2F003E
    */
   public void setTraceOptionsFlag(PSTraceFlag optionsFlag)
   {
      setTraceOptionsFlag(optionsFlag, true);
   }

   /**
    * Sets the options to enable for the application.  All options specified by
    * the flag will be enabled.  All that are not enabled by the flag will be
    * disabled. If any trace options in the flag are enabled, this will set
    * tracing to enabled overall if the updateEnabled is set to
    * <code>true</code>.  If none of the bits are enabled, this will
    * diable tracing overall if the updateEnabled is set to
    * <code>true</code>.  Also, all listeners must be notified of a
    * start or stop event.
    * @param optionsFlag a composite flag representing the trace options to
    * enable for the application.  May not be <code>null</code>.
    * @param updateEnabled If <code>true</code>, the overall trace enabled flag
    * is affected by the state of the flag.
    */
   public void setTraceOptionsFlag(PSTraceFlag optionsFlag,
      boolean updateEnabled)
   {
      // check to see if any groups are non-zero (trace enabled)
      boolean isEnabled = optionsFlag.isTraceEnabled();

      // replace the flag with the one passed in
      m_traceFlag = optionsFlag;

      // update the enabled status
      if (updateEnabled)
         setTraceEnabled(isEnabled);
   }

   /**
    * Used to determine if a tracing is enabled for the application.
    *
    * @return <code>true</code> if tracing is enabled, <code>false</code> if not
    * @roseuid 39F5BD5E038A
    */
   public boolean isTraceEnabled()
   {
      return m_enabled;
   }

   /**
    * Accessor for trace message output column width
    *
    * @return the width of the trace message output in number of chars
    * @roseuid 39F5BE07032C
    */
   public int getColumnWidth()
   {
      return m_columnWidth;
   }

   /**
    * Accessor for the description of a particular trace option.
    *
    * @param traceFlag describes which trace option's description to return.
    * May not be <code>null</code>.
    * @return the description of the specified option
    * @roseuid 39F5BE4A01A5
    */
   public String getOptionDescription(int traceFlag)
   {
      String desc = null;
      boolean found = false;
      Iterator i = ms_options.iterator();
      while (i.hasNext())
      {
         PSTraceOption option = (PSTraceOption)i.next();
         if (option.getFlag() == traceFlag)
         {
            desc = option.getDescription();
            found = true;
            break;
         }
      }

      if (!found)
         throw new IllegalArgumentException("invalid traceflag: " + traceFlag);

      return desc;
   }

   /**
    * Retrieves the text name for this option
    *
    * @param traceFlag Flag specifiying the option.  May not be <code>null</code>.
    * @return the name of this option.  Never <code>null</code>.
    * @roseuid 39F5CB680000
    */
   public String getOptionName(int traceFlag)
   {
      String name = null;
      boolean found = false;
      Iterator i = ms_options.iterator();
      while (i.hasNext())
      {
         PSTraceOption option = (PSTraceOption)i.next();
         if (option.getFlag() == traceFlag)
         {
            name = option.getDisplayName();
            found = true;
            break;
         }
      }

      if (!found)
         throw new IllegalArgumentException("invalid traceflag: " + traceFlag);

      return name;
   }

   /**
    * Used to determine if a particular trace option is enabled.
    *
    * @param traceFlag flag indicating the option to check. May not be
    * <code>null</code>.
    * @return <code>true</code> if that option is enabled, <code>false</code> if not
    * @roseuid 39F6FE950177
    */
   public boolean isTraceEnabled(int traceFlag)
   {
      if (!isValidOption(traceFlag))
         throw new IllegalArgumentException("traceFlag is not valid: " + traceFlag);

      return m_traceFlag.checkBit(traceFlag);
   }

   /**
    * This method is called to create a PSXTraceInfo XML element node containing the
    * data described in this object.  Creates node and sets attributes for
    * traceEnabled, outputColumnWidth, and walks options in m_options to store
    * attributes for each option, checking the bit in m_traceFlag to see if they are
    * enabled and creating attribute with the option's name.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    * &lt;!--
    * PSXTraceInfo is used to describe all information regarding
    * the trace options for an application.
    * --&gt;
    * &lt;!ELEMENT PSXTraceInfo &gt;
    * &lt;ATTLIST PSXTraceInfo
    * traceEnabled (yes|no) "no"
    * traceOutputColumnWidth CDATA
    * traceTimestampOnlyEnabled (yes|no) "no"
    * traceBasicRequestInfo (yes|no) "no"
    * traceInitHttpVar (yes|no) "no"
    * traceFileInfo (yes|no) "no"
    * traceAppHandlerProc (yes|no) "no"
    * traceAppSecurity (yes|no) "no"
    * tracePostPreProcHttpVar (yes|no) "no"
    * traceResourceHandler (yes|no) "no"
    * traceMapper (yes|no) "no"
    * traceSessionInfo (yes|no) "no"
    * traceDbPool (yes|no) "no"
    * traceExitProc (yes|no) "no"
    * traceExitExec (yes|no) "no"
    * tracePostExitXml (yes|no) "no"
    * tracePostExitCgi (yes|no) "no"
    * traceOutputConv (yes|no) "no"
    * &gt;
    * </code></pre>
    * @param doc the document from which to create the returned element
    * @return  the newly created XML element node
    * @roseuid 39F72D340251
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      // first set the fixed attributes for tracing
      root.setAttribute("traceEnabled", (m_enabled ? "yes" : "no"));
      root.setAttribute("traceOutputColumnWidth", Integer.toString(m_columnWidth));
      root.setAttribute("traceTimestampOnlyEnabled",
                        (m_timeStampOnly ? "yes" : "no"));

      // now set the trace option attributes
      Iterator i = ms_options.iterator();
      while (i.hasNext())
      {
         PSTraceOption o = (PSTraceOption)i.next();
         root.setAttribute(o.getName(),
                           (isTraceEnabled(o.getFlag()) ? "yes" : "no"));
      }

      return root;
   }

   /**
    * This method is called to populate a PSTraceOption Java object
    * from a PSXTraceOption XML element node. Uses PSTraceMessageFactory to get
    * a list of all possible options.  Uses options set in its Xml and get's
    * the list of possible trace options.  For each option it finds in that
    * list, it sets the bit value on a new PSTraceFlag if enabled in its own
    * xml.  Finally, creates a copy of the flag to save as the initial options.
    * See the {@link #toXml(Document) toXml} method for a description of the XML
    * object.
    * @throws PSUnknownNodeTypeException  if the XML element node does not
    * represent a type supported
    * by the class.
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException
   {

      if (sourceNode == null){
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      // make sure we got the correct type node
      if (false == ms_NodeType.equals(sourceNode.getNodeName())){
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      // get the main trace enabled setting
      sTemp = tree.getElementData("traceEnabled");
      if ((sTemp == null) || !(sTemp.equals("yes") || sTemp.equals("no")))
      {
         Object[] args = { ms_NodeType, "traceEnabled",
            ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else
      {
         m_enabled = (sTemp.equals("yes") ? true : false);
      }

      // get the column width setting
      sTemp = tree.getElementData("traceOutputColumnWidth");
      try{
         m_columnWidth = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, "traceOutputColumnWidth",
            ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      // get the Timestamp Only setting
      sTemp = tree.getElementData("traceTimestampOnlyEnabled");
      if ((sTemp == null) || !(sTemp.equals("yes") || sTemp.equals("no")))
      {
         Object[] args = { ms_NodeType, "traceTimestampOnlyenabled",
            ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else
      {
         m_timeStampOnly = (sTemp.equals("yes") ? true : false);
      }

      // now check for each option - it's okay if not found, just set to false
      m_traceFlag = new PSTraceFlag();    // reset all options to off
      Iterator i = ms_options.iterator();
      while (i.hasNext())
      {
         PSTraceOption o = (PSTraceOption)i.next();
         sTemp = tree.getElementData(o.getName());
         if (sTemp != null)
         {
            if (sTemp.equals("yes"))
               m_traceFlag.setBit(o.getFlag());
            else if (!sTemp.equals("no"))
            {
               Object[] args = { ms_NodeType, o.getName(), sTemp };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }
      }

      // save copy as initial options
      m_initialTraceFlag = new PSTraceFlag();
      try
      {
         m_initialTraceFlag.copyFrom(m_traceFlag);
      }
      catch(IllegalArgumentException e)
      {
         // this should never happen since we just initialized the trace flag
      }
   }

   /**
    * Default Constructor for this class.  Initializes object with all default
    * values.  Sets default values for column width, sets trace disabled, and gets a
    * trace flag object from the factory with the default trace messages enabled.
    * The default locale will be used for the names and descriptions of trace messages
    * @roseuid 39F7473401E4
    */
   public PSTraceInfo()
   {
      this(Locale.getDefault());
   }

   /**
    * Adds a trace state listener to this object so they may be informed of starting
    * and stopping trace events.  If tracing is enabled, notify a start.
    *
    * @param traceStateListener the listener to add.  May not be <code>null</code>.
    * @roseuid 39F84B9E0167
    */
   public void addTraceStateListener(IPSTraceStateListener traceStateListener)
   {
      m_traceStateListeners.add(traceStateListener);
      if (m_enabled)
         traceStateListener.traceStarted(this);
   }

   /**
    * Removes a traceStateListener so it will no longer be informed of any trace
    * start and stop events.
    *
    * @param listener the listener to remove.  May not be <code>null</code>.
    * @roseuid 39F84C130213
    */
   public void removeTraceStateListener(IPSTraceStateListener listener)
   {
      m_traceStateListeners.remove(listener);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTraceInfo)) return false;
      if (!super.equals(o)) return false;
      PSTraceInfo that = (PSTraceInfo) o;
      return m_enabled == that.m_enabled &&
              m_timeStampOnly == that.m_timeStampOnly &&
              m_columnWidth == that.m_columnWidth &&
              CollectionUtils.isEqualCollection(m_traceStateListeners, that.m_traceStateListeners) &&
              Objects.equals(m_traceFlag, that.m_traceFlag);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_enabled, m_traceStateListeners.hashCode(), m_timeStampOnly, m_columnWidth, m_traceFlag);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param info a valid PSTraceInfo. If null, a IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( PSTraceInfo info )
   {
      // call super's copy
      copyFrom((PSComponent) info );

      // copy all members
      m_enabled = info.isTraceEnabled();
      m_columnWidth = info.getColumnWidth();
      m_timeStampOnly = info.IsTimeStampOnlyTrace();
      m_initialTraceFlag = info.getInitialOptionsFlag();

      // copy the flag
      PSTraceFlag flag = new PSTraceFlag();
      flag.copyFrom(info.getTraceOptionsFlag());
      m_traceFlag = flag;


   }

   /**
    * Notify all TraceStateListeners if enabled flag is changing from disabled to
    * enabled.
    * @roseuid 39F86E8900CB
    */
   private void notifyStart()
   {
      Iterator list = m_traceStateListeners.iterator();
      while(list.hasNext())
         ((IPSTraceStateListener)list.next()).traceStarted(this);
   }

   /**
    * Notitfy all listeners when enabled flag changed from enabled to disabled.
    * @roseuid 39F86F6202FD
    */
   private void notifyStop()
   {
      Iterator list = m_traceStateListeners.iterator();
      while(list.hasNext())
      {
         ((IPSTraceStateListener)list.next()).traceStopped(this);
      }
   }

   /**
    * Notitfy all listeners when flag set with at least one group enabled and
    * tracing was already enabled.
    */
   private void notifyRestart()
   {
      Iterator list = m_traceStateListeners.iterator();
      while(list.hasNext())
      {
         ((IPSTraceStateListener)list.next()).traceRestarted(this);
      }
   }

   /**
    * Sets the application's tracing as enabled or disabled.
    *
    * @param isEnabled if <code>true</code>, tracing is enabled, if
    * <code>false</code>, tracing is disabled.
    * @roseuid 3A01DA06002E
    */
   public void setTraceEnabled(boolean isEnabled)
   {

      // see if starting or stopping
      if (isEnabled && !m_enabled)
         notifyStart();
      else if (!isEnabled && m_enabled)
         notifyStop();
      else if (isEnabled && m_enabled)
         notifyRestart();

      m_enabled = isEnabled;
   }

   /**
    * get list of allowable trace options
    *
    * @return returns list of PSTraceOption objects.  Never <code>null</code>
    * @roseuid 3A01DAE5037A
    */
   public Iterator getTraceOptions()
   {
      return ms_options.iterator();
   }

   /**
    * Sets the trace output column width
    *
    * @param width the number of chars
    * @roseuid 3A02D63100AB
    */
   public void setColumnWidth(int width)
   {
      m_columnWidth = width;
   }

   /**
    * Determine if Timestamp Only tracing is enabled.
    * @return <code>true</code> if enabled, <code>false</code> if not enabled.
    * @roseuid 3A02DAFB009C
    */
   public boolean IsTimeStampOnlyTrace()
   {
      return m_timeStampOnly;
   }

   /**
    * Enables or disables Timestamp Only tracing
    *
    * @param isTimestampOnly If <code>true</code> then option is enabled, if
    * <code>false</code>, it is disabled.
    * @roseuid 3A02DB8E03B9
    */
   public void setTimeStampOnlyTrace(boolean isTimestampOnly)
   {
      m_timeStampOnly = isTimestampOnly;
   }

   /**
    * Enables or disables tracing for a particular trace option
    *
    * @param traceFlag flag specifying the particular trace option
    * @param isEnabled if <code>true</code>, tracing is enabled.  if
    * <code>false</code>, it is disabled.
    * @roseuid 3A02E43B037A
    */
   public void setTraceEnabled(int traceFlag, boolean isEnabled)
   {
      if (!isValidOption(traceFlag))
         throw new IllegalArgumentException("traceFlag is not valid: " + traceFlag);

      if (isEnabled)
         m_traceFlag.setBit(traceFlag);
      else
         m_traceFlag.clearBit(traceFlag);
   }

   /**
    * Constructor for this class.  Calls fromXml to initialize itself using the
    * supplied xml. Saves a copy of the flag as initial options.
    *
    * @param sourceNode the source node containing all info required to create this
    * object
    * @param parentDoc the Java object which is the parent of this Object
    * @param parentComponents the parent objects of this object
    * @param locale the locale to use for the names and descriptions of trace
    * messages.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    * appropriate type
    * @roseuid 3A06C9CE000F
    */
   public PSTraceInfo(Element sourceNode, IPSDocument parentDoc,
         List parentComponents, Locale locale)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
      createOptionsList(locale);
   }

   /**
    * Default Constructor for this class.  Initializes object with all default
    * values.  Sets default values for column width, sets trace disabled, and
    * gets a trace flag object from the factory with the default trace messages
    * enabled.  Saves a copy as the initial options.
    *
    * @param locale The Locale to use for the names and descriptions of the trace
    * messages.
    * @roseuid 3A06C9F90399
    */
   public PSTraceInfo(Locale locale)
   {
      m_columnWidth = 80;
      m_enabled = false;
      m_traceFlag = PSTraceMessageFactory.getDefaultOptionsFlag();
      createOptionsList(locale);

      // save copy as initial options
      try
      {
         m_initialTraceFlag = new PSTraceFlag();
         m_initialTraceFlag.copyFrom(m_traceFlag);
      }
      catch(IllegalArgumentException e)
      {
         // this should never happen since we just created the flag
      }
   }

   /**
    * gets the list of options from the factory and stores it
    *
    * @param locale The Locale to use for the names and descriptions of the trace
    * messages.
    */
   private void createOptionsList(Locale locale)
   {
      synchronized (ms_options)
      {
         ms_options = PSTraceMessageFactory.getPossibleOptions(locale);
      }
   }

   /**
    * checks for flag in list of options.
    *
    * @param flag the flag to validate
    * @return <code>true</code> if flag specifies a valid trace option,
    * <code>false</code> if not.
    */
   private boolean isValidOption(int flag)
   {
      Iterator list = ms_options.iterator();
      while (list.hasNext())
      {
         if (((PSTraceOption)list.next()).getFlag() == flag)
            return true;
      }
      return false;
   }

   /**
    * Indicates if tracing is enabled for the application
    */
   private boolean m_enabled;

   /**
    * A list of allowable PSTraceOptions.  Used to set enabled bits on PSTraceFlag
    * when reading in state from Xml, and for checking enabled bits when writing out
    * state to Xml.
    */
   private static ArrayList ms_options = new ArrayList();

   /**
    * Stores the name of the Xml element used to persist this object
    *
    * @see #toXml(Document)
    */
   static final String ms_NodeType = "PSXTraceInfo";

   /**
    * list of listeners to inform when changes are made to the enabled flag
    */
   private ArrayList m_traceStateListeners = new ArrayList();

   /**
    * If <code>true</code>, trace messages are logged with a timestamp and header,
    * but with no body.  <code>false</code> by default.
    */
   private boolean m_timeStampOnly = false;

   /**
    * column width of the trace output
    */
   private int m_columnWidth = 80;

   /**
    * The composite flag object which maintains the current enabled state of each
    * trace option.  Initialized during constructor execution.
    */
   private PSTraceFlag m_traceFlag;

   /**
    * The composite flag object which maintains the initial enabled state of
    * each trace option.  Initialized during constructor execution or in fromXml.
    * @see #fromXml(Element, IPSDocument, List)
    */
   private PSTraceFlag m_initialTraceFlag = null;


}
