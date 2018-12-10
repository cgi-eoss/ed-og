package com.cgi.eoss.osiris.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cgi.eoss.osiris.model.User;
import com.cgi.eoss.osiris.model.UserPreference;

public interface UserPreferencesApiCustom {

    Page<UserPreference> findByType(String type, Pageable pageable);

    Page<UserPreference> findByName(String name, Pageable pageable);
    
    Page<UserPreference> search(User user, String name, String type, Pageable pageable);

}
