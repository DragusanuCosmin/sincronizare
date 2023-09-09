package ro.ctce.sincronizare.Dao;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ro.ctce.sincronizare.Entities.*;
import ro.ctce.sincronizare.Mapper.DataObjectRowMapper;
import ro.ctce.sincronizare.Mapper.DosareRowMapper;
import ro.ctce.sincronizare.Service.FileService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Repository("InserareDao")
public class InserareDataAccessService implements InserareDao {
    private final FileService fileService;
    private final JdbcTemplate jdbcTemplate;
    private Clienti client;

    Date now = Date.from(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.ofHours(3)));
    int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int month = Calendar.getInstance().get(Calendar.MONTH);

    @Autowired
    public InserareDataAccessService(FileService fileService, JdbcTemplate jdbcTemplate) {
        this.fileService = fileService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void AdaugareDatabase(Clienti client) {
            this.client=client;
            String sql="select * from dosare_"+client.getBazaDeDate()+".dosare where userid=?";
            List<Dosar> dosare = jdbcTemplate.query(sql,new DosareRowMapper(),client.getUserId());
            for (Dosar dosar:dosare) {

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
                    List<Parti> partiList = new ArrayList<>();
                    for (int i = 0; i < Math.min(dosarSolr.getCalitateparte().size(), dosarSolr.getNumeparte().size()); i++) {
                        String nume = dosarSolr.getNumeparte().get(i);
                        String calitateParte = dosarSolr.getCalitateparte().get(i);

                        Parti p = new Parti(nume, calitateParte);
                        partiList.add(p);
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
                    List<Sedinte> sedinteList = new ArrayList<>();
                    for (int i = 0; i < min(Arrays.asList(cnt1, cnt2, cnt3, cnt4, cnt5, cnt6, cnt7, cnt8, cnt9)); i++) {
                        if (dosarSolr.getDatasedinta().get(i).toString().contains("1900-01"))
                            dosarSolr.setDataSedintai(i, new Date());
                        if (dosarSolr.getDatadocument().get(i).toString().contains("1900-01"))
                            dosarSolr.setDataDocumenti(i, new Date());
                        if (dosarSolr.getDatapronuntare().get(i).toString().contains("1900-01"))
                            dosarSolr.setDataPronuntarei(i, new Date());
                        Sedinte s = new Sedinte(
                                dosarSolr.getComplet() != null && dosarSolr.getComplet().size() > i ? dosarSolr.getComplet().get(i) : ".",
                                dosarSolr.getDatasedinta() != null && dosarSolr.getDatasedinta().size() > i ? String.valueOf(dosarSolr.getDatasedinta().get(i)) : ".",
                                dosarSolr.getOra() != null && dosarSolr.getOra().size() > i ? dosarSolr.getOra().get(i) : ".",
                                dosarSolr.getSolutie() != null && dosarSolr.getSolutie().size() > i ? dosarSolr.getSolutie().get(i) : ".",
                                dosarSolr.getSolutiesumar() != null && dosarSolr.getSolutiesumar().size() > i ? dosarSolr.getSolutiesumar().get(i) : ".",
                                dosarSolr.getDatapronuntare() != null && dosarSolr.getDatapronuntare().size() > i ? String.valueOf(dosarSolr.getDatapronuntare().get(i)) : ".",
                                dosarSolr.getDocumentsedinta() != null && dosarSolr.getDocumentsedinta().size() > i ? dosarSolr.getDocumentsedinta().get(i) : ".",
                                dosarSolr.getNumardocument() != null && dosarSolr.getNumardocument().size() > i ? dosarSolr.getNumardocument().get(i) : ".",
                                dosarSolr.getDatadocument() != null && dosarSolr.getDatadocument().size() > i ? String.valueOf(dosarSolr.getDatadocument().get(i)) : "."
                        );
                        sedinteList.add(s);
                    }
                    cnt1 = dosarSolr.getTipcaleatac() != null ? dosarSolr.getTipcaleatac().size() : 0;
                    cnt2 = dosarSolr.getDatadeclarare() != null ? dosarSolr.getDatadeclarare().size() : 0;
                    cnt3 = dosarSolr.getPartedeclaratoare() != null ? dosarSolr.getPartedeclaratoare().size() : 0;

                    List<CaiAtac> caiAtacList = new ArrayList<>();

                    for (int i = 0; i < min(Arrays.asList(cnt1, cnt2, cnt3)); i++) {
                        CaiAtac caleAtac = new CaiAtac(
                                dosarSolr.getTipcaleatac() != null && dosarSolr.getTipcaleatac().size() > i ? dosarSolr.getTipcaleatac().get(i) : ".",
                                dosarSolr.getDatadeclarare() != null && dosarSolr.getDatadeclarare().size() > i ? String.valueOf(dosarSolr.getDatadeclarare().get(i)) : ".",
                                dosarSolr.getPartedeclaratoare() != null && dosarSolr.getPartedeclaratoare().size() > i ? dosarSolr.getPartedeclaratoare().get(i) : "."
                        );
                        caiAtacList.add(caleAtac);
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
                    String iduser = client.getUserId();
                    Map<String, String> valMaterie = new HashMap<>();
                    valMaterie.put("denumire", materieJustRo);
                    valMaterie.put("denumirejustro", materieJustRo);
                    int idMaterie = saveMaterii(valMaterie);
                    Map<String, String> valObiect = new HashMap<>();
                    valObiect.put("idmaterie", String.valueOf(idMaterie));
                    valObiect.put("denumire", obiectDosar);
                    int idobiect = saveObiecte(valObiect);
                    Map<String, String> valParte = new HashMap<>();
                    valParte.put("numeprenume", partiList.get(0).getNumeParte());
                    valParte.put("societate", partiList.get(0).getNumeParte());
                    int idclient = saveParti(valParte);
                    Map<String, String> optStadii = new HashMap<>();
                    optStadii.put("denumire", dosarSolr.getStadiu());
                    optStadii.put("idmaterie", String.valueOf(idMaterie));
                    int idstadiu = saveStadii(optStadii);

                    Map<String, String> valCalitate = new HashMap<>();
                    valCalitate.put("denumire", partiList.get(0).getCalitateParte());
                    valCalitate.put("idstadiu", String.valueOf(idstadiu));
                    valCalitate.put("calitateclient", "0");
                    int idcalitate = saveCalitati(valCalitate);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    //insert into dosare
                    Dosar dosarDB = new Dosar(
                            Integer.parseInt(dosarSolr.getId()),
                            1,
                            Date.from(dosarSolr.getData().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                            now,
                            null,
                            '1',
                            numardosar,
                            instantadosar,
                            andosar,
                            accesoriidosar,
                            null,
                            0,//?
                            0,//?
                            null,
                            idclient,
                            '1',
                            false,
                            false,//?
                            false,//?
                            "",
                            null,
                            null,
                            0,
                            0,//?
                            null
//                            idInstantaStadiuDosar,
//                            dataStadiuDosar,
//                            idstadiu,
//                            idSectie,
                    );
                    int[] iduri = saveDosar(dosarDB);
                    int iddosar = iduri[0];
                    int idstadiudosar = iduri[1];
                    Map<String, String> valPD = new HashMap<>();
                    valPD.put("idparte", String.valueOf(idclient));
                    valPD.put("idcalitate", String.valueOf(idcalitate));
                    valCalitate.put("idstadiudosar", String.valueOf(idstadiudosar));
                    int idPDClient = savePartiDosar(valPD);

                    Map<String, String> valObDosar = new HashMap<>();
                    //valObDosar.put("iddosar", iddosar);
                    valObDosar.put("idobiect", String.valueOf(idobiect));
                    System.out.println(saveObiecteDosar(valObDosar));
                    for (Sedinte termen : sedinteList) {
                        Map<String, String> valTD = new HashMap<>();
                        valTD.put("id", "");
                        valTD.put("datatermen", termen.getDataSedinta().substring(0, dosarSolr.getData().toString().indexOf('T')) + " " + termen.getOra());
                        valTD.put("tipsolutie", termen.getSolutie().replace("'", "").replace("[", "").replace("]", ""));
                        valTD.put("sumarsolutie", termen.getSolutiesumar().replace("'", "").replace("[", "").replace("]", ""));
                        valTD.put("complet", termen.getComplet());
                        valTD.put("idstadiudosar", String.valueOf(idstadiudosar));
                        valTD.put("datadocument", termen.getDatadocument());
                        valTD.put("numarDocument", termen.getNumardocument());
                        int termenid = saveTermen(valTD,dosar.getId());
                    }
                }
            }
                        String datastartsync = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(now);
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
    public int saveTermen(Map<String, String> data, int iddosar) {
        try {
            String timezone = "Europe/Bucharest";
            TimeZone.setDefault(TimeZone.getTimeZone(timezone));

            if (StringUtils.isNotBlank(data.get("datatermen")) && data.get("idstadiudosar") != null) {
                String datat = new SimpleDateFormat("yyyy-MM-dd").format(data.get("datatermen"));
                String datatermen = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data.get("datatermen"));

                String qcomplet = "";

                if (StringUtils.isNotBlank(data.get("complet")) && !"-".equals(data.get("complet"))) {
                    qcomplet = " AND (complet = " + data.get("complet") +" OR complet IS NULL OR complet = '-' OR complet = '') ";
                }

                String sql = "SELECT COUNT(*) AS cnt FROM dosare_"+client.getBazaDeDate()+".termenedosar WHERE datatermen = ? AND idstadiudosar = ? " + qcomplet;
                Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, datat, data.get("idstadiudosar"));

                if (Objects.equals(cnt, 0)) {
                    String updateSql = "UPDATE dosare_"+client.getBazaDeDate()+".termenedosar SET datatermen = ?, tipsolutie = ?, sumarsolutie = ?, " +
                            "dataDocument = ?, numarDocument = ?, complet = ? " +
                            "WHERE id = ?";

                    int rowsUpdated = jdbcTemplate.update(updateSql, datatermen, data.get("tipsolutie"), data.get("sumarsolutie"), data.get("datadocument"), data.get("numarDocument"), data.get("complet"), data.get("id"));

                    if (rowsUpdated > 0) {

                        return Integer.parseInt(data.get("id"));
                    }
                } else {
                    String insertSql = "INSERT INTO dosare_"+client.getBazaDeDate()+".termenedosar (datatermen, tipsolutie, sumarsolutie, idstadiudosar, dataDocument, numarDocument, dataluatlacunostinta, complet, sala, dataIntrare) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    int rowsInserted = jdbcTemplate.update(insertSql, datatermen, data.get("tipsolutie"), data.get("sumarsolutie"), data.get("idstadiudosar"), data.get("datadocument"), data.get("numarDocument"), now, data.get("complet"), data.get("sala"), now);

                    if (rowsInserted > 0) {
                        Map<String, Object> did = new HashMap<>();
                        did.put("iduser", client.getUserId());
                        did.put("iddosar", iddosar);
                        did.put("tip", "Modificare termen dosar");
                        String descriere = "termenedosar[" + data.get("id") + "],stadiidosar[" + data.get("idstadiudosar") + "],info[" + data.get("datatermen") + " - " + data.get("tipsolutie") + "]";
                        did.put("descriere", descriere);
                        did.put("data", now);

                        sql = "INSERT INTO dosare_"+client.getBazaDeDate()+".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES (?, ?, ?, ?, ?)";
                        Object[] params = { did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data") };

                        jdbcTemplate.update(sql, params);
                        int esteMonit=dosarisMonitorizat(iddosar, Integer.parseInt(client.getUserId()));
                        int estesincronizat=dosarisSincronizat(iddosar, Integer.parseInt(client.getUserId()));
//                        if(estesincronizat+esteMonit>1&&data.get("datatermen")!=null){
//                            //TODO  se adauga atentionare la termen cu notificare pe email/sms
//                        }
                        return Math.toIntExact(jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class));
                    }
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    public int saveObiecteDosar(Map<String, String> data) {
        if (!data.get("iddosar").isEmpty() && !data.get("idobiect").isEmpty()) {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM dosare_" + client.getBazaDeDate() + ".obiectedosar WHERE iddosar=? AND idobiect=?",
                    Integer.class,
                    data.get("iddosar"), data.get("idobiect"));

            if (!Objects.equals(cnt, 0)) {
                Integer id = jdbcTemplate.queryForObject(
                        "SELECT id FROM dosare_" + client.getBazaDeDate() + ".obiectedosar WHERE iddosar=? AND idobiect=?",
                        Integer.class, data.get("iddosar"), data.get("idobiect"));
                assert id != null;
                return id;
            } else {
                    String insertQuery = "INSERT INTO obiectedosar(iddosar, idobiect) VALUES(?, ?)";
                    jdbcTemplate.update(insertQuery, data.get("iddosar"), data.get("idobiect"));
                    Integer idod = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                    assert idod != null;
                    Map<String, Object> did=new HashMap<>();
                    did.put("iduser", client.getUserId());
                    did.put("iddosar", data.get("iddosar"));
                    did.put("tip", "Adaugare obiect dosar");
                    did.put("descriere", "obiectedosar[" + idod + "],obiecte[" + data.get("idobiect") + "]");
                    did.put("data", now);

                    jdbcTemplate.update(
                            "INSERT INTO dosare_" + client.getBazaDeDate() + ".istoricdosare(iddosar, iduser, tip, descriere, data) VALUES(?, ?, ?, ?, ?)",
                            did.get("iddosar"), did.get("iduser"), did.get("tip"), did.get("descriere"), did.get("data"));
                    return idod;
            }
        }
        return -1;
    }
    public int savePartiDosar(Map<String,String> valPD){
        return 1;
    }
    public int[] saveDosar(Dosar dosarDB){
        return new int[]{1,1};
    }
    public int saveStadii(Map<String,String> optStadii){
        return 1;
    }
    public int saveCalitati(Map<String,String> valCalitate){
        return 1;
    }
    public int saveObiecte(Map<String,String> valObiect){
        return 1;
    }
    public int saveParti(Map<String,String> valParte){
        return 1;
    }
    public int saveMaterii(Map<String,String> valMaterie){
        return 1;
    }

    public int saveSectii(Map<String,String> valSectie){
        return 1;
    }
    public int getIdInstantaByDenumirejustro(String denumireJustro){
        return 1;
    }
    public void actualizareDataum(int id) throws SQLException, ParseException {
        if (id==0) {
            System.out.println("no ID");
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String sql ="SELECT dataum, dataumSCJ FROM dosare_"+client.getBazaDeDate()+".dosare_"+client.getBazaDeDate()+".dosare WHERE id = ?";
        String dpjr = null;
        String dscj = null;

        List<DataObject> resultList = jdbcTemplate.query(sql, new DataObjectRowMapper(), id);
        if (!resultList.isEmpty()) {
            DataObject result = resultList.get(0);
            dpjr = result.getDataum();
            dscj = result.getDataumSCJ();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long dpjrMillis = dpjr != null ? sdf.parse(dpjr).getTime() : 0;
        long dscjMillis = dscj != null ? sdf.parse(dscj).getTime() : 0;
        long dupdMillis = sdf.parse(String.valueOf(now)).getTime();

        try {
                if (dupdMillis > dpjrMillis || dpjrMillis == 0) {
                    String updateQuery = "UPDATE dosare_"+client.getBazaDeDate()+".dosare SET dataum = ? WHERE id = ?";
                    jdbcTemplate.update(updateQuery, dupdMillis, id);
            }
                if (dupdMillis > dscjMillis || dscjMillis == 0) {
                    String updateQuery = "UPDATE dosare_"+client.getBazaDeDate()+".dosare SET dataumSCJ = ? WHERE id = ?";
                    jdbcTemplate.update(updateQuery, dupdMillis, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Integer dosarisMonitorizat(int iddosar, int iduser) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM dosare_"+client.getBazaDeDate()+".monitorizaridosar WHERE iddosar = ? AND iduser = ? AND monitorizat = 1";
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, iddosar, iduser);
            return cnt != null ? cnt : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Integer dosarisSincronizat(int iddosar, int iduser) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM dosare_"+client.getBazaDeDate()+".monitorizaridosar WHERE iddosar = ? AND iduser = ? AND sincronizat = 1";
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, iddosar, iduser);
            return cnt != null ? cnt : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean existaDosar(String numardosar, String instantadosar, String andosar, String accesoriidosar) {
        try {
            String sql = "SELECT COUNT(*) AS cnt FROM dosare_"+client.getBazaDeDate()+".dosare " +
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
            String sql = "SELECT email FROM dosare_"+client.getBazaDeDate()+".users WHERE id = ?";

            return jdbcTemplate.queryForObject(sql, String.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getUsernameByIdUsers(String id) {
        try {
            String sql = "SELECT name FROM dosare_"+client.getBazaDeDate()+".users WHERE id = ?";
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

        final String sql="select * from dosare_"+client.getBazaDeDate()+".partidosar where idstadiudosar=?";
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
            String sql = "SELECT descriere FROM dosare_"+client.getBazaDeDate()+".tipParte WHERE id = ?";
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
            String sql = "SELECT COUNT(*) AS cnt FROM dosare_"+client.getBazaDeDate()+".firmemonitorizatedosar WHERE emailmonitorizare = ? AND codcui = ? AND iddosar = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, email, cui, iddosar);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public  Map<String, Object> findDupaParti(int id) {
        try {
            String sql = "SELECT * FROM dosare_"+client.getBazaDeDate()+".parti WHERE id = ?";
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
            String sql = "SELECT * FROM dosare_"+client.getBazaDeDate()+".calitati WHERE id = ?";
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
            String sql = "SELECT * FROM dosare_"+client.getBazaDeDate()+".stadiidosar WHERE id = ?";
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
            String sql = "SELECT * FROM dosare_"+client.getBazaDeDate()+".dosare WHERE id = ?";
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
    public String getListaPartiDosar(int iddosar) {
        StringBuilder sirparti = new StringBuilder();
        try {
            String idstadiudosarSql = "SELECT id FROM dosare_"+client.getBazaDeDate()+".stadiidosar WHERE iddosar = ? ORDER BY data DESC LIMIT 1";
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
            String sql = "SELECT * FROM dosare_"+client.getBazaDeDate()+".parti WHERE id IN (" + id + ")";
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
