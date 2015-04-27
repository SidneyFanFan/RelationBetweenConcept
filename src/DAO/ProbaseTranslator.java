package DAO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import util.FileUtil;

public class ProbaseTranslator {

	String[] wordList;
	int[] idList;

	public ProbaseTranslator(String filePath) {
		List<String> list = FileUtil.readFileByLine(filePath);

		wordList = new String[list.size()];
		idList = new int[list.size()];
		String[] split;
		int count = 0;
		int i = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			while (br.ready()) {
				split = br.readLine().split("\t");
				count++;
				idList[i] = Integer.valueOf(split[0]);
				wordList[i] = split[1];
				if (count % 10000 == 0) {
					System.out.println("Reading... " + count);
				}
				i++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int lookup(String word) {
		// special cases
		// case 1: title
		// many entities in title has strange "__[number]"
		// for example "Lowri_Turner__1", "Alan_Mruvka__5"
		int pos = word.lastIndexOf("__");
		if (pos > 0) {
			word = word.substring(0, pos);
		}
		// case 2: launchPad
		// many entities are apollo_1, apollo_2
		// remove the tail behind
		pos = word.lastIndexOf("_");
		if (pos >= 0) {
			String lastChar = word.substring(pos + 1);
			try {
				Integer.parseInt(lastChar);
				word = word.substring(0, pos);
			} catch (NumberFormatException e) {
				// do nothing
			}
			if (pos > 0) {
				word = word.substring(0, pos);
			}
		}
		// normal cases
		String w = word.replace("_", " ");
		w.trim();
		for (int i = 0; i < wordList.length; i++) {
			if (wordList[i].equalsIgnoreCase(w)) {
				return idList[i];
			}
		}
		return -1;
	}

	public String lookup(int index) {
		for (int i = 0; i < idList.length; i++) {
			if (idList[i] == index) {
				return wordList[i];
			}
		}
		return null;
	}
}
