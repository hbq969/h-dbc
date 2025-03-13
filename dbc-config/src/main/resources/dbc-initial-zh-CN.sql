delete from h_dict_base where dict_name in ('import,file,type','config,dataType');
delete from h_dict_pairs where dict_name in ('import,file,type','config,dataType');
insert into h_dict_base(dict_name,dict_desc,dict_source,key_column,val_column,app) values('import,file,type','文件类型',1,'key','value','h-dbc');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yml','yml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yaml','yaml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','properties','properties');
insert into h_dict_base(dict_name,dict_desc,dict_source,key_column,val_column,app) values('config,dataType','配置属性数据类型',1,'key','value','h-dbc');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.String','字符串类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Character','字符类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Double','双精度浮点类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Float','单精度浮点类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Long','长整数类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Integer','整数类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Short','短整数类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Byte','字节类型');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('config,dataType','java.lang.Boolean','布尔类型');



delete from h_menus where app='h-dbc' and name in ('Config','Profile','Service','Backup','Encrypt','Guide','OperLog');
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Config','配置管理','inner:/h-dbc/Config','-',1,1,'Console',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Profile','环境管理','inner:/h-dbc/dbc-ui/index.html#/profile','Config',0,2,'profile2',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Service','服务管理','inner:/h-dbc/dbc-ui/index.html#/service','Config',1,2,'service3',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Backup','配置恢复','inner:/h-dbc/dbc-ui/index.html#/back/data','Config',2,2,'recovery5',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Encrypt','加解密工具','inner:/h-dbc/hbq969-common/index.html#/encrypt','Config',3,2,'encrypt2',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Guide','引导文档','inner:/h-dbc/dbc-ui/index.html#/guide','Config',-1,2,'guide',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','OperLog','操作日志','inner:/h-dbc/hbq969-tabula/index.html#/data?report=true&source=mysql%2C${defaultDataBaseSchema}&dialect=mysql&schema=${defaultDataBaseSchema}&name=log_dbc&desc=%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83%E6%93%8D%E4%BD%9C%E6%97%A5%E5%BF%97&manage=0&fmtManage=%E5%90%A6&create=null&creator=admin&createTime=1743584250','Config',6,2,'TableManagementIcon',1735800456);

delete from h_dbc_profiles where profile_name in ('default','dev','test','prod','mysql','oracle');
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('default','缺省环境',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('dev','开发环境',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('test','测试环境',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('prod','生产环境',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('mysql','MySQL环境',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('oracle','Oracle环境',1739763078);
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','default');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','dev');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','test');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','prod');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','mysql');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','oracle');

delete from h_sm_info where app='h-dbc';
insert into h_sm_info(app,info_content) values('h-dbc','{"title":"配置中心"}');

delete from h_tabula where table_name='log_dbc';
delete from h_tabula_col where table_name='log_dbc';
INSERT INTO h_tabula (table_name, table_desc, creator, manage_enabled, create_time, update_time, source_dialect, source_schema) VALUES ('log_dbc', '配置中心操作日志', 'admin', 0, 1743584250, 0, 'mysql', '${defaultDataBaseSchema}');
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'get_paras', '请求get参数', 'page-input', NULL, 1, 1, 1, 1, 0, 0, 1, 'admin', 1743584490, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'method_desc', '接口描述', 'page-input', NULL, 1, 1, 1, 1, 0, 0, 1, 'admin', 1743584475, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'method_name', '接口方法类型', 'page-input', NULL, 1, 1, 1, 1, 0, 0, 0, 'admin', 1743584457, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'oper_name', '操作者', 'backend-session-key', 'h-sm-user', 1, 1, 0, 1, 0, 0, 1, 'admin', 1743584347, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'oper_time', '操作时间', 'backend-time-num-sec', NULL, 1, 1, 0, 1, 1, 1, 0, 'admin', 1743584376, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'post_body', '请求post参数', 'page-input', NULL, 1, 1, 1, 1, 0, 0, 1, 'admin', 1743584501, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'req_id', '请求ID', 'backend-pri-str-uuid', NULL, 1, 1, 0, 1, 0, 0, 1, 'admin', 1743584286, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'result', '请求结果', 'page-input', NULL, 1, 1, 1, 1, 0, 0, 1, 'admin', 1743584513, NULL);
INSERT INTO h_tabula_col (table_name, col_name, col_desc, col_source, col_source_define, is_query, is_list, is_add, is_edit, is_sort, sort_way, is_fuzzy, creator, create_time, update_time) VALUES ('log_dbc', 'url', '接口url', 'page-input', NULL, 1, 1, 1, 1, 0, 0, 1, 'admin', 1743584397, NULL);

