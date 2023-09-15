package ro.ctce.sincronizare.Mapper;
import org.springframework.jdbc.core.RowMapper;
import ro.ctce.sincronizare.Entities.Dosar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class DosareRowMapper implements RowMapper<Dosar> {
    @Override
    public Dosar mapRow(ResultSet rs, int rowNum) {
         int id;
         int userid;
         Date dataCreare;
         Date dataUM;
         String nrIntern;
         int dosarInstanta;
         String numarDosar;
         String instantadosar;
         String andosar;
         String accesoriidosar;
         String solutionatfavorabil;
         double valoaredosar;
         double valoarerecuperata;
         Date dataFinalizare;
         int idClient;
         int idStare;
         boolean finalizat;
         boolean bPublic;
         boolean sters;
         String comentarii;
         String solutiedosar;
         Date dataUMSCJ;
         int idParteaAdversa;
         int rezultat;
         String sentintaPrimita;
        try {
            id = rs.getInt("id");
            userid = rs.getInt("userid");
            dataCreare = rs.getDate("datacreare");
            dataUM = rs.getDate("dataum");
            nrIntern = rs.getString("numarintern");
            dosarInstanta = rs.getInt("dosarinstanta");
            numarDosar = rs.getString("numardosar");
            instantadosar = rs.getString("instantadosar");
            andosar = rs.getString("andosar");
            accesoriidosar = rs.getString("accesoriidosar");
            solutionatfavorabil = rs.getString("solutionatfavorabil");
            valoaredosar = rs.getDouble("valoaredosar");
            valoarerecuperata = rs.getDouble("valoarerecuperata");
            dataFinalizare = rs.getDate("datafinalizare");
            if (dataFinalizare != null && dataFinalizare.equals(new Date(0))) {
                System.out.println("data finalizare null");
                dataFinalizare = null;
            }
            idClient = rs.getInt("idclient");
            idStare = rs.getInt("idstare");
            finalizat = rs.getBoolean("finalizat");
            bPublic = rs.getBoolean("bpublic");
            sters = rs.getBoolean("sters");
            comentarii = rs.getString("comentarii");
            solutiedosar = rs.getString("solutiedosar");
            dataUMSCJ = rs.getDate("dataumSCJ");
            idParteaAdversa = rs.getInt("idparteadversa");
            rezultat = rs.getInt("rezultat");
            sentintaPrimita = rs.getString("sentintaprimita");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Dosar(id,userid,dataCreare,dataUM,nrIntern,dosarInstanta,numarDosar,instantadosar,andosar,accesoriidosar,solutionatfavorabil,valoaredosar,valoarerecuperata,dataFinalizare,idClient,idStare,finalizat,bPublic,sters,comentarii,solutiedosar,dataUMSCJ,idParteaAdversa,rezultat,sentintaPrimita);
    }
}

