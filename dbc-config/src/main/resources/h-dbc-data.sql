insert into h_dict_base(dict_name,dict_desc,dict_source,key_column,val_column,app) values('import,file,type','文件类型',1,'key','value','h-dbc');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yml','yml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yaml','yaml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','properties','properties');

insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Config','配置管理','inner:/h-dbc/Config','-',1,1,'Console',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Profile','环境管理','inner:/h-dbc/dbc-ui/index.html#/profile','Config',0,2,'profile2',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Service','服务管理','inner:/h-dbc/dbc-ui/index.html#/service','Config',1,2,'service3',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Backup','配置恢复','inner:/h-dbc/dbc-ui/index.html#/back/data','Config',2,2,'recovery5',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Encrypt','加解密工具','inner:/h-dbc/hbq969-common/index.html#/encrypt','Config',3,2,'encrypt2',1735800456);
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Config');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Profile');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Service');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Backup');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Encrypt');

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