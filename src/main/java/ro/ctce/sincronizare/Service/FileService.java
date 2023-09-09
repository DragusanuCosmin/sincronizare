package ro.ctce.sincronizare.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ro.ctce.sincronizare.Dao.DosarSolrDao;
import ro.ctce.sincronizare.Dao.SolrFileDao;
import ro.ctce.sincronizare.Entities.SolrFile;

@Service
public class FileService {
    public final DosarSolrDao dosarSolrDao;
    @Autowired
    public FileService(DosarSolrDao dosarSolrDao) {
        this.dosarSolrDao = dosarSolrDao;
    }
    public Page<SolrFile> findByNumardosar(String searchTerm){
        return dosarSolrDao.findByNumardosarContainingCustom(searchTerm, PageRequest.of(0, 10));
    }
}