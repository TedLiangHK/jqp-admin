
AMIS_JSON={
    "type": "page",
    "definitions":{
        "jsonForm":{
            "title": "json-${name}",
            "size": "full",
            "body": {
                "type": "form",
                "initApi": "/admin/page/getJson?id=${id}",
                "api": "/admin/page/saveJson",
                "actions":[],
                "body": [
                    {
                        "type": "editor",
                        "name": "json",
                        "size":"xxl",
                        "language": "json",
                        "disabled": false,
                        "allowFullscreen":true
                    }
                ]
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
                "label": "新增",
                "type": "button",
                "actionType": "dialog",
                "primary":true,
                "dialog": {
                    "title":"新增",
                    "size":"full",
                    "body":{
                        "type":"iframe",
                        "src":"/admin/page/pageEdit.html",
                        "height":"calc( 100% - 5px )"
                    },
                    "actions":[]
                }
            },
            {
                "type": "button",
                "actionType": "dialog",
                "label": "JSON新增",
                "size":"sm",
                "icon": "fa fa-plus pull-left",
                "primary": true,
                "dialog":{
                    "$ref":"jsonForm"
                }
            }
        ],
        "filter": {
            "title": "条件搜索<button type='button' onclick='window.location.reload()' style='float:right;position:relative;top:-8px;' class='cxd-Button cxd-Button--sm cxd-Button--default is-active'>刷新页面</button>",
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
                "label": "编号",
                "width":200
            },
            {
                "name": "name",
                "label": "名称",
                "width":200
            },
            // {
            //     "name": "querySql",
            //     "label": "查询sql"
            // },
            {
                "name": "pageType",
                "label": "页面类型",
                "width":200
            },
            {
                "name": "orderBy",
                "label": "排序",
                "width":200
            },
            {
                "name": "js",
                "label": "js脚本"
            },
            {
                "type": "operation",
                "label": "操作",
                "width":200,
                "buttons": [
                    {
                        "label": "编辑",
                        "type": "button",
                        "actionType": "dialog",
                        "dialog": {
                            "title":"编辑-${name}",
                            "size":"full",
                            "body":{
                                "type":"iframe",
                                "src":"/admin/page/pageEdit.html?id=${id}",
                                "height":"calc( 100% - 5px )"
                            },
                            "actions":[]
                        }
                    },{
                        "label": "复制",
                        "type": "button",
                        "actionType": "dialog",
                        "dialog": {
                            "title":"复制-${name}",
                            "size":"full",
                            "body":{
                                "type":"iframe",
                                "src":"/admin/page/pageEdit.html?id=${id}&type=copy",
                                "height":"calc( 100% - 5px )"
                            },
                            "actions":[]
                        }
                    },{
                        "label": "预览",
                        "type": "button",
                        "actionType": "dialog",
                        "dialog": {
                            "title": "预览-${name}",
                            "size": "full",
                            "body": {
                                "type":"iframe",
                                "src":"/crud/${code}",
                                "height":"98%"
                            },
                            "actions":[]
                        }
                    },{
                        "type": "button",
                        "level":"danger",
                        "actionType": "ajax",
                        "label": "删除",
                        "confirmText": "您确认要删除${name}?",
                        "api": "/admin/common/page/delete/${id}"
                    },{
                        "label": "编辑JSON",
                        "type": "button",
                        "actionType": "dialog",
                        "dialog": {
                            "$ref":"jsonForm"
                        }
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
