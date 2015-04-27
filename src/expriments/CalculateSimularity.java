package expriments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import util.FileUtil;

import clldsystem.data.LUCENEWikipediaAnalyzer;
import clldsystem.esa.ESAAnalyzer;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Searcher.UnknownWordException;
import com.medallia.word2vec.Word2VecModel;
import common.config.AppConfig;
import common.db.DB;

public class CalculateSimularity {

	private Word2VecModel binModel;
	private Searcher binSearcher;
	private ESAAnalyzer esa;

	public CalculateSimularity(String modelPath) {

		try {
			initW2V(modelPath);
			initESA();
		} catch (ClassNotFoundException | SQLException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void initESA() throws ClassNotFoundException, SQLException,
			IOException {
		// load config
		AppConfig cfg = AppConfig.getInstance();
		cfg.setSection("ESAAnalyzer");

		// create analyzer
		System.out.println(cfg.getSString("db"));
		esa = new ESAAnalyzer(new DB(cfg.getSString("db")),
				cfg.getSString("lang"));
		LUCENEWikipediaAnalyzer wikiAnalyzer = new LUCENEWikipediaAnalyzer(
				cfg.getSString("stopWordsFile"), cfg.getSString("stemmerClass"));
		wikiAnalyzer.setLang(cfg.getSString("lang"));
		esa.setAnalyzer(wikiAnalyzer);
	}

	private void initW2V(String modelPath) throws IOException {
		File binFile = new File(modelPath);
		System.out.println("loading binary model...");
		binModel = Word2VecModel.fromBinFile(binFile);
		System.out.println("loading binary model finished...");
		binSearcher = binModel.forSearch();
		System.out.println("finish loading binary model...");
	}

	public double similarityW2V(String word1, String word2)
			throws UnknownWordException {
		double s = binSearcher.cosineDistance(word1, word2);
		return s;
	}
	
	public double similarityESA(String word1, String word2){
		PrintStream oo = System.out;
		try {
			System.setOut(new PrintStream("esa.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double s = esa.getRelatedness(word1, word2);
		System.setOut(oo);
		return s;
	}

	public boolean hasWord(String word) throws UnknownWordException {
		return binSearcher.contains(word);
	}

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, IOException {

	}

	static void start(String attr, CalculateSimularity cs) {
		String rawPath = "data/attr-raw-small/" + attr + ".txt";
		String simPath = "data/attr-sim/" + attr + ".txt";
		if (!(FileUtil.hasFile(rawPath) && !FileUtil.hasFile(simPath))) {
			return;
		}
		List<String> file = FileUtil.readFileByLine(rawPath);
		HashMap<String, Double> simMap = new HashMap<String, Double>();
		String[] split;
		String[] words1;
		String[] words2;
		for (String pair : file) {
			split = pair.replaceAll("\\(|\\)", "").toLowerCase().split(", ");
			words1 = split[0].split("_");
			words2 = split[1].split("_");
			String word1;
			String word2;
			double sum = 0.0;
			int count = 0;
			try {
				for (int i = 0; i < words1.length; i++) {
					word1 = words1[i];
					for (int j = 0; j < words2.length; j++) {
						word2 = words2[j];
						if (cs.hasWord(word1) && cs.hasWord(word2)) {
							double sim = cs.similarityW2V(word1, word2);
							sum += 1.0 / sim;
							if (sim < 0) {
								System.out.printf("(%s, %s)less=%f\n", word1,
										word2, sim);
							}
							count++;
						}
					}
				}
				double simScore = sum == 0 ? -1 : (double) count / sum;
				System.out.println(pair + "=" + simScore);
				simMap.put(pair, simScore);
			} catch (UnknownWordException e) {
				continue;
			}
		}
		FileUtil.exportHashMapByEquation(simMap, simPath);
	}
}
