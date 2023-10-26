package ro.ctce.sincronizare.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ro.ctce.sincronizare.Dao.InserareDao;
import ro.ctce.sincronizare.Dao.InserareDataAccessService;
import ro.ctce.sincronizare.Entities.Clienti;
import ro.ctce.sincronizare.Entities.SolrFile;
import ro.ctce.sincronizare.Mapper.ClientiRowMapper;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class InserareService {
    public final InserareDao inserareDao;
    private final JdbcTemplate jdbcTemplate;
    public final FileService fileService;
@Autowired
    public InserareService(InserareDao inserareDao, JdbcTemplate jdbcTemplate, FileService fileService) {
    this.inserareDao = inserareDao;
    this.jdbcTemplate = jdbcTemplate;

    this.fileService = fileService;
}

    public String adaugareDatabase(String nrDosar) {
        long startTime = System.nanoTime();
        //ExecutorService executorService = Executors.newFixedThreadPool(2);
//        Future<List<Clienti>> clientiFuture = executorService.submit(() -> {
//            String sql = "SELECT * FROM indexdosare.clienti WHERE nr_dosar=?";
//            return jdbcTemplate.query(sql, new ClientiRowMapper(), nrDosar);
//        });
        //Future<SolrFile> solrFileFuture = executorService.submit(() -> fileService.findByNumardosar(nrDosar.substring(0, nrDosar.length() - 1)).getContent().get(0));
        try {
            //List<Clienti> clienti = clientiFuture.get();
            String sql = "SELECT * FROM indexdosare.clienti WHERE nr_dosar=?";
            List<Clienti> clienti= jdbcTemplate.query(sql, new ClientiRowMapper(), nrDosar);

            SolrFile dosarSolr = fileService.findByNumardosar(nrDosar.substring(0, nrDosar.length() - 1)).getContent().get(0);
            //executorService.shutdown();
            if (clienti.isEmpty()) {
                System.out.println("clienti null");
                return "Eroare401:Clienti inexistenti in baza de date";
            }
            for (Clienti client : clienti) {
                InserareDataAccessService.setClient(client);
                inserareDao.Sincronizare(dosarSolr);
                LogService.log(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Eroare500:Eroare sincronizare";
        }
        long endTime = System.nanoTime();
        long elapsedTimeInMilliseconds = (endTime - startTime) / 1_000_000;
        System.out.println("Time elapsed: " + elapsedTimeInMilliseconds + " ms");

        return "Sincronizare finalizata pentru dosarul " + nrDosar;
    }
}
