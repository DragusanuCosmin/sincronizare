package ro.ctce.sincronizare.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.ctce.sincronizare.Service.FileService;
import ro.ctce.sincronizare.Service.InserareService;

@RestController
public class TestController {
    public final FileService fileService;
    public final JdbcTemplate jdbcTemplate;
    public final InserareService inserareService;

    @Autowired
    public TestController(FileService fileService, JdbcTemplate jdbcTemplate, InserareService inserareService) {
        this.fileService = fileService;
        this.jdbcTemplate = jdbcTemplate;
        this.inserareService = inserareService;
    }

    @GetMapping("/adaugare")
    public String adaugareDatabase(@RequestParam(value = "numardosar") String numardosar) {
        return inserareService.adaugareDatabase(numardosar);
    }
}
