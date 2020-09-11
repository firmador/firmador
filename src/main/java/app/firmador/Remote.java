/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2020 Firmador authors.

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

package app.firmador;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class Remote extends SwingWorker<Void, Void> {
    private HttpServer server;
    public Remote(String origin) throws IOException, InterruptedException {
        HttpRequestHandler requestHandler = new HttpRequestHandler() {
            public void handle(HttpRequest request, HttpResponse response,
                HttpContext context) throws HttpException, IOException {
                System.out.println(request.toString());
                response.setStatusCode(HttpStatus.SC_OK);
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Vary", "Origin");
                // FIXME return signed document
                response.setEntity(new StringEntity("Prueba\n",
                    ContentType.TEXT_PLAIN));
            }
        };
        server = ServerBootstrap.bootstrap()
            .setListenerPort(3516)
            .setLocalAddress(InetAddress.getLoopbackAddress())
            .registerHandler("*", requestHandler)
            .create();
        server.start();
    }
    protected Void doInBackground() throws InterruptedException {
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        // FIXME call publish() on POST request
        return null;
    }
}
