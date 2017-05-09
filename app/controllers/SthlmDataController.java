package controllers;

import play.db.Database;
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

public class SthlmDataController extends Controller {
	private Database db;
	private static String urlParks = "http://api.stockholm.se/ServiceGuideService/ServiceUnitTypes/9da341e4-bdc6-4b51-9563-e65ddc2f7434/ServiceUnits?apikey=56010af30b114502bfbf8db404ef41a4";
	private static String urlPools = "http://api.stockholm.se/ServiceGuideService/ServiceUnitTypes/dce7cb97-b393-465b-a44c-0583f77a7cc6/ServiceUnits?apikey=56010af30b114502bfbf8db404ef41a4";
	private static String urlSchools = "http://api.stockholm.se/ServiceGuideService/ServiceUnitTypes/4edcc8c3-8eb8-424d-ae2a-447ba9f349bb/ServiceUnits?apikey=56010af30b114502bfbf8db404ef41a4";

	@Inject
	public SthlmDataController(Database db) {
		this.db = db;
	}
	
	public Result importAll() {
//		processSthlmData(urlParks, 1, "insert");
//		processSthlmData(urlPools, 2, "insert");
//		processSthlmData(urlSchools, 3, "insert");
//		return ok("Imported Sthlm data successfully");
		return ok("Import already made.");
	}
	
	public Result updateAll() {
		processSthlmData(urlParks, 1, "update");
		processSthlmData(urlPools, 2, "update");
		processSthlmData(urlSchools, 3, "update");
		return ok("Updated Sthlm data successfully");
	}

	private void processSthlmData(String url, int locationType, String sqlOperation) {
		HttpURLConnection conn = startConnection(url);
		NodeList nList = getXMLNodeList(conn, "ServiceUnit");

		for (int i = 0; i < nList.getLength(); i++) {

			Node nNode = nList.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element elementMain = (Element) nNode;
				Element elementGeoPos = (Element) elementMain.getElementsByTagName("GeographicalPosition").item(0);
				
				if (sqlOperation == "insert")
					executeInsert(elementMain, elementGeoPos, locationType);
				else
					executeUpdate(elementMain, elementGeoPos);
			}
		}
		conn.disconnect();
	}

	private void executeInsert(Element elementMain, Element elementGeoPos, int lType) {

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, elementMain.getAttribute("id"));
			pstmt.setString(2, elementMain.getAttribute("name"));
			pstmt.setString(3, getShortName(elementMain.getAttribute("name")));
			pstmt.setInt(4, lType);
			pstmt.setString(5, elementGeoPos.getAttribute("x"));
			pstmt.setString(6, elementGeoPos.getAttribute("y"));
		};

		SQLTools.ResultSetProcessor rp = rs -> {
		};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Locations VALUES (NULL,?,?,?,?,?,?)", sf, rp);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void executeUpdate(Element elementMain, Element elementGeoPos) {

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, elementMain.getAttribute("name"));
			pstmt.setString(2, getShortName(elementMain.getAttribute("name")));
			pstmt.setString(3, elementGeoPos.getAttribute("x"));
			pstmt.setString(4, elementGeoPos.getAttribute("y"));
			pstmt.setString(5, elementMain.getAttribute("id"));
		};

		SQLTools.ResultSetProcessor rp = rs -> {
		};

		try {
			SQLTools.doPreparedStatement(db,
					"UPDATE Locations SET name = ?, name_short = ?, position_x = ?, position_y = ? WHERE sthlm_id = ?",
					sf, rp);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	private String getShortName(String str) {
		if (str.contains(","))
			return str.substring(0, str.indexOf(","));
		else
			return str;
	}
}
