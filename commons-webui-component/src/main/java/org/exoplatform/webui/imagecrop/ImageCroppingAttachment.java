package org.exoplatform.webui.imagecrop;
/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : An Bao NGUYEN
 *          annb@exoplatform.com
 * Oct 31, 2012  
 */
public class ImageCroppingAttachment {

  private static final Log LOG = ExoLogger.getLogger(ImageCroppingAttachment.class);

  /**
   * The id.
   */
  private String id;

  /**
   * The file name.
   */
  private String fileName;

  /**
   * The mime type.
   */
  private String mimeType;

  /**
   * The workspace.
   */
  private String workspace;

  /**
   * The image bytes.
   */
  private byte[] imageBytes;

  /**
   * The last modified.
   */
  private long lastModified;

  public ImageCroppingAttachment() {
  }

  /**
   * Constructor.
   *
   * @param id
   * @param fileName
   * @param mimeType
   * @param inputStream
   * @param workspace
   * @param lastModified
   * @throws Exception
   */
  public ImageCroppingAttachment(String id,
                          String fileName,
                          String mimeType,
                          InputStream inputStream,
                          String workspace,
                          long lastModified) throws Exception {
    this.id = id;
    this.fileName = fileName;
    setInputStream(inputStream);
    this.mimeType = mimeType;
    this.workspace = workspace;
    this.lastModified = lastModified;
  }

  /**
   * Gets the data path by specifying a PortalContainer instance
   *
   * @return the data path
   * @throws Exception the exception
   */
  public String getDataPath(PortalContainer portalContainer) throws Exception {
    Node attachmentData;
    try {
      attachmentData = (Node) getCurrentSession().getItem(getId());
    } catch (ItemNotFoundException e) {
      LOG.warn("Failed to get data path", e);
      return null;
    }
    return attachmentData.getPath();
  }

  /**
   * Gets the data path.
   *
   * @return the data path
   * @throws Exception the exception
   */
  public String getDataPath() throws Exception {
    Node attachmentData;
    try {
      attachmentData = (Node) getCurrentSession().getItem(getId());
    } catch (ItemNotFoundException e) {
      LOG.warn("Failed to get data path", e);
      return null;
    }
    return attachmentData.getPath();
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param s the new id
   */
  public void setId(String s) {
    id = s;
  }

  /**
   * Gets the workspace.
   *
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * Sets the workspace.
   *
   * @param ws the new workspace
   */
  public void setWorkspace(String ws) {
    workspace = ws;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param s the new file name
   */
  public void setFileName(String s) {
    fileName = s;
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   *
   * @param s the new mime type
   */
  public void setMimeType(String s) {
    mimeType = s;
  }

  /**
   * Gets the last modified.
   *
   * @return the last modified
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the new last modified
   */
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Gets images size in MB/ KB/ Bytes.
   *
   * @return image size string
   */
  public String getSize() {
    int KB_SIZE = 1024;
    int MB_SIZE = 1024 * KB_SIZE;
    int length = imageBytes.length;
    double size;
    if (length >= MB_SIZE) {
      size = length / MB_SIZE;
      return size + " MB";
    } else if (length >= KB_SIZE) {
      size = length / KB_SIZE;
      return size + " KB";
    } else { //Bytes size
      return length + " Bytes";
    }
  }

  /**
   * Gets imageBytes.
   *
   * @return
   */
  public byte[] getImageBytes() {
    return imageBytes;
  }

  public void setImageBytes(byte[] imageBytes) {
    this.imageBytes = imageBytes;
  }
  
  /**
   * Sets the input stream.
   *
   * @param input the new input stream
   * @throws Exception the exception
   */
  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
    } else {
      imageBytes = null;
    }
  }



  /**
   * Gets the session from a portal container.
   *
   * @return the session
   * @throws Exception the exception
   */
 public Session getCurrentSession() throws Exception {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(RepositoryService.class);
    String defaultWorkspace = repoService.getCurrentRepository()
                                         .getConfiguration()
                                         .getDefaultWorkspaceName();
    return repoService.getDefaultRepository().getSystemSession(defaultWorkspace);
  }
  
  private String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PortalContainerInfo containerInfo = (PortalContainerInfo) container.getComponentInstanceOfType(PortalContainerInfo.class);
    return containerInfo.getContainerName();
  }
  
  public String getFileURL(String path) throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (!(context instanceof PortalRequestContext)) {
      context = (WebuiRequestContext) context.getParentAppRequestContext();
    }
    String portalName = getPortalName();
    String requestURL = ((PortalRequestContext) context).getRequest().getRequestURL().toString();
    String domainURL = requestURL.substring(0, requestURL.indexOf(portalName));
    Session session = getCurrentSession();
    String workspace = session.getWorkspace().getName();
    String repository = ((ManageableRepository) session.getRepository()).getConfiguration()
                                                                        .getName();

    String url = domainURL + portalName + "/" + PortalContainer.getCurrentRestContextName()
        + "/jcr/" + repository + "/" + workspace + path;
    return url;
  }
  

}