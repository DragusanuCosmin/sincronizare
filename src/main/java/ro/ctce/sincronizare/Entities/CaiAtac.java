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
public class CaiAtac {
    private String TipCaleatac;
    private String DataDeclarare;
    private String ParteDeclaratoare;
}
