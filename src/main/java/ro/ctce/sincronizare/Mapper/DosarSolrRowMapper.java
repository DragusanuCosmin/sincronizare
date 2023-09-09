package ro.ctce.sincronizare.Mapper;
import org.springframework.jdbc.core.RowMapper;
import ro.ctce.sincronizare.Entities.SolrFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DosarSolrRowMapper implements RowMapper<SolrFile> {
    @Override
    public SolrFile mapRow(ResultSet rs, int rowNum) {
        String id;
        String iddosar;
        String numardosar;
        String institutie;
        Date data;
        Date datamodificarii;
        String stadiu;
        String sectie;

        String obiect;

        String materie;

        List<String> numeparte;

        List<String> calitateparte;

        List<Date> datapronuntare= new java.util.ArrayList<>(Collections.emptyList());

        List<Date> datasedinta=new java.util.ArrayList<>(Collections.emptyList());

        List<Date> datadocument=new java.util.ArrayList<>(Collections.emptyList());

        List<String> complet;

        List<String> documentsedinta;

        List<String> numardocument;

        List<String> ora;

        List<String> solutie;

        List<String> solutiesumar;

        List<Date> datadeclarare=new java.util.ArrayList<>(Collections.emptyList());

        List<String> partedeclaratoare;

        List<String> tipcaleatac;

        String numardosarvechi;
        try {
            id = rs.getString("id");
            iddosar = rs.getString("id_dosar");
            numardosar = rs.getString("nr_dosar");
            institutie = rs.getString("institutie");
            data = rs.getDate("data");
            datamodificarii = rs.getDate("datamodificarii");
            stadiu = rs.getString("stadiu");
            sectie = rs.getString("sectie");
            obiect = rs.getString("obiect");
            materie = rs.getString("materie");
            numeparte = Collections.singletonList(rs.getString("numeparte"));
            calitateparte = Collections.singletonList(rs.getString("calitateparte"));
            datapronuntare.set(0,rs.getDate("datapronuntare"));
            datasedinta.set(0,rs.getDate("datasedinta"));
            datadocument.set(0,rs.getDate("datadocument"));
            complet = Collections.singletonList(rs.getString("complet"));
            documentsedinta = Collections.singletonList(rs.getString("documentsedinta"));
            numardocument = Collections.singletonList(rs.getString("numardocument"));
            ora = Collections.singletonList(rs.getString("ora"));
            solutie = Collections.singletonList(rs.getString("solutie"));
            solutiesumar = Collections.singletonList(rs.getString("solutiesumar"));
            datadeclarare.set(0,rs.getDate("datadeclarare"));
            partedeclaratoare = Collections.singletonList(rs.getString("partedeclaratoare"));
            tipcaleatac = Collections.singletonList(rs.getString("tipcaleatac"));
            numardosarvechi = rs.getString("numardosarvechi");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new SolrFile(id,iddosar,numardosar,institutie,data,datamodificarii,stadiu,sectie,obiect,
                materie,numeparte,calitateparte,datapronuntare,datasedinta,datadocument,complet,documentsedinta,
                numardocument,ora,solutie,solutiesumar,datadeclarare,partedeclaratoare,tipcaleatac,numardosarvechi);
    }
}

