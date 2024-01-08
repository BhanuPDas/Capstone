package main.java.de.fh.dortmund.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import main.java.de.fh.dortmund.chargeStation.ChargeReserveBatteries;
import main.java.de.fh.dortmund.chargeStation.ChargeStation;
import main.java.de.fh.dortmund.metadata.Metadata;
import main.java.de.fh.dortmund.model.Car;
import main.java.de.fh.dortmund.model.EnergySource;
import main.java.de.fh.dortmund.model.Location;
import main.java.de.fh.dortmund.users.Admin;
import main.java.de.fh.dortmund.users.User;

public class CarChargingStationMain {

	private static BufferedWriter fr1;
	private static Scanner scan = new Scanner(System.in);

	public static void main(String[] args) {
		// Archieve all log files
		Admin ad1 = new Admin();
		ad1.archieveLogFiles();
		BlockingQueue<User> userInput = new ArrayBlockingQueue<User>(10);
		List<Admin> adminList = new ArrayList<>();
		adminList = getAdminData();
		userInput = getUserData();
		ExecutorService executor = Executors.newCachedThreadPool();
		int adminCount = 0;
		while (adminCount < adminList.size()) {
			Runnable charge;
			charge = new ChargeStation(adminList.get(adminCount).getLocation(), userInput);
			executor.execute(charge);
			adminCount++;
		}
		adminCount = 0;
		while (adminCount < adminList.size()) {
			chargeAlternateBatteries(adminList.get(adminCount).getLocation());
			adminCount++;
		}

		System.out.println("Creating metadata file");

		Metadata meta = new Metadata();
		meta.writeMetatdata("AdminData.log");
		meta.writeMetatdata("ChargeStationMainlogs.log");
		meta.writeMetatdata("PriorityQueueLogs.log");
		meta.writeMetatdata("UserData.log");
		meta.writeMetatdata("LocationIsFull.log");
		meta.writeMetatdata("CalcWaitTimeForCars.log");
		meta.writeMetatdata("AdminActivity.log");

		// To delete any log file - pass "yes" to admin method
		System.out.println("Do you want to perform admin task");
		Admin admin = new Admin();
		admin.deleteFile("No", "AdminData.log");

		// To read any log file
		User us = new User();
		us.readLogFile("AdminData.log");

	}

	public static void printLogs(String msg, String fileName) {

		try {
			fr1 = new BufferedWriter(new FileWriter(fileName, true));
			fr1.write(msg);
			fr1.newLine();
			fr1.close();
		} catch (IOException e) {
			System.out.println("Error while writing to log file " + e.getMessage());
		}
	}

	public static List<Admin> getAdminData() {

		List<Admin> adminList = new ArrayList<>();
		System.out.println("---- Enter Admin data ----");
		while (true) {
			Admin admin = new Admin();
			List<Location> locList = new ArrayList<>();
			System.out.println("Enter Admin Name");
			String adminName = scan.nextLine();
			admin.setAdminName(adminName);
			System.out.println("Enter Admin ID (eg.001): ");
			String adminID = scan.nextLine();
			admin.setAdminID(adminID);
			while (true) {
				Location loc = new Location();
				List<EnergySource> energyList = new ArrayList<>();
				System.out.println("Enter Area Name (eg.Dortmund): ");
				String area = scan.nextLine();
				loc.setAreaName(area);
				System.out.println("Enter Zipcode: ");
				int zip = scan.nextInt();
				loc.setZipcode(zip);
				scan.nextLine();
				System.out.println("Enter Weather (eg. Sunny, Windy, etc.): ");
				String weather = scan.nextLine();
				loc.setWeather(weather);
				while (true) {
					EnergySource energy = new EnergySource();
					System.out.println("Enter Energy Source ID (eg. ES001): ");
					String sourceID = scan.nextLine();
					energy.setId(sourceID);
					System.out.println("Enter Energy Source Name (eg.Solar, Hydro, Wind): ");
					String sourceName = scan.nextLine();
					energy.setSourceName(sourceName);
					System.out.println("Enter Energy Capacity (eg.1000w): ");
					String capacity = scan.nextLine();
					energy.setCapacity(capacity);
					energyList.add(energy);
					System.out.println("Do you want to add more energy sources? (yes/no): ");
					String cont1 = scan.nextLine();
					if (cont1.equalsIgnoreCase("yes")) {
						continue;
					} else {
						break;
					}
				}
				loc.setEnergySource(energyList);
				locList.add(loc);
				System.out.println("Do you want to add more locations? (yes/no): ");
				String cont = scan.nextLine();
				if (cont.equalsIgnoreCase("yes")) {
					continue;
				} else {
					break;
				}
			}
			admin.setLocation(locList);
			adminList.add(admin);
			printLogs("Admin " + admin.getAdminName() + " data received", "AdminData.log");
			System.out.println("Do you want to add more admins? (yes/no): ");
			String canContinue = scan.nextLine();
			if (canContinue.equalsIgnoreCase("yes")) {
				continue;
			} else {
				break;
			}
		}
		return adminList;

	}

	public static BlockingQueue<User> getUserData() {
		BlockingQueue<User> userInput = new ArrayBlockingQueue<User>(10);
		while (true) {
			User user = new User();
			Car car = new Car();
			System.out.println("Enter Car Owner Name");
			String carOwner = scan.nextLine();
			car.setOwnerName(carOwner);
			System.out.println("Enter Car Number");
			String carNumber = scan.nextLine();
			car.setNumber(carNumber);
			System.out.println("Enter Slot Book Date & Time in 24 Hours format (dd-mm-yyyy HH:mm (24 hours)): ");
			String bookTime = scan.nextLine();
			car.setBookedTimeSlot(LocalDateTime.parse(bookTime, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
			System.out.println("Enter Battery Level % (eg.10): ");
			int battery = scan.nextInt();
			car.setBatteryLevel(battery);
			scan.nextLine();
			System.out.println("Enter Car Brand");
			String carBrand = scan.nextLine();
			car.setBrand(carBrand);
			user.setCar(car);
			try {
				userInput.put(user);
			} catch (InterruptedException e) {
				printLogs(e.getMessage(), "CarChargingStationMain.log");
			}
			printLogs("User data received for " + user.getCar().getOwnerName(), "UserData.log");
			
			String data = String.format("%nCar Number: %s%nBattery level: %s%nBooked time slot: %s%nApprox. finish time: %s%n", 
					user.getCar().getNumber(), 
					user.getCar().getBatteryLevel(), 
					user.getCar().getBookedTimeSlot(), 
					user.getCar().getApproximateTimeToGetCharged());
			printLogs(data, "UserData.log");
			
			System.out.println("Do you want to add more users? (yes/no): ");
			String addUsers = scan.nextLine();
			if (addUsers.equalsIgnoreCase("yes")) {
				continue;
			} else {
				break;
			}
		}
		scan.close();
		return userInput;
	}

	public static void chargeAlternateBatteries(List<Location> locationList) {
		for (Location loc : locationList) {
			ChargeReserveBatteries chargeBatteries = new ChargeReserveBatteries(loc);
			FutureTask<Integer> future = new FutureTask<Integer>(chargeBatteries);
			Thread th1 = new Thread(future);
			try {
				th1.start();
				if (future.get() == 1) {
					printLogs("Reserved battery is charged now.", "ChargeReservedBatteries.log");
				}
			} catch (Exception e) {
				printLogs("System got interupted in between", "ChargeStation.log");
			}
			future = null;
			chargeBatteries = null;
		}
	}

}
