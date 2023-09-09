package ro.ctce.sincronizare.Dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import ro.ctce.sincronizare.Entities.SolrFile;

import java.util.List;

@Repository
public interface DosarSolrDao extends SolrCrudRepository<SolrFile, String> {
    @Query("numardosar:?0")
    Page<SolrFile> findByNumardosarContainingCustom(String searchTerm, Pageable pageable);
    List<SolrFile> findByInstitutie(String institutie);

    @Query("numardosar:\"?0\" AND institutie:?1")
    List<SolrFile> findByNumardosarAndInstitutie(String numardosar,String institutie);

    @Query("numardosar:\"?0\" AND institutie:?1")
    List<SolrFile> findByNumardosarAndInstitutie(String numardosar,String institutie, Sort sort);

    @Query("datamodificarii: [?0 TO ?1]")
    List<SolrFile> findByDatamodificarii(String datamodificariistart,String datamodificariiend);
    @Query(fields= {"id","numardosar","institutie"})
    Page<SolrFile> findByInstitutie(String institutie, Pageable page);
}
