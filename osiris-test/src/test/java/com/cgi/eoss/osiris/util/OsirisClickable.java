package com.cgi.eoss.osiris.util;

import lombok.Getter;

public enum OsirisClickable {
    PROJECT_CTRL_CREATE_NEW_PROJECT("#projects button[uib-tooltip='Create New Project']"),
    PROJECT_CTRL_EXPAND("#projects .panel-heading-container[ng-click='toggleProjectShow()']"),
    FORM_NEW_PROJECT_CREATE("#item-dialog[aria-label='Create Project dialog'] > md-dialog-actions > button");

    @Getter
    private final String selector;

    OsirisClickable(String selector) {
        this.selector = selector;
    }

}
