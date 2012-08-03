/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package betatorrentclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * BenCoding（bee encoding）形式ファイル読み込みのためのユーティリティ
 * Map内に文字列、整数、配列を再帰的に格納した値を返却します
 *
 * @author mitsu
 */
public class BenCodingUtil {

    public static Map parceTorrentFile(File f){
        Map ret = null;
        try{
            List data = new ArrayList();
            InputStream in = null;
            try{
                in = new FileInputStream(f);

                int cnt = 0;
                byte[] buf = new byte[1024];
                while((cnt = in.read(buf, 0, buf.length)) != -1){
                    for(int i = 0 ; i < cnt ; i++){
                        data.add(buf[i]);
                    }
                }


                byte[] rawdata = new byte[ data.size() ];
                for(int i = 0 ; i < data.size() ; i++){
                    rawdata[i] = ((Byte)data.get(i)).byteValue();
                }

                ret =  parseBencoding(rawdata);
            }finally{
                if(in != null)in.close();
            }
        }catch(Exception err){
            err.printStackTrace();
        }

//        debugPrint(ret);
        return ret;
    }


    public static void debugPrint(Map m){
        System.out.println(object2String(m));
    }

    /**
     * Object型 から もっとも適切だと思われるString に
     * 変換するユーティリティ
     *
     * @param b
     * @return
     */
    public static String object2String(Object o){
        String ret = "";
        if(o instanceof byte[]){
            ret = new String((byte[])o);
        }else if(o instanceof List){
            ret = "[";
            for(Object oo : (List)o){
                ret += object2String(oo) + " ";
            }
            ret += "]\n    ";
        }else if(o instanceof Map){
            ret = "{\n";
            for(Object oo : ((Map)o).keySet()){
                ret += "    " + new String((byte[])oo) + ":" + object2String(((Map)o).get(oo)) + "\n";
            }
            ret += "}";
        }
        return ret;
    }


    public static Map parseBencoding(byte[] bencoding){
        Map ret = null;
        for(int i = 0 ; i < bencoding.length ; i++){
            if(ret == null){
                ret = (Map)parseObject(bencoding, i);
            }else{
                System.err.println("[WORNING!]: root element not dictionary.");
            }
            i += object2Bencoding(ret).length;
        }
        return ret;
    }

    /**
     * Object型からBencoding変換を行う
     *
     * @param obj
     * @return
     */
    public static byte[] object2Bencoding(Object obj){
        List ret = new ArrayList();

        if(obj instanceof Number){
            for(byte b : ("i" + ((Number)obj).toString() + "e").getBytes()){
                ret.add(b);
            }
        }else if(obj instanceof byte[]){
            //String
            for(byte b : Integer.toString(((byte[])obj).length).getBytes()){
                ret.add(b);
            }
            ret.add((byte)':');
            for(byte b : (byte[])obj){
                ret.add(b);
            }
        }else if(obj instanceof List){
            ret.add((byte)'l');
            for(Object oo : (List)obj ){
                for(byte b : object2Bencoding(oo)){
                    ret.add(b);
                }
            }
            ret.add((byte)'e');
        }else if(obj instanceof Map){
            ret.add((byte)'d');
            for(Object oKey : ((Map)obj).keySet().toArray() ){
                for(byte b : object2Bencoding(oKey)){
                    ret.add(b);
                }
                for(byte b : object2Bencoding(((Map)obj).get(oKey))){
                    ret.add(b);
                }
            }
            ret.add((byte)'e');
        }

        byte[] retArray = new byte[ret.size()];
        for(int i = 0 ; i < ret.size() ; i++){
            retArray[i] = (byte)ret.get(i);
        }

        //System.out.println("debug >> " + new String(retArray));
        return retArray;
    }


    /**
     * オブジェクトの取得
     *
     * @param data データ
     * @param idx 開始位置
     * @return
     */
    private static Object parseObject(byte[] data, int idx){
        Object ret = null;

        //文字列
        for(int i = idx ; i < data.length ; i++){
            if(data[i] == 'd'){
                ret = parseMap(data, i+1);
                break;
            }else if(data[i] == 'l'){
                ret = parseList(data, i+1);
                break;
            }else if(data[i] == 'i'){
                ret = parseNumber(data, i+1);
                break;
            }else{
                ret = parseString(data, i);
                break;
            }
        }
        return ret;
    }

    /**
     * Map生成
     *
     * @param data
     * @param idx
     * @return
     */
    private static Map parseMap(byte[] data, int idx){
        Map ret = new LinkedHashMap();

        //文字列
        Object key = null;
        for(int i = idx ; i < data.length ; i++){
            if(data[i] == 'e'){
                break;
            }else{
                Object o = parseObject(data, i);
                i += object2Bencoding(o).length-1;

                if(key == null){
                    key = o;
                }else{
                    ret.put( key, o );
                    key = null;
                }
            }
        }

        return ret;
    }

    /**
     * List生成
     *
     * @param data
     * @param idx
     * @return
     */
    private static List parseList(byte[] data, int idx){
        List ret = new ArrayList();

        //文字列
        for(int i = idx ; i < data.length ; i++){
            if(data[i] == 'e'){
                break;
            }else{
                Object o = parseObject(data, i);
                i += object2Bencoding(o).length - 1;
                ret.add( o );
            }
        }

        return ret;
    }

    /**
     * 文字列の取得
     *
     * 文字列はEncodeが狂ってしまうため、String型を使用しない。
     * 一度Stringにすると正しいByte配列に戻すのは困難ですので
     * 全てbyte[]で管理しています
     *
     * @param data データ
     * @param idx 開始位置
     * @return
     */
    private static byte[] parseString(byte[] data, int idx) {
        //文字列
        int i = idx;
        while(true){
            if(data[i] == ':'){
                break;
            }else{
                i++;
            }
        }

        int cnt = Integer.parseInt( new String(data, idx, i - idx) );
        return Arrays.copyOfRange(data, i+1, (i+1) + cnt);
    }

    /**
     * 文字列の取得
     *
     * @param data データ
     * @param idx 開始位置
     * @return
     */
    private static Number parseNumber(byte[] data, int idx){
        //文字列
        int i = idx;
        while(true){
            if(data[i] == 'e'){
                break;
            }else{
                i++;
            }
        }

        return Long.parseLong(new String(data, idx, i - idx));
    }
}
