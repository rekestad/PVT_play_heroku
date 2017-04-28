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

	@Inject
	public SthlmDataController(Database db) {
		this.db = db;
	}

	public Result importParks() {

		HttpURLConnection conn = startConnection(
				"http://api.stockholm.se/ServiceGuideService/ServiceUnitTypes/9da341e4-bdc6-4b51-9563-e65ddc2f7434/ServiceUnits?apikey=56010af30b114502bfbf8db404ef41a4");
		NodeList nList = getXMLNodeList(conn, "ServiceUnit");

		String[] values = new String[6];
		values[3] = "1"; // location_type = park

		for (int i = 0; i < nList.getLength(); i++) {

			Node nNode = nList.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element elementMain = (Element) nNode;
				Element elementGeoPos = (Element) elementMain.getElementsByTagName("GeographicalPosition").item(0);
				values[0] = elementMain.getAttribute("id");
				values[1] = elementMain.getAttribute("name");
				values[2] = getShortName(elementMain.getAttribute("name"));
				values[4] = elementGeoPos.getAttribute("x");
				values[5] = elementGeoPos.getAttribute("y");

			}

			if (!executeQuery(values))
				return badRequest("Import of parks failed.");
		}

		conn.disconnect();
		return ok("Imported parks successfully.");
	}

	public Result importPools() {
		// code for pools goes here
		return ok();
	}

	public Result importSchools() {
		// code for schools goes here
		return ok();
	}

	private boolean executeQuery(String[] values) {
		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setString(1, values[0]);
			pstmt.setString(2, values[1]);
			pstmt.setString(3, values[2]);
			pstmt.setString(4, values[3]);
			pstmt.setString(5, values[4]);
			pstmt.setString(6, values[5]);
		};

		SQLTools.ResultSetProcesser rp = rs -> {
		};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Locations VALUES (NULL,?,?,?,?,?,?)", sf, rp);
		} catch (SQLException e) {
			return false;
		}

		return true;
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
