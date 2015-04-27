package expriments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import structure.ComparableEntry;
import util.DataUtil;
import util.FileUtil;
import util.SortUtil;
import DAO.ProbaseDAO;
import DAO.ProbaseTranslator;

public class PrepareConceptPairs {

	static ProbaseDAO pbd = new ProbaseDAO(ProbaseDAO.FileMode);
	static ProbaseTranslator pt = new ProbaseTranslator("data/pbwordlist.txt");
	// static CalculateSimularity cs = new CalculateSimularity(
	// "library/vectors.bin");

	static int N = 50;
	static SortUtil<ComparableEntry<String, Double>, List<ComparableEntry<String, Double>>> sort = new SortUtil<ComparableEntry<String, Double>, List<ComparableEntry<String, Double>>>();

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

		// get attribute
		System.out.println("get attribute...");
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		for (String attr : attrsList) {
			start(attr);
		}
		// start("founder");
	}

	private static void start(String attr) {
		// get entity pairs
		System.out.printf("get entity pairs of %s...\n", attr);
		String rawSmallPath = String.format("data/attr-raw-small/%s.txt", attr);
		String transEntityPath = String.format("data/attr-trans/%s.txt", attr);
		String resultPath = "data/attr-prepare/" + attr + ".txt";

		if (!(FileUtil.hasFile(transEntityPath)
				&& FileUtil.hasFile(rawSmallPath) && !FileUtil
					.hasFile(resultPath))) {
			return;
		}
		List<Integer[]> entList = FileUtil
				.readFileByLineToTupleInInteger(transEntityPath);

		// get concept
		// System.out.println("get concept...");
		int e1, e2;
		HashMap<String, Double> conceptPairScoreMap = new HashMap<String, Double>();
		int num = entList.size();
		for (int i = 0; i < num; i++) {
			e1 = entList.get(i)[0];
			e2 = entList.get(i)[1];
			if (i % 10 == 0)
				System.out.printf(
						"Searching %d (of %s) concepts for (%d, %d) ...\n", i,
						num, e1, e2);
			if (e1 == -1 || e2 == -1) {
				continue;
			}
			List<Integer[]> triList1 = new ArrayList<Integer[]>();
			List<Integer[]> triList2 = new ArrayList<Integer[]>();
			pbd.getTripleByEntityInFileMode(triList1, e1);
			// System.out.printf("Entity %s has %d concepts...\n", e1,
			// triList1.size());
			pbd.getTripleByEntityInFileMode(triList2, e2);
			// System.out.printf("Entity %s has %d concepts...\n", e2,
			// triList2.size());
			if (triList1.size() == 0 || triList2.size() == 0) {
				continue;
			}
			calculateConceptPairScore(conceptPairScoreMap, triList1, triList2);
			// reduction
			List<ComparableEntry<String, Double>> tempList = new ArrayList<ComparableEntry<String, Double>>();
			for (Entry<String, Double> en : conceptPairScoreMap.entrySet()) {
				tempList.add(new ComparableEntry<String, Double>(en.getKey(),
						en.getValue()));
			}
			sort.heapSort(tempList);
			int size = (int) (N * Math.sqrt(N));
//			List<Entry<String, Double>> tempList = DataUtil.topNInMap2List(
//					conceptPairScoreMap, size);
			conceptPairScoreMap.clear();
			for (int j = 0; j < size; j++) {
				conceptPairScoreMap.put(tempList.get(j).getKey(),
						tempList.get(j).getValue());
			}

			System.out.println("concept pair size = "
					+ conceptPairScoreMap.size());
		}
		// final result
//		System.out.printf("Sorting %d results...\n", sortedList.size());
		 List<Entry<String, Double>> finalScore = DataUtil.topNInMap2List(
		 conceptPairScoreMap, 100);

		String[] split;
		String ent1, ent2;
		List<String> conceptPairList = new ArrayList<String>();
		int printSize = finalScore.size();
		if (printSize == 0) {
			System.out.println("Empty concept pair of " + attr);
		}
		for (int i = 0; i < printSize; i++) {
			split = finalScore.get(i).getKey().replaceAll("\\(|\\)", "")
					.split(",");
			ent1 = pt.lookup(Integer.parseInt(split[0]));
			ent2 = pt.lookup(Integer.parseInt(split[1]));
			conceptPairList.add(String.format("(%s, %s)=%f", ent1, ent2,
					finalScore.get(i).getValue()));
		}
		FileUtil.exportListToFile(conceptPairList, resultPath);
		System.out.println("Export " + resultPath);

	}

	private static void calculateConceptPairScore(
			HashMap<String, Double> conceptPairScoreMap,
			List<Integer[]> triList1, List<Integer[]> triList2) {
		int c1, c2;
		String pair;
		Double score;
		int numC1 = triList1.size();
		int numC2 = triList2.size();
		for (int i = 0; i < numC1; i++) {
			for (int j = 0; j < numC2; j++) {
				c1 = triList1.get(i)[0];
				c2 = triList2.get(j)[0];
				pair = String.format("(%d,%d)", c1, c2);
				double value = triList1.get(i)[2] * triList2.get(j)[0];
				score = conceptPairScoreMap.get(pair);
				if (score == null) {
					score = value;
				} else {
					score += value;
				}
				conceptPairScoreMap.put(pair, score);
			}
		}

	}

}
