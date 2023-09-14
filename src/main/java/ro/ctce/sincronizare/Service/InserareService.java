package ro.ctce.sincronizare.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.ctce.sincronizare.Dao.InserareDao;
import ro.ctce.sincronizare.Entities.Clienti;

@Service
public class InserareService {
    public final InserareDao inserareDao;
@Autowired
    public InserareService(InserareDao inserareDao) {
        this.inserareDao = inserareDao;
    }

    public void adaugareDatabase(Clienti client) {
        inserareDao.AdaugareDatabase(client);
    }
}
