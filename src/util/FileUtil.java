package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import structure.Tuple;

public class FileUtil {

	public FileUtil() {
	}

	public static String readFile(String filePath) {
		String content = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			while (br.ready()) {
				content += br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void exportStringArray(Object[] output, String filepath) {
		try {
			FileWriter fw = new FileWriter(filepath);
			for (int i = 0; i < output.length; i++) {
				fw.write(output[i].toString());
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void outputPrologFactBase(String baseName,
			Collection<String> list, String filepath) {
		String str = String.format("%s(%s).", baseName, list.toString());
		try {
			FileWriter fw = new FileWriter(filepath);
			fw.write(str);
			fw.write("\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> readFileByLine(String filePath) {
		ArrayList<String> content = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			while (br.ready()) {
				content.add(br.readLine());
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return content;
	}

	public static void exportHashMapByline(HashMap<String, String> map,
			String exportPath) {
		try {
			FileWriter fw = new FileWriter(exportPath);
			for (Entry<String, String> en : map.entrySet()) {
				fw.write(en.getKey());
				fw.write("\n");
				fw.write(en.getValue());
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void exportHashMapByEquation(HashMap<?, ?> map,
			String exportPath) {
		try {
			FileWriter fw = new FileWriter(exportPath);
			for (Entry<?, ?> en : map.entrySet()) {
				fw.write(en.toString());
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void mergeDirectoryToSingleFile(String directoryPath,
			String exportPath) {
		File file = new File(directoryPath);
		try {
			FileWriter writer = new FileWriter(exportPath);
			if (file.isDirectory()) {
				File[] dirFile = file.listFiles();
				for (File f : dirFile) {
					if (f.isFile()) {
						BufferedReader reader = new BufferedReader(
								new FileReader(f));
						while (reader.ready()) {
							writer.write(reader.readLine());
							writer.write("\n");
						}
						reader.close();
					}
				}
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void mergeFiles(List<String> patternPathList,
			String mergedPath) {
		try {
			FileWriter writer = new FileWriter(mergedPath);
			for (String path : patternPathList) {
				File file = new File(path);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while (reader.ready()) {
					writer.write(reader.readLine());
					writer.write("\n");
				}
				reader.close();
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void exportListToFile(List<String> attrsList, String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			for (String string : attrsList) {
				writer.write(string);
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void exportListEntryToFile(
			List<Entry<String, Double>> finalScore, String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			for (Entry<String, Double> en : finalScore) {
				writer.write(en.toString());
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Tuple<String, String>> readFileByLineToTupleInString(
			String filePath) {
		List<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] split;
			while (br.ready()) {
				line = br.readLine().replaceAll("\\(|\\)", "");
				split = line.split(", ");
				list.add(new Tuple<String, String>(split[0], split[1]));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			return null;
		} catch (IOException e) {
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
		return list;
	}

	public static List<Integer[]> readFileByLineToTupleInInteger(String filePath) {
		List<Integer[]> list = new ArrayList<Integer[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] split;
			while (br.ready()) {
				line = br.readLine().replaceAll("\\(|\\)", "");
				split = line.split(", ");
				list.add(new Integer[] { Integer.valueOf(split[0]),
						Integer.valueOf(split[1]) });
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
	
	public static List<int[]> readFileByLineToArrayInInteger(String filePath) {
		List<int[]> list = new ArrayList<int[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] split;
			while (br.ready()) {
				line = br.readLine();
				split = line.split(" ");
				int[] ints = new int[split.length];
				for (int i = 0; i < split.length; i++) {
					ints[i] = Integer.valueOf(split[i]);
				}
				list.add(ints);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
	
	public static List<Tuple<String, String>> readFileByLineToTupleInStringLimit(
			String filePath, int from, int end) {
		List<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] split;
			int pos = 0;
			while (br.ready()) {
				pos++;
				if(pos<from){
					br.readLine();
					continue;
				}
				if(pos>end){
					break;
				}
				line = br.readLine().replaceAll("\\(|\\)", "");
				split = line.split(", ");
				list.add(new Tuple<String, String>(split[0], split[1]));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			return null;
		} catch (IOException e) {
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
		return list;
		
	}


	public static boolean hasFile(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	public static HashMap<String, Double> readFileToMap(String filePath) {
		HashMap<String, Double> content = new HashMap<String, Double>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			String[] split;
			while (br.ready()) {
				try {
					line = br.readLine();
					split = line.split("=");
					content.put(split[0], Double.valueOf(split[1]));
				} catch (NumberFormatException e) {
					continue;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return content;
	}

	
}
