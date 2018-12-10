-- Rename all types, tables and constraints for for the new product

ALTER TYPE FTEP_COSTING_EXPRESSIONS_TYPE
RENAME TO OSIRIS_COSTING_EXPRESSIONS_TYPE;

ALTER TYPE FTEP_CREDENTIALS_TYPE
RENAME TO OSIRIS_CREDENTIALS_TYPE;

ALTER TYPE FTEP_DATA_SOURCES_POLICY
RENAME TO OSIRIS_DATA_SOURCES_POLICY;

ALTER TYPE FTEP_FILES_TYPE
RENAME TO OSIRIS_FILES_TYPE;

ALTER TYPE FTEP_JOBS_STATUS
RENAME TO OSIRIS_JOBS_STATUS;

ALTER TYPE FTEP_PUBLISHING_REQUESTS_OBJECT_TYPE
RENAME TO OSIRIS_PUBLISHING_REQUESTS_OBJECT_TYPE;

ALTER TYPE FTEP_PUBLISHING_REQUESTS_STATUS
RENAME TO OSIRIS_PUBLISHING_REQUESTS_STATUS;

ALTER TYPE FTEP_ROLES
RENAME TO OSIRIS_ROLES;

ALTER TYPE FTEP_SERVICES_LICENCE
RENAME TO OSIRIS_SERVICES_LICENCE;

ALTER TYPE FTEP_SERVICES_STATUS
RENAME TO OSIRIS_SERVICES_STATUS;

ALTER TYPE FTEP_SERVICES_TYPE
RENAME TO OSIRIS_SERVICES_TYPE;

ALTER TYPE FTEP_WALLET_TRANSACTIONS_TYPE
RENAME TO OSIRIS_WALLET_TRANSACTIONS_TYPE;

ALTER FUNCTION ftep_costing_expressions_type_cast( CHARACTER VARYING )
RENAME TO osiris_costing_expressions_type_cast;

ALTER FUNCTION ftep_credentials_type_cast( CHARACTER VARYING )
RENAME TO osiris_credentials_type_cast;

ALTER FUNCTION ftep_data_sources_policy_cast( CHARACTER VARYING )
RENAME TO osiris_data_sources_policy_cast;

ALTER FUNCTION ftep_files_type_cast( CHARACTER VARYING )
RENAME TO osiris_files_type_cast;

ALTER FUNCTION ftep_jobs_status_cast( CHARACTER VARYING )
RENAME TO osiris_jobs_status_cast;

ALTER FUNCTION ftep_publishing_requests_object_type_cast( CHARACTER VARYING )
RENAME TO osiris_publishing_requests_object_type_cast;

ALTER FUNCTION ftep_publishing_requests_status_cast( CHARACTER VARYING )
RENAME TO osiris_publishing_requests_status_cast;

ALTER FUNCTION ftep_roles_cast( CHARACTER VARYING )
RENAME TO osiris_roles_cast;

ALTER FUNCTION ftep_services_licence_cast( CHARACTER VARYING )
RENAME TO osiris_services_licence_cast;

ALTER FUNCTION ftep_services_status_cast( CHARACTER VARYING )
RENAME TO osiris_services_status_cast;

ALTER FUNCTION ftep_services_type_cast( CHARACTER VARYING )
RENAME TO osiris_services_type_cast;

ALTER FUNCTION ftep_wallet_transactions_type_cast( CHARACTER VARYING )
RENAME TO osiris_wallet_transactions_type_cast;

ALTER TABLE ftep_costing_expressions
  RENAME TO osiris_costing_expressions;

ALTER SEQUENCE ftep_costing_expressions_id_seq
RENAME TO osiris_costing_expressions_id_seq;

ALTER TABLE ftep_credentials
  RENAME TO osiris_credentials;

ALTER SEQUENCE ftep_credentials_id_seq
RENAME TO osiris_credentials_id_seq;

ALTER TABLE ftep_data_sources
  RENAME TO osiris_data_sources;

ALTER SEQUENCE ftep_data_sources_id_seq
RENAME TO osiris_data_sources_id_seq;

ALTER TABLE ftep_databasket_files
  RENAME TO osiris_databasket_files;

ALTER TABLE ftep_databaskets
  RENAME TO osiris_databaskets;

ALTER SEQUENCE ftep_databaskets_id_seq
RENAME TO osiris_databaskets_id_seq;

ALTER TABLE ftep_files
  RENAME TO osiris_files;

ALTER SEQUENCE ftep_files_id_seq
RENAME TO osiris_files_id_seq;

ALTER TABLE ftep_group_member
  RENAME TO osiris_group_member;

ALTER TABLE ftep_groups
  RENAME TO osiris_groups;

ALTER SEQUENCE ftep_groups_gid_seq
RENAME TO osiris_groups_gid_seq;

ALTER TABLE ftep_job_configs
  RENAME TO osiris_job_configs;

ALTER SEQUENCE ftep_job_configs_id_seq
RENAME TO osiris_job_configs_id_seq;

ALTER TABLE ftep_job_output_files
  RENAME TO osiris_job_output_files;

ALTER TABLE ftep_jobs
  RENAME TO osiris_jobs;

ALTER SEQUENCE ftep_jobs_id_seq
RENAME TO osiris_jobs_id_seq;

ALTER TABLE ftep_project_databaskets
  RENAME TO osiris_project_databaskets;

ALTER TABLE ftep_project_job_configs
  RENAME TO osiris_project_job_configs;

ALTER TABLE ftep_project_services
  RENAME TO osiris_project_services;

ALTER TABLE ftep_projects
  RENAME TO osiris_projects;

ALTER SEQUENCE ftep_projects_id_seq
RENAME TO osiris_projects_id_seq;

ALTER TABLE ftep_publishing_requests
  RENAME TO osiris_publishing_requests;

ALTER SEQUENCE ftep_publishing_requests_id_seq
RENAME TO osiris_publishing_requests_id_seq;

ALTER TABLE ftep_service_files
  RENAME TO osiris_service_files;

ALTER SEQUENCE ftep_service_files_id_seq
RENAME TO osiris_service_files_id_seq;

ALTER TABLE ftep_services
  RENAME TO osiris_services;

ALTER SEQUENCE ftep_services_id_seq
RENAME TO osiris_services_id_seq;

ALTER TABLE ftep_users
  RENAME TO osiris_users;

ALTER SEQUENCE ftep_users_uid_seq
RENAME TO osiris_users_uid_seq;

ALTER TABLE ftep_wallet_transactions
  RENAME TO osiris_wallet_transactions;

ALTER SEQUENCE ftep_wallet_transactions_id_seq
RENAME TO osiris_wallet_transactions_id_seq;

ALTER TABLE ftep_wallets
  RENAME TO osiris_wallets;

ALTER SEQUENCE ftep_wallets_id_seq
RENAME TO osiris_wallets_id_seq;

ALTER TABLE ftep_worker_locator_expressions
  RENAME TO osiris_worker_locator_expressions;

ALTER SEQUENCE ftep_worker_locator_expressions_id_seq
RENAME TO osiris_worker_locator_expressions_id_seq;

ALTER TABLE osiris_costing_expressions
  RENAME CONSTRAINT ftep_costing_expressions_pkey TO osiris_costing_expressions_pkey;

ALTER TABLE osiris_credentials
  RENAME CONSTRAINT ftep_credentials_pkey TO osiris_credentials_pkey;

ALTER TABLE osiris_data_sources
  RENAME CONSTRAINT ftep_data_sources_pkey TO osiris_data_sources_pkey;

ALTER TABLE osiris_databaskets
  RENAME CONSTRAINT ftep_databaskets_pkey TO osiris_databaskets_pkey;

ALTER TABLE osiris_files
  RENAME CONSTRAINT ftep_files_pkey TO osiris_files_pkey;

ALTER TABLE osiris_groups
  RENAME CONSTRAINT ftep_groups_pkey TO osiris_groups_pkey;

ALTER TABLE osiris_job_configs
  RENAME CONSTRAINT ftep_job_configs_owner_service_inputs_key TO osiris_job_configs_owner_service_inputs_key;

ALTER TABLE osiris_job_configs
  RENAME CONSTRAINT ftep_job_configs_pkey TO osiris_job_configs_pkey;

ALTER TABLE osiris_jobs
  RENAME CONSTRAINT ftep_jobs_pkey TO osiris_jobs_pkey;

ALTER TABLE osiris_projects
  RENAME CONSTRAINT ftep_projects_pkey TO osiris_projects_pkey;

ALTER TABLE osiris_publishing_requests
  RENAME CONSTRAINT ftep_publishing_requests_pkey TO osiris_publishing_requests_pkey;

ALTER TABLE osiris_service_files
  RENAME CONSTRAINT ftep_service_files_pkey TO osiris_service_files_pkey;

ALTER TABLE osiris_services
  RENAME CONSTRAINT ftep_services_pkey TO osiris_services_pkey;

ALTER TABLE osiris_users
  RENAME CONSTRAINT ftep_users_pkey TO osiris_users_pkey;

ALTER TABLE osiris_wallet_transactions
  RENAME CONSTRAINT ftep_wallet_transactions_pkey TO osiris_wallet_transactions_pkey;

ALTER TABLE osiris_wallets
  RENAME CONSTRAINT ftep_wallets_pkey TO osiris_wallets_pkey;

ALTER TABLE osiris_worker_locator_expressions
  RENAME CONSTRAINT ftep_worker_locator_expressions_pkey TO osiris_worker_locator_expressions_pkey;

ALTER INDEX ftep_costing_expressions_type_associated_id_idx
RENAME TO osiris_costing_expressions_type_associated_id_idx;

ALTER INDEX ftep_credentials_host_idx
RENAME TO osiris_credentials_host_idx;

ALTER INDEX ftep_data_sources_name_idx
RENAME TO osiris_data_sources_name_idx;

ALTER INDEX ftep_data_sources_owner_idx
RENAME TO osiris_data_sources_owner_idx;

ALTER INDEX ftep_databasket_files_basket_file_idx
RENAME TO osiris_databasket_files_basket_file_idx;

ALTER INDEX ftep_databaskets_name_idx
RENAME TO osiris_databaskets_name_idx;

ALTER INDEX ftep_databaskets_name_owner_idx
RENAME TO osiris_databaskets_name_owner_idx;

ALTER INDEX ftep_databaskets_owner_idx
RENAME TO osiris_databaskets_owner_idx;

ALTER INDEX ftep_files_owner_idx
RENAME TO osiris_files_owner_idx;

ALTER INDEX ftep_files_resto_id_idx
RENAME TO osiris_files_resto_id_idx;

ALTER INDEX ftep_files_uri_idx
RENAME TO osiris_files_uri_idx;

ALTER INDEX ftep_group_member_user_group_idx
RENAME TO osiris_group_member_user_group_idx;

ALTER INDEX ftep_groups_name_idx
RENAME TO osiris_groups_name_idx;

ALTER INDEX ftep_groups_name_owner_idx
RENAME TO osiris_groups_name_owner_idx;

ALTER INDEX ftep_groups_owner_idx
RENAME TO osiris_groups_owner_idx;

ALTER INDEX ftep_job_configs_label_idx
RENAME TO osiris_job_configs_label_idx;

ALTER INDEX ftep_job_configs_owner_idx
RENAME TO osiris_job_configs_owner_idx;

ALTER INDEX ftep_job_configs_service_idx
RENAME TO osiris_job_configs_service_idx;

ALTER INDEX ftep_job_output_files_job_file_idx
RENAME TO osiris_job_output_files_job_file_idx;

ALTER INDEX ftep_jobs_ext_id_idx
RENAME TO osiris_jobs_ext_id_idx;

ALTER INDEX ftep_jobs_job_config_idx
RENAME TO osiris_jobs_job_config_idx;

ALTER INDEX ftep_jobs_owner_idx
RENAME TO osiris_jobs_owner_idx;

ALTER INDEX ftep_project_databaskets_ids_idx
RENAME TO osiris_project_databaskets_ids_idx;

ALTER INDEX ftep_project_job_configs_ids_idx
RENAME TO osiris_project_job_configs_ids_idx;

ALTER INDEX ftep_project_services_ids_idx
RENAME TO osiris_project_services_ids_idx;

ALTER INDEX ftep_projects_name_idx
RENAME TO osiris_projects_name_idx;

ALTER INDEX ftep_projects_name_owner_idx
RENAME TO osiris_projects_name_owner_idx;

ALTER INDEX ftep_projects_owner_idx
RENAME TO osiris_projects_owner_idx;

ALTER INDEX ftep_publishing_requests_owner_idx
RENAME TO osiris_publishing_requests_owner_idx;

ALTER INDEX ftep_publishing_requests_owner_object_idx
RENAME TO osiris_publishing_requests_owner_object_idx;

ALTER INDEX ftep_service_files_filename_idx
RENAME TO osiris_service_files_filename_idx;

ALTER INDEX ftep_service_files_filename_service_idx
RENAME TO osiris_service_files_filename_service_idx;

ALTER INDEX ftep_service_files_service_idx
RENAME TO osiris_service_files_service_idx;

ALTER INDEX ftep_services_name_idx
RENAME TO osiris_services_name_idx;

ALTER INDEX ftep_services_owner_idx
RENAME TO osiris_services_owner_idx;

ALTER INDEX ftep_users_name_idx
RENAME TO osiris_users_name_idx;

ALTER INDEX ftep_wallet_transactions_wallet_idx
RENAME TO osiris_wallet_transactions_wallet_idx;

ALTER INDEX ftep_wallets_owner_idx
RENAME TO osiris_wallets_owner_idx;

ALTER INDEX ftep_worker_locator_expressions_service_idx
RENAME TO osiris_worker_locator_expressions_service_idx;

ALTER TABLE osiris_data_sources
  RENAME CONSTRAINT ftep_data_sources_owner_fkey TO osiris_data_sources_owner_fkey;

ALTER TABLE osiris_databasket_files
  RENAME CONSTRAINT ftep_databasket_files_databasket_id_fkey TO osiris_databasket_files_databasket_id_fkey;

ALTER TABLE osiris_databasket_files
  RENAME CONSTRAINT ftep_databasket_files_file_id_fkey TO osiris_databasket_files_file_id_fkey;

ALTER TABLE osiris_databaskets
  RENAME CONSTRAINT ftep_databaskets_owner_fkey TO osiris_databaskets_owner_fkey;

ALTER TABLE osiris_files
  RENAME CONSTRAINT ftep_files_datasource_fkey TO osiris_files_datasource_fkey;

ALTER TABLE osiris_files
  RENAME CONSTRAINT ftep_files_owner_fkey TO osiris_files_owner_fkey;

ALTER TABLE osiris_group_member
  RENAME CONSTRAINT ftep_group_member_group_id_fkey TO osiris_group_member_group_id_fkey;

ALTER TABLE osiris_group_member
  RENAME CONSTRAINT ftep_group_member_user_id_fkey TO osiris_group_member_user_id_fkey;

ALTER TABLE osiris_groups
  RENAME CONSTRAINT ftep_groups_owner_fkey TO osiris_groups_owner_fkey;

ALTER TABLE osiris_job_configs
  RENAME CONSTRAINT ftep_job_configs_owner_fkey TO osiris_job_configs_owner_fkey;

ALTER TABLE osiris_job_configs
  RENAME CONSTRAINT ftep_job_configs_service_fkey TO osiris_job_configs_service_fkey;

ALTER TABLE osiris_job_output_files
  RENAME CONSTRAINT ftep_job_output_files_file_id_fkey TO osiris_job_output_files_file_id_fkey;

ALTER TABLE osiris_job_output_files
  RENAME CONSTRAINT ftep_job_output_files_job_id_fkey TO osiris_job_output_files_job_id_fkey;

ALTER TABLE osiris_jobs
  RENAME CONSTRAINT ftep_jobs_job_config_fkey TO osiris_jobs_job_config_fkey;

ALTER TABLE osiris_jobs
  RENAME CONSTRAINT ftep_jobs_owner_fkey TO osiris_jobs_owner_fkey;

ALTER TABLE osiris_project_databaskets
  RENAME CONSTRAINT ftep_project_databaskets_databasket_id_fkey TO osiris_project_databaskets_databasket_id_fkey;

ALTER TABLE osiris_project_databaskets
  RENAME CONSTRAINT ftep_project_databaskets_project_id_fkey TO osiris_project_databaskets_project_id_fkey;

ALTER TABLE osiris_project_job_configs
  RENAME CONSTRAINT ftep_project_job_configs_job_config_id_fkey TO osiris_project_job_configs_job_config_id_fkey;

ALTER TABLE osiris_project_job_configs
  RENAME CONSTRAINT ftep_project_job_configs_project_id_fkey TO osiris_project_job_configs_project_id_fkey;

ALTER TABLE osiris_project_services
  RENAME CONSTRAINT ftep_project_services_project_id_fkey TO osiris_project_services_project_id_fkey;

ALTER TABLE osiris_project_services
  RENAME CONSTRAINT ftep_project_services_service_id_fkey TO osiris_project_services_service_id_fkey;

ALTER TABLE osiris_projects
  RENAME CONSTRAINT ftep_projects_owner_fkey TO osiris_projects_owner_fkey;

ALTER TABLE osiris_publishing_requests
  RENAME CONSTRAINT ftep_publishing_requests_owner_fkey TO osiris_publishing_requests_owner_fkey;

ALTER TABLE osiris_service_files
  RENAME CONSTRAINT ftep_service_files_service_fkey TO osiris_service_files_service_fkey;

ALTER TABLE osiris_services
  RENAME CONSTRAINT ftep_services_owner_fkey TO osiris_services_owner_fkey;

ALTER TABLE osiris_wallet_transactions
  RENAME CONSTRAINT ftep_wallet_transactions_wallet_fkey TO osiris_wallet_transactions_wallet_fkey;

ALTER TABLE osiris_wallets
  RENAME CONSTRAINT ftep_wallets_owner_fkey TO osiris_wallets_owner_fkey;

ALTER TABLE osiris_worker_locator_expressions
  RENAME CONSTRAINT ftep_worker_locator_expressions_service_fkey TO osiris_worker_locator_expressions_service_fkey;

-- Update default user

UPDATE osiris_users
SET name = 'osiris', mail = 'foodsecurity-tep@esa.int'
WHERE name = 'ftep';
