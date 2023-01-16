package com.jqp.admin.rbac.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.Result;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.ActionType;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.RefType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.DicService;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.rbac.service.InputParam;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.UrlUtil;
import com.jqp.admin.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/doc")
public class DocController {
    @Resource
    private JdbcService jdbcService;

    @Resource
    private DicService dicService;

    @Resource
    private PageService pageService;

    @Resource
    private FormService formService;

    @GetMapping("/category")
    public Result category() {
        List<Map<String, Object>> menuTypes = dicService.options("menuType");
        for (Map<String, Object> menuType : menuTypes) {
            List<SysMenu> menus = jdbcService.find(SysMenu.class, new String[]{
                    SysMenu.Fields.menuType,
                    SysMenu.Fields.whetherButton
            }, new Object[]{
                    menuType.get("value"),
                    Whether.NO
            });
            menus = Util.buildTree(menus);
            menuType.put("menus", menus);
        }
        return Result.success(menuTypes);
    }

    @GetMapping("/page/{code}")
    public Result page(@PathVariable String code) {
        Page page = pageService.get(code);
        Set<String> apiKeys = new HashSet<>();
        List<Map<String, Object>> apis = buildPageApi(page,apiKeys);
        return Result.success(apis);
    }

    private List<Map<String, Object>> buildFormApi(Form form,Set<String> apiKeys) {
        String apiKey = StrUtil.format("form:{}",form.getCode());

        List<Map<String, Object>> apis = new ArrayList<>();
        if(apiKeys.contains(apiKey)){
            return apis;
        }
        apiKeys.add(apiKey);

        String initApi = null;
        String saveApi = null;

        if(StrUtil.isNotBlank(form.getTableName())){
            initApi = StrUtil.format("post:/admin/common/{}/get",form.getCode());
            saveApi = StrUtil.format("post:/admin/common/{}/saveOrUpdate",form.getCode());
        }
        if(StrUtil.isNotBlank(form.getInitApi())){
            initApi = form.getInitApi();
        }
        if(StrUtil.isNotBlank(form.getApi())){
            saveApi = form.getApi();
        }
        if(StringUtils.isNotBlank(initApi)){
            if(UrlUtil.match(initApi,"post:/admin/common/{code}/get")){
                String formCode = initApi.substring("post:/admin/common/".length(), initApi.lastIndexOf("/"));
                Form initForm = formService.get(formCode);
                Map<String, Object> api = new HashMap<>();
                api.put("name", initForm.getName() + "-详情");
                api.put("url", initApi.substring("post:".length())+"?id=${id}");
                api.put("method", "get");

                List<Map<String,Object>> params = new ArrayList<>();
                params.add(buildParam("id","主键","","(非必填)整数类型"));

                api.put("params",params);
                List<Map<String,Object>> data = new ArrayList<>();
                for (FormField formField : initForm.getFormFields()) {
                    Map<String, Object> rs = buildInputParam(formField);
                    data.add(rs);
                }
                api.put("result",buildResult(data,"json"));
                apis.add(api);
            }
        }

        if(StringUtils.isNotBlank(saveApi)){
            if(UrlUtil.match(saveApi,"post:/admin/common/{code}/saveOrUpdate")){
                String formCode = saveApi.substring("post:/admin/common/".length(), initApi.lastIndexOf("/"));
                Form saveForm = formService.get(formCode);
                Map<String, Object> api = new HashMap<>();
                api.put("name", saveForm.getName() + "-保存");
                api.put("url", saveApi.substring("post:".length()));
                api.put("method", "post");

                List<Map<String,Object>> params = new ArrayList<>();
                for (FormField formField : saveForm.getFormFields()) {
                    params.add(buildInputParam(formField));
                }
                api.put("params",params);

                List<Map<String,Object>> data = new ArrayList<>();
                for (FormField formField : saveForm.getFormFields()) {
                    Map<String, Object> rs = buildInputParam(formField);
                    data.add(rs);
                }

                api.put("result",buildResult(data,"json"));
                apis.add(api);
            }
        }

//        Map<String, Object> api = new HashMap<>();
//        api.put("name", page.getName() + "-分页查询");
//        api.put("url", StrUtil.format("/admin/page/crudQuery/{}", page.getCode()));
//        api.put("method", "post");


        return apis;
    }

    private List<Map<String,Object>> buildResult(List<Map<String,Object>> data,String type){
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(buildParam("msg", "提示信息", "", "字符串"));
        results.add(buildParam("status", "状态码", "", "0:成功;1:未登录;2:无权限;9:其他错误"));
        results.add(buildParam("msg", "提示信息", "", "字符串"));
        Map<String, Object> dataSchema = buildParam("data", "数据", "", "");
        dataSchema.put("children",data);
        dataSchema.put("type",type);
        results.add(dataSchema);
        return results;
    }

    private List<Map<String, Object>> buildPageApi(Page page,Set<String> apiKeys) {
        String apiKey = StrUtil.format("page:{}",page.getCode());

        List<Map<String, Object>> apis = new ArrayList<>();
        if(apiKeys.contains(apiKey)){
            return apis;
        }

        Map<String, Object> api = new HashMap<>();
        api.put("name", page.getName() + "-分页查询");
        api.put("url", StrUtil.format("/admin/page/crudQuery/{}", page.getCode()));
        api.put("method", "post");

        List<Map<String, Object>> params = new ArrayList<>();
        params.add(buildParam("page", "第几页", "1", "数字类型"));
        params.add(buildParam("perPage", "每页数量", "10", "数字类型"));
        params.add(buildParam("orderBy", "排序字段", "", "字符串类型"));
        params.add(buildParam("orderDir", "排序方式", "asc", "字符串类型;asc:正序;desc:倒序"));
        List<PageQueryField> queryFields = page.getQueryFields();
        for (PageQueryField field : queryFields) {
            params.add(buildInputParam(field));
        }

        api.put("params", params);

        List<Map<String, Object>> data = new ArrayList<>();

        List<Map<String, Object>> results = buildResult(data, "json");
        api.put("result", results);

        data.add(buildParam("total", "总数量", "100", "数字类型"));

        Map<String, Object> itemsSchema = buildParam("items", "明细", "100", "");
        data.add(itemsSchema);

        List<Map<String, Object>> items = new ArrayList<>();
        itemsSchema.put("children", items);

        List<PageResultField> resultFields = page.getResultFields();
        for (PageResultField field : resultFields) {
            items.add(buildInputParam(field));
        }
        apis.add(api);

        List<PageButton> pageButtons = page.getPageButtons();
        for (PageButton pageButton : pageButtons) {

            String optionType = pageButton.getOptionType();
            if (ActionType.PopForm.equals(optionType)) {
                //弹出表单
                apis.addAll(buildFormApi(formService.get(pageButton.getOptionValue()),apiKeys));
            } else if (ActionType.PopPage.equals(optionType)) {
                //弹出页面
                continue;
            } else if (ActionType.OpenNew.equals(optionType)) {
                //浏览器打开新页面
                continue;
            } else if (ActionType.Ajax.equals(optionType)) {
                //ajax请求
                continue;
            } else if (ActionType.PopIframe.equals(optionType)) {
                //弹出框加载iframe
                continue;
            }
        }
        List<PageRef> pageRefs = page.getPageRefs();
        for (PageRef pageRef : pageRefs) {
            String refType = pageRef.getRefType();
            if(RefType.Page.equals(refType)){
                Page refPage = pageService.get(pageRef.getRefPageCode());
                apis.addAll(buildPageApi(refPage,apiKeys));
            }else if(RefType.Form.equals(refType)){
                Form form = formService.get(pageRef.getRefPageCode());
                apis.addAll(buildFormApi(form,apiKeys));
            }
        }
        return apis;
    }

    Map<String,Object> buildInputParam(InputParam field){
        String remark = "";
        String example = "";
        String type = field.getType();
        if (DataType.isDate(type)) {
            remark += "日期类型,格式:" + field.getFormat();
        } else if (DataType.isImg(type)) {
            remark += "图片路径,多个图片用英文逗号隔开,最后斜杠为上传图片名称";
            example = "/download/241/abc.jpg,/download/241/123.png";
        } else if (DataType.isFile(type)) {
            remark += "文件路径,多个文件用英文逗号隔开,最后斜杠为上传文件名称";
            example = "/download/241/abc.zip,/download/241/123.jpg";
        } else if (DataType.isDic(type)) {
            remark = "数据字典(" + field.getFormat() + ");";
            List<Map<String, Object>> options = dicService.options(field.getFormat());
            StringBuilder sb = new StringBuilder();
            options.forEach(option -> {
                sb.append(option.get("value")).append(":").append(option.get("label")).append(";");
            });
            remark += sb.toString();
            if (!options.isEmpty()) {
                example = (String) options.get(0).get("value");
            }
        } else if (DataType.isStr(type)) {
            remark += "字符串";
        } else if (DataType.isNumber(type)) {
            remark += "整数";
        } else if (DataType.isDouble(type)) {
            remark += "小数";
        }
        String fieldName = null;
        if(field instanceof FormField){
            fieldName = field.getField();
        }else{
            fieldName = StringUtil.toFieldColumn(field.getField());
        }
        return buildParam(fieldName, field.getLabel(), example, remark);
    }

    Map<String, Object> buildParam(String name, String label, String example, String remark) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", name);
        param.put("label", label);
        param.put("remark", remark);
        return param;
    }
}
