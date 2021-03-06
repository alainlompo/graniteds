/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.tide.server;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.ServerApp;
import org.granite.client.test.tide.server.remoting.RemotingApplication;
import org.granite.client.test.tide.server.remoting.TestService;
import org.granite.client.tide.Context;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.util.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestRemoting {

    private static final Logger log = Logger.getLogger(TestRemoting.class);

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}")
    public static Iterable<Object[]> data() {
        return ContainerTestUtil.data();
    }

    private ContentType contentType;
    protected static EmbeddedContainer container;

    private Context context = new SimpleContextManager().getContext();

    private static final ServerApp SERVER_APP_APP = new ServerApp("/remoting", false, "localhost", 8787);

    public TestRemoting(String containerClassName, ContentType contentType) {
        this.contentType = contentType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "remoting.war");
        war.addClass(RemotingApplication.class);
        war.addClass(TestService.class);
        war.addAsWebInfResource(new File("granite-client-java-advanced/src/test/resources/META-INF/services-config.properties"), "classes/META-INF/services-config.properties");

        container = ContainerTestUtil.newContainer(war, false);
        container.start();
        log.info("Container started");
    }

    @AfterClass
    public static void stopContainer() throws Exception {
        container.stop();
        container.destroy();
        log.info("Container stopped");
    }

    @Test
    public void testFailCall() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);

        Component testService = context.set("testService", new ComponentImpl(serverSession));
        final String[] message = new String[1];
        final CountDownLatch waitForCall = new CountDownLatch(1);
        testService.call("fail", new TideResponder<Void>() {
            @Override
            public void result(TideResultEvent<Void> event) {
                waitForCall.countDown();
            }

            @Override
            public void fault(TideFaultEvent event) {
                message[0] = event.getFault().getFaultDescription();
                waitForCall.countDown();
            }
        });
        waitForCall.await(10000, TimeUnit.MILLISECONDS);

        Assert.assertEquals("Fault message", "fail", message[0]);

        serverSession.stop();
    }
}
