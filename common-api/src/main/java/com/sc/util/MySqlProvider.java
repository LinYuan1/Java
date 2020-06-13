package com.sc.util;

import com.sc.annotation.Id;
import com.sc.annotation.Table;
import org.apache.ibatis.jdbc.SQL;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MySqlProvider {

    public static final String INSERT = "insert";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";

    public static String insert(Object obj){
        String table = getTableName(obj);
        Map<String, String> map = new HashMap<>();
        getMap(obj,map);
        return new SQL(){
            {
                INSERT_INTO(table);
                for (String key:map.keySet()){
                    VALUES(key, map.get(key));
                }
            }
        }.toString();
    }

    public static String update(Object obj){
        Map<String, String> map = new HashMap<>();
        String table = getTableName(obj);
        String idName = getMap(obj, map);
        if (StringUtil.isEmpty(idName)) {
            throw new RuntimeException("实体类->" + obj.getClass().getCanonicalName() + "必须有@Id注解");
        }
        String idValue = map.get(idName);
        map.remove(idName);

        return new SQL(){
            {
                UPDATE(table);
                for (String key : map.keySet()) {
                    SET(key + "=" + map.get(key));
                }
                WHERE(idName + "=" +  idValue);
            }
        }.toString();
    }

    public static String delete(String table, String where) {
        if (StringUtil.isEmpty(table)) {
            return null;
        }

        if (StringUtil.isEmpty(where)) {
            return null;
        }

        return new SQL(){
            {
                DELETE_FROM(table);
                WHERE(where);
            }
        }.toString();
    }

    private static String getMap(Object obj,Map<String, String> map) {
        Class c = obj.getClass();
        Field[] fs = c.getDeclaredFields();
        String idName = null;
        for (Field item : fs){
            String key = item.getName();
            item.setAccessible(true);
            if (idName == null) {
                idName = getIdName(item);
            }
            try {
                if (item.get(obj)!=null){
                    map.put(StringUtil.underscoreName(key), "#{" + key + "}");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return idName;
    }

    private static String getIdName(Field obj) {
        Class c = obj.getClass();
        Id id = (Id)c.getAnnotation(Id.class);
        if (id != null && !StringUtil.isEmpty(id.value())) {
            return id.value();
        }

        return StringUtil.underscoreName(obj.getName());
    }

    private static String getTableName(Object obj) {
        Class c = obj.getClass();
        Table table = (Table) c.getAnnotation(Table.class);
        if(table != null){
            return table.value();
        }
        return StringUtil.underscoreName(c.getSimpleName());
    }

}