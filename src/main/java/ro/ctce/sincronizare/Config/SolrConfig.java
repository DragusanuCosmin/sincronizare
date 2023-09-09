package ro.ctce.sincronizare.Config;

import lombok.extern.log4j.Log4j2;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@Log4j2
@EnableSolrRepositories(
        basePackages = "ro.ctce.sincronizare.Dao")
public class SolrConfig {
    String solrURL="http://192.168.3.39:8984/solr";

    @Bean
    @Primary
    public SolrClient solrClient() {
        return new ConcurrentUpdateSolrClient.Builder(solrURL)
                .withThreadCount(24)
                .withSocketTimeout(3600000)
                .build();
    }
    @Bean
    public SolrTemplate solrTemplate(SolrClient client) throws Exception {
        return new SolrTemplate(solrClient());
    }

}