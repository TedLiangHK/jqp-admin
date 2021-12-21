
AMIS_JSON={
    "type": "page",
    "definitions":{
        "formTabs":{
            "tabs":[{
                "title":"基本信息",
                "body": [
                    {
                        "type": "input-text",
                        "name": "tableName",
                        "label": "表名",
                        "required": true
                    },
                    {
                        "type": "input-text",
                        "name": "tableComment",
                        "label": "注释",
                        "required": true
                    }
                ]
            },{
                title:"字段",
                body:[
                    {
                        "type": "input-table",
                        "name": "columnInfos",
                        "addable": true,
                        "removable": true,
                        "needConfirm":false,
                        //"copyable": true,
                        //"editable": true,
                        "required": true,
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "columnName",
                                "label": "名称",
                                "required": true
                            },
                            {
                                "type":"select",
                                "name": "columnType",
                                "label": "类型",
                                "required": true,
                                "options": [
                                    {
                                        "label": "整数(11)",
                                        "value": "int(11)"
                                    },
                                    {
                                        "label": "整数(20)",
                                        "value": "bigint(20)"
                                    },
                                    {
                                        "label": "字符串(20)",
                                        "value": "varchar(20)"
                                    },
                                    {
                                        "label": "字符串(64)",
                                        "value": "varchar(64)"
                                    },
                                    {
                                        "label": "字符串(256)",
                                        "value": "varchar(256)"
                                    },
                                    {
                                        "label": "字符串(1024)",
                                        "value": "varchar(1024)"
                                    },
                                    {
                                        "label": "大文本类型",
                                        "value": "longtext"
                                    },
                                    {
                                        "label": "日期类型",
                                        "value": "datetime"
                                    },
                                    {
                                        "label": "小数",
                                        "value": "float"
                                    },
                                    {
                                        "label": "小数1",
                                        "value": "float(19,2)"
                                    },
                                    {
                                        "label": "小数2",
                                        "value": "float(19,4)"
                                    }
                                ]
                            },
                            {
                                "type":"input-text",
                                "name": "columnComment",
                                "label": "注释",
                                "required": true
                            },
                            {
                                "type":"select",
                                "name": "isNullable",
                                "label": "允许为空",
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
                title:"索引",
                body:[
                    {
                        "type": "input-table",
                        "name": "indexInfos",
                        "addable": true,
                        "removable": true,
                        "needConfirm":false,
                        //"copyable": true,
                        //"editable": true,
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "keyName",
                                "label": "名称",
                                "required": true
                            },
                            {
                                //"type":"input-text",
                                "type": "select",
                                "name": "columnName",
                                "label": "字段",
                                "multiple": true,
                                "labelField":"columnComment",
                                "valueField":"columnName",
                                "source":"${columnInfos}",
                                "required": true
                            },
                            {
                                "type":"input-text",
                                "name": "indexComment",
                                "label": "注释",
                                "required": true
                            }
                        ]
                    }
                ]
            }]
        },
        "form":{
            "title": "${IF(ISEMPTY(tableName),'新增',CONCATENATE('编辑-',tableName))}",
            "size": "lg",
            "body": {
                "type": "form",
                "initApi": "/tableInfo/tableInfo?tableName=${oldTableName}",
                "api": "post:/tableInfo/updateTable",
                "$ref":"formTabs"
            }
        },
        "copyForm":{
            "title": "复制-${tableName}",
            "size": "lg",
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
        "api": "post:/tableInfo/queryTable",
        "syncLocation": false,
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
                    "name": "tableName",
                    "label":"表名",
                    "placeholder": "表名"
                },
                {
                    "type": "input-text",
                    "name": "tableComment",
                    "label":"注释",
                    "placeholder": "注释"
                },{
                    "label": "搜索",
                    "type": "submit"
                }
            ]
        },
        "columns": [
            {
                "name": "tableName",
                "label": "表名"
            },
            {
                "name": "tableComment",
                "label": "注释"
            },
            {
                "name": "tableRows",
                "label": "大概数据行数"
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