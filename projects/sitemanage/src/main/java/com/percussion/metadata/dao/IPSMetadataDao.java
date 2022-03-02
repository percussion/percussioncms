package com.percussion.metadata.dao;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.IPSGenericDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface IPSMetadataDao {
    PSMetadata create(PSMetadata data) throws IPSGenericDao.SaveException;

    void delete(String key) throws IPSGenericDao.DeleteException, IPSGenericDao.LoadException;

    void delete(PSMetadata data) throws IPSGenericDao.DeleteException;

    PSMetadata save(PSMetadata data) throws IPSGenericDao.SaveException;

    PSMetadata find(String key) throws IPSGenericDao.LoadException;

    @SuppressWarnings("unchecked")
    @Transactional
    Collection<PSMetadata> findByPrefix(String prefix) throws IPSGenericDao.LoadException;
}
