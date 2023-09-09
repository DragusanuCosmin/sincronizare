package ro.ctce.sincronizare.Mapper;
import org.springframework.jdbc.core.RowMapper;
import ro.ctce.sincronizare.Entities.Clienti;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientiRowMapper implements RowMapper<Clienti> {
    @Override
    public Clienti mapRow(ResultSet rs, int rowNum) {
        String userId;
        String nrDosar;
        String bazaDeDate;
        try {
            userId = rs.getString("user_id");
            nrDosar = rs.getString("nr_dosar");
            bazaDeDate = rs.getString("baza_de_date");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Clienti(userId,nrDosar,bazaDeDate);
    }
}

