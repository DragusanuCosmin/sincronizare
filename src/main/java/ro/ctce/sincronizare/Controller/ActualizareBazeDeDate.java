package ro.ctce.sincronizare.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ctce.sincronizare.Entities.Clienti;
import ro.ctce.sincronizare.Service.ActualizareService;

@RestController
@RequestMapping("/indexdosare/actualizareclienti")
public class ActualizareBazeDeDate {
    public final ActualizareService actualizareService;

    @Autowired
    public ActualizareBazeDeDate(ActualizareService actualizareService) {
        this.actualizareService = actualizareService;
    }
    @PostMapping("/adaugare")
    public void adaugareDatabase(@RequestBody Clienti clienti){
        actualizareService.adaugareDatabase(clienti);
    }
    @PostMapping("/stergere")
    public void stergereDatabase(@RequestBody String nrDosar){
        actualizareService.stergereDatabase(nrDosar);
    }
    @PostMapping("/actualizare")
    public void actualizareDatabase(@RequestBody Clienti clienti){
        actualizareService.actualizareDatabase(clienti);
    }
}
