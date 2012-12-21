/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.commons;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.form.input.UIUploadInput;


/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 19, 2010  
 */
@ComponentConfig(
   template = "classpath:groovy/webui/commons/UIFormUploadInput.gtmpl", 
   events = {
    @EventConfig(listeners = UIUploadInput.CreateUploadIdActionListener.class),
    @EventConfig(listeners = UIUploadInput.RemoveUploadIdActionListener.class) })
public class UIFormUploadInput extends UIUploadInput {

  final static public String UPLOAD_ACTION = "UploadAttachment" ;
  
  private String postUploadActionLink_ = "";
  
  public UIFormUploadInput(String name, String bindingExpression) {
    super(name, bindingExpression);
    setComponentConfig(UIFormUploadInput.class, null);
  }
  
  public UIFormUploadInput(String name, String bindingExpression, int limit) {
    super(name, bindingExpression, limit);
    setComponentConfig(UIFormUploadInput.class, null);
  }
  
  public void setPostUploadActionLink(String value) {
    postUploadActionLink_ = value;
  }
  
  public String getPostUploadActionLink() throws Exception {
    return postUploadActionLink_;
  }
}
