package ro.ctce.sincronizare.Listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ro.ctce.sincronizare.Entities.Clienti;
import ro.ctce.sincronizare.Entities.SolrFile;
import ro.ctce.sincronizare.Mapper.ClientiRowMapper;
import ro.ctce.sincronizare.Service.FileService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class KafkaMessageListener {
    private final JdbcTemplate jdbcTemplate;
    private final FileService fileService;
    @Autowired
    public KafkaMessageListener(JdbcTemplate jdbcTemplate, FileService fileService) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileService = fileService;
    }
    @KafkaListener(topics = "dosare_noi", groupId = "test-consumer-group")
    public void listen(String nrDosar) {
        final String sql = "SELECT * FROM indexdosare.clienti WHERE nr_dosar=?";
        List<Clienti> clienti = jdbcTemplate.query(sql, new ClientiRowMapper(), nrDosar);
        SolrFile dosar=fileService.findByNumardosar(nrDosar).getContent().get(0);
        ExecutorService executor = Executors.newFixedThreadPool(clienti.size());
        for (Clienti client : clienti) {
            executor.execute(() -> {
                if (exists(nrDosar, client.getBazaDeDate())) {
                    InlocuireDosar(dosar, client);
                } else {
                    AdaugareDosar(dosar, client);
                }
            });
        }
        executor.shutdown();
    }
    public void InlocuireDosar(SolrFile dosar,Clienti clienti) {
        String[] str=convert(dosar.getNumardosar());
        final String sql = "UPDATE "+ clienti.getBazaDeDate()+".dosare SET id=?, userid = ? ,datacreare=?,dataum=?,numarintern=?, dosarinstanta = ? , numardosar = ? , instantadosar = ? ,andosar=?, accesoriidosar=?,solutionatfavorabil = ?, valoaredosar = ?, valoarerecuperata = ?, datafinalizare = ?, idclient = ?, idstare = ?, finalizat = ?,bpublic = ?  , sters = ?  , comentarii = ? , solutiedosar = ? , dataumSCJ = ? , idparteadversa = ?, rezultat = ?,sentintaprimita=? WHERE numardosar=?";
        jdbcTemplate.update(sql,null,clienti.getUserId(),dosar.getData(),dosar.getDatamodificarii(),null,clienti.getUserId(),str[0],str[1],str[2],str[3],null,null,null,null,null,null,null,0,null,0,null,null,"1971-01-01 05:00",null,null,null);
    }
    public void AdaugareDosar(SolrFile dosar,Clienti clienti) {
        String[] str=convert(dosar.getNumardosar());
        final String sql = "UPDATE "+ clienti.getBazaDeDate()+".dosare SET id=?, userid = ? ,datacreare=?,dataum=?,numarintern=?, dosarinstanta = ? , numardosar = ? , instantadosar = ? ,andosar=?, accesoriidosar=?,solutionatfavorabil = ?, valoaredosar = ?, valoarerecuperata = ?, datafinalizare = ?, idclient = ?, idstare = ?, finalizat = ?,bpublic = ?  , sters = ?  , comentarii = ? , solutiedosar = ? , dataumSCJ = ? , idparteadversa = ?, rezultat = ?,sentintaprimita=? WHERE numardosar=?";
        jdbcTemplate.update(sql,null,clienti.getUserId(),dosar.getData(),dosar.getDatamodificarii(),null,clienti.getUserId(),str[0],str[1],str[2],str[3],null,null,null,null,null,null,null,0,null,0,null,null,"1971-01-01 05:00",null,null,null);
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
    public String[] convert(String str) {
            String[] parts = str.split("/");
            if (parts.length >= 4) {
                return Arrays.copyOfRange(parts, 0, 4);
            } else if (parts.length == 3) {
                return new String[]{parts[0], parts[1], parts[2], ""};
            }else if (parts.length == 2) {
                return new String[]{ parts[0], parts[1], "" };
            } else if (parts.length == 1) {
                return new String[]{ parts[0], "", "" };
            } else {
                return new String[]{ "", "", "" };
            }

    }
}
