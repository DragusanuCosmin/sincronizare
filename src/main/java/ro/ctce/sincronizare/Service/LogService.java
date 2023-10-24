package ro.ctce.sincronizare.Service;

import ro.ctce.sincronizare.Entities.Clienti;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogService {
public static void log(Clienti client){
    String datastartsync = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(new Date());
    StringBuilder txt = new StringBuilder();
    txt.append("Id client: ").append(client.getUserId()).append(System.lineSeparator());
    txt.append("Data incepere sincronizare: ").append(datastartsync).append(System.lineSeparator());
    txt.append("Data sfarsire sincronizare: ").append(new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(new Date())).append(System.lineSeparator());
    txt.append("Dosar actualizate");
    String log = txt + System.lineSeparator() + "-------------------------" + System.lineSeparator();
    txt.setLength(0);
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy.MM");
    String month = monthFormat.format(new Date());

    String logDirectory = "autosync/log-sincronizari/" + month;
    if (!new java.io.File(logDirectory).isDirectory()) {
        System.out.println(new java.io.File(logDirectory).mkdirs());
    }

    String logFileName = "/sincronizare.1_" + System.currentTimeMillis() + ".log";
    String logFilePath = logDirectory + logFileName;

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
        writer.write(log);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
