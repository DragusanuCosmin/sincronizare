package ro.ctce.sincronizare.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ro.ctce.sincronizare.Entities.Clienti;
import ro.ctce.sincronizare.Entities.SolrFile;
import ro.ctce.sincronizare.Mapper.ClientiRowMapper;
import ro.ctce.sincronizare.Service.FileService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
public class TestController {
    public final FileService fileService;
    public final JdbcTemplate jdbcTemplate;
    @Autowired
    public TestController(FileService fileService, JdbcTemplate jdbcTemplate) {
        this.fileService = fileService;
        this.jdbcTemplate = jdbcTemplate;
    }
    @GetMapping("/test")
    public void test(@RequestBody String nrDosar) {
        final String sql = "SELECT * FROM indexdosare.clienti WHERE nr_dosar=?";
        List<Clienti> clienti = jdbcTemplate.query(sql, new ClientiRowMapper(), nrDosar);
        SolrFile dosar=fileService.findByNumardosar(nrDosar).getContent().get(0);
        for (Clienti client : clienti) {
            System.out.println(client.getBazaDeDate());
            if(exists(nrDosar,client.getBazaDeDate())) {
                InlocuireDosar(dosar, client.getBazaDeDate());
            }
            else {
                AdaugareDosar(dosar, client.getBazaDeDate());
            }
        }

    }
    public void InlocuireDosar(SolrFile dosar,String bazaDeDate) {
        final String sql = "UPDATE "+bazaDeDate+".dosaresolr SET id=?, iddosar = ? ,institutie=?,data=?,datamodificarii=?, stadiu = ? , sectie = ? , obiect = ? ,materie=?, numeparte=?,calitateparte = ?, datapronuntare = ?, datasedinta = ?, datadocument = ?, complet = ?, documentsedinta = ?, numardocument = ?,ora = ?  , solutie = ?  , solutiesumar = ? , datadeclarare = ? , partedeclaratoare = ? , tipcaleatac = ?, numardosarvechi = ? WHERE nrdosar=?";
        jdbcTemplate.update(sql,dosar.getId(),dosar.getIddosar(),dosar.getInstitutie(),dosar.getData(),dosar.getDatamodificarii(),dosar.getStadiu(),dosar.getSectie(),dosar.getObiect(),dosar.getMaterie(),convertstr(dosar.getNumeparte()),convertstr(dosar.getCalitateparte()),convertdate(dosar.getDatapronuntare()),convertdate(dosar.getDatasedinta()),convertdate(dosar.getDatadocument()),convertstr(dosar.getComplet()),convertstr(dosar.getDocumentsedinta()),convertstr(dosar.getNumardocument()),convertstr(dosar.getOra()),convertstr(dosar.getSolutie()),convertstr(dosar.getSolutiesumar()),convertdate(dosar.getDatadeclarare()),convertstr(dosar.getPartedeclaratoare()),convertstr(dosar.getTipcaleatac()),dosar.getNumardosarvechi(),dosar.getNumardosar());
    }
    public void AdaugareDosar(SolrFile dosar,String bazaDeDate) {
        final String sql="INSERT INTO "+bazaDeDate+".dosaresolr(id,iddosar, numardosar ,institutie,data,datamodificarii, stadiu  , sectie  , obiect ,materie, numeparte,calitateparte, datapronuntare , datasedinta , datadocument , complet, documentsedinta, numardocument ,ora  , solutie  , solutiesumar , datadeclarare , partedeclaratoare , tipcaleatac , numardosarvechi) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,dosar.getId(),dosar.getIddosar(),dosar.getNumardosar(),dosar.getInstitutie(),dosar.getData(),dosar.getDatamodificarii(),dosar.getStadiu(),dosar.getSectie(),dosar.getObiect(),dosar.getMaterie(),convertstr(dosar.getNumeparte()),convertstr(dosar.getCalitateparte()),convertdate(dosar.getDatapronuntare()),convertdate(dosar.getDatasedinta()),convertdate(dosar.getDatadocument()),convertstr(dosar.getComplet()),convertstr(dosar.getDocumentsedinta()),convertstr(dosar.getNumardocument()),convertstr(dosar.getOra()),convertstr(dosar.getSolutie()),convertstr(dosar.getSolutiesumar()),convertdate(dosar.getDatadeclarare()),convertstr(dosar.getPartedeclaratoare()),convertstr(dosar.getTipcaleatac()),dosar.getNumardosarvechi());
    }
    public boolean exists(String nrdosar,String bazaDeDate) {
        String query = "SELECT COUNT(*) FROM "+bazaDeDate+".dosaresolr WHERE numardosar = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, nrdosar);
        return count != null && count > 0;
    }
    public static String convertdate(List<Date> dateList) {
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        List<String> formattedDates = dateList.stream()
                .map(isoDateFormat::format)
                .collect(Collectors.toList());

        return String.join(", ", formattedDates);
    }
    public String convertstr(List<String> str) {
        return String.join(", ", str);
    }
}
