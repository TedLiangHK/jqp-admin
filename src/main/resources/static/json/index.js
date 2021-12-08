AMIS_JSON={
    "type": "page",
    "data":{
       "menus":[{
           "id":"1",
           "name":"顶部1"
       },{
           "id":"1",
           "name":"顶部1"
       },{
           "id":"1",
           "name":"顶部1"
       }]
    },
    "aside": [
        {
            "type": "tpl",
            "tpl": "这是侧边栏部分"
        }
    ],
    "toolbar": [
        {
            "type": "nav",
            "links": [
                {
                    "label": "Nav 1",
                    "href":"javascript:void(0)"
                },
                {
                    "label": "Nav 2",
                    "href":"javascript:void(0)"
                },
                {
                    "label": "Nav 3",
                    "href":"javascript:void(0)"
                }
            ]
        }
    ],
    "body": [
        {
            "type": "tpl",
            "tpl": "这是内容区 ${m||1}"
        }
    ]
}