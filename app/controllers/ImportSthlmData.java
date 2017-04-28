package controllers;

import play.mvc.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import controllers.tools.SQLTools;
import play.db.Database;

public class ImportSthlmData extends Controller {
	private Database db;

	@Inject
	public ImportSthlmData(Database db) {
		this.db = db;
	}

	public Result importParks() {

		HttpURLConnection conn = startConnection(
				"http://api.stockholm.se/ServiceGuideService/ServiceUnitTypes/9da341e4-bdc6-4b51-9563-e65ddc2f7434/ServiceUnits?apikey=56010af30b114502bfbf8db404ef41a4");
		NodeList nList = getXMLNodeList(conn, "ServiceUnit");

		String tempOutput = ""; // just for testing

		for (int i = 0; i < nList.getLength(); i++) {

			String values = "NULL, ";
			
			Node nNode = nList.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element parent = (Element) nNode;
				Element childGeo = (Element) parent.getElementsByTagName("GeographicalPosition").item(0);
				
				SQLTools.StatementFiller sf = pstmt -> {
					//pstmt.setString(1, null);
					pstmt.setString(1, parent.getAttribute("id"));
					pstmt.setString(2, parent.getAttribute("name"));
					pstmt.setInt(3, 1);
					pstmt.setString(4, childGeo.getAttribute("x"));
					pstmt.setString(5, childGeo.getAttribute("y"));
				};
				
				SQLTools.ResultSetProcesser rp = rs -> {
				};

				//EXECUTE SQL QUERY HERE
				try {
					SQLTools.doPreparedStatement(db,"INSERT INTO Locations VALUES (NULL,?,?,?,?,?)", sf, rp);
				} catch (SQLException e) {
					return badRequest();
				}
				
			}
			
			//tempOutput += "INSERT INTO Locations VALUES (NULL,?,?,?,?,?)",\n"; // just for testing
		}

		conn.disconnect();

		return ok(tempOutput);
	}

	public Result importPools() {
		// code for pools goes here
		return ok();
	}

	public Result importSchools() {
		// code for schools goes here
		return ok();
	}

	private HttpURLConnection startConnection(String theURL) {
		try {

			URL url = new URL(theURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/xml");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			return conn;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private NodeList getXMLNodeList(HttpURLConnection conn, String tagName) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;

		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}

		doc.getDocumentElement().normalize();

		return doc.getElementsByTagName(tagName);

	}
}
