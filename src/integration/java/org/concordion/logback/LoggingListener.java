package org.concordion.logback;

import java.util.Iterator;

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Extension wishing to listen to the log events must extends this class.
 * 
 * @author Andrew Sumner
 */
public abstract class LoggingListener extends AppenderBase<ILoggingEvent> {

	public abstract String[] getFilterMarkers();

	public abstract Marker getThrowableCaughtMarker();

	public abstract Marker getFailureReportedMarker();
	
	protected Marker findMarker(Marker reference, String name) {
		if (reference == null) {
			return null;
		}

		if (reference.getName().equals(name)) {
			return reference;
		}
		
		Iterator<Marker> references = reference.iterator();
		while (references.hasNext()) {
			Marker found = findMarker(references.next(), name);
			
			if (found != null) {
				return found;
			}
		}
		
		return null;
	}
	
	protected boolean containsMarker(Marker reference, String name) {
		if (reference == null) {
			return false;
		}

		return reference.contains(name);
	}
}