
AMIS_JSON={
    "type": "page",
    "title":"数据库表管理",
    "definitions":{
        "formTabs":{
            "tabs":[{
                "title":"基本信息",
                "body": [
                    {
                        "type": "input-text",
                        "name": "tableName",
                        "label": "表名"
                    },
                    {
                        "type": "input-text",
                        "name": "tableComment",
                        "label": "注释"
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
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "columnName",
                                "label": "名称"
                            },
                            {
                                "type":"select",
                                "name": "columnType",
                                "label": "类型",
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
                                "label": "注释"
                            },
                            {
                                "type":"select",
                                "name": "isNullable",
                                "label": "允许为空",
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
                                "label": "名称"
                            },
                            {
                                //"type":"input-text",
                                "type": "select",
                                "name": "columnName",
                                "label": "字段",
                                "multiple": true,
                                "labelField":"columnComment",
                                "valueField":"columnName",
                                "source":"${columnInfos}"
                            },
                            {
                                "type":"input-text",
                                "name": "indexComment",
                                "label": "注释"
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
        "api": "post:/tableInfo/queryTable",
        "syncLocation": false,
        "autoFillHeight": true,
        "headerToolbar": [
            "export-excel",
            "export-csv"
        ],
        "filter": {
            "title": "条件搜索",
            "submitText": "",
            "mode": "inline",
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
                        "api": "delete:/tableInfo/dropTable?tableName=${oldTableName}",
                        //"disabled":"${tableName=='test10'}"
                    }
                ]
            }
        ]
    }
}