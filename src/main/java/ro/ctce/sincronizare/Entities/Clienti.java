package ro.ctce.sincronizare.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Clienti {
    private String userId;
    private String nrDosar;
    private String bazaDeDate;
}
