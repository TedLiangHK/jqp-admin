/*
MySQL Backup
Database: jqp
Backup Time: 2022-01-10 09:38:08
*/

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `jqp`.`dic`;
DROP TABLE IF EXISTS `jqp`.`dic_item`;
DROP TABLE IF EXISTS `jqp`.`form`;
DROP TABLE IF EXISTS `jqp`.`form_button`;
DROP TABLE IF EXISTS `jqp`.`form_field`;
DROP TABLE IF EXISTS `jqp`.`form_ref`;
DROP TABLE IF EXISTS `jqp`.`page`;
DROP TABLE IF EXISTS `jqp`.`page_button`;
DROP TABLE IF EXISTS `jqp`.`page_query_field`;
DROP TABLE IF EXISTS `jqp`.`page_result_field`;
DROP TABLE IF EXISTS `jqp`.`sys_menu`;
DROP TABLE IF EXISTS `jqp`.`test`;
CREATE TABLE `dic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dic_code` varchar(64) DEFAULT NULL COMMENT '字典编号',
  `dic_name` varchar(64) DEFAULT NULL COMMENT '字典名称1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='数据字典';
CREATE TABLE `dic_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '字典id',
  `label` varchar(64) DEFAULT NULL COMMENT '字典标签',
  `value` varchar(64) DEFAULT NULL COMMENT '字典值',
  PRIMARY KEY (`id`),
  KEY `dic_item_parent_id` (`parent_id`),
  CONSTRAINT `dic_item_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `dic` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COMMENT='字典明细';
CREATE TABLE `form` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(64) NOT NULL COMMENT '编号',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `table_name` varchar(64) DEFAULT NULL COMMENT '主表',
  `js` longtext COMMENT 'js脚本',
  `init_api` varchar(1024) DEFAULT NULL COMMENT '初始化接口',
  `api` varchar(1024) DEFAULT NULL COMMENT '保存接口',
  `size` varchar(20) DEFAULT NULL COMMENT '窗口大小',
  `field_width` int(11) DEFAULT NULL COMMENT '字段宽度',
  `disabled` varchar(20) NOT NULL COMMENT '是否只读',
  PRIMARY KEY (`id`),
  KEY `form_code` (`code`) COMMENT '表单编号索引',
  KEY `form_table_name` (`table_name`) COMMENT '表单主表索引'
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='表单';
CREATE TABLE `form_button` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `form_id` bigint(20) DEFAULT NULL COMMENT '表单id',
  `label` varchar(20) DEFAULT NULL COMMENT '按钮名称',
  `option_type` varchar(20) DEFAULT NULL COMMENT '操作类型',
  `option_value` varchar(1024) DEFAULT NULL COMMENT '操作配置',
  `level` varchar(20) DEFAULT NULL COMMENT '按钮级别',
  `confirm_text` varchar(64) DEFAULT NULL COMMENT '二次确认提示',
  `seq` int(11) DEFAULT NULL COMMENT '序号',
  `js_rule` varchar(1024) DEFAULT NULL COMMENT '按钮禁用规则',
  `close` varchar(20) DEFAULT NULL COMMENT '关闭弹出层',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='表单按钮';
CREATE TABLE `form_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `form_id` bigint(20) DEFAULT NULL COMMENT '表单id',
  `field` varchar(64) DEFAULT NULL COMMENT '字段',
  `label` varchar(64) DEFAULT NULL COMMENT '注释',
  `width` int(11) DEFAULT NULL COMMENT '宽度',
  `type` varchar(20) DEFAULT NULL COMMENT '类型',
  `format` varchar(64) DEFAULT NULL COMMENT '格式化',
  `hidden` varchar(20) DEFAULT NULL COMMENT '是否隐藏',
  `seq` int(11) DEFAULT NULL COMMENT '序号',
  `value` varchar(64) DEFAULT NULL COMMENT '默认值',
  `multi` varchar(20) DEFAULT NULL COMMENT '是否多选',
  `must` varchar(20) DEFAULT NULL COMMENT '是否必填',
  `disabled` varchar(20) NOT NULL COMMENT '是否只读',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8 COMMENT='表单字段';
CREATE TABLE `form_ref` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `form_id` bigint(20) DEFAULT NULL COMMENT '表单id',
  `ref_page_code` varchar(64) DEFAULT NULL COMMENT '关联页面编号',
  `ref_field` varchar(64) DEFAULT NULL COMMENT '关联字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='表单关联';
CREATE TABLE `page` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(64) DEFAULT NULL COMMENT '编码',
  `query_sql` longtext COMMENT '查询sql',
  `page_type` varchar(20) DEFAULT NULL COMMENT '页面类型',
  `order_by` varchar(256) DEFAULT NULL COMMENT '排序',
  `js` longtext COMMENT 'js脚本',
  `name` varchar(20) NOT NULL COMMENT '名称',
  `label_field` varchar(64) DEFAULT NULL COMMENT '名称字段',
  `value_field` varchar(64) DEFAULT NULL COMMENT '值字段',
  PRIMARY KEY (`id`),
  KEY `code` (`code`) COMMENT '编码索引'
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='页面';
CREATE TABLE `page_button` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `page_id` bigint(20) DEFAULT NULL COMMENT '页面id',
  `label` varchar(20) DEFAULT NULL COMMENT '按钮名称',
  `button_location` varchar(20) DEFAULT NULL COMMENT '按钮位置',
  `option_type` varchar(20) DEFAULT NULL COMMENT '操作类型',
  `option_value` varchar(1024) DEFAULT NULL COMMENT '操作配置',
  `level` varchar(20) DEFAULT NULL COMMENT '按钮级别',
  `confirm_text` varchar(64) DEFAULT NULL COMMENT '二次确认提示',
  `seq` int(11) DEFAULT NULL COMMENT '序号',
  `js_rule` varchar(1024) DEFAULT NULL COMMENT '按钮禁用规则',
  PRIMARY KEY (`id`),
  KEY `page_button_page_id` (`page_id`) COMMENT '页面id索引'
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8 COMMENT='页面按钮';
CREATE TABLE `page_query_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `page_id` bigint(20) DEFAULT NULL COMMENT '页面id',
  `field` varchar(64) DEFAULT NULL COMMENT '字段',
  `label` varchar(64) DEFAULT NULL COMMENT '标签',
  `value` varchar(1024) DEFAULT NULL COMMENT '默认值',
  `opt` varchar(20) DEFAULT NULL COMMENT '操作符',
  `hidden` varchar(20) DEFAULT NULL COMMENT '是否隐藏',
  `must` varchar(20) DEFAULT NULL COMMENT '是否必填',
  `type` varchar(20) DEFAULT NULL COMMENT '类型',
  `format` varchar(20) DEFAULT NULL COMMENT '格式化',
  `date_express` varchar(64) DEFAULT NULL COMMENT '日期表达式',
  `option_sql` longtext COMMENT '选项查询sql',
  `seq` bigint(20) DEFAULT NULL COMMENT '序号',
  `width` int(11) DEFAULT NULL COMMENT '宽度',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=138 DEFAULT CHARSET=utf8 COMMENT='页面查询字段';
CREATE TABLE `page_result_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `page_id` bigint(20) DEFAULT NULL COMMENT '页面id',
  `field` varchar(64) DEFAULT NULL COMMENT '字段',
  `label` varchar(64) DEFAULT NULL COMMENT '注释',
  `width` int(11) DEFAULT NULL COMMENT '宽度',
  `type` varchar(20) DEFAULT NULL COMMENT '类型',
  `format` varchar(64) DEFAULT NULL COMMENT '格式化',
  `hidden` varchar(20) DEFAULT NULL COMMENT '是否隐藏',
  `seq` int(11) NOT NULL COMMENT '序号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=347 DEFAULT CHARSET=utf8 COMMENT='页面查询结果';
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父菜单',
  `menu_code` varchar(64) DEFAULT NULL COMMENT '菜单编号',
  `menu_name` varchar(64) DEFAULT NULL COMMENT '菜单名称',
  `menu_type` varchar(20) DEFAULT NULL COMMENT '菜单类型',
  PRIMARY KEY (`id`),
  KEY `sys_menu_parent_id` (`parent_id`),
  CONSTRAINT `sys_menu_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='系统菜单';
CREATE TABLE `test` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(20) DEFAULT NULL COMMENT '名称',
  `code` varchar(64) DEFAULT NULL COMMENT '编号',
  `create_time` datetime DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2214 DEFAULT CHARSET=utf8 COMMENT='测试';
BEGIN;
LOCK TABLES `jqp`.`dic` WRITE;
DELETE FROM `jqp`.`dic`;
INSERT INTO `jqp`.`dic` (`id`,`dic_code`,`dic_name`) VALUES (1, 'gender', '性别'),(2, 'whether', '是否'),(5, 'menuType', '菜单类型');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`dic_item` WRITE;
DELETE FROM `jqp`.`dic_item`;
INSERT INTO `jqp`.`dic_item` (`id`,`parent_id`,`label`,`value`) VALUES (1, 1, '男', '1'),(3, 1, '保密', '3'),(4, 2, '是', 'YES'),(5, 2, '否', 'NO'),(10, 5, '后台菜单', '1'),(11, 5, '前台菜单', '2');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`form` WRITE;
DELETE FROM `jqp`.`form`;
INSERT INTO `jqp`.`form` (`id`,`code`,`name`,`table_name`,`js`,`init_api`,`api`,`size`,`field_width`,`disabled`) VALUES (1, 'test', '测试', 'test', '', NULL, NULL, 'lg', 6, '0'),(2, 'dicForm', '数据字典', 'dic', '', NULL, NULL, 'full', 12, 'NO'),(3, 'dicItemForm', '字典明细表单', 'dic_item', '', NULL, NULL, 'default', 12, '0'),(4, 'foreignKeyForm', '外键表单', NULL, '', NULL, 'post:/tableInfo/saveForeignKey', 'default', 12, '0'),(5, 'sysMenuForm', '菜单表单', 'sys_menu', '', NULL, NULL, 'default', 12, '0'),(6, 'dicFormView', '数据字典查看', 'dic', '', NULL, NULL, 'full', 12, 'YES');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`form_button` WRITE;
DELETE FROM `jqp`.`form_button`;
INSERT INTO `jqp`.`form_button` (`id`,`form_id`,`label`,`option_type`,`option_value`,`level`,`confirm_text`,`seq`,`js_rule`,`close`) VALUES (4, 1, '测试', 'ajax', 'post:/test', 'danger', NULL, 0, NULL, 'YES');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`form_field` WRITE;
DELETE FROM `jqp`.`form_field`;
INSERT INTO `jqp`.`form_field` (`id`,`form_id`,`field`,`label`,`width`,`type`,`format`,`hidden`,`seq`,`value`,`multi`,`must`,`disabled`) VALUES (72, 3, 'id', '主键', NULL, 'long', NULL, 'YES', 0, NULL, 'NO', 'NO', ''),(73, 3, 'parentId', '字典id', NULL, 'long', NULL, 'YES', 0, NULL, 'NO', 'NO', ''),(74, 3, 'label', '字典标签', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(75, 3, 'value', '字典值', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(76, 4, 'constraintName', '外键名称', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'YES', ''),(77, 4, 'tableName', '表名', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'YES', ''),(78, 4, 'columnName', '列名', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'YES', ''),(79, 4, 'referencedTableName', '关联表名', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'YES', ''),(80, 4, 'referencedColumnName', '关联列名', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'YES', ''),(100, 5, 'id', '主键', NULL, 'long', NULL, 'YES', 0, NULL, 'NO', 'NO', ''),(101, 5, 'parentId', '父菜单', NULL, 'selector', 'menu', 'NO', 0, NULL, 'NO', 'NO', ''),(102, 5, 'menuCode', '菜单编号', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(103, 5, 'menuName', '菜单名称', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(104, 5, 'menuType', '菜单类型', NULL, 'dic', 'menuType', 'NO', 0, NULL, 'NO', 'NO', ''),(114, 2, 'id', '主键', NULL, 'long', NULL, 'YES', 0, NULL, 'NO', 'NO', ''),(115, 2, 'dicCode', '字典编号', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(116, 2, 'dicName', '字典名称', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(117, 6, 'id', '主键', NULL, 'long', NULL, 'YES', 0, NULL, 'NO', 'NO', ''),(118, 6, 'dicCode', '字典编号', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(119, 6, 'dicName', '字典名称', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(136, 1, 'id', '主键', NULL, 'long', NULL, 'YES', 0, NULL, 'NO', 'NO', ''),(137, 1, 'name', '名称', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(138, 1, 'code', '编号', NULL, 'string', NULL, 'NO', 0, NULL, 'NO', 'NO', ''),(139, 1, 'createTime', '时间', NULL, 'date', 'yyyy-MM-dd', 'NO', 0, NULL, 'NO', 'NO', '');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`form_ref` WRITE;
DELETE FROM `jqp`.`form_ref`;
INSERT INTO `jqp`.`form_ref` (`id`,`form_id`,`ref_page_code`,`ref_field`) VALUES (4, 2, 'dic_item', 'parentId'),(5, 6, 'dic_item_read_only', 'parentId');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page` WRITE;
DELETE FROM `jqp`.`page`;
INSERT INTO `jqp`.`page` (`id`,`code`,`query_sql`,`page_type`,`order_by`,`js`,`name`,`label_field`,`value_field`) VALUES (1, 'test1', 'select * from page ', 'list', NULL, '', '测试1', NULL, NULL),(2, 'menu', 'select * from sys_menu', 'tree', 'order by menu_code asc', '', '菜单查询', 'menuName', 'id'),(3, 'dic', 'select * from dic', 'list', NULL, '', '字典', NULL, NULL),(4, 'dic_item', 'select * from dic_item', 'list', NULL, '', '字典明细', NULL, NULL),(6, 'test', 'select * from test', 'list', NULL, '', '测试', NULL, NULL),(7, 'form', 'select * from form ', 'list', NULL, '', '表单管理', NULL, NULL),(8, 'foreignKey', 'select\r\ns.CONSTRAINT_NAME,\r\ns.TABLE_NAME,\r\nt.TABLE_COMMENT,\r\ns.COLUMN_NAME,\r\ntc.COLUMN_COMMENT,\r\ns.ORDINAL_POSITION,\r\ns.REFERENCED_TABLE_NAME,\r\nr.TABLE_COMMENT REFERENCED_TABLE_COMMENT,\r\ns.REFERENCED_COLUMN_NAME,\r\nrc.COLUMN_COMMENT REFERENCED_COLUMN_COMMENT\r\nfrom INFORMATION_SCHEMA.KEY_COLUMN_USAGE s\r\nleft join information_schema.`TABLES` t on t.TABLE_NAME = s.TABLE_NAME and t.TABLE_SCHEMA = s.TABLE_SCHEMA\r\nleft join information_schema.`COLUMNS` tc on tc.TABLE_NAME = s.TABLE_NAME and tc.TABLE_SCHEMA = s.TABLE_SCHEMA and tc.COLUMN_NAME = s.COLUMN_NAME\r\nleft join information_schema.`TABLES` r on r.TABLE_NAME = s.REFERENCED_TABLE_NAME and r.TABLE_SCHEMA = s.TABLE_SCHEMA\r\nleft join information_schema.`COLUMNS` rc on rc.TABLE_NAME = s.REFERENCED_TABLE_NAME and rc.TABLE_SCHEMA = s.TABLE_SCHEMA and rc.COLUMN_NAME = s.REFERENCED_COLUMN_NAME\r\nwhere s.table_schema =\'jqp\'\r\nand s.REFERENCED_COLUMN_NAME is not null', 'list', NULL, '', '外键管理', NULL, NULL),(9, 'dic_item_read_only', 'select * from dic_item', 'list', NULL, '', '字典明细-查看', NULL, NULL);
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page_button` WRITE;
DELETE FROM `jqp`.`page_button`;
INSERT INTO `jqp`.`page_button` (`id`,`page_id`,`label`,`button_location`,`option_type`,`option_value`,`level`,`confirm_text`,`seq`,`js_rule`) VALUES (10, 6, '新建', 'top', 'form', 'test', 'default', NULL, 1, NULL),(11, 6, '编辑', 'row', 'form', 'test', 'default', NULL, 2, NULL),(12, 6, '删除', 'row', 'ajax', 'post:/admin/common/test/delete/${id}', 'danger', '确认要删除${name}?', 3, 'name==\'123\''),(32, 8, '新增', 'top', 'form', 'foreignKeyForm', 'primary', NULL, 1, NULL),(33, 8, '删除', 'row', 'ajax', '/tableInfo/dropForeignKey?tableName=${tableName}&constraintName=${constraintName}', 'danger', NULL, 2, NULL),(73, 2, '删除', 'row', 'ajax', '/admin/common/sysMenu/delete/${id}', 'danger', NULL, 1, NULL),(74, 2, '新增', 'top', 'form', 'sysMenuForm', 'primary', NULL, 2, NULL),(75, 2, '编辑', 'row', 'form', 'sysMenuForm', 'default', NULL, 3, NULL),(79, 4, '新建', 'top', 'form', 'dicItemForm', 'primary', NULL, 1, 'parentId==-1||parentId==\'\''),(80, 4, '编辑', 'row', 'form', 'dicItemForm', 'default', NULL, 2, NULL),(81, 4, '删除', 'row', 'ajax', 'post:/admin/common/dicItem/delete/${id}', 'danger', '确定删除字典明细${label}吗?', 3, NULL),(82, 3, '新建', 'top', 'form', 'dicForm', 'primary', NULL, 1, NULL),(83, 3, '编辑', 'row', 'form', 'dicForm', 'default', NULL, 2, NULL),(84, 3, '删除', 'row', 'ajax', 'post:/admin/common/dic/delete/${id}', 'danger', '确定删除字典${name}吗?', 3, NULL),(85, 3, '查看', 'row', 'form', 'dicFormView', 'primary', NULL, 4, NULL);
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page_query_field` WRITE;
DELETE FROM `jqp`.`page_query_field`;
INSERT INTO `jqp`.`page_query_field` (`id`,`page_id`,`field`,`label`,`value`,`opt`,`hidden`,`must`,`type`,`format`,`date_express`,`option_sql`,`seq`,`width`) VALUES (15, 1, 'name', '名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(41, 7, 'code', '编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(42, 7, 'name', '名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, NULL),(51, 6, 'code', '编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(52, 6, 'create_time', '日期', NULL, 'betweenAnd', 'NO', 'NO', 'date', 'yyyy-MM-dd', NULL, NULL, 2, NULL),(76, 8, 'TABLE_NAME', '主表', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(77, 8, 'TABLE_COMMENT', '主表备注', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, NULL),(78, 8, 'COLUMN_NAME', '字段', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 3, NULL),(79, 8, 'COLUMN_COMMENT', '字段备注', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 4, NULL),(80, 8, 'REFERENCED_TABLE_NAME', '关联表', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 5, NULL),(81, 8, 'REFERENCED_TABLE_COMMENT', '关联表备注', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 6, NULL),(82, 8, 'REFERENCED_COLUMN_NAME', '关联字段', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 7, NULL),(83, 8, 'REFERENCED_COLUMN_COMMENT', '关联字段备注', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 8, NULL),(124, 2, 'menu_code', '菜单编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(125, 2, 'menu_name', '菜单名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, NULL),(126, 2, 'menu_type', '菜单类型', NULL, 'eq', 'NO', 'NO', 'dic', 'menuType', NULL, NULL, 3, NULL),(130, 4, 'label', '标签', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, 6),(131, 4, 'value', '值', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, 6),(132, 4, 'parent_id', '字典id', '-1', 'eq', 'YES', 'YES', 'long', NULL, NULL, NULL, 3, NULL),(133, 9, 'label', '标签', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, 6),(134, 9, 'value', '值', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, 6),(135, 9, 'parent_id', '字典id', '-1', 'eq', 'YES', 'YES', 'long', NULL, NULL, NULL, 3, NULL),(136, 3, 'dic_code', '字典编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, 6),(137, 3, 'dic_name', '字典名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, 6);
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page_result_field` WRITE;
DELETE FROM `jqp`.`page_result_field`;
INSERT INTO `jqp`.`page_result_field` (`id`,`page_id`,`field`,`label`,`width`,`type`,`format`,`hidden`,`seq`) VALUES (127, 1, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(128, 1, 'code', '编码', NULL, 'string', NULL, 'NO', 2),(129, 1, 'name', '名称', NULL, 'string', NULL, 'NO', 3),(130, 1, 'page_type', '页面类型', NULL, 'string', NULL, 'NO', 4),(131, 1, 'query_sql', '查询sql', NULL, 'sql', NULL, 'NO', 5),(132, 1, 'order_by', '排序', NULL, 'string', NULL, 'NO', 6),(133, 1, 'js', 'js脚本', NULL, 'js', NULL, 'NO', 7),(174, 7, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(175, 7, 'code', '编号', NULL, 'string', NULL, 'NO', 2),(176, 7, 'name', '名称', NULL, 'string', NULL, 'NO', 3),(177, 7, 'table_name', '主表', NULL, 'string', NULL, 'NO', 4),(194, 6, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(195, 6, 'name', '名称', NULL, 'string', NULL, 'NO', 2),(196, 6, 'code', '编号', NULL, 'string', NULL, 'NO', 3),(197, 6, 'create_time', '时间', NULL, 'date', NULL, 'NO', 4),(251, 8, 'CONSTRAINT_NAME', '外键名称', NULL, 'string', NULL, 'NO', 1),(252, 8, 'TABLE_NAME', '主表', NULL, 'string', NULL, 'NO', 2),(253, 8, 'TABLE_COMMENT', '主表备注', NULL, 'string', NULL, 'NO', 3),(254, 8, 'COLUMN_NAME', '字段', NULL, 'string', NULL, 'NO', 4),(255, 8, 'COLUMN_COMMENT', '字段备注', NULL, 'string', NULL, 'NO', 5),(256, 8, 'ORDINAL_POSITION', '位置', NULL, 'long', NULL, 'NO', 6),(257, 8, 'REFERENCED_TABLE_NAME', '关联表', NULL, 'string', NULL, 'NO', 7),(258, 8, 'REFERENCED_TABLE_COMMENT', '关联表备注', NULL, 'string', NULL, 'NO', 8),(259, 8, 'REFERENCED_COLUMN_NAME', '关联字段', NULL, 'string', NULL, 'NO', 9),(260, 8, 'REFERENCED_COLUMN_COMMENT', '关联字段备注', NULL, 'string', NULL, 'NO', 10),(327, 2, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(328, 2, 'parent_id', '父菜单', NULL, 'long', NULL, 'YES', 2),(329, 2, 'menu_code', '菜单编号', NULL, 'string', NULL, 'NO', 3),(330, 2, 'menu_name', '菜单名称', NULL, 'string', NULL, 'NO', 4),(331, 2, 'menu_type', '菜单类型', NULL, 'dic', 'menuType', 'NO', 5),(336, 4, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(337, 4, 'parent_id', '字典id', NULL, 'long', NULL, 'YES', 2),(338, 4, 'label', '字典标签', NULL, 'string', NULL, 'NO', 3),(339, 4, 'value', '字典值', NULL, 'string', NULL, 'NO', 4),(340, 9, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(341, 9, 'parent_id', '字典id', NULL, 'long', NULL, 'YES', 2),(342, 9, 'label', '字典标签', NULL, 'string', NULL, 'NO', 3),(343, 9, 'value', '字典值', NULL, 'string', NULL, 'NO', 4),(344, 3, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(345, 3, 'dic_code', '字典编号', NULL, 'string', NULL, 'NO', 2),(346, 3, 'dic_name', '字典名称', NULL, 'string', NULL, 'NO', 3);
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`sys_menu` WRITE;
DELETE FROM `jqp`.`sys_menu`;
INSERT INTO `jqp`.`sys_menu` (`id`,`parent_id`,`menu_code`,`menu_name`,`menu_type`) VALUES (1, NULL, '01', '菜单01', '1'),(2, NULL, '02', '菜单02', NULL),(3, 1, '0101', '菜单0101', '2'),(4, 1, '0102', '菜单0102', NULL),(5, 2, '0201', '菜单0201', NULL),(6, 3, '010101', '菜单010101', '1');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`test` WRITE;
DELETE FROM `jqp`.`test`;
INSERT INTO `jqp`.`test` (`id`,`name`,`code`,`create_time`) VALUES (1, '阿萨德', '012345', '2021-12-17 00:00:00'),(2, '方法', '第三方', '2021-11-10 17:05:33'),(3, '123', 'dsfgsgd', '2021-12-08 00:00:00'),(6, '123', '123', '2021-12-24 00:00:00'),(7, '123', '123', '2021-12-15 00:00:00'),(8, '123', '123', '2021-12-23 00:00:00'),(9, '123', '12323', '2021-12-22 00:00:00'),(10, '123', '123', '2021-12-30 00:00:00'),(11, '12321', '213213', '2021-12-24 00:00:00'),(12, '123123', '123123', '2021-12-31 00:00:00'),(13, '123123', '123123', '2021-12-31 00:00:00'),(14, '阿萨德', '安抚', '2021-12-17 17:05:25'),(15, '方法', '第三方', '2021-11-10 17:05:33'),(16, '123', 'dsfgsgd', '2021-12-08 00:00:00'),(17, '123', '123', '2021-12-24 00:00:00'),(18, '123', '123', '2021-12-15 00:00:00'),(19, '123', '123', '2021-12-23 00:00:00'),(20, '123', '12323', '2021-12-22 00:00:00'),(21, '123', '123', '2021-12-30 00:00:00'),(22, '12321', '213213', '2021-12-24 00:00:00'),(23, '123123', '123123', '2021-12-31 00:00:00'),(24, '123123', '123123', '2021-12-31 00:00:00'),(25, '阿萨德', '安抚', '2021-12-17 17:05:25'),(26, '方法', '第三方', '2021-11-10 17:05:33'),(27, '123', 'dsfgsgd', '2021-12-08 00:00:00'),(28, '123', '123', '2021-12-24 00:00:00'),(29, '123', '123', '2021-12-15 00:00:00'),(30, '123', '123', '2021-12-23 00:00:00');
UNLOCK TABLES;
COMMIT;
