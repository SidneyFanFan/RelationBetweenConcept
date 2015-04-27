package expriments;

import java.util.ArrayList;
import java.util.List;

import structure.Tuple;
import util.FileUtil;
import DAO.DBpediaDAO;
import DAO.ProbaseTranslator;

public class TranslateAttribue {

	static ProbaseTranslator pt = new ProbaseTranslator("data/pbwordlist.txt");

	public static void main(String[] args) {
		// load dictionary
		System.out.println("load dictionary...");

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
		for (String attr : attrsList) {
			start(attr);
		}
		start("launchPad");// special case in translate

	}

	private static void start(String attr) {
		// System.out.printf("get entity pairs of %s...\n", attr);
		String attrEntityPath = String.format("data/attr-raw-small/%s.txt",
				attr);
		String transPath = String.format("data/attr-trans/%s.txt", attr);
		if (!(FileUtil.hasFile(attrEntityPath) && !FileUtil.hasFile(transPath))) {
			return;
		}
		List<Tuple<String, String>> entList;
		System.out.println("Processing " + attr + " ...");
		int succCount = 0;
		int from = 0;
		int gap = 1000;
		List<String> transList = new ArrayList<String>();
		while (succCount < gap) {
			entList = FileUtil.readFileByLineToTupleInStringLimit(
					attrEntityPath, from, from + gap);
			if (entList.size() == 0) {
				break;
			}
			succCount += translate(entList, transList);
			System.out.println("translate " + succCount);
			from += gap;
		}
		FileUtil.exportListToFile(transList, transPath);
	}

	private static int translate(List<Tuple<String, String>> entList,
			List<String> transList) {
		int count = 0;
		int validCount = 0;
		if (entList != null) {
			String trans;
			int trans1, trans2;
			for (Tuple<String, String> tuple : entList) {
				count++;
				if (count % 100 == 0) {
					System.out
							.printf("%s...%d of %d, valid: %d\n",
									tuple.toString(), count, entList.size(),
									validCount);
				}
				trans1 = pt.lookup(tuple.getArg1().toLowerCase());
				trans2 = pt.lookup(tuple.getArg2().toLowerCase());
				if (trans1 == -1 || trans2 == -1) {
					continue;
				}
				validCount++;
				trans = String.format("(%d, %d)", trans1, trans2);
				// translate
				transList.add(trans);
			}
		}
		return validCount;
	}
}
