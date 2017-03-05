package net.creativeparkour;

/**
 * A map rating.
 * @author Obelus
 */
class Vote
{
	private int difficulty;
	private int quality;
	
	Vote (int difficulty, int quality)
	{
		this.difficulty = difficulty;
		this.quality = quality;
	}

	/**
	 * @return the difficulty
	 */
	int getDifficulty() {
		return difficulty;
	}

	/**
	 * @param difficulty the difficulty to set
	 */
	void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * @return the quality
	 */
	int getQuality() {
		return quality;
	}

	/**
	 * @param quality the quality to set
	 */
	void setQuality(int quality) {
		this.quality = quality;
	}

	String toConfigString() {
		return difficulty + "," + quality;
	}
}
