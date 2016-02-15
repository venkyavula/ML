import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Record {

	Map<String, Integer> recMap = null;
	Integer classResult = -1;

	public Record() {
		recMap = new HashMap<String, Integer>();
	}

	public Map getRecMap() {
		return recMap;
	}

	public void setRecMap(Map recMap) {
		this.recMap = recMap;
	}

	public Integer getClassResult() {
		return classResult;
	}

	public void setClassResult(Integer classResult) {
		this.classResult = classResult;
	}

	public boolean equals(Object obj) {
		Record rec = (Record) obj;

		Set<Entry<String, Integer>> thisSet = this.recMap.entrySet();
		Set<Entry<String, Integer>> recSet = rec.recMap.entrySet();
		Iterator iter1 = thisSet.iterator();
		Iterator iter2 = thisSet.iterator();
		boolean res = false;
		while (iter1.hasNext()) {
			Entry<String, Integer> entry1 = (Entry<String, Integer>) iter1.next();
			Entry<String, Integer> entry2 = (Entry<String, Integer>) iter2.next();

			String attr = entry1.getKey();
			if (!attr.equals("Class"))
				if ((Integer) this.recMap.get(attr) != (Integer) rec.recMap.get(attr)) {
					return false;
				}

		}

		return true;
	}

}
