
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
                        //"copyable": true,
                        //"editable": true,
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "columnName",
                                "label": "名称"
                            },
                            {
                                "type":"input-text",
                                "name": "columnType",
                                "label": "类型"
                            },
                            {
                                "type":"input-text",
                                "name": "columnComment",
                                "label": "注释"
                            },
                            {
                                "type":"input-text",
                                "name": "isNullable",
                                "label": "允许为空"
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
                        //"copyable": true,
                        //"editable": true,
                        "columns": [
                            {
                                "type":"input-text",
                                "name": "keyName",
                                "label": "名称"
                            },
                            {
                                "type":"input-text",
                                "name": "columnName",
                                "label": "字段"
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
            "title": "${tableName || '新增'}",
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
        "filter": {
            "title": "条件搜索",
            "submitText": "",
            "body": [
                {
                    "type": "input-text",
                    "name": "tableName",
                    "placeholder": "表名"
                },
                {
                    "type": "input-text",
                    "name": "tableComment",
                    "placeholder": "注释",
                    "addOn": {
                       "label": "搜索",
                       "type": "submit"
                    }
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
                        "api": "delete:/tableInfo/dropTable?tableName=${oldTableName}"
                    }
                ]
            }
        ]
    }
}