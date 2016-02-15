import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TreeNodeVG {

	TreeNodeVG left = null;
	TreeNodeVG right = null;
	String selectedAttr = null;
	String[] attrArr = null;
	ArrayList<Record> positiveRecords = new ArrayList<Record>();
	ArrayList<Record> negativeRecords = new ArrayList<Record>();
	ArrayList<Record> totalRecords = new ArrayList<Record>();
	HashMap<String, Double> infGainMap = new HashMap<String, Double>();
	HashSet<String> ignoreAttrSet = new HashSet<String>();
	int result = -1;
	boolean isLeaf = false;
	int nodeNumber = -1;
	
	public TreeNodeVG()
	{
		
	}

	public TreeNodeVG(List<Record> trianingList, String[] attributeArr, HashSet<String> ignoreSet, boolean isLeft) {

		totalRecords = (ArrayList<Record>) trianingList;
		attrArr = attributeArr;
		setPosNegRecords(totalRecords);
		ignoreAttrSet = ignoreSet;
	}

	public void setPosNegRecords(ArrayList<Record> totalRecords) {

		if (isSameClassDataRecords(totalRecords)) {
			result = (Integer) totalRecords.get(0).getRecMap().get("Class");
			isLeaf = true;
		}

		for (Record r : totalRecords) {
			if (((Integer) r.recMap.get("Class")) == 1) {
				positiveRecords.add(r);
			} else {
				negativeRecords.add(r);
			}

		}

		int totPos = positiveRecords.size();
		int totNegs = negativeRecords.size();
		if (isSameRecordData(totalRecords)) {
			if (totPos > totNegs)
				result = 1;
			else
				result = 0;
			isLeaf = true;

		}

	}

	public TreeNodeVG buildTreeIG(String selected) {
		// first calculate entrophy of S
		int totPos = positiveRecords.size();
		int totNegs = negativeRecords.size();
		int totalRecs = totPos + totNegs;
		HashSet<String> tempSet = null;
		double varienceImpurityOfS = Utilities.calculateVarienceImpurity(totPos, totNegs);

		// System.out.println("S is " + entrophyS);
		double variencePos = 0;
		double varienceNeg = 0;
		int posCntForPos = 0;
		int posCntForNeg = 0;
		int negCntForPos = 0;
		int negCntForNeg = 0;
		double maxIG = -1 * Double.MIN_VALUE;
		String maxIGAttr = null;

		for (String currAttr : attrArr) {
			if (!ignoreAttrSet.contains(currAttr)) {
				for (Record r : totalRecords) {
					int val = ((Integer) r.getRecMap().get(currAttr));
					if (val == 1) {
						if ((Integer) r.getRecMap().get("Class") == 1)
							posCntForPos++;// Attr XX == 1 && Class == 1 
						else
							posCntForNeg++; // Attr XX == 1 && Class == 0 
					} else {
						if ((Integer) r.getRecMap().get("Class") == 1)
							negCntForPos++; //  Attr XX == 0 && Class == 1 
						else
							negCntForNeg++; // Attr XX == 1 && Class == 0 
					}

				}
				variencePos = Utilities.calculateVarienceImpurity(posCntForPos, posCntForNeg);
				varienceNeg = Utilities.calculateVarienceImpurity(negCntForPos, negCntForNeg);
				int posSum = posCntForPos + posCntForNeg;
				int negSum = negCntForPos + negCntForNeg;
				double infoGainCurrAttr = varienceImpurityOfS - ((posSum / totalRecs) * variencePos)
						- ((negSum / totalRecs) * varienceNeg);

				if (maxIG < infoGainCurrAttr) {
					maxIG = infoGainCurrAttr;
					maxIGAttr = currAttr;
				}
				// System.out.println("ATTR : " + currAttr + " " + "Info Gain" +
				// infoGainCurrAttr);

			}

			variencePos = 0;
			varienceNeg = 0;
			posCntForPos = 0;
			posCntForNeg = 0;
			negCntForPos = 0;
			negCntForNeg = 0;

		}
		// System.out.println("selectedAttr" + maxIGAttr);
		selectedAttr = maxIGAttr;
		ignoreAttrSet.add(selectedAttr);
		tempSet = new HashSet<String>(ignoreAttrSet);

		List<Record> zeroChildRecs = new ArrayList<Record>();
		List<Record> oneChildRecs = new ArrayList<Record>();

		for (Record r : totalRecords) {
			if ((Integer) r.getRecMap().get(selectedAttr) == 0) {
				zeroChildRecs.add(r);
			} else {
				oneChildRecs.add(r);
			}

		}

		if (zeroChildRecs.size() == 0 || oneChildRecs.size() == 0) {
			this.isLeaf = true;
			if (zeroChildRecs.size() > 0)
				result = 0;
			else
				result = 1;

			return this;
		}

		left = new TreeNodeVG(zeroChildRecs, attrArr, tempSet, true);
		if (!left.isLeaf)
			left.buildTreeIG(selectedAttr);
		right = new TreeNodeVG(oneChildRecs, attrArr, tempSet, false);
		if (!right.isLeaf)
			right.buildTreeIG(selectedAttr);
		return this;
	}

	public boolean isSameClassDataRecords(ArrayList<Record> listRecs) {
		int cntPositive = 0, cntNegative = 0;
		for (Record r : listRecs) {
			if ((Integer) r.getRecMap().get("Class") == 0)
				cntNegative++;
			else
				cntPositive++;
			if (Math.min(cntNegative, cntPositive) != 0)
				return false;

		}
		if (Math.min(cntNegative, cntPositive) == 0)
			return true;
		return false;
	}

	public boolean isSameRecordData(ArrayList<Record> listRecs) {
		if (listRecs.size() == 0) {
			return true;
		}
		Record first = listRecs.get(0);
		for (Record r : listRecs) {
			if (!first.equals(r))
				return false;
		}
		return true;

	}

}
