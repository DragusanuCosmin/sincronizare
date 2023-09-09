package ro.ctce.sincronizare.Entities;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@SolrDocument(collection = "dosare")
public class SolrFile {
    @Id
    @Field
    @EqualsAndHashCode.Exclude
    String id;
    @EqualsAndHashCode.Exclude
    @Field
    String iddosar;
    @Field
    String numardosar;
    @Field
    String institutie;
    @Field
    Date data;
    @Field
    @EqualsAndHashCode.Exclude
    Date datamodificarii;
    @Field
    String stadiu;
    @Field
    String sectie;
    @Field
    String obiect;
    @Field
    String materie;
    @Field
    List<String> numeparte;
    @Field
    List<String> calitateparte;
    @Field
    List<Date> datapronuntare;
    @Field
    List<Date> datasedinta;
    @Field
    List<Date> datadocument;
    @Field
    List<String> complet;
    @Field
    List<String> documentsedinta;
    @Field
    List<String> numardocument;
    @Field
    List<String> ora;
    @Field
    List<String> solutie;
    @Field
    List<String> solutiesumar;
    @Field
    List<Date> datadeclarare;
    @Field
    List<String> partedeclaratoare;
    @Field
    List<String> tipcaleatac;
    @Field
    String numardosarvechi;
    public void setDataSedintai(int i,Date dataSedinta) {
        this.datasedinta.set(i,dataSedinta);
    }
    public void setDataDocumenti(int i,Date datadocument) {
        this.datadocument.set(i,datadocument);
    }
    public void setDataPronuntarei(int i,Date datapronuntare) {
        this.datapronuntare.set(i,datapronuntare);
    }

}

