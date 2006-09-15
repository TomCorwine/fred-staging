/*
  GlobalProbe.java / Freenet
  Copyright (C) 2005-2006 The Free Network project

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package freenet.node;

import freenet.support.Logger;

public class GlobalProbe implements Runnable {

	double lastLocation = 0.0;
	boolean doneSomething = false;
	final ProbeCallback cb;
	final Node node;
	int ctr;
	
	GlobalProbe(Node n) {
		this.node = n;
    	cb = new ProbeCallback() {
			public void onCompleted(String reason, double target, double best, double nearest, long id, short counter) {
				String msg = "Completed probe request: "+target+" -> "+best+"\r\nNearest actually hit "+nearest+", "+counter+" hops, id "+id+"\r\n";
				Logger.error(this, msg);
				synchronized(GlobalProbe.this) {
					doneSomething = true;
					lastLocation = best;
					GlobalProbe.this.notifyAll();
				}
			}
    	};
		
	}
	
	public void run() {
		synchronized(this) {
			lastLocation = 0.0;
			double prevLoc = lastLocation;
			while(true) {
				doneSomething = false;
	    		node.dispatcher.startProbe(lastLocation, cb);
	    		for(int i=0;i<20 && !doneSomething;i++) {
	    			try {
						wait(1000*10);
					} catch (InterruptedException e) {
						// Ignore
					}
	    			// All vars should be updated by resynchronizing here, right?
	    		}
	    		if(!doneSomething) {
	    			error("Stalled on "+lastLocation+" , waiting some more.");
	    			try {
						wait(100*1000);
					} catch (InterruptedException e) {
						// Ignore
					}
	    			if(!doneSomething) {
	    				error("Still no response to probe request, trying again.");
		    			continue;
	    			}
	    		}
	    		if(Math.abs(lastLocation-prevLoc) < (Double.MIN_VALUE*2)) {
	    			error("Location is same as previous ! Sleeping then trying again");
	    			try {
						wait(100*1000);
					} catch (InterruptedException e) {
						// Ignore
					}
	    			continue;
	    		}
	    		output(lastLocation);
	    		prevLoc = lastLocation;
	    		if(lastLocation > 1.5) break;
	    		ctr++;
	    		// Sleep 10 seconds so we don't flood
	    		try {
					wait(10*1000);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
		
	}

	private void output(double loc) {
		Logger.error(this, "LOCATION "+ctr+": " + loc);
		System.out.println("LOCATION "+ctr+": " + loc);
	}

	private void error(String string) {
		Logger.error(this, string);
		System.out.println("GlobalProbe error: "+string);
	}
	

}
