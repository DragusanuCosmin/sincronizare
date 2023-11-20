package ro.ctce.sincronizare.Controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.ctce.sincronizare.Service.CautareLogService;

@RestController
@RequestMapping("/logs")
@Api(tags = "Cautare Loguri Dosare actualizate")
public class CautareLogController {
    public final CautareLogService cautareLogService;
    @Autowired
    public CautareLogController(CautareLogService cautareLogService) {
        this.cautareLogService = cautareLogService;
    }
    @GetMapping("/cautareDupaNrDosar")
    @ApiOperation("Cautare Log Dupa Nr Dosar")
    public String cautaLog(@RequestParam(value = "numardosar" ) String numardosar) {
        return cautareLogService.cautareLogNrDosar(numardosar);
    }
    @GetMapping("/cautareDupaIdClient")
    @ApiOperation("Cautare Log Dupa Id Client")
    public String cautaLog2(@RequestParam(value = "idclient") String idclient) {
        return cautareLogService.cautareLogIdClient(idclient);
    }
}
