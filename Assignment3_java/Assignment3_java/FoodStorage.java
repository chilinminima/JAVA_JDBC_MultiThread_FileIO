package Assignment3_java;

import java.util.concurrent.locks.*;

public class FoodStorage {
	Lock lock = new ReentrantLock();
	Condition newDeposit = lock.newCondition();
	int balance = 0;
	
	
	public int getBalance() {
		return balance;
	}
	
	//add food into storage
	public void addFood(int amount) {
		lock.lock();
		balance += amount;
		System.out.printf("%-18s%-50s%10s\n", "Add " + amount + "kg", "", balance);
		newDeposit.signalAll();
		lock.unlock();
	}
	
	//feed animal and deduct from storage
	public void feedAnimal(Animal animal) {
		lock.lock();
		String name = animal.getName();
		int amount = animal.getRequiredAmount();
		try {
			//if there is no enough food to feed, wait 
			while(balance < amount) {
				System.out.printf("%-18s%-50s%10s\n", "", name + " got hungry. Wait for food...", "");
				animal.setHungryCount(animal.getHungryCount()+1);
				newDeposit.await();
			}
			balance -= amount;
			System.out.printf("%-18s%-50s%10s\n", "", name + " got hungry. Feed " + name + " "  +  amount + "kg", balance);
			animal.setHungryCount(animal.getHungryCount()+1);
			animal.setFeedingCount(animal.getFeedingCount()+1);
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
	}
}
