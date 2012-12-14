package org.exoplatform.services.deployment.plugins;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

import org.exoplatform.services.deployment.DeploymentDescriptor;
import org.exoplatform.services.deployment.DeploymentDescriptor.Target;

/*
 * JUnit test suite for org.exoplatform.services.deployment.plugins.XMLDeploymentPluginTest, 
 * based on BaseCommonsTestCase in commons/testing, 
 * that has configuration for PortalContainer and services, JCR services. 
 * The test cases will verify the feature of deployment data defined by XML file format to JCR.  
 */
public class XMLDeploymentPluginTest extends BaseCommonsTestCase {

  private SessionProvider      sessionProvider;

  private XMLDeploymentPlugin  plugin;

  private DeploymentDescriptor depDesc;

  /*
   * Set up before each test case.
   * @see org.exoplatform.commons.testing.BaseCommonsTestCase#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();
    
    // Session Provider
    SessionProviderService serv = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    this.sessionProvider = serv.getSystemSessionProvider(null);

    // Initial parameters for XML Deployment Plugin
    this.depDesc = new DeploymentDescriptor();
    depDesc.setCleanupPublication(true);
    // Target to JCR local
    Target target = new Target();
    target.setNodePath(root.getPath());
    target.setRepository(REPO_NAME);
    target.setWorkspace(WORKSPACE_NAME);
    depDesc.setTarget(target);

    ObjectParameter objParam = new ObjectParameter();
    objParam.setName("Test case XML Deployment Plugin");
    objParam.setObject(depDesc);

    InitParams initParams = new InitParams();
    initParams.addParam(objParam);

    // Create XML Deployment plugin instance
    this.plugin = new XMLDeploymentPlugin(initParams, configurationManager, repositoryService);

  }

  /*
   * Test case of deployment XML data in format of system view (SV), without history data. 
   */
  public void testDeploySVFormat() throws Exception {
    // Test case context
    String sourcePath = "classpath:/conf/data/ImagesSVFormat.xml";
    String rootDataName = "GlobalImages";
    int expectedChildNodes = 1;

    // Set details Deployment-Description: without history data
    setDepDesc(sourcePath, null);
    
    // Deploy
    this.plugin.deploy(sessionProvider);

    // Test data
    Session session = sessionProvider.getSession(WORKSPACE_NAME,
                                                 repositoryService.getRepository(REPO_NAME));

    Node rootJCR = session.getRootNode();
    Node rootData = rootJCR.getNode(rootDataName);

    assertNotNull(rootData);

    NodeIterator nodes = rootData.getNodes();
    assertEquals(nodes.getSize(), expectedChildNodes);

  }

  /*
   * Test case of deployment XML data in format of system view (SV), with history data. 
   */
  public void testDeploySVFormatWithHistory() throws Exception {
    //Test case context
    String sourcePath = "classpath:/conf/data/dataSV.xml";
    String vHistoryPath = "classpath:/conf/data/dataSV_versionHistory";
    String rootDataName = "testFolder";
    int expectedChildNodes = 2;

    // Set details Deployment Description: with history data
    setDepDesc(sourcePath, vHistoryPath);

    // Deploy data
    this.plugin.deploy(sessionProvider);

    // Test data
    Session session = sessionProvider.getSession(WORKSPACE_NAME,
                                                 repositoryService.getRepository(REPO_NAME));

    Node rootJCR = session.getRootNode();
    Node rootData = rootJCR.getNode(rootDataName);

    assertNotNull(rootData);

    NodeIterator nodes = rootData.getNodes();
    assertEquals(nodes.getSize(), expectedChildNodes);

  }

  /*
   * Tear down after each test case.
   * @see org.exoplatform.commons.testing.BaseCommonsTestCase#tearDown()
   */
  public void tearDown() throws Exception {
    // Remove existing data
    super.tearDown();
  }

  
   //======== PRIVATE FUNCTIONS =====// 
  
  /*
   * Set details for Deployment-Description following each test case context.
   */
  private void setDepDesc(String source, String historyPath) {    
    this.depDesc.setSourcePath(source);
    this.depDesc.setVersionHistoryPath(historyPath);
  }
}