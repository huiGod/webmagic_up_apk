package com.uq.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.uq.base.db.anno.Table;

public class VirtualORM {

	/**
     * 保存一个对象的sql语句。最后两个参数是临时为止，
     * 他们的存在令人很不爽。
     * @param bean
     * @param sql 用于带回sql语句的参数
     * @param params 用于带回参数的参数
     * @return
     */
    public static DBParams save(Object bean){
        Class<? extends Object> beanClass = bean.getClass();
        List<String> fields   = new ArrayList<String>();
        List<Method> getters  = new ArrayList<Method>();
        getGetters(beanClass, fields, getters);
         
        /**开始拼装sql和params*/
        List<Object> tempParams = new ArrayList<Object>();
        boolean boo = beanClass.isAnnotationPresent(Table.class);
        String tablename = "";
        if(boo){
        	Table t = (Table)beanClass.getAnnotation(Table.class);
        	String vv = t.value();
//        	System.err.println(vv);
        	tablename=" "+vv;
        }else {
			tablename = beanClass.getSimpleName();//没注解默认为类名
		}
        String insertSql = "insert  into " + tablename;
        String valueSql  = "";
        String columnSql = "";
        Method getter    = null;
        String field     = null;
        for(int i=0;i<fields.size();i++){
            getter = getters.get(i);
            field  =  fields.get(i);
            columnSql += (field+",");
            valueSql += "?,";
            try{
                tempParams.add(getter.invoke(bean, null));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        columnSql = ( "(" + columnSql + ")").replace(",)", ")");
        valueSql = ( "(" + valueSql + ")").replace(",)", ")");
         
        DBParams DBP = new DBParams();
        DBP.setSql(insertSql+columnSql+"values"+valueSql);
        DBP.setParams(tempParams.toArray());
        return DBP;
    }
    /**
     * 更新一条记录
     * @param bean 所要更新的表
     * @param columns 所要查询的字段
     * @param whereSql 查询的条件
     * @return
     */
    public static DBParams update(Object bean,String[] columns,String whereSql){
        Class<? extends Object> beanClass = bean.getClass();
        List<String> fields  = new ArrayList<String>();
        List<Method> getters = new ArrayList<Method>();
        getGetters(beanClass, fields, getters);
         
        /**过滤出要更新的字段*/
        int oldSize = fields.size();
        int index = -1;
        for(int i=0;i<columns.length;i++){
            index = fields.indexOf(columns[i].toString().toLowerCase());
            if(index != -1){
                fields.add(fields.get(index));
                getters.add(getters.get(index));
            }
        }
        int newSize = fields.size();
        fields = fields.subList(oldSize,newSize);
        getters = getters.subList(oldSize, newSize);
        
        boolean boo = beanClass.isAnnotationPresent(Table.class);
        String tablename = "";
        if(boo){
        	Table t = (Table)beanClass.getAnnotation(Table.class);
        	String vv = t.value();
        	System.err.println(vv);
        	tablename=" "+vv;
        }else {
			tablename = beanClass.getSimpleName();//没注解默认为类名
		}
        
        /**开始拼装sql和params*/
        List<Object> tempParams = new ArrayList<Object>();
        String updateSql = "update " + tablename + " set ";
        String columnSql = "";
        Method getter    = null;
        String field     = null;
        for(int i=0;i<fields.size();i++){
            getter = getters.get(i);
            field  =  fields.get(i);
            columnSql += ("," + field+"=?");
            try{
                tempParams.add(getter.invoke(bean, null));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        columnSql = columnSql.replaceFirst(",", "");
        /**返回*/
        DBParams DBP = new DBParams();
        DBP.setSql(updateSql+columnSql+" "+whereSql);
        DBP.setParams(tempParams.toArray());
        return DBP;
    }
    /**
     * @param bean 要持久化得对象
     * @param breakNull 持久化时是否忽略值为null的属性
     * @param whereSql 查询条件
     * @return
     */
    public static DBParams update(Object bean,boolean breakNull,String whereSql){
        Class<? extends Object> beanClass = bean.getClass();
        List<String> fields  = new LinkedList<String>();
        List<Method> getters = new ArrayList<Method>();
        getGetters(beanClass, fields, getters);
        
        boolean boo = beanClass.isAnnotationPresent(Table.class);
        String tablename = "";
        if(boo){
        	Table t = (Table)beanClass.getAnnotation(Table.class);
        	String vv = t.value();
        	System.err.println(vv);
        	tablename=" "+vv;
        }else {
			tablename = beanClass.getSimpleName();//没注解默认为类名
		}
        
        List<Object> tempParams = new LinkedList<Object>();
        Method getter = null;
        for(int i=0;i<getters.size();i++){
            getter = getters.get(i);
            try {
                tempParams.add(getter.invoke(bean,null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /**排除null属性，不予保存*/
        if(breakNull){
            for(int i=0;i<tempParams.size();i++){
                if(tempParams.get(i)==null){
                    tempParams.remove(i);
                    fields.remove(i);
                }
            }
        }
        /**开始拼接sql语句*/
        String updateSql = "update " + tablename + " set ";
        String columnSql = "";
        for(int i=0;i<tempParams.size();i++){
            columnSql += ("," + fields.get(i)+"=?");
        }
         
        columnSql = columnSql.replaceFirst(",", "");
        /**返回*/
        DBParams DBP = new DBParams();
        DBP.setSql(updateSql+columnSql+" "+whereSql);
        DBP.setParams(tempParams.toArray());
        return DBP;
    }
    /**
     * 拼接出一个select sql 语句
     * @param table 所要查询的表
     * @param columns 所要查询的字段
     * @param whereSql 查询的条件
     * @return
     */
    public static String select(String table,String[] columns,String whereSql){
        /**开始拼装sql和params*/
        String selectSql = "select ? from " + table + " ";
        String columnSql = "";
        for(int i=0;i<columns.length;i++){
            columnSql += (","+columns[i]);
        }
        columnSql = columnSql.replaceFirst(",", "");
        selectSql = selectSql.replace("?", columnSql);
        return selectSql+" "+whereSql;
    }
    /**
     * 找到指定类的所有getters()方法，但是去除Object父类的getClass()方法。
     * @param beanClass 指定的持久化对象
     * @param fields 返回所有写有getters的方法的属性，Object父类的class除外  
     * @param getters 返回所有除了从Object继承的getters方法
     */
    private static void getGetters(Class<? extends Object> beanClass,List<String> fields,List<Method> getters){
        Method[] methods = beanClass.getMethods();
         
        HashMap<String,Method> map = new HashMap<String,Method>();
        String field = null,methodName = null;
        for(Method meth:methods){
            methodName  = meth.getName();
            field       = methodName.substring(3).toLowerCase();
             
            if(methodName.substring(0, 3).equals("get")){
                map.put(field,meth);
            }
        }
        map.remove("class");
        map.entrySet();
        for(Entry<String,Method> item:map.entrySet()){
            fields.add(item.getKey());
            getters.add(item.getValue());
        }
    }
}
