
AMIS_JSON={
    "type": "page",
    "definitions":{
        "formTabs":{
            "tabs":[{
                "title":"基本信息",
                "body": [
                    {
                        "type": "input-text",
                        "name": "code",
                        "label": "编号",
                        "required": true
                    },
                    {
                        "type": "input-text",
                        "name": "name",
                        "label": "名称",
                        "required": true
                    },
                    {
                        "type": "input-text",
                        "name": "tableName",
                        "label": "表名",
                        "required": true
                    },
                    {
                        "type": "input-text",
                        "name": "initApi",
                        "placeholder":"默认配置:post:/admin/common/{formCode}/get?id=${id}",
                        "label": "初始化接口"
                    },
                    {
                        "type": "input-text",
                        "name": "api",
                        "placeholder":"默认配置:post:/admin/common/{formCode}/saveOrUpdate",
                        "label": "保存接口"
                    },
                    {
                        "type": "select",
                        "name": "size",
                        "label": "窗口大小",
                        "options":[{
                            "label":"较小",
                            "value":"sm"
                        },{
                            "label":"标准",
                            "value":"default"
                        },{
                            "label":"较大",
                            "value":"lg"
                        },{
                            "label":"很大",
                            "value":"xl"
                        },{
                            "label":"全屏",
                            "value":"full"
                        }]
                    },
                    {
                        "type": "input-number",
                        "name": "fieldWidth",
                        "label": "字段宽度"
                    },
                    {
                        "type": "editor",
                        "name": "js",
                        "label": "js脚本",
                        "language": "javascript",
                        "required": false
                    }
                ]
            },{
                title:"表单字段",
                body:[
                    {
                        "type":"button",
                        "label":"刷新",
                        "actionType": "ajax",
                        "api": "post:/admin/form/formFields",
                        //"reload":"resultFields?resultFields=${resultFields}"
                    },{
                        "type": "input-table",
                        "label":"查询结果",
                        "name": "formFields",
                        "addable": false,
                        "removable": false,
                        "needConfirm":false,
                        "draggable": true,
                        //"copyable": true,
                        //"editable": true,
                        "required": true,
                        "headerToolbar": [
                            "filter-toggler"
                        ],
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "field",
                                "label": "字段",
                                "required": true,
                                //"disabled":true
                            },
                            {
                                "type":"input-text",
                                "name": "label",
                                "label": "名称",
                                "required": true
                            },
                            {
                                "type":"input-number",
                                "name": "width",
                                "label": "宽度",
                                "required": false
                            },
                            {
                                "type":"select",
                                "name": "type",
                                "label": "字段类型",
                                "required": true,
                                "options":[{
                                    "label":"字符串",
                                    "value":"string"
                                },{
                                    "label":"长文本",
                                    "value":"long-string"
                                },{
                                    "label":"超长文本",
                                    "value":"big-string"
                                },{
                                    "label":"SQL",
                                    "value":"sql"
                                },{
                                    "label":"js脚本",
                                    "value":"js"
                                },{
                                    "label":"文章",
                                    "value":"article"
                                },{
                                    "label":"短整数",
                                    "value":"int"
                                },{
                                    "label":"长整数",
                                    "value":"long"
                                },{
                                    "label":"小数",
                                    "value":"double"
                                },{
                                    "label":"日期",
                                    "value":"date"
                                },{
                                    "label":"数据字典",
                                    "value":"dic"
                                }]
                            },
                            {
                                "type":"input-text",
                                "name": "format",
                                "label": "格式化"
                            },
                            {
                                "type":"select",
                                "name": "hidden",
                                "label": "是否隐藏",
                                "required": true,
                                "options":[{
                                    "label":"YES",
                                    "value":"YES"
                                },{
                                    "label":"NO",
                                    "value":"NO"
                                }]
                            },
                            {
                                "type":"select",
                                "name": "multi",
                                "label": "是否多选",
                                "required": true,
                                "options":[{
                                    "label":"YES",
                                    "value":"YES"
                                },{
                                    "label":"NO",
                                    "value":"NO"
                                }]
                            },
                            {
                                "type":"select",
                                "name": "must",
                                "label": "是否必填",
                                "required": true,
                                "options":[{
                                    "label":"YES",
                                    "value":"YES"
                                },{
                                    "label":"NO",
                                    "value":"NO"
                                }]
                            },
                            {
                                "type":"input-text",
                                "name": "value",
                                "label": "默认值",
                            }
                        ]
                    }
                ]
            }]
        },
        "form":{
            "title": "${IF(ISEMPTY(name),'新增',CONCATENATE('编辑-',name))}",
            "size": "full",
            "body": {
                "type": "form",
                "initApi": "/admin/form/get?id=${id}",
                "api": "post:/admin/form/save",
                "$ref":"formTabs"
            }
        },
        "copyForm":{
            "title": "复制-${tableName}",
            "size": "xl",
            "body": {
                "type": "form",
                "initApi": "/tableInfo/copyTableInfo?tableName=${id}",
                "api": "post:/tableInfo/updateTable",
                "$ref":"formTabs"
            }
        }
    },
    "body": {
        "type": "crud",
        "api": "post:/admin/form/query",
        "syncLocation": false,
        "filterTogglable": true,
        "headerToolbar": [
            "filter-toggler",
            {
                "type": "button",
                "actionType": "dialog",
                "label": "新增",
                "size":"sm",
                "icon": "fa fa-plus pull-left",
                "primary": true,
                "dialog":{
                    "$ref":"form"
                }
            }
        ],
        "filter": {
            "title": "条件搜索",
            "submitText": "",
            "body": [
                {
                    "type": "input-text",
                    "name": "code",
                    "label":"编号"
                },
                {
                    "type": "input-text",
                    "name": "name",
                    "label":"名称"
                },{
                    "label": "搜索",
                    "type": "submit"
                }
            ]
        },
        "columns": [
            {
                "name": "code",
                "label": "编号"
            },
            {
                "name": "name",
                "label": "名称"
            },
            {
                "name": "tableName",
                "label": "表"
            },
            {
                "name": "js",
                "label": "js脚本"
            },
            {
                "type": "operation",
                "label": "操作",
                "buttons": [
                    {
                        "label": "编辑",
                        "type": "button",
                        "actionType": "dialog",
                        "dialog": {
                            "$ref":"form"
                        }
                    },{
                        "label": "复制",
                        "type": "button",
                        "actionType": "dialog",
                        "dialog": {
                            "$ref":"copyForm"
                        }
                    },{
                        "type": "button",
                        "level":"danger",
                        "actionType": "ajax",
                        "label": "删除",
                        "confirmText": "您确认要删除${oldTableName}?",
                        "api": "/tableInfo/dropTable?tableName=${oldTableName}"
                    }
                ]
            }
            /*,{
                "type":"each",
                "label":"循环",
                "name":"buttons",
                "className":"antd-OperationField",
                "items":{
                    "type":"button",
                    "size":"sm",
                    "label":"${name}",
                    "actionType": "ajax",
                    "api":"/xxxx.xxx"
                }
            }
            */
        ]
    }
}