package expriments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javatools.parsers.NounGroup;

import structure.ComparableEntry;
import util.DataUtil;
import util.FileUtil;
import util.SortUtil;
import DAO.ProbaseDAO;
import DAO.ProbaseTranslator;

public class PriorProbability {

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
//		 start("founder");
	}

	private static void start(String attr) {
		// get entity pairs
		System.out.printf("get entity pairs of %s...\n", attr);
		String rawSmallPath = String.format("data/attr-raw-small/%s.txt", attr);
		String transEntityPath = String.format("data/attr-trans/%s.txt", attr);
		String resultPath = "data/attr-prepare/" + attr + ".txt";
//		String resultPath2 = "data/attr-concept-batch/" + attr + ".txt";

		if (!(FileUtil.hasFile(transEntityPath)
				&& FileUtil.hasFile(rawSmallPath) && !FileUtil
					.hasFile(resultPath))) {
			return;
		}
		List<Integer[]> entList = FileUtil.readFileByLineToTupleInInteger(transEntityPath);

		// get concept
		// System.out.println("get concept...");
		int e1, e2;
		HashMap<String, Double> conceptPairScoreMap = new HashMap<String, Double>();
//		HashMap<String, Double> conceptPairScoreBatchMap = new HashMap<String, Double>();
//		HashMap<Integer, Double> c1pi = new HashMap<Integer, Double>();
//		HashMap<Integer, Double> c2pi = new HashMap<Integer, Double>();
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
			// for e1
			List<Entry<Integer, Double>> scoreList1 = calculateProportion(triList1);
			// for e2
			List<Entry<Integer, Double>> scoreList2 = calculateProportion(triList2);
			// calculate score
			if (scoreList1 == null || scoreList2 == null) {
				continue;
			}
			calculateConceptPairScore(conceptPairScoreMap, scoreList1,
					scoreList2);
			// accumulateE2cBatchScore(c1pi, scoreList1);
			// accumulateE2cBatchScore(c2pi, scoreList2);
			// reduction
			if (i % 2 == 0 && conceptPairScoreMap.size() > N * N) {
				List<ComparableEntry<String, Double>> tempList = new ArrayList<ComparableEntry<String, Double>>();
				for (Entry<String, Double> en : conceptPairScoreMap.entrySet()) {
					tempList.add(new ComparableEntry<String, Double>(en
							.getKey(), en.getValue()));
				}
				sort.heapSort(tempList);
				int size = (int) (N * Math.sqrt(N));
//				List<Entry<String, Double>> tempList = DataUtil.topNInMap2List(conceptPairScoreMap, size);
				
				conceptPairScoreMap.clear();
				for (int j = 0; j < size; j++) {
					conceptPairScoreMap.put(tempList.get(j).getKey(), tempList
							.get(j).getValue());
				}
			}
			if (i % 2 == 0) {
				System.out.println("concept pair size = "
						+ conceptPairScoreMap.size());
			}
		}
		// final result
		System.out
				.printf("Sorting %d results...\n", conceptPairScoreMap.size());
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
		// export batch score
		// calculateConceptPairScoreBatch(conceptPairScoreBatchMap, c1pi, c2pi);
		// List<Entry<String, Double>> blist =
		// DataUtil.topNInMap2List(conceptPairScoreBatchMap, 10);
		// List<String> bliststr = new ArrayList<String>();
		// for (Entry<String, Double> en : blist) {
		// bliststr.add(en.toString());
		// }
		// FileUtil.exportListToFile(bliststr, resultPath2);

	}

	static void calculateConceptPairScoreBatch(
			HashMap<String, Double> conceptPairScoreBatchMap,
			HashMap<Integer, Double> c1pi, HashMap<Integer, Double> c2pi) {
		List<Entry<Integer, Double>> topc1 = DataUtil.topSmallNInMap2List2(
				c1pi, 10);
		List<Entry<Integer, Double>> topc2 = DataUtil.topSmallNInMap2List2(
				c2pi, 10);
		String pair;
		String c1, c2;
		double sim, score;
		for (Entry<Integer, Double> en1 : topc1) {
			for (Entry<Integer, Double> en2 : topc2) {
				c1 = pt.lookup(en1.getKey());
				c2 = pt.lookup(en2.getKey());
				pair = String.format("(%s, %s)", c1, c2);
				sim = conceptSim(c1, c2);
				score = (1.0 - en1.getValue()) * (1.0 - en2.getValue()) * sim;
				conceptPairScoreBatchMap.put(pair, score);
			}
		}

	}

	 static void accumulateE2cBatchScore(HashMap<Integer, Double> c1pi,
			List<Entry<Integer, Double>> scoreList1) {
		int c1;
		for (int i = 0; i < scoreList1.size(); i++) {
			c1 = scoreList1.get(i).getKey();
			Double c1mul = c1pi.get(c1);
			if (c1mul == null) {
				c1mul = 1.0 - scoreList1.get(i).getValue();
			} else {
				c1mul = c1mul.doubleValue()
						* (1.0 - scoreList1.get(i).getValue().doubleValue());
			}
			c1pi.put(c1, c1mul);
		}
	}

	private static void calculateConceptPairScore(
			HashMap<String, Double> conceptPairScoreMap,
			List<Entry<Integer, Double>> scoreList1,
			List<Entry<Integer, Double>> scoreList2) {
		int c1, c2;
		String pair;
		double simularity;
		Double score;
		int numC1 = scoreList1.size();
		int numC2 = scoreList2.size();
		for (int i = 0; i < numC1; i++) {
			for (int j = 0; j < numC2; j++) {
				c1 = scoreList1.get(i).getKey();
				c2 = scoreList2.get(j).getKey();
				pair = String.format("(%d,%d)", c1, c2);
				simularity = conceptSim(c1, c2);
				double pecec = scoreList1.get(i).getValue()
						* scoreList2.get(j).getValue() * simularity;
				score = conceptPairScoreMap.get(pair);
				if (score == null) {
					score = pecec;
				} else {
					score += pecec;
				}
				conceptPairScoreMap.put(pair, score);
			}
		}

	}

	private static double conceptSim(int c1, int c2) {
		String cs1 = pt.lookup(c1);
		String cs2 = pt.lookup(c2);
		cs1 = new NounGroup(cs1).head();
		cs2 = new NounGroup(cs2).head();
		double sim = 1;
		// try {
		// if (cs.hasWord(cs1) && cs.hasWord(cs2)) {
		// sim = cs.similarityW2V(cs1, cs2);
		// }
		// } catch (UnknownWordException e) {
		// sim = -1;
		// }

		return sim;
	}

	private static double conceptSim(String cs1, String cs2) {
		cs1 = new NounGroup(cs1).head();
		cs2 = new NounGroup(cs2).head();
		double sim = 1;
		// try {
		// if (cs.hasWord(cs1) && cs.hasWord(cs2)) {
		// sim = cs.similarityW2V(cs1, cs2);
		// }
		// } catch (UnknownWordException e) {
		// sim = -1;
		// }

		return sim;
	}

	private static List<Entry<Integer, Double>> calculateProportion(
			List<Integer[]> triList) {
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		int total = 0;
		for (Integer[] triple : triList) {
			total += triple[2];
		}
		double proportion;
		if (total == 0) {
			return null;
		}
		for (Integer[] triple : triList) {
			proportion = (double) triple[2] / (double) total;
			map.put(triple[0], proportion);
		}
		return DataUtil.topNInMap2List2(map, N);
	}
}
