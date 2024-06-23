/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.libre.firmador.remote;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import jakarta.mail.MessagingException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

//import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
//import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;
import cr.libre.firmador.signers.FirmadorUtils;
import eu.europa.esig.dss.model.DSSDocument;

public class RemoteHttpWorker<T, V> extends SwingWorker<T, V> {

    protected GUIInterface gui;
    private HttpServer server;
    private String requestFileName;

    public RemoteHttpWorker(GUIInterface gui) {
        super();
        this.gui=gui;

    }

    protected T doInBackground() throws IOException, InterruptedException {

        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        server = ServerBootstrap.bootstrap().setListenerPort(settings.portNumber).setLocalAddress(InetAddress.getLoopbackAddress()).register("*",
                new RequestHandler(gui, settings)).create();
        server.start();
        server.awaitTermination(TimeValue.MAX_VALUE);
        return null;
    }
}
