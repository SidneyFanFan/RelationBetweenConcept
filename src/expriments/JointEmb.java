package expriments;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import util.DataUtil;
import util.FileUtil;

public class JointEmb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// get attribute
		System.out.println("get attribute...");
		String attrPath = "data/attrs.txt";
		List<String> attrsList;
		attrsList = FileUtil.readFileByLine(attrPath);
		for (String attr : attrsList) {
			start(attr);
		}
	}

	private static void start(String attr) {
		String conPath = "data/attr-concept/" + attr + ".txt";
		String jointPath = "data/attr-concept/" + attr + "_joint.txt";
		String embPath2 = "data/attr-embed/" + attr + "_ce-ce.txt";
		if (!(FileUtil.hasFile(conPath) && FileUtil.hasFile(embPath2))) {
			return;
		}
		HashMap<String, Double> conMap = FileUtil.readFileToMap(conPath);
		HashMap<String, Double> ceMap = FileUtil.readFileToMap(embPath2);
		HashMap<String, Double> jointMap = new HashMap<String, Double>();

		double jointValue;
		for (Entry<String, Double> en : conMap.entrySet()) {
			double nor = nor(ceMap.get(en.getKey()).doubleValue());
			jointValue = nor * en.getValue().doubleValue();
			jointMap.put(en.getKey(), jointValue);
		}
		FileUtil.exportListEntryToFile(
				DataUtil.convertMapToSortedListInDouble(jointMap), jointPath);

	}

	public static double nor(double x) {
		return 1 / Math.sqrt(2 * Math.PI) * Math.pow(Math.E, -x * x / 2);
	}
}
