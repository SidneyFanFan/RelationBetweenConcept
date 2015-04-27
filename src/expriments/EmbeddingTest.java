package expriments;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.medallia.word2vec.Searcher.UnknownWordException;

import util.DataUtil;
import util.FileUtil;

public class EmbeddingTest {

	static CalculateSimularity cs = new CalculateSimularity(
			"library/vectors.bin");
	static PrintStream oo = System.out;
	static PrintStream oe = System.err;
	
	public static void main(String[] args) {

		// get attribute
		System.out.println("get attribute...");
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		for (String attr : attrsList) {
			// System.out.println("start entityPairSimularity for " + attr);
			// entityPairSimularity(attr);
			System.out.println("start entityConceptSimularity for " + attr);
			entityConceptSimularity(attr);
		}
		// entityConceptSimularity("country");
	}

	private static void entityPairSimularity(String attr) {
		String rawPath = "data/attr-raw-small/" + attr + ".txt";
		String simPath = "data/attr-sim/" + attr + ".txt";
		if (!(FileUtil.hasFile(rawPath) && !FileUtil.hasFile(simPath))) {
			return;
		}
		List<String> file = FileUtil.readFileByLine(rawPath);
		HashMap<String, Double> simMap = new HashMap<String, Double>();
		String[] split;
		String[] tokens1;
		String[] tokens2;
		String ent1, ent2;
		for (String pair : file) {
			split = pair.replaceAll("\\(|\\)", "").toLowerCase().split(", ");
			if (split.length != 2) {
				continue;
			}
			ent1 = split[0];
			ent2 = split[1];
			tokens1 = ent1.split("_");
			tokens2 = ent2.split("_");
			String token1;
			String token2;
			double sum = 0.0;
			int count = 0;
			// TODO
			// try {
			// for (int i = 0; i < words1.length; i++) {
			// token1 = tokens1[i];
			// for (int j = 0; j < words2.length; j++) {
			// token2 = tokens2[j];
			// if (cs.hasWord(token1) && cs.hasWord(token2)) {
			// double sim = cs.similarityW2V(token1, token2);
			// sum += 1.0 / sim;
			// if (sim < 0) {
			// System.out.printf("(%s, %s)less=%f\n", token1,
			// token2, sim);
			// }
			// count++;
			// }
			// }
			// }
			double simScore;
			simScore = cs.similarityESA(split[0], split[1]);
			System.out.println(pair + "=" + simScore);
			simMap.put(pair, simScore);
			// } catch (UnknownWordException e) {
			// continue;
			// }
		}
		FileUtil.exportHashMapByEquation(simMap, simPath);

	}

	private static void entityConceptSimularity(String attr) {
		String conPath = "data/attr-concept/" + attr + ".txt";
		String simPath = "data/attr-sim/" + attr + ".txt";
		String embPath1 = "data/attr-esa/" + attr + "_cc-ee.txt";
		String embPath2 = "data/attr-esa/" + attr + "_ce-ce.txt";
		if (!(FileUtil.hasFile(conPath) && FileUtil.hasFile(simPath))) {
			return;
		}
		HashMap<String, Double> conMap = FileUtil.readFileToMap(conPath);
		HashMap<String, Double> entMap = FileUtil.readFileToMap(simPath);

		HashMap<String, Double> cceeMap = new HashMap<String, Double>();
		HashMap<String, Double> ceceMap = new HashMap<String, Double>();

		String[] split;
		String pair;
		double consim, simdiffsum;
		// count average attr
		int meancount = 0;
		double eesum = 0.0;
		for (Entry<String, Double> en : entMap.entrySet()) {
			if (en.getValue() != -1 && en.getValue() != 0
					&& !en.getValue().isNaN()) {
				// ccee
				eesum += en.getValue();
				meancount++;
			}
		}
		double attrmean = eesum / (double) meancount;
		// count sim for each concept
		for (Entry<String, Double> conpair : conMap.entrySet()) {
			pair = conpair.getKey().replaceAll("\\(|\\)", "");
			split = pair.split(", ");
			String con1 = split[0];
			String con2 = split[1];
			simdiffsum = 0.0;
			int diffcount = 0;
			try { // for ccee
				oo.println(attr+": "+conpair);
				redirect();
				consim = cs.similarityW2V(con1, con2);
				// try { // for cece
				// int tokenCount = 0;
				for (Entry<String, Double> entpair : entMap.entrySet()) {
					if (entpair.getValue() != -1 && entpair.getValue() != 0
							&& !entpair.getValue().isNaN()) {
						// cece
						String[] entsplit = entpair.getKey()
								.replaceAll("\\(|\\)", "").toLowerCase()
								.split(", ");
						String ent1, ent2;
						ent1 = entsplit[0];
						ent2 = entsplit[1];
						// String[] entTokens1 = ent1.split("_");
						// String[] entTokens2 = ent2.split("_");
						// double tempdiffsum = 0.0;
						// int tempdiffcount = 0;
						// TODO
						// for (int i = 0; i < ents1.length; i++) {
						// for (int j = 0; j < ents2.length; j++) {
						// String ent1 = ents1[i];
						// String ent2 = ents2[j];
						// if (cs.hasWord(ent1) &&
						// cs.hasWord(ent2)&&cs.hasWord(split[0]) &&
						// cs.hasWord(split[1])) {
						// tempdiffcount++;
						// tempdiffsum += Math
						// .abs(cs.similarityW2V(con1,
						// ent1)
						// - cs.similarityW2V(
						// con2, ent2));
						// }
						// }
						// }
						// if(tempdiffcount!=0){
						// simdiff = tempdiffsum / (double) tempdiffcount;
						// simdiffsum += simdiff;
						// tokenCount++;
						// }
						// TODO esa-esa so slow
						simdiffsum += 0;
						diffcount++;
					}
				}
				// } catch (UnknownWordException e) {
				// simdiffsum = -1;
				// }
			} catch (UnknownWordException e) {
				consim = -1;
				simdiffsum = 0;
			}
			// ccee
			double cceeresult = normaldistribute(1, consim / attrmean)
					* conpair.getValue();
			cceeMap.put(conpair.getKey(), cceeresult);
			// cece
			double ceceresult = normaldistribute(0, simdiffsum
					/ (double) diffcount)
					* conpair.getValue();
			ceceMap.put(conpair.getKey(), ceceresult);
		}
		// cc/ee
		FileUtil.exportListEntryToFile(
				DataUtil.convertMapToSortedListInDouble(cceeMap), embPath1);

		// ce/ce
		FileUtil.exportListEntryToFile(
				DataUtil.convertMapToSortedListInDouble(ceceMap), embPath2);
		redirectBack();
	}

	private static void redirect() {
		try {
			System.setErr(new PrintStream("logs/esa.log"));
			System.setOut(new PrintStream("logs/esa.log"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void redirectBack() {
			System.setErr(oe);
			System.setOut(oo);
	}

	static double normaldistribute(double u, double x) {
		double result = 2.0 / (Math.exp(x - u) + Math.exp(-(x - u)));
		return result;
	}
}
