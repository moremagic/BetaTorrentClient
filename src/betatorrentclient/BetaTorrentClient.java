/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package betatorrentclient;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mitsu
 */
public class BetaTorrentClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    ServerSocket ssoc = new ServerSocket(6881);
                    while (true) {
                        Socket soc = ssoc.accept();
                        BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));

                        String line = "";
                        while ((line = br.readLine()) != null) {
                            System.out.println(">[ " + line);
                        }
                        soc.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BetaTorrentClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

//        t.start();


        trackerGET();

    }

    public static void trackerGET() {
        // TODO code application logic here
        File f = new File("C:/Users/mitsu/Downloads/CentOS-5.3-i386-bin-DVD.torrent");

        TorrentBean torrent = new TorrentBean(f);
        for (URI uri : torrent.getAnnounceList()) {
            try {
                URL url = uri.toURL();

                Map<String, String> param = new LinkedHashMap<String, String>();
                param.put("info_hash", torrent.getInfoHash());
                param.put("peer_id", URLEncoder.encode(torrent.getPeer_id(), "UTF-8"));        //通信ごとに割り当てられるランダムな値
                param.put("port", "6881");                          //UP専用ポート番号は6881番から6889番の間で最初にbindできるポート
                param.put("uploaded", "0");                         //UL済みbyte数
                param.put("downloaded", "0");                       //DL済みbyte数
                param.put("left", "155062");                        //ファイルの全体のサイズから現在ダウンロードが完了したピースのバイト数
                param.put("compact", "1");                          //レスポンスで返すピアのリストを従来のbencodeで返すか、IPとポート番号のバイナリ値で返すかを制御します。1に設定されている場合はバイナリ値で返します。これは、以前のBitTorrentがbencodeで設定されていることを前提としていたため、下位互換性のために作成されました。
                param.put("event", "started");                      //イベント名(started,stopped,completed)

                String strParam = "";
                for (String key : param.keySet()) {
                    if (strParam.length() > 0) {
                        strParam += "&";
                    }
                    strParam += key + "=" + param.get(key);
                }
                
                //Socket通信
                socketTestConnection(url, strParam);
                httpTestConnection(url, strParam);
            } catch (Exception ex) {
                Logger.getLogger(BetaTorrentClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Socket 通信
     * 
     * @param url
     * @param strParam 
     */
    public static void socketTestConnection(URL url, String strParam) {
        int port = (url.getPort() == -1) ? 80 : url.getPort();
        Socket socket = null;
        try {
            try {
                socket = new Socket(url.getHost(), port);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bw.write("GET " + url.getPath() + "?" + strParam + " HTTP/1.0\r\n");
                bw.write("Host: " + url.getHost() + ":" + url.getPort() + " \r\n");
                bw.write("Accept-encoding: gzip, deflate\r\n");
                bw.write("User-agent: MyTorrentClient/0.0.1\r\n");
                bw.write("Connection: keep-alive\r\n");
                bw.write("\r\n");
                bw.flush();


                String line = "";
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }

            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    
    /**
     * Socket 通信
     * 
     * @param url
     * @param strParam 
     */
    public static void httpTestConnection(URL url, String strParam) {
        Socket socket = null;
        try {
            try {
                //debug code
                System.out.println(">> " + new URL(url + "?" + strParam));                
                
                URLConnection connection = new URL(url + "?" + strParam).openConnection();
                connection.setRequestProperty("User-Agent", "test");
                connection.setReadTimeout(3000);
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty("Accept", "text/html");

                
                Reader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int cnt = -1;
                char[] buf = new char[1024];
                while ((cnt = br.read(buf, 0, buf.length)) != -1) {
                    System.out.println(new String(buf, 0, cnt));
                }
     
                

            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    
}
