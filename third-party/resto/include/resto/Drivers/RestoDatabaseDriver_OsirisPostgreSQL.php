<?php
/*
 * Copyright 2014 Jérôme Gasperi
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

require_once __DIR__.'/PostgreSQL/Functions_general.php';
require_once __DIR__.'/PostgreSQL/Functions_cart.php';
require_once __DIR__.'/PostgreSQL/Functions_collections.php';
require_once __DIR__.'/PostgreSQL/Functions_facets.php';
require_once __DIR__.'/PostgreSQL/Osiris_Functions_features.php';
require_once __DIR__.'/PostgreSQL/Osiris_Functions_filters.php';
require_once __DIR__.'/PostgreSQL/Functions_licenses.php';
require_once __DIR__.'/PostgreSQL/Functions_rights.php';
require_once __DIR__.'/PostgreSQL/Functions_users.php';
require_once __DIR__.'/PostgreSQL/Functions_history.php';

/**
 * RESTo PostgreSQL Database
 */
class RestoDatabaseDriver_OsirisPostgreSQL extends RestoDatabaseDriver_PostgreSQL {

    /*
     * Facet Util reference
     */
    public $facetUtil;

    /**
     * Constructor
     *
     * @param array $config
     * @param RestoCache $cache
     * @throws Exception
     */
    public function __construct($config, $cache) {
        parent::__construct($config, $cache);
    }

    /**
     * Get object by typename
     *
     * @param string $typeName
     * @param array $params
     * @return array
     * @throws  Exception
     */
    public function get($typeName, $params = array()) {
        switch ($typeName) {

            /*
             * Get Database Handler
             */
            case parent::HANDLER:
                return $this->getHandler($params);

            /*
             * Get cart items
             */
            case parent::CART_ITEMS:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->getCartItems($params['email']);

            /*
             * Get facet
             */
            case parent::FACET:
                $facetsFunctions = new Functions_facets($this);
                return $facetsFunctions->getFacet($params['type'], $params['value']);

            /*
             * Get collections descriptions
             */
            case parent::COLLECTIONS_DESCRIPTIONS:
                $collectionsFunctions = new Functions_collections($this);
                return $collectionsFunctions->getCollectionsDescriptions(isset($params['collectionName']) ? $params['collectionName'] : null);

            /*
             * Get collections descriptions
             */
            case parent::FEATURE_DESCRIPTION:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->getFeatureDescription($params['context'], $params['user'], $params['featureIdentifier'], isset($params['collection']) ? $params['collection'] : null, isset($params['filters']) ? $params['filters'] : array());

            /*
             * Get feature collections description
             */
            case parent::FEATURES_DESCRIPTIONS:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->search($params['context'], $params['user'], $params['collection'], $params['filters'], $params['options']);

            /*
             * Get groups list
             */
            case parent::GROUPS:
                $rightsFunctions = new Functions_rights($this);
                return $rightsFunctions->getGroups();

            /*
             * Get Keywords
             */
            case parent::KEYWORDS:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->getKeywords($params['language'], isset($params['types']) ? $params['types'] : array());

            /*
             * Get license
             */
            case parent::LICENSES:
                $licensesFunctions = new Functions_licenses($this);
                return $licensesFunctions->getLicenses(isset($params['licenseId']) ? $params['licenseId'] : null);

            /*
             * Get orders
             */
            case parent::ORDERS:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->getOrders($params['email'], isset($params['orderId']) ? $params['orderId'] : null);

            /*
             * Get rights
             */
            case parent::RIGHTS:
                $rightsFunctions = new Functions_rights($this);
                if (isset($params['user'])) {
                    return $rightsFunctions->getRightsForUser($params['user'], isset($params['targetType']) ? $params['targetType'] : null, isset($params['target']) ? $params['target'] : null);
                }
                else if (isset($params['groups'])) {
                    return $rightsFunctions->getRightsForGroups($params['groups'], isset($params['targetType']) ? $params['targetType'] : null, isset($params['target']) ? $params['target'] : null);
                }
                return null;

            /*
             * Get statistics
             */
            case parent::STATISTICS:
                $facetsFunctions = new Functions_facets($this);
                return $facetsFunctions->getStatistics($params['collectionName'], $params['facetFields']);

            /*
             * Get signatures
             */
            case parent::SIGNATURES:
                $licensesFunctions = new Functions_licenses($this);
                return $licensesFunctions->getSignatures($params['email']);

            /*
             * Get share links
             */
            case parent::SHARED_LINK:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->createSharedLink($params['email'], $params['resourceUrl']);

            /*
             * Get encrypted user password
             */
            case parent::USER_PASSWORD:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->getUserPassword($params['email']);

            /*
             * Get user profile
             */
            case parent::USER_PROFILE:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->getUserProfile(isset($params['email']) ? $params['email'] : $params['userid'], isset($params['password']) ? $params['password'] : null);

            /*
             * Get all users administrative information
             */
            case parent::USERS_PROFILES:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->getUsersProfiles($params);

            /*
             * Get all history information
             */
            case parent::HISTORY:
                $historyFunctions = new Functions_history($this);
                return $historyFunctions->getHistory($params);

            /*
             * Get Where clause from filters
             */
            case parent::WHERE_CLAUSE:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                $whereClauseArray =  $featuresFunctions->getWhereClause($params['user'], $params['model'], $params['filters']);
                die("<pre> where clause".var_export($whereClauseArray,true));
                return $whereClauseArray;

            /*
             * Get count estimate from query
             */
            case parent::COUNT_ESTIMATE:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->getCount($params['from'], $params['filters']);

            /*
             * Compute area from geometry
             */
            case parent::AREA:
                $featuresFunctions = new Functions_general($this);
                return $featuresFunctions->getArea($params['geometry']);

            default:
                return null;
        }
    }





    /**
     * Return true if object exist
     *
     * @param string $typeName
     * @param array $params
     * @return bool
     */
    public function check($typeName, $params = array()) {
        switch ($typeName) {

            /*
             * True if collection exists
             */
            case parent::COLLECTION:
                $collectionsFunctions = new Functions_collections($this);
                return $collectionsFunctions->collectionExists($params['collectionName']);

            /*
             * True if feature exists
             */
            case parent::FEATURE:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->featureExists($params['featureIdentifier'], isset($params['schema']) ? $params['schema'] : null);

            /*
             * True if user is item is in cart
             */
            case parent::CART_ITEM:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->isInCart($params['itemId']);

            /*
             * True if user is license is signed
             */
            case parent::SIGNATURE:
                $licensesFunctions = new Functions_licenses($this);
                return $licensesFunctions->isLicenseSigned($params['email'], $params['licenseId']);

            /*
             * True if schema exists
             */
            case parent::SCHEMA:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->schemaExists($params['name']);

            /*
             * Return SHARED_LINK initiator email or false if not found
             */
            case parent::SHARED_LINK:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->getSharedLinkInitiator($params['resourceUrl'], $params['token']);

            /*
             * True if table exists
             */
            case parent::TABLE:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->tableExists($params['name'], isset($params['schema']) ? $params['schema'] : 'public');

            /*
             * True if table is empty
             */
            case parent::TABLE_EMPTY:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->tableIsEmpty($params['name'], isset($params['schema']) ? $params['schema'] : 'public');

            /*
             * True if user is license is signed
             */
            case parent::TOKEN_REVOKED:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->isTokenRevoked($params['token']);

            /*
             * True if user exists
             */
            case parent::USER:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->userExists($params['email']);

            default:
                return null;
        }
    }

    /**
     * Remove object
     *
     * @param string $typeName
     * @param array $params
     * @return array
     */
    public function remove($typeName, $params = array()) {
        switch ($typeName) {

            /*
             * Remove collection
             */
            case parent::COLLECTION:
                $collectionsFunctions = new Functions_collections($this);
                return $collectionsFunctions->removeCollection($params['collection']);

            /*
             * Remove facet
             */
            case parent::FACET:
                $facetsFunctions = new Functions_facets($this);
                return $facetsFunctions->removeFacet($params['hash'], $params['collectionName']);

            /*
             * Remove feature
             */
            case parent::FEATURE:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->removeFeature($params['feature']);

            /*
             * Remove cart item
             */
            case parent::CART_ITEM:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->removeFromCart($params['email'], $params['itemId']);

            /*
             * Remove all cart items
             */
            case parent::CART_ITEMS:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->clearCart($params['email']);

            /*
             * Remove license
             */
            case parent::LICENSE:
                $licensesFunctions = new Functions_licenses($this);
                return $licensesFunctions->removeLicense($params['licenseId']);

            /*
             * Remove collection/feature rights for user
             */
            case parent::RIGHTS:
                $rightsFunctions = new Functions_rights($this);
                return $rightsFunctions->removeRights($params['ownerType'], $params['owner'], isset($params['targetType']) ? $params['targetType'] : null, isset($params['target']) ? $params['target'] : null);

            /*
             * Remove groups for user
             */
            case parent::GROUPS:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->removeUserGroups($params['userid'], $params['groups']);

            default:
                return null;
        }
    }

    /**
     * Store object
     *
     * @param string $typeName
     * @param array $params
     * @return bool
     */
    public function store($typeName, $params = array()) {
        switch ($typeName) {

            /*
             * Store cart item
             */
            case parent::CART_ITEM:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->addToCart($params['email'], $params['item']);

            /*
             * Store collection
             */
            case parent::COLLECTION:
                $collectionsFunctions = new Functions_collections($this);
                return $collectionsFunctions->storeCollection($params['collection'], $params['rights']);

            /*
             * Store facets
             */
            case parent::FACETS:
                $facetsFunctions = new Functions_facets($this);
                return $facetsFunctions->storeFacets($params['facets'], $params['collectionName']);

            /*
             * Store feature
             */
            case parent::FEATURE:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->storeFeature($params['collection'], $params['featureArray']);

            /*
             * Store license
             */
            case parent::LICENSE:
                $licensesFunctions = new Functions_licenses($this);
                return $licensesFunctions->storelicense($params['license']);

            /*
             * Store cart item
             */
            case parent::ORDER:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->placeOrder($params['email'], isset($params['items']) ? $params['items'] : null);

            /*
             * Store query
             */
            case parent::QUERY:
                $generalFunctions = new Functions_general($this);
                return $generalFunctions->storeQuery($params['email'], $params['query']);

            /*
             * Store rights
             */
            case parent::RIGHTS:
                $rightsFunctions = new Functions_rights($this);
                return $rightsFunctions->storeOrUpdateRights($params['rights'], $params['ownerType'], $params['owner'], $params['targetType'], $params['target'], isset($params['productIdentifier']) ? $params['productIdentifier'] : null);

            /*
             * Store user profile
             */
            case parent::USER_PROFILE:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->storeUserProfile($params['profile']);

            /*
             * Store groups
             */
            case parent::GROUPS:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->storeUserGroups($params['userid'], $params['groups']);

            /*
             * Store new group
             */
            case parent::GROUP:
                $usersFunctions = new Functions_rights($this);
                return $usersFunctions->storeGroup($params['groupid']);


            default:
                return null;
        }
    }

    /**
     * Update object
     *
     * @param string $typeName
     * @param array $params
     * @return bool
     */
    public function update($typeName, $params = array()) {
        switch ($typeName) {

            /*
             * Update cart item
             */
            case parent::CART_ITEM:
                $cartFunctions = new Functions_cart($this);
                return $cartFunctions->updateCart($params['email'], $params['itemId'], $params['item']);

            /*
             * Update rights
             */
            case parent::RIGHTS:
                $rightsFunctions = new Functions_rights($this);
                return $rightsFunctions->storeOrUpdateRights($params['rights'], $params['ownerType'], $params['owner'], $params['targetType'], $params['target']);

            /*
             * Update user profile
             */
            case parent::USER_PROFILE:
                $usersFunctions = new Functions_users($this);
                return $usersFunctions->updateUserProfile($params['profile']);

            /*
             * Update Keywords
             */
            case parent::KEYWORDS:
                $featuresFunctions = new Osiris_Functions_features($this, __DIR__.'/../../osiris_config.php');
                return $featuresFunctions->updateFeatureKeywords($params['feature'], $params['keywords']);

            default:
                return null;
        }
    }

    /**
     * Return PostgreSQL database handler
     *
     * @param array $options
     * @throws Exception
     */
    private function getHandler($options = array()) {

        $dbh = null;

        /*
         * Store db username
         */
        if (isset($options,$options['user'])) {
            $this->dbUsername = $options['user'];
        }

        if (isset($options,$options['dbname'])) {
            try {
                $dbInfo = array(
                    'dbname=' . $options['dbname'],
                    'user=' . $options['user'],
                    'password=' . $options['password']
                );
                /*
                 * If host is specified, then TCP/IP connection is used
                 * Otherwise socket connection is used
                 */
                if (isset($options['host'])) {
                    $dbInfo[] = 'host=' . $options['host'];
                    $dbInfo[] = 'port=' . (isset($options['port']) ? $options['port'] : '5432');
                }
                $dbh = @pg_connect(implode(' ', $dbInfo));
                if (!$dbh) {
                    throw new Exception();
                }
            } catch (Exception $e) {
                RestoLogUtil::httpError(500, 'Database connection error');
            }
        }

        return $dbh;
    }
}
