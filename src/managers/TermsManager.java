package managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class TermsManager {
    private static final String filepath = System.getProperty("user.dir") + "/resources/txt/consentform.txt";
    public static String getConsentForm() {
        StringBuilder consentBuilder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(filepath); InputStreamReader isr = new InputStreamReader(fis); BufferedReader br = new BufferedReader(isr)){
            String line;
            while ((line = br.readLine()) != null) {
                consentBuilder.append(line).append("\n");
            }
            return consentBuilder.toString();
        } catch (FileNotFoundException e) {
            System.out.println("No encontre el archivo");
        } catch (IOException e) {
            System.out.println("mori");
        }
        return "Aqui van los terminos y condiciones o la forma de consentimiento";
    }
}
