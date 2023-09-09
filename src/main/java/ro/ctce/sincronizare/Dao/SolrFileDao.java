package ro.ctce.sincronizare.Dao;

import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import ro.ctce.sincronizare.Entities.SolrFile;

@Repository
public interface SolrFileDao extends SolrCrudRepository<SolrFile, String> {
}

