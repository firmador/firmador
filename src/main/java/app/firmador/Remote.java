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
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class Remote {
    public Remote(String origin) throws IOException, InterruptedException {
        HttpRequestHandler requestHandler = new HttpRequestHandler() {
            public void handle(HttpRequest request, HttpResponse response,
                HttpContext context) throws HttpException, IOException {
                response.setStatusCode(HttpStatus.SC_OK);
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Vary", "Origin");
                // FIXME return signed document
                response.setEntity(new StringEntity("Prueba",
                    ContentType.TEXT_PLAIN));
            }
        };
        HttpProcessor httpProcessor = HttpProcessorBuilder.create()
            .add(new ResponseDate())
            .add(new ResponseServer("Firmador"))
            .add(new ResponseContent())
            .add(new ResponseConnControl())
            .build();
        SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(15000)
            .setTcpNoDelay(true)
            .build();
        HttpServer server = ServerBootstrap.bootstrap()
            .setListenerPort(3516)
            .setLocalAddress(InetAddress.getLoopbackAddress())
            .setHttpProcessor(httpProcessor)
            .setSocketConfig(socketConfig)
            .setExceptionLogger(new StdErrorExceptionLogger())
            .registerHandler("*", requestHandler)
            .create();
        server.start();
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
}

class StdErrorExceptionLogger implements ExceptionLogger {
    @Override
    public void log(Exception ex) {
        if (ex instanceof SocketTimeoutException) {
            System.err.println("Connection timed out");
        } else if (ex instanceof ConnectionClosedException) {
            System.err.println(ex.getMessage());
        } else {
            ex.printStackTrace();
        }
    }
}
