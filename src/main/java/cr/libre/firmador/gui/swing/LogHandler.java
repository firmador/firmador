/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

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

package cr.libre.firmador.gui.swing;

import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//import org.slf4j.impl.JCLLoggerAdapter;

public class LogHandler extends Handler {

      private LoggingFrame writter = null;

      //the singleton instance
      private static LogHandler handler = null;

      private LogHandler() {
        configure();

      }
      public void setWritter(LoggingFrame writter) {
          this.writter = writter;
      }

      /**
       * The getInstance method returns the singleton instance of the
       * WindowHandler object It is synchronized to prevent two threads trying to
       * create an instance simultaneously. @ return WindowHandler object
       */

      public static synchronized LogHandler getInstance() {

        if (handler == null) {
          handler = new LogHandler();
        }
        return handler;
      }


      private void configure() {

        setLevel(Level.ALL);
        setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                return true;
            }
        });
        setFormatter(new SimpleFormatter());

      }


      public synchronized void publish(LogRecord record) {
        String message = null;
        if (!isLoggable(record))
          return;
        try {
          message = getFormatter().format(record);
        } catch (Exception e) {
          reportError(null, e, ErrorManager.FORMAT_FAILURE);
        }

        try {
          writter.showInfo(message);

        } catch (Exception ex) {
          reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }

      }

      public void close() {
      }

      public void flush() {
      }
      public void register() {
        Logger rootlog = Logger.getGlobal();
        rootlog.addHandler(this);
        Logger.getLogger("").addHandler(this);

      }
    }
