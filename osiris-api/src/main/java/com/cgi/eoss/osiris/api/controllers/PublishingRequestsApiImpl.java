package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.security.OsirisSecurityService;
import com.cgi.eoss.osiris.model.PublishingRequest;
import com.cgi.eoss.osiris.model.QPublishingRequest;
import com.cgi.eoss.osiris.model.QUser;
import com.cgi.eoss.osiris.persistence.dao.PublishingRequestDao;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Getter
@Component
public class PublishingRequestsApiImpl extends BaseRepositoryApiImpl<PublishingRequest> implements PublishingRequestsApiCustom {

    private final OsirisSecurityService securityService;
    private final PublishingRequestDao dao;

    @Override
    NumberPath<Long> getIdPath() {
        return QPublishingRequest.publishingRequest.id;
    }

    @Override
    QUser getOwnerPath() {
        return QPublishingRequest.publishingRequest.owner;
    }

    @Override
    Class<PublishingRequest> getEntityClass() {
        return PublishingRequest.class;
    }

    @Override
    public Page<PublishingRequest> findAll(Pageable pageable) {
        if (getSecurityService().isSuperUser()) {
            return getDao().findAll(pageable);
        } else {
            return getDao().findAll(getOwnerPath().eq(getSecurityService().getCurrentUser()), pageable);
        }
    }

    @Override
    public Page<PublishingRequest> findByStatus(Collection<PublishingRequest.Status> statuses, Pageable pageable) {
        return getFilteredResults(QPublishingRequest.publishingRequest.status.in(statuses), pageable);
    }

    @Override
    Page<PublishingRequest> getFilteredResults(Predicate predicate, Pageable pageable) {
        if (getSecurityService().isSuperUser()) {
            return getDao().findAll(predicate, pageable);
        } else {
            BooleanExpression isVisible = getOwnerPath().eq(getSecurityService().getCurrentUser());
            return getDao().findAll(isVisible.and(predicate), pageable);
        }
    }
}
