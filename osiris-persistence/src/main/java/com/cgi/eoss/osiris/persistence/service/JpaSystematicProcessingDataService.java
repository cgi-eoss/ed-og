package com.cgi.eoss.osiris.persistence.service;

import static com.cgi.eoss.osiris.model.QSystematicProcessing.systematicProcessing;
import com.cgi.eoss.osiris.model.SystematicProcessing;
import com.cgi.eoss.osiris.model.SystematicProcessing.Status;
import com.cgi.eoss.osiris.persistence.dao.OsirisEntityDao;
import com.cgi.eoss.osiris.persistence.dao.SystematicProcessingDao;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class JpaSystematicProcessingDataService extends AbstractJpaDataService<SystematicProcessing>
        implements SystematicProcessingDataService {

    private final SystematicProcessingDao dao;

    @Autowired
    public JpaSystematicProcessingDataService(SystematicProcessingDao systematicProcessingDao) {
        this.dao = systematicProcessingDao;
    }

    @Override
    OsirisEntityDao<SystematicProcessing> getDao() {
        return dao;
    }

    @Override
    Predicate getUniquePredicate(SystematicProcessing entity) {
    	if (entity.getId() == null)
    		return null;
        return systematicProcessing.id.eq(entity.getId());
    }

    @Override
    public List<SystematicProcessing> findByStatus(Status s) {
        return dao.findAll(systematicProcessing.status.eq(s));
    }
}
