package ro.ctce.sincronizare.Dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ro.ctce.sincronizare.Entities.Clienti;

@Repository("ActualizareDao")
public class ActualizareDataAccessService implements ActualizareDao {
    public final JdbcTemplate jdbcTemplate;
    @Autowired
    public ActualizareDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void adaugareDatabase(Clienti clienti) {
        final String sql="insert into indexdosare.clienti(user_id,nr_dosar,baza_de_date) values(?,?,?)";
        jdbcTemplate.update(sql,clienti.getUserId(),clienti.getNrDosar(),clienti.getBazaDeDate());
    }
    @Override
    public void stergereDatabase(String nrDosar) {
        final String sql="delete from indexdosare.clienti where user_id=?";
        jdbcTemplate.update(sql,nrDosar);
    }
    @Override
    public void actualizareDatabase(Clienti clienti){
        final String sql="update indexdosare.clienti set user_id=?, nr_dosar=?,baza_de_date=? where nr_dosar=?";
        jdbcTemplate.update(sql,clienti.getUserId(),clienti.getNrDosar(),clienti.getBazaDeDate(),clienti.getNrDosar());
    }
}
