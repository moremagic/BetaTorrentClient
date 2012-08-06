/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package betatorrentclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
        // TODO code application logic here
        File f = new File("C:/Users/mitsu/Downloads/CentOS-5.3-i386-bin-DVD.torrent");
        trackerGET(f);
    }

    public static void trackerGET(File f) {
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
                param.put("compact", "0");                          //レスポンスで返すピアのリストを従来のbencodeで返すか、IPとポート番号のバイナリ値で返すかを制御します。1に設定されている場合はバイナリ値で返します。これは、以前のBitTorrentがbencodeで設定されていることを前提としていたため、下位互換性のために作成されました。
                param.put("event", "started");                      //イベント名(started,stopped,completed)

                String strParam = "";
                for (String key : param.keySet()) {
                    if (strParam.length() > 0) {
                        strParam += "&";
                    }
                    strParam += key + "=" + param.get(key);
                }

                //Socket通信
                Map m = BenCodingUtil.parseBencoding(urlConnection(url, strParam));
                BenCodingUtil.debugPrint(m);
            } catch (Exception ex) {
                Logger.getLogger(BetaTorrentClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * URLConnection 通信
     *
     * @param url
     * @param strParam
     */
    public static byte[] urlConnection(URL url, String strParam) throws MalformedURLException, IOException {
        byte[] ret = new byte[0];
        Socket socket = null;
        try {
            //debug code
            System.out.println(">> " + new URL(url + "?" + strParam));

            URLConnection connection = new URL(url + "?" + strParam).openConnection();
            connection.setRequestProperty("User-Agent", "test");
            connection.setReadTimeout(3000);
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Accept", "text/html");


            BufferedInputStream br = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int cnt = -1;
            byte[] buf = new byte[1024];
            while ((cnt = br.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, cnt);
            }

            ret = out.toByteArray();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }


        return ret;
    }
}
