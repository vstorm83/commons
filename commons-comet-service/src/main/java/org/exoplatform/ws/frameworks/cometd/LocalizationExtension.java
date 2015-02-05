/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
  
package org.exoplatform.ws.frameworks.cometd;

import java.util.Locale;

public class LocalizationExtension implements SessionExtension {
  public static String LOCALE = "locale";
  
  public Message send(Session session, Message message) {    
    Locale locale = (Locale)session.getAttribute(LOCALE);
    if (locale != null) {
      Object data = message.getData();
      if (data instanceof LocalizableMessage) {
        ((LocalizableMessage)data).localize(locale);
      }
    }
    return message;
  }

}
