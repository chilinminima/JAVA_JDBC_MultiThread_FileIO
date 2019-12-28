package Assignment3_java;

public class Animal {
	private String name;
	private int requiredAmount;
	private int feedingCount;
	private int hungryCount;
	
	public Animal(String name, int requiredAmount) {
		super();
		this.name = name;
		this.requiredAmount = requiredAmount;
		this.feedingCount = 0;
		this.hungryCount = 0;
	}

	public String getName() {
		return name;
	}

	public int getRequiredAmount() {
		return requiredAmount;
	}

	public int getFeedingCount() {
		return feedingCount;
	}

	public int getHungryCount() {
		return hungryCount;
	}

	public void setFeedingCount(int feedingCount) {
		this.feedingCount = feedingCount;
	}

	public void setHungryCount(int hungryCount) {
		this.hungryCount = hungryCount;
	}
	
	

}
