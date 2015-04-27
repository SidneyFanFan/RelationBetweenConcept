package expriments;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import algorithms.FitnessAlgorithm;
import algorithms.GeneticAlgorithm;
import structure.Tuple;
import util.FileUtil;
import DAO.ProbaseDAO;
import DAO.ProbaseTranslator;

public class MDLconceptPair {

	public static void main(String[] args) {
		MDLconceptPair mdl = new MDLconceptPair();
		// get attribute
		System.out.println("get attribute...");
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		for (String attr : attrsList) {
			try {
				int status = mdl.init(attr);
				switch (status) {
				case 0: {
					for (double i = 1; i <= 9; i++) {
						mdl.MDLTrain(i / 10.0, 10, 1000);
					}
					mdl.exportResult(attr);
					mdl.clean();
					break;
				}
				case 1: {
					System.out.println("already has " + attr);
					break;
				}
				case 2: {
					System.out.println("small entities " + attr);
					break;
				}

				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	ProbaseDAO pbd = new ProbaseDAO(ProbaseDAO.FileMode);
	ProbaseTranslator pt = new ProbaseTranslator("data/pbwordlist.txt");
	List<int[]> intersectSizeList = FileUtil
			.readFileByLineToArrayInInteger("data/pbpairintersect.txt");

	List<Integer[]> entList;
	HashMap<String, Double> conMap;
	List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> conList;
	Map<String, Double> sharenessMap;
	PrintStream mdlOutput;
	List<String> result;

	public MDLconceptPair() {
		// constructor
	}

	public int init(String attr) throws FileNotFoundException {
		System.out.println("mdl: " + attr);
		String conPath = String.format("data/attr-vote/%s-vote.txt", attr);
		String transEntityPath = String.format("data/attr-trans/%s.txt", attr);
		String conMDLPath = String.format("data/attr-mdl/%s-share.txt", attr);
		if (!(FileUtil.hasFile(conPath) && !FileUtil.hasFile(conMDLPath))) {
			return 1;
		}
		result = new ArrayList<String>();
		entList = FileUtil.readFileByLineToTupleInInteger(transEntityPath);
		conMap = FileUtil.readFileToMap(conPath);
		conList = new ArrayList<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>>();
		sharenessMap = new HashMap<String, Double>();

		// check data
		if (entList.size() < 10) {
			return 2;
		}

		// divide to two list
		System.out.println("translate concept: " + attr);
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
		}
		System.out.println("build sharenessMap: " + attr);
		buildSharenessMap();
		return 0;

	}

	public void MDLTrain(final double alpha, int generationNum, int trainTime) {
		// start MDL
		GeneticAlgorithm ga = new GeneticAlgorithm(conList.size(),
				generationNum);
		int[] best = ga.train(trainTime, new FitnessAlgorithm<int[]>() {
			@Override
			public double calcFitness(int[] entity) {
				double code = MDLcode(entity, alpha);
				if (Double.isNaN(code)) {
					System.out.println("here");
				}
				double fitness = Math.exp(1.0 / code); // smaller code win
				return fitness;
			}
		});
		List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> bestCon = geneToConList(best);
		result.add(String.format("α = %f: %s", alpha, bestCon.toString()));
	}

	public void exportResult(String attr) throws FileNotFoundException {
		String conMDLPath = String.format("data/attr-mdl/%s-share.txt", attr);
		mdlOutput = new PrintStream(conMDLPath);
		for (String r : result) {
			mdlOutput.println(r);
		}
	}

	public void clean() {
		mdlOutput.close();
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

	double MDLcode(int[] entity, double alpha) {
		List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> C = geneToConList(entity);
		if (C.size() == 0) {
			return Double.MAX_VALUE;
		}
		// code = αL(C)+(1−α)L(X|C)
		String key;
		double value;
		// L(C) = sigma L(c)
		double LC = 0;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : C) {
			key = String.format("p(%s)", conpair.toString());
			value = sharenessMap.get(key);
			LC += CODE(value);
		}
		if (Double.isNaN(LC)) {
			System.out.println("here");
		}
		// L(X|C) = sigma L*(xi|C)
		double LXC = 0;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> xipair : C) {
			// L*(xi|C) = min{L(x), log |C| + L(x|c)} for c ∈ C
			double LxiC1 = calculateLxiC(C, xipair);
			LXC += LxiC1;
			if (Double.isNaN(LXC)) {
				System.out.println();
			}
		}
		// code = αL(C)+(1−α)L(X|C)

		double code = alpha * LC + (1f - alpha) * LXC;
		if (Double.isNaN(code)) {
			System.out.println();
		}
		return code;
	}

	private double calculateLxiC(
			List<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>> C,
			Tuple<Tuple<Integer, String>, Tuple<Integer, String>> xipair) {
		String key = String.format("p(%s)", xipair.toString());
		double value = sharenessMap.get(key);
		double Lx = CODE(value);
		// L*(x|C) = log |C| + min L(x|c)
		double logC = Math.log(C.size());
		double minLxc = Double.MAX_VALUE;
		double localLxc;
		// P (c|x) = 1 − (1 − Pe(c|x))(1 − Pa(c|x))
		// P (x|c) = P (c|x)P (x)/P (c)
		double Pcx, Pecx, Pacx, Pxc, Px, Pc;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : C) {
			key = String.format("p(%s|%s)", xipair.toString(),
					conpair.toString());
			Pecx = sharenessMap.get(key).doubleValue();
			Pacx = 0; // TODO other features
			key = String.format("p(%s)", xipair.toString());
			Px = sharenessMap.get(key).doubleValue();
			key = String.format("p(%s)", conpair.toString());
			Pc = sharenessMap.get(key).doubleValue();
			Pcx = 1f - (1f - Pecx) * (1f - Pacx);
			// Px, Pc might be zero because concept pairs are combined
			// previously
			// if both are zeros, Pxc is NaN, and this is not OK
			// if onlt Pc is zero, Pxc is Infinity, and this is OK
			Pxc = Pcx * Px / Pc;
			if (Double.isNaN(Pxc)) {
				Pxc = Double.MAX_VALUE;
			}
			localLxc = CODE(Pxc);
			minLxc = Math.min(minLxc, localLxc);
		}
		// L*(xi|C) = min{L(x), log |C| + L(x|c)} for c ∈ C
		double LxiC = Math.min(Lx, logC + minLxc);
		return LxiC;
	}

	private void buildSharenessMap() {
		// entity set of concept
		HashMap<Integer, HashSet<Integer>> conANDent = new HashMap<Integer, HashSet<Integer>>();
		HashSet<Integer> set;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : conList) {
			set = new HashSet<Integer>();
			pbd.getEntitySetConceptInFileMode(set, conpair.getArg1().getArg1());
			conANDent.put(conpair.getArg1().getArg1(), set);
			set = new HashSet<Integer>();
			pbd.getEntitySetConceptInFileMode(set, conpair.getArg2().getArg1());
			conANDent.put(conpair.getArg2().getArg1(), set);
		}
		sharenessMap(conANDent);
	}

	static double CODE(double p) {
		return -Math.log(p);
	}

	void sharenessMap(HashMap<Integer, HashSet<Integer>> conANDent) {
		HashSet<Integer> Ei, Ej, Exi, Exj;
		String key;
		double value;
		int globalSum = 0;
		int localSharenessSize;
		int localSharenessSum;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : conList) {
			localSharenessSum = 0;
			Ei = conANDent.get(conpair.getArg1().getArg1());
			Ej = conANDent.get(conpair.getArg2().getArg1());
			int[] localSharenessArray = new int[conList.size()];
			System.out.println("calculate shareness of: " + conpair.toString());
			// calculate local shareness of conpair
			for (int i = 0; i < conList.size(); i++) {
				Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpairx = conList
						.get(i);
				System.out.println("calculate pair shareness: "
						+ conpair.toString() + ", " + conpairx.toString());
				Exi = conANDent.get(conpairx.getArg1().getArg1());
				Exj = conANDent.get(conpairx.getArg2().getArg1());
				if ((localSharenessSize = checkShareness(conpair, conpairx)) != -1) {
					localSharenessArray[i] = localSharenessSize;
					localSharenessSum += localSharenessSize;
				} else {
					// store intersectSize
					localSharenessSize = 0;
					// chekc how many entity pairs in (ci,cj)&(cxi,cxj)
					int ent1, ent2;
					for (Integer[] ints : entList) {
						ent1 = ints[0];
						ent2 = ints[1];
						if (Ei.contains(ent1) && Ej.contains(ent2)
								&& Exi.contains(ent1) && Exj.contains(ent2)) {
							localSharenessSize++;
						}
					}
					localSharenessArray[i] = localSharenessSize;
					localSharenessSum += localSharenessSize;
					intersectSizeList.add(new int[] {
							conpair.getArg1().getArg1(),
							conpair.getArg2().getArg1(),
							conpairx.getArg1().getArg1(),
							conpairx.getArg2().getArg1(), localSharenessSize });
				}
			}
			// calculate proportion
			for (int i = 0; i < conList.size(); i++) {
				Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpairx = conList
						.get(i);
				key = String.format("p(%s|%s)", conpairx.toString(),
						conpair.toString());
				value = localSharenessSum == 0 ? 0
						: (double) localSharenessArray[i]
								/ (double) localSharenessSum;
				sharenessMap.put(key, value);
				key = String.format("n(%s)", conpair.toString());
				value = (double) localSharenessSum;
				sharenessMap.put(key, value);
			}
			// export intersect
			exportIntersectList();
			globalSum += localSharenessSum;
		}
		// calculate global proportion
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : conList) {
			key = String.format("n(%s)", conpair.toString());
			value = globalSum == 0 ? 0 : sharenessMap.get(key).doubleValue()
					/ (double) globalSum;
			sharenessMap.remove(key);
			key = String.format("p(%s)", conpair.toString());
			sharenessMap.put(key, value);
		}
	}

	private void exportIntersectList() {
		List<String> exportList = new ArrayList<String>();
		for (int[] ints : intersectSizeList) {
			exportList.add(String.format("%d %d %d %d %d", ints[0], ints[1],
					ints[2], ints[3], ints[4]));
		}
		FileUtil.exportListToFile(exportList, "data/pbpairintersect.txt");
	}

	private int checkShareness(
			Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair,
			Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpairx) {
		int ci, cj, cxi, cxj;
		ci = conpair.getArg1().getArg1().intValue();
		cj = conpair.getArg2().getArg1().intValue();
		cxi = conpairx.getArg1().getArg1().intValue();
		cxj = conpairx.getArg2().getArg1().intValue();

		return -1;
		
//		for (int[] ints : intersectSizeList) {
//			if (ints[0] == ci && ints[1] == cj && ints[2] == cxi
//					&& ints[3] == cxj) {
//				return ints[4];// TODO wrong
//			}
//			if (ints[2] == ci && ints[3] == cj && ints[0] == cxi
//					&& ints[1] == cxj) {
//				return ints[4];// TODO wrong
//			}
//		}
//		return -1;
	}

}
