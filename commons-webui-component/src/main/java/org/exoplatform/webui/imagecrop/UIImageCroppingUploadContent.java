/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.webui.imagecrop;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.commons.UIDocumentSelector;
import org.exoplatform.webui.commons.UISaveAttachment;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : An Bao NGUYEN
 *          annb@exoplatform.com
 * Oct 31, 2012  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:groovy/webui/commons/imagecrop/UIImageCroppingUploadContent.gtmpl",
  events = {
    @EventConfig(listeners = UIImageCroppingUploadContent.CropActionListener.class),
    @EventConfig(listeners = UIImageCroppingUploadContent.CancelActionListener.class)
  }
)
public class UIImageCroppingUploadContent extends UIForm {
  

  private static final Log LOG = ExoLogger.getLogger(UIImageCroppingUploadContent.class);
  private static final String CROPPED_INFO = "croppedInfo";
  private static final String X = "X";
  private static final String Y = "Y";
  private static final String WIDTH = "WIDTH";
  private static final String HEIGHT = "HEIGHT";
  
  private String                filePath           = "";
  /** AvatarAttachment instance. */
  private ImageCroppingAttachment imageAttachment;

  /** Stores information of image storage. */
  private String imageSource;

  private InputStream originImg;

  private byte[] resizedImgInBytes;
  
  private Map<String, String> croppedInfo;
  
  /**
   * Default constructor.<br>
   */
  public UIImageCroppingUploadContent() {
    UIFormStringInput input = new UIFormStringInput(CROPPED_INFO, CROPPED_INFO, "0");
    input.setId(CROPPED_INFO);
    addUIFormInput(input);
  }

  /**
   * Initializes object at the first run time.<br>
   * @param ImageCroppingAttachment
   *        Information about attachment.
   * @throws Exception
   */
  public UIImageCroppingUploadContent(ImageCroppingAttachment avatarAttachment) throws Exception {
    this.imageAttachment = avatarAttachment;
    setImageSource(avatarAttachment.getImageBytes());
  }

  /**
   * Gets information of AvatarAttachment.<br>
   * @return AvatarAttachment
   */
  public ImageCroppingAttachment getAvatarAttachment() {
    return imageAttachment;
  }

  /**
   * Sets information of AvatarAttachment.<br>
   * @param ImageCroppingAttachment
   * @throws Exception
   */
  public void setAvatarAttachment(ImageCroppingAttachment avatarAttachment) throws Exception {
    this.imageAttachment = avatarAttachment;
    setImageSource(avatarAttachment.getImageBytes());
  }  
  
 @Override
 public void processRender(WebuiRequestContext context) throws Exception {
   super.processRender(context);
}    

  public String getImageSource() {
    return imageSource;
  }

  public InputStream getOriginImg() {
    return originImg;
  }

  public void setOriginImg(InputStream originImg) {
    this.originImg = originImg;
  }

  public Map<String, String> getCroppedInfo() {
    return croppedInfo;
  }

  public void setCroppedInfo(Map<String, String> croppedInfo) {
    this.croppedInfo = croppedInfo;
  }

  public byte[] getResizedImgInBytes() {
    return resizedImgInBytes;
  }

  public void setResizedImgInBytes(byte[] resizedImgInBytes) {
    this.resizedImgInBytes = resizedImgInBytes;
  }
  
  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Crop image, make preview image.
   */
  public static class CropActionListener extends EventListener<UIImageCroppingUploadContent> {
    @Override
    public void execute(Event<UIImageCroppingUploadContent> event) throws Exception {
      UIImageCroppingUploadContent uiUploadContent = event.getSource();
      ImageCroppingAttachment att = uiUploadContent.imageAttachment;      
      String croppedInfoVal =  ((UIFormStringInput)uiUploadContent.getChildById(CROPPED_INFO)).getValue();
      Map<String, String> croppedInfos = getCroppedInfoValues(croppedInfoVal);

      // set cropping information
      uiUploadContent.setCroppedInfo(croppedInfos);

      // get cropped information
      int x = (int)Double.parseDouble(croppedInfos.get(X));
      int y = (int)Double.parseDouble(croppedInfos.get(Y));
      int w = (int)Double.parseDouble(croppedInfos.get(WIDTH));
      int h = (int)Double.parseDouble(croppedInfos.get(HEIGHT));
      
      InputStream in = new ByteArrayInputStream(att.getImageBytes());
      uiUploadContent.setResizedImgInBytes(att.getImageBytes());      
      BufferedImage image = ImageIO.read(in); 
      image = image.getSubimage(x, y, w, h);      
      
      // create and re-store attachment info
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      String extension = mimeTypeResolver.getExtension(att.getMimeType());
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(image, extension, os);
      os.flush();
      byte[] imageInByte = os.toByteArray();
      os.close();
      att.setImageBytes(imageInByte);
      InputStream input = new ByteArrayInputStream(imageInByte);
      att.setInputStream(input);    
      uiUploadContent.setAvatarAttachment(att);      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadContent); 
      
      
      //save image cropping
      saveImageCropping(uiUploadContent, event);
      UIPopupWindow uiPopup = uiUploadContent.getParent();
      uiPopup.setShow(false);
//      Utils.updateWorkingWorkSpace(); 
    }

    
    private void saveImageCropping(UIImageCroppingUploadContent uiAvatarUploadContent, Event<UIImageCroppingUploadContent> event) throws Exception {
      LOG.info("CropActionListener --- saveImageCropping");
      UIComponent parent =uiAvatarUploadContent.getParent();
      while (parent != null) {
         /*if (UISpaceInfo.class.isInstance(parent)) {
           UISpaceInfo uiSpaceInfo = ((UISpaceInfo)parent);
           SpaceService spaceService = uiSpaceInfo.getSpaceService();
           String id = uiSpaceInfo.getUIStringInput("id").getValue();
           Space space = spaceService.getSpaceById(id);
           if (space != null) {
             uiSpaceInfo.saveAvatar(uiAvatarUploadContent, space);
             return;
           }
         }*/
         parent = parent.getParent();
      }
      
      // Save user avatar
      uiAvatarUploadContent.saveUserAvatar(uiAvatarUploadContent, event);
      return;
    }
    
    
    private Map<String, String> getCroppedInfoValues(String input) {
      Map<String, String> croppedInfo = new HashMap<String, String>();
      String[] value = input.split(",");
      
      for (String val : value) {
        String[] info = val.split(":");
        croppedInfo.put(info[0], info[1]);
      }      
      return croppedInfo;
    }
  }
  
//  /**
//   * Accepts and saves the uploaded image to profile.
//   * Closes the popup window, refreshes UIProfile.
//   *
//   */
//  public static class SaveActionListener extends EventListener<UIImageCroppingUploadContent> {
//    @Override
//    public void execute(Event<UIImageCroppingUploadContent> event) throws Exception {
//      UIImageCroppingUploadContent uiAvatarUploadContent = event.getSource();
//      
//      // crop image
//      uiAvatarUploadContent.crop();
//      
//      saveImageCropping(uiAvatarUploadContent);
//      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
//      uiPopup.setShow(false);
//      //Utils.updateWorkingWorkSpace();
//    }
//
//    private void saveImageCropping(UIImageCroppingUploadContent uiAvatarUploadContent,Event<UIImageCroppingUploadContent> event) throws Exception {
//      UIComponent parent =uiAvatarUploadContent.getParent();
//      while (parent != null) {
//         /*if (UISpaceInfo.class.isInstance(parent)) {
//           UISpaceInfo uiSpaceInfo = ((UISpaceInfo)parent);
//           SpaceService spaceService = uiSpaceInfo.getSpaceService();
//           String id = uiSpaceInfo.getUIStringInput("id").getValue();
//           Space space = spaceService.getSpaceById(id);
//           if (space != null) {
//             uiSpaceInfo.saveAvatar(uiAvatarUploadContent, space);
//             return;
//           }
//         }*/
//         parent = parent.getParent();
//      }
//      
//      // Save user avatar
//      uiAvatarUploadContent.saveUserAvatar(uiAvatarUploadContent, event);
//      return;
//    }
//  }

  /**
   * Saves avatar of users.
   * 
   * @param uiAvatarUploadContent
   * @throws Exception
   * @since 1.2.2
   */
  public void saveUserAvatar(UIImageCroppingUploadContent uiAvatarUploadContent, Event<UIImageCroppingUploadContent> event) throws Exception {
    ImageCroppingAttachment attacthment = uiAvatarUploadContent.getAvatarAttachment();
    LOG.info("---------saveUserAvatar--------");
       
    //copy UISaveAttachment
    UIImageCroppingUploadContent component = event.getSource();
    LOG.info("--event.getSource()");
    
    LOG.info("--file path: "+component.filePath);
    
    String tempPath = component.filePath.substring(1);
    

    String workspaceName = tempPath.substring(0, tempPath.indexOf("/"));
    LOG.info("--workspaceName: "+workspaceName);

      String nodePath = tempPath.substring(tempPath.indexOf("/"));
      Session srcSession = component.getUserSession(workspaceName);
      Node srcNode = (Node) srcSession.getItem(nodePath);
      Node srcContent = srcNode.getNode("jcr:content");
      Value value = srcContent.getProperty("jcr:data").getValue();
      String mimeType = srcContent.getProperty("jcr:mimeType").getString();
//      srcSession.logout();
      
//      String selectedFolder = selector.getSeletedFolder();
//      if (StringUtils.isEmpty(selectedFolder)) {
//        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.not-a-folder",
//                                                                                       null,
//                                                                                       ApplicationMessage.WARNING));
//        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
//        return;
//      }
//      String desWorkSpace = selectedFolder.substring(0, selectedFolder.indexOf("/"));
//      Session desSession = component.getUserSession(desWorkSpace);
//      selectedFolder = selectedFolder.substring(selectedFolder.indexOf("/"));
//      Node desNode = (Node) desSession.getItem(selectedFolder);
//      try {
//        desSession.checkPermission(desNode.getPath(), PermissionType.ADD_NODE);
//      } catch (RepositoryException e) {
//        event.getRequestContext()
//             .getUIApplication()
//             .addMessage(new ApplicationMessage("UISaveAttachment.msg.save-file-not-allow", null, ApplicationMessage.WARNING));
//        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
//        return;
//      }
//      try {
//        validate(fileName);
//      } catch (IllegalNameException e) {
//        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.not-valid-name",
//                                                new String[] { invalidCharacters },
//                                                ApplicationMessage.WARNING));
//        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
//        return;
//      }
      Node file = srcNode.addNode(uiAvatarUploadContent.getAvatarAttachment().getFileName(), "nt:file");
      Node jcrContent = file.addNode("jcr:content", "nt:resource");
      jcrContent.setProperty("jcr:data", value);
      jcrContent.setProperty("jcr:lastModified", new GregorianCalendar());
      jcrContent.setProperty("jcr:mimeType", mimeType);
      
     // desSession.save();
     // desSession.logout();
      
      LOG.info("--node filepath: "+ file.getPath());
      
      srcSession.save();
      srcSession.logout();      
      

      UIPopupWindow uiPopupWindow = event.getSource().getParent();
      uiPopupWindow.setUIComponent(null);
      uiPopupWindow.setRendered(false);
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISaveAttachment.msg.saved-successfully",
                                                                                     null,
                                                                                     ApplicationMessage.INFO));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());      
    
    
    
    
    
    
    
    
    
    /*Profile p = Utils.getOwnerIdentity().getProfile();
    p.setProperty(Profile.AVATAR, attacthment);
    Map<String, Object> props = p.getProperties();

    // Removes avatar url and resized avatar
    for (String key : props.keySet()) {
      if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
        p.removeProperty(key);
      }
    }

    Utils.getIdentityManager().updateProfile(p);*/
  }
  
  /**
   * Cancels, close the popup window.
   *
   */
  public static class CancelActionListener extends EventListener<UIImageCroppingUploadContent> {
    @Override
    public void execute(Event<UIImageCroppingUploadContent> event) throws Exception {
      UIImageCroppingUploadContent uiAvatarUploadContent = event.getSource();
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
    }

  }

  /**
   * Sets information of image storage.<br>
   *
   * @param imageBytes
   *        Image information in byte type for storing.
   * @throws Exception
   */
  private void setImageSource(byte[] imageBytes) throws Exception {
    if (imageBytes == null || imageBytes.length == 0) return;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
    DownloadService downloadService = getApplicationComponent(DownloadService.class);
    InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, "image");
    downloadResource.setDownloadName(imageAttachment.getFileName());
    imageSource = downloadService.getDownloadLink(downloadService.addDownloadResource(downloadResource));
  }
  
  private void crop() throws Exception {
    int x_co = 0;
    int y_co = 0;
    int width_co = 0; 
    int height_co = 0;
    
    BufferedImage originImg = ImageIO.read(getOriginImg());
    
    //
    if (getResizedImgInBytes() == null) {
      int imgWidth = originImg.getWidth();
      int imgHeight = originImg.getHeight();
      
      if (imgWidth != imgHeight) { // in case of image has width and height not the same
        int cropDimension = imgWidth > imgHeight ? imgHeight : imgWidth;
        int offset = (cropDimension == imgWidth) ? imgHeight - cropDimension : imgWidth - cropDimension;
        
        if (imgWidth < imgHeight) {
          x_co = 0;
          y_co = (int)(offset/2);
          width_co = imgWidth;
          height_co = imgHeight - offset;
        } else {
          x_co = (int)(offset/2);
          y_co = 0;
          width_co = imgWidth - offset;
          height_co = imgHeight;
        }
      }
    } else {
      BufferedImage resizedImg = ImageIO.read(new ByteArrayInputStream(getResizedImgInBytes()));
  
      // Original image information
      int w_o = originImg.getWidth();
      int h_o = originImg.getHeight();
      
      // Resized image information
      int w_r = resizedImg.getWidth();
      int h_r = resizedImg.getHeight();
      
      // cropped image information on resized image
      int x_cr = (int)Double.parseDouble(getCroppedInfo().get(X));
      int y_cr = (int)Double.parseDouble(getCroppedInfo().get(Y));
      int width_cr = (int)Double.parseDouble(getCroppedInfo().get(WIDTH));
      int height_cr = (int)Double.parseDouble(getCroppedInfo().get(HEIGHT));
      
      // calculate the scale
      double scale_w = (double)w_o/(double)w_r;
      double scale_h = (double)h_o/(double)h_r;
      
      double scale_OR = scale_w > scale_h ? scale_h : scale_w;
      
      // cropped image information on original image
      x_co = (int)(x_cr*scale_w);
      y_co = (int)(y_cr*scale_h);
      width_co = (int) (width_cr*scale_OR); 
      height_co = (int) (height_cr*scale_OR);
    }
    

    // sub image with new information
    BufferedImage croppedImg = originImg.getSubimage(x_co, y_co, width_co, height_co);
    
    ImageCroppingAttachment att = getAvatarAttachment();
    MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
    String extension = mimeTypeResolver.getExtension(att.getMimeType());
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(croppedImg, extension, os);
    os.flush();
    byte[] imageInByte = os.toByteArray();
    os.close();
    att.setImageBytes(imageInByte);
    InputStream input = new ByteArrayInputStream(imageInByte);

    att.setInputStream(input);    
    setAvatarAttachment(att);
  }
  
  public Session getUserSession(String workspace) throws Exception {
    ManageableRepository repository = getCurrentRepository();
    SessionProviderService sessionProviderService = (SessionProviderService) PortalContainer.getInstance()
                                                            .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    return sessionProvider.getSession(workspace, repository);
  }
  
  private ManageableRepository getCurrentRepository() throws RepositoryException {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(RepositoryService.class);
    return repoService.getCurrentRepository();
  }
  
}
