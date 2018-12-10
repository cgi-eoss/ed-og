package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.security.OsirisSecurityService;
import com.cgi.eoss.osiris.model.OsirisEntityWithOwner;
import com.cgi.eoss.osiris.model.QUser;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.persistence.dao.OsirisEntityDao;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public abstract class BaseRepositoryApiImpl<T extends OsirisEntityWithOwner<T>> implements BaseRepositoryApi<T> {

    @Override
    public <S extends T> S save(S entity) {
        if (entity.getOwner() == null) {
            getSecurityService().updateOwnerWithCurrentUser(entity);
        }
        return getDao().save(entity);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        if (getSecurityService().isSuperUser()) {
            return getDao().findAll(pageable);
        } else {
            Set<Long> visibleIds = getSecurityService().getVisibleObjectIds(getEntityClass(), getDao().findAllIds());
            BooleanExpression isVisible = getIdPath().in(visibleIds);
            return getDao().findAll(isVisible, pageable);
        }
    }

    @Override
    public Page<T> findByOwner(User user, Pageable pageable) {
        return getFilteredResults(getOwnerPath().eq(user), pageable);
    }

    @Override
    public Page<T> findByNotOwner(User user, Pageable pageable) {
        return getFilteredResults(getOwnerPath().ne(user), pageable);
    }

    Page<T> getFilteredResults(Predicate predicate, Pageable pageable) {
        if (getSecurityService().isSuperUser()) {
            return getDao().findAll(predicate, pageable);
        } else {
            Set<Long> visibleIds = getSecurityService().getVisibleObjectIds(getEntityClass(), getDao().findAllIds());
            BooleanExpression isVisible = getIdPath().in(visibleIds);
            return getDao().findAll(isVisible.and(predicate), pageable);
        }
    }

    abstract NumberPath<Long> getIdPath();

    abstract QUser getOwnerPath();

    abstract Class<T> getEntityClass();

    abstract OsirisSecurityService getSecurityService();

    abstract OsirisEntityDao<T> getDao();

}
