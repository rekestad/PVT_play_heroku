package controllers;

import javax.inject.Inject;

import play.db.Database;
import play.mvc.Controller;

public class DatabaseController extends Controller {
	private Database db;

	@Inject
	public DatabaseController(Database db) {
		this.db = db;
	}
	
	public Database getDb() {
		return db;
	}
}
