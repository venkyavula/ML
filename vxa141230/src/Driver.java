import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Driver {

	String[] attributeArr = null;
	List<Record> trianingList = new ArrayList<Record>();
	List<Record> validationDataList = new ArrayList<Record>();
	List<Record> testDataList = new ArrayList<Record>();
	String trainFile = null;
	String validationFile = null;
	String testFile = null;

	public static void main(String[] args) throws CloneNotSupportedException {
		Driver obj = new Driver();
		obj.trainFile = args[2];
		obj.validationFile = args[3];
		obj.testFile = args[4];
		obj.readTrainFile(args);

	}

	private void readTrainFile(String[] args) throws CloneNotSupportedException {

		BufferedReader trainBuffReader = null, validationBufReader = null, testBuffReader = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			trainBuffReader = new BufferedReader(new FileReader(trainFile));
			validationBufReader = new BufferedReader(new FileReader(validationFile));
			testBuffReader = new BufferedReader(new FileReader(testFile));
			if ((line = trainBuffReader.readLine()) != null) {
				// use comma as separator
				attributeArr = line.split(cvsSplitBy);
			}
			while ((line = trainBuffReader.readLine()) != null) {
				// use comma as separator
				Record rec = new Record();
				int index = 0;
				for (String s : line.split(cvsSplitBy)) {
					Integer val = Integer.parseInt(s);
					rec.recMap.put(attributeArr[index++], val);
				}
				trianingList.add(rec);
			}
			// reading validation data
			validationDataList = readingFIle(validationBufReader, cvsSplitBy);
			// reading test data
			testDataList = readingFIle(testBuffReader, cvsSplitBy);
			HashSet<String> ignoreAttrSet = new HashSet<String>();
			ignoreAttrSet.add("Class");
			TreeNode root = new TreeNode(trianingList, attributeArr, ignoreAttrSet, false);
			root = root.buildTreeIG("");
			if (args[5].equals("yes"))
				printTree(root, 0);
			TreeNode initRoot = root;
			System.out.println("Before Pruning Accuracy on test dataset : "+calculateAccuracy(initRoot, testDataList));
			TreeNode finalRoot = pruneTree(root, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			if (args[5].equals("yes"))
				printTree(finalRoot, 0);
			System.out.println("After Pruning Accuracy on test dataset : "+calculateAccuracy(finalRoot, testDataList));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeBuffReader(trainBuffReader);
			closeBuffReader(validationBufReader);
			closeBuffReader(testBuffReader);
		}

		System.out.println("Done");
	}

	private void closeBuffReader(BufferedReader buffReader) {
		if (buffReader != null) {
			try {
				buffReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<Record> readingFIle(BufferedReader validationBufReader, String cvsSplitBy) throws IOException {
		String line;
		ArrayList<Record> dataList = new ArrayList<Record>();
		if ((line = validationBufReader.readLine()) != null) {
			// use comma as separator
			// attributeArr = line.split(cvsSplitBy);
		}
		while ((line = validationBufReader.readLine()) != null) {
			// use comma as separator
			Record rec = new Record();
			int index = 0;
			for (String s : line.split(cvsSplitBy)) {
				Integer val = Integer.parseInt(s);
				rec.recMap.put(attributeArr[index++], val);
			}
			dataList.add(rec);
		}
		return dataList;
	}

	private void printTree(TreeNode root, int pipeCnt) {

		if (root == null)
			return;
		 
		if (root.isLeaf) {
			System.out.print(" " + root.result);
		}
		System.out.println();
		if (root.left != null) {
			printPipes(pipeCnt);
			System.out.print(root.selectedAttr + " = 0 :");
			if (root.result != -1)
				System.out.print(root.result);
			printTree(root.left, pipeCnt + 1);
		}
		if (root.right != null) {
			printPipes(pipeCnt);
			System.out.print(root.selectedAttr + " = 1 :");
			if (root.result != -1)
				System.out.print(root.result);
			printTree(root.right, pipeCnt + 1);
		}

	}

	private void printPipes(int pipeCnt) {

		for (int i = pipeCnt; i > 0; i--) {
			System.out.print("| ");
		}

	}

	private double calculateAccuracy(TreeNode root, List<Record> trianingList2) {
		if (root == null || trianingList2 == null || trianingList2.size() == 0)
			return 0.0;
		int hitCtr = 0;
		int totalRecs = trianingList2.size();

		for (Record rec : trianingList2) {
			if (checkClassForRecord(rec, root)) {
				hitCtr++;
			}

		}
		double accuracy = (double) hitCtr / totalRecs;

		return accuracy;

	}

	private boolean checkClassForRecord(Record rec, TreeNode root) {
		int actualClass = (Integer) rec.getRecMap().get("Class");
		int predictedClass = -1;

		TreeNode temp = root;

		while (temp != null) {
			if (temp.isLeaf) {
				predictedClass = temp.result;
				break;
			}
			String currAttr = temp.selectedAttr;
			int currVal = (Integer) rec.getRecMap().get(currAttr);
			if (currVal == 1) {
				temp = temp.right;
			} else {
				temp = temp.left;
			}

		}

		return actualClass == predictedClass;
	}

	// ====================Pruning ==============================

	public TreeNode pruneTree(final TreeNode root, int first, int last) throws CloneNotSupportedException {
		if (root == null)
			return null;
		TreeNode bestRoot = root;
		double bestAccuracy = calculateAccuracy(root, validationDataList);
		double currAccuracy = 0.0;

		for (int index = 1; index <= first; index++) {
			TreeNode currRoot = copy(root);
			int mIndex = getRandomNumberInRange(1, last);
			for (int jIndex = 1; jIndex <= mIndex; jIndex++) {
				int nonLeafCnt = findNonLeafCount(currRoot);
				int currNumber = assignNumbers(currRoot, 1, nonLeafCnt);
				if (nonLeafCnt + 1 != currNumber) {
					System.out.println("Error in something");
				} else {
					if (nonLeafCnt == 0)
						break;
					int pIndex = getRandomNumberInRange(1, nonLeafCnt);
					// TreeNode temp = currRoot;
					pruneNodeRec(currRoot, pIndex);

				}
			}
			currAccuracy = calculateAccuracy(currRoot, validationDataList);
			if (currAccuracy > bestAccuracy) {
				bestRoot = currRoot;
				bestAccuracy = currAccuracy;
			}
			//System.out.println("NonleafCount" + findNonLeafCount(currRoot) + ":::" + findNonLeafCount(bestRoot)
				//	+ ": accuracy is " + currAccuracy + ": best accuracy " + bestAccuracy);
			//System.out.println(calculateAccuracy(bestRoot, validationDataList));
		}
		//System.out.println("Pruning over ");

		return bestRoot;

	}

	private TreeNode copy(TreeNode root) {

		if (root == null)
			return null;
		TreeNode newNode = new TreeNode();
		newNode.isLeaf = root.isLeaf;
		newNode.result = root.result;
		newNode.selectedAttr = root.selectedAttr;
		newNode.attrArr = root.attrArr;
		newNode.left = copy(root.left);
		newNode.right = copy(root.right);
		return newNode;
	}

	private static boolean pruneNodeRec(TreeNode temp, int pIndex) {
		if (temp != null) {
			if (temp.nodeNumber == pIndex) {
				int res = findCountRes(temp);
				temp.isLeaf = true;
				temp.left = null;
				temp.right = null;
				temp.result = res;
				return true;
			} else {
				boolean ret = pruneNodeRec(temp.left, pIndex);
				if (ret)
					return ret;
				else
					ret = pruneNodeRec(temp.right, pIndex);

			}
		}
		return false;
	}

	private static int findCountRes(TreeNode temp) {

		int arr[] = new int[2];
		if (temp == null)
			return -1;
		else {
			arr = findCount(temp);
		}

		if (arr[0] >= arr[1])
			return 1; // More class = 1 attributes are at P node

		return 0;
	}

	private static int[] findCount(TreeNode temp) {
		int res[] = new int[2];
		int left[] = new int[2];
		int right[] = new int[2];
		if (temp == null)
			return res;

		if (temp.isLeaf) {
			if (temp.result == 1)
				res[0] = 1;
			else
				res[1] = 1;
		}
		left = findCount(temp.left);
		right = findCount(temp.right);
		res[0] = res[0] + left[0] + right[0];
		res[1] = res[1] + left[1] + right[1];
		return res;
	}

	public static int assignNumbers(TreeNode currRoot, int currNumber, int nonLeafCnt) {

		if (currRoot == null || currNumber > nonLeafCnt)
			return currNumber;
		if (!currRoot.isLeaf) {
			currRoot.nodeNumber = currNumber;
			currNumber++;
			currNumber = assignNumbers(currRoot.left, currNumber, nonLeafCnt);
			currNumber = assignNumbers(currRoot.right, currNumber, nonLeafCnt);

		}

		return currNumber;
	}

	public static int findNonLeafCount(TreeNode currRoot) {
		if (currRoot == null)
			return 0;
		if (!currRoot.isLeaf) {
			return 1 + findNonLeafCount(currRoot.left) + findNonLeafCount(currRoot.right);
		}
		return findNonLeafCount(currRoot.left) + findNonLeafCount(currRoot.right);
	}

	public int getRandomNumberInRange(int min, int max) {

		Random rand = new Random();

		// nextInt excludes the top value so we have to add 1 to include the top
		// value
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

}
