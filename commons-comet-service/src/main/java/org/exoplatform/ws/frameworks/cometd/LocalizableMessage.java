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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class LocalizableMessage {
  
  private String       data;

  private String       localized;

  private Set<String> bundleKeys = new HashSet<String>();
  
  private ResourceBundleResolver resolver;  
  
  public static final  String PREFIX = "${";
  
  public static final  String SUFFIX = "}";

  public LocalizableMessage(String data, ResourceBundleResolver resolver) {
    if (data == null || resolver == null) {
      throw new IllegalArgumentException("data and resolver can't be null");
    }
    
    this.data = data;
    this.localized = data;
    this.resolver = resolver;
    
    int idx = data.indexOf(PREFIX);
    while (idx != -1) {
      int next = data.indexOf(SUFFIX, idx);
      if (next > idx + 2) {
        bundleKeys.add(data.substring(idx + 2, next));        
      }
      idx = data.indexOf(PREFIX, next);
    }
  }

  public void localize(Locale locale) {
    Map<String, String> resolved = new HashMap<String, String>();

    ResourceBundle bundle = resolver.resolve(locale);
    if (bundle != null) {
      for (String k : bundleKeys) {
        try {
          String label = bundle.getString(k);
          resolved.put(k, label);
          break;
        } catch (Exception e) {
        }
      }      
    }

    StringBuilder tmp = new StringBuilder(data);

    for (String k : resolved.keySet()) {
      String label = resolved.get(k);
      if (label != null) {
        int idx = tmp.indexOf(PREFIX + k + SUFFIX);
        while (idx != -1) {
          tmp.replace(idx, k.length() + 3, label);
          idx = tmp.indexOf(PREFIX + k + SUFFIX, idx + label.length());
        }
      }
    }
    localized = tmp.toString();
  }

  @Override
  public String toString() {
    return localized;
  }

  public static interface ResourceBundleResolver {
    ResourceBundle resolve(Locale locale);
  }
}
