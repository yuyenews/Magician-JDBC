package com.magician.jdbc.sqlbuild.sql;

import com.magician.jdbc.sqlbuild.BaseSqlBuilder;

import java.lang.reflect.Field;

/**
 * 插入数据
 */
public class Insert extends BaseSqlBuilder {

    /**
     * 要插入的字段
     */
    private Class setFields;

    public Insert(String tableName){
        this.setTableName(tableName);
    }

    /**
     * 设置要插入的字段
     * @param cls
     * @return
     */
    public Insert column(Class cls){
        this.setFields = cls;
        return this;
    }

    /**
     * 构建sql
     * @return
     * @throws Exception
     */
    @Override
    public String builder() throws Exception {

        Field[] fields = this.setFields.getDeclaredFields();
        if(fields == null || fields.length < 1){
            throw new Exception(setFields.getName()+"里面没有字段，不符合规则");
        }
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ");
        sql.append(this.getTableName());
        sql.append("(");
        sql.append(getCol(fields));
        sql.append(") values(");
        boolean isFirst = true;
        for (Field field : fields) {
            if(ignore(field)){
                continue;
            }

            String fieldName = getFieldName(field);

            if (!isFirst) {
                sql.append(",");
            }
            sql.append("#{");
            sql.append(fieldName);
            sql.append("}");
            isFirst = false;
        }
        sql.append(")");
        return sql.toString();
    }

    @Override
    public BaseSqlBuilder end(String end) {
        return this;
    }
}
