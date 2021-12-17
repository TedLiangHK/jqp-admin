
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
                        "label":"查询结果",
                        "name": "resultFields",
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
                                "disabled":true
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
                            }
                        ]
                    }
                ]
            },{
                title:"查询条件",
                body:[{
                        "type": "input-table",
                        "name": "queryFields",
                        "addable": true,
                        "removable": true,
                        "needConfirm":false,
                        "draggable": true,
                        "strictMode":true,
                        //"copyable": true,
                        //"editable": true,
                        "label":"查询条件",
                        "required": false,
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
                                "type":"select",
                                "name": "opt",
                                "label": "操作类型",
                                "required": true,
                                "options":[{
                                    "label":"等于",
                                    "value":"eq"
                                },{
                                    "label":"不等于",
                                    "value":"notEq"
                                },{
                                    "label":"等于多值",
                                    "value":"in"
                                },{
                                    "label":"不等于多值",
                                    "value":"notIn"
                                },{
                                    "label":"包含",
                                    "value":"like"
                                },{
                                    "label":"包含多值",
                                    "value":"likeIn"
                                },{
                                    "label":"不包含",
                                    "value":"notLike"
                                },{
                                    "label":"不包含多值",
                                    "value":"notLikeIn"
                                },{
                                    "label":"范围",
                                    "value":"betweenAnd"
                                },{
                                    "label":"小于",
                                    "value":"less"
                                },{
                                    "label":"小于等于",
                                    "value":"lessEq"
                                },{
                                    "label":"大于",
                                    "value":"large"
                                },{
                                    "label":"大于等于",
                                    "value":"largeEq"
                                }]
                            },
                            {
                                "type":"input-text",
                                "name": "value",
                                "label": "默认值",
                                "required": false
                            },
                            {
                                "type":"select",
                                "name": "type",
                                "label": "数据类型",
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
                                    "label":"整数(20)",
                                    "value":"long"
                                },{
                                    "label":"整数(11)",
                                    "value":"int"
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
                                "type":"input-number",
                                "name": "width",
                                "label": "宽度(1~12)"
                            },
                            {
                                "type":"input-text",
                                "name": "format",
                                "label": "格式化"
                            },
                            {
                                "type":"input-text",
                                "name": "dateExpress",
                                "label": "日期表达式"
                            },
                            {
                                "type":"textarea",
                                "name": "optionSql",
                                "label": "选项sql"
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
    "body": {
        "type": "crud",
        "api": "post:/admin/page/query",
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