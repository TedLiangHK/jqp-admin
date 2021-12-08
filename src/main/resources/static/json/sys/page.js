
AMIS_JSON={
    "type": "page",
    "title":"页面管理",
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
                        "name": "orderBy",
                        "label": "排序",
                        "required": false
                    },
                    {
                        "type": "select",
                        "name": "pageType",
                        "label": "页面类型",
                        "required": true,
                        "options":[
                            {"label":"列表","value":"list"},
                            {"label":"树","value":"tree"}
                        ]
                    },
                    {
                        "type": "editor",
                        "name": "querySql",
                        "label": "查询sql",
                        "language": "sql",
                        "required": true
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
                title:"查询结果配置",
                body:[
                    {
                        "type":"button",
                        "label":"刷新",
                        "actionType": "ajax",
                        "api": "post:/admin/page/resultFields",
                        //"reload":"resultFields?resultFields=${resultFields}"
                    },{
                        "type": "input-table",
                        "name": "resultFields",
                        "addable": true,
                        "removable": true,
                        "needConfirm":false,
                        //"copyable": true,
                        //"editable": true,
                        "required": true,
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "field",
                                "label": "字段",
                                "required": true
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
                                "required": true
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
                                    "label":"数字",
                                    "value":"number"
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
                "initApi": "/admin/page/get?id=${id}",
                "api": "post:/admin/page/save",
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
    "toolbar": [
        {
            "type": "button",
            "actionType": "dialog",
            "label": "新增",
            "icon": "fa fa-plus pull-left",
            "primary": true,
            "dialog":{
                "$ref":"form"
            }
        }
    ],
    "body": {
        "type": "crud",
        "api": "post:/admin/page/query",
        "syncLocation": false,
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
                "name": "querySql",
                "label": "查询sql"
            },
            {
                "name": "pageType",
                "label": "页面类型"
            },
            {
                "name": "orderBy",
                "label": "排序"
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