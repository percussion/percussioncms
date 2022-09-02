package com.percussion.soln.p13n.delivery.ds.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;

public class DSDeliveryHibernateDao extends HibernateDaoSupport 
    implements IDeliveryDataService{

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(DSDeliveryHibernateDao.class);

    public void resetRepository() throws DeliveryDataException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @SuppressWarnings("unchecked")
    public List<DeliveryListItem> retrieveAllListItems() throws DeliveryDataException {
        return (List<DeliveryListItem>) getHibernateTemplate().find("from DeliveryListItem");
    }

    public void saveListItems(Collection<DeliveryListItem> ruleItems)
            throws DeliveryDataException {
        HibernateTemplate t = getHibernateTemplate();
        for (DeliveryListItem d  : ruleItems) {
            try {
                DeliveryListItem old = t.get(DeliveryListItem.class, d.getContentId());
                if (old != null) {
                    log.debug("Deleting old : {}" , d.getContentId());
                    t.delete(old);
                }
                t.save(d);
            } 
            catch (DataAccessException e) {
                String message = "Database Error Saving: " + d;
                log.error("{} Error: {}",
                        message,
                        PSExceptionUtils.getMessageForLog(e));
                throw new DeliveryDataException(message,e);
            }
            catch (Exception e) {
                String message = "Unexpected Error Saving: " + d;
                log.error(message, e);
                throw new DeliveryDataException(message,e);
            }
        }
    }

    public List<DeliveryListItem> getListItems(List<Long> ids)
            throws DeliveryDataException {
        List<DeliveryListItem> items = new LinkedList<>();
        HibernateTemplate t = getHibernateTemplate();
        for(Long id : ids) {
            try { 
                DeliveryListItem item = t.get(DeliveryListItem.class, id);
                items.add(item);
            } 
            catch (DataAccessException e) {
                String message = "Database error getting list item for id: " +  id;
                log.error(message, e);
                throw new DeliveryDataException(message, e);
            }
            catch (Exception e) {
                String message = "Unexpected error getting list item for id: " +  id;
                log.error(message, e);
                throw new DeliveryDataException(message, e);
            }
        }
        return items;
    }
    
    

}
