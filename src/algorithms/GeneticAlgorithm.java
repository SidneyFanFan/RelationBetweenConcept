package algorithms;

/**
 * Genetic Algorithm
 * 
 * @author Sidney Fan
 *
 */
public class GeneticAlgorithm {

	private int geneLength;
	private int generationNum;

	public GeneticAlgorithm(int geneLength, int generationNum) {
		super();
		this.geneLength = geneLength;
		this.generationNum = generationNum;
	}

	/**
	 * Training the generations
	 * 
	 * @param trainingTime
	 *            -number of training generations
	 * @param fitnessAlgorithm
	 *            - fitness algorithm
	 * @return
	 */
	public int[] train(int trainingTime,
			FitnessAlgorithm<int[]> fitnessAlgorithm) {
		int[][] gene = new int[generationNum][geneLength];
		int[] bestGene = new int[geneLength];
		double[] range = new double[generationNum];
		// generate first generation
		for (int i = 0; i < generationNum; i++) {
			for (int j = 0; j < geneLength; j++) {
				gene[i][j] = (int) (Math.random() * 2);
			}
		}
		int fatherIndex, motherIndex, losserIndex;
		int[] fatherGene, motherGene;
		for (int i = 0; i < trainingTime; i++) {
			range = calcProbability(gene, fitnessAlgorithm);
			// choose any two
			fatherIndex = getRandomGeneIndex(gene, geneLength, range);
			motherIndex = getRandomGeneIndex(gene, geneLength, range);
			losserIndex = getLosserIndex(gene, range);
			fatherGene = gene[fatherIndex];
			motherGene = gene[motherIndex];
			// mate and replace
			gene[losserIndex] = geneCross(fatherGene, motherGene);
			// find winner and print
			bestGene = findBestGene(bestGene, gene, range, i, fitnessAlgorithm);
			System.out
					.printf("Current Best Gene: %s : fitness=%.4f\n",
							printGene(bestGene),
							fitnessAlgorithm.calcFitness(bestGene));
		}
		return bestGene;
	}

	private int[] findBestGene(int[] bestGene, int[][] gene, double[] range,
			int generation, FitnessAlgorithm<int[]> fitnessAlgorithm) {
		int winnerIndex = getWinnerGene(gene, range);
		int[] winner = gene[winnerIndex];
		System.out.printf("%dth Generation Best Gene: %s\n", generation + 1,
				printGene(winner));
		// compare winner with best
		double bestGeneFitness = fitnessAlgorithm.calcFitness(bestGene);
		double winnerFitness = fitnessAlgorithm.calcFitness(winner);
		if (bestGeneFitness < winnerFitness) {
			return winner;
		} else {
			return bestGene;
		}
	}

	private double[] calcProbability(int[][] gene,
			FitnessAlgorithm<int[]> fitnessAlgorithm) {
		int n = gene.length;
		double[] fitness = new double[n];
		double[] prob = new double[n];
		double fitnessSum = 0.0;
		double probPos = 0.0;
		// calc fitness
		for (int i = 0; i < n; i++) {
			fitness[i] = fitnessAlgorithm.calcFitness(gene[i]);
			fitnessSum += fitness[i];
		}
		// calc prob
		for (int i = 0; i < n; i++) {
			double probability = fitness[i] / fitnessSum;
			probPos += probability;
			prob[i] = probPos;
		}
		return prob;
	}

	private int getLosserIndex(int[][] gene, double[] range) {
		double left = 0, probability = 0.0;
		double min = 1.0;
		int minIndex = -1;
		for (int i = 0; i < range.length; i++) {
			probability = range[i] - left;
			if (min > probability) {
				min = probability;
				minIndex = i;
			}
			left = range[i];
		}
		return minIndex;
	}

	private int getWinnerGene(int[][] gene, double[] range) {
		double left = 0, probability = 0.0;
		double max = 0;
		int geneSum = -1;
		int maxIndex = -1;
		int maxGeneSum = -1;
		for (int i = 0; i < range.length; i++) {
			probability = range[i] - left;
			if (max < probability) {
				geneSum = 0;
				for (int j = 0; j < gene[i].length; j++) {
					geneSum += gene[i][j];
				}
				if (maxGeneSum < geneSum) {
					max = probability;
					maxIndex = i;
				}
			}
			left = range[i];
		}
		return maxIndex;
	}

	/**
	 * Cross mother gene to father gene
	 * 
	 * @param fatherGene
	 * @param motherGene
	 * @return
	 */
	private int[] geneCross(int[] fatherGene, int[] motherGene) {
		if (fatherGene.length != motherGene.length) {
			return null;
		} else {
			int geneLength = fatherGene.length;
			int cut = geneLength / 2;
			int[] offspring = new int[geneLength];
			// cross
			for (int i = 0; i < geneLength; i++) {
				// cross father
				offspring[i] = fatherGene[i];
			}
			for (int i = cut; i < geneLength; i++) {
				// cross mother
				offspring[i] = motherGene[i];
			}
			// mutation
			int m = (int) (Math.random() * geneLength);
			offspring[m] = 1 - offspring[m];
			// print
			// System.out.print("(");
			// for (int i = 0; i < geneLength; i++) {
			// System.out.print(fatherGene[i] + " ");
			// }
			// System.out.print(") X (");
			// for (int i = 0; i < geneLength; i++) {
			// System.out.print(motherGene[i] + " ");
			// }
			// System.out.print(") = (");
			// for (int i = 0; i < geneLength; i++) {
			// System.out.print(offspring[i] + " ");
			// }
			// System.out.println(")");
			return offspring;
		}
	}

	private int getRandomGeneIndex(int[][] gene, int geneLength, double[] range) {
		double r = Math.random();
		double left = 0.0;
		for (int i = 0; i < range.length; i++) {
			// System.out.println(left + "~" + prob[i] + "?" + r);
			if (left <= r && r < range[i]) {
				return i;
			}
			left = range[i];
		}
		return 0;
	}

	private String printGene(int[] gene) {
		String str = "";
		for (int j = 0; j < gene.length; j++) {
			str += String.format("%d ", gene[j]);
		}
		return str;
	}

}
