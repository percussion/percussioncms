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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This node displays a collection of Delivery Types.
 */
public class PSDeliveryTypeContainerNode extends PSEditableNodeContainer 
{
   public static final String DELIVERY_TYPE_LIST = "pub-design-deliverytypes-views";
   
   /**
    * The node title
    */
   public static final String NODE_TITLE = "Delivery Types";
   
   /**
    * Constructs an instance.
    *
    */
   public PSDeliveryTypeContainerNode() 
   {
      super(NODE_TITLE, DELIVERY_TYPE_LIST);
   }

   @Override
   public List<? extends PSNodeBase> getChildren() throws PSNotFoundException {
      if (m_children == null)
      {               
         List<IPSDeliveryType> dtypes = getAllDeliveryTypes();
         
         PSDeliveryTypeNode node;
         for (IPSDeliveryType dtype : dtypes)
         {
            node = new PSDeliveryTypeNode(dtype);
            addNode(node);
         }
      }
      return super.getChildren();
   }

   /**
    * @return all Delivery Types in ascending order. Never <code>null</code>
    * may be empty.
    */
   private List<IPSDeliveryType> getAllDeliveryTypes()
   {
      IPSPublisherService psvc = getPublisherService();
      List<IPSDeliveryType> dtypes = psvc.findAllDeliveryTypes();

      Collections.sort(dtypes, new Comparator<IPSDeliveryType>() {
      public int compare(IPSDeliveryType o1, IPSDeliveryType o2)
      {
         return o1.getName().compareToIgnoreCase(o2.getName());
      }});

      return dtypes;
   }

   // see base
   @Override
   protected boolean findObjectByName(String name)
   {
      IPSPublisherService pub = getPublisherService();
      try
      {
         pub.loadDeliveryType(name);
         return true;
      }
      catch (PSNotFoundException e)
      {
         return false;
      }
   }

   @Override
   public Set<Object> getAllNames()
   {
      final Set<Object> names = new HashSet<>();
      for (final IPSDeliveryType dtype :
            getPublisherService().findAllDeliveryTypes())
      {
         names.add(dtype.getName());
      }
      return names;
   }

   /**
    * Convenience method to access publisher service.
    * @return the publisher service object. Not <code>null</code>.
    */
   private IPSPublisherService getPublisherService()
   {
      return PSPublisherServiceLocator.getPublisherService();
   }
   
   /**
    * Action to create a new Delivery Type, and add it to the tree.
    * @return the perform action for the Delivery Type node, which will 
    * navigate to the editor.
    */
   public String create() throws PSNotFoundException {
      IPSDeliveryType dtype = getPublisherService().createDeliveryType();
      dtype.setName(getUniqueName("DeliveryType", false));
      getPublisherService().saveDeliveryType(dtype);
      PSDeliveryTypeNode node = new PSDeliveryTypeNode(dtype);
      return node.editNewNode(this, node);
   }

   @Override
   public String returnToListView()
   {
      return DELIVERY_TYPE_LIST;
   }

   @Override
   public String getHelpTopic()
   {
      return "DeliveryTypeList";
   }

}
