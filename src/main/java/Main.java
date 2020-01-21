import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String SITE_URL = "https://geco.ethz.ch";
    private static final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) {
        String dom = "";
        try{
            dom = sendGet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Matcher m = Pattern.compile("<div class=\"progressBarPrecentage\">\\s*(\\d+)\\s*/\\s*(\\d+)\\s*</div>").matcher(dom);

        Properties prop = getProperties();

        StringBuilder messageText = new StringBuilder();
        boolean changed = false;
        int i = 0;
        while (m.find()) {
            String key = String.format("BAR_%d", i);
            String oldValue = prop.getProperty(key);
            String newValue = String.format("%s/%s", m.group(1), m.group(2));
            if (!oldValue.equals(newValue)) {
                changed = true;
            }
            prop.setProperty(key, newValue);

            messageText.append(newValue + "\n");
            i++;
        }

        saveProperties(prop);

        if(changed) {
            sendMessage(messageText.toString());
        }
    }

    private static void sendMessage(String text) {
        System.out.println("Message:\n" + text);
        Properties prop = getProperties();
        TelegramBot bot = new TelegramBot(prop.getProperty("TG_API_KEY"));
        bot.execute(new SendMessage(prop.getProperty("TG_CHAT_ID"), text));
    }

    private static Properties getProperties() {
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void saveProperties(Properties prop) {
        try (OutputStream output = new FileOutputStream("config.properties")) {
            // save properties to project root folder
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // HTTP GET request
    private static String sendGet() throws Exception {
        URL obj = new URL(SITE_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        // add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("Sending 'GET'");
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());

        // return result
        return response.toString();
    }
}
