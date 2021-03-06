package com.cgi.eoss.osiris.api.controllers;

import com.cgi.eoss.osiris.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

interface BaseRepositoryApi<T> {
    Page<T> findAll(Pageable pageable);
    <S extends T> S save(S entity);
    Page<T> findByOwner(User user, Pageable pageable);
    Page<T> findByNotOwner(User user, Pageable pageable);
}
