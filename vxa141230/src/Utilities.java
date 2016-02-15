import java.util.Random;


public class Utilities {

	static int BASE = 2;

	public static void main(String[] args) {
		Utilities util = new Utilities();
		System.out.println(util.calculateVarienceImpurity(13,5));

	}

	public static double calculateEntropy(int pos, int neg) {
		int sum = pos + neg;
		if (sum == 0)
			return 0;
		double p_plus = (double) pos / sum;
		double p_neg = (double) neg / sum;
		double res = ((p_plus) * log(p_plus) + (p_neg) * log(p_neg));
		if (res == 0.0) {
			return res;
		}

		double entrophy = -1 * res;
		return entrophy;
	}
	
	public static double calculateVarienceImpurity(int pos,int neg)
	{
		double varImpty = 0.0;
		int sum = pos + neg;
		if(sum==0)
			return varImpty;
		double p_plus = (double) pos / sum;
		double p_neg = (double) neg / sum;
		varImpty = p_plus * p_neg ;  
		return varImpty;
		
	}

	public static double log(double x) {
		if (x == 0.0)
			return 0.0;

		return (Math.log(x) / Math.log(BASE));
	}
	
	

}
