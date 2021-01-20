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

package com.percussion.server.actions;

import com.percussion.data.IPSDataExtractor;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSUrlRequestExtractor;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a single request to be taken as part of a PSActionSet.
 *
 * @see PSActionSet
 */
public class PSAction
{
   /**
    * Creates a newly created <code>PSAction</code> object, from
    * an XML representation described in <code>sys_StoredActions.dtd</code>.
    * The {@link #init(IPSExtensionManager) init} method must be called before
    * using the getter methods.
    *
    * @param sourceNode XML element to construct this object from, not
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   public PSAction(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException( "sourceNode cannot be null" );

      fromXml( sourceNode );
   }


   /**
    * Prepares internal data structures for runtime processing.  Creates
    * an extractor for the action URL and prepares each exit.
    *
    * @param extMgr responsible for resolving exit references, not
    * <code>null</code>.
    *
    * @throws PSException if there is a problem preparing the redirect URL for
    * extraction.
    */
   public void init(IPSExtensionManager extMgr) throws PSException
   {
      if (extMgr == null) throw new IllegalArgumentException(
         "Extension manager may not be null" );

      try
      {
         m_extractor = new PSUrlRequestExtractor( m_url );
      } catch (IllegalArgumentException e)
      {
         // cannot happen as m_url is never null
         throw new RuntimeException( "bug: url extractor threw exception" );
      }

      if (m_extensions != null)
      {
         m_extensionInstances = new ArrayList( m_extensions.size() );
         for (int i = 0; i < m_extensions.size(); i++)
         {
            PSExtensionCall call = (PSExtensionCall) m_extensions.get( i );
            PSExtensionRef ref = call.getExtensionRef();
            IPSExtension ext = extMgr.prepareExtension( ref, null );
            if (!(ext instanceof IPSResultDocumentProcessor))
            {
               // only post-exits are supported
               throw new PSActionSetException(
                  IPSServerErrors.ACTION_SET_INVALID_EXTENSION, new Object[]
                  { ref.toString(), m_name } );
            }
            m_extensionInstances.add(
               PSExtensionRunner.createRunner( call, ext ) );
         }
      }
   }


   /**
    * Initializes this object from its XML representation:
    *
    * <code><pre>
    * &lt;!ELEMENT Action (PSXParam*, PSXExtensionCallSet?)>
    * &lt;!ATTLIST Action
    *    name CDATA #REQUIRED
    * >
    * </pre></code>
    *
    * @param sourceNode XML element to construct this object from, assumed not
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   private void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      // validate the root element
      String localName = sourceNode.getNodeName();
      if (!XML_NODE_NAME.equals( localName ))
      {
         Object[] args = {XML_NODE_NAME, localName};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args );
      }

      // process the name attribute (required)
      m_name = sourceNode.getAttribute( NAME_XATTR );
      if (m_name == null || m_name.trim().length() == 0)
      {
         Object[] args = {XML_NODE_NAME, NAME_XATTR, m_name};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );
      }

      // process the ignoreError attribute (optional -- known default)
      m_ignoreError = false;
      String ignoreErrorAttrValue = sourceNode.getAttribute(IGNORE_ERROR_XATTR);
      if (ignoreErrorAttrValue != null && ignoreErrorAttrValue.equals("yes"))
         m_ignoreError = true;

      int searchChildrenFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int searchSiblingsFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      Element elem;
      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

      // process all the param children (zero or more)
      PSCollection params = new PSCollection( PSParam.class );
      elem = tree.getNextElement( searchChildrenFlags );

      // make sure children of the expected types
      if (elem != null && !elem.getNodeName().equals( PSParam.XML_NODE_NAME )
          && !elem.getNodeName().equals( PSExtensionCallSet.ms_NodeType ))
      {
         Object[] args = { PSParam.XML_NODE_NAME + " or " +
            PSExtensionCallSet.ms_NodeType, elem.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args );
      }

      while (elem != null && elem.getNodeName().equals( PSParam.XML_NODE_NAME ))
      {
         PSParam param = new PSParam( elem, null, null );
         params.add( param );
         elem = tree.getNextElement( searchSiblingsFlags );
      }

      // roll the params into a URL request
      m_url = new PSUrlRequest( m_name, null, params );

      // process the extensions (optional)
      if (elem != null && elem.getNodeName().equals(
         PSExtensionCallSet.ms_NodeType ))
         m_extensions = new PSExtensionCallSet( elem, null, null );

   }


   /**
    * Gets the name of this action, which should be unique across all actions
    * in a given action set.
    *
    * @return the name of this action, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Gets the URL for this action.  It will use the provided href as its base
    * and will resolve its replacement values using the provided execution data.
    *
    * @return The string representation of the action URL using the provided
    * href as a base and with any replacement values resolved using the
    * specified execution data.  Will be <code>null</code> if the the
    * replacement value specified by the URL request is not found.
    *
    * @throws IllegalStateException if the {@link #init(IPSExtensionManager)
    * init} method has not been called.
    */
   public String getUrl(String ceHref, PSExecutionData execData)
      throws PSDataExtractionException
   {
      if (ceHref == null || ceHref.trim().length() == 0)
         throw new IllegalArgumentException(
            "content editor href may not be null or empty" );
      if (execData == null)
         throw new IllegalArgumentException( "data cannot be null" );
      if (m_extractor == null)
         throw new IllegalStateException(
            "Must call init method before calling this method" );

      // reset the base href to use the specified content editor
      m_url.setHref( ceHref );

      Object value = m_extractor.extract( execData );
      if (value != null)
         return value.toString();
      else
         return null;
   }


   /**
    * Gets the extensions to be run after this action has successfully been
    * peformed, if any.
    *
    * @return an iterator of PSExtensionRunner objects, never <code>null</code>
    * but may be empty.
    */
   public Iterator getExtensionRunners()
   {
      if (m_extensionInstances != null)
         return m_extensionInstances.iterator();
      else
         return PSIteratorUtils.emptyIterator();
   }


   /**
    * Should an exception generated by this action stop the execution of the
    * action set?
    *
    * @return <code>true</code> if any error should be ignored, and execution
    * continue; <code>false</code> if any error should abort execution.
    */
   public boolean ignoreError()
   {
      return m_ignoreError;
   }


   /**
    * Gets the member variable holding the extension call set registered for
    * this action.  Protected access for use by unit test.
    *
    * @return the member variable or <code>null</code> if an extension call
    * set has not been assigned.
    */
   PSExtensionCallSet getExtensions()
   {
      return m_extensions;
   }


   /** Name of the root element in this class' XML representation */
   public static final String XML_NODE_NAME = "Action";

   /**
    * Name of the attribute that holds the name of the action. Package-level
    * protection so the unit test can reference.
    */
   static final String NAME_XATTR = "name";

   /**
    * Name of the attribute that determines whether errors generated by this
    * action abort the execution of the set.  Package-level protection so the
    * unit test can reference.
    */
   static final String IGNORE_ERROR_XATTR = "ignoreError";

   /**
    * Name of this action, used in the results.  Never <code>null</code> or
    * empty after construction.
    */
   private String m_name;

   /**
    * Collection of param objects added to the URL generated by this action.
    * Never <code>null</code> after ctor, but maybe empty.
    */
   private PSCollection m_params;

   /** The URL representing this action, never <code>null</code> after ctor */
   private PSUrlRequest m_url;

   /**
    * The extractor for <code>m_url</code>, used for efficient runtime
    * resolution of the replacement value.  Assigned in the <code>init</code>
    * method and, never <code>null</code> or modified after that.
    */
   private IPSDataExtractor m_extractor;

   /**
    * A group of extensions to run after the action as been successfully
    * completed.  Assigned in the <code>fromXml</code> method and prepared for
    * runtime use in the <code>init</code> method.  Optional, may be
    * <code>null</code>.
    */
   private PSExtensionCallSet m_extensions = null;

   /**
    * A list of <code>PSExtensionRunner</code> objects created from the
    * extensions in <code>m_extensions</code> during the init method.
    * Will be <code>null</code> if <code>m_extensions</code> is
    * <code>null</code>.
    */
   private List m_extensionInstances = null;

   /**
    * Determines whether an action set should stop executing if this action
    * generates an exception.  If <code>true</code>, the error is ignored, and
    * the action set continues.  If <code>false</code> (the default), the error
    * stops the set.  Assigned in the <code>fromXml</code> method.
    */
   private boolean m_ignoreError = false;
}
