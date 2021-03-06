/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.gravity.tomcat;

import flex.messaging.messages.Message;
import org.apache.catalina.CometEvent;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AbstractGravityServlet;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.GravityInternal;
import org.granite.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Franck WOLFF
 */
public class TomcatChannel extends AbstractChannel {

    private static final Logger log = Logger.getLogger(TomcatChannel.class);

    private final AtomicReference<CometEvent> event = new AtomicReference<CometEvent>();

    
    public TomcatChannel(GravityInternal gravity, String id, TomcatChannelFactory factory, String clientType) {
        super(gravity, id, factory, clientType);
    }
    
    public void setCometEvent(CometEvent event) {
        if (log.isDebugEnabled())
            log.debug("Channel: %s got new event: %s", getId(), EventUtil.toString(event));
        
        // Set this channel's event.
        CometEvent previousEvent = this.event.getAndSet(event);

        // Normally, we should have only two cases here:
        //
        // 1) this.event == null && event != null -> new (re)connect message.
        // 2) this.event != null && event == null -> timeout.
        //
        // Not sure about what should be done if this.event != null && event != null, so
        // warn about this case and close this.event if it is not the same as the event
        // parameter.
        if (previousEvent != null) {
        	if (event != null) {
        		log.warn(
        			"Got a new non null event %s while current event %s isn't null",
        			EventUtil.toString(event), EventUtil.toString(this.event.get())
        		);
        	}
        	if (previousEvent != event) {
	        	try {
	        		previousEvent.close();
	        	}
	        	catch (Exception e) {
	        		log.debug(e, "Error while closing event");
	        	}
        	}
        }
        
        // Try to queue receiver if the new event isn't null.
        if (event != null)
        	queueReceiver();
    }
    
    @Override
	protected boolean hasAsyncHttpContext() {
    	return event.get() != null;
	}

	@Override
	protected AsyncHttpContext acquireAsyncHttpContext() {

		CometEvent event = this.event.getAndSet(null);
		if (event == null)
			return null;

    	AsyncHttpContext context = null;
    	
    	try {
	    	HttpServletRequest request = null;
	        HttpServletResponse response = null;
	        try {
	        	request = event.getHttpServletRequest();
	        	response = event.getHttpServletResponse();
	        } catch (Exception e) {
	        	log.warn(e, "Illegal event: %s", EventUtil.toString(event));
	        	return null;
	        }
	        if (request == null || response == null) {
	        	log.warn("Illegal event (request or response is null): %s", EventUtil.toString(event));
	        	return null;
	        }
	
	        Message requestMessage = AbstractGravityServlet.getConnectMessage(request);
	        if (requestMessage == null) {
	        	log.warn("No request message while running channel: %s", getId());
	        	return null;
	        }
			
	        context = new AsyncHttpContext(request, response, requestMessage, event);
    	}
    	finally {
    		if (context == null) {
    			try {
    				event.close();
    			}
    			catch (Exception e) {
    				log.debug(e, "Error while closing event: %s", EventUtil.toString(event));
    			}
    		}
    	}
    	
    	return context;
	}

	@Override
	protected void releaseAsyncHttpContext(AsyncHttpContext context) {
		try {
			if (context != null && context.getObject() != null)
				((CometEvent)context.getObject()).close();
		}
		catch (Exception e) {
			log.warn(e, "Could not release event for channel: %s", this);
		}
	}

	@Override
	public void destroy(boolean timeout) {
		try {
			super.destroy(timeout);
		}
		finally {
			close(timeout);
		}
	}
	
	public void close(boolean timeout) {
		CometEvent event = this.event.getAndSet(null);
		if (event != null) {
			try {
				event.close();
			}
			catch (Exception e) {
				log.debug(e, "Could not close event: %s for channel: %s", EventUtil.toString(event), this);
			}
		}
	}
}
