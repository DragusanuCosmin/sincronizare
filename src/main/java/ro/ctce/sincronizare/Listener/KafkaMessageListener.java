package ro.ctce.sincronizare.Listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ro.ctce.sincronizare.Service.InserareService;

@Component
public class KafkaMessageListener {
    private final InserareService inserareService;
    @Autowired
    public KafkaMessageListener( InserareService inserareService) {
        this.inserareService = inserareService;
    }
    @KafkaListener(topics = "dosare_noi", groupId = "test-consumer-group")
    public void listen(String nrDosar) {
        inserareService.adaugareDatabase(nrDosar);
    }
}
