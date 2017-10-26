package nooni;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util {
public static Object[] o (Object var1) {
	return new Object[]{var1};}
public static Object[] o (Object var1, Object var2) {
	return new Object[]{var1, var2};}
public static Object[] o (Object var1, Object var2, Object var3) {
	return new Object[]{var1, var2, var3};}
public static Object[] o (Object var1, Object var2, Object var3, Object var4) {
	return new Object[]{var1, var2, var3, var4};}
public static Object[] o (Object var1, Object var2, Object var3, Object var4, Object var5) {
	return new Object[]{var1, var2, var3, var4, var5};}
public static Object[] o (Object var1, Object var2, Object var3, Object var4, Object var5, Object var6) {
	return new Object[]{var1, var2, var3, var4, var5, var6};}
public static List<Object> list (Object var1) {
	List<Object> ret = new ArrayList<Object>();	ret.add(var1);
	return ret;}
public static List<Object> list (Object var1, Object var2) {
	List<Object> ret = new ArrayList<Object>();	ret.add(var1);ret.add(var2);
	return ret;}
public static List<Object> list (Object var1, Object var2, Object var3) {
	List<Object> ret = new ArrayList<Object>();	ret.add(var1);ret.add(var2);ret.add(var3);
	return ret;}

public static Map<String,Object> toList (List<Object> arr) {
	HashMap<String, Object> ret = new HashMap<String, Object>();
	for (Object entry : arr) {
		ret.put(STR(arr.indexOf(entry)), entry);
	}
	return ret;
}
public static int indexOf (String[] arr, String str) {
	for (int i=0;i<arr.length;i++) {
		if (arr[i].equals(str)) {
			return i;
		}
	}
	return -1;
}
public static String STR (int num) {			return Integer.toString(num);}
public static double DBL (String str) {		return Double.parseDouble(str);}
public static int INT (Object obj) {			return ((Double)Double.parseDouble(String.valueOf(obj))).intValue();}
public static String md5 (String message){
    String digest = null;
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(message.getBytes("UTF-8"));
       
        //converting byte array to Hexadecimal String
       StringBuilder sb = new StringBuilder(2*hash.length);
       for(byte b : hash){
           sb.append(String.format("%02x", b&0xff));
       }
       digest = sb.toString();
    } catch (UnsupportedEncodingException ex) {
        //Logger.getLogger(StringReplace.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchAlgorithmException ex) {
       // Logger.getLogger(StringReplace.class.getName()).log(Level.SEVERE, null, ex);
    }
    return digest;
}
public static float getAngle (Point source, Point target, boolean retRadians) {
    float angle = (float) Math.toDegrees(Math.atan2(target.x - source.x, target.y - source.y));
    angle -= 90;
    if (angle < 0) angle += 360;
    return (float) (retRadians ? Math.toRadians(angle) : angle);
}
public static double rand (long min, long max) {
	return Math.floor(Math.random() * (max - min + 1)) + min;
}
public static String humanDate (long milliseconds) { //unix timestamp -> human date
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
	Date resultdate = new Date(milliseconds);
	return sdf.format(resultdate);
}
public static Point interpolate (Point start, Point end, double t) {
	double x = start.x+(end.x-start.x)*t;
	double y = start.y+(end.y-start.y)*t;
	Point ret = new Point();
	ret.setLocation(x, y);
	return ret;
}
public static double strToSecs (String str) {
	double incNumb = Double.parseDouble(str.substring(0,str.length()-1));
	double divider=1;
	String incUnit = str.substring(str.length()-1);
	if (incUnit.equals("s")) divider=1;
	else if (incUnit.equals("m")) divider = 60;
	else if (incUnit.equals("h")) divider = 60*60;
	else if (incUnit.equals("d")) divider = 60*60*24;
	else if (incUnit.equals("w")) divider = 60*60*24*7;
	return divider*incNumb;
}
public static String secsToStr (int secs) {
	String unite;
	if (secs<=60) unite = "s";
	else if (secs<=60*60) { unite = "m"; secs=Math.round(secs/60);}
	else if (secs<=60*60*24) { unite = "h"; secs=Math.round(secs/(60*60));}
	else if (secs<=60*60*24*7) { unite = "d"; secs=Math.round(secs/(60*60*24));}
	else { unite = "w"; secs=Math.round(secs/(60*60*24*7));}
	return secs+unite;
}
public static Map<String, String> xmlAttributesToMap (NamedNodeMap incMap) {
	Map<String, String> map = new HashMap<String, String>();
    for (int j = 0; j < incMap.getLength(); j++) {
    	map.put(incMap.item(j).getNodeName(), incMap.item(j).getNodeValue().toString());
    }
    return map;
}
public static boolean hasProperty (Object obj, String property) {
	return Arrays.asList(obj.getClass().getFields()).contains(property);
}
public static Object getProperty (Object obj, String property) {
	try {
		return obj.getClass().getField(property).get(obj);
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchFieldException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
}
public static boolean hasChildNodes (Element elem) {
	NodeList nodes = elem.getChildNodes();
	for (int temp = 0; temp < nodes.getLength(); temp++) {
		Node nNode = nodes.item(temp);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			return true;
		}
	}
	return false;
}
public static ArrayList<Node> getChildNodes (Element elem) {
	ArrayList<Node> retNodes = new ArrayList<Node>();
	NodeList nodes = elem.getChildNodes();
	for (int temp = 0; temp < nodes.getLength(); temp++) {
		Node nNode = nodes.item(temp);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			retNodes.add(nNode);
		}
	}
	return retNodes;
}
public static ArrayList<Map<String,String>> getChildNodesFlat(Element elem) {
	ArrayList<Map<String, String>> retNodes = new ArrayList<Map<String,String>>();
	NodeList nodes = elem.getChildNodes();
	for (int temp = 0; temp < nodes.getLength(); temp++) {
		//Map<String,String> subMap = new HashMap<String,String>();
		Node nNode = nodes.item(temp);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			retNodes.add(Util.xmlAttributesToMap(((Element) nNode).getAttributes()));
		}
	}
	return retNodes;
}
public static String milisToTime (long milis) {
	return nanosToTime(milis*1000);
}
public static String nanosToTime (long milis) {
	long secs = TimeUnit.SECONDS.convert(milis, TimeUnit.NANOSECONDS);
	int days = (int)TimeUnit.SECONDS.toDays(secs);        
	long hours = TimeUnit.SECONDS.toHours(secs) - (days *24);
	long minutes = TimeUnit.SECONDS.toMinutes(secs) - (TimeUnit.SECONDS.toHours(secs)* 60);
	long seconds = TimeUnit.SECONDS.toSeconds(secs) - (TimeUnit.SECONDS.toMinutes(secs) *60);
	String ret = "";
	if (days>0) ret += ""+days+"d";
	if (hours>0) ret += ""+hours+"h";
	if (minutes>0) ret += ""+minutes+"m";
	ret += seconds+"s";
	return ret;
}
public static Object map (String str, Object stats) {
	Map<String, Object> ret = new HashMap<String,Object>();
	ret.put(str, stats);
	return ret;
}
public static String plusOrMinus (int inc) {
	if (inc>0) return "+"+STR(inc);
	else if (inc<0) return STR(inc);
	else return "+0";
}
/*public static String callURL (String myURL) {
	System.out.println("Requested URL:" + myURL);
	StringBuilder sb = new StringBuilder();
	URLConnection urlConn = null;
	InputStreamReader in = null;
	try {
		URL url = new URL(myURL);
		urlConn = url.openConnection();
		if (urlConn != null)
			urlConn.setReadTimeout(10 * 1000);
		if (urlConn != null && urlConn.getInputStream() != null) {
			in = new InputStreamReader(urlConn.getInputStream(),
					Charset.defaultCharset());
			BufferedReader bufferedReader = new BufferedReader(in);
			if (bufferedReader != null) {
				int cp;
				while ((cp = bufferedReader.read()) != -1) {
					sb.append((char) cp);
				}
				bufferedReader.close();
			}
		}
	in.close();
	} catch (Exception e) {
		//throw new RuntimeException("Exception while calling get URL:"+ myURL, e);
		sb.append("Exception while calling get URL:"+ myURL);
	} 

	return sb.toString();
}*/

//HTTP GET request
public static String urlGET (String url) {
	StringBuffer response = new StringBuffer();
	long milis = System.currentTimeMillis();
	try {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		System.out.println("\nSending 'GET' request to URL : " + url);
		con.setReadTimeout(10*1000);
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println("Response Code : " + responseCode+", in " + (System.currentTimeMillis()-milis)+"ms");
	} catch (Exception e) {
		//throw new RuntimeException("Exception while calling URL:"+ url, e);
		response.append("Exception while calling get URL:"+ url);
	}
	return response.toString();
	//System.out.println(response.toString());
}

//HTTP POST request
public static String urlPOST (String url, String params) {
	StringBuffer response = new StringBuffer();
	long milis = System.currentTimeMillis();
	try {
		//String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setReadTimeout(10*1000);
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();
	
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + params);
		//System.out.println("Response Code : " + responseCode);
	
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println("Response Code : " + responseCode+", in " + (System.currentTimeMillis()-milis)+"ms");
		//System.out.println("Got POST request in: " + (System.currentTimeMillis()-milis)+"ms");
	} catch (Exception e) {
		//throw new RuntimeException("Exception while calling post URL:"+ url, e);
		response.append("Exception while calling get URL:"+ url);
	} 
	//print result
	return response.toString();

}

/** Read the object from Base64 string. */
public static Object deserialize (String s) {
    byte [] data = Base64Coder.decode( s );
    ObjectInputStream ois;
    Object o = new Object();
	try {
		ois = new ObjectInputStream( 
		new ByteArrayInputStream(  data ) );
		try {
			o = ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	ois.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
    return o;
}

/** Write the object to a Base64 string. */
public static String serialize (Serializable o) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos;
	try {
		oos = new ObjectOutputStream( baos );
	    oos.writeObject( o );
	    oos.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
    return new String( Base64Coder.encode( baos.toByteArray() ) );
}

public static String stringArrKey (String[] stri, int inte) {
	return (stri.length>=inte+1) ? stri[inte] : "";
}
public static String join (String separator) {return "";}

}
