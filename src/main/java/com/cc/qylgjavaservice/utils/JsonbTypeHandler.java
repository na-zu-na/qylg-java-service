package com.cc.qylgjavaservice.utils;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(List.class)
public class JsonbTypeHandler extends JacksonTypeHandler {

    public JsonbTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            Object parameter,
            JdbcType jdbcType) throws SQLException {

        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        jsonObject.setValue(toJson(parameter));

        ps.setObject(i, jsonObject);
    }
}
