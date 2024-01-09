package main.java.de.fh.dortmund.chargeStation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

import main.java.de.fh.dortmund.model.EnergySource;
import main.java.de.fh.dortmund.model.Location;

public class ChargeReserveBatteries implements Callable<Integer> {

	private BufferedWriter fr1;
	private Location loc = null;

	public ChargeReserveBatteries(Location loc) {
		this.loc = loc;
	}

	public synchronized int chargeBatteries() {

		if (loc.getEnergySource() != null && !loc.getEnergySource().isEmpty()) {
			for (EnergySource src : loc.getEnergySource()) {
				printLogs("Location " + loc.getAreaName() + " reserved battery is being charged with energy source "
						+ src.getSourceName(), "ChargeReservedBatteries.log");
				printLogs("Location " + loc.getAreaName() + " available energy source -  " + src.getSourceName(),
						"EnergySource.log");
			}
			return loc.getEnergySource().size();
		} else {
			return 0;
		}
	}

	public synchronized void printLogs(String msg, String file) {
		  try {
	            fr1 = new BufferedWriter(new FileWriter(file, true));
	            fr1.write(msg);
	            fr1.newLine();
	        } catch (IOException e) {
	            System.out.println("Error while writing to log file " + e.getMessage());
	        } finally { //to ensure this resource is always closed, even in case of exceptions
	            try {
	                if (fr1 != null) {
	                    fr1.close();
	                }
	            } catch (IOException e) {
	                System.out.println("Error while closing log file " + e.getMessage());
	            }
	        }
	}

	@Override
	public Integer call() throws Exception {

		return chargeBatteries();
	}

}
