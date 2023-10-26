package ro.ctce.sincronizare.Dao;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ro.ctce.sincronizare.Entities.Clienti;
import ro.ctce.sincronizare.Entities.SolrFile;
import ro.ctce.sincronizare.Mapper.ClientiRowMapper;
import ro.ctce.sincronizare.Service.FileService;
import ro.ctce.sincronizare.Service.LogService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Repository("InserareDao")
public class InserareDataAccessService implements InserareDao {
    private final JdbcTemplate jdbcTemplate;

    public static void setClient(Clienti client) {
        InserareDataAccessService.client = client;
    }
    private static Clienti client;
    @Autowired
    public InserareDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void Sincronizare(SolrFile dosarSolr){
        try {
            //ExecutorService executorSrvc = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            final List<Map<String, String>> partiList = getMapList(dosarSolr);
            List<Map<String, String>> sedinteList = corectieDataDocument(dosarSolr);
            String materieJustRo = dosarSolr.getMaterie();
            String obiectDosar = dosarSolr.getObiect();
            String dataStadiuDosar = ZonedDateTime.parse(dosarSolr.getData().toString(), DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String sectie = dosarSolr.getSectie();
            //Future<Integer> idInstantaStadiuDosar = executorSrvc.submit(() -> getIdInstantaByDenumirejustro(dosarSolr.getInstitutie()));
            Integer idInstantaStadiuDosar = getIdInstantaByDenumirejustro(dosarSolr.getInstitutie());
            Map<String, String> valSectie = new HashMap<>();
            valSectie.put("denumire", sectie);
            //Future<Integer> idSectie = executorSrvc.submit(() -> saveSectii(valSectie));
            Integer idSectie=saveSectii(valSectie);
            String[] nreDosar = convert(dosarSolr.getNumardosar());
            String numardosar = nreDosar[0];
            String instantadosar = nreDosar[1];
            String andosar = nreDosar[2];
            String accesoriidosar = nreDosar[3];
            //Future<Integer> idMaterie = executorSrvc.submit(() -> saveMaterii(materieJustRo));
            Integer idMaterie = saveMaterii(materieJustRo);
            Map<String, String> valObiect = new HashMap<>();
            valObiect.put("idmaterie", String.valueOf(idMaterie));
            valObiect.put("denumire", obiectDosar);
            //Future<Integer> idobiect = executorSrvc.submit(() -> saveObiecte(valObiect));
            Integer idobiect=saveObiecte(valObiect);
            Map<String, String> valParte = new HashMap<>();
            valParte.put("numeprenume", partiList.get(0).get("nume"));
            valParte.put("societate", partiList.get(0).get("nume"));
            //Future<Integer> idclient = executorSrvc.submit(() -> saveParti(valParte));
            Integer idclient=saveParti(valParte);
            Map<String, String> optStadii = new HashMap<>();
            optStadii.put("denumire", dosarSolr.getStadiu());
            optStadii.put("idmaterie", String.valueOf(idMaterie));
            //Future<Integer> idstadiu = executorSrvc.submit(() -> saveStadii(optStadii));
            Integer idstadiu=saveStadii(optStadii);
            Map<String, String> valCalitate = new HashMap<>();
            valCalitate.put("denumire", partiList.get(0).get("calitateParte"));
            valCalitate.put("idstadiu", String.valueOf(idstadiu));
            valCalitate.put("calitateclient", "0");
            //Future<Integer> idcalitate = executorSrvc.submit(() -> saveCalitati(valCalitate));
            Integer idcalitate = saveCalitati(valCalitate);
            //insert into dosare
            Map<String, String> dosardb = new HashMap<>();
            dosardb.put("id", dosarSolr.getId());
            dosardb.put("userid", client.getUserId());//? de ce aici normal era 1?
            dosardb.put("datacreare", dosarSolr.getId());
            SimpleDateFormat originalDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            originalDateFormat.setTimeZone(TimeZone.getTimeZone("EEST"));
            try {
                dosardb.put("dataum", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(originalDateFormat.parse(now().toString())));
            } catch (ParseException e) {
                System.out.println("Eroare la parsarea datii ultimei actualizari");
            }
            dosardb.put("dosarinstanta", "1");
            dosardb.put("numardosar", numardosar);
            dosardb.put("instantadosar", instantadosar);
            dosardb.put("andosar", andosar);
            if (!accesoriidosar.isEmpty())
                dosardb.put("accesoriidosar", accesoriidosar);
            dosardb.put("solutionatfavorabil", null);
            dosardb.put("idclient", String.valueOf(idclient));
            dosardb.put("idparteadversa", "0");
            dosardb.put("idstare", "1");
            dosardb.put("finalizat", "0");
            dosardb.put("comentarii", "");
            dosardb.put("idinstantaStadiuDosar", String.valueOf(idInstantaStadiuDosar));
            dosardb.put("dataStadiuDosar", dataStadiuDosar);
            dosardb.put("idstadiu", String.valueOf(idstadiu));
            dosardb.put("idsectie", String.valueOf(idSectie));
            Map<String, Object> iduri = saveDosar(dosardb);
            int iddosar = Integer.parseInt(iduri.get("iddosar").toString());
            int idstadiudosar = Integer.parseInt(iduri.get("idstadiudosar").toString());
            Map<String, String> valPD = new HashMap<>();
            valPD.put("idparte", String.valueOf(idclient));
            valPD.put("idcalitate", String.valueOf(idcalitate));
            valPD.put("iddosar", String.valueOf(iddosar));
            valPD.put("idobiect", String.valueOf(idobiect));
            valPD.put("idstadiudosar", String.valueOf(idstadiudosar));
            //executorSrvc.submit(() -> savePartiDosar(valPD));
            savePartiDosar(valPD);
            Map<String, String> valObDosar = new HashMap<>();
            valObDosar.put("iddosar", String.valueOf(iddosar));
            valObDosar.put("idobiect", String.valueOf(idobiect));
            //executorSrvc.submit(() -> (saveObiecteDosar(valObDosar)));
            saveObiecteDosar(valObDosar);
                for (Map<String, String> termen : sedinteList) {
                    Map<String, Object> valTD = new HashMap<>();
                    valTD.put("iddosar", Integer.parseInt(iduri.get("iddosar").toString()));
                    valTD.put("datatermen", termen.get("datasedinta").substring(0, dosarSolr.getData().toString().indexOf('T')) + " " + termen.get("ora"));
                    valTD.put("tipsolutie", termen.get("solutie").replace("'", "").replace("[", "").replace("]", ""));
                    valTD.put("sumarsolutie", termen.get("solutiesumar").replace("'", "").replace("[", "").replace("]", ""));
                    valTD.put("complet", termen.get("complet"));
                    valTD.put("idstadiudosar", String.valueOf(idstadiudosar));
                    valTD.put("datadocument", termen.get("datadocument"));
                    valTD.put("numarDocument", termen.get("numardocument"));
                    //executorSrvc.submit(() ->saveTermen(valTD));
                    saveTermen(valTD);
           }
            System.out.println("Termene salvate");
        }catch (Exception e){
            e.printStackTrace();
        }
        }

    private static List<Map<String, String>> getMapList(SolrFile dosarSolr) {
        List<Map<String, String>> partiList = new ArrayList<>();
        for (int i = 0; i < Math.min(dosarSolr.getCalitateparte().size(), dosarSolr.getNumeparte().size()); i++) {
            String nume = dosarSolr.getNumeparte().get(i);
            String calitateParte = dosarSolr.getCalitateparte().get(i);
            Map<String, String> parti = new HashMap<>();
            parti.put("nume", nume);
            parti.put("calitateParte", calitateParte);
            partiList.add(parti);
        }
        return partiList;
    }


    private static Map<String, String> getStringStringMap(SolrFile dosarSolr, int i) {
        Map<String, String> s = new HashMap<>();
        s.put("complet", dosarSolr.getComplet() != null && dosarSolr.getComplet().size() > i ? dosarSolr.getComplet().get(i) : ".");
        s.put("datasedinta", dosarSolr.getDatasedinta() != null && dosarSolr.getDatasedinta().size() > i ? String.valueOf(dosarSolr.getDatasedinta().get(i)) : ".");
        s.put("ora", dosarSolr.getOra() != null && dosarSolr.getOra().size() > i ? dosarSolr.getOra().get(i) : ".");
        s.put("solutie", dosarSolr.getSolutie() != null && dosarSolr.getSolutie().size() > i ? dosarSolr.getSolutie().get(i) : ".");
        s.put("solutiesumar", dosarSolr.getSolutiesumar() != null && dosarSolr.getSolutiesumar().size() > i ? dosarSolr.getSolutiesumar().get(i) : ".");
        s.put("datapronuntare", dosarSolr.getDatapronuntare() != null && dosarSolr.getDatapronuntare().size() > i ? String.valueOf(dosarSolr.getDatapronuntare().get(i)) : ".");
        s.put("documentsedinta", dosarSolr.getDocumentsedinta() != null && dosarSolr.getDocumentsedinta().size() > i ? dosarSolr.getDocumentsedinta().get(i) : ".");
        s.put("numardocument", dosarSolr.getNumardocument() != null && dosarSolr.getNumardocument().size() > i ? dosarSolr.getNumardocument().get(i) : ".");
        s.put("datadocument", dosarSolr.getDatadocument() != null && dosarSolr.getDatadocument().size() > i ? String.valueOf(dosarSolr.getDatadocument().get(i)) : ".");
        return s;
    }

    public void saveTermen(Map<String, Object> data) {
        try {
            String sql = "SELECT iddosar FROM " + client.getBazaDeDate() + ".stadiidosar WHERE id = ?";
            String idd = jdbcTemplate.queryForObject(sql, String.class, data.get("idstadiudosar"));
            assert idd != null;
            String datatermen = data.get("datatermen").toString();
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("E MMM dd HH:mm:ss z", Locale.ENGLISH);
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

            Date parsedDate;
            String datat = null;

            try {
                dateFormat1.setTimeZone(TimeZone.getTimeZone("EET"));
                parsedDate = dateFormat1.parse(datatermen);
                datat = outputFormat.format(parsedDate);
            } catch (ParseException e1) {
                try {
                    dateFormat2.setTimeZone(TimeZone.getTimeZone("EET"));
                    parsedDate = dateFormat2.parse(datatermen);
                    datat = outputFormat.format(parsedDate);
                } catch (ParseException e2) {
                    try {
                        datat = datatermen;
                    } catch (Exception e3) {
                        e1.printStackTrace();
                        System.out.println("Unable to parse the date: " + datatermen);
                    }
                }
            }
            data.put("datatermen", datatermen);

            String qcomplet = "";
            if (StringUtils.isNotBlank(data.get("complet").toString()) && !"-".equals(data.get("complet").toString())) {
                qcomplet = " AND complet='" + data.get("complet").toString() + "' OR complet IS NULL OR complet='-' OR complet='' ";
            }

            sql = "SELECT count(*) AS cnt FROM " + client.getBazaDeDate() + ".termenedosar WHERE datatermen LIKE '" + datat + "%' AND idstadiudosar=" + data.get("idstadiudosar") + " " + qcomplet;
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class);
            assert cnt != null;
            if (!Objects.equals(cnt, 0)) {
                // Termen already exists, return its id and update if the solution or summary is different
                sql = "SELECT id, datatermen, tipsolutie, sumarsolutie, dataDocument, numarDocument, complet FROM " + client.getBazaDeDate() + ".termenedosar WHERE datatermen LIKE '" + datat + "%' AND idstadiudosar=" + data.get("idstadiudosar") + " " + qcomplet + " ORDER BY id DESC LIMIT 1";
                Map<String, Object> result = jdbcTemplate.queryForMap(sql);

                int id = Integer.parseInt(result.get("id").toString());
                String tipsolutie = (String) result.get("tipsolutie");
                String sumarsolutie = (String) result.get("sumarsolutie");
                String dataDocument = (String) result.get("dataDocument");
                String numarDocument = (String) result.get("numarDocument");
                String complet = (String) result.get("complet");

                System.out.println(tipsolutie+" "+sumarsolutie+" "+dataDocument+" "+numarDocument+" "+complet);
                String datatermenDB = (String) result.get("datatermen");
                int orat = Integer.parseInt(datatermenDB.substring(11, 13));

                if ((StringUtils.isBlank(tipsolutie) && StringUtils.isNotBlank(data.get("tipsolutie").toString())) ||
                        (StringUtils.isBlank(sumarsolutie) && StringUtils.isNotBlank(data.get("sumarsolutie").toString())) ||
                        ("0000-00-00".equals(dataDocument) && StringUtils.isNotBlank(data.get("dataDocument").toString())) ||
                        (StringUtils.isBlank(numarDocument) && StringUtils.isNotBlank(data.get("numarDocument").toString())) ||
                        ((0 != datatermenDB.compareTo(datatermen)) && orat == 0) ||
                        (StringUtils.isBlank(complet) && StringUtils.isNotBlank(data.get("complet").toString()))) {

                    data.put("id", id);
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".termenedosar " +
                            "SET datatermen=?, tipsolutie=?, sumarsolutie=?, " +
                            "dataDocument=?, numarDocument=?, complet=? " +
                            "WHERE id=?";

                    jdbcTemplate.update(
                            updateSql,
                            data.get("datatermen"),
                            data.get("tipsolutie"),
                            data.get("sumarsolutie"),
                            data.get("dataDocument"),
                            data.get("numarDocument"),
                            data.get("complet"),
                            data.get("id")
                    );

                    Map<String, Object> did = new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", idd);
                    did.put("tip", "Modificare termen dosar");
                    did.put("descriere", "termenedosar[" + id + "],stadiidosar[" + data.get("idstadiidosar") + "],info[" + datatermen + " - " + data.get("tipsolutie").toString() + "]");
                    did.put("data", now());

                    String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(:iddosar, :iduser, :tip, :descriere, :data)";
                    jdbcTemplate.update(insertSql, did);

                    // If the dosar is monitored and synchronized, automatically add an alert for the termen
                    int iduser = Integer.parseInt(dosaregetUserId(Integer.parseInt(idd)));
                    String idatentionare = getString(data, Integer.parseInt(idd), id, iduser);
                    if (idatentionare != null) {
                        return;
                    }
                } else {
                    // Conditions for update not met; if datatermen or complet is different, insert a new one
                    if ((datatermen.compareTo(data.get("datatermen").toString()) != 0 && orat > 0) ||
                            (complet.compareTo(data.get("complet").toString()) != 0)) {
                        cnt = 0; // It will go to the if (cnt <= 0) branch and perform an INSERT
                    } else {
                        return;
                    }
                }
            }
            if(Objects.equals(cnt,0))  {
                // Insert
                Integer id = data.get("id") == null ? null : (Integer) data.get("id");
                if (Objects.equals(id, 0)) {
                    data.remove("id");
                    String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".termenedosar(datatermen, tipsolutie, sumarsolutie, idstadiudosar, dataDocument, numarDocument, dataluatlacunostinta, complet, sala, dataIntrare) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertSql,
                            data.get("datatermen"),
                            data.get("tipsolutie"),
                            data.get("sumarsolutie"),
                            data.get("idstadiudosar"),
                            data.get("dataDocument"),
                            data.get("numarDocument"),
                            data.get("dataluatlacunostinta"),
                            data.get("complet"),
                            data.get("sala"),
                            data.get("dataIntrare"));

                    id = getLastInsertID();
                    assert id != null;
                    Map<String, Object> did = new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", idd);
                    did.put("tip", "Adaugare termen dosar");
                    did.put("descriere", "termenedosar[" + id + "],stadiidosar[" + data.get("idstadiudosar") + "],info[" + data.get("datatermen").toString() + " - " + data.get("tipsolutie").toString() + "]");
                    did.put("data", now());

                    String insertSql2 = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertSql2, did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));


                    // If the dosar is monitored and synchronized, automatically add an alert for the termen
                    if (StringUtils.isNotBlank(idd) && !idd.trim().isEmpty()) {
                        int iduser = getUserIdDosar(Integer.parseInt(idd));
                        String idatentionare = getString(data, Integer.parseInt(idd), id, iduser);
                        if (idatentionare != null) return;
                    }
                } else {
                    String updateSql = "update " + client.getBazaDeDate() + ".termenedosar set datatermen=?, tipsolutie=?, sumarsolutie=?, dataDocument=?, numarDocument=?, complet=? WHERE id=?";
                    jdbcTemplate.update(updateSql,
                            data.get("datatermen"),
                            data.get("tipsolutie"),
                            data.get("sumarsolutie"),
                            data.get("dataDocument"),
                            data.get("numarDocument"),
                            data.get("complet"),
                            data.get("id"));

                    Map<String, Object> did = new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", idd);
                    did.put("tip", "Modificare termen dosar");
                    did.put("descriere", "termenedosar[" + id + "],stadiidosar[" + data.get("idstadiudosar") + "],info[" + data.get("datatermen").toString() + " - " + data.get("tipsolutie").toString() + "]");
                    did.put("data", now());

                    String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertSql,
                            did.get("iddosar"),
                            did.get("iduser"),
                            did.get("tip"),
                            did.get("descriere"),
                            did.get("data"));


                }
                return;
                }
            System.out.println("Eroare salvare termene");
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }

    }
    public Integer getLastInsertID(){
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    @Nullable
    private String getString(Map<String, Object> data, int iddosar, int id, int iduser) {
        int esteMonitorizat = dosarisMonitorizat(iddosar, iduser);
        int esteSincronizat = dosarisSincronizat(iddosar, iduser);

        if ((esteMonitorizat + esteSincronizat) > 1 && StringUtils.isNotBlank(data.get("datatermen").toString())) {
            Map<String, Object> optatn = new HashMap<>();
            optatn.put("id", "");
            optatn.put("iddosar", iddosar);
            optatn.put("iduser", iduser);
            optatn.put("deladata", data.get("datatermen"));
            optatn.put("panaladata", data.get("datatermen"));
            optatn.put("descriere", "Termen dosar");
            optatn.put("reminder", -1);
            optatn.put("recurent", "N");
            optatn.put("notificare", 1);

            int idatentionare = saveAtentionari(optatn);
            return "id=" + id + ", iduser=" + iduser + ", idatentionare=" + idatentionare;
        }
        return null;
    }

    public int getUserIdDosar(int id) {
        String sql = "SELECT userid FROM " + client.getBazaDeDate() + ".dosare WHERE id=?";

        // Define a RowMapper to map the result set to a String
        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("userid");

        try {
            // Execute the query using JdbcTemplate
            return Integer.parseInt(Objects.requireNonNull(jdbcTemplate.queryForObject(sql, rowMapper, id)));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int saveAtentionari(Map<String, Object> data) {
        try {
            int userIdIstoric = -1;

            if (data.containsKey("iduser")) {
                userIdIstoric = Integer.parseInt(String.valueOf(data.get("iduser")));
                data.remove("iduser");
            }

            String deladata = (String) data.get("deladata");
            String dt = deladata.substring(0, deladata.indexOf(" "));
            String dataat = String.valueOf(java.sql.Date.valueOf(dt));
            String now = String.valueOf(java.sql.Date.valueOf(java.time.LocalDate.now()));
            int idAt;
            if (dataat.compareTo(now) > 0) {
                String selectCountSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".atentionari WHERE deladata = ? AND panaladata = ? AND iddosar = ? AND reminder = -1 AND recurent = 'N' AND notificare = 1 AND userid IS NULL";
                Integer cnt = jdbcTemplate.queryForObject(selectCountSql, Integer.class, dataat, dataat, data.get("iddosar"));
                if (!Objects.equals(cnt, 0)) {
                    // Update existing atentionare
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".atentionari WHERE deladata = ? AND panaladata = ? AND iddosar = ? AND reminder = -1 AND recurent = 'N' AND notificare = 1 AND userid IS NULL";
                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class, dataat, dataat, data.get("iddosar"));
                    assert id != null;
                    idAt = id;
                } else {
                    // Insert new atentionare
                    String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".atentionari(iddosar, descriere, deladata, panaladata, notificare, reminder, recurent, userid, gcal_eventID, dataluatlacunostinta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)";
                    jdbcTemplate.update(insertSql, data.get("iddosar"), data.get("descriere"), dataat, dataat, data.get("notificare"), data.get("reminder"), data.get("recurent"), data.get("userid"), data.get("gcal_eventID"));
                    Integer id = getLastInsertID();
                    assert id != null;
                    idAt = id;
                    // Insert into istoricdosare
                    String nowTimestamp = java.time.LocalDateTime.now().toString();
                    Map<String, Object> istoricDosareData = new HashMap<>();
                    istoricDosareData.put("iddosar", data.get("iddosar"));
                    istoricDosareData.put("iduser", userIdIstoric);
                    istoricDosareData.put("tip", "Adaugare atentionare dosar S");
                    istoricDosareData.put("descriere", "atentionari[" + idAt + "],info[" + data.get("deladata") + " - " + data.get("descriere") + "]");
                    istoricDosareData.put("data", nowTimestamp);
                    String insertIstoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES (?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertIstoricSql, istoricDosareData.get("iddosar"), istoricDosareData.get("iduser"), istoricDosareData.get("tip"), istoricDosareData.get("descriere"), istoricDosareData.get("data"));
                }

                // Handle notificare logic
                if ("1".equals(data.get("notificare"))) {
                    // Insert into Notificari (silent, no response needed; all checks are done on the Notification server)
                    // Get dosar number
                    String iddosar = data.get("iddosar").toString();
                    String nrdosar = "";
                    String partiDosar = "";
                    String clientDosar = "";
                    String parteAdversa = "";
                    String denobiectdosar = "";
                    String observatii = "";
                    String descriere = data.get("descriere").toString().replace("\n", " ");
                    int userid=0;
                    if (!iddosar.isEmpty()) {
                        nrdosar = getNrUnicDosar5(iddosar);
                        partiDosar = getListaPartiDosar(iddosar);
                        clientDosar = getDenumireClientDosar(iddosar).replace("\n", " ");
                        parteAdversa = getDenumireParteAdversaDosar(iddosar).replace("\n", " ");
                        int idobiectdosar = getIdObiectRecent(iddosar);
                        denobiectdosar = getDenumireByIdObiecte(idobiectdosar).replace("\n", " ");
                        observatii = getObservatiiDosar(iddosar).replace("\n", " ");
                        userid = getUserIdDosar(Integer.parseInt(iddosar));
                    }

                    StringBuilder mesaj = new StringBuilder("<font face=\"tahoma\">");
                    mesaj.append("Dosar nr. <b>").append(nrdosar).append("</b><br/>");
                    mesaj.append(descriere).append(": <b>").append(data.get("deladata")).append("</b><br/>");
                    if (!denobiectdosar.trim().isEmpty()) {
                        mesaj.append("Obiect dosar: <b>").append(denobiectdosar).append("</b><br/>");
                    }
                    if (!clientDosar.trim().isEmpty()) {
                        mesaj.append("Client dosar: <b>").append(clientDosar).append("</b><br/>");
                    }
                    if (!parteAdversa.trim().isEmpty()) {
                        mesaj.append("Parte adversă: <b>").append(parteAdversa).append("</b><br/>");
                    }
                    if (!observatii.trim().isEmpty()) {
                        mesaj.append("Observații: <b>").append(observatii).append("</b><br/>");
                    }
                    mesaj.append("Părți dosar: <b><br/>").append(partiDosar).append("</b><br/><br/></font>");

                    String sms = "Dosarul nr. " + nrdosar + " - are termen în data: " + data.get("deladata");

                    // Cleanup
                    mesaj = new StringBuilder(mesaj.toString().replace("'", ""));
                    sms = sms.replace("'", "");

                    // Encoding base64 for diacritics
                    mesaj = new StringBuilder(Base64.getEncoder().encodeToString(mesaj.toString().getBytes(StandardCharsets.UTF_8)));
                    sms = Base64.getEncoder().encodeToString(sms.getBytes(StandardCharsets.UTF_8));

                    List<Map<String, Object>> atentionari = new ArrayList<>();
                    // Get observatii dosar
                    // data.get("iddosar")
                    Map<String, Object> attn = new HashMap<>();
                    attn.put("mesaj", mesaj);
                    attn.put("sms", sms);
                    attn.put("dataAtentionare", dataat);
                    attn.put("idAtentionare", idAt);
                    attn.put("reminder", data.get("reminder"));
                    attn.put("recurent", data.get("recurent"));

                    atentionari.add(attn);

                    setNotificariAtentionari(atentionari, userid);
                    System.out.println("Atentionari salvate");
                }

            }
        }
        catch(Exception e)
    {
        e.printStackTrace();
        System.out.println("Eroare la adaugare atentionare: " + e.getMessage());
    }
        return -1;
}
    public void setNotificariAtentionari(List<Map<String,Object>> atentionari, int userId) {
        String licenta = getLicenta();
        String username = (userId != 0) ? getUsernameById(userId) : "";

        String soapEndpointUrl = "https://dosare.ctce.ro/Notificari/listenerws.php";
        String soapAction = "setNotificare";

        try {
            URL url = new URL(soapEndpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the necessary HTTP request headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            connection.setRequestProperty("SOAPAction", soapAction);
            connection.setDoOutput(true);

            // Construct the SOAP request payload
            String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://web.com/\">\n"
                    + "   <soapenv:Header/>\n"
                    + "   <soapenv:Body>\n"
                    + "      <web:setNotificare>\n"
                    + "         <licenta>" + licenta + "</licenta>\n"
                    + "         <denumire>" + username + "</denumire>\n"
                    + "         <atentionari>" + atentionari + "</atentionari>\n"
                    + "      </web:setNotificare>\n"
                    + "   </soapenv:Body>\n"
                    + "</soapenv:Envelope>";

            // Send the SOAP request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = soapRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the SOAP response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse and return the response here
            } else {
                // Handle the error
            }
        } catch (Exception e) {
            // Handle exceptions here
            e.printStackTrace();
            e.getMessage();
        }
    }
    public String getUsernameById(int id) {

        if (id == 0) {
            return null;
        }

        String sql = "SELECT name FROM " + client.getBazaDeDate() + ".users WHERE id = ?";

        try {
            String name = jdbcTemplate.queryForObject(sql, String.class,id);
            if (name != null) {
                return name;
            }
        } catch (Exception e) {
           e.printStackTrace();
           return null;
        }

        return null;
    }

    public String getLicenta() {
        String sql = "SELECT serialno FROM " + client.getBazaDeDate() + ".status";
        List<String> results = jdbcTemplate.queryForList(sql, String.class);

        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            return null;
        }
    }

        public String getObservatiiDosar(String iddosar) {

        if (Integer.parseInt(iddosar) == 0) {
            return null;
        }

        String sql = "SELECT comentarii FROM " + client.getBazaDeDate() + ".dosare WHERE id = ?";

        try {
            String observatii = jdbcTemplate.queryForObject(sql, String.class,iddosar);
            if (observatii != null) {
                return observatii;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null; // Return null if no result is found
    }

    public String getDenumireByIdObiecte(int id) {

        if (id == 0) {
            return null;
        }

        String sql = "SELECT denumire FROM " + client.getBazaDeDate() + ".obiecte WHERE id = ?";

        try {
            String denumire = jdbcTemplate.queryForObject(sql, String.class,id);
            if (denumire != null) {
                return denumire;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public int getIdObiectRecent( String iddosar) {
        String sql = "SELECT idobiect FROM " + client.getBazaDeDate() + ".obiectedosar WHERE iddosar = ? ORDER BY id DESC LIMIT 1";

        try {
            Integer idobiect = jdbcTemplate.queryForObject(sql, Integer.class,iddosar);
            if (idobiect != null) {
                return idobiect;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return 0;
    }
    private List<Map<String,String>> corectieDataDocument(SolrFile dosarSolr) {
        int cnt1 = dosarSolr.getOra() != null ? dosarSolr.getOra().size() : 0;
        int cnt2 = dosarSolr.getDatadocument() != null ? dosarSolr.getDatadocument().size() : 0;
        int cnt3 = dosarSolr.getSolutie() != null ? dosarSolr.getSolutie().size() : 0;
        int cnt4 = dosarSolr.getSolutiesumar() != null ? dosarSolr.getSolutiesumar().size() : 0;
        int cnt5 = dosarSolr.getNumardocument() != null ? dosarSolr.getNumardocument().size() : 0;
        int cnt6 = dosarSolr.getDatapronuntare() != null ? dosarSolr.getDatapronuntare().size() : 0;
        int cnt7 = dosarSolr.getDocumentsedinta() != null ? (dosarSolr.getDocumentsedinta()).size() : 0;
        int cnt8 = dosarSolr.getDatasedinta() != null ? dosarSolr.getDatasedinta().size() : 0;
        int cnt9 = dosarSolr.getComplet() != null ? dosarSolr.getComplet().size() : 0;
        List<Map<String,String>> sedinteList = new ArrayList<>();
        for (int i = 0; i < min(Arrays.asList(cnt1, cnt2, cnt3, cnt4, cnt5, cnt6, cnt7, cnt8, cnt9)); i++) {
            if (dosarSolr.getDatasedinta().get(i).toString().contains("1900-01"))
                dosarSolr.setDataSedintai(i, new Date());
            if (dosarSolr.getDatadocument().get(i).toString().contains("1900-01"))
                dosarSolr.setDataDocumenti(i, new Date());
            if (dosarSolr.getDatapronuntare().get(i).toString().contains("1900-01"))
                dosarSolr.setDataPronuntarei(i, new Date());
            final Map<String, String> s = getStringStringMap(dosarSolr, i);
            sedinteList.add(s);
        }
        return sedinteList;
    }

    public String getDenumireParteAdversaDosar( String id) {

        if (Integer.parseInt(id) == 0) {
            return "nu este parte";
        }

        String sql = "SELECT idparteadversa FROM " + client.getBazaDeDate() + ".dosare WHERE id = ?";

        try {
            Integer idparteadversa = jdbcTemplate.queryForObject(sql, Integer.class,id);
            if (idparteadversa != null) {
                Map<String, Object> dateparteadversa = findSimpluParti(String.valueOf(idparteadversa));
                if (dateparteadversa != null) {
                    if (dateparteadversa.containsKey("societate") && !dateparteadversa.get("societate").toString().trim().isEmpty()) {
                        return dateparteadversa.get("societate").toString().trim();
                    } else if (dateparteadversa.containsKey("numeprenume")) {
                        return dateparteadversa.get("numeprenume").toString().trim();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return "nu este parte";
    }
    public String getDenumireClientDosar( String id) {

        if (Integer.parseInt(id) == 0) {
            return null;
        }

        String sql = "select idclient from " + client.getBazaDeDate() + ".dosare where id = ?";

        try {
            Integer idclient = jdbcTemplate.queryForObject(sql, Integer.class, id);
            Map<String, Object> dateclient = findSimpluParti(String.valueOf(idclient));
            String denclient = "";
            if (dateclient != null) {
                if (dateclient.containsKey("societate") && !dateclient.get("societate").toString().trim().isEmpty()) {
                    denclient = dateclient.get("societate").toString().trim();
                } else if (dateclient.containsKey("numeprenume")) {
                    denclient = dateclient.get("numeprenume").toString().trim();
                }
            }
            return denclient;
        } catch (Exception e) {
            return null;
        }
    }


    public String getNrUnicDosar5( String iddosar) {

        String sql = "SELECT numarintern, numardosar, instantadosar, andosar, accesoriidosar, datacreare FROM "+client.getBazaDeDate()+".dosare where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String numarintern = rs.getString("numarintern");
                String numardosar = rs.getString("numardosar");
                String instantadosar = rs.getString("instantadosar");
                String andosar = rs.getString("andosar");
                String accesoriidosar = rs.getString("accesoriidosar");
                String datacreare = rs.getString("datacreare");

                String nrinst = "";
                if (numardosar == null || instantadosar == null || andosar == null) {
                    if (numarintern != null && !numarintern.trim().isEmpty()) {
                        nrinst = numarintern.trim(); // ."/"+andc;
                    }
                } else {
                    nrinst = (numardosar.trim().isEmpty() ? "--" : numardosar.trim()) + "/";
                    nrinst += (instantadosar.trim().isEmpty() ? "--" : instantadosar.trim()) + "/";
                    nrinst += (andosar.trim().isEmpty() ? "----" : andosar.trim());
                    nrinst += (accesoriidosar != null && !accesoriidosar.trim().isEmpty() ? "/" + accesoriidosar.trim() : "");
                }
                return nrinst;
            },iddosar);
        } catch (Exception e) {
            return null;
        }
    }

    public String dosaregetUserId(int id) {
        String sql = "SELECT userid FROM " + client.getBazaDeDate() + ".dosare WHERE id = ?";
        Object[] params = new Object[]{id};

        try {
            return jdbcTemplate.queryForObject(sql, String.class,params);
        } catch (Exception e) {
            System.err.println("Error selecting dosar (2): " + e.getMessage());
            return null;
        }
    }
    public Object saveObiecteDosar(Map<String, String> data) {
        if (!data.get("iddosar").isEmpty() && !data.get("idobiect").isEmpty()) {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + client.getBazaDeDate() + ".obiectedosar WHERE iddosar=? AND idobiect=?",
                    Integer.class,
                    data.get("iddosar"), data.get("idobiect"));
            if (!Objects.equals(cnt, 0)) {
                return jdbcTemplate.queryForObject(
                        "SELECT id FROM " + client.getBazaDeDate() + ".obiectedosar WHERE iddosar=? AND idobiect=?",
                        Integer.class, data.get("iddosar"), data.get("idobiect"));
            } else {
                    if (data.get("id")==null)
                        data.remove("id");
                    //TODO de facut astea la toate
                    String insertQuery = "INSERT INTO " + client.getBazaDeDate() + ".obiectedosar(iddosar, idobiect) VALUES(?, ?)";
                    jdbcTemplate.update(insertQuery, data.get("iddosar"), data.get("idobiect"));
                    Integer idod = getLastInsertID();
                    Map<String, Object> did=new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", data.get("iddosar"));
                    did.put("tip", "Adaugare obiect dosar");
                    did.put("descriere", "obiectedosar[" + idod + "],obiecte[" + data.get("idobiect") + "]");
                    did.put("data", now());

                    jdbcTemplate.update(
                            "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)",
                            did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));
                    System.out.println("Obiecte salvate");
                    return idod;
            }
        }
        System.out.println("Eroare salvare obiecte");
        return null;
    }
    public Integer savePartiDosar(Map<String,String> valPD){
        if (!valPD.get("iddosar").isEmpty() && !valPD.get("idobiect").isEmpty()) {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + client.getBazaDeDate() + ".partidosar WHERE idparte=? AND idcalitate=? AND idstadiudosar=?",
                    Integer.class,
                    valPD.get("idparte"), valPD.get("idcalitate"),valPD.get("idstadiudosar"));

            if (!Objects.equals(cnt, 0)) {
                return jdbcTemplate.queryForObject(
                        "SELECT id FROM " + client.getBazaDeDate() + ".partidosar WHERE idparte=? AND idcalitate=? AND idstadiudosar=? LIMIT 1",
                        Integer.class, valPD.get("iddosar"), valPD.get("idobiect"));
            } else {
                String insertQuery = "INSERT INTO " + client.getBazaDeDate() + ".partidosar(idparte, idcalitate, idstadiudosar) VALUES(?, ?,?)";
                System.out.println(valPD.get("idstadiudosar")+" "+valPD.get("idparte")+" "+valPD.get("idcalitate"));
                jdbcTemplate.update(insertQuery, valPD.get("idparte"), valPD.get("idcalitate"),valPD.get("idstadiudosar"));
                Integer idod = getLastInsertID();
                Map<String, Object> did=new HashMap<>();
                did.put("iduser", client.getUserId());
                did.put("iddosar", 0);
                did.put("tip", "Adaugare parte dosar");
                did.put("descriere", "partidosar["+idod+"],parti["+valPD.get("idparte")+"],stadiidosar["+valPD.get("idstadiudosar"));
                did.put("data", now());

                jdbcTemplate.update(
                        "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)",
                        did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));
                System.out.println("Parti salvate");
                return idod;
            }
        }
        System.out.println("Eroare salvare parti");
        return null;
    }
    public Map<String, Object> saveDosar(Map<String, String> data) {
        String query;
        Integer cnt;
            query = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".dosare WHERE numardosar=? AND instantadosar=? AND andosar=? " +
                    "AND accesoriidosar=? AND sters=0";
            cnt=jdbcTemplate.queryForObject(query, Integer.class, data.get("numardosar"), data.get("instantadosar"), data.get("andosar"), data.getOrDefault("accesoriidosar",""));
        try {
            if (!Objects.equals(cnt, 0)) {
                query = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar=? AND idinstanta=? AND idstadiu=?";
                cnt=jdbcTemplate.queryForObject(query, Integer.class, data.get("id"), data.get("instantadosar"), data.get("idstadiu"));
                Map<String, Object> sd = new HashMap<>();
                if (!Objects.equals(cnt, 0)) {
                    sd.put("iddosar", data.get("id"));
                    sd.put("idstadiu", data.get("idstadiu"));
                    sd.put("data", data.get("dataStadiuDosar"));
                    sd.put("idsectie", data.get("idsectie"));
                    sd.put("judecatori", "");
                    sd.put("idinstanta", data.get("instantadosar"));
                    String idstadiudosar = saveStadiiDosar(sd);
                    query = "SELECT dataum FROM " + client.getBazaDeDate() + ".dosare WHERE numardosar=? AND instantadosar=? AND andosar=? AND accesoriidosar=? AND dataum<?";
                    String dataum = jdbcTemplate.queryForObject(query, String.class, data.get("numardosar"), data.get("instantadosar"), data.get("andosar"), data.getOrDefault("accesoriidosar", ""), data.get("dataum"));
                    if (!Objects.equals(dataum, "")) {
                        Map<String, Object> dumData = new HashMap<>();
                        dumData.put("id", "");
                        dumData.put("dataum", data.get("dataum"));
                        dumData.put("flagpjr", "1");
                        dumData.put("flagscj", "");
                        dumData.put("numardosar", data.get("numardosar"));
                        dumData.put("instantadosar", data.get("instantadosar"));
                        dumData.put("andosar",data.get("andosar"));
                        dumData.put("accesoriidosar", data.getOrDefault("accesoriidosar",""));
                        actualizareDataum(dumData);
                    }
                    Map<String, Object> rv = new HashMap<>();
                    rv.put("iddosar", data.get("id"));
                    rv.put("idstadiudosar", idstadiudosar);
                    rv.put("dosarnou", "0");
                    return rv;
                } else {
                    System.out.println(data.get("id")+" "+data.get("instantadosar")+" "+data.get("idstadiu"));
                    query="SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar=? AND idinstanta=? AND idstadiu=?";
                    cnt = jdbcTemplate.queryForObject(query, Integer.class, data.get("id"), data.get("instantadosar"), data.get("idstadiu"));
                    if(!Objects.equals(cnt,0)){
                        query = "SELECT id FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar=? AND idinstanta=? AND idstadiu=? LIMIT 1";
                        String idstadiudosar = jdbcTemplate.queryForObject(query, String.class, data.get("id"), data.get("instantadosar"), data.get("idstadiu"));
                        Map<String, Object> rv = new HashMap<>();
                        rv.put("iddosar", data.get("id"));
                        rv.put("idstadiudosar", idstadiudosar);
                        rv.put("dosarnou", "0");
                        return rv;
                    }
                    else{
                        query = "INSERT INTO " + client.getBazaDeDate() + ".stadiidosar(iddosar, idinstanta, idstadiu,data,judecatori,idsectie) VALUES(?, ?, ?,?,?,?)";
                        jdbcTemplate.update(query, data.get("id"), data.get("instantadosar"), data.get("idstadiu"),new Date(),"",data.get("idsectie"));
                        Map<String, Object> rv = new HashMap<>();
                        String idstadiudosar= String.valueOf(getLastInsertID());
                        rv.put("iddosar", data.get("id"));
                        rv.put("idstadiudosar", idstadiudosar);
                        rv.put("dosarnou", "0");
                        return rv;
                    }
                }
            }
            else {
                Map<String, Object> dataDosar = new HashMap<>();
                dataDosar.put("userid", client.getUserId());
                dataDosar.put("datacreare", data.get("datacreare"));
                dataDosar.put("dosarinstanta", data.get("dosarinstanta"));
                dataDosar.put("numardosar", data.get("numardosar"));
                dataDosar.put("instantadosar", data.get("instantadosar"));
                dataDosar.put("andosar", data.get("andosar"));
                dataDosar.put("accesoriidosar", data.get("accesoriidosar"));
                dataDosar.put("idclient", data.get("idclient"));
                dataDosar.put("idparteadversa", data.get("idparteadversa"));
                dataDosar.put("idstare", data.get("idstare"));
                dataDosar.put("finalizat", data.get("finalizat"));
                dataDosar.put("bpublic", data.get("bpublic"));
                dataDosar.put("rezultat", data.get("rezultat"));
                dataDosar.put("sentitaprimita", data.get("sentitaprimita"));
                dataDosar.put("solutiedosar", data.get("solutiedosar"));
                dataDosar.put("datafinalizare", data.get("datafinalizare"));
                dataDosar.put("valoaredosar", data.get("valoaredosar"));
                dataDosar.put("valoarerecuperata", data.get("valoarerecuperata"));
                Integer idod;
                if (Objects.equals(data.get("id"), "")) {
                    query="INSERT INTO  " + client.getBazaDeDate() + ".dosare(userid, datacreare, dosarinstanta, numardosar, instantadosar, andosar, accesoriidosar, idclient, idparteadversa, idstare, finalizat, comentarii) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
                    jdbcTemplate.update(query, client.getUserId(), dataDosar.get("datacreare"), dataDosar.get("dosarinstanta"),dataDosar.get("numardosar"),dataDosar.get("instantadosar"),dataDosar.get("andosar"),dataDosar.get("accesoriidosar"),dataDosar.get("idclient"),dataDosar.get("idparteadversa"),dataDosar.get("idstare"),dataDosar.get("finalizat"),dataDosar.get("comentarii"));
                    idod=getLastInsertID();
                    Map<String, Object> did = new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", idod);
                    did.put("tip", data.get("Adaugare dosar *"));
                    did.put("descriere", "dosare[" + idod + "],users[" + data.get("userid") + "],info[" + data.get("numardosar") + "/"
                            + data.get("instantadosar") + "/" + data.get("andosar") + "/" + data.get("accesoriidosar") + " | "
                            + data.get("numarintern") + "]");
                    did.put("data", now());
                    query="INSERT INTO  " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?,?,?,?,?)";
                    jdbcTemplate.update(query, did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));
                }
                else{
                    idod= Integer.valueOf(data.get("id"));
                    query="UPDATE " + client.getBazaDeDate() + ".dosare SET id=?, userid=?, datacreare=?, dataum=?, dataumSCJ=?, dosarinstanta=?, numardosar=?, instantadosar=?, andosar=?, accesoriidosar=?, solutionatfavorabil=?, solutiedosar=?, valoaredosar=?, valoarerecuperata=?, datafinalizare=?, idclient=?, idparteadversa=?, idstare=?, finalizat=?, bpublic=?, comentarii=?, rezultat=?, sentintaprimita=? WHERE id=?";
                    jdbcTemplate.update(query, idod, dataDosar.get("userid"), dataDosar.get("datacreare"), dataDosar.get("dataum"), dataDosar.get("dataumSCJ"),dataDosar.get("dosarinstanta"),dataDosar.get("numardosar"),dataDosar.get("instantadosar"),dataDosar.get("andosar"),dataDosar.get("accesoriidosar"),dataDosar.get("solutionatfavorabil"),dataDosar.get("solutiedosar"),dataDosar.get("valoaredosar"),dataDosar.get("valoarerecuperata"),dataDosar.get("datafinalizare"),dataDosar.get("idclient"), dataDosar.get("idparteadversa"),dataDosar.get("idstare"),dataDosar.get("finalizat"),dataDosar.get("bpublic"),dataDosar.get("comentarii"),dataDosar.get("rezultat"),dataDosar.get("sentintaprimita"),dataDosar.get("id"));
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("iddosar", idod);
                    paramMap.put("iduser", client.getUserId());
                    paramMap.put("tip", "Actualizare dosar *");
                    paramMap.put("descriere", "dosare[" + idod + "],info[" + data.get("numardosar") + "/"
                            + data.get("instantadosar") + "/" + data.get("andosar") + "/" + data.get("accesoriidosar") + " | "
                            + data.get("numarintern") + "]");
                    paramMap.put("data", now());
                    query = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                            "VALUES(?,?,?,?,?)";
                    jdbcTemplate.update(query, paramMap.get("iddosar"), paramMap.get("iduser"), paramMap.get("tip"), paramMap.get("descriere"), paramMap.get("data"));
                }

                Map<String, Object> sd = new HashMap<>();
                sd.put("id", "");
                sd.put("iddosar", idod);
                sd.put("idstadiu", data.get("idstadiu"));
                sd.put("data", data.get("dataStadiuDosar"));
                sd.put("idinstanta", data.get("idinstantaStadiuDosar"));
                sd.put("idsectie", data.get("idsectie"));
                sd.put("judecatori", "");

                String idstadiudosar = saveStadiiDosar(sd);
                Map<String, Object> rv = new HashMap<>();
                rv.put("iddosar", idod);
                rv.put("idstadiudosar", idstadiudosar);
                rv.put("dosarnou", "1");
                System.out.println("Dosar salvat");
                return rv;
            }

        } catch (Exception e) {
            System.out.println("Eroare salvare dosar");
            e.printStackTrace();
            return null;
        }
    }

    private String saveStadiiDosar(Map<String, Object> data) {
        try {

            if (data.containsKey("iddosar") && data.containsKey("idstadiu") &&
                    data.containsKey("data") && data.containsKey("idinstanta")) {

                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar = ? AND idstadiu = ? " +
                        "AND data = ? AND idinstanta = ? AND idsectie = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        data.get("iddosar"), data.get("idstadiu"), data.get("data"),
                        data.get("idinstanta"), data.get("idsectie"));

                if (!Objects.equals(cnt, 0)) {
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar = ? AND idstadiu = ? " +
                            "AND data = ? AND idinstanta = ? AND idsectie = ? LIMIT 1";
                    return jdbcTemplate.queryForObject(selectIdSql, String.class,
                            data.get("iddosar"), data.get("idstadiu"), data.get("data"),
                            data.get("idinstanta"), data.get("idsectie"));
                } else {
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".stadiidosar(iddosar, idstadiu, data, idinstanta, idsectie, judecatori) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertSql, data.get("iddosar"), data.get("idstadiu"),
                                data.get("data"), data.get("idinstanta"), data.get("idsectie"), data.get("judecatori"));

                        Integer idsd = getLastInsertID();
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", data.get("iddosar"));
                        did.put("tip", "Adaugare stadiu dosar");
                        did.put("descriere", "stadiidosar[" + idsd.toString() + "],stadii[" + data.get("idstadiu") +
                                "],instante[" + data.get("idinstanta") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));
                        return idsd.toString();
                    }
                }
            else {
                System.out.println("Eroare salvare stadii");
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Error occurred
        }
    }

    public int saveStadii(Map<String,String> optStadii){
        try {

            if (optStadii.containsKey("denumire") && optStadii.containsKey("idmaterie")) {

                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".stadii WHERE denumire = ? AND idmaterie = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        optStadii.get("denumire"), optStadii.get("idmaterie"));

                if (!Objects.equals(cnt, 0)) {
                    // Stadii already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".stadii WHERE denumire = ? AND idmaterie = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            optStadii.get("denumire"), optStadii.get("idmaterie"));

                    assert id != null;
                    System.out.println("Stadii salvate");
                    return id;
                } else {
                    // Stadii with provided parameters doesn't exist, insert it
                    String id =  optStadii.get("id");
                    if (id == null) {
                        optStadii.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".stadii(idmaterie, denumire) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, optStadii.get("idmaterie"), optStadii.get("denumire"));

                        Integer ids = getLastInsertID();
                        assert ids != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Adaugare stadiu");
                        did.put("descriere", "stadii[" + ids + "],materii[" + optStadii.get("idmaterie") +
                                "],info[" + optStadii.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));
                        System.out.println("Stadii salvate");
                        return ids;
                    } else {
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".stadii SET denumire = ?, idmaterie = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, optStadii.get("denumire"), optStadii.get("idmaterie"), id);

                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare stadiu");
                        did.put("descriere", "stadii[" + id + "],materii[" + optStadii.get("idmaterie") +
                                "],info[" + optStadii.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));
                        System.out.println("Stadii salvate");
                        return Integer.parseInt(id);
                    }
                }
            } else {
                System.out.println("Eroare salvarii stadii");
                return 0; // Cannot insert without both parameters
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error occurred
        }

    }
    public int saveCalitati(Map<String,String> data){
        try {

            if (data.containsKey("denumire") && data.containsKey("idstadiu")) {

                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".calitati WHERE denumire = ? AND idstadiu = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        data.get("denumire"), data.get("idstadiu"));

                if (!Objects.equals(cnt, 0)) {
                    // Entry already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".calitati WHERE denumire = ? AND idstadiu = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            data.get("denumire"), data.get("idstadiu"));
                    System.out.println("Calitati salvate");
                    assert id != null;
                    return id;
                } else {
                    // Doesn't exist, insert it
                    String idd = data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".calitati(denumire, idstadiu, calitateclient) " +
                                "VALUES (?, ?, ?)";

                        jdbcTemplate.update(insertSql, data.get("denumire"), data.get("idstadiu"), data.get("calitateclient"));

                        Integer idc = getLastInsertID();
                        assert idc != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Adaugare calitate");
                        did.put("descriere", "calitati[" + idc + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));
                        System.out.println("Calitati salvate");
                        return idc;
                    } else {
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".calitati SET denumire = ?, idstadiu = ?, calitateclient = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("denumire"), data.get("idstadiu"), data.get("calitateclient"), idd);
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare calitate");
                        did.put("descriere", "calitati[" + idd + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));
                        System.out.println("Calitati salvate");
                        return Integer.parseInt(idd);
                    }
                }
            } else {
                System.out.println("Eroare salvare calitati");
                return 0; // Cannot insert without both parameters
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error occurred
        }

    }
    public int saveObiecte(Map<String,String> data){
        try {
            // Check for duplicate data
            if (data.containsKey("denumire") && data.containsKey("idmaterie")) {
                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".obiecte WHERE denumire = ? AND idmaterie = ? LIMIT 1";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        data.get("denumire"), data.get("idmaterie"));

                if (!Objects.equals(cnt, 0)) {
                    // Entry with the same denumire and idmaterie already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".obiecte WHERE denumire = ? AND idmaterie = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            data.get("denumire"), data.get("idmaterie"));
                    assert id != null;
                    System.out.println("Obiecte salvate");
                    return id;
                } else {
                    // Insert new denumire/idmaterie or update existing
                    if (data.get("id")==null||data.get("id").isEmpty()) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".obiecte(idmaterie, denumire) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, data.get("idmaterie"), data.get("denumire"));

                        Integer insertedId = getLastInsertID();
                        assert insertedId != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare calitate");
                        did.put("descriere", "obiecte[" + insertedId + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip,descriere,data) " +
                                "VALUES (?, ?, ?,?,?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));
                        System.out.println("Obiecte salvate");

                        return insertedId;
                    } else {
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".obiecte SET idmaterie = ?, denumire = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("idmaterie"), data.get("denumire"), data.get("id"));

                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare calitate");
                        did.put("descriere", "obiecte[" + data.get("id") + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip,descriere,data) " +
                                "VALUES (?, ?, ?,?,?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"),did.get("iduser"), did.get("tip"),did.get("descriere"), did.get("data"));
                        System.out.println("Obiecte salvate");

                        return Integer.parseInt(data.get("id"));
                    }
                }
            } else {
                // Try to insert a record without both parameters
                if (data.get("id") == null||data.get("id").isEmpty()) {
                    data.put("idmaterie", "");
                    data.put("denumire", "");
                    data.remove("id");
                    String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".obiecte(idmaterie, denumire) " +
                            "VALUES (?, ?)";

                    jdbcTemplate.update(insertSql, data.get("idmaterie"), data.get("denumire"));

                    Integer insertedId = getLastInsertID();
                    assert insertedId != null;
                    Map<String, Object> did = new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", 0);
                    did.put("tip", "Modificare calitate");
                    did.put("descriere", "obiecte[" + data.get("id") + "],info[" + data.get("denumire") + "]");
                    did.put("data", now());

                    String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iduser, iddosar, tip,descriere,data) " +
                            "VALUES (?, ?, ?,?,?)";

                    jdbcTemplate.update(insertHistoricSql, did.get("iduser"), did.get("iddosar"),did.get("tip"),did.get("descriere"), did.get("data"));
                    System.out.println("Obiecte salvate");

                    return insertedId;
                } else {
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".obiecte SET idmaterie = ?, denumire = ? WHERE id = ?";

                    jdbcTemplate.update(updateSql, data.get("idmaterie"), data.get("denumire"), data.get("id"));

                    Map<String, Object> did = new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", 0);
                    did.put("tip", "Modificare calitate");
                    did.put("descriere", "obiecte[" + data.get("id") + "],info[" + data.get("denumire") + "]");
                    did.put("data", now());

                    String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iduser, iddosar, tip,descriere,data) " +
                            "VALUES (?, ?, ?,?,?)";

                    jdbcTemplate.update(insertHistoricSql, did.get("iduser"), did.get("iddosar"),did.get("tip"),did.get("descriere"), did.get("data"));
                    System.out.println("Obiecte salvate");

                    return Integer.parseInt(data.get("id"));
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare salvare obiecte");
            e.printStackTrace();
            return 0;
        }
    }
    public int saveParti(Map<String,String> data){
        try {
            data.put("numeprenume", data.get("numeprenume").replace("'", ""));
            data.put("societate", data.get("societate").replace("'", ""));

            // Check for duplicate data
            if (!data.get("numeprenume").toString().isEmpty() || !data.get("societate").toString().isEmpty()) {
                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".parti WHERE numeprenume = ? AND societate = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        data.get("numeprenume"), data.get("societate"));

                if (!Objects.equals(cnt, 0)) {
                    // Entry with the same numeprenume and societate already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".parti WHERE numeprenume = ? AND societate = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            data.get("numeprenume"), data.get("societate"));
                    assert id != null;
                    System.out.println("Parti salvate");
                    return id;
                } else {
                    // Insert new parte or update if it has an id
                    String idd = data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".parti(numeprenume, societate) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, data.get("numeprenume"), data.get("societate"));

                        Integer insertedId = getLastInsertID();
                        assert insertedId!= null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Adaugare parte");
                        did.put("descriere", "parti[" + insertedId + "],info[" + data.get("numeprenume") + " / " + data.get("societate") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));
                        System.out.println("Parti salvate");
                        return insertedId;
                    } else {
                        data.put("id", idd);
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".parti SET numeprenume = ?, cnp = ?, codabonat = ?, " +
                                "societate = ?, cui = ?, registrucomert = ?, adresa = ?, telefon = ?, email = ?, banca = ?, " +
                                "iban = ?, idtipparte = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("numeprenume"), data.get("cnp"), data.get("codabonat"),
                                data.get("societate"), data.get("cui"), data.get("registrucomert"), data.get("adresa"),
                                data.get("telefon"), data.get("email"), data.get("banca"), data.get("iban"),
                                data.get("idtipparte"), idd);

                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare parte");
                        did.put("descriere", "parti[" + idd + "],info[" + data.get("numeprenume") + " / " + data.get("societate") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));
                        System.out.println("Parti salvate");
                        return Integer.parseInt(idd);
                    }
                }
            } else {
                System.out.println("Eroare salvare parti");
                return 0; // Cannot insert a parte without nume or societate
            }
        } catch (Exception e) {

            e.printStackTrace();
            return 0; // Error occurred
        }

    }
    public int saveMaterii(String denumire){
        try {

            // Check for duplicate data
            if (!denumire.trim().isEmpty()) {
                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".materii WHERE denumirejustro = ? OR denumire = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        denumire, denumire);

                if (!Objects.equals(cnt, 0)) {
                    // Entry with the same denumire or denumirejustro already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".materii WHERE denumirejustro = ? OR denumire = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            denumire, denumire);
                    assert id != null;
                    System.out.println("Materii salvate");
                    return id;
                } else {
                    // Insert new denumire or update if it has an id
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".materii(denumire, denumirejustro) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, denumire, denumire);

                        Integer insertedId = getLastInsertID();
                        assert insertedId != null;
                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, client.getUserId(), 0, "Adaugare materie",
                                "materii[" + insertedId + "],info[" + denumire + " / " + denumire + "]"
                                , now());
                    System.out.println("Materii salvate");

                        return insertedId;

                }
            }
        } catch (Exception e) {
            System.out.println("Eroare salvare materii");

            e.printStackTrace();
            return 0;
        }

        return 0;
}

    public int saveSectii(Map<String,String> data){
        try {

            if (data.get("denumire") == null || data.get("denumire").isEmpty()) {
                data.put("denumire", "-");
            }

            // Check for duplicate data
            if (!data.get("denumire").isEmpty()) {
                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".sectii WHERE denumire = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class, data.get("denumire"));

                if (!Objects.equals(cnt, 0)) {
                    // Entry with the same denumire already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".sectii WHERE denumire = ?";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class, data.get("denumire"));
                    assert id != null;
                    System.out.println("Sectii salvate");

                    return id;
                } else {
                    // Insert new denumire or update if it has an id
                    String idd =  data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".sectii(denumire) VALUES (?)";

                        jdbcTemplate.update(insertSql, data.get("denumire"));

                        Integer insertedId = getLastInsertID();
                        assert insertedId != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Adaugare sectie");
                        did.put("descriere", "sectii[" + insertedId + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());
                        System.out.println(did.get("descriere"));
                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));
                        System.out.println("Sectii salvate");
                        return insertedId;
                    } else {
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".sectii SET denumire = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("denumire"), idd);
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare sectie");
                        did.put("descriere", "sectii[" + idd + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));
                        System.out.println("Sectii salvate");
                        return Integer.parseInt(idd);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare salvare sectii");
            e.printStackTrace();
            return 0; // Error occurred
        }

        return 0;
    }

    public int getIdInstantaByDenumirejustro(String denumireJustro) {
        Integer id;

        if (denumireJustro == null || denumireJustro.trim().isEmpty()) {
            return 0;
        }

        try {
            String selectSql = "SELECT id FROM " + client.getBazaDeDate() + ".instante WHERE denumirejustro = ?";

            id = jdbcTemplate.queryForObject(selectSql, Integer.class, denumireJustro);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        assert id != null;
        return id;
    }
    public Date now() {
        return Date.from(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.ofHours(3)));
    }
    public void actualizareDataum(Map<String, Object> data)  {
        Timestamp dataum = new Timestamp(now().getTime());
        String selectSql = "SELECT dataum, dataumSCJ FROM " + client.getBazaDeDate() + ".dosare WHERE numardosar=? AND instantadosar=? AND andosar=? " +
                    "AND accesoriidosar=?";
        Map<String, Object> result;
        try {
            result = jdbcTemplate.queryForMap(selectSql, data.get("numardosar"), data.get("instantadosar"), data.get("andosar"),data.getOrDefault("accesoriidosar",""));
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            System.out.println("No result found for the given ID.");
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            System.out.println(result.get("dataum")+" "+result.get("dataumSCJ"));
             Date dataum1 = dateFormat.parse(result.get("dataum").toString());
             Date dataumSCJ = dateFormat.parse(result.get("dataumSCJ").toString());
            Timestamp dpjr = new Timestamp(dataum1.getTime());
            Timestamp dscj= new Timestamp(dataumSCJ.getTime());
            if (data.containsKey("flagpjr") && data.get("flagpjr") != null) {
                if (dataum.after(dpjr)) {
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".dosare SET dataum = ? WHERE id = ?";
                    jdbcTemplate.update(updateSql, dataum, data.get("id"));
                }
            }

            if (data.containsKey("flagscj") && data.get("flagscj") != null) {
                if (dataum.after(dscj)) {
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".dosare SET dataumSCJ = ? WHERE id = ?";
                    jdbcTemplate.update(updateSql, dataum, data.get("id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        public int dosarisMonitorizat(int iddosar, int iduser) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".monitorizaridosar WHERE iddosar = ? AND iduser = ? AND monitorizat = 1";
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, iddosar, iduser);
            return cnt != null ? cnt : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public Integer dosarisSincronizat(int iddosar, int iduser) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".monitorizaridosar WHERE iddosar = ? AND iduser = ? AND sincronizat = 1";
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, iddosar, iduser);
            return cnt != null ? cnt : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean existaDosar(String numardosar, String instantadosar, String andosar, String accesoriidosar) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".dosare " +
                    "WHERE numardosar = ? AND instantadosar = ? AND andosar = ? AND accesoriidosar = ? AND sters = 0";

            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, numardosar.trim(), instantadosar.trim(), andosar.trim(), accesoriidosar.trim());

            return cnt != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public String getEmailByIdUsers(String id) {
        try {
            String sql = "SELECT email FROM " + client.getBazaDeDate() + ".users WHERE id = ?";

            return jdbcTemplate.queryForObject(sql, String.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getUsernameByIdUsers(String id) {
        try {
            String sql = "SELECT name FROM " + client.getBazaDeDate() + ".users WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Map<String, Object>> findDupaStadiuDosarPartiDosar(int idStadiuDosar) {
        List<Map<String, Object>> entries = new ArrayList<>();

        if (idStadiuDosar == 0) {
            return entries;
        }

        Map<String, Object> stadiuDosar = findStadii(idStadiuDosar);
        int iddosar = Integer.parseInt( stadiuDosar.get("iddosar").toString());

        Map<String, Object> dosar = findSimpluDosar(iddosar);

        int idclient = (int) dosar.get("idclient");
        int idparteadversa = (int) dosar.get("idparteadversa");

        final String sql="select * from " + client.getBazaDeDate() + ".partidosar where idstadiudosar=?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, idStadiuDosar);
        String email = getEmailByIdUsers(client.getUserId());

        if (!result.isEmpty()) {
            for (Map<String, Object> row : result) {
                try {
                    Map<String, Object> entry = new HashMap<>();
                    Map<String, Object> sirCalitati = findDupaCalitati((int) row.get("idcalitate"));
                    Map<String, Object> sirParti = findDupaParti((int) row.get("idparte"));
                    entry.put("id", row.get("id"));
                    entry.put("idparte", row.get("idparte"));
                    entry.put("idclient", idclient);
                    entry.put("idparteadversa", idparteadversa);
                    entry.put("iddosar", iddosar);

                    if (!sirParti.isEmpty()) {
                        entry.put("numeprenume", sirParti.get("numeprenume"));
                        entry.put("cnp", sirParti.get("cnp"));
                        entry.put("codabonat", sirParti.get("codabonat"));
                        entry.put("societate", sirParti.get("societate"));
                        entry.put("cui", sirParti.get("cui"));
                        entry.put("registrucomert", sirParti.get("registrucomert"));
                        entry.put("adresa", sirParti.get("adresa"));
                        entry.put("telefon", sirParti.get("telefon"));
                        entry.put("email", sirParti.get("email"));
                        entry.put("banca", sirParti.get("banca"));
                        entry.put("iban", sirParti.get("iban"));
                        entry.put("idcalitate", row.get("idcalitate"));
                        entry.put("denumirecalitate", sirCalitati.isEmpty() ? null : sirCalitati.get("denumire"));
                        entry.put("idtipparte", sirParti.get("idtipparte"));
                        entry.put("dennumiretipparte", getDescriereTipParte((int) sirParti.get("idtipparte")));
                        entry.put("firmamonitorizata", getcountDupaEmailSiCuiSiIddosarFirmeMonitorizateDosar(
                                        email,
                                        sirParti.get("cui").toString(),
                                        iddosar
                                )
                        );
                        entries.add(entry);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return entries;
    }
    public Map<String, Object> getDescriereTipParte(int id) {
        try {
            String sql = "SELECT descriere FROM " + client.getBazaDeDate() + ".tipParte WHERE id = ?";
            Map<String, Object> resultSet = jdbcTemplate.queryForMap(sql, id);
            Map<String, Object> entry = new HashMap<>();
            if (!resultSet.isEmpty()) {
                entry.put("descriere", resultSet.get("descriere"));
            }

            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public int getcountDupaEmailSiCuiSiIddosarFirmeMonitorizateDosar(String email, String cui, int iddosar) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".firmemonitorizatedosar WHERE emailmonitorizare = ? AND codcui = ? AND iddosar = ?";
            Integer rv=jdbcTemplate.queryForObject(sql, Integer.class, email, cui, iddosar);
            assert rv!=null;
            return rv;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public  Map<String, Object> findDupaParti(int id) {
        try {
            String sql = "SELECT * FROM " + client.getBazaDeDate() + ".parti WHERE id = ?";
            Map<String, Object> resultSet = jdbcTemplate.queryForMap(sql, id);
            Map<String, Object> entry = new HashMap<>();
            if (!resultSet.isEmpty()) {
                entry.put("id", resultSet.get("id"));
                entry.put("numeprenume", resultSet.get("numeprenume"));
                entry.put("cnp", resultSet.get("cnp"));
                entry.put("codabonat", resultSet.get("codabonat"));
                entry.put("societate", resultSet.get("societate"));
                entry.put("cui", resultSet.get("cui"));
                entry.put("registrucomert", resultSet.get("registrucomert"));
                entry.put("adresa", resultSet.get("adresa"));
                entry.put("telefon", resultSet.get("telefon"));
                entry.put("email", resultSet.get("email"));
                entry.put("banca", resultSet.get("banca"));
                entry.put("iban", resultSet.get("iban"));
                entry.put("idtipparte", resultSet.get("idtipparte"));
            }

            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> findDupaCalitati(int id) {
        try {
            String sql = "SELECT * FROM " + client.getBazaDeDate() + ".calitati WHERE id = ?";
            Map<String, Object> resultSet = jdbcTemplate.queryForMap(sql, id);
            Map<String, Object> entry = new HashMap<>();
            if (!resultSet.isEmpty()) {
                    entry.put("id", resultSet.get("id"));
                    entry.put("denumire", resultSet.get("denumire"));
                    entry.put("idstadiu", resultSet.get("idstadiu"));
            }
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
public Map<String, Object> findStadii(int id) {
        try {
            String sql = "SELECT * FROM " + client.getBazaDeDate() + ".stadiidosar WHERE id = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, id);
            Map<String, Object> entry = new HashMap<>();
            if (!result.isEmpty()) {
                    entry.put("id", result.get("id"));
                    entry.put("iddosar", result.get("iddosar"));
                    entry.put("idstadiu", result.get("idstadiu"));
                    entry.put("data", result.get("data"));
                    entry.put("idinstanta", result.get("idinstanta"));
                    entry.put("idsectie", result.get("idsectie"));
                    entry.put("judecatori", result.get("judecatori"));

            }
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Map<String, Object> findSimpluDosar(int id) {
        try {
            String sql = "SELECT * FROM " + client.getBazaDeDate() + ".dosare WHERE id = ?";
            Map<String, Object> resultSet = jdbcTemplate.queryForMap(sql, id);

            Map<String, Object> dosar = new HashMap<>();
            if (!resultSet.isEmpty()) {
                    dosar.put("id", resultSet.get("id"));
                    dosar.put("userid", resultSet.get("userid"));
                    dosar.put("datacreare", resultSet.get("datacreare"));
                    dosar.put("dataum", resultSet.get("dataum"));
                    dosar.put("dataumSCJ", resultSet.get("dataumSCJ"));
                    dosar.put("numardosar", resultSet.get("numardosar"));
                    dosar.put("instantadosar", resultSet.get("instantadosar"));
                    dosar.put("andosar", resultSet.get("andosar"));
                    dosar.put("accesoriidosar", resultSet.get("accesoriidosar"));
                    dosar.put("idclient", resultSet.get("idclient"));
                    dosar.put("idparteadversa", resultSet.get("idparteadversa"));
                    dosar.put("idstare", resultSet.get("idstare"));
                    dosar.put("finalizat", resultSet.get("finalizat"));
                    dosar.put("solutionatfavorabil", resultSet.get("solutionatfavorabil"));
                    dosar.put("solutiedosar", resultSet.get("solutiedosar"));
                    dosar.put("valoaredosar", resultSet.get("valoaredosar"));
                    dosar.put("valoarerecuperata", resultSet.get("valoarerecuperata"));
                    dosar.put("datafinalizare", resultSet.get("datafinalizare"));
                    dosar.put("comentarii", resultSet.get("comentarii"));
                    dosar.put("numarintern", resultSet.get("numarintern"));
                    dosar.put("dosarinstanta", resultSet.get("dosarinstanta"));
                    dosar.put("rezultat", resultSet.get("rezultat"));
                    dosar.put("sentintaprimita", resultSet.get("sentintaprimita"));
            }
            return dosar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getListaPartiDosar(String iddosar) {
        StringBuilder sirparti = new StringBuilder();
        try {
            String idstadiudosarSql = "SELECT id FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar = ? ORDER BY data DESC LIMIT 1";
            Integer idstadiudosar = jdbcTemplate.queryForObject(idstadiudosarSql, Integer.class, iddosar);

            if (idstadiudosar != null) {
                List<Map<String, Object>> rows = findDupaStadiuDosarPartiDosar(idstadiudosar);
                for (Map<String, Object> row : rows) {
                    String denumire = row.get("numeprenume").toString();
                    if (!row.get("societate").toString().isEmpty()) {
                        denumire = row.get("societate").toString();
                    }
                    sirparti.append(denumire.replaceAll("\n", " ")).append(" - ").append(row.get("denumirecalitate")).append("\n");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sirparti.toString();
    }

    public Map<String, Object> findSimpluParti(String id) {
        try {
            String sql = "SELECT * FROM " + client.getBazaDeDate() + ".parti WHERE id IN (" + id + ")";
            Map<String, Object> resultList = jdbcTemplate.queryForMap(sql);

            Map<String, Object> entries = new HashMap<>();
            if (!resultList.isEmpty()) {
                entries.put("id", resultList.get("id"));
                entries.put("numeprenume", resultList.get("numeprenume"));
                entries.put("cnp", resultList.get("cnp"));
                entries.put("codabonat", resultList.get("codabonat"));
                entries.put("societate", resultList.get("societate"));
                entries.put("cui", resultList.get("cui"));
                entries.put("registrucomert", resultList.get("registrucomert"));
                entries.put("adresa", resultList.get("adresa"));
                entries.put("telefon", resultList.get("telefon"));
                entries.put("email", resultList.get("email"));
                entries.put("banca", resultList.get("banca"));
                entries.put("iban", resultList.get("iban"));
                entries.put("idtipparte", resultList.get("idtipparte"));
            }
            return entries;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String convertdate(List<Date> dateList) {
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        List<String> formattedDates = dateList.stream()
                .map(isoDateFormat::format)
                .collect(Collectors.toList());

        return String.join(", ", formattedDates);
    }

    public static int min(List<Integer> numbers) {
        int smallest = numbers.get(0);
        for (int number : numbers) {
            if (number < smallest) {
                smallest = number;
            }
        }
        return smallest;
    }
    public String[] convert(String str) {
        String[] parts = str.split("/");
        if (parts.length >= 4) {
            return Arrays.copyOfRange(parts, 0, 4);
        } else if (parts.length == 3) {
            return new String[]{parts[0], parts[1], parts[2], ""};
        }else if (parts.length == 2) {
            return new String[]{ parts[0], parts[1], "" };
        } else if (parts.length == 1) {
            return new String[]{ parts[0], "", "" };
        } else {
            return new String[]{ "", "", "" };
        }

    }
}
