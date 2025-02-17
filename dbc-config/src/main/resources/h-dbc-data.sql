insert into h_dict_base(dict_name,dict_desc,dict_source,key_column,val_column,app) values('import,file,type','文件类型',1,'key','value','h-dbc');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yml','yml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','yaml','yaml');
insert into h_dict_pairs(dict_name,pair_key,pair_value) values('import,file,type','properties','properties');

insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Config','配置管理','inner:/h-dbc/Config','-',1,1,'Console',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Profile','环境管理','inner:/h-dbc/dbc-ui/index.html#/profile','Config',0,2,'DashboardIcon',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Service','服务管理','inner:/h-dbc/dbc-ui/index.html#/service','Config',1,2,'guide',1735800456);
insert into h_menus(app,name,menu_desc,url,parent_key,order_index,menu_level,icon_name,created_at) values('h-dbc','Encrypt','加解密工具','inner:/h-dbc/hbq969-common/index.html#/encrypt','Config',2,2,'DictionaryIcon',1735800456);
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Config');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Profile');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Service');
insert into h_role_menus(app,role_name,menu_name) values('h-dbc','ADMIN','Encrypt');