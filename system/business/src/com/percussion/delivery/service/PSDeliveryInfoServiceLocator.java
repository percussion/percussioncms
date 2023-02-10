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
package com.percussion.delivery.service;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Delivery Info Service locator 
 * @author YuBingChen
 */
public class PSDeliveryInfoServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSDeliveryInfoService infoService = null;
   /**
    * Get the delivery service
    * @return the delivery service, never <code>null</code> in a correct
    * configuration
    */
   public static IPSDeliveryInfoService getDeliveryInfoService()
   {
      if (infoService==null)
      {
          synchronized(PSDeliveryInfoServiceLocator.class)
          {
              if (infoService==null)
              {
                  infoService=(IPSDeliveryInfoService) getBean("sys_deliveryInfoService");
              }
          }
      }
      return infoService;
   }
}
