package ro.ctce.sincronizare.Controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ro.ctce.sincronizare.Entities.Clienti;
import ro.ctce.sincronizare.Service.ActualizareService;

@RestController
@RequestMapping("/indexdosare/actualizareclienti")
@Api(tags = "Actualizare Index Dosare Clienti")
public class ActualizareBazeDeDate {
    public final ActualizareService actualizareService;

    @Autowired
    public ActualizareBazeDeDate(ActualizareService actualizareService) {
        this.actualizareService = actualizareService;
    }
    @ApiOperation("Adaugare user nou in Index Dosare")
    @PostMapping("/adaugare")
    public void adaugareDatabase(@RequestBody Clienti clienti){
        actualizareService.adaugareDatabase(clienti);
    }

    @ApiOperation("Stergere user din Index Dosare")
    @PostMapping("/stergere")
    public void stergereDatabase(@RequestParam(value = "numardosar") String numardosar){
        actualizareService.stergereDatabase(numardosar);
    }

    @ApiOperation("Actualizare user din Index Dosare")
    @PostMapping("/actualizare")
    public void actualizareDatabase(@RequestBody Clienti clienti){
        actualizareService.actualizareDatabase(clienti);
    }
}
