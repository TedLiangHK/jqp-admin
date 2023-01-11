package com.jqp.admin.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.TreeData;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Slf4j
public class Util {


    public static boolean exists(Integer count) {
        return count != null && count > 0;
    }

    public static boolean isTest() {
        return !Objects.equals("prod", SpringUtil.getActiveProfile());
    }

    public static void writeResponse(HttpServletResponse response, Object obj) {
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().print(JSONUtil.toJsonStr(obj));
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    /***
     * 构建树状结构
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends TreeData<T>> List<T> buildTree(List<T> list){
        //根节点
        List<T> roots = new ArrayList<>();

        //全部节点id
        Set<Long> allIds = new HashSet<>();

        //父节点id:子节点id列表
        Map<Long,Set<Long>> idChildren = new HashMap<>();

        //id:节点对象
        Map<Long,T> idMap = new HashMap<>();
        for(T t:list){
            allIds.add(t.getId());
            idMap.put(t.getId(),t);
            //如果有父节点,将自己加入父节点的子节点列表里面
            if(t.getParentId() != null){
                if(!idChildren.containsKey(t.getParentId())){
                    idChildren.put(t.getParentId(),new HashSet<>());
                }
                idChildren.get(t.getParentId()).add(t.getId());
            }
        }

        for(T t:list){
            //父节点为空或者父节点id不在id列表里面,即为根节点
            if(t.getParentId() == null || !allIds.contains(t.getParentId())){
                roots.add(t);
            }
        }
        for(Map.Entry<Long,Set<Long>> entry:idChildren.entrySet()){
            T parent = idMap.get(entry.getKey());
            if(parent != null){
                //组装子节点
                for(Long childId:entry.getValue()){
                    parent.getChildren().add(idMap.get(childId));
                }
                //子节点排序
                Collections.sort(parent.getChildren());
            }
        }

        //根节点排序
        Collections.sort(roots);
        return roots;
    }
}