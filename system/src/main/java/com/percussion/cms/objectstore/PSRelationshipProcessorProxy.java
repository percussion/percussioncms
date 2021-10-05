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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;

import java.util.List;

/**
 * This class presents the interfaces supported by the processor classes, but
 * doesn't actually perform any work. It has a configuration file that is read
 * to find the appropriate processor g178iven a component type. That processor is
 * instantiated and is used to perform the heavy lifting. Once the processor
 * type is known, the config is searched for associated properties for the
 * supplied component and procesor. These properties are made available to the
 * real processor. The default processors support web
 * services and direct server access both locally and remotely.
 * <p>See the config schema, sys_CmsProcessorConfig.xsd for details on the
 * format of the configuration file.
 * <p>To provide an idea of how it will work, let's look at what happens when an
 * operation is performed on a remote client using web services, say creating a
 * new instance of some type.
 * <ol>
 *    <li>A new instance of the object would be created locally using the
 *       standard technique (e.g., new PSFolder("foo")).</li>
 *    <li>The object would be manipulated until it reached the desired state.
 *       </li>
 *    <li>The object would then be passed to the {@link #save(PSRelationshipSet)
 *       save} method of this class. </li>
 *    <li>The proxy would look up the real processor and pass it the
 *       properties from the config file, which would include the name of the
 *       web service to use.</li>
 *    <li>The real processor would call toXml on the component and pass this
 *       element as the document when making the web service request. This
 *       request would be received by the web services handler at the server.
 *       </li>
 *    <li>The handler would re-create the original object, passing the supplied
 *       xml to the component's fromXml method.</li>
 *    <li>This object would then be passed to the local proxy processor's save
 *       method.</li>
 *    <li>The local proxy would look up the real processor and pass the
 *       config information. By default this processor will generate a new key
 *       and call toDbXml on the component and pass the generated fragment to
 *       the resource defined in the processor's config</li>
 *    <li>After this is successful, setPersisted is called on the component.
 *       </li>
 *    <li>Success is returned to the web service caller.</li>
 *    <li>The WS processor will call setPersisted on the remote object.</li>
 * </ol>
 * When the methods of a processor are called, a component type must be supplied
 * (or it is obtained from the supplied component). In general, this should be
 * the base name of the component (but for the relationship processor methods,
 * it is an arbitrary name, usually based on the type of relationship being
 * processed). This name is used to find the 'real' processor class name and a
 * set of properties appropriate for this type of component. Most components
 * will use the generic processor, which performs an internal request to a
 * resource as described above.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSRelationshipProcessorProxy extends PSProcessorProxy
   implements IPSRelationshipProcessor
{
   /**
    * Delegates to the base class. See 
    * {@link PSProcessorProxy#PSProcessorProxy(String, Object)
    * super(location, ctx)} for more information.
    */
   public PSRelationshipProcessorProxy(String location, Object ctx)
      throws PSCmsException
   {
      super(location, ctx);
   }

   /**
    * Delegates to the base class. See 
    * {@link PSProcessorProxy#PSProcessorProxy(String, Object)
    * super(location, ctx)} for more information.
    * 
    * @param componentType name of the component the processor is used to 
    *    manipulate. This must match an entry in the configuration document. 
    *    Not <code>null</code> or empty.
    */
   public PSRelationshipProcessorProxy(String location, Object ctx,
      String componentType)
      throws PSCmsException
   {
      super(location, ctx);
      
      if (componentType == null)
         throw new IllegalArgumentException("componentType cannpt be null");
         
      componentType = componentType.trim();
      if (componentType.length() == 0)
         throw new IllegalArgumentException("componentType cannpt be empty");
         
      m_componentType = componentType;
   }

   //see interface for description
   public void add(
      String componentType,
      String relationshipType,
      List children,
      PSKey targetParent)
      throws PSCmsException
   {
      getProcessor(componentType).add(
         componentType,
         relationshipType,
         children,
         targetParent);
   }

   //see interface for description
   public void add(
      String relationshipType,
      List children,
      PSLocator targetParent)
      throws PSCmsException
   {
      getProcessor(m_componentType).add(
         relationshipType,
         children,
         targetParent);
   }

   //see interface for description
   public void move(
      String relationshipType,
      PSKey sourceParent,
      List children,
      PSKey targetParent)
      throws PSCmsException
   {
      getProcessor(m_componentType).move(
         relationshipType,
         sourceParent,
         children,
         targetParent);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#copy(
    * java.lang.String, java.util.List, com.percussion.cms.objectstore.PSKey)
    */
   public void copy(String relationshipType, List children, PSKey targetParent)
      throws PSCmsException
   {
      getProcessor(m_componentType).copy(
         relationshipType,
         children,
         targetParent);
   }

   //see interface for description
   public void delete(
      String relationshipType,
      PSKey sourceParent,
      List children)
      throws PSCmsException
   {
      getProcessor(m_componentType).delete(
         relationshipType,
         sourceParent,
         children);
   }

   //see interface for description
   public PSComponentSummary[] getChildren(String componentType, PSKey parent)
      throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      return proc.getChildren(componentType, parent);
   }

   //see interface for description
   public PSComponentSummary[] getChildren(
      String componentType,
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      return proc.getChildren(componentType, relationshipType, parent);
   } 
   
   //see interface for description
   public PSComponentSummary[] getParents(
      String componentType,
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      return proc.getParents(componentType, relationshipType, parent);
   }

   /**
    * Helper method that overrides the base class version to return a specfic
    * type of processor (relationship processor) object from the configuration.
    * 
    * @param componentType component type for the processor, not
    *    <code>null</code> or empty.
    * @return IPSRelationshipProcessor the requested relationship processor, 
    *    never <code>null</code>.
    * @throws PSCmsException if no processor was found for the supplied
    *    component type.
    */
   public IPSRelationshipProcessor getProcessor(String componentType)
      throws PSCmsException
   {
      if (componentType == null)
         throw new IllegalArgumentException("componentType cannot be null");
      
      componentType = componentType.trim();
      if (componentType.length() == 0)
         throw new IllegalArgumentException("componentType cannot be empty");

      return (IPSRelationshipProcessor) m_processorConfig.getProcessor(
         componentType);
   }

   /**
    * Get current component type for relationship processor.
    * 
    * @return the component type for the relationship processor, never 
    *    <code>null</code> or empty. The value set in the constructor or
    *    defaults to <code>RELATIONSHIP_COMPTYPE</code> is not supplied.
    */
   public String getComponentType()
   {
      return m_componentType;
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#delete(
    * com.percussion.cms.objectstore.PSKey, java.util.List)
    */
   public void delete(PSKey sourceParent, List children) throws PSCmsException
   {
      getProcessor(m_componentType).delete(null, sourceParent, children);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationships(java.lang.String, com.percussion.design.objectstore.
    * PSLocator, boolean)
    */
   public PSRelationshipSet getRelationships(
      String relationshipType,
      PSLocator locator,
      boolean owner)
      throws PSCmsException
   {
      return getProcessor(m_componentType).getRelationships(
         relationshipType,
         locator,
         owner);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#move(com.
    * percussion.design.objectstore.PSLocator, java.util.List, com.percussion.
    * design.objectstore.PSLocator)
    */
   public void move(
      String relationshipType,
      PSLocator sourceParent,
      List children,
      PSLocator targetParent)
      throws PSCmsException
   {
      getProcessor(m_componentType).move(
         relationshipType,
         sourceParent,
         children,
         targetParent);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationships(com.percussion.cms.objectstore.PSRelationshipFilter)
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter)
      throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(m_componentType);
      return proc.getRelationships(filter);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getSummaries
    * (com.percussion.cms.objectstore.PSRelationshipFilter, boolean)
    */
   public PSComponentSummaries getSummaries(PSRelationshipFilter filter, 
   boolean owner) throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(m_componentType);
      return proc.getSummaries(filter, owner); 
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#save(
    * com.percussion.design.objectstore.PSRelationshipSet)
    */
   public void save(PSRelationshipSet relationships) throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(m_componentType);
      proc.save(relationships);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#remove(com.
    * percussion.design.objectstore.PSRelationshipSet)
    */
   public void delete(PSRelationshipSet relationships) throws PSCmsException
   {
      getProcessor(m_componentType).delete(relationships);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getConfig(
    * java.lang.String)
    */
   public PSRelationshipConfig getConfig(String relationshipTypeName) 
      throws PSCmsException
   {
      return getProcessor(m_componentType).getConfig(relationshipTypeName);
   }

   /*
   * @see com.percussion.cms.objectstore.IPSRelationshipProcessor
   * #getSummaryByPath(java.lang.String, java.lang.String)
   */
   public PSComponentSummary getSummaryByPath(
      String componentType,
      String path, 
      String relationshipTypeName) throws PSCmsException 
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      return proc.getSummaryByPath(componentType, path, relationshipTypeName); 
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationshipOwnerPaths(java.lang.String, com.percussion.design.
    * objectstore.PSLocator, java.lang.String)
    */
   public String[] getRelationshipOwnerPaths(
      String componentType,
      PSLocator locator,
      String relationshipTypeName)
      throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      return proc.getRelationshipOwnerPaths(
         componentType,
         locator,
         relationshipTypeName);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * isDescendent(java.lang.String, com.percussion.design.objectstore.PSLocator, 
    * com.percussion.design.objectstore.PSLocator, java.lang.String)
    */
   public boolean isDescendent(
      String componentType,
      PSLocator parent,
      PSLocator child,
      String relationshipTypeName) throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      
      return proc.isDescendent(componentType, parent, child, 
         relationshipTypeName);
   }
   
   // see interface for description
   public PSKey[] getDescendentsLocators(
      String componentType,
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      IPSRelationshipProcessor proc = getProcessor(componentType);
      return proc.getDescendentsLocators(
         componentType, relationshipType, parent);
   }

   /**
    * The component type to locate the appropriate processor from the 
    * configuration document. Default is  
    * {@link PSProcessorProxy#RELATIONSHIP_COMPTYPE}. This can be 
    * set in one of the constructors. Note: Currently we have only one 
    * relationship processor and hence the default is the only one component 
    * type used for relationship processing until we have another one.
    */
   private String m_componentType = RELATIONSHIP_COMPTYPE;
}
