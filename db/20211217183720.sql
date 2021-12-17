/*
MySQL Backup
Database: jqp
Backup Time: 2021-12-17 18:37:20
*/

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `jqp`.`dic`;
DROP TABLE IF EXISTS `jqp`.`dic_item`;
DROP TABLE IF EXISTS `jqp`.`page`;
DROP TABLE IF EXISTS `jqp`.`page_query_field`;
DROP TABLE IF EXISTS `jqp`.`page_result_field`;
DROP TABLE IF EXISTS `jqp`.`sys_menu`;
DROP TABLE IF EXISTS `jqp`.`test`;
CREATE TABLE `dic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dic_code` varchar(64) DEFAULT NULL COMMENT '字典编号',
  `dic_name` varchar(64) DEFAULT NULL COMMENT '字典名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='数据字典';
CREATE TABLE `dic_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '字典id',
  `label` varchar(64) DEFAULT NULL COMMENT '字典标签',
  `value` varchar(64) DEFAULT NULL COMMENT '字典值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='字典明细';
CREATE TABLE `page` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(64) DEFAULT NULL COMMENT '编码',
  `query_sql` longtext COMMENT '查询sql',
  `page_type` varchar(20) DEFAULT NULL COMMENT '页面类型',
  `order_by` varchar(256) DEFAULT NULL COMMENT '排序',
  `js` longtext COMMENT 'js脚本',
  `name` varchar(20) NOT NULL COMMENT '名称',
  PRIMARY KEY (`id`),
  KEY `code` (`code`) COMMENT '编码索引'
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='页面';
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
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8 COMMENT='页面查询字段';
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
) ENGINE=InnoDB AUTO_INCREMENT=174 DEFAULT CHARSET=utf8 COMMENT='页面查询结果';
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父菜单',
  `menu_code` varchar(64) DEFAULT NULL COMMENT '菜单编号',
  `menu_name` varchar(64) DEFAULT NULL COMMENT '菜单名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='系统菜单';
CREATE TABLE `test` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(20) DEFAULT NULL COMMENT '名称',
  `code` varchar(64) DEFAULT NULL COMMENT '编号',
  `create_time` datetime DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='测试';
BEGIN;
LOCK TABLES `jqp`.`dic` WRITE;
DELETE FROM `jqp`.`dic`;
INSERT INTO `jqp`.`dic` (`id`,`dic_code`,`dic_name`) VALUES (1, 'gender', '性别'),(2, 'whether', '是否');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`dic_item` WRITE;
DELETE FROM `jqp`.`dic_item`;
INSERT INTO `jqp`.`dic_item` (`id`,`parent_id`,`label`,`value`) VALUES (1, 1, '男', '1'),(2, 1, '女', '2'),(3, 1, '保密', '3'),(4, 2, '是', 'YES'),(5, 2, '否', 'NO');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page` WRITE;
DELETE FROM `jqp`.`page`;
INSERT INTO `jqp`.`page` (`id`,`code`,`query_sql`,`page_type`,`order_by`,`js`,`name`) VALUES (1, 'test1', 'select * from page ', 'list', NULL, '', '测试1'),(2, 'test02', 'select * from sys_menu', 'tree', 'order by menu_code asc', '', '菜单查询'),(3, 'dic', 'select * from dic', 'list', NULL, '', '字典'),(4, 'dic_item', 'select * from dic_item', 'list', NULL, '', '字典明细'),(6, 'test', 'select * from test', 'list', NULL, '', '测试');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page_query_field` WRITE;
DELETE FROM `jqp`.`page_query_field`;
INSERT INTO `jqp`.`page_query_field` (`id`,`page_id`,`field`,`label`,`value`,`opt`,`hidden`,`must`,`type`,`format`,`date_express`,`option_sql`,`seq`,`width`) VALUES (15, 1, 'name', '名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(16, 2, 'menu_code', '菜单编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(17, 2, 'menu_name', '菜单名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, NULL),(34, 3, 'dic_code', '字典编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, 6),(35, 3, 'dic_name', '字典名称', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, 6),(36, 4, 'label', '标签', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, 6),(37, 4, 'value', '值', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 2, 6),(38, 4, 'parent_id', '字典id', '-1', 'eq', 'YES', 'NO', 'long', NULL, NULL, NULL, 3, NULL),(39, 6, 'code', '编号', NULL, 'like', 'NO', 'NO', 'string', NULL, NULL, NULL, 1, NULL),(40, 6, 'create_time', '日期', NULL, 'betweenAnd', 'NO', 'NO', 'date', 'yyyy-MM-dd', NULL, NULL, 2, NULL);
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`page_result_field` WRITE;
DELETE FROM `jqp`.`page_result_field`;
INSERT INTO `jqp`.`page_result_field` (`id`,`page_id`,`field`,`label`,`width`,`type`,`format`,`hidden`,`seq`) VALUES (127, 1, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(128, 1, 'code', '编码', NULL, 'string', NULL, 'NO', 2),(129, 1, 'name', '名称', NULL, 'string', NULL, 'NO', 3),(130, 1, 'page_type', '页面类型', NULL, 'string', NULL, 'NO', 4),(131, 1, 'query_sql', '查询sql', NULL, 'sql', NULL, 'NO', 5),(132, 1, 'order_by', '排序', NULL, 'string', NULL, 'NO', 6),(133, 1, 'js', 'js脚本', NULL, 'js', NULL, 'NO', 7),(134, 2, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(135, 2, 'parent_id', '父菜单', NULL, 'long', NULL, 'YES', 2),(136, 2, 'menu_code', '菜单编号', NULL, 'string', NULL, 'NO', 3),(137, 2, 'menu_name', '菜单名称', NULL, 'string', NULL, 'NO', 4),(160, 3, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(161, 3, 'dic_code', '字典编号', NULL, 'string', NULL, 'NO', 2),(162, 3, 'dic_name', '字典名称', NULL, 'string', NULL, 'NO', 3),(163, 4, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(164, 4, 'parent_id', '字典id', NULL, 'long', NULL, 'YES', 2),(165, 4, 'label', '字典标签', NULL, 'string', NULL, 'NO', 3),(166, 4, 'value', '字典值', NULL, 'string', NULL, 'NO', 4),(170, 6, 'id', '主键', NULL, 'long', NULL, 'YES', 1),(171, 6, 'name', '名称', NULL, 'string', NULL, 'NO', 2),(172, 6, 'code', '编号', NULL, 'string', NULL, 'NO', 3),(173, 6, 'create_time', '时间', NULL, 'date', NULL, 'NO', 4);
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`sys_menu` WRITE;
DELETE FROM `jqp`.`sys_menu`;
INSERT INTO `jqp`.`sys_menu` (`id`,`parent_id`,`menu_code`,`menu_name`) VALUES (1, NULL, '01', '菜单01'),(2, NULL, '02', '菜单02'),(3, 1, '0101', '菜单0101'),(4, 1, '0102', '菜单0102'),(5, 2, '0201', '菜单0201'),(6, 3, '010101', '菜单010101');
UNLOCK TABLES;
COMMIT;
BEGIN;
LOCK TABLES `jqp`.`test` WRITE;
DELETE FROM `jqp`.`test`;
INSERT INTO `jqp`.`test` (`id`,`name`,`code`,`create_time`) VALUES (1, '阿萨德', '安抚', '2021-12-17 17:05:25'),(2, '方法', '第三方', '2021-11-10 17:05:33');
UNLOCK TABLES;
COMMIT;
