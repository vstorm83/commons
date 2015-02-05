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
import java.util.Map;

import org.apache.commons.lang.LocaleUtils;

public class ContextualSessionListener implements SessionListener {
  public static String EXO_CONTEXT = "exo.context";  
  
  @SuppressWarnings("unchecked")
  public void sessionAdded(Session session, Message message) {
    Map<String, Object> ext = message.getExt();
    
    if (ext != null && ext.get(EXO_CONTEXT) != null) {
      Map<String, String> context = (Map<String, String>)ext.get(EXO_CONTEXT);
      String sLocale = context.get(LocalizationExtension.LOCALE);
      
      if (sLocale != null) {
        Locale locale = LocaleUtils.toLocale(sLocale);
        session.setAttribute(LocalizationExtension.LOCALE, locale);
      }
    }
  }
  
  public void sessionRemoved(Session session, boolean timedout) {    
  }
}  
