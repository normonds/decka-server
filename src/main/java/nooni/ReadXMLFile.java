package nooni;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import nooni.Constants.DataTarget;


import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
 
public class ReadXMLFile {
public Document xmlDoc;

public ReadXMLFile (String file) {
	try {
		File fXmlFile;
		fXmlFile = new File(file);
		if (!fXmlFile.exists()) {
			fXmlFile = new File("/opt/jetty/"+file);
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		xmlDoc = dBuilder.parse(fXmlFile);
		xmlDoc.getDocumentElement().normalize();
		//System.out.println("Root element :" + doc.toString());
		//System.out.println("dropship.getLength():" + nodes.getLength());
	} catch (Exception e) {
		e.printStackTrace();
	}
}
public Map<String, String> getSpell (String charClass, String spellName) {
	Map<String, String> subData = new HashMap<String, String>();
	NodeList nodes = xmlDoc.getElementsByTagName(charClass).item(0).getChildNodes();
	for (int temp = 0; temp < nodes.getLength(); temp++) {
		Node nNode = nodes.item(temp);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element) nNode;
			if (elem.getAttribute("name").equals(spellName)) {
				subData = Util.xmlAttributesToMap(elem.getAttributes());
				break;
			}
		}
	}
	return subData;
}
public Map<String, Map<String, Object>> getCardsData (String charClass, DataTarget dataTarget) {
	Map<String, Map<String, Object>> retData = new LinkedHashMap<String, Map<String, Object>>();
	int i=0;
	for(Node nNode=xmlDoc.getElementsByTagName(charClass).item(0).getFirstChild(); nNode!=null; nNode=nNode.getNextSibling()){
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element) nNode;
			Map<String, Object> subData = new HashMap<String, Object>();
				String[] stats = elem.getAttribute("stats").split("/");
				if (elem.getAttribute("type").indexOf("spell")>-1) { // spell
					if (stats.length>0) {
						subData.put("cost", stats[0]);
					} 
					if (stats.length>1) {
						subData.put("attack", stats[1]);
					}
					if (stats.length>2) {
						subData.put("armor", stats[2]);
					}
				} else { // minion
					subData.put("cost", stats[0]);
					subData.put("attack", stats[1]);
					subData.put("armor", stats[2]);
				}
				subData.put("type", elem.getAttribute("type"));
				subData.put("targMods", (elem.hasAttribute("targMods"))?elem.getAttribute("targMods"):"");
				subData.put("subtype", (elem.hasAttribute("subtype"))?elem.getAttribute("subtype"):"");
				subData.put("mods", elem.getAttribute("mods"));
				subData.put("descr", elem.getAttribute("descr"));
				subData.put("title", elem.getAttribute("title"));
				subData.put("portrait", elem.getAttribute("portrait"));
				retData.put(elem.getAttribute("title"), subData);
				i++;
		}
	}
	return retData;
}
public String printNodeXML (Node node) {
	try {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		String output = writer.getBuffer().toString();
		return output;
	} catch (Exception e) {
    	e.printStackTrace();
	}
	return "ReadXMLFile.printNodeXML() error";
}
public ArrayList<Map<String,String>> spellSubNodes (ArrayList<Map<String,String>> parentNodes, Element elem, String spellName, String str) {
	if (Util.hasChildNodes(elem)) {
		//System.out.println("");
		//System.out.println(str+"init sub search:"+(elem.hasAttribute("name")?elem.getAttribute("name"):elem.getNodeName()));
		ArrayList<Node> childNodes = Util.getChildNodes(elem);
		for (int j=0; j<childNodes.size(); j++) {

				if (( (Element) childNodes.get(j)).getAttribute("name").equals(spellName)) {
					//System.out.println(str+( (Element) childNodes.get(j)).getAttribute("name") + " found "+spellName);
					//System.out.println(str+Utils.getChildNodes((Element) childNodes.get(j)).size());
					return Util.getChildNodesFlat((Element) childNodes.get(j));
				} else if (Util.hasChildNodes((Element) childNodes.get(j))) {
					//System.out.println(str+( (Element) childNodes.get(j)).getAttribute("name") + " has child nodes");
					if (spellSubNodes(parentNodes, (Element) childNodes.get(j), spellName, str+"-").size()>0) {
						//System.out.println(str+( (Element) childNodes.get(j)).getAttribute("name") + " has "+spellName);
						return spellSubNodes(parentNodes, (Element) childNodes.get(j), spellName, str+"  ");
					} else {
						//System.out.println(str+( (Element) childNodes.get(j)).getAttribute("name") + " doesnt contain "+spellName);
					}
				} else {
					//System.out.println(str+( (Element) childNodes.get(j)).getAttribute("name")+" doesnt contain child nodes and is not needed node");
				}

		}
		//Utils.childNodes(elem.getChild);
	}
	//System.out.println("");
	//System.out.println(str+"no node "+spellName+" in "+(elem.hasAttribute("name")?elem.getAttribute("name"):elem.getNodeName()));
	return parentNodes;
}
public Map<String,String> spellNode (Map<String,String> parentNode, Element elem, String spellName, String str) {
	if (elem.getAttribute("name").equals(spellName)) {
		return Util.xmlAttributesToMap(elem.getAttributes());
	} else if (Util.hasChildNodes(elem)) {
		ArrayList<Node> childNodes = Util.getChildNodes(elem);
		for (int j=0; j<childNodes.size(); j++) {
			if (spellNode(parentNode, ((Element)childNodes.get(j)), spellName, str).size()>0) {
				return spellNode(parentNode, ((Element)childNodes.get(j)), spellName, str);
			}
		}
	}
	return parentNode;
}
public ArrayList<Map<String,String>> searchFingerSpells (String charClass, String spellName) {
	ArrayList<Map<String,String>> targNodes = new ArrayList<Map<String,String>>();
	return spellSubNodes(targNodes, (Element) xmlDoc.getElementsByTagName(charClass).item(0), spellName, "");
}
public Map<String, String> searchFingerSpell (String charClass, String name) {
	Map<String,String> targNodes = new HashMap<String,String>();
	return spellNode(targNodes, (Element) xmlDoc.getElementsByTagName(charClass).item(0), name, "");
}
public Map<String, String> mapProps (String name) {
	Map<String, String> retData = new HashMap<String, String>();
	for (Node nNode=xmlDoc.getElementsByTagName("maps").item(0).getFirstChild(); nNode!=null; nNode=nNode.getNextSibling()) {
		//System.out.println(nNode.getAttributes().getNamedItem("name").toString().equals(name));
		if (nNode.getNodeType() == Node.ELEMENT_NODE && nNode.getAttributes().getNamedItem("name").getNodeValue().equals(name)) {
			//System.out.println("--"+);
			Element elem = (Element) nNode;
			
			retData = Util.xmlAttributesToMap(elem.getAttributes());
		}
	}
	return retData;
}
 
}