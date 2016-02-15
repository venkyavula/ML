import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TreeNode implements Cloneable{

	TreeNode left = null;
	TreeNode right = null;
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
	public TreeNode()
	{
		 
	}
	
	@Override
    protected TreeNode clone() throws CloneNotSupportedException {
        return (TreeNode) super.clone();
    }
	public TreeNode(TreeNode copy)
	{
		 this.totalRecords = new ArrayList<Record> (copy.totalRecords);
		 this.attrArr = new String[copy.attrArr.length];
		 this.attrArr= copy.attrArr;
		 this.ignoreAttrSet = new HashSet<String>(copy.ignoreAttrSet);
		 this.positiveRecords = new ArrayList<Record>(copy.positiveRecords);
		 this.negativeRecords = new ArrayList<Record>(copy.negativeRecords);
		if(copy.left!=null)
		{
			this.left= new TreeNode(copy.left);
		}
		if(copy.right!=null)
		{
			this.right= new TreeNode(copy.right);
		}
		 this.right= copy.right;
		 this.result = copy.result;
		 this.isLeaf= copy.isLeaf;
		 this.nodeNumber= copy.nodeNumber;
		 this.selectedAttr= copy.selectedAttr;
	}

	public TreeNode(List<Record> trianingList, String[] attributeArr, HashSet<String> ignoreSet, boolean isLeft) {

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

	public TreeNode buildTreeIG(String selected) {
		// first calculate entrophy of S
		int totPos = positiveRecords.size();
		int totNegs = negativeRecords.size();
		int totalRecs = totPos + totNegs;
		HashSet<String> tempSet = null;
		/*
		 * if (ignoreAttrSet.size() == attrArr.length) { System.out.println(
		 * "returned 3"); if (totPos >= totNegs) result = 1; else result = 0;
		 * isLeaf = true; return this; }
		 */

		double entrophyS = Utilities.calculateEntropy(totPos, totNegs);

		// System.out.println("S is " + entrophyS);
		double entrophyPos = 0;
		double entrophyNeg = 0;
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
							posCntForPos++;
						else
							posCntForNeg++;
					} else {
						if ((Integer) r.getRecMap().get("Class") == 1)
							negCntForPos++;
						else
							negCntForNeg++;
					}

				}
				entrophyPos = Utilities.calculateEntropy(posCntForPos, posCntForNeg);
				entrophyNeg = Utilities.calculateEntropy(negCntForPos, negCntForNeg);
				int posSum = posCntForPos + posCntForNeg;
				int negSum = negCntForPos + negCntForNeg;
				double infoGainCurrAttr = entrophyS - ((posSum / totalRecs) * entrophyPos)
						- ((negSum / totalRecs) * entrophyNeg);

				if (maxIG < infoGainCurrAttr) {
					maxIG = infoGainCurrAttr;
					maxIGAttr = currAttr;
				}
				// System.out.println("ATTR : " + currAttr + " " + "Info Gain" +
				// infoGainCurrAttr);

			}

			entrophyPos = 0;
			entrophyNeg = 0;
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

		left = new TreeNode(zeroChildRecs, attrArr, tempSet, true);
		if (!left.isLeaf)
			left.buildTreeIG(selectedAttr);
		right = new TreeNode(oneChildRecs, attrArr, tempSet, false);
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
