package ro.ctce.sincronizare.Dao;

import ro.ctce.sincronizare.Entities.Clienti;

public interface ActualizareDao {
     void adaugareDatabase(Clienti clienti);
     void stergereDatabase(String nrDosar);
     void actualizareDatabase(Clienti clienti);
}
