package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.security.OsirisSecurityService;
import com.cgi.eoss.osiris.model.JobConfig;
import com.cgi.eoss.osiris.model.QJobConfig;
import com.cgi.eoss.osiris.model.QUser;
import com.cgi.eoss.osiris.persistence.dao.JobConfigDao;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Getter
@Component
public class JobConfigsApiImpl extends BaseRepositoryApiImpl<JobConfig> {

    private final OsirisSecurityService securityService;
    private final JobConfigDao dao;

    @Override
    public <S extends JobConfig> S save(S entity) {
        if (entity.getOwner() == null) {
            getSecurityService().updateOwnerWithCurrentUser(entity);
        }

        BooleanExpression equalityExpr = QJobConfig.jobConfig.owner.eq(entity.getOwner())
        .and(QJobConfig.jobConfig.service.eq(entity.getService()))
        .and(QJobConfig.jobConfig.inputs.eq(entity.getInputs()));
        if (entity.getParent() == null) {
            equalityExpr = equalityExpr.and(QJobConfig.jobConfig.parent.isNull());
        }
        else {
            equalityExpr = equalityExpr.and(QJobConfig.jobConfig.parent.id.eq(entity.getParent().getId()));
        }
        if (entity.getSystematicParameter() == null) {
            equalityExpr = equalityExpr.and(QJobConfig.jobConfig.systematicParameter.isNull());
        }
        else {
            equalityExpr = equalityExpr.and(QJobConfig.jobConfig.systematicParameter.eq(entity.getSystematicParameter()));
        }
        JobConfig existing = getDao().findOne(equalityExpr);
        if (existing != null) {
            entity.setId(existing.getId());
        }
        return getDao().save(entity);
    }

    @Override
    NumberPath<Long> getIdPath() {
        return QJobConfig.jobConfig.id;
    }

    @Override
    QUser getOwnerPath() {
        return QJobConfig.jobConfig.owner;
    }

    @Override
    Class<JobConfig> getEntityClass() {
        return JobConfig.class;
    }

}
