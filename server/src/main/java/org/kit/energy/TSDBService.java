package org.kit.energy;

import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * Class to send/get data to/from a OpenTSDB database
 */
@Component
public class TSDBService {

    /**
     * Example of how to send data to a openTSDB database via Telnet with java.
     */
    public void telIt(){
        // POST via telnet, but not so important as getting the data
        long unixTimestamp = Instant.now().getEpochSecond();
        int randomNum = ThreadLocalRandom.current().nextInt(100, 200 + 1);
        System.out.println("new datapoint via tellnet will be added!");
        String[] strArray = new String[0];
        //Example.main(strArray);
        try(Socket sock = new Socket("localhost", 4242)){
            String point = "put NewMetrik "+ unixTimestamp + " " + randomNum  + " tag=key\n";
            sock.getOutputStream().write(point.getBytes());
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Gets the time series as json from a openTSDB database.
     * @param url The URL pointing to the location of the TSDB database.
     */
    public String getIt(String url){
        try{
            // for example "http://localhost:4242/api/query?start=1497225600&end=1497312000&m=sum:NewMetrik&{tag=key}";
            if(!url.startsWith("http://") && !url.startsWith("https://")){
                url = "http://"+url;
            }
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", USER_AGENT);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        }
        catch(Exception e){
            System.out.println(e.toString());
        }

        return "";
    }
}
