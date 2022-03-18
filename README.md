# jqp-admin

#### 介绍
基于百度amis,springboot2.6,magic-api开发的一套低代码后台管理平台,从建表开始,配置页面,表单,复杂的关联关系,实现了功能权限,数据权限,工作流,sass运营商模式,其中大部分页面(90%以上)都是通过配置出来的

#### 整体设计思路
##### 列表页(列表+树状)+表单页+magic-api(在线业务逻辑脚本)+activiti(工作流) 满足绝大部分业务场景,不满足的可以用java来实现
1. 列表页面通过查询的sql语句来实现,为什么原则用sql语句直接查询,因为做后台查询,通常面临业务变动,加字段,减字段,如果用mybatis本身就得改sql,如果用hibernate,有直接的关联关系还好,没有的话,就得大改了,如果做扩展字段的方式查询,列表查询没问题,会导致导出excel的时候很慢,用sql的方式最灵活,最能适应业务的变动
2. 表单页可以做复杂的关联关系,每一个列表页面都可以成为表单页面的选择器,amis天然支持,故此,加入特别方便,表单页面还可以关联子表,做各种操作
3. 主要表关联关系
    3.1 page 页面,page_result_field 页面结果字段,page_query_field 页面查询字段,page_ref 页面关联,做主表子表,左右关联布局,page_button 页面按钮
    3.2 form 表单,form_field 表单字段,form_ref 表单关联页面,form_button 表单按钮
    3.3 关于组织机构,功能权限,数据权限的表关系,看下面的 rbac 人员组织设计说明
4. 特别说明
配置完页面后,在菜单里面配置之后,需要在 **企业管理里面配置菜单权限** ,然后退出 **重新登录** 就能看到菜单了,具体的权限逻辑请看 rbac 人员组织设计说明,可以在本页面搜索到

#### 安装教程

1.  mysql5.8,数据库备份文件在db/20220308172121.nb3,可以通过Navicat复制到数据库备份目录,直接还原备份
2.  jdk8
3.  maven
4.  使用idea开发的


#### 演示  
1.    http://jqp.hyz792901324.com/ 
2.    账号密码admin,1

# 统一说明
1. table_name为表名 比如dic_item
2. model为实体名字会自动进行驼峰转换,比如dic_item会转换为 DicItem,反过来也可以

# 使用说明

系统启动后,管理员账号密码为 admin,1

# 更新/保存
/admin/common/{formCode}/saveOrUpdate

# 删除
/admin/common/{model}/delete/{id}

# 查询关联id
/admin/common/{model}/getRelationIds/{mainField}/{relationField}/{id}

# 查询关联id,且携带企业id,企业id值为当前登录人的企业id
/admin/common/{model}/getRelationIdsForEnterprise/{mainField}/{relationField}/{id}

# 重新保存关联关系,这个场景是有限列表里面勾选的,比如下拉树多选的情况,比如岗位菜单,会删除原数据,重新保存
/admin/common/{model}/reSaveRelation/{mainField}/{relationField}

# 重新保存关联关系,携带企业id
/admin/common/{model}/reSaveRelationForEnterprise/{mainField}/{relationField}

# 增加关联关系
/admin/common/{model}/addRelation/{mainField}/{relationField}

# 默认表单initApi接口,获取数据,如果表单配置了初始化sql,会自动携带数据
/admin/common/{formCode}/get

# 查询数据字典接口
/options/{dicCode}

# 页面访问页面
/crud/{pageCode}

# 一对多左右布局的访问页面,关联关系只能是parentId
/oneToMany/{pageCode}/{childPageCode}

# 按钮配置
1. 弹出表单,配置值为表单编号
2. 弹出iframe,配置值为页面url
3. 弹出页面,配置值为   页面编号,关联字段
4. 弹出ajax请求,配置值为url,前面可以加  post:url 会携带requestBody当前表单全部参数 或者get:url

# 日志页面
/admin/operationLog/page/{model}/{id}

# 流程审核配置 ajax请求
1. /admin/process/{code}/{nodeCode}/{auditResult}/{id}
2. code为流程编号
3. nodeCode为流程节点编号
4. auditResult为是否审核通过,pass为通过,back为打回
5. 配置流程审核表单可以通过复制的方式,审核记录也是

# 菜单初始化按钮
1. 会自动初始化这个页面下面的关联按钮
2. 会自动查找页面按钮,表单按钮,表单关联页面按钮
3. 会自动初始化菜单,按钮对应的url请求

# 页面查询可以携带变量参数
1. $userId 用户id
2. $enterpriseId 企业id
3. $deptId 部门id
4. $service 为templateService,里面有树转换接口,和数据权限接口

# rbac 人员组织设计说明
1. 菜单 sys_menu
2. 用户 user 用户,用户分为普通用户和管理员,管理员可以拥有全部企业,全部企业菜单功能
3. 企业 enterprise
4. 企业管理员 enterprise_manager  企业管理员拥有此企业下全部菜单功能
5. 企业菜单 enterprise_menu
6. 岗位 position,岗位分固定岗位和自定义岗位,岗位编码全局唯一,岗位配置菜单是配置当前所在企业的菜单
7. 企业岗位菜单 position_menu 
8. 企业人员 ,enterprise_user,创建企业用户会自动创建user表和enterprise_user关联,也可以关联其他企业用户
9. 企业人员岗位 enterprise_user_position 
10. 权限定义 permission,字典为selfOrAll 的是自己/全部,字典为dept为部门级别数据权限
11. position_permission 岗位数据权限
12. dept_permission 部门数据权限
13. 用户菜单逻辑,管理员取企业全部菜单,包含系统管理员和企业管理员
14. 关联关系,用户->用户岗位->用户岗位菜单
15. 数据权限,用户所在部门的数据权限和用户拥有全部岗位的数据权限和用户数据权限进行合并,取并集,最大范围
16. 数据权限使用,在页面配置的查询sql里面 增加$service.permission('{code}','{field}')
    code为权限编码,field为当前查询对应的权限字段
17. magicApi是表单前后置逻辑,返回字符串表示失败,返回null表示成功,最主要的写业务逻辑的地方,注意不要使用数据库增删改不要用magicApi内置的db接口,这样会导致无法记录字段日志

# 关联字段
关联字段为destField=${srcId},将srcId的值以destField名称传入此页面,多个用&隔开

 

# 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

# 联系方式
1.  讨论qq群 550662264
2.  微信![输入图片说明](https://images.gitee.com/uploads/images/2022/0311/102556_86cf9a17_358006.png "屏幕截图.png")

# 参考

1.  百度amis https://aisuda.bce.baidu.com/amis/zh-CN/docs/index
2.  magic-api https://www.ssssssss.org/magic-api
3.  activiti 5.22
4.  spring boot2.6

# 捐献,开源不易,希望大家多多支持
1.  支付宝![输入图片说明](7d221c9c64946c48d47ad6e556e8791.jpg)
2.  微信![输入图片说明](d1e90d37c43028b399f60ec62349f29.jpg)



