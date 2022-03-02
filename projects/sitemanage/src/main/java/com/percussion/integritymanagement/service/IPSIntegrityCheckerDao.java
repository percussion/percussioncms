package com.percussion.integritymanagement.service;

import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.share.dao.IPSGenericDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IPSIntegrityCheckerDao {
    @Transactional
    PSIntegrityStatus find(String token);

    @Transactional
    List<PSIntegrityStatus> find(PSIntegrityStatus.Status status);

    @Transactional
    void delete(PSIntegrityStatus intStatus);

    @Transactional
    void save(PSIntegrityStatus status) throws IPSGenericDao.SaveException;
}
