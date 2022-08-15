package com.jqp.admin.page.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.*;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.constants.Constants;
import com.jqp.admin.common.data.Obj;
import com.jqp.admin.db.data.ColumnMeta;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.constants.DataType;
import com.jqp.admin.page.constants.RefType;
import com.jqp.admin.page.constants.Whether;
import com.jqp.admin.page.data.*;
import com.jqp.admin.page.service.FormService;
import com.jqp.admin.page.service.PageButtonService;
import com.jqp.admin.page.service.PageConfigService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.rbac.service.ApiService;
import com.jqp.admin.util.StringUtil;
import com.jqp.admin.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/page")
@Slf4j
public class PageController {
    @Resource
    private JdbcService jdbcService;

    @Resource
    private PageService pageService;

    @Resource
    private FormService formService;
    @Resource
    private PageConfigService pageConfigService;

    @Resource
    private PageButtonService pageButtonService;

    @Resource
    private ApiService apiService;

    @RequestMapping("/query")
    public Result<PageData<Page>> query(@RequestBody PageParam pageParam){
        String sql = "select * from page where 1=1 ";
        List<Object> values = new ArrayList<>();
        if(StringUtils.isNotBlank(pageParam.getStr("code"))){
            sql += " and code like ? ";
            values.add("%"+pageParam.getStr("code")+"%");
        }
        if(StringUtils.isNotBlank(pageParam.getStr("name"))){
            sql += " and name like ? ";
            values.add("%"+pageParam.getStr("name")+"%");
        }
        return jdbcService.query(pageParam,Page.class,sql,values.toArray());
    }
    @RequestMapping("/copyPage")
    public Result<Page> copyPage(Long id){
        if(id == null){
            return Result.success();
        }
        Page copy = pageService.get(id);
        copy.setId(null);
        copy.setCode(copy.getCode()+"_copy");
        return Result.success(copy);
    }
    @RequestMapping("/get")
    public Result<Page> get(Long id){
        if(id == null){
            return Result.success(new Page());
        }
        return Result.success(pageService.get(id));
    }
    @RequestMapping("/getJson")
    public Result getJson(Long id){
        Page page = pageService.get(id);
        if(page == null){
            page = new Page();
        }
        return Result.success(MapUtil.builder().put("json",JSONUtil.toJsonPrettyStr(page)).build());
    }
    @RequestMapping("/saveJson")
    public Result saveJson(@RequestBody Map<String,Object> map){
        String json = (String) map.get("json");
        Page page = JSONUtil.toBean(json, Page.class);
        Page oldPage = pageService.get(page.getCode());
        if(oldPage != null){
            page.setId(oldPage.getId());
        }else{
            page.setId(null);
        }
        pageService.save(page);
        return Result.success();
    }

    @RequestMapping("/save")
    public Result<String> save(@RequestBody Page page){
        if(jdbcService.isRepeat("select id from page where code = '$code' and id <> $id ",BeanUtil.beanToMap(page))){
            return Result.error("页面编号重复");
        }
        pageService.save(page);
        return Result.success();
    }
    @RequestMapping("/resultFields")
    public Result resultFields(@RequestBody Page page){
        pageService.reload(page);
        return Result.success(page,"已刷新");
    }

    @RequestMapping("/crudQuery/{pageCode}")
    public Result<CrudData<Map<String,Object>>> crudQuery(@RequestBody PageParam pageParam, @PathVariable(name="pageCode") String pageCode){
        Set<String> keySet = new HashSet<>(pageParam.keySet());
        keySet.forEach(key->{
            if(key.startsWith(Constants.QUERY_KEY_START)){
                Object value = pageParam.remove(key);
                pageParam.put(key.substring(Constants.QUERY_KEY_START.length()),value);
            }
        });
        return pageService.query(pageCode,pageParam);
    }
    @RequestMapping("/options/{pageCode}")
    public Result pageOptions(@PathVariable(name="pageCode") String pageCode){
        PageParam pageParam = new PageParam();
        pageParam.put("page",1);
        pageParam.put("perPage",Integer.MAX_VALUE);
        Result<CrudData<Map<String, Object>>> result = pageService.query(pageCode, pageParam);
        Map<String,Object> data = new HashMap<>();
        data.put("options",result.getData().getRows());
        return Result.success(data);
    }
    @RequestMapping("/selector/{pageCode}")
    public Result<CrudData<Map<String,Object>>> selector(@RequestBody PageParam pageParam, @PathVariable(name="pageCode") String pageCode, HttpServletRequest request){
        pageParam.put("selector",true);

        Map<String, String[]> parameterMap = request.getParameterMap();

        Set<String> keySet = new HashSet<>(pageParam.keySet());
        keySet.forEach(key->{
            if(key.startsWith(Constants.QUERY_KEY_START)){
                Object value = pageParam.remove(key);
                pageParam.put(key.substring(Constants.QUERY_KEY_START.length()),value);
            }
        });
        //过滤树结构当前id,防止循环引用
        if(parameterMap.containsKey("parentId") && parameterMap.containsKey("id")){
            String[] ids = parameterMap.get("id");
            if(ids != null && ids.length == 1 && StrUtil.isNotBlank(ids[0])){
                Long treeId = Long.parseLong(ids[0]);
                pageParam.put("treeId",treeId);
            }
        }
        return pageService.query(pageCode,pageParam);
    }

    @RequestMapping("/crudExport/{pageCode}")
    public void crudExport(@RequestParam Map<String,Object> pageParam, @PathVariable(name="pageCode") String pageCode,HttpServletResponse response){
        Set<String> keySet = new HashSet<>(pageParam.keySet());
        keySet.forEach(key->{
            if(key.startsWith(Constants.QUERY_KEY_START)){
                Object value = pageParam.remove(key);
                pageParam.put(key.substring(Constants.QUERY_KEY_START.length()),value);
            }
        });
        PageParam param = new PageParam();
        param.putAll(pageParam);
        param.put("page",1);
        param.put("perPage",Integer.MAX_VALUE);

        Result<CrudData<Map<String, Object>>> result = pageService.query(pageCode, param);
        Page page = pageService.get(pageCode);

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String fn = page.getName() + sdf.format(new Date()) + ".csv";
            // 读取字符编码
            String utf = "UTF-8";
            // 设置响应
            response.setContentType("application/ms-txt.numberformat:@");
            response.setCharacterEncoding(utf);
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=30");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fn, utf));
            response.setCharacterEncoding("GBK");
            PrintWriter writer = response.getWriter();
            List<String> fields = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            List<ColumnData> columns = result.getData().getColumns();
            Map<String,ColumnData> columnsMap = new HashMap<>();
            columns.forEach(c->{
                columnsMap.put(c.getName(),c);
            });
            for(PageResultField resultField:page.getResultFields()){
                if(!Whether.YES.equals(resultField.getHidden())){
                    fields.add(StringUtil.toFieldColumn(resultField.getField()));
                    titles.add(resultField.getLabel());
                }
            }
            writer.println("\t"+StringUtil.concatStr(titles,",\t"));
            List<Map<String, Object>> rows = result.getData().getRows();
            for(Map<String,Object> row:rows){
                writeTree(row,writer,fields,0,columnsMap);
            }
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeTree(Map<String,Object> data,PrintWriter writer,List<String> fields,int len,Map<String,ColumnData> columnsMap){
        List<Object> rowDatas = new ArrayList<>();
        fields.forEach(field->{
            ColumnData columnData = columnsMap.get(field);
            Object value = data.get(field);
            if("mapping".equals(columnData.get("type"))){
                Map<String,Object> map = (Map<String, Object>) columnData.get("map");
                value = map.get(data.get(field));
            }
            if(value == null){
                value = "";
            }
            rowDatas.add(value);
        });

        String pre = "";
        for(int i=0;i<len;i++){
            if(i==len-1){
                pre += "  ";
            }else{
                pre += "    ";
            }
        }
        if(len>0){
            pre += "└";
        }
        List<Map<String,Object>> children = (List<Map<String, Object>>) data.get("children");
        writer.println("\t"+pre+StringUtil.concatStr(rowDatas,",\t"));
        if(children != null){
            children.forEach(child->{
                writeTree(child,writer,fields,len+1,columnsMap);
            });
        }
    }

    private String checkPage(Page page){
        Map<String,Object> context = new HashMap<>();
        context.put("page",page);

        Result<String> beforeApiResult = apiService.call(page.getBeforeApi(), context);
        if(!beforeApiResult.isSuccess()){

            Map<String,Object> params = new HashMap<>();
            params.put("errMsg",beforeApiResult.getMsg());
            return TemplateUtil.getUi("error.js.vm",params);
        }
        return null;
    }

    @RequestMapping("/js/{pageCode}.js")
    public String js(@PathVariable("pageCode") String pageCode,HttpServletResponse response){

        response.setContentType("application/javascript");
        response.addHeader("Cache-Control","no-store");
        Page page = pageService.get(pageCode);

        String checkPage = this.checkPage(page);
        if(StringUtils.isNotBlank(checkPage)){
            return checkPage;
        }

        if(page == null){
            return "location.href='/admin/lyear_pages_error.html?url=crud/"+pageCode+"';";
        }
        String template = "ui-json-template/crud.js.vm";
        if(!page.getPageRefs().isEmpty()){
            template = "ui-json-template/crudTabs.js.vm";
        }

        InputStream in = getClass().getClassLoader().getResourceAsStream(template);
        String js = IoUtil.readUtf8(in);
        IoUtil.close(in);
        Map<String,Object> params = new HashMap<>();
        params.put("pageCode",pageCode);



        StringBuffer downloadParam = new StringBuffer("?1=1");
        page.getQueryFields().forEach(f->{
            String fieldName = StringUtil.toFieldColumn(f.getField());
            if(!fieldName.toLowerCase().contains("id")&& !Whether.YES.equals(f.getRef())){
                fieldName = Constants.QUERY_KEY_START+fieldName;
            }
            downloadParam.append("&")
                    .append(fieldName)
                    .append("=${")
                    .append(fieldName)
                    .append("}");
        });

        List<Map<String,Object>> queryConfigs = pageConfigService.queryConfigs(page);

        params.put("pageName",page.getName());
        params.put("queryConfigs", JSONUtil.toJsonPrettyStr(queryConfigs));
        params.put("downloadParam",downloadParam);

        PageButtonData pageButtonData = pageButtonService.dealPageButton(page.getPageButtons(), false);

        int perPage = 10;
        List<Integer> perPageAvailable = new ArrayList<>();
        perPageAvailable.add(10);
        perPageAvailable.add(20);
        perPageAvailable.add(50);
        perPageAvailable.add(100);
        perPageAvailable.add(300);
        perPageAvailable.add(500);
        if("tree".equals(page.getPageType())){
            perPage = 10000;
            perPageAvailable.clear();
            perPageAvailable.add(10000);
        }
        params.put("perPage",perPage);
        params.put("perPageAvailable",JSONUtil.toJsonPrettyStr(perPageAvailable));
        params.put("topButtons",JSONUtil.toJsonPrettyStr(pageButtonData.getTopButtons()));
        params.put("bulkButtons",JSONUtil.toJsonPrettyStr(pageButtonData.getBulkButtons()));

        if(!page.getPageRefs().isEmpty()){
            int width = page.getWidth() != null ? page.getWidth() : 6;
            int tabWidth = 12-width;
            params.put("width",width);
            params.put("tabWidth",tabWidth);


            List<String> targets = new ArrayList<>();

            List<Map<String, Object>> tabs = new ArrayList<>();
            for(PageRef pageRef:page.getPageRefs()){
                Map<String,Object> tab = new HashMap<>();
                String refType = pageRef.getRefType();
                Map<String,Object> tabBody = null;
                String title = null;

                String[] arr = StringUtil.splitStr(pageRef.getRefField(), "&");
                Map<String,Object> data = new HashMap<>();
                data.put("id","");
                for(String p:arr){
                    String[] kv = StringUtil.splitStr(p, "=");
                    data.put(kv[0],kv[1]);
                }

                if(RefType.Page.equals(refType)){
                    Page tabPage = pageService.get(pageRef.getRefPageCode());
                    title = tabPage.getName();
                    tabBody = pageConfigService.getCurdJson(pageRef.getRefPageCode());
                    tabBody.remove("title");
                    targets.add(StrUtil.format("{}Table?{}",tabPage.getCode(),pageRef.getRefField()));
                }else if(RefType.Form.equals(refType)){
                    Form form = formService.get(pageRef.getRefPageCode());
                    Map<String, Object> formJson = formService.getFormJson(pageRef.getRefPageCode(), new BaseButton());
                    Map<String,Object> formBody = (Map<String, Object>) formJson.get("body");
                    List<Map<String,Obj>> actions = (List<Map<String, Obj>>) formJson.get("actions");
                    formBody.put("name",form.getCode()+"Form");
                    if(actions != null && !actions.isEmpty()){
                        formBody.put("actions",actions);
                    }
                    targets.add(StrUtil.format("{}Form?{}",form.getCode(),pageRef.getRefField()));
                    title = form.getName();
                    tabBody = formBody;
                }else if(RefType.Iframe.equals(refType)){
                    tabBody = new HashMap<>();
                    tabBody.put("type","iframe");
                    tabBody.put("src",pageRef.getRefPageCode());
                    tabBody.put("name",pageRef.getId()+"tabFrame");
                    tabBody.put("height","95%");
                    title = "关联页面";
                    targets.add(StrUtil.format("{}tabFrame?{}",pageRef.getId(),pageRef.getRefField()));
                }
                tabBody.put("data",data);
                tab.put("title",StringUtils.isBlank(pageRef.getRefName()) ? title:pageRef.getRefName());
                tab.put("body",tabBody);
                tabs.add(tab);
            }
            params.put("target",StringUtil.concatStr(targets,","));
            params.put("tabs",JSONUtil.toJsonPrettyStr(tabs));
        }
        params.put("openPage",!Whether.NO.equals(page.getOpenPage()));
        js = TemplateUtil.getValue(js,params);
        return js;
    }

    @RequestMapping("/queryConfigs/{pageCode}")
    public Result queryConfigs(@PathVariable("pageCode") String pageCode){
        Page page = pageService.get(pageCode);
        List<Map<String, Object>> queryConfigs = pageConfigService.queryConfigs(page);
        return Result.successForKey("config",queryConfigs);
    }
}
