package expriments;

import java.util.ArrayList;
import java.util.List;

import DAO.DBpediaDAO;

import structure.Tuple;
import util.FileUtil;

public class DivideAttribute {
	public static double scoreThreshold = 0.0001;

	/**
	 * <b>Logic Flow:</b><br>
	 * <code>p((ci,cj)|a)=<br>sigma(p((ci,cj)|(ei,ej))*p((ei,ej)|a)</code><br>
	 * <ul>
	 * <li>Get an attribute <code>a</code> from DBpedia</li>
	 * <li>Get all the entity pairs <code>(ei,ej)</code> of <code>a</code></li>
	 * </ul>
	 * <code>p((ci,cj)|(ei,ej))=<br>sigma(p((ci,cj)|(ei,ej))*p((ei,ej)|a)</code><br>
	 */
	public static void main(String[] args) {

		// load dictionary
		System.out.println("load dictionary...");
		// ProbaseTranslator pt = new ProbaseTranslator("data/pbwordlist.txt");

		// get attribute
		System.out.println("get attribute...");
		DBpediaDAO dbd = null;
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		if (attrsList == null) {
			dbd = new DBpediaDAO("jdbc:mysql://localhost:3306/dbpedia");
			attrsList = new ArrayList<String>();
			dbd.getDistinctAttributes(attrsList);
			FileUtil.exportListToFile(attrsList, attrPath);
		}
		for (int i = 0; i < attrsList.size(); i++) {
			// get entity pairs
			String attr = attrsList.get(i);
			System.out.printf("get entity pairs of %s...\n", attr);
			String attrEntityPath = String.format("data/attr-raw/%s.txt", attr);
			if (!FileUtil.hasFile(attrEntityPath)) {
				dbd = new DBpediaDAO("jdbc:mysql://localhost:3306/dbpedia");
				ArrayList<Tuple<String, String>> tempEnList = new ArrayList<Tuple<String, String>>();
				dbd.getEntityPairsWithAttr(tempEnList, attr);
				// check value
				List<String> tempList = new ArrayList<String>();
				for (Tuple<String, String> tuple : tempEnList) {
					tempList.add(tuple.toString());
				}
				FileUtil.exportListToFile(tempList, attrEntityPath);
			} else {
				System.out.println("has attribute, skip to next...");
			}
		}

	}
}
