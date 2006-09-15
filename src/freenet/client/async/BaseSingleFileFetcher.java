/*
  BaseSingleFileFetcher.java / Freenet
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

package freenet.client.async;

import freenet.client.FetcherContext;
import freenet.keys.ClientCHK;
import freenet.keys.ClientKey;
import freenet.keys.ClientKeyBlock;
import freenet.keys.ClientSSK;
import freenet.node.LowLevelGetException;
import freenet.node.NodeClientCore;
import freenet.node.SendableGet;
import freenet.support.Logger;

public abstract class BaseSingleFileFetcher implements SendableGet {

	final ClientKey key;
	protected boolean cancelled;
	final int maxRetries;
	private int retryCount;
	final FetcherContext ctx;
	final BaseClientGetter parent;

	BaseSingleFileFetcher(ClientKey key, int maxRetries, FetcherContext ctx, BaseClientGetter parent) {
		retryCount = 0;
		this.maxRetries = maxRetries;
		this.key = key;
		this.ctx = ctx;
		this.parent = parent;
	}
	
	public ClientKey getKey() {
		return key;
	}

	/** Do the request, blocking. Called by RequestStarter. */
	public void send(NodeClientCore core) {
		synchronized (this) {
			if(cancelled) {
				onFailure(new LowLevelGetException(LowLevelGetException.CANCELLED));
				return;
			}	
		}
		// Do we need to support the last 3?
		ClientKeyBlock block;
		try {
			block = core.realGetKey(key, ctx.localRequestOnly, ctx.cacheLocalRequests, ctx.ignoreStore);
		} catch (LowLevelGetException e) {
			onFailure(e);
			return;
		} catch (Throwable t) {
			Logger.error(this, "Caught "+t, t);
			onFailure(new LowLevelGetException(LowLevelGetException.INTERNAL_ERROR));
			return;
		}
		onSuccess(block, false);
	}

	/** Try again - returns true if we can retry */
	protected boolean retry() {
		retryCount++;
		if(Logger.shouldLog(Logger.MINOR, this))
			Logger.minor(this, "Attempting to retry... (max "+maxRetries+", current "+retryCount+")");
		// We want 0, 1, ... maxRetries i.e. maxRetries+1 attempts (maxRetries=0 => try once)
		if((retryCount <= maxRetries) || (maxRetries == -1)) {
			schedule();
			return true;
		}
		return false;
	}

	public void schedule() {
		if(Logger.shouldLog(Logger.MINOR, this))
			Logger.minor(this, "Scheduling "+this+" for "+key);
		if(key instanceof ClientCHK)
			parent.chkScheduler.register(this);
		else if(key instanceof ClientSSK)
			parent.sskScheduler.register(this);
		else
			throw new IllegalStateException(String.valueOf(key));
	}

	public int getRetryCount() {
		return retryCount;
	}

	public ClientRequester getClientRequest() {
		return parent;
	}

	public short getPriorityClass() {
		return parent.getPriorityClass();
	}

	public boolean ignoreStore() {
		return ctx.ignoreStore;
	}

	public synchronized void cancel() {
		cancelled = true;
	}

	public synchronized boolean isFinished() {
		return cancelled;
	}
	
	public Object getClient() {
		return parent.getClient();
	}

	public boolean dontCache() {
		return !ctx.cacheLocalRequests;
	}
	

}
