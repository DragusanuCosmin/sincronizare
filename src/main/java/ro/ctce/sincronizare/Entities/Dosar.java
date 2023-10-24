package ro.ctce.sincronizare.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Dosar {
    private int id;
    private String userid;
    private Date dataCreare;
    private Date dataUM;
    private String nrIntern;
    private int dosarInstanta;
    private String numarDosar;
    private String instantadosar;
    private String andosar;
    private String accesoriidosar;
    private String solutionatfavorabil;
    private double valoaredosar;
    private double valoarerecuperata;
    private Date dataFinalizare;
    private int idClient;
    private int idStare;
    private boolean finalizat;
    private boolean bPublic;
    private boolean sters;
    private String comentarii;
    private String solutiedosar;
    private Date dataUMSCJ;
    private int idParteaAdversa;
    private int rezultat;
    private String sentintaPrimita;
}
