package com.percussion.integritymanagement.service;

import com.percussion.integritymanagement.data.IPSIntegrityStatus;
import com.percussion.share.dao.IPSGenericDao;

import java.util.List;

public interface IPSIntegrityCheckerDao {

    IPSIntegrityStatus find(String token);

    List<IPSIntegrityStatus> find(IPSIntegrityStatus.Status status);

    void delete(IPSIntegrityStatus intStatus);

    void save(IPSIntegrityStatus status) throws IPSGenericDao.SaveException;

}
