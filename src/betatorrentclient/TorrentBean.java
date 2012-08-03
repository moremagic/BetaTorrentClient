/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package betatorrentclient;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Torrentファイルを抽象化するクラス
 *
 * @author mitsu
 */
public class TorrentBean{
    private File mf = null;
    private Map mMap = null;
    private String peer_id = null;

    public TorrentBean(File f) {
        mf = f;
        mMap = BenCodingUtil.parceTorrentFile(mf);
    }

    /**
     * トラッカのURIリストを返却する
     *
     * @return
     */
    public URI[] getAnnounceList() {
        List<URI> ret = new ArrayList<URI>();
        
        if (get("announce-list") instanceof List) {
            List l = (List) get("announce-list");
            for (int i = 0; i < l.size(); i++) {
                if (l.get(i) instanceof List) {
                    for (Object o : (List) l.get(i)) {
                        try {
                            ret.add(new URL(BenCodingUtil.object2String(o)).toURI());
                        } catch (URISyntaxException | MalformedURLException ex) {
                            //無視
                        }
                    }
                } else {
                    try {
                        ret.add(new URL(BenCodingUtil.object2String(l.get(i))).toURI());
                    } catch (URISyntaxException | MalformedURLException ex) {
                            //無視
                    }
                }
            }
        } else {
            try {
                ret.add(new URL(BenCodingUtil.object2String(get("announce"))).toURI());
            } catch (URISyntaxException | MalformedURLException ex) {
                //無視
            }
        }
        
        return ret.toArray(new URI[0]);
    }

    /**
     * peer_id を返却します
     *
     * @return
     */
    public String getPeer_id() {
        if (peer_id == null) {
            peer_id = createPeerID();
        }
        return peer_id;
    }

    /**
     * info-hash を取得する
     * 
     * @return 
     */
    public String getInfoHash() throws Exception{
        byte[] buf = BenCodingUtil.object2Bencoding(get("info"));
        String ret = "";
        for(byte b :getSHA1StringDigest(buf)){
            ret += String.format("%%%x", b);
        }        
        return ret;
    }
    
    /**
     * peer_id を生成するユーティリティ
     *
     * @return
     */
    private static String createPeerID() {
        return String.format("EX_%017d", System.nanoTime());
    }

    /**
     * info_hash 文字列を作り出すためのユーティリティ
     * 
     * @param data
     * @return
     * @throws Exception
     */
    private static byte[] getSHA1StringDigest(byte[] data) throws Exception {
        //文字列からダイジェストを生成する
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(data);
        return md.digest();
    }

    /**
     * Mapにアクセスする場合は必ずこのメソッドを使用する
     * 
     * @param key
     * @return 
     */
    private Object get(String key){
        Object ret = null;
        for(Object k : mMap.keySet().toArray()){
            if( Arrays.equals((byte[])k, key.getBytes()) ){
                ret = mMap.get(k);
                break;
            }
        }
        return ret;
    }
}
