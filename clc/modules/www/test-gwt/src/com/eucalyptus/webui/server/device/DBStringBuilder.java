package com.eucalyptus.webui.server.device;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class DBStringBuilder {
    
    private StringBuilder sb = new StringBuilder();
    
    public DBStringBuilder append(DBTable table) {
        sb.append(table.toString());
        return this;
    }
    
    public DBStringBuilder append(DBTableColumn column) {
        sb.append(column.toString());
        return this;
    }
    
    public DBStringBuilder append(int v) {
        sb.append(v);
        return this;
    }
    
    public DBStringBuilder appendNull() {
        sb.append("null");
        return this;
    }
    
    public DBStringBuilder append(double v) {
        sb.append(v);
        return this;
    }
    
    public DBStringBuilder append(String s) {
        if (s == null) {
            s = "";
        }
        sb.append(s);
        return this;
    }
    
    public DBStringBuilder appendDate(Date date) {
        if (date == null) {
            sb.append("null");
        }
        else {
            sb.append("\"").append(DBData.format(date)).append("\"");
        }
        return this;
    }
    
    public DBStringBuilder appendDate() {
        sb.append("\"").append(DBData.format(new Date())).append("\"");
        return this;
    }
    
    public DBStringBuilder appendString(String s) {
        if (s == null) {
            s = "";
        }
        sb.append("\"").append(s).append("\"");
        return this;
    }
    
    public DBStringBuilder appendDateBound(DBTableColumn column, Date beg, Date end) {
        append(column).append(" != ").appendString("0000-00-00");
        if (beg != null) {
            append(" AND ").append(column).append(" >= ").appendDate(beg);
        }
        if (end != null) {
            append(" AND ").append(column).append(" <= ").appendDate(end);
        }
        return this;
    }
    
    public DBStringBuilder appendDateLifeRemains(DBTableColumn beg, DBTableColumn end, DBTableColumn alias) {
        append(" IF (");
        append("DATEDIFF(").append(end).append(", ").append(beg).append(")");
        append(" < ");
        append("DATEDIFF(").append(end).append(", now())");
        append(", ");
        append("DATEDIFF(").append(end).append(", ").append(beg).append(")");
        append(", ");
        append("DATEDIFF(").append(end).append(", now())");
        append(") AS ").append(alias);
        return this;
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
    
    public String toSql(Logger log) {
        String sql = sb.toString();
        if (log != null) {
            log.info(sql);
        }
        return sql;
    }
    
    public static String listToString(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        int total = 0;
        for (int id : list) {
            if (total ++ != 0) {
                sb.append(", ");
            }
            sb.append(id);
        }
        return sb.toString();
    }
    
    public static String getDate() {
        return DBData.format(new Date());
    }
    
    public static String getDate(Date date) {
        return DBData.format(date);
    }
    
}
