package ro.ctce.sincronizare.Dao;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ro.ctce.sincronizare.Entities.*;
import ro.ctce.sincronizare.Mapper.DosareRowMapper;
import ro.ctce.sincronizare.Service.FileService;

import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository("InserareDao")
public class InserareDataAccessService implements InserareDao {
    private final FileService fileService;
    private final JdbcTemplate jdbcTemplate;
    private Clienti client;

    @Autowired
    public InserareDataAccessService(FileService fileService, JdbcTemplate jdbcTemplate) {
        this.fileService = fileService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void AdaugareDatabase(Clienti client) {
        this.client = client;
        String sql = "select * from " + client.getBazaDeDate() + ".dosare where userid=?";
        List<Dosar> dosare = jdbcTemplate.query(sql, new DosareRowMapper(), client.getUserId());
        for (Dosar dosar : dosare) {
            StringBuilder sb = new StringBuilder();
            if (dosar.getNumarDosar() != null)
                sb.append(dosar.getNumarDosar()).append("/");
            if (dosar.getInstantadosar() != null)
                sb.append(dosar.getInstantadosar()).append("/");
            if (dosar.getAndosar() != null)
                sb.append(dosar.getAndosar()).append("/");
            if (dosar.getAccesoriidosar() != null)
                sb.append(dosar.getAccesoriidosar()).append("/");
            String nrDosar = sb.toString();
            List<SolrFile> dosareSolr = fileService.findByNumardosar(nrDosar).getContent();
            for (SolrFile dosarSolr : dosareSolr) {
                List<Map<String, String>> partiList = new ArrayList<>();
                for (int i = 0; i < Math.min(dosarSolr.getCalitateparte().size(), dosarSolr.getNumeparte().size()); i++) {
                    String nume = dosarSolr.getNumeparte().get(i);
                    String calitateParte = dosarSolr.getCalitateparte().get(i);
                    Map<String, String> parti = new HashMap<>();
                    parti.put("nume", nume);
                    parti.put("calitateParte", calitateParte);
                    partiList.add(parti);
                }
                int cnt1 = dosarSolr.getOra() != null ? dosarSolr.getOra().size() : 0;
                int cnt2 = dosarSolr.getDatadocument() != null ? dosarSolr.getDatadocument().size() : 0;
                int cnt3 = dosarSolr.getSolutie() != null ? dosarSolr.getSolutie().size() : 0;
                int cnt4 = dosarSolr.getSolutiesumar() != null ? dosarSolr.getSolutiesumar().size() : 0;
                int cnt5 = dosarSolr.getNumardocument() != null ? dosarSolr.getNumardocument().size() : 0;
                int cnt6 = dosarSolr.getDatapronuntare() != null ? dosarSolr.getDatapronuntare().size() : 0;
                int cnt7 = dosarSolr.getDocumentsedinta() != null ? (dosarSolr.getDocumentsedinta()).size() : 0;
                int cnt8 = dosarSolr.getDatasedinta() != null ? dosarSolr.getDatasedinta().size() : 0;
                int cnt9 = dosarSolr.getComplet() != null ? dosarSolr.getComplet().size() : 0;
                List<Map<String, String>> sedinteList = new ArrayList<>();
                for (int i = 0; i < min(Arrays.asList(cnt1, cnt2, cnt3, cnt4, cnt5, cnt6, cnt7, cnt8, cnt9)); i++) {
                    if (dosarSolr.getDatasedinta().get(i).toString().contains("1900-01"))
                        dosarSolr.setDataSedintai(i, new Date());
                    if (dosarSolr.getDatadocument().get(i).toString().contains("1900-01"))
                        dosarSolr.setDataDocumenti(i, new Date());
                    if (dosarSolr.getDatapronuntare().get(i).toString().contains("1900-01"))
                        dosarSolr.setDataPronuntarei(i, new Date());
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
                    sedinteList.add(s);
                }
                String materieJustRo = dosarSolr.getMaterie();
                String obiectDosar = dosarSolr.getObiect();
                List<String> numeClient = dosarSolr.getNumeparte();
                String dataDosar = ZonedDateTime.parse(String.valueOf(dosarSolr.getData())).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                Date DataUMPJR = new Date();
                Date DataUMSCJ = new Date();
                String dataStadiuDosar = ZonedDateTime.parse(String.valueOf(dosarSolr.getData())).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String sectie = dosarSolr.getSectie();
                int idInstantaStadiuDosar = getIdInstantaByDenumirejustro(dosarSolr.getInstitutie());
                Map<String, String> valSectie = new HashMap<>();
                valSectie.put("denumire", sectie);
                int idSectie = saveSectii(valSectie);
                String[] nreDosar = convert(dosarSolr.getNumardosar());
                String numardosar = nreDosar[0];
                String instantadosar = nreDosar[1];
                String andosar = nreDosar[2];
                String accesoriidosar = nreDosar[3];
                if (existaDosar(numardosar, instantadosar, andosar, accesoriidosar)) {
                    break;
                }
                Map<String, String> valMaterie = new HashMap<>();
                valMaterie.put("denumire", materieJustRo);
                valMaterie.put("denumirejustro", materieJustRo);
                int idMaterie = saveMaterii(valMaterie);
                Map<String, String> valObiect = new HashMap<>();
                valObiect.put("idmaterie", String.valueOf(idMaterie));
                valObiect.put("denumire", obiectDosar);
                int idobiect = saveObiecte(valObiect);
                Map<String, String> valParte = new HashMap<>();
                valParte.put("numeprenume", partiList.get(0).get("nume"));
                valParte.put("societate", partiList.get(0).get("nume"));
                int idclient = saveParti(valParte);
                Map<String, String> optStadii = new HashMap<>();
                optStadii.put("denumire", dosarSolr.getStadiu());
                optStadii.put("idmaterie", String.valueOf(idMaterie));
                int idstadiu = saveStadii(optStadii);

                Map<String, String> valCalitate = new HashMap<>();
                valCalitate.put("denumire", partiList.get(0).get("calitateParte"));
                valCalitate.put("idstadiu", String.valueOf(idstadiu));
                valCalitate.put("calitateclient", "0");
                int idcalitate = saveCalitati(valCalitate);
                //insert into dosare
                Map<String, String> dosardb = new HashMap<>();
                dosardb.put("id", dosarSolr.getId());
                dosardb.put("userid", client.getUserId());//? de ce aici normal era 1?
                dosardb.put("datacreare", dosarSolr.getId());
                dosardb.put("dataum", dosarSolr.getId());
                dosardb.put("numarintern", null);
                dosardb.put("dosarinstanta", "1");
                dosardb.put("numardosar", numardosar);
                dosardb.put("instantadosar", instantadosar);
                dosardb.put("andosar", andosar);
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
                valCalitate.put("idstadiudosar", String.valueOf(idstadiudosar));
                System.out.println(savePartiDosar(valPD));
                Map<String, String> valObDosar = new HashMap<>();
                valObDosar.put("iddosar", String.valueOf(iddosar));
                valObDosar.put("idobiect", String.valueOf(idobiect));
                System.out.println(saveObiecteDosar(valObDosar));
                for (Map<String, String> termen : sedinteList) {
                    Map<String, Object> valTD = new HashMap<>();
                    valTD.put("id", "");
                    valTD.put("datatermen", termen.get("datasedinta").substring(0, dosarSolr.getData().toString().indexOf('T')) + " " + termen.get("ora"));
                    valTD.put("tipsolutie", termen.get("solutie").replace("'", "").replace("[", "").replace("]", ""));
                    valTD.put("sumarsolutie", termen.get("solutiesumar").replace("'", "").replace("[", "").replace("]", ""));
                    valTD.put("complet", termen.get("complet"));
                    valTD.put("idstadiudosar", String.valueOf(idstadiudosar));
                    valTD.put("datadocument", termen.get("datadocument"));
                    valTD.put("numarDocument", termen.get("numardocument"));
                    System.out.println(saveTermen(valTD, Integer.parseInt(iduri.get("iddosar").toString())));
                }
            }
        }
        String datastartsync = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(now());
        StringBuilder txt = new StringBuilder();
        txt.append("Id client: ").append(client.getUserId()).append(System.lineSeparator());
        txt.append("Data si ora inceput sincronizare: ").append(datastartsync).append(System.lineSeparator());
        txt.append("Data si ora sfarsit sincronizare: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())).append(System.lineSeparator());
        txt.append("Dosare actualizate: ").append(dosare.size());
        String log = txt + System.lineSeparator() + "-------------------------" + System.lineSeparator();
        txt.setLength(0);

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy.MM");
        String month = monthFormat.format(new Date());

        String logDirectory = "autosync/log-sincronizari/" + month;
        if (!new java.io.File(logDirectory).isDirectory()) {
            new java.io.File(logDirectory).mkdirs();
        }

        String logFileName = "/sincronizare.1_" + System.currentTimeMillis() + ".log";
        String logFilePath = logDirectory + logFileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String saveTermen(Map<String, Object> data, int iddosar) {
        try {

            // Get the id of the stadii dosar
            String sql = "SELECT iddosar FROM " + client.getBazaDeDate() + ".stadiidosasr WHERE id = ?";
            String idd = jdbcTemplate.queryForObject(sql, String.class, data.get("idstadiudosar"));
            assert idd != null;
            if (StringUtils.isNotBlank(data.get("datatermen").toString()) && Integer.parseInt(idd) > 0) {
                // Parse datatermen to get the date part and format it
                String datatermen = data.get("datatermen").toString();
                String datat = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datatermen));
                data.put("datatermen", datatermen);

                String qcomplet = "";

                // If we have 'complet' value, add it to the query
                if (StringUtils.isNotBlank(data.get("complet").toString()) && !"-".equals(data.get("complet").toString())) {
                    qcomplet = " AND (complet='" + data.get("complet").toString() + "' OR complet IS NULL OR complet='-' OR complet='') ";
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
                        int iduser = Integer.parseInt(dosaregetUserId(iddosar));
                        String idatentionare = getString(data, iddosar, id, iduser);
                        if (idatentionare != null) return idatentionare;
                    } else {
                        // Conditions for update not met; if datatermen or complet is different, insert a new one
                        if ((datatermen.compareTo(data.get("datatermen").toString()) != 0 && orat > 0) ||
                                (complet.compareTo(data.get("complet").toString()) != 0)) {
                            cnt = 0; // It will go to the if (cnt <= 0) branch and perform an INSERT
                        } else {
                            return String.valueOf(id);
                        }
                    }
                }

                if (Objects.equals(cnt, 0)) {
                    // Insert
                    Integer id = data.get("id") == null ? null : (Integer) data.get("id");
                    if (Objects.equals(id, 0)) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".termenedosar(datatermen, tipsolutie, sumarsolutie, idstadiudosar, dataDocument, numarDocument, dataluatlacunostinta, complet, sala, dataIntrare) " +
                                "VALUES(:datatermen, :tipsolutie, :sumarsolutie, :idstadiudosar, :dataDocument, :numarDocument, :dataluatlacunostinta, :complet, :sala, :dataIntrare)";
                        jdbcTemplate.update(insertSql, data);
                        id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                        assert id != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", idd);
                        did.put("tip", "Adaugare termen dosar");
                        did.put("descriere", "termenedosar[" + id + "],stadiidosar[" + data.get("idstadiudosar") + "],info[" + data.get("datatermen").toString() + " - " + data.get("tipsolutie").toString() + "]");
                        did.put("data", now());

                        String insertSql2 = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(:iddosar, :iduser, :tip, :descriere, :data)";
                        jdbcTemplate.update(insertSql2, did);

                        // If the dosar is monitored and synchronized, automatically add an alert for the termen
                        if (StringUtils.isNotBlank(String.valueOf(iddosar)) && String.valueOf(iddosar).trim().length() > 0) {
                            int iduser = getUserIdDosar(iddosar);
                            String idatentionare = getString(data, iddosar, id, iduser);
                            if (idatentionare != null) return idatentionare;
                        }
                        return "id=" + id;
                    } else {
                        String updateSql = "update " + client.getBazaDeDate() + ".termenedosar set datatermen=:datatermen, tipsolutie=:tipsolutie, sumarsolutie=:sumarsolutie, dataDocument=:dataDocument, numarDocument=:numarDocument, complet=:complet WHERE id=:id";
                        jdbcTemplate.update(updateSql, data);

                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", idd);
                        did.put("tip", "Modificare termen dosar");
                        did.put("descriere", "termenedosar[" + id + "],stadiidosar[" + data.get("idstadiudosar") + "],info[" + data.get("datatermen").toString() + " - " + data.get("tipsolutie").toString() + "]");
                        did.put("data", now());

                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(:iddosar, :iduser, :tip, :descriere, :data)";
                        jdbcTemplate.update(insertSql, did);

                        return String.valueOf(id);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return "Eroare la save termendosar: " + e.getMessage();
        }
    }

    @Nullable
    private String getString(Map<String, Object> data, int iddosar, int id, int iduser) {
        int estemon = dosarisMonitorizat(iddosar, iduser);
        int estesin = dosarisSincronizat(iddosar, iduser);

        if ((estemon + estesin) > 1 && StringUtils.isNotBlank(data.get("datatermen").toString())) {
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
                    Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
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

                    if (!iddosar.isEmpty()) {
                        nrdosar = getNrUnicDosar5(iddosar);
                        partiDosar = getListaPartiDosar(iddosar);
                        clientDosar = getDenumireClientDosar(iddosar).replace("\n", " ");
                        parteAdversa = getDenumireParteAdversaDosar(iddosar).replace("\n", " ");
                        int idobiectdosar = getIdObiectRecent(iddosar);
                        denobiectdosar = getDenumireByIdObiecte(idobiectdosar).replace("\n", " ");
                        observatii = getObservatiiDosar(iddosar).replace("\n", " ");
                        int userid = getUserIdDosar(Integer.parseInt(iddosar));
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

                    //setNotificariAtentionari(atentionari, userid);
                }

//            } else if ("1".equals(notifAnterioara)) {
//                // Delete from Notificari if needed
//                // ...
            }
        }
                catch(
    Exception e)

    {
        e.printStackTrace();
        System.out.println("Eroare la adaugare atentionare: " + e.getMessage());
    }
        return -1;
}
//    public void setNotificariAtentionari(List<Map<String, Object>> atentionari, int userId) {
//
//        String licenta = getLicenta();
//
//        String username;
//        if (userId > 0) {
//            username = getUsernameById(userId);
//        } else {
//            username = "";
//        }
//
//        String url = "https://dosare.ctce.ro/Notificari/listenerws.php?wsdl";
//
//        try {
//            HttpClient httpClient = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(url);
//
//            // Prepare the parameters
//            List<NameValuePair> params = new ArrayList<>();
//            params.add(new BasicNameValuePair("licenta", licenta));
//            params.add(new BasicNameValuePair("denumire", username));
//
//            String atentionariJson = convertAtentionariToJson(atentionari);
//            params.add(new BasicNameValuePair("atentionari", atentionariJson));
//
//            // Set the parameters in the request
//            httpPost.setEntity(new UrlEncodedFormEntity(params));
//
//            // Execute the request
//            HttpResponse response = httpClient.execute(httpPost);
//
//            // Handle the response as needed
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
//                String line;
//                StringBuilder result = new StringBuilder();
//                while ((line = reader.readLine()) != null) {
//                    result.append(line);
//                }
//                // Parse and handle the result here
//                String responseString = result.toString();
//                // Return true or false based on the response
//                handleResponse(responseString);
//            }
//
//        } catch (Exception e) {
//            // Handle any exceptions here if needed
//            System.out.println("Eroare la adaugare/modificare notificari Atentionari: " + e.getMessage());
//        }
//    }

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
            return jdbcTemplate.queryForObject(sql, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
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
                }
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
                    Integer idod = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                    Map<String, Object> did=new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", data.get("iddosar"));
                    did.put("tip", "Adaugare obiect dosar");
                    did.put("descriere", "obiectedosar[" + idod + "],obiecte[" + data.get("idobiect") + "]");
                    did.put("data", now());

                    jdbcTemplate.update(
                            "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)",
                            did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));
                    return idod;
            }
        }
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
                jdbcTemplate.update(insertQuery, valPD.get("idparte"), valPD.get("idcalitate"),valPD.get("idstadiudosar"));
                Integer idod = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                Map<String, Object> did=new HashMap<>();
                did.put("iduser", client.getUserId());
                did.put("iddosar", 0);
                did.put("tip", "Adaugare parte dosar");
                did.put("descriere", "partidosar["+idod+"],parti["+valPD.get("idparte")+"],stadiidosar["+valPD.get("idstadiudosar"));
                did.put("data", now());

                jdbcTemplate.update(
                        "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)",
                        did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));
                return idod;
            }
        }
        return null;
    }
    public Map<String, Object> saveDosar(Map<String, String> data) {
        if (!data.get("solutiedosar").isEmpty()) {
            data.put("solutie",(""));
        }
        boolean nrd = true;

        if ("".equals(
                data.get("numardosar") + data.get("instantadosar") + data.get("andosar") + data.get("accesoriidosar"))) {
            nrd = false;
            if ("".equals(data.get("numarintern"))) {
                return null;
            }
        }
        String query;
        Object[] params;
        Integer cnt;
        if (nrd) {
            query = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".dosare WHERE numardosar=? AND instantadosar=? AND andosar=? " +
                    "AND accesoriidosar=? AND sters=0";
            cnt=jdbcTemplate.queryForObject(query, Integer.class, data.get("numardosar"), data.get("instantadosar"), data.get("andosar"), data.get("accesoriidosar"));
        } else {
            query = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".dosare WHERE numarintern=? AND sters=0";
            cnt=jdbcTemplate.queryForObject(query, Integer.class, data.get("numarintern"));
        }
        try {
            Integer ids;
            if (!Objects.equals(cnt, 0)) {
                if (nrd) {
                    query = "SELECT id FROM " + client.getBazaDeDate() + ".dosare WHERE numardosar=? AND instantadosar=? AND andosar=? AND accesoriidosar=? AND sters=0 AND id=? ORDER BY id ASC";
                    RowMapper<Integer> rowMapper = (rs, rowNum) -> rs.getInt("id");
                    ids = jdbcTemplate.queryForObject(query, rowMapper, data.get("numardosar"), data.get("instantadosar"), data.get("andosar"), data.get("accesoriidosar"), data.get("id"));
                } else {
                    query = "SELECT id FROM " + client.getBazaDeDate() + ".dosare WHERE numarintern=? AND sters=0 AND id=? ORDER BY id ASC";
                    RowMapper<Integer> rowMapper = (rs, rowNum) -> rs.getInt("id");
                    ids = jdbcTemplate.queryForObject(query, rowMapper, data.get("numarintern"), data.get("id"));
                }
                query = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar=? AND idinstanta=? AND idstadiu=?";
                //cnt=jdbcTemplate.queryForObject(query, Integer.class, ids.get(0), );
                Map<String, Object> sd = new HashMap<>();
                if (!Objects.equals(cnt, 0)) {
                    sd.put("id", "");
                    sd.put("iddosar", ids);
                    sd.put("idstadiu", data.get("idstadiu"));
                    sd.put("data", data.get("dataSD"));
                    sd.put("idsectie", data.get("idsectie"));
                    sd.put("judecatori", "");
                    String idstadiudosar = saveStadiiDosar(sd);
                    query = "SELECT dataum FROM " + client.getBazaDeDate() + ".dosare WHERE id=? AND dataum<?";
                    String dataum = jdbcTemplate.queryForObject(query, String.class, data.get("id"), data.get("dataum"));
                    if (!Objects.equals(dataum, "")) {
                        Map<String, Object> dumData = new HashMap<>();
                        dumData.put("id", "");
                        dumData.put("dataum", data.get("dataum"));
                        dumData.put("flagpjr", "1");
                        dumData.put("flagscj", "");
                        actualizareDataum(dumData);
                    }
                    Map<String, Object> rv = new HashMap<>();
                    rv.put("iddosar", ids);
                    rv.put("idstadiudosar", idstadiudosar);
                    rv.put("dosarnou", "0");
                    return rv;
                } else {
                    query = "SELECT id FROM " + client.getBazaDeDate() + ".stadiidosar WHERE iddosar=? AND idinstanta=? AND idstadiu=? LIMIT 1";
                    String idstadiudosar = jdbcTemplate.queryForObject(query, String.class, ids, data.get("idinstantaStadiuDosar"), data.get("idstadiu"));
                    Map<String, Object> rv = new HashMap<>();
                    rv.put("iddosar", ids);
                    rv.put("idstadiudosar", idstadiudosar);
                    rv.put("dosarnou", "0");
                    return rv;
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
                    idod=jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
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
                return rv;
            }

        } catch (Exception e) {
            System.err.println("Error saving dosar: " + e.getMessage());
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
                    // Insert
                    String idd = data.get("id").toString();
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".stadiidosar(iddosar, idstadiu, data, idinstanta, idsectie, judecatori) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertSql, data.get("iddosar"), data.get("idstadiu"),
                                data.get("data"), data.get("idinstanta"), data.get("idsectie"), data.get("judecatori"));

                        String idsd = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", String.class);
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", data.get("iddosar"));
                        did.put("tip", "Adaugare stadiu dosar");
                        did.put("descriere", "stadiidosar[" + idsd + "],stadii[" + data.get("idstadiu") +
                                "],instante[" + data.get("idinstanta") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));

                        return idsd;
                    } else {
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".stadiidosar SET iddosar = ?, idstadiu = ?, data = ?, " +
                                "idinstanta = ?, idsectie = ?, judecatori = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("iddosar"), data.get("idstadiu"),
                                data.get("data"), data.get("idinstanta"), data.get("idsectie"),
                                data.get("judecatori"), idd);
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", data.get("iddosar"));
                        did.put("tip", "Modificare stadiu dosar");
                        did.put("descriere", "stadiidosar[" + idd + "],stadii[" + data.get("idstadiu") +
                                "],instante[" + data.get("idinstanta") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"),
                                did.get("tip"), did.get("descriere"), did.get("data"));

                        return idd;
                    }
                }
            } else {
                return ""; // Incorrect data, nothing to insert
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
                    return id;
                } else {
                    // Stadii with provided parameters doesn't exist, insert it
                    String id =  optStadii.get("id");
                    if (id == null) {
                        optStadii.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".stadii(idmaterie, denumire) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, optStadii.get("idmaterie"), optStadii.get("denumire"));

                        Integer ids = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
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

                        return Integer.parseInt(id);
                    }
                }
            } else {
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

                if (Objects.equals(cnt, 0)) {
                    // Entry already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".calitati WHERE denumire = ? AND idstadiu = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            data.get("denumire"), data.get("idstadiu"));
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

                        Integer idc = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
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

                        return Integer.parseInt(idd);
                    }
                }
            } else {
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
                    return id;
                } else {
                    // Insert new denumire/idmaterie or update existing
                    String idd = data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".obiecte(idmaterie, denumire) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, data.get("idmaterie"), data.get("denumire"));

                        Integer insertedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                        assert insertedId != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("idmaterie", data.get("idmaterie"));
                        did.put("denumire", data.get("denumire"));
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(idmaterie, denumire, data) " +
                                "VALUES (?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("idmaterie"), did.get("denumire"), did.get("data"));

                        return insertedId;
                    } else {
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".obiecte SET idmaterie = ?, denumire = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("idmaterie"), data.get("denumire"), idd);

                        Map<String, Object> did = new HashMap<>();
                        did.put("idmaterie", data.get("idmaterie"));
                        did.put("denumire", data.get("denumire"));
                        did.put("data", "NOW() + " + client.getBazaDeDate());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(idmaterie, denumire, data) " +
                                "VALUES (?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("idmaterie"), did.get("denumire"), did.get("data"));

                        return Integer.parseInt(idd);
                    }
                }
            } else {
                // Try to insert a record without both parameters
                String id =data.get("id");
                if (id == null) {
                    data.put("idmaterie", "");
                    data.put("denumire", "");
                    data.remove("id");
                    String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".obiecte(idmaterie, denumire) " +
                            "VALUES (?, ?)";

                    jdbcTemplate.update(insertSql, data.get("idmaterie"), data.get("denumire"));

                    Integer insertedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                    assert insertedId != null;
                    Map<String, Object> did = new HashMap<>();
                    did.put("idmaterie", data.get("idmaterie"));
                    did.put("denumire", data.get("denumire"));
                    did.put("data", now());

                    String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(idmaterie, denumire, data) " +
                            "VALUES (?, ?, ?)";

                    jdbcTemplate.update(insertHistoricSql, did.get("idmaterie"), did.get("denumire"), did.get("data"));

                    return insertedId;
                } else {
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".obiecte SET idmaterie = ?, denumire = ? WHERE id = ?";

                    jdbcTemplate.update(updateSql, data.get("idmaterie"), data.get("denumire"), id);

                    Map<String, Object> did = new HashMap<>();
                    did.put("idmaterie", data.get("idmaterie"));
                    did.put("denumire", data.get("denumire"));
                    did.put("data", now());

                    String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(idmaterie, denumire, data) " +
                            "VALUES (?, ?, ?)";

                    jdbcTemplate.update(insertHistoricSql, did.get("idmaterie"), did.get("denumire"), did.get("data"));

                    return Integer.parseInt(id);
                }
            }
        } catch (Exception e) {
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
                    return id;
                } else {
                    // Insert new parte or update if it has an id
                    String idd = data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".parti(numeprenume, societate) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, data.get("numeprenume"), data.get("societate"));

                        Integer insertedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
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

                        return Integer.parseInt(idd);
                    }
                }
            } else {
                return 0; // Cannot insert a parte without nume or societate
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error occurred
        }

    }
    public int saveMaterii(Map<String,String> data){
        try {

            // Check for duplicate data
            if (!data.get("denumirejustro").trim().isEmpty() || !data.get("denumire").trim().isEmpty()) {
                String selectSql = "SELECT COUNT(*) AS cnt FROM " + client.getBazaDeDate() + ".materii WHERE denumirejustro = ? OR denumire = ?";

                Integer cnt = jdbcTemplate.queryForObject(selectSql, Integer.class,
                        data.get("denumirejustro"), data.get("denumire"));

                if (!Objects.equals(cnt, 0)) {
                    // Entry with the same denumire or denumirejustro already exists, return its id
                    String selectIdSql = "SELECT id FROM " + client.getBazaDeDate() + ".materii WHERE denumirejustro = ? OR denumire = ? LIMIT 1";

                    Integer id = jdbcTemplate.queryForObject(selectIdSql, Integer.class,
                            data.get("denumirejustro"), data.get("denumire"));
                    assert id != null;
                    return id;
                } else {
                    // Insert new denumire or update if it has an id
                    if (data.get("denumire").trim().isEmpty())
                        data.put("denumire", data.get("denumirejustro"));
                    if (data.get("denumirejustro").trim().isEmpty())
                        data.put("denumirejustro", data.get("denumire"));

                    String idd =  data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".materii(denumire, denumirejustro) " +
                                "VALUES (?, ?)";

                        jdbcTemplate.update(insertSql, data.get("denumire"), data.get("denumirejustro"));

                        Integer insertedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                        assert insertedId != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Adaugare materie");
                        did.put("descriere", "materii[" + insertedId + "],info[" + data.get("denumire") + " / " + data.get("denumirejustro") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));

                        return insertedId;
                    } else {
                        data.put("id", idd);
                        String updateSql = "UPDATE " + client.getBazaDeDate() + ".materii SET denumire = ?, denumirejustro = ? WHERE id = ?";

                        jdbcTemplate.update(updateSql, data.get("denumire"), data.get("denumirejustro"), idd);

                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Modificare materie");
                        did.put("descriere", "materii[" + idd + "],info[" + data.get("denumire") + " / " + data.get("denumirejustro") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));

                        return Integer.parseInt(idd);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error occurred
        }

        return 0; // Cannot insert a materie without denumirejustro or denumire
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
                    return id;
                } else {
                    // Insert new denumire or update if it has an id
                    String idd =  data.get("id");
                    if (idd == null) {
                        data.remove("id");
                        String insertSql = "INSERT INTO " + client.getBazaDeDate() + ".sectii(denumire) VALUES (?)";

                        jdbcTemplate.update(insertSql, data.get("denumire"));

                        Integer insertedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                        assert insertedId != null;
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", 0);
                        did.put("tip", "Adaugare sectie");
                        did.put("descriere", "sectii[" + insertedId + "],info[" + data.get("denumire") + "]");
                        did.put("data", now());

                        String insertHistoricSql = "INSERT INTO " + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        jdbcTemplate.update(insertHistoricSql, did.get("iddosar"), did.get("iduser"), did.get("tip"),
                                did.get("descriere"), did.get("data"));

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

                        return Integer.parseInt(idd);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error occurred
        }

        return 0; // Cannot insert a sectie without denumire
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
    public void actualizareDataum(Map<String, Object> data) {
        if (!data.containsKey("id") || data.get("id") == null) {
            return;
        }
        Timestamp dataum = (Timestamp) data.get("dataum");

        String selectSql = "SELECT dataum, dataumSCJ FROM " + client.getBazaDeDate() + ".dosare WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, data.get("id"));

        Timestamp dpjr = (Timestamp) result.get("dataum");
        Timestamp dscj = (Timestamp) result.get("dataumSCJ");

        try {
            if (data.containsKey("flagpjr") && data.get("flagpjr") != null) {
                if (dpjr == null || dataum.after(dpjr)) {
                    // Update the dataum field for PJR synchronization
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".dosare SET dataum = ? WHERE id = ?";
                    jdbcTemplate.update(updateSql, dataum, data.get("id"));
                }
            }

            if (data.containsKey("flagscj") && data.get("flagscj") != null) {
                if (dscj == null || dataum.after(dscj)) {
                    // Update the dataumSCJ field for SCJ synchronization
                    String updateSql = "UPDATE " + client.getBazaDeDate() + ".dosare SET dataumSCJ = ? WHERE id = ?";
                    jdbcTemplate.update(updateSql, dataum, data.get("id"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
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
            // Handle the exception, log or print an error message
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
