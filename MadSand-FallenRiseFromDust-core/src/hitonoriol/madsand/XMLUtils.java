package hitonoriol.madsand;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLUtils {

	public static Document XMLString(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			return builder.parse(is);
		} catch (Exception e) {
			Utils.out("Oopsie (" + e.getMessage() + ")");
			e.printStackTrace();
			return null;
		}
	}

	static int countKeys(Document doc, String list) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			int temp, c = 0;
			for (temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					c++;
				}
			}
			return c;
		} catch (Exception e) {
			return -1;
		}
	}

	static String getKey(Document doc, String list, String id, String element, String def) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id)) {
						return eElement.getElementsByTagName(element).item(0).getTextContent();
					}
				}
			}
			return def;
		} catch (Exception e) {
			return def;
		}
	}

	static String getKey(Document doc, String list, String id, String element) {
		return getKey(doc, list, id, element, "-1");
	}

	static String getKey(Document doc, String list, int id, String element) {
		return getKey(doc, list, Utils.str(id), element, "-1");
	}

	static String getAttr(Document doc, String list, String id, String attr) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id) || !eElement.hasAttribute("id")) {
						return eElement.getAttribute(attr);
					}
				}
			}
			return "-1";
		} catch (Exception e) {
			return "-1";
		}
	}

	static String nodeMapDump(NamedNodeMap map) {
		if (map == null)
			return "-1";
		String ret = "";
		int len = map.getLength();
		for (int i = 1; i < len; ++i) {
			Node attr = map.item(i);
			ret += attr.getNodeValue();
			if (i < len - 1)
				ret += ",";
		}
		if (ret.equals("")) {
			Utils.out("nodemapdump oopsie");
			return "-1";
		}
		// out("ret: " + ret);
		return ret;
	}

	static HashMap<String, String> nodeMapToHashMap(NamedNodeMap map) {
		HashMap<String, String> ret = new HashMap<String, String>();
		if (map == null) {
			ret.put("tid", "-1");
			return ret;
		}
		int len = map.getLength();
		for (int i = 0; i < len; ++i) {
			Node attr = map.item(i);
			ret.put(attr.getNodeName(), attr.getNodeValue());
		}
		return ret;
	}

	static NamedNodeMap getNested(Document doc, String list, String id, String name, String iid) {
		try {
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(list);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("id").equals(id)) {
						NodeList cList = eElement.getChildNodes();
						for (int pos = 0; pos < cList.getLength(); pos++) {
							Node cNode = cList.item(pos);
							if (cNode.getNodeType() == Node.ELEMENT_NODE) {
								Element cElement = (Element) cNode;
								if (cElement.getTagName().equals(name) && cElement.getAttribute("id").equals(iid)) {
									return (cElement.getAttributes());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
		return null;
	}

	static String getAttrValues(Document doc, String list, String id, String name, String iid) {
		return nodeMapDump(getNested(doc, list, id, name, iid));
	}

	static Vector<String> getGroup(int biome, String gname) {
		Vector<String> group = new Vector<String>();
		int j = 0;
		String tmp = "";
		while (!tmp.equals("-1")) {
			tmp = getAttrValues(Resources.gendoc, "biome", Utils.str(biome), gname, Utils.str(j));
			if (tmp.equals("-1"))
				break;
			group.add(tmp);
			++j;
		}
		return group;
	}

}
