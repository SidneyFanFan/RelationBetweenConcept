package DAO;

import java.sql.SQLException;
import java.util.List;

import structure.Tuple;

public class DBpediaDAO extends DAO {

	public static String selectDistinctAttributes = "select distinct attribute from infobox";
	public static String selectEntityPairWithAttr = "select * from infobox where attribute='%s'";

	public DBpediaDAO(String database) {
		super(database);
	}

	public void getDistinctAttributes(List<String> attrsList) {
		try {
			super.connect();
			stmt = conn.createStatement();
			String sql = selectDistinctAttributes;
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				attrsList.add(rs.getString("attribute"));
			}
			super.close();
		} catch (SQLException e) {
			System.out.println("数据操作错误");
			e.printStackTrace();
		}
	}

	public void getEntityPairsWithAttr(List<Tuple<String, String>> list,
			String attr) {
		try {
			super.connect();
			stmt = conn.createStatement();
			String sql = String.format(selectEntityPairWithAttr, attr);
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				list.add(new Tuple<String, String>(rs.getString("entity"), rs
						.getString("value").replaceAll("\r|\n", "")));
			}
			super.close();
		} catch (SQLException e) {
			System.out.println("数据操作错误");
			e.printStackTrace();
		}
	}

}
