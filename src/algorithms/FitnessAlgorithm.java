package algorithms;

/**
 * Algorithm interface for calculate entity's fitness
 * 
 * @author Sidney Fan
 *
 * @param <T>
 *            - type of import
 */
public interface FitnessAlgorithm<T> {

	/**
	 * Calculate fitness of entity
	 * 
	 * @param entity
	 * @return
	 */
	public double calcFitness(T entity);
}
