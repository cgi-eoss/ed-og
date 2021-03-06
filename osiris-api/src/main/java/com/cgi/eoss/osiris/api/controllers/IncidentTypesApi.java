package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.model.IncidentType;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.model.projections.ShortIncidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "incidentTypes", itemResourceRel = "incidentType", collectionResourceRel = "incidentTypes", excerptProjection = ShortIncidentType.class)
public interface IncidentTypesApi extends BaseRepositoryApi<IncidentType>, IncidentTypesApiCustom, PagingAndSortingRepository<IncidentType, Long> {

    @Override
    @PostAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN') or hasPermission(returnObject, 'read')")
    IncidentType findOne(Long id);

    @Override
    @Query("select t from IncidentType t where t.owner=user")
    Page<IncidentType> findByOwner(@Param("owner") User user, Pageable pageable);

    @Override
    @PreAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN')")
    void delete(Iterable<? extends IncidentType> incidentTypes);

    @Override
    @PreAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN') or hasPermission(returnObject, 'write')")
    void delete(@P("incidentType") IncidentType incidentType);

    @Override
    @Query("select t from IncidentType t where not t.owner=user")
    Page<IncidentType> findByNotOwner(@Param("owner") User user, Pageable pageable);

    @Override
    @RestResource(path="findByFilterOnly", rel="findByFilterOnly")
    @Query("select t from IncidentType t where (t.title like %:filter% or t.description like %:filter%)")
    Page<IncidentType> findByFilterOnly(@Param("filter") String filter, Pageable pageable);

    @Override
    @RestResource(path = "findByFilterAndOwner", rel = "findByFilterAndOwner")
    @Query("select t from IncidentType t where t.owner=:owner and (t.title like %:filter% or t.description like %:filter%)")
    Page<IncidentType> findByFilterAndOwner(@Param("filter") String filter, @Param("owner") User user, Pageable pageable);

    @Override
    @RestResource(path = "findByFilterAndNotOwner", rel = "findByFilterAndNotOwner")
    @Query("select t from IncidentType t where not t.owner=:owner and (t.title like %:filter% or t.description like %:filter%)")
    Page<IncidentType> findByFilterAndNotOwner(@Param("filter") String filter, @Param("owner") User user, Pageable pageable);

}
