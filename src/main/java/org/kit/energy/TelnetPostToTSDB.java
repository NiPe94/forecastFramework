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
 * Created by qa5147 on 07.06.2017.
 */
@Component
public class TelnetPostToTSDB {
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

    public void getIt(){
        try{
            System.out.println("new data via http GET will be loaded!");
            String url = "http://localhost:4242/api/query?start=1497225600&m=sum:NewMetrik";
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println("The resulting String:");
            System.out.println(response.toString());

            JSONDataPreperator jsonDataPreperator = new JSONDataPreperator();
            SparkSession spark = SparkSession.builder().master("local").getOrCreate();
            InputFile myFile = new TSDBFile();
            jsonDataPreperator.prepareDataset(myFile,spark);

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
