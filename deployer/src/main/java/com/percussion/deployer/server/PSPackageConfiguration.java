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

package com.percussion.deployer.server;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.server.dependencies.PSCustomDependencyHandler;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The configuration of the package tool. It contains all supported dependency
 * mapping and the definition of the deployment order sequence.
 */
public class PSPackageConfiguration
{
   /**
    * Construct the configuration from its XML representation.
    * 
    * @param sourceNode The element containing the XML definition. May not be
    * <code>null</code>. Format is:
    * 
    * <pre><code>
    * &lt;ELEMENT PSXPackageConfiguration (PSXDependencyMap, PkgDeployOrder) &gt;
    * &lt;ELEMENT PSXDependencyMap (PSXDependencyDef*) &gt;
    * &lt;ELEMENT PkgDeployOrder (PkgElement*) &gt;
    * &lt;ELEMENT PkgElement (EMPTY) &gt;
    *    &lt;!ATTLIST PkgElement
    *       objectType  CDATA #REQUIRED
    *       parentType  CDATA
    *    &gt;
    * &lt;ELEMENT UninstallIgnoreTypes (Object*) &gt;
    * &lt;ELEMENT Object (EMPTY) &gt;
    *    &lt;!ATTLIST Object
    *       type  CDATA #REQUIRED
    *    &gt;
    * </code></pre>
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is
    * malformed.
    * @throws PSDeployException if there are any other errors.
    */
   public PSPackageConfiguration(Element sourceNode)
      throws PSUnknownNodeTypeException, PSDeployException
   {
      this(sourceNode, true);
   }

   /**
    * Package private ctor to allow choice in building dependency maps for unit
    * testing when <code>PSDependencyHandler</code> classes are not available.
    * See {@link #PSDependencyMap(Element)} for information on params and
    * exceptions not noted below.
    * 
    * @param buildDepMaps If <code>true</code>, handler, parent and child
    * maps will be built (requires <code>PSDependencyHandler</code> to be
    * implemented for each <code>PSDependencyDef</code> to be defined,
    * otherwise only loads the defs. If <code>false</code>, then any calls
    * made to <code>getDependencyHandler()</code>,
    * <code>getChildDependencyTypes()</code> or
    * <code>getParentDependencyTypes()</code> will throw
    * <code>IllegalArgumentException</code>s.
    */
   PSPackageConfiguration(Element sourceNode, boolean buildDepMaps)
      throws PSUnknownNodeTypeException, PSDeployException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      PSXMLDomUtil.checkNode(sourceNode, XML_NODE_NAME);

      Element depMapEl = PSXMLDomUtil.getFirstElementChild(sourceNode);
      m_depMap = new PSDependencyMap(depMapEl, buildDepMaps);
      Element orderEl = PSXMLDomUtil.getNextElementSibling(depMapEl);
      parseDeployOrder(orderEl);
      Element ignoreEl = PSXMLDomUtil.getNextElementSibling(orderEl);
      parseUninstallIgnoreTypes(ignoreEl);
   }

   /**
    * Gets the dependency map of the configuration.
    * 
    * @return the dependency map, never <code>null</code> or empty for proper
    * configuration.
    */
   PSDependencyMap getDependencyMap()
   {
      return m_depMap;
   }

   /**
    * Gets the deployment order sequence.
    * 
    * @return the deployment order as a list of element types, never
    * <code>null</code> or empty for proper configuration.
    */
   List<String> getDeployOrder()
   {
      return m_deployOrder;
   }

   /**
    * Gets the list of guid types that can be ignored for uninstall.
    * 
    * @return never <code>null</code> or empty for proper configuration.
    */
   List<String> getUninstallIgnoreTypes()
   {
      return m_uninstallIgnoreTypes;
   }

   /**
    * This is used to validate the ordered element definition. The definition
    * must contain all deployable elements. If the element is not deployable,
    * then its parent must be deployable.
    */
   class ValidateOrderedElement
   {
      Set<String> mi_deployableEls = new HashSet<String>();

      Map<String, Set<String>> mi_nonDepEls = new HashMap<String, Set<String>>();

      /**
       * Adds the given type for validation.
       * 
       * @param types the type in question. If it is has only one element,
       * assumed it is deployable type; otherwise (it has 2 elements) the 2nd
       * type is the parent and deplyable element.
       */
      void addElementType(String[] types)
      {
         if (types[1] == null)
            mi_deployableEls.add(types[0]);
         else
            mi_deployableEls.add(types[1]);

         if (types[1] != null)
         {
            Set<String> cList = mi_nonDepEls.get(types[1]);
            if (cList == null)
               cList = new HashSet<String>();
            cList.add(types[0]);
            mi_nonDepEls.put(types[1], cList);
         }
      }

      /**
       * Gets the deplyable types from the dependency map.
       * 
       * @return the collection of the types, never <code>null</code> or
       * empty.
       */
      Set<String> getDeplyableDefTypes()
      {
         Set<String> result = new HashSet<String>();
         Iterator<PSDependencyDef> defs = m_depMap.getDefs();
         while (defs.hasNext())
         {
            PSDependencyDef def = defs.next();
            if (def.isDeployableElement())
               result.add(def.getObjectType());
         }
         return result;
      }

      /**
       * Validates the order definition.
       * 
       * @throws PSDeployException if the definition is invalid.
       */
      void validate() throws PSDeployException
      {
         // make sure all deployable elements are included.
         Set<String> deployableTyeps = getDeplyableDefTypes();
         if (mi_deployableEls.size() != deployableTyeps.size())
         {
            throw new PSDeployException(
                  IPSDeploymentErrors.INCOMPLATE_ORDER_DEF);
         }

         // make sure all child of "Custom" element are included.
         if (mi_nonDepEls.keySet().size() != 1)
         {
            throw new PSDeployException(
                  IPSDeploymentErrors.INVALID_NUM_PARENT_DEFS);
         }

         String parentType = mi_nonDepEls.keySet().iterator().next();
         if (!parentType.equals(PSCustomDependencyHandler.DEPENDENCY_TYPE))
         {
            throw new PSDeployException(
                  IPSDeploymentErrors.UNEXPECTED_PARENT_TYPE,
                  new String[] { parentType,
                        PSCustomDependencyHandler.DEPENDENCY_TYPE });
         }

         Set<String> cTypes = mi_nonDepEls.get(parentType);
         if (cTypes.size() != PSCustomDependencyHandler.getChildTypeList()
               .size())
         {
            throw new PSDeployException(
                  IPSDeploymentErrors.INVALID_NUM_CHILD_DEFS,
                  PSCustomDependencyHandler.DEPENDENCY_TYPE);
         }
      }
   }

   /**
    * Parse the definition of packaging order sequence.
    * 
    * @param orderElem
    * @throws PSUnknownNodeTypeException if XML is malformed.
    * @throws PSDeployException if any other error occurs.
    */
   private void parseDeployOrder(Element orderElem)
      throws PSUnknownNodeTypeException, PSDeployException
   {
      m_deployOrder = new ArrayList<String>();

      // collects the elements for validation later
      ValidateOrderedElement vOrderedElements = new ValidateOrderedElement();

      PSXMLDomUtil.checkNode(orderElem, XML_PKG_ORDER_NAME);
      Element elem = PSXMLDomUtil.getFirstElementChild(orderElem);
      while (elem != null)
      {
         PSXMLDomUtil.checkNode(elem, XML_ElEM_ORDER_NAME);
         String[] types = getPkgElementType(elem);
         m_deployOrder.add(types[0]);

         vOrderedElements.addElementType(types);

         elem = PSXMLDomUtil.getNextElementSibling(elem);
      }

      vOrderedElements.validate();
   }
   
   /**
    * Parse the ignore for uninstall object element.
    * 
    * @param ignoreElem
    * @throws PSUnknownNodeTypeException
    */
   private void parseUninstallIgnoreTypes(Element ignoreElem)
      throws PSUnknownNodeTypeException
   {
      m_uninstallIgnoreTypes = new ArrayList<String>();
      PSXMLDomUtil.checkNode(ignoreElem, XML_ElEM_IGNORE_FOR_UNINSTALL_NAME);
      Element elem = PSXMLDomUtil.getFirstElementChild(ignoreElem);
      while (elem != null)
      {
         PSXMLDomUtil.checkNode(elem, XML_ElEM_OBJECT_NAME);
         String type = elem.getAttribute("type");
         m_uninstallIgnoreTypes.add(type);
         elem = PSXMLDomUtil.getNextElementSibling(elem);
      }

   }
   /**
    * Gets the object type of the given element.
    * 
    * @param elem the element in question, assumed not <code>null</code>.
    * 
    * @return the both object type of the element and the parent type of it (if
    * exist), never <code>null</code> or empty. The 1st element of the array
    * is always the object type; the 2nd element of the array is the parent type
    * if exists. The 2nd element of the array may be <code>null</code> if
    * there is no parent type.
    * 
    * @throws PSDeployException if an error occurs.
    */
   private String[] getPkgElementType(Element elem) throws PSDeployException
   {
      String objType = elem.getAttribute("objectType");
      String parentType = elem.getAttribute("parentType");

      PSDependencyDef objDef = m_depMap.getDependencyDef(objType);
      if (objDef == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.CANNOT_FIND_DEP_DEF,
               objType);
      }

      if (objDef.isDeployableElement())
         return new String[] { objType, null };

      // if the Dependency Def is not deployable, then it must have a
      // deployable parent.
      if (StringUtils.isBlank(parentType))
      {
         throw new PSDeployException(
               IPSDeploymentErrors.DEP_DEF_NOT_DEPLOYABLE, objType);
      }

      PSDependencyDef parentDef = m_depMap.getDependencyDef(parentType);
      if (parentDef == null)
      {
         throw new PSDeployException(
               IPSDeploymentErrors.CANNOT_FIND_PARENT_DEP_DEF, parentType);
      }

      if (!parentDef.isDeployableElement())
      {
         throw new PSDeployException(
               IPSDeploymentErrors.PARENT_DEP_DEF_NOT_DEPLOYABLE, parentType);
      }

      return new String[] { objType, parentType };
   }

   /**
    * The dependency map defined in the configuration. Initialized by the
    * constructor, never <code>null</code> (or empty) after that.
    */
   private PSDependencyMap m_depMap;

   /**
    * The deployment order sequence defined in the configuration. Initialized by
    * the constructor, never <code>null</code> (or empty) after that.
    */
   private List<String> m_deployOrder;

   /**
    * The guid types that can be ignored for uninstall. Initialized by the
    * constructor, never <code>null</code> (or empty) after that.
    */
   private List<String> m_uninstallIgnoreTypes;

   /**
    * The XML node name of the order definition.
    */
   private static final String XML_PKG_ORDER_NAME = "PkgDeployOrder";

   /**
    * The XML node name of element node in the ignore for un install list.
    */
   private static final String XML_ElEM_OBJECT_NAME = "Object";

   /**
    * The XML node name of the ignore for uninstall list.
    */
   private static final String XML_ElEM_IGNORE_FOR_UNINSTALL_NAME = 
      "UninstallIgnoreTypes";

   /**
    * The XML node name of element node in the order definition.
    */
   private static final String XML_ElEM_ORDER_NAME = "PkgElement";


   /**
    * Constant for this object's root XML node.
    */
   public static final String XML_NODE_NAME = "PSXPackageConfiguration";

}
