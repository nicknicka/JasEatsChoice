package com.xx.jaseatschoicejava.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JsonNode类型处理器，用于MyBatis与数据库的JSON类型转换
 */
@MappedTypes({JsonNode.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.CLOB, JdbcType.NVARCHAR, JdbcType.NCLOB})
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, JsonNode jsonNode, JdbcType jdbcType) throws SQLException {
        try {
            String jsonString = objectMapper.writeValueAsString(jsonNode);
            preparedStatement.setString(i, jsonString);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to convert JsonNode to JSON string", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String jsonString = resultSet.getString(s);
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON string to JsonNode", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String jsonString = resultSet.getString(i);
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON string to JsonNode", e);
        }
    }

    @Override
    public JsonNode getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String jsonString = callableStatement.getString(i);
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON string to JsonNode", e);
        }
    }
}
