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
 * This class describes a feature that is supported.  Each feature has a name and 
 * a list of supported versions.  Each version has a number and an introduced 
 * date.  Each feature must have at least one version, the initial version of the 
 * feature.
 */
public class PSFeature
{
   /**
    * The name used to identify the full text search engine functionality 
    * added in Rx 5.5.
    */
   public static final String FEATURE_FTS = "FullTextSearch";

   /**
    * Returns the name of the feature
    *
    * @return the version name
    * @roseuid 39FD887A004E
    */
   public String getName()
   {
      return m_featureName;
   }

   /**
    * Returns a list of versions that have been supported by the server
    *
    * @return the list of versions.
    * @roseuid 39FD88AC00EA
    */
   public Iterator getVersionList()
   {
      return m_versionList.iterator();
   }

   /**
    * Constructor for this class.  Must be passed at least one version.
    *
    * @param name The name of the version
    * @param versionList A list of supported versions of this feature, all objects of
    * type PSVersion.  There must be at least one PSVersion object in this list, the
    * initial version.
    * @roseuid 39FD89820242
    */
   public PSFeature(String name, ArrayList versionList)
   {
      m_featureName = name;

      boolean isValid = true;
      if ((versionList == null) || (versionList.size() == 0))
         isValid = false;

      if (isValid)
      {
         Iterator i = versionList.iterator();
         while (i.hasNext())
         {
            if (!(i.next() instanceof PSVersion))
            {
               isValid = false;
               break;
            }
         }
      }

      if (!isValid)
         throw new IllegalArgumentException(
            "versionList must contain at least one valid PSVersion object");
   }

   /**
    * Constructor for this class.  Must be passed a valid PSXFeature node
    * containing at least one valid PXSVersion node.
    *
    * @param sourceNode the Xml element node from which to construct this object.
    *                   must contain at least one PSXVersion node.
    * @throws PSUnknownNodeTypeException if node is not found or invalid
    * @see PSFeatureSet#fromXml(Document)  for more information.
    */
   public PSFeature(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null){
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_nodeName);
      }

      // make sure we got the correct type node
      if (false == ms_nodeName.equals(sourceNode.getNodeName())){
         Object[] args = { ms_nodeName, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // get this feature's name attribute
      String sTemp = tree.getElementData("Name");
      if (sTemp == null)
      {
         Object[] args = { ms_nodeName, "Name", "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else m_featureName = sTemp;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // now load all the versions - there must be at least one
      m_versionList = new ArrayList();
      String curNodeType = PSVersion.ms_nodeName;
      if (tree.getNextElement(curNodeType, firstFlags) != null){
         PSVersion version;
         do{
            version = new PSVersion((Element)tree.getCurrent());
            m_versionList.add(version);
         } while (tree.getNextElement(curNodeType, nextFlags) != null);
      }
      else
      {
         // must be at least one
         Object[] args = { ms_nodeName, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }


   }

   /**
    * The name of this feature
    */
   private String m_featureName = null;

   /**
    * List of versions supported
    */
   private ArrayList m_versionList = null;

   /**
    * The name of the Xml node this object is serialized to and from.
    */
   static final String ms_nodeName = "PSXFeature";
}
