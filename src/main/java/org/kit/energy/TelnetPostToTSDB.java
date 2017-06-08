package org.kit.energy;

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
        System.out.println("Bin schon hier!");
        String[] strArray = new String[0];
        //Example.main(strArray);
        try(Socket sock = new Socket("localhost", 4242)){
            String point = "put NewMetrik "+ unixTimestamp + " " + randomNum  + " tag=key\n";
            sock.getOutputStream().write(point.getBytes());
            System.out.println("geschafft");
        }
        catch(IOException ex){
            System.out.println("Böser Fehler!");
            ex.printStackTrace();
        }
    }

    public void getIt(){
        try{
            System.out.println("start getting bro ;)");
            String url = "http://localhost:4242/api/query?start=1496736000&m=sum:testmetrik";
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
            System.out.println("This is what you get man!:");
            System.out.println(response.toString());

        }
        catch(Exception e){

        }
    }
}
