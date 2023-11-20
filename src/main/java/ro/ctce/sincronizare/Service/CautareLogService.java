package ro.ctce.sincronizare.Service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
@Service
public class CautareLogService {
    public String cautareLogNrDosar(String nrDosar) {
        File directory = new File("autosync/log-sincronizari");

        if (directory.exists() && directory.isDirectory()) {
            return searchFilesForString(directory, nrDosar);
        } else {
            System.out.println("Invalid directory path");
        }
        return "Eroare Interna";
    }
    public String cautareLogIdClient(String idClient) {
        File directory = new File("autosync/log-sincronizari");

        if (directory.exists() && directory.isDirectory()) {
            return searchFilesForString(directory, "Id client: "+idClient);
        } else {
            System.out.println("Invalid directory path");
        }
        return "Eroare Interna";
    }
    public static String searchFilesForString(File directory, String searchString) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                StringBuilder txt = new StringBuilder();
                for (File file : files) {
                    if (file.isDirectory()) {
                        String result = searchFilesForString(file, searchString);
                        txt.append(result);
                    } else {
                        try {
                            String fileContent = searchAndReturnFileContent(file, searchString);
                            if (fileContent != null) {
                                txt.append("Nume Fisier: ").append(file.getName());
                                txt.append(System.lineSeparator());
                                txt.append("Data Incepere Sincronizare: ").append(directory.getName());
                                txt.append(System.lineSeparator());
                                txt.append("Continut:\n").append(fileContent);
                                txt.append(System.lineSeparator());

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return txt.toString();
            }
        }
        return "Numar Dosar: " + directory.getName() + " invalid";
    }

    public static String searchAndReturnFileContent(File file, String searchString) throws IOException {
        System.out.println("Searching for: " + searchString);
        StringBuilder fileContent = new StringBuilder();

        try (Scanner scanner = new Scanner(file)) {
            boolean searchStringFound = false;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                fileContent.append(line).append(System.lineSeparator());
                if (line.contains(searchString)) {
                    searchStringFound = true;
                }
            }

            if (searchStringFound) {
                return fileContent.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("SearchString not found in the file.");
        return null;
    }



}
