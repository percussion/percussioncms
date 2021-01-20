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

package com.percussion.design.objectstore;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class wraps the system def xml document and provides access to the
 * objectstore objects defined within it.
 */
public class PSContentEditorSystemDef implements IPSDocument
{
   /**
    * Constructor for this class that takes a source document.
    *
    * @param sourceDoc The Xml document containing the Content Editor system
    * def.
    *
    * @throws PSUnknownDocTypeException if the XML document is not of the
    * appropriate type.
    * @throws PSUnknownNodeTypeException if an XML element node is not of the
    * appropriate type.
    * @see #fromXml(Document)
    */
   public PSContentEditorSystemDef(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      if (sourceDoc == null)
         throw new IllegalArgumentException("sourceDoc may not be null");

      fromXml( sourceDoc );
   }

   /**
    * Calculates and returns the guid for this object.
    * 
    * @return The guid, always the same value, never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.CONFIGURATION, SYSTEM_DEF_ID);      
   }
   
   /**
    * Returns the minimum period of time to wait (in minutes) before any
    * document in the local error cache is discarded.
    * @return The timeout in minutes.
    */
   public int getCacheTimeout()
   {
      return m_cacheTimeout;
   }

   /**
    * Get the command names for which there are input data exits specified.
    * Names may be passed to {@link #getInputDataExits(String)} to retrieve
    * input data exits.
    *
    * @return An iterator over zero or more command names as <code>String</code>
    * objects.
    */
   public Iterator getInputDataExitCommands()
   {
      return m_inputDataExits.keySet().iterator();
   }

   /**
    * Returns an iterator over <code>zero</code> or more PSExtensionCall objects
    * used for input processing for the specified command name.
    *
    * @param commandName The command name for which the exits are to be
    * returned.  May not be <code>null</code>.
    *
    * @return The iterator, may be empty if none were supplied for the specified
    * command.
    */
   public Iterator getInputDataExits(String commandName)
   {
      if (commandName == null)
         throw new IllegalArgumentException("commandName may not be null");

      Iterator results = null;

      PSExtensionCallSet exits = (PSExtensionCallSet)m_inputDataExits.get(
         commandName);


      if (exits != null)
         results = exits.iterator();
      else
      {
         // create an empty iterator to return
         ArrayList list = new ArrayList();
         results = list.iterator();
      }

      return results;
   }

   /**
    * Get the command names for which there are result data exits specified.
    * Names may be passed to {@link #getResultDataExits(String)} to retrieve
    * result data exits.
    *
    * @return An iterator over zero or more command names as <code>String</code>
    * objects.
    */
   public Iterator getResultDataExitCommands()
   {
      return m_resultDataExits.keySet().iterator();
   }

   /**
    * Returns an iterator over <code>zero</code> or more PSExtensionCall objects
    * used for result data processing for the specified command name.
    *
    * @param commandName The command name for which the exits are to be
    * returned.  May not be <code>null</code>.
    *
    * @return The iterator, may be empty if none were supplied for the specified
    * command.
    */
   public Iterator getResultDataExits(String commandName)
   {
      if (commandName == null)
         throw new IllegalArgumentException("commandName may not be null");

      Iterator results = null;

      PSExtensionCallSet exits = (PSExtensionCallSet)m_resultDataExits.get(
         commandName);


      if (exits != null)
         results = exits.iterator();
      else
      {
         // create an empty iterator to return
         results = PSIteratorUtils.emptyIterator();
      }

      return results;

   }


   /**
    * Returns a map of param names, with the internal name as the key and the
    * html parameter name as the value.
    * @return The map of names, never <code>null</code>, possibly empty.
    */
   public Map getParamNames()
   {
      return m_paramNames;
   }

   /**
    * Returns the map of initparams, with the cmd name as the key and an
    * ArrayList of PSParam objects as the value.
    * @return The map, never <code>null</code>, possibly empty.
    */
   public Map getInitParams()
   {
      return m_initParams;
   }

   /**
    * Returns the default system application flow.
    * @return The application flow object, never <code>null</code>.
    */
   public PSApplicationFlow getApplicationFlow()
   {
      return m_appFlow;
   }

   /**
    * Returns the stylesheets for converting queried data for output.
    * @return The set of PSCommandHandlerStyleSheets, never <code>null</code>.
    */
   public PSCommandHandlerStylesheets getStyleSheetSet()
   {
      return m_styleSheets;
   }

   /**
    * Returns the system locator for retrieving global items such as lookups
    * or system ids.
    * @return The locator object, never <code>null</code>.
    */
   public PSContainerLocator getSystemLocator()
   {
      return m_systemLocator;
   }

   /**
    * Returns the container locator for retrieving system fields.
    * @return The locator object, may be <code>null</code>.
    */
   public PSContainerLocator getContainerLocator()
   {
      return m_containerLocator;
   }

   /**
    * Returns the field set defining all system fields.
    * @return The field set, may be <code>null</code>.
    */
   public PSFieldSet getFieldSet()
   {
      return m_fieldSet;
   }

   /**
    * Returns the UI definition used to display all system fields.
    * @return The UI def, may be <code>null</code>.
    */
   public PSUIDefinition getUIDefinition()
   {
      return m_uiDef;
   }


   /**
    * Returns an iterator over zero or more PSConditionalExits used
    * to validate all system fields.
    * @return The iterator, never <code>null</code>, may be empty.
    */
   public Iterator getValidationRules()
   {
      Iterator results = null;

      if (m_groupValidations != null)
         results = m_groupValidations.iterator();
      else
      {
         // create an empty iterator to return
         results = PSIteratorUtils.emptyIterator();
      }

      return results;
   }


   /**
    * Returns an iterator over <code>zero</code> or more PSConditionalExits
    * used to do input translations on all system fields.
    * @return The iterator, never <code>null</code>, may be empty.
    */
   public Iterator getInputTranslations()
   {
      Iterator results = null;

      if (m_inputTranslations != null)
         results = m_inputTranslations.iterator();
      else
      {
         // create an empty iterator to return
         results = PSIteratorUtils.emptyIterator();
      }

      return results;
   }


   /**
    * Returns an iterator over <code>zero</code> or more PSConditionalExits
    * used to do output translations on all system fields.
    * @return The iterator, never <code>null</code>, may be empty.
    */
   public Iterator getOutputTranslations()
   {
      Iterator results = null;

      if (m_outputTranslations != null)
         results = m_outputTranslations.iterator();
      else
      {
         // create an empty iterator to return
         results = PSIteratorUtils.emptyIterator();
      }

      return results;
   }


   /**
    * Produces an XML representation of this object that conforms to the DTD
    * defined by "E2/design/dtd/sys_ContentEditorSystemDef.dtd", which is
    * excerpted here:
    *
    * <code><pre>
    * &lt;!ELEMENT ContentEditorSystemDef (SystemLocator,
    * PSXCommandHandlerStylesheets, PSXApplicationFlow, SectionLinkList?,
    * CommandHandlerExits*, SystemParamNames?, InitParams*,
    * (PSXContainerLocator, PSXFieldSet, PSXUIDefinition, PSXValidationRules?,
    *  PSXInputTranslations?, PSXOutputTranslations?)?)>
    * &lt;!ATTLIST ContentEditorSystemDef
    *    cacheTimeout CDATA "15"
    * >
    *
    * &lt;!ELEMENT SystemLocator (PSXContainerLocator)>
    * &lt;!ELEMENT CommandHandlerExits (InputDataExits, ResultDataExits)>
    * &lt;!ATTLIST CommandHandlerExits
    *    commandName CDATA #REQUIRED
    * >
    * &lt;!ELEMENT InputDataExits (PSXExtensionCallSet?)>
    * &lt;!ELEMENT ResultDataExits (PSXExtensionCallSet?)>
    * &lt;!ELEMENT SystemParamNames (PSXParam+)>
    * &lt;!ELEMENT InitParams (CommandName+, PSXParam+)>
    * </pre></code>
    * @return the newly created XML document (never <code>null</code> or empty)
    * @throws IllegalStateException if the internal state of this object
    * violates the DTD
    */
   public Document toXml()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, XML_NODE_NAME);

      root.setAttribute( ATTR_CACHE_TIMEOUT, String.valueOf( m_cacheTimeout ) );
      // SystemLocator
      Element systemLocator = doc.createElement( ELEMENT_SYSTEM_LOCATOR );
      systemLocator.appendChild( m_systemLocator.toXml( doc ) );
      root.appendChild( systemLocator );
      // PSXCommandHandlerStylesheets
      root.appendChild( m_styleSheets.toXml( doc ) );
      // PSXApplicationFlow
      root.appendChild( m_appFlow.toXml( doc ) );

      // SectionLinkList?
      if (m_sectionLinkList.size() > 0)
      {
         Element sectionLinks = doc.createElement( ELEMENT_SECTION_LINK_LIST );
         for (int i = 0; i < m_sectionLinkList.size(); i++)
         {
            PSUrlRequest url = (PSUrlRequest) m_sectionLinkList.elementAt( i );
            sectionLinks.appendChild( url.toXml( doc ) );
         }
         root.appendChild( sectionLinks );
      }

      // CommandHandlerExits*
      Iterator iter = m_inputDataExits.keySet().iterator();
      while ( iter.hasNext() )
      {
         String cmdName = iter.next().toString();
         Element cmdExits = doc.createElement( ELEMENT_COMMAND_HANDLER_EXITS );
         cmdExits.setAttribute( ATTR_COMMAND_NAME, cmdName );
         Element inputExits = doc.createElement( ELEMENT_INPUT_DATA_EXITS);
         inputExits.appendChild( ((PSExtensionCallSet)m_inputDataExits
               .get( cmdName )).toXml( doc ) );
         cmdExits.appendChild( inputExits );
         Element resultExits = doc.createElement( ELEMENT_RESULT_DATA_EXITS);
         resultExits.appendChild( ((PSExtensionCallSet)m_resultDataExits
               .get( cmdName )).toXml( doc ) );
         cmdExits.appendChild( resultExits );
         root.appendChild( cmdExits );
      }

      // SystemParamNames?
      if (m_paramNames.size() > 0)
      {
         Element sysParams = doc.createElement( ELEMENT_SYSTEM_PARAM_NAMES );
         iter = m_paramNames.entrySet().iterator();
         while ( iter.hasNext() )
         {
            Map.Entry entry = (Map.Entry) iter.next();
            PSParam param = new PSParam( entry.getKey().toString(),
                  (IPSReplacementValue) entry.getValue() );
            sysParams.appendChild( param.toXml( doc ) );
         }
         root.appendChild( sysParams );
      }

      // InitParams*
      /* it isn't possible to recreate the XML exactly b/c we don't know
         which command names are using the same param set.  So an input of:
         <InitParams>
            <CommandName>Alpha</CommandName>
            <CommandName>Bravo</CommandName>
            <PSXParam name="yada">...</PSXParam>
         </InitParams>
         will become (correct, but verbose):
         <InitParams>
            <CommandName>Alpha</CommandName>
            <PSXParam name="yada">...</PSXParam>
         </InitParams>
         <InitParams>
            <CommandName>Bravo</CommandName>
            <PSXParam name="yada">...</PSXParam>
         </InitParams> */
      // TODO: change the internal representation so we can reproduce output
      // TODO: that matches the input.  this should happen before this object
      // TODO: is used to overwrite the source file.
      Iterator cmdIter = m_initParams.keySet().iterator();
      while ( cmdIter.hasNext() )
      {
         String cmdName = cmdIter.next().toString();
         Element initParams = doc.createElement( ELEMENT_INIT_PARAMS );
         PSXmlDocumentBuilder.addElement( doc, initParams, ELEMENT_COMMAND_NAME,
            cmdName );
         Iterator paramIter = ((List)m_initParams.get( cmdName )).iterator();
         while ( paramIter.hasNext() )
         {
            PSParam param = (PSParam) paramIter.next();
            initParams.appendChild( param.toXml( doc ) );
         }
         root.appendChild( initParams );
      }

      if ( ( (null == getContainerLocator()) != (null == getFieldSet()) ) ||
            ( (null == getFieldSet()) != (null == getUIDefinition()) ) )
      {
         throw new IllegalStateException("PSXContainerLocator, PSXFieldSet, "+
            "and PSXUIDefinition must be all null or all not null.");
      }

      PSContainerLocator loc = getContainerLocator();
      if (loc != null)
      {
         // PSXContainerLocator
         root.appendChild( loc.toXml( doc ) );
         // PSXFieldSet
         root.appendChild( getFieldSet().toXml( doc ) );
         // PSXUIDefinition
         root.appendChild( getUIDefinition().toXml( doc ) );

         // PSXValidationRules?
         if (m_groupValidations != null)
         {
            root.appendChild( m_groupValidations.toXml( doc ) );
         }
         // PSXInputTranslations?
         if (m_inputTranslations != null)
         {
            root.appendChild( m_inputTranslations.toXml( doc ) );
         }
         // PSXOutputTranslations?
         if (m_outputTranslations != null)
         {
            root.appendChild( m_outputTranslations.toXml( doc ) );
         }
      }

      return doc;
   }


   /**
    * This method is called to populate a PSContentEditorSystemDef Java object
    * from a document containing a ContentEditorSystemDef XML root element node.
    * The format is specified by E2/design/dtd/sys_ContentEditorSystemDef.dtd
    *
    * @param doc the PSXContentEditorSharedDef document. May not be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if an XML element node is missing or
    *    is not of the appropriate type.
    */
   public void fromXml(Document doc)
         throws PSUnknownNodeTypeException, PSUnknownDocTypeException
   {
      if (null == doc) throw new PSUnknownDocTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      Element sourceNode = doc.getDocumentElement();
      PSComponent.validateElementName(sourceNode, XML_NODE_NAME);

      // get the cacheTimeout if specified
      String timeOut = sourceNode.getAttribute(ATTR_CACHE_TIMEOUT);
      if (timeOut != null || timeOut.trim().length() != 0)
      {
         try
         {
            m_cacheTimeout = Integer.parseInt(timeOut);
         }
         catch (NumberFormatException e)
         {
            Object[] args = {XML_NODE_NAME, ATTR_CACHE_TIMEOUT,
               timeOut};

            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }

      // walk the tree
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;


      // load SystemLocator
      tree.setCurrent(sourceNode);
      Element systemLocator = tree.getNextElement(ELEMENT_SYSTEM_LOCATOR,
         firstFlags);
      if (systemLocator == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ELEMENT_SYSTEM_LOCATOR);

      loadSystemLocator(systemLocator);

      // load StylesheetSet
      Element styleSheets = tree.getNextElement(
         PSCommandHandlerStylesheets.XML_NODE_NAME, nextFlags);

      if (styleSheets == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
               PSCommandHandlerStylesheets.XML_NODE_NAME);

      m_styleSheets = new PSCommandHandlerStylesheets(styleSheets, null, null);

      // load ApplicationFlow
      Element appFlow = tree.getNextElement(PSApplicationFlow.XML_NODE_NAME,
         nextFlags);

      if (appFlow == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
               PSApplicationFlow.XML_NODE_NAME);

      m_appFlow = new PSApplicationFlow(appFlow, null, null);

      // load SectionLinkList if found
      tree.setCurrent(sourceNode);
      Element sectionLinkList = tree.getNextElement(ELEMENT_SECTION_LINK_LIST,
         firstFlags);
      if (sectionLinkList != null)
         loadSectionLinkList(sectionLinkList);

      // load CommandHandlerExits if found
      tree.setCurrent(sourceNode);
      Element exits = tree.getNextElement(ELEMENT_COMMAND_HANDLER_EXITS,
         firstFlags);
      while (exits != null)
      {
         loadExits(exits);
         exits = tree.getNextElement(ELEMENT_COMMAND_HANDLER_EXITS, nextFlags);
      }

      // load SystemParamNames if found
      tree.setCurrent(sourceNode);
      Element paramNames = tree.getNextElement(ELEMENT_SYSTEM_PARAM_NAMES,
         firstFlags);

      if (paramNames != null)
         loadParamNames(paramNames);

      // load InitParams if found
      tree.setCurrent(sourceNode);
      Element initParams = tree.getNextElement(ELEMENT_INIT_PARAMS, firstFlags);
      while (initParams != null)
      {
         loadInitParams(initParams);
         initParams = tree.getNextElement(ELEMENT_INIT_PARAMS, nextFlags);
      }

      /* load (ContainerLocator, FieldSet, UIDef)? - if we find any of these,
       * we need to find them all, start looking from last guaranteed sibling
       * before these nodes.
       */
      Element lastSibling = loadFieldSet(appFlow);

      // if we have a field set, there may be validations and translations
      if (m_fieldSet != null)
      {
         tree.setCurrent(lastSibling);

         // load validations if we have them
         Element validations = tree.getNextElement(
            PSValidationRules.XML_NODE_NAME, nextFlags);
         if (validations != null)
         {
            m_groupValidations = new PSValidationRules();
            m_groupValidations.fromXml(validations, null, null);
         }

         // load input translations if we have them.
         Element inputTranslations = tree.getNextElement(
            PSInputTranslations.XML_NODE_NAME, nextFlags);
         if (inputTranslations != null)
         {
            m_inputTranslations = new PSInputTranslations();
            m_inputTranslations.fromXml(inputTranslations, null, null);
         }

         Element outputTranslations = tree.getNextElement(
            PSOutputTranslations.XML_NODE_NAME, nextFlags);
         if (outputTranslations != null)
         {
            m_outputTranslations = new PSOutputTranslations();
            m_outputTranslations.fromXml(outputTranslations, null, null);
         }

      }

   }


   /**
    * Given the table ref alias of a table in the system locator, returns
    * a PSBackEndCredential and a PSBackEndTable.  This method assumes that
    * any aliased Credentials in the tableSets have already been resolved to
    * include the full credential information.
    *
    * @param tableRef The alias of the table to return.  May not be <code>null
    * </code>.
    * @param cred A PSBackEndCredential object, will be fully populated after
    * the method returns.  May not be <code>null</code>.  Any state this object
    * has will be overwritten by this method.
    * @param table Must be a PSBackEndTable object with alias defined, will be
    * fully populated after the method returns.  May not be <code>null</code>.
    */
   public void populateSystemTableInfo(String tableRef,
      PSBackEndCredential cred, PSBackEndTable table)
   {

      if (tableRef == null)
         throw new IllegalArgumentException("tableRef may not be null");

      if (cred == null)
         throw new IllegalArgumentException("cred may not be null");

      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      // get the first tableset (should be exactly one)
      PSTableSet tableSet = (PSTableSet)m_systemLocator.getTableSets().next();

      // get the table to use
      String tableName = null;
      Iterator refs = tableSet.getTableRefs();
      while (refs.hasNext())
      {
         PSTableRef ref = (PSTableRef)refs.next();
         if (ref.getAlias().equals(tableRef))
         {
            tableName = ref.getName();
            break;
         }
      }

      // get the credential to use
      PSTableLocator locator = tableSet.getTableLocation();
      PSBackEndCredential sysCred = locator.getCredentials();
      try
      {
         cred.copyFrom(sysCred);

         // fill out the table
         table.setTable(tableName);
         table.setInfoFromLocator(locator);
      }
      catch (IllegalArgumentException e)
      {
         // this should have already been validated, so we should never get this
         throw new RuntimeException("Invalid system table credential");
      }


   }

   /**
    * Get the section link list.
    *
    * @return the section link list, never <code>null</code>, might
    *    be empty. An iterator of PSUrlRequest objects.
    */
   public Iterator getSectionLinkList()
   {
      return m_sectionLinkList.iterator();
   }

   /**
    * Loads the PSUrlRequest objects from the supplied element and stores them.
    *
    * @param sectionLinkList The element containing the PSUrlRequest xml
    * elements.  May not be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the sectionLinkList node does not
    * contain nodes of the appropriate type.
    */
   private void loadSectionLinkList(Element sectionLinkList)
      throws PSUnknownNodeTypeException
   {

      if (sectionLinkList == null)
         throw new IllegalArgumentException("sectionLinkList may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sectionLinkList);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;


      PSUrlRequest urlRequest = null;
      Element node = tree.getNextElement(
         PSUrlRequest.XML_NODE_NAME, firstFlags);
      while(node != null)
      {
         urlRequest = new PSUrlRequest(
            node, null, null);
         m_sectionLinkList.add(urlRequest);

         node = tree.getNextElement(
            PSUrlRequest.XML_NODE_NAME, nextFlags);
      }

   }

   /**
    * Loads the input and result data extension call sets from the supplied
    * element and stores them by command name.
    * @param exits The element containing the extension call sets
    * @throws PSUnknownNodeTypeException if the exits node is not of the
    * appropriate type.
    */
   @SuppressWarnings("unchecked")
   private void loadExits(Element exits) throws PSUnknownNodeTypeException
   {
      if (exits == null)
         throw new IllegalArgumentException("exits may not be null");

      // get the command name
      String cmdName = exits.getAttribute(ATTR_COMMAND_NAME);
      if (cmdName == null || cmdName.length() == 0)
      {
         Object[] args = {exits.getNodeName(), ATTR_COMMAND_NAME,
            (cmdName == null ? "null" : cmdName)};

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(exits);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

      // get the input data exits
      Element inputDataExits = tree.getNextElement(ELEMENT_INPUT_DATA_EXITS,
         firstFlags);

      if (inputDataExits == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ELEMENT_INPUT_DATA_EXITS);

      // within this element must be the extension call set
      Element inputCallSetEl = tree.getNextElement(firstFlags);
      PSExtensionCallSet inputCallSet = new PSExtensionCallSet(inputCallSetEl,
         null, null);

      // get the result data exits
      tree.setCurrent(exits);
      Element resultDataExits = tree.getNextElement(ELEMENT_RESULT_DATA_EXITS,
         firstFlags);

      if (resultDataExits == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ELEMENT_RESULT_DATA_EXITS);

      // within this element must be the extension call set
      Element resultCallSetEl = tree.getNextElement(firstFlags);
      PSExtensionCallSet resultCallSet = new PSExtensionCallSet(resultCallSetEl,
         null, null);

      m_inputDataExits.put(cmdName, inputCallSet);
      m_resultDataExits.put(cmdName, resultCallSet);
   }

   /**
    * Loads the system param names from the supplied elemement.
    *
    * @param paramNames The XML element containing the param names.
    *
    * @throws PSUnknownNodeTypeException if the paramNames node is not of the
    * appropriate type.
    */
   @SuppressWarnings("unchecked")
   private void loadParamNames(Element paramNames)
      throws PSUnknownNodeTypeException
   {
      if (paramNames == null)
         throw new IllegalArgumentException("paramNames may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(paramNames);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

      // Should find at least one
      String searchEl = PSParam.XML_NODE_NAME;
      Element paramEl = tree.getNextElement(searchEl, firstFlags);
      if (paramEl == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, searchEl);
      while (paramEl != null)
      {
         // create the param object
         PSParam param = new PSParam(paramEl, null, null);

         // be sure it doesn't match a key already in the map
         String curVal = (String)m_paramNames.get(param.getName());
         if (curVal != null)
         {
            Object[] args = {ELEMENT_SYSTEM_PARAM_NAMES, searchEl,
               param.getName()};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // store in map
         m_paramNames.put(param.getName(), param.getValue());

         // get the next one
         paramEl = tree.getNextElement(searchEl, nextFlags);
      }
   }

   /**
    * Loads a set of InitParams from the specified element.  Contains one or
    * more command names followed by one or more name-value pairs.
    *
    * @param initParams The XML element containing the InitParams.  May not be
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the initParam node is not of the
    * appropriate type.
    */
   @SuppressWarnings({"unchecked"})
   private void loadInitParams(Element initParams) throws
      PSUnknownNodeTypeException
   {
      if (initParams == null)
         throw new IllegalArgumentException("initParams may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(initParams);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

      // Should find at least one command name - build a list
      ArrayList cmdNames = new ArrayList();
      String searchEl = ELEMENT_COMMAND_NAME;
      Element cmdNameEl = tree.getNextElement(searchEl, firstFlags);
      if (cmdNameEl == null)
      {
         Object[] args = {ELEMENT_INIT_PARAMS, ELEMENT_COMMAND_NAME, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,args);
      }

      while (cmdNameEl != null)
      {
         cmdNames.add(PSXmlTreeWalker.getElementData(cmdNameEl));
         cmdNameEl = tree.getNextElement(searchEl, nextFlags);
      }

      // now get the list of params
      ArrayList params = new ArrayList();
      searchEl = PSParam.XML_NODE_NAME;
      Element paramEl = tree.getNextElement(searchEl, nextFlags);
      if (paramEl == null)
      {
         Object[] args = {ELEMENT_INIT_PARAMS, PSParam.XML_NODE_NAME, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,args);
      }

      while (paramEl != null)
      {
         // create the param object
         PSParam param = new PSParam(paramEl, null, null);

         // add to list
         params.add(param);

         // get the next one
         paramEl = tree.getNextElement(searchEl, nextFlags);
      }

      // now build the map, appending the list of params to each cmd name entry
      Iterator names = cmdNames.iterator();
      while (names.hasNext())
      {
         // may already have some params for this cmd name
         String key = (String)names.next();
         List paramList = (List)m_initParams.get(key);
         if (paramList == null)
            paramList = new ArrayList();
         paramList.addAll(params);
         m_initParams.put(key, paramList);
      }

   }

   /**
    * Loads the container locator, fieldset, and uidef if present.  Either all
    * three or none of the three must be present.
    *
    * @param sibling The element node to start searching from looking for
    * siblings only.
    *
    * @return The last sibling element found - either the uidef element or the
    * sibling element supplied to this method.
    *
    * @throws PSUnknownNodeTypeException if only one or two of the three elements
    * are present or if any of the three nodes are present but invalid.
    */
   private Element loadFieldSet(Element sibling)
         throws PSUnknownNodeTypeException
   {
      if (sibling == null)
         throw new IllegalArgumentException("sibling may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sibling);

      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

      tree.setCurrent(sibling);

      /* search for (ContainerLocator, FieldSet, UIDef)? - if we find any of
       * these, we need to find them all
       */
      Element containerLocator = tree.getNextElement(
         PSContainerLocator.XML_NODE_NAME, nextFlags);

      Element fieldSet = tree.getNextElement(PSFieldSet.XML_NODE_NAME,
         nextFlags);

      Element uiDef = tree.getNextElement(PSUIDefinition.XML_NODE_NAME,
         nextFlags);

      // they must all be null or all not-null
      if (!((containerLocator == null) ^ (fieldSet == null)) &&
         ((fieldSet == null) ^ (uiDef == null)))
      {
         // we have at least one not null - throw error on first null
         String badEl;
         if ((containerLocator == null))
            badEl = PSContainerLocator.XML_NODE_NAME;
         else if (fieldSet == null)
            badEl = PSFieldSet.XML_NODE_NAME;
         else
            badEl = PSUIDefinition.XML_NODE_NAME;

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, badEl);
      }
      else if (containerLocator != null)
      {
         // this means we have found them all
         m_containerLocator = new PSContainerLocator(containerLocator, null,
            null);
         m_fieldSet = new PSFieldSet(fieldSet, null, null);
         m_fieldSet.setSourceType( PSField.TYPE_SYSTEM );
         m_uiDef = new PSUIDefinition(uiDef, null, null);
      }

      // return the last "sibling" node we found
      return (uiDef != null ? uiDef : sibling);
   }


   /**
    * Loads the container locator contained in the system locator element
    *
    * @param systemLocator The XML element containing the container locator.
    *
    * @throws PSUnknownNodeTypeException if the systemLocator node is not of the
    * appropriate type.
    */
   private void loadSystemLocator(Element systemLocator)
      throws PSUnknownNodeTypeException
   {
      if (systemLocator == null)
         throw new IllegalArgumentException("systemLocator may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(systemLocator);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;

      Element containerLocator = tree.getNextElement(
         PSContainerLocator.XML_NODE_NAME, firstFlags);
      if (containerLocator == null)
      {
         Object[] args = {ELEMENT_SYSTEM_LOCATOR,
            PSContainerLocator.XML_NODE_NAME, "null"};

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_systemLocator = new PSContainerLocator(containerLocator, null, null);
   }

   /**
    * Tests if the supplied field is a system field or not. If the fields
    * submit name is found in a field contained by the system def and is not in
    * the list of supplied field excludes, then this is considered as a system
    * field, otherwise not. The check is case sensitive.
    *
    * @param field the field to test, may not be <code>null</code>.
    * @param fieldExcludes list of field excludes, may not be <code>null</code>
    *
    * @return <code>true</code> if the provided field is a system field,
    *    <code>false</code> otherwise.
    */
   public boolean isSystemField(PSField field, List fieldExcludes)
   {
      if(field == null)
         throw new IllegalArgumentException("field may not be null.");

      if(fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null.");

      return getFieldSet().contains(field.getSubmitName()) &&
         !fieldExcludes.contains(field.getSubmitName());
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return PSXmlDocumentBuilder.toString(toXml());
   }

   /**
    * Constant for the alias of the system table locator in the system
    * definition.
    */
   public static final String SYSTEM_TABLE_ALIAS = "sysTable";

   /**
    * Constant for the alias of the system lookup table in the
    * system locator.
    */
   public static final String LOOKUP_TABLE_ALIAS = "RXLOOKUP";

   /**
    * Constant for the alias of the system table used to generate ids in the
    * system locator.
    */
   public static final String NEXT_ID_TABLE_ALIAS = "NEXTNUMBER";

   /**
    * Constant for the alias of the system table used to store content status
    * information
    */
   public static final String CONTENT_STATUS_TABLE_ALIAS = "CONTENTSTATUS";

   /**
    * The content editor system id used for locking through web services.
    * See {@link PSContentEditorSharedDef#SHARED_DEF_ID} to make sure its
    * unique.
    */
   public static final long SYSTEM_DEF_ID = 1000;

   /**
    * Constant for the root element name of the source Xml document for this
    * object.
    */
   public static final String XML_NODE_NAME = "ContentEditorSystemDef";

   /**
    * Constant for the cache timeout attribute
    */
   private static final String ATTR_CACHE_TIMEOUT = "cacheTimeout";

   /**
    * Constant for the element name in the source Xml document for this
    * object that contains the input and result data exit sets.
    */
   private static final String ELEMENT_COMMAND_HANDLER_EXITS =
      "CommandHandlerExits";

   /**
    * Constant for the name of the attribute used to identify which command type
    * an exit set should be used with.
    */
   private static final String ATTR_COMMAND_NAME = "commandName";

   /**
    * Constant for the exit set element name in the source Xml document for
    * this object for exits used against the input data.
    */
   private static final String ELEMENT_INPUT_DATA_EXITS = "InputDataExits";

   /**
    * Constant for the exit set element name in the source Xml document for
    * this object for exits used against the input data.
    */
   private static final String ELEMENT_RESULT_DATA_EXITS = "ResultDataExits";

   /**
    * Constant for the element name in the source Xml document for this object
    * for the command param names node.
    */
   private static final String ELEMENT_SYSTEM_PARAM_NAMES = "SystemParamNames";

   /**
    * Constant for the element name in the source Xml document for this object
    * for the system locator node.
    */
   private static final String ELEMENT_SYSTEM_LOCATOR = "SystemLocator";

   /**
    * Constant for the element name in the source Xml document for this object
    * for the init params node.
    */
   private static final String ELEMENT_INIT_PARAMS = "InitParams";


   /**
    * Constant for the name of the element used to identify which command type
    * a set of initparams should be available to.
    */
   private static final String ELEMENT_COMMAND_NAME = "CommandName";

   /**
    * Constant for the name of the element used to identify the list of section
    * links
    */
   private static final String ELEMENT_SECTION_LINK_LIST = "SectionLinkList";

   /**
    * The minimum period of time to wait (in minutes) before any document in
    * the local error cache is discarded.  Default value is <code>15</code>.
    */
   private int m_cacheTimeout = 15;

   /**
    * Map of extension calls to be run against input data, with commandName as
    * the key and the PSExtensionCallSet as the value.
    */
   private Map m_inputDataExits = new HashMap();

   /**
    * Map of extension calls to be run against result doc, with commandName as
    * the key and the PSExtensionCallSet as the value.
    */
   private Map m_resultDataExits = new HashMap();

   /**
    * Map of param names and their values.  Filled in by the constructor.
    */
   private Map m_paramNames = new HashMap();

   /**
    * Specifies the tables used by fields defined in the fieldSet.  Must be
    * <code>null</code> if {@link #m_fieldSet} is <code>null</code>.  If
    * {@link #m_fieldSet} is NOT <code>null</code>, then this must not be
    * <code>null</code>. Initialzed in the constructor if it is to be defined.
    */
   private PSContainerLocator m_containerLocator = null;

   /**
    * Specifies any system fields supplied to the user.  If not
    * <code>null</code>, then {@link #m_containerLocator} and {@link #m_uiDef}
    * must not be <code>null</code>.  If <code>null</code>, they must also be
    * <code>null</code>.  Initialzed in the constructor if it is to be defined.
    */
   private PSFieldSet m_fieldSet = null;

   /**
    * Specifies the controls used to display each field defined in the fieldSet.
    * Will be <code>null</code> if {@link #m_fieldSet} is <code>null</code>.  If
    * {@link #m_fieldSet} is NOT <code>null</code>, then this must not be
    * <code>null</code>.  Initialzed in the constructor if it is to be defined.
    */
   private PSUIDefinition m_uiDef = null;

   /**
    * Specifies the tables used by the system.  Initialzed in the constructor,
    * never <code>null</code> after that.
    */
   private PSContainerLocator m_systemLocator = null;

   /**
    * Specifies where to redirect once non-query type command processing is
    * completed.
    * Initialized in the constructor, never <code>null</code> after that.
    */
   private PSApplicationFlow m_appFlow = null;

   /**
    * Specifies the stylesheets used for converting any query type results.
    * Initialized in the constructor, never <code>null</code> after that.
    */
   private PSCommandHandlerStylesheets m_styleSheets = null;

   /**
    * Contains the input translation conditional exits.
    * Initialized in the constructor, may be <code>null</code>.
    */
   private PSInputTranslations m_inputTranslations = null;

   /**
    * Contains the output translation conditional exits.
    * Initialized in the constructor, may be <code>null</code>.
    */
   private PSOutputTranslations m_outputTranslations = null;

   /**
    * Contains the field validation conditional exits.
    * Initialized in the constructor, may be <code>null</code>.
    */
   private PSValidationRules m_groupValidations = null;

   /**
    * Map of init params used by the command handlers.  Key is the command name,
    * value is a List of PSParams.  Never <code>null</code>.
    */
   private Map m_initParams = new HashMap();

   /**
    * A collection of PSUrlRequest objects, never <code>null</code> after
    * construction.
    */
   private PSCollection m_sectionLinkList =
      new PSCollection(PSUrlRequest.class);
}

