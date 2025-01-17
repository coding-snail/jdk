/*
 * Copyright (c) 2017, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.net.SocketTimeoutException;
import java.util.Map;

public class JdpJmxRemoteDynamicPortTestCase extends JdpTestCase {

    private int receivedJDPpackets = 0;

    public JdpJmxRemoteDynamicPortTestCase(ClientConnection connection) {
        super(connection);
    }

    @Override
    protected String initialLogMessage() {
        return "Starting test case JdpJmxRemoteDynamicPortTestCase";
    }

    /**
     * This method is executed after a correct Jdp packet (coming from this VM) has been received.
     *
     * @param payload A dictionary containing the Jdp packet data.
     */
    protected void packetFromThisVMReceived(Map<String, String> payload) throws Exception {
        receivedJDPpackets++;
        final String jmxServiceurl = payload.get("JMX_SERVICE_URL");
        int lastcolon = jmxServiceurl.lastIndexOf(':');
        int nextslash = jmxServiceurl.indexOf('/', lastcolon);
        int jmxRemotePort = Integer.parseInt(jmxServiceurl, lastcolon + 1, nextslash, 10);

        log.fine("Received #" + String.valueOf(receivedJDPpackets) +
                  ", jmxStringUrl=" + jmxServiceurl + ", jmxRemotePort=" + jmxRemotePort);

        if (0 == jmxRemotePort) {
           throw new Exception("JmxRemotePort value is zero. Test case failed.");
        }

        log.fine("Test case passed");
    }

    /**
     * The socket should not timeout.
     * It is set to wait for 10 times the defined pause between Jdp packet. See JdpOnTestCase.TIME_OUT_FACTOR.
     */
    @Override
    protected void onSocketTimeout(SocketTimeoutException e) throws Exception {
        String message = "Timed out waiting for JDP packet. Should arrive within " +
                connection.pauseInSeconds + " seconds, but waited for " +
                timeOut + " seconds.";
        log.severe(message);
        throw new Exception(message, e);
    }


    /**
     * After receiving one Jdp packets the test should end.
     */
    @Override
    protected boolean shouldContinue() {
        return receivedJDPpackets < 1;
    }

    /**
     * To run this test manually you might need the following VM options:
     * <p/>
     * -Dcom.sun.management.jmxremote.authenticate=false
     * -Dcom.sun.management.jmxremote.ssl=false
     * -Dcom.sun.management.jmxremote.port=0
     * -Dcom.sun.management.jmxremote=true
     * -Dcom.sun.management.jmxremote.autodiscovery=true
     * -Dcom.sun.management.jdp.pause=1
     * -Dcom.sun.management.jdp.name=alex  (or some other string to identify this VM)
     * <p/>
     * Recommended for nice output:
     * -Djava.util.logging.SimpleFormatter.format="%1$tF %1$tT %4$-7s %5$s %n"
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JdpTestCase client = new JdpJmxRemoteDynamicPortTestCase(new ClientConnection());
        client.run();
    }

}
