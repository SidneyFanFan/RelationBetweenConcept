package DAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import structure.Triple;
import util.FileUtil;

public class ProbaseDAO extends DAO {

	public final static int DataBaseMode = 0;
	public final static int FileMode = 1;

	private int mode;

	public static String selectEntityConcepts = "select * from pair where entity='%s'";

	public static String selectEntitysOfConcept = "select * from pair where concept='%s'";

	List<Integer[]> probase;

	public ProbaseDAO(int mode) {
		super("jdbc:mysql://localhost:3306/probase");
		this.mode = mode;
		if (mode == FileMode) {
			// load probase
			probase = new ArrayList<Integer[]>();
			List<String> lines = FileUtil
					.readFileByLine("data/idedges.orig.txt");
			String[] split;
			int count = 0;
			for (String string : lines) {
				count ++;
				split = string.split("\t");
				probase.add(new Integer[] { Integer.valueOf(split[0]),
						Integer.valueOf(split[1]), Integer.valueOf(split[2]) });
				if(count%1000==0){
					System.out.printf("%d of %d...\n", count,lines.size());
				}
			}
		}
	}

	public void getTripleByEntity(List<Triple<String, String, Integer>> list,
			String entity) {
		if (mode == DataBaseMode) {
			try {
				super.connect();
				stmt = conn.createStatement();
				String sql = String.format(selectEntityConcepts, entity);
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					list.add(new Triple<String, String, Integer>(rs
							.getString("concept"), rs.getString("entity"), rs
							.getInt("cooccurrence")));
				}
				super.close();
			} catch (SQLException e) {
				System.out.println("数据操作错误");
				e.printStackTrace();
			}
		}

	}

	public void getTripleByEntityInFileMode(List<Integer[]> list, int entity) {
		if (mode == FileMode) {
			for (Integer[] integers : probase) {
				if (integers[1] == entity && integers[2] >=1 ) {
					list.add(integers);
				}
			}
		}
	}

	public void getEntityListConceptInFileMode(List<Integer[]> list, int concept) {
		if (mode == FileMode) {
			for (Integer[] integers : probase) {
				if (integers[0] == concept) {
					list.add(integers);
				}
			}
		}
	}

	public void getEntityListConcept(List<String> list, String concept) {
		if (mode == DataBaseMode) {
			try {
				super.connect();
				stmt = conn.createStatement();
				String sql = String.format(selectEntitysOfConcept, concept);
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					list.add(rs.getString("entity"));
				}
				super.close();
			} catch (SQLException e) {
				System.out.println("数据操作错误");
				e.printStackTrace();
			}
		}
	}

	public void getEntitySetConceptInFileMode(HashSet<Integer> set, int concept) {
		if (mode == FileMode) {
			for (Integer[] integers : probase) {
				if (integers[0] == concept) {
					set.add(integers[1]);
				}
			}
		}
	}
	
	public void getEntitySetConceptInFileMode(HashMap<Integer, Integer> set, int concept) {
		if (mode == FileMode) {
			for (Integer[] integers : probase) {
				if (integers[0] == concept) {
					set.put(integers[1],integers[2]);
				}
			}
		}
	}
}
