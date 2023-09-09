package ro.ctce.sincronizare.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class Sedinte {
    private String complet;
    private String dataSedinta;
    private String ora;
    private String solutie;
    private String solutiesumar;
    private String datapronuntare;
    private String documentSedinta;
    private String numardocument;
    private String datadocument;
}
