package com.magician.jdbc.helper.templete;

import com.magician.jdbc.core.util.JSONUtil;
import com.magician.jdbc.core.util.ThreadUtil;
import com.magician.jdbc.helper.DBHelper;
import com.magician.jdbc.helper.manager.ConnectionManager;
import com.magician.jdbc.helper.manager.DataSourceManager;
import com.magician.jdbc.helper.templete.conversion.SqlConversion;
import com.magician.jdbc.helper.templete.model.Condition;
import com.magician.jdbc.helper.templete.model.PageModel;
import com.magician.jdbc.helper.templete.model.PageParamModel;
import com.magician.jdbc.helper.templete.model.SqlBuilderModel;
import com.magician.jdbc.helper.templete.util.ConditionBuilder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * database operations
 */
public class JDBCTemplate {

    /**
     * data source
     */
    private String dataSource;

    private JDBCTemplate(){}

    /**
     * Get JDBCTemplate object
     * @return
     */
    public static JDBCTemplate get(){
        return get(null);
    }

    /**
     * Get JDBCTemplate object
     * @param dataSource
     * @return
     */
    public static JDBCTemplate get(String dataSource){
        if (dataSource == null) {
            dataSource = DataSourceManager.getDefaultDataSourceName();
        }
        JDBCTemplate jdbcTemplate = new JDBCTemplate();
        jdbcTemplate.dataSource = dataSource;
        return jdbcTemplate;
    }

    /* -------------------------------------- SQL-free operation of a single table ------------------------------------------ */

    /**
     * No sql, single table query
     * @param tableName
     * @param conditionBuilder
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> select(String tableName, ConditionBuilder conditionBuilder, Class<T> cls) throws Exception {
        ConnectionManager connectionManager = getConnection();
        try {
            List<Condition> conditions = conditionBuilder.build();
            StringBuffer sql = new StringBuffer();
            sql.append("select * from ");
            sql.append(tableName);

            List<Map<String, Object>> result = null;
            if (conditions != null && conditions.size() > 0) {
                sql.append(" where ");
                SqlBuilderModel sqlBuilderModel = SqlConversion.getSql(sql, conditions);
                result = DBHelper.selectList(sqlBuilderModel.getSql(), connectionManager.getConnection(), sqlBuilderModel.getParams());
            } else {
                result = DBHelper.selectList(sql.toString(), connectionManager.getConnection(), null);
            }

            List<T> resultList = new ArrayList<>();
            for (Map<String, Object> item : result){
                resultList.add(JSONUtil.toJavaObject(item, cls));
            }
            return resultList;
        } catch (Exception e){
            throw e;
        } finally {
            connectionManager.close();
        }
    }

    /**
     * No sql, single table update
     * @param tableName
     * @param data
     * @param conditionBuilder
     * @return
     * @throws Exception
     */
    public int update(String tableName,  Object data, ConditionBuilder conditionBuilder) throws Exception {
        List<Condition> conditions = conditionBuilder.build();

        if (conditions == null || conditions.size() < 1) {
            throw new Exception("For the sake of safety, please write sql for unconditional modification operations.");
        }

        ConnectionManager connectionManager = getConnection();

        try {
            Map<String, Object> paramMap = JSONUtil.toMap(data);

            StringBuffer sql = new StringBuffer();
            sql.append("update ");
            sql.append(tableName);
            sql.append(" set ");

            List<Object> paramList = new ArrayList<>();

            Boolean first = false;
            for (Map.Entry<String, Object> item : paramMap.entrySet()) {
                if (item.getValue() == null) {
                    continue;
                }
                if (first) {
                    sql.append(",");
                }
                sql.append(item.getKey());
                sql.append(" = ?");
                paramList.add(item.getValue());

                first = true;
            }
            sql.append(" where ");
            SqlBuilderModel sqlBuilderModel = SqlConversion.getSql(sql, conditions);
            for (Object item : sqlBuilderModel.getParams()) {
                paramList.add(item);
            }
            return DBHelper.update(sqlBuilderModel.getSql(), connectionManager.getConnection(), paramList.toArray());
        } catch (Exception e) {
            throw e;
        } finally {
            connectionManager.close();
        }
    }

    /**
     * No sql, single table delete
     * @param tableName
     * @param conditionBuilder
     * @return
     * @throws Exception
     */
    public int delete(String tableName, ConditionBuilder conditionBuilder) throws Exception {
        List<Condition> conditions = conditionBuilder.build();

        if (conditions == null || conditions.size() < 1) {
            throw new Exception("For the sake of safety, please write sql for unconditional delete operations.");
        }
        ConnectionManager connectionManager = getConnection();
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("delete from ");
            sql.append(tableName);
            sql.append(" where ");
            SqlBuilderModel sqlBuilderModel = SqlConversion.getSql(sql, conditions);

            return DBHelper.update(sqlBuilderModel.getSql(), connectionManager.getConnection(), sqlBuilderModel.getParams());
        } catch (Exception e) {
            throw e;
        } finally {
            connectionManager.close();
        }
    }

    /**
     * No sql, single table insert
     * @param tableName
     * @param data
     * @return
     * @throws Exception
     */
    public int insert(String tableName, Object data) throws Exception {
        ConnectionManager connectionManager = getConnection();
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("insert into ");
            sql.append(tableName);
            sql.append(" (");

            StringBuffer values = new StringBuffer();
            values.append(") values (");

            Map<String, Object> paramMap = JSONUtil.toMap(data);

            List<Object> paramList = new ArrayList<>();

            Boolean first = false;
            for (Map.Entry<String, Object> item : paramMap.entrySet()) {
                if (item.getValue() == null) {
                    continue;
                }
                if (first) {
                    sql.append(",");
                    values.append(",");
                }
                sql.append(item.getKey());
                values.append("?");
                paramList.add(item.getValue());

                first = true;
            }

            sql.append(values);
            sql.append(")");

            return DBHelper.update(sql.toString(), connectionManager.getConnection(), paramList.toArray());
        } catch (Exception e) {
            throw e;
        } finally {
            connectionManager.close();
        }
    }

    /* -------------------------------------- Customize sql to do complex operations ------------------------------------------ */

    /**
     * query list
     * @param sql
     * @param param
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> selectList(String sql, Object param, Class<T> cls) throws Exception {

        ConnectionManager connectionManager = getConnection();
        try {
            List<T> resultList = new ArrayList<>();

            if (param instanceof Object[]) {
                List<Map<String, Object>> result = DBHelper.selectList(sql, connectionManager.getConnection(), (Object[]) param);
                for (Map<String, Object> item : result) {
                    resultList.add(JSONUtil.toJavaObject(item, cls));
                }
            } else {

                SqlBuilderModel sqlBuilderModel = SqlConversion.builderSql(sql, param);

                List<Map<String, Object>> result = DBHelper.selectList(sqlBuilderModel.getSql(), connectionManager.getConnection(), sqlBuilderModel.getParams());
                for (Map<String, Object> item : result) {
                    resultList.add(JSONUtil.toJavaObject(item, cls));
                }
            }
            return resultList;
        } catch (Exception e) {
            throw e;
        } finally {
            connectionManager.close();
        }
    }

    /**
     * query list
     * @param sql
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> selectList(String sql, Class<T> cls) throws Exception {
        return selectList(sql, new Object[0], cls);
    }

    /**
     * query a piece of data
     * @param sql
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T selectOne(String sql, Object param, Class<T> cls) throws Exception {
        List<T> resultList = selectList(sql, param, cls);
        if (resultList != null && resultList.size() > 1) {
            throw new Exception("more than one data");
        }
        if (resultList != null && resultList.size() < 1) {
            return null;
        }
        return resultList.get(0);
    }

    /**
     * query a piece of data
     * @param sql
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T selectOne(String sql, Class<T> cls) throws Exception {
        return selectOne(sql, new Object[0], cls);
    }

    /**
     * insert, delete, update
     * @param sql
     * @param param
     * @return
     */
    public int exec(String sql, Object param) throws Exception {
        ConnectionManager connectionManager = getConnection();
        try {
            if (param instanceof Object[]) {
                return DBHelper.update(sql, connectionManager.getConnection(), (Object[]) param);
            }

            SqlBuilderModel sqlBuilderModel = SqlConversion.builderSql(sql, param);
            return DBHelper.update(sqlBuilderModel.getSql(), connectionManager.getConnection(), sqlBuilderModel.getParams());
        } catch (Exception e) {
            throw e;
        } finally {
            connectionManager.close();
        }
    }

    /**
     * insert, delete, update
     * @param sql
     * @return
     */
    public int exec(String sql) throws Exception {
        return exec(sql, new Object[0]);
    }

    /* -------------------------------------- Paging query ------------------------------------------ */

    /**
     * Use the default countSql for paging queries
     * @param sql
     * @param pageParamModel
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> PageModel<T> selectPage(String sql, PageParamModel pageParamModel, Class<T> cls) throws Exception {
        String countSql = "select count(0) total from(" + sql + ") tbl";
        return selectPageCustomCountSql(sql, countSql, pageParamModel, cls);
    }

    /**
     * Paging query with custom countSql
     * @param sql
     * @param countSql
     * @param pageParamModel
     * @param cls
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> PageModel<T> selectPageCustomCountSql(String sql, String countSql, PageParamModel pageParamModel, Class<T> cls) throws Exception {

        Map result = selectOne(countSql, pageParamModel.getParam(), Map.class);
        Object totalObj = result.get("total");
        if (totalObj == null || "".equals(totalObj)) {
            totalObj = 0;
        }

        pageParamModel.getParam().put("pageStart",(pageParamModel.getCurrentPage()-1) * pageParamModel.getPageSize());
        pageParamModel.getParam().put("pageSize",pageParamModel.getPageSize());

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(sql);
        stringBuffer.append(" limit {pageStart},{pageSize}");

        List<T> resultList = selectList(stringBuffer.toString(), pageParamModel.getParam(), cls);

        PageModel<T> pageModel = new PageModel<>();
        pageModel.setPageCount(Integer.parseInt(totalObj.toString()));
        pageModel.setCurrentPage(pageParamModel.getCurrentPage());
        pageModel.setPageSize(pageParamModel.getPageSize());
        pageModel.setPageTotal(getPageTotal(pageModel));
        pageModel.setDataList(resultList);
        return pageModel;
    }

    /**
     * Calculate the total number of pages
     * @param pageModel
     * @return
     */
    private int getPageTotal(PageModel pageModel){
        int pageTotal = pageModel.getPageCount() / pageModel.getPageSize();

        if (pageModel.getPageCount() % pageModel.getPageSize() == 0) {
            return pageTotal;
        } else {
            return pageTotal + 1;
        }
    }

    /**
     * Get the connection in the transaction
     * @return
     */
    private ConnectionManager getConnection() throws Exception {
        ConnectionManager connectionManager = new ConnectionManager();

        Object obj = ThreadUtil.getThreadLocal().get();
        if (obj != null) {
            Map<String, Connection> connections = (Map<String, Connection>) obj;

            connectionManager.setTransaction(true);
            connectionManager.setConnection(connections.get(dataSource));
        } else {
            connectionManager.setTransaction(false);
            connectionManager.setConnection(DataSourceManager.getConnection(dataSource));
        }

        return connectionManager;
    }
}
