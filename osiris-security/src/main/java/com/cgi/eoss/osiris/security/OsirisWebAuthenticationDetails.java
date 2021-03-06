package com.cgi.eoss.osiris.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

public class OsirisWebAuthenticationDetails extends WebAuthenticationDetails {
    private String userEmail;

    /**
     * Records the remote address and will also set the session Id if a session already
     * exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public OsirisWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
