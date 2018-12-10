package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.model.OsirisService;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.model.projections.ShortOsirisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestResource(path = "services", itemResourceRel = "service", collectionResourceRel = "services", excerptProjection = ShortOsirisService.class)
public interface ServicesApi extends BaseRepositoryApi<OsirisService>, ServicesApiCustom, JpaRepository<OsirisService, Long> {

    @Override
    @PostAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN') or (returnObject.status == T(com.cgi.eoss.osiris.model.OsirisService$Status).AVAILABLE) or hasPermission(returnObject, 'read')")
    OsirisService findOne(Long id);

    @Override
    Page<OsirisService> findAll(Pageable pageable);

    @Override
    @PreAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN') or (#service.id == null and hasRole('EXPERT_USER')) or (#service.id != null && hasPermission(#service, 'write'))")
    <S extends OsirisService> S save(@P("service") S service);

    @Override
    @PreAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN')")
    void delete(Iterable<? extends OsirisService> services);

    @Override
    @PreAuthorize("hasAnyRole('CONTENT_AUTHORITY', 'ADMIN') or (!@osirisSecurityService.isPublic(#service.class, #service.id) and hasPermission(#service, 'administration'))")
    void delete(@P("service") OsirisService service);

    @Override
    @Query("select t from OsirisService t where t.owner=user")
    Page<OsirisService> findByOwner(@Param("owner") User user, Pageable pageable);

    @Override
    @Query("select t from OsirisService t where not t.owner=user")
    Page<OsirisService> findByNotOwner(@Param("owner") User user, Pageable pageable);

    @Override
    @RestResource(path="findByFilterOnly", rel="findByFilterOnly")
    @Query("select t from OsirisService t where (t.name like %:filter% or t.description like %:filter%) and (:serviceType is null or t.type = :serviceType)")
    Page<OsirisService> findByFilterOnly(@Param("filter") String filter, @Param("serviceType") @RequestParam(required = false) OsirisService.Type serviceType, Pageable pageable);

    @Override
    @RestResource(path = "findByFilterAndOwner", rel = "findByFilterAndOwner")
    @Query("select t from OsirisService t where t.owner=:owner and (t.name like %:filter% or t.description like %:filter%) and (:serviceType is null or t.type = :serviceType)")
    Page<OsirisService> findByFilterAndOwner(@Param("filter") String filter, @Param("owner") User user, @Param("serviceType") @RequestParam(required = false) OsirisService.Type serviceType, Pageable pageable);

    @Override
    @RestResource(path = "findByFilterAndNotOwner", rel = "findByFilterAndNotOwner")
    @Query("select t from OsirisService t where not t.owner=:owner and (t.name like %:filter% or t.description like %:filter%) and (:serviceType is null or t.type = :serviceType)")
    Page<OsirisService> findByFilterAndNotOwner(@Param("filter") String filter, @Param("owner") User user, @Param("serviceType") @RequestParam(required = false) OsirisService.Type serviceType, Pageable pageable);
}
