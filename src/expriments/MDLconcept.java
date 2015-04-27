package expriments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import algorithms.FitnessAlgorithm;
import algorithms.GeneticAlgorithm;

import structure.Tuple;
import util.DataUtil;
import util.FileUtil;
import DAO.ProbaseDAO;
import DAO.ProbaseTranslator;

public class MDLconcept {

	public static void main(String[] args) {
		MDLconcept mdl = new MDLconcept();
		// get attribute
		System.out.println("get attribute...");
		// String attrPath = "data/attrs.txt";
		// List<String> attrsList;
		// attrsList = FileUtil.readFileByLine(attrPath);
		// for (String attr : attrsList) {
		// start(attr);
		// }
		mdl.start("president");
	}

	ProbaseDAO pbd = new ProbaseDAO(ProbaseDAO.FileMode);
	ProbaseTranslator pt = new ProbaseTranslator("data/pbwordlist.txt");
	List<int[]> intersectSizeList = FileUtil
			.readFileByLineToArrayInInteger("data/pbintersect.txt");
	double α = 0.7;
	int N = 10;
	int IN = 1000;

	HashMap<String, Double> conMap;
	List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> conList;
	List<Tuple<Integer, String>> conList1;
	Map<String, Double> sharenessMap1;
	Map<String, Double> wordNetMap1;
	List<Tuple<Integer, String>> conList2;
	Map<String, Double> sharenessMap2;
	Map<String, Double> wordNetMap2;

	public MDLconcept() {
	}

	private void start(String attr) {
		System.out.println("mdl: " + attr);
		String conPath = String.format("data/attr-concept/%s.txt", attr);
		String conMDLPath = String.format("data/attr-mdl/%s-share.txt", attr);
		if (!(FileUtil.hasFile(conPath) && !FileUtil.hasFile(conMDLPath))) {
			return;
		}
		conMap = FileUtil.readFileToMap(conPath);
		conList = new ArrayList<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>>();
		conList1 = new ArrayList<Tuple<Integer, String>>();
		sharenessMap1 = new HashMap<String, Double>();
		wordNetMap1 = new HashMap<String, Double>();
		conList2 = new ArrayList<Tuple<Integer, String>>();
		sharenessMap2 = new HashMap<String, Double>();
		wordNetMap2 = new HashMap<String, Double>();

		// divide to two list
		System.out.println("prepare");
		String[] split;
		for (Entry<String, Double> en : conMap.entrySet()) {
			split = en.getKey().substring(1, en.getKey().length() - 1)
					.split(", ");
			Tuple<Integer, String> t1 = new Tuple<Integer, String>(
					pt.lookup(split[0]), split[0]);
			Tuple<Integer, String> t2 = new Tuple<Integer, String>(
					pt.lookup(split[1]), split[1]);
			conList.add(new Tuple<Tuple<Integer, String>, Tuple<Integer, String>>(
					t1, t2));
			boolean has = false;
			for (Tuple<Integer, String> tuple : conList1) {
				if (tuple.toString().equals(t1.toString())) {
					has = true;
				}
			}
			if (!has) {
				conList1.add(t1);
			}
			has = false;
			for (Tuple<Integer, String> tuple : conList2) {
				if (tuple.toString().equals(t2.toString())) {
					has = true;
				}
			}
			if (!has) {
				conList2.add(t2);
			}
		}

		prepare(conList1, sharenessMap1, wordNetMap1);
		prepare(conList2, sharenessMap2, wordNetMap2);
		// start MDL
		System.out.println("start MDL: " + attr);
		// for left
		GeneticAlgorithm ga = new GeneticAlgorithm(conList.size(), N);
		int[] best = ga.train(IN, new FitnessAlgorithm<int[]>() {
			@Override
			public double calcFitness(int[] entity) {
				double code = MDLcode(entity);
				double fitness = 1.0 / code;
				return fitness;
			}
		});
		List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> bestCon = geneToConList(best);
		System.out.println(bestCon);
	}

	private List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> geneToConList(
			int[] entity) {
		List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> list = new ArrayList<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>>();
		for (int i = 0; i < entity.length; i++) {
			if (entity[i] == 1) {
				list.add(conList.get(i));
			}
		}
		return list;
	}

	double MDLcode(int[] entity) {
		List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> C = geneToConList(entity);
		// code = αL(C)+(1−α)L(X|C)
		String key;
		double value;
		// L(C) = sigma L(c)
		double LC = 0;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : C) {
			key = String.format("p(%s)", conpair.getArg1());
			value = sharenessMap1.get(key);
			LC += CODE(value);
			key = String.format("p(%s)", conpair.getArg2());
			value = sharenessMap1.get(key);
			LC += CODE(value);
		}
		// L(X|C) = sigma L*(xi|C)
		double LXC = 0;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> xipair : C) {
			// L*(xi|C) = min{L(x), log |C| + L(x|c)} for c ∈ C
			double LxiC1 = calculateLxiC(1, C, xipair.getArg1(), sharenessMap1,
					wordNetMap1);
			double LxiC2 = calculateLxiC(2, C, xipair.getArg2(), sharenessMap2,
					wordNetMap2);
			LXC += LxiC1 + LxiC2;
		}
		// code = αL(C)+(1−α)L(X|C)

		double code = α * LC + (1f - α) * LXC;
		return code;
	}

	private double calculateLxiC(int arg,
			List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> C,
			Tuple<Integer, String> xi, Map<String, Double> sharenessMap,
			Map<String, Double> wordNetMap) {
		String key = String.format("p(%s)", xi);
		double value = sharenessMap.get(key);
		double Lx = CODE(value);
		// L*(x|C) = log |C| + min L(x|c)
		double logC = Math.log(C.size());
		double minLxc = Double.MAX_VALUE;
		double localLxc;
		// P (c|x) = 1 − (1 − Pe(c|x))(1 − Pa(c|x))
		// P (x|c) = P (c|x)P (x)/P (c)
		double Pcx, Pecx, Pacx, Pxc, Px, Pc;
		Tuple<Integer, String> c;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : C) {
			if (arg == 1) {
				// left
				c = conpair.getArg1();
			} else {
				// right
				c = conpair.getArg2();
			}
			key = String.format("p(%s|%s)", xi, c);
			Pecx = sharenessMap.get(key).doubleValue();
			Pacx = wordNetMap.get(key).doubleValue();
			key = String.format("p(%s)", xi);
			Px = sharenessMap.get(key).doubleValue();
			key = String.format("p(%s)", c);
			Pc = sharenessMap.get(key).doubleValue();
			Pcx = 1f - (1f - Pecx) * (1f - Pacx);
			Pxc = Pcx * Px / Pc;
			localLxc = CODE(Pxc);
			minLxc = Math.min(minLxc, localLxc);
		}
		// L*(xi|C) = min{L(x), log |C| + L(x|c)} for c ∈ C
		double LxiC = Math.min(Lx, logC + minLxc);
		return LxiC;
	}

	private void prepare(List<Tuple<Integer, String>> conList,
			Map<String, Double> sharenessMap, Map<String, Double> wordNetMap) {
		// set
		HashMap<Integer, HashSet<Integer>> conANDent = new HashMap<Integer, HashSet<Integer>>();
		HashSet<Integer> set;
		for (Tuple<Integer, String> coni : conList) {
			set = new HashSet<Integer>();
			pbd.getEntitySetConceptInFileMode(set, coni.getArg1());
			conANDent.put(coni.getArg1(), set);
		}
		sharenessMap(sharenessMap, conList, conANDent);
		wordNetMap(wordNetMap, conList);
	}

	static double CODE(double p) {
		return -Math.log(p);
	}

	void sharenessMap(Map<String, Double> sharenessMap,
			List<Tuple<Integer, String>> conList,
			HashMap<Integer, HashSet<Integer>> map) {
		HashSet<Integer> set1, set2, jointSet;
		String key;
		double value;
		int globleSum = 0;
		int intersectSize;
		for (Tuple<Integer, String> c : conList) {
			int shareSum = 0;
			set1 = map.get(c.getArg1());
			int[] jointSetSizes = new int[conList.size()];
			for (int i = 0; i < conList.size(); i++) {
				Tuple<Integer, String> cx = conList.get(i);
				System.out.println("prepare intersect: " + c.toString()
						+ " and " + cx.toString());
				if ((intersectSize = intersect(c, cx)) != -1) {
					jointSetSizes[i] = intersectSize;
					shareSum += intersectSize;
				} else {
					if (c.getArg1().intValue() == cx.getArg1().intValue()) {
						intersectSize = set1.size();
						jointSetSizes[i] = intersectSize;
						shareSum += intersectSize;
					} else {
						set2 = map.get(cx.getArg1());
						jointSet = DataUtil.intersectSets(set1, set2);
						intersectSize = jointSet.size();
						jointSetSizes[i] = intersectSize;
						shareSum += intersectSize;
					}
					// store intersectSize
					intersectSizeList.add(new int[] { c.getArg1(),
							cx.getArg1(), intersectSize });
				}
			}
			// export intersect
			exportIntersectList();
			globleSum += shareSum;
			for (int i = 0; i < conList.size(); i++) {
				Tuple<Integer, String> cx = conList.get(i);
				key = String.format("p(%s|%s)", cx, c);
				value = (double) jointSetSizes[i] / (double) shareSum;
				sharenessMap.put(key, value);
				key = String.format("n(%s)", c);
				value = (double) shareSum;
				sharenessMap.put(key, value);
			}
		}
		// calculate globle
		for (Tuple<Integer, String> c : conList) {
			key = String.format("n(%s)", c);
			value = sharenessMap.get(key).doubleValue() / (double) globleSum;
			key = String.format("p(%s)", c);
			sharenessMap.put(key, value);
		}

	}

	private void exportIntersectList() {
		List<String> exportList = new ArrayList<String>();
		for (int[] ints : intersectSizeList) {
			exportList
					.add(String.format("%d %d %d", ints[0], ints[1], ints[2]));
		}
		FileUtil.exportListToFile(exportList, "data/pbintersect.txt");
	}

	private int intersect(Tuple<Integer, String> c, Tuple<Integer, String> cx) {
		for (int[] ints : intersectSizeList) {
			if (ints[0] == c.getArg1().intValue()
					&& ints[1] == cx.getArg1().intValue()) {
				return ints[2];
			}
			if (ints[1] == c.getArg1().intValue()
					&& ints[0] == cx.getArg1().intValue()) {
				return ints[2];
			}
		}
		return -1;
	}

	void wordNetMap(Map<String, Double> wordNetMap,
			List<Tuple<Integer, String>> conList) {
		System.out.println("prepare wordNetMAp");
		String key;
		double value;
		for (Tuple<Integer, String> c : conList) {
			int simSum = 0, sim;
			int[] simSizes = new int[conList.size()];
			for (int i = 0; i < conList.size(); i++) {
				// Tuple<Integer, String> cx = conList.get(i);
				// sim = WordNetUtil.simularity(c.getArg2(), cx.getArg2());
				sim = 0;
				simSizes[i] = sim;
				simSum += sim;
			}
			for (int i = 0; i < conList.size(); i++) {
				Tuple<Integer, String> cx = conList.get(i);
				key = String.format("p(%s|%s)", cx, c);
				if (simSum == 0) {
					value = 0;
				} else {
					value = (double) simSizes[i] / (double) simSum;
				}

				wordNetMap.put(key, value);
			}
		}
	}

}
