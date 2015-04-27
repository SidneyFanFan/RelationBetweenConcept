package expriments;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import structure.Tuple;
import util.DataUtil;
import util.FileUtil;
import DAO.ProbaseDAO;
import DAO.ProbaseTranslator;

public class VoteConceptPair {

	public static void main(String[] args) {
		VoteConceptPair voteModel = new VoteConceptPair();
		// get attribute
		System.out.println("get attribute...");
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		for (String attr : attrsList) {
			try {
				int status = voteModel.init(attr);
				switch (status) {
				case 0: {
					voteModel.rank();
					voteModel.exportResult(attr);
					voteModel.clean();
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
	Map<String, Double> probabilityMap;
	PrintStream mdlOutput;
	List<String> result;

	public VoteConceptPair() {
		// constructor
	}

	public int init(String attr) throws FileNotFoundException {
		System.out.println("init: " + attr);
		String conPath = String.format("data/attr-concept/%s.txt", attr);
		String transEntityPath = String.format("data/attr-trans/%s.txt", attr);
		String votePath = String.format("data/attr-vote/%s-vote.txt", attr);
		if (!(FileUtil.hasFile(conPath) && !FileUtil.hasFile(votePath))) {
			return 1;
		}
		result = new ArrayList<String>();
		entList = FileUtil.readFileByLineToTupleInInteger(transEntityPath);
		conMap = FileUtil.readFileToMap(conPath);
		conList = new ArrayList<Tuple<Tuple<Integer, String>, Tuple<Integer, String>>>();
		probabilityMap = new HashMap<String, Double>();

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
		System.out.println("build probabilityMap: " + attr);
		buildProbabilityMap();
		return 0;

	}

	public void rank() {
		// start rank
		List<Entry<String, Double>> rank = DataUtil
				.convertMapToSortedListInDouble(probabilityMap);
		for (Entry<String, Double> entry : rank) {
			result.add(entry.toString());
		}
	}

	public void exportResult(String attr) throws FileNotFoundException {
		String conMDLPath = String.format("data/attr-vote/%s-vote.txt", attr);
		mdlOutput = new PrintStream(conMDLPath);
		for (String r : result) {
			mdlOutput.println(r);
		}
	}

	public void clean() {
		mdlOutput.close();
	}

	private void buildProbabilityMap() {
		// entity set of concept
		HashMap<Integer, HashMap<Integer, Integer>> conANDent = new HashMap<Integer, HashMap<Integer,Integer>>();
		HashMap<Integer, Integer> set;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : conList) {
			set = new HashMap<Integer, Integer>();
			pbd.getEntitySetConceptInFileMode(set, conpair.getArg1().getArg1());
			conANDent.put(conpair.getArg1().getArg1(), set);
			set = new HashMap<Integer, Integer>();
			pbd.getEntitySetConceptInFileMode(set, conpair.getArg2().getArg1());
			conANDent.put(conpair.getArg2().getArg1(), set);
		}
		vote(conANDent);
	}

	void vote(HashMap<Integer, HashMap<Integer, Integer>> conANDent) {
		HashMap<Integer, Integer> Ei, Ej;
		String key;
		double value;
		int globalSum = 0;
		int localSize;
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : conList) {
			localSize = 0;
			Ei = conANDent.get(conpair.getArg1().getArg1());
			Ej = conANDent.get(conpair.getArg2().getArg1());
			System.out.println("calculate vote of: " + conpair.toString());
			int ent1, ent2;
			for (Integer[] ints : entList) {
				ent1 = ints[0];
				ent2 = ints[1];
				if (Ei.containsKey(ent1) && Ej.containsKey(ent2)) {
					localSize+=Ei.get(ent1)*Ej.get(ent2);
				}
			}
			// store local size
			globalSum += localSize;
			key = String.format("n(%s)", conpair.toString());
			value = (double) localSize;
			probabilityMap.put(key, value);
		}
		// calculate proportion
		for (Tuple<Tuple<Integer, String>, Tuple<Integer, String>> conpair : conList) {
			key = String.format("n(%s)", conpair.toString());
			value = probabilityMap.get(key).doubleValue();
			probabilityMap.remove(key);
			// avoid 0.0/0.0
			value = globalSum == 0 ? 0 : value / (double) globalSum;
			key = String.format("(%s, %s)", conpair.getArg1().getArg2(),conpair.getArg2().getArg2());
			probabilityMap.put(key, value);
		}
	}

}
