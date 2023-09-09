package ro.ctce.sincronizare.Mapper;

import org.springframework.jdbc.core.RowMapper;
import ro.ctce.sincronizare.Entities.DataObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataObjectRowMapper implements RowMapper<DataObject> {
    @Override
    public DataObject mapRow(ResultSet resultSet, int i) throws SQLException {
        return new DataObject(
                resultSet.getString("dataum"),
                resultSet.getString("dataumSCJ")
        );
    }
}