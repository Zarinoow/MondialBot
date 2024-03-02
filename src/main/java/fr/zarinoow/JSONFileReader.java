package fr.zarinoow;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JSONFileReader {

    public static String getJSONAsString(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        // Read the file
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                content.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    public static String getJSONAsString(File file) throws FileNotFoundException {
        return getJSONAsString(new FileInputStream(file));
    }

    public static JsonObject getJSONAsObject(File file) throws FileNotFoundException {
        return JsonParser.parseString(getJSONAsString(file)).getAsJsonObject();
    }

}
