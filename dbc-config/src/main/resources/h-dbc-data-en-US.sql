delete from h_dict_base where dict_name='import,file,type';
insert into h_dict_base(dict_name,dict_desc,dict_source,key_column,val_column,app) values('import,file,type','file type',1,'key','value','h-dbc');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yml','yml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yaml','yaml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','properties','properties');

delete from h_menus where app='h-dbc' and name in ('Config','Profile','Service','Backup','Encrypt','Guide');
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Config','Configuration','inner:/h-dbc/Config','-',1,1,'Console',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Profile','Environment','inner:/h-dbc/dbc-ui/index.html#/profile','Config',0,2,'profile2',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Service','Service','inner:/h-dbc/dbc-ui/index.html#/service','Config',1,2,'service3',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Backup','Recovery','inner:/h-dbc/dbc-ui/index.html#/back/data','Config',2,2,'recovery5',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Encrypt','Encryption tool','inner:/h-dbc/hbq969-common/index.html#/encrypt','Config',3,2,'encrypt2',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Guide','Guide','inner:/h-dbc/dbc-ui/index.html#/guide','Config',-1,2,'guide',1735800456);
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Config');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Profile');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Service');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Backup');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Encrypt');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Guide');

delete from h_dbc_profiles where profile_name in ('default','dev','test','prod','mysql','oracle');
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('default','default environment',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('dev','development environment',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('test','test environment',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('prod','production environment',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('mysql','MySQL environment',1739763078);
insert into h_dbc_profiles(profile_name,profile_desc,created_at) values('oracle','Oracle environment',1739763078);
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','default');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','dev');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','test');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','prod');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','mysql');
insert into h_dbc_acc_profiles(app,username,profile_name) values('h-dbc','admin','oracle');

delete from h_sm_info where app='h-dbc';
insert into h_sm_info(app,info_content) values('h-dbc','{"title":"Configuration Center"}');