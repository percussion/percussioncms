/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is a container for a list supported features.  This class can 
 * restore its state from Xml.  This class may be queried to determine if a 
 * particular feature and version of that feature are supported.
 */
public class PSFeatureSet 
{

   /**
    * Constructor for this class.  <code>fromXml</code> must be called before this
    * object can be used.
    * 
    * @see #fromXml(Document)
    * @roseuid 39F9D704000F
    */
   public PSFeatureSet() 
   {
      //noop;
   }

   /**
    * Restores this objects state from the supplied XML document.  File location is
    * specified by the {@link #FEATURE_SET_FILE FEATURE_SET_FILE} field.
    * <p>
    * Deserializes this objects state from Xml.  Expects a PSXFeatureSet node
    * and nested within that 0-n Feature nodes.  Uses the following dtd:
    * the server.
    * &lt;!ELEMENT PSXFeatureSet (PSXFeature*) &gt;
    * &lt;!--
    * A feature has a Name and an Introduced date, whose format is YYYYMMDD.
    * --&gt;
    * &lt;!ELEMENT PSXFeature (PSXVersion+)&gt;
    * &lt;!ATTLIST PSXFeature
    * Name CDATA #REQ&gt;
    * &lt;!--
    * Each feature contains at least one version. Versions have a number
    * and an Introduced date, whose format is YYYYMMDD.  Subsequent additions
    * to the feature's functionality will add new versions.
    * --&gt;
    * &lt;!ELEMENT PSXVersion #PCDATA&gt;
    * &lt;!ATTLIST PSXVersion
    * Number CDATA #REQ
    * Introduced CDATA #REQ&gt;
    *
    * @param sourceDoc the source Xml representation of this object
    * @throws com.percussion.design.objectstore.PSUnknownDocTypeException if the
    * root PSXFeatureSet element is not found in the supplied Xml document
    * @throws com.percussion.design.objectstore.PSUnknownNodeTypeException if the
    * expected elements are not found in the PSXFeatureSet node
    * @see #FEATURE_SET_FILE
    * @roseuid 39F9DC3E01C5
    */
   public void fromXml(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      if (null == sourceDoc)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_nodeName);

      Element root = sourceDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,  ms_nodeName);

      //make sure we got the correct root node tag
      if (!ms_nodeName.equals(root.getNodeName()))
      {
         Object[] args = {  ms_nodeName, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      //Read in all of the features
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceDoc);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      if(m_featureList != null)
         m_featureList.clear();
      else
         m_featureList = new ArrayList<>();

      String curNodeType = PSFeature.ms_nodeName;
      if (tree.getNextElement(curNodeType, firstFlags) != null){
         PSFeature feature;
         do{
            feature = new PSFeature((Element)tree.getCurrent());
            m_featureList.add(feature);
         } while (tree.getNextElement(curNodeType, nextFlags) != null);
      }
   }

   /**
    * Returns a list of PSFeature objects.  Checks the ServerVersion and if earlier
    * than the first version that supports this request, then returns null.
    * fromXml must have been called previously
    *
    * @return a List of PSFeatureObjects.  May be not be <code>null</code>.
    * @throws IllegalStateException if fromXml has not yet been called.
    * @see PSFeature
    * @roseuid 39F9E9080186
    */
   public Iterator getFeatureSet()
   {
      if (m_featureList == null)
         throw new IllegalStateException("fromXml must have been called");

      return m_featureList.iterator();
   }

   /**
    * The name of the Xml node this object is serialized to and from.
    */
   public static final String ms_nodeName = "PSXFeatureSet";

   /**
    * A list of PSFeature objects, each representing a supported feature.
    */
   private ArrayList m_featureList = null;

   /**
    * Name of file containing server's supported feature list
    */
   public static final String FEATURE_SET_FILE = "featureset.xml";
}
