package expriments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import util.DataUtil;
import util.FileUtil;
import DAO.ProbaseDAO;
import DAO.ProbaseTranslator;

public class CombineConcept {

	static ProbaseDAO pbd = new ProbaseDAO(ProbaseDAO.FileMode);
	static ProbaseTranslator pt = new ProbaseTranslator("data/pbwordlist.txt");

	public static void main(String[] args) {

		// get attribute
		System.out.println("get attribute...");
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		for (String attr : attrsList) {
			start(attr);
		}
//		start("founder");
	}

	private static void start(String attr) {
		System.out.println("share "+attr);
		String conPath = String.format("data/attr-concept/%s.txt", attr);
		//String transPath = String.format("data/attr-trans/%s.txt", attr);
		String conFinalPath = String.format("data/attr-share/%s-share.txt", attr);
		String conShareCombinePath = String.format("data/attr-share/%s-share-concept.txt", attr);
		if (!(FileUtil.hasFile(conPath) && !FileUtil.hasFile(conFinalPath))) {
			return;
		}
		HashMap<String, Double> conMap = FileUtil.readFileToMap(conPath);
		// divide to two list
		Set<Integer> conset1 = new HashSet<Integer>();
		Set<Integer> conset2 = new HashSet<Integer>();
		String[] split;
		for (Entry<String, Double> en : conMap.entrySet()) {
			split=en.getKey().substring(1, en.getKey().length()-1).split(", ");
			conset1.add(pt.lookup(split[0]));
			conset2.add(pt.lookup(split[1]));
		}
		// check share entities for each concept
		HashMap<String, Double> con1SimMap = new HashMap<String, Double>();
		HashMap<String, Double> con2SimMap = new HashMap<String, Double>();
		calculateSimMap(conset1, con1SimMap);
		calculateSimMap(conset2, con2SimMap);
		// Combine back
		HashMap<String, Double> shareMap = FileUtil.readFileToMap(conPath);
		HashMap<String, Double> shareconMap = FileUtil.readFileToMap(conPath);
		for (Entry<String, Double> en : conMap.entrySet()) {
			split=en.getKey().substring(1, en.getKey().length()-1).split(", ");
			double con1score = con1SimMap.get(split[0]);
			double con2score = con2SimMap.get(split[1]);
			double cscore = (con1score*con2score);
			shareMap.put(en.getKey(), cscore);
			shareconMap.put(en.getKey(), cscore*en.getValue().doubleValue());
		}
		
		List<Entry<String, Double>> exportList = new ArrayList<Entry<String, Double>>();
		exportList.addAll(DataUtil.convertMapToSortedListInDouble(shareMap));
		FileUtil.exportListEntryToFile(exportList, conFinalPath);
		exportList.clear();
		exportList.addAll(DataUtil.convertMapToSortedListInDouble(shareconMap));
		FileUtil.exportListEntryToFile(exportList, conShareCombinePath);
	}

	private static void calculateSimMap(Set<Integer> conset, HashMap<String, Double> conSimMap) {
		double sim;
		HashMap<Integer, HashSet<Integer>> conANDent = new HashMap<Integer, HashSet<Integer>>();
		HashSet<Integer> set;
		for (Integer coni : conset) {
			set = new HashSet<Integer>();
			pbd.getEntitySetConceptInFileMode(set, coni);
			conANDent.put(coni, set);
		}
		
		for (Integer coni : conset) {
			sim = 0;
			for (Integer conj : conset) {
				if(coni.intValue()!=conj.intValue()){
					sim += calculateConceptShare(coni,conj, conANDent);
				}
			}
			conSimMap.put(pt.lookup(coni), sim);
		}
	}

	private static double calculateConceptShare(int con1, int con2, HashMap<Integer, HashSet<Integer>> map) {
		HashSet<Integer> set1 = map.get(con1);
		HashSet<Integer> set2 = map.get(con2);
		List<HashSet<Integer>> setlist = new ArrayList<HashSet<Integer>>();
		setlist.add(set1);
		setlist.add(set2);
		HashSet<Integer> jointSet = DataUtil.intersectSets(setlist);
		double score = (double)jointSet.size()/(double)set2.size();
		// score = 1 means set1 belongs to set 2
		// score < 1 means the similarity
		return score;
	}
}
