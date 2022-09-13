package com.percussion.soln.p13n.delivery.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryService;



public class SpringDeliveryService extends AbstractDeliveryService 
    implements IDeliveryService, ApplicationContextAware {

    private ApplicationContext applicationContext;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(SpringDeliveryService.class);
    
    
    public List<IDeliverySnippetFilter> findSnippetFilters(List<String> names) throws DeliveryException {
        List<IDeliverySnippetFilter> deliveryFilters = new ArrayList<IDeliverySnippetFilter>();
        for (String name : names) {
            IDeliverySnippetFilter deliveryFilter = snippetFilters.get(name);
            if (deliveryFilter != null) {
                log.debug("Retrieved filter: " + name + " from local filters.");
            }
            else {
                log.trace("Filter not found in local filters... checking if its a spring bean");
                try {
                    deliveryFilter = (IDeliverySnippetFilter) applicationContext.getBean(name);
                } catch (BeansException e) {
                    //Do nothing.
                    log.error("Filter: " + name + " was not found in spring config.", e);
                    throw new DeliveryException("Filter with name = " + name + " was not found.", e);
                }
                log.debug("Retrieved filter: " + name + " from spring config");
            }
            deliveryFilters.add(deliveryFilter);
        }
        return deliveryFilters;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


}
