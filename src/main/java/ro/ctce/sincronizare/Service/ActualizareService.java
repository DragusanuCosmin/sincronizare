package ro.ctce.sincronizare.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.ctce.sincronizare.Dao.ActualizareDao;
import ro.ctce.sincronizare.Entities.Clienti;

@Service
public class ActualizareService {
    public final ActualizareDao actualizareDao;

    @Autowired
    public ActualizareService(ActualizareDao actualizareDao) {
        this.actualizareDao = actualizareDao;
    }
    public void adaugareDatabase(Clienti clienti){
        actualizareDao.adaugareDatabase(clienti);
    }
    public void stergereDatabase(String nrDosar){
        actualizareDao.stergereDatabase(nrDosar);
    }
    public void actualizareDatabase(Clienti clienti){
        actualizareDao.actualizareDatabase(clienti);
    }
}
