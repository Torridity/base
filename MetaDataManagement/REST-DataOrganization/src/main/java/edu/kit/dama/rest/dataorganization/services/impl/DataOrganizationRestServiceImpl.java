/*
 * Copyright 2014 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.rest.dataorganization.services.impl;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.services.interfaces.IDataOrganizationRestService;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.dataorganization.impl.staging.AttributeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.dataorganization.services.impl.util.DownloadStreamWrapper;
import edu.kit.dama.rest.dataorganization.services.impl.util.FileDownloadHandler;
import edu.kit.dama.rest.dataorganization.services.impl.util.HttpDownloadHandler;
import edu.kit.dama.rest.dataorganization.types.ElementPath;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class DataOrganizationRestServiceImpl implements IDataOrganizationRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataOrganizationRestServiceImpl.class);

    private static final Class[] IMPL_CLASSES = new Class[]{
        DataOrganizationNodeWrapper.class,
        LFNImpl.class,
        URL.class,
        DataOrganizationNodeImpl.class,
        FileNodeImpl.class,
        CollectionNodeImpl.class,
        AttributeImpl.class
    };

    // inTreeIdVersion is currently fixed here and in DataOrganization. This may change in future.
    int inTreeIdVersion = 1;

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNode(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting root node id for digital object id {}", objectId);
        NodeId rootId = null;
        try {
            rootId = DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(objectId, viewName);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to get root node id.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        LOGGER.debug("Loading node for node id {}", rootId);
        IDataOrganizationNode rootNode = null;

        try {
            rootNode = DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(rootId);
        } catch (InvalidNodeIdException ex) {
            LOGGER.error("Failed to load root node.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        LOGGER.debug("Writing node to output stream");
        // return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(rootNode, false)));
        return new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(rootNode, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNodeCount(String groupId, Long id, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting root node id for digital object id {}", objectId);

        NodeId rootId = null;
        try {
            rootId = DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(objectId, viewName);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to get root node id.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        //return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper((rootId != null) ? 1 : 0));
        return new DataOrganizationNodeWrapper((rootId != null) ? 1 : 0);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getChildren(String groupId, Long id, Long nodeId, Integer first, Integer results, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        NodeId nid = new NodeId(objectId, nodeId, inTreeIdVersion, viewName);

        List<? extends IDataOrganizationNode> l;
        try {
            l = DataOrganizerFactory.getInstance().getDataOrganizer().getChildren(nid, first, results);
        } catch (InvalidNodeIdException ex) {
            LOGGER.error("Failed to get children for node.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        List<DataOrganizationNodeImpl> children = new LinkedList();

        LOGGER.debug("Converting children to serializable format.");
        for (IDataOrganizationNode aChild : l) {
            if (aChild instanceof DataOrganizationNodeImpl) {
                //add child directly
                children.add((DataOrganizationNodeImpl) aChild);
            } else {
                try {
                    //reload child and copy content
                    children.add(DataOrganizationUtils.copyNode(DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(aChild.getTransientNodeId()), false));
                } catch (InvalidNodeIdException ex) {
                    LOGGER.error("Failed to get children for node.", ex);
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
            }
        }

        //return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(children));
        return new DataOrganizationNodeWrapper(children);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getNodeInformation(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        NodeId nid = new NodeId(objectId, nodeId, inTreeIdVersion, viewName);
        IDataOrganizationNode node;
        try {
            node = DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(nid);
        } catch (InvalidNodeIdException ex) {
            LOGGER.error("Failed to load node by id.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        //return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(node, false)));
        return new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(node, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getChildCount(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting child count for object with id {} and nodeId {}", objectId, nodeId);
        NodeId nid = new NodeId(objectId, nodeId, inTreeIdVersion, viewName);
        LOGGER.debug("Returning child count for node {}", nid);
        try {
            //return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getChildCount(nid).intValue()));
            return new DataOrganizationNodeWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getChildCount(nid).intValue());
        } catch (InvalidNodeIdException ex) {
            LOGGER.error("Failed to get child cound for node by id.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    public Response checkService() {
        IMetaDataManager doMdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(PersistenceFacade.getInstance().getPersistenceUnitName());
        doMdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        ServiceStatus status = ServiceStatus.UNKNOWN;
        try {
            LOGGER.debug("Doing service check by obtaining data organization node count");
            Number result = (Number) doMdm.findSingleResult("SELECT COUNT(a) FROM DataOrganizationNode a");
            LOGGER.debug("Service check using node count returned {}", result);
            status = ServiceStatus.OK;
        } catch (Throwable t) {
            LOGGER.error("Obtaining node count returned an error. Service status is set to ERROR", t);
            status = ServiceStatus.ERROR;
        } finally {
            doMdm.close();
        }
        return Response.status(200).entity(new CheckServiceResponse("DataOrganization", status)).build();
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNodeChildren(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting root node id for digital object id {}", objectId);
        NodeId rootId;
        try {
            rootId = DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(objectId, viewName);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to get root node id.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        LOGGER.debug("Loading node for node id {}", rootId);
        IDataOrganizationNode rootNode;
        try {
            rootNode = DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(rootId);
        } catch (InvalidNodeIdException ex) {
            LOGGER.error("Failed to load root node.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        LOGGER.debug("Converting result to DataOrganizationNodeImpl list.");

        List<DataOrganizationNodeImpl> implList = new ArrayList<>();
        for (IDataOrganizationNode node : ((ICollectionNode) rootNode).getChildren()) {
            implList.add(DataOrganizationUtils.copyNode(node, false));
        }
        //return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(implList));
        return new DataOrganizationNodeWrapper(implList);

    }

    @Override
    public DataOrganizationViewWrapper getViews(String groupId, Long id, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting views for object with id {}", objectId);
        return new DataOrganizationViewWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getViews(objectId));
    }

    @Override
    public DataOrganizationViewWrapper getViewCount(String groupId, Long id, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting view count for object with id {}", objectId);
        return new DataOrganizationViewWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getViews(objectId).size());
    }

    @Override
    public Response downloadContent(String pGroupId, Long pBaseId, String pViewName, String pPath, HttpContext hc, ResourceConfig config) {

        DigitalObjectId objectId = null;
        boolean preliminaryAccess = false;
        IAuthorizationContext ctx = null;
        try {
            LOGGER.debug("Trying to obtain real authorization context for groupId {}.", pGroupId);
            ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
            LOGGER.debug("Authorization context obtained. No preliminary access required.");
        } catch (WebApplicationException wae) {
            if (wae.getResponse().getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
                LOGGER.info("Caller is not authorized to access object. Granting temporary access using system context.");
                //failed...grant preliminary access
                preliminaryAccess = true;
                ctx = AuthorizationContext.factorySystemContext();
            } else {
                LOGGER.debug("Response status is not UNAUTHORIZED. Preliminary access not available.");
                throw wae;
            }
        }

        objectId = getDigitalObjectId(pBaseId, ctx);

        LOGGER.debug("Continuing with {} access to object with id {}", (preliminaryAccess) ? "preliminary" : "authorized", objectId);

        //check path whether it is a single id...in that case access a node directly
        //if path is a slash-separated string, obtain the according node
        //return node value depending on the media type stored in the request (either XML or octet-stream)
        try {
            LOGGER.error("Getting data organization node for object with id '{}', view '{}' and path '{}'", objectId, pViewName, pPath);

            final IDataOrganizationNode node;
            try {
                LOGGER.debug("Getting node for element path {}, object with id '{}' and view '{}'", pPath, objectId, pViewName);
                node = new ElementPath(pPath).getNodeForPath(objectId, pViewName);
                if (node != null) {
                    LOGGER.debug("Obtained node {}", node);
                } else {
                    LOGGER.warn("Obtained no node.");
                }
            } catch (EntityNotFoundException | InvalidNodeIdException ex) {
                LOGGER.error("Failed to get node for path '" + pPath + "'.", ex);
                throw new WebApplicationException(Status.NOT_FOUND);
            }

            if (preliminaryAccess) {
                LOGGER.debug("Preliminary access enabled. Checking if node with id {} is eligible for authorization less access.", node.getTransientNodeId());
                checkAuthorizationLessAccess(node, config);
                LOGGER.debug("Node is eligible for authorization less access.");
            }

            DownloadStreamWrapper wrapper = null;
            if (node instanceof IFileNode) {
                LOGGER.debug("Obtaining LFN from file node {}", node.getName());
                ILFN ilfn = ((IFileNode) node).getLogicalFileName();
                if (ilfn == null) {
                    LOGGER.error("Invalid file node at path {} for object {}. No LFN available.", pPath, pBaseId);
                    throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
                }
                LOGGER.debug("LFN is {}.", ilfn);

                URL lfnUrl = new URL(ilfn.asString());
                try {
                    if ("file".equals(lfnUrl.getProtocol().toLowerCase())) {
                        LOGGER.debug("Using file handler for providing download of URL {}.", lfnUrl);
                        wrapper = new FileDownloadHandler().prepareStream(new File(lfnUrl.toURI()));
                    } else if ("http".equals(lfnUrl.getProtocol().toLowerCase())) {
                        //more flexible handling for http might be useful here
                        LOGGER.debug("Using Http handler for providing download of URL {}.", lfnUrl);
                        wrapper = new HttpDownloadHandler().prepareStream(lfnUrl);
                    } else {
                        //support for other protocols in future?
                        LOGGER.error("No download handler available for protocol '{}'.", lfnUrl.getProtocol());
                        //not implemented for this protocol type
                        throw new WebApplicationException(501);
                    }
                } catch (URISyntaxException use) {
                    LOGGER.error("Direct data access is not available for LFNs in the form of '{}'", lfnUrl);
                    //not implemented for this LFN type
                    throw new WebApplicationException(501);
                }

                return Response.ok(wrapper.getOutputStream(), wrapper.getContentType()).build();
            } else if (node instanceof ICollectionNode) {
                LOGGER.debug("Establishing piped streams.");
                PipedInputStream in = new PipedInputStream();
                final PipedOutputStream out = new PipedOutputStream(in);
                LOGGER.debug("Starting streaming thread.");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LOGGER.debug("Start zipping operation to piped output stream.");
                            DataOrganizationUtils.zip((ICollectionNode) node, out);
                            LOGGER.debug("Zipping operation finshed.");
                        } catch (IOException ex) {
                            LOGGER.error("Failed to zip node content to output stream.", ex);
                        } finally {
                            if (out != null) {
                                try {
                                    out.flush();
                                    out.close();
                                } catch (IOException ex) {
                                    //ignore
                                }
                            }
                        }
                    }
                }
                ).start();
                String resultName = objectId + "_" + pViewName + ((node.getName() != null) ? "_" + node.getName() : "");
                LOGGER.debug("Returning response file named '{}' in stream linked to piped zip stream.", resultName);
                //Using piped input stream rather than streaming output via DowloadStreamWrapper as this seems not to work here. 
                return Response.ok(in, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                        "attachment; filename=" + resultName + ".zip").build();
            } else {
                LOGGER.error("Data access to nodes of type '{}' is currently not supported.", ((node != null) ? node.getClass().getCanonicalName() : "<NULL>"));
                //not implemented for these nodes
                throw new WebApplicationException(501);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to perform data download.", ex);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> addDataOrganizationView(String groupId, Long baseId, String viewData, Boolean preserveAttributes, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(baseId, groupId, hc);
        IFileTree tree;
        String viewName = null;
        DataOrganizer org = DataOrganizerFactory.getInstance().getDataOrganizer();

        try {
            tree = Util.jsonViewToFileTree(new JSONObject(viewData), preserveAttributes);
            viewName = tree.getViewName();
            if (viewName == null || Util.isReservedViewName(viewName)) {
                LOGGER.error("The view name '{}' provided with the view description is invalid or reserved by the system.", viewName);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            if (!objectId.getStringRepresentation().equals(tree.getDigitalObjectId().getStringRepresentation())) {
                LOGGER.error("The objectId " + tree.getDigitalObjectId() + " provided in the JSON data does not fit the object id " + objectId + " that belongs to baseID " + baseId);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            org.createFileTree(tree);

            NodeId rootId = org.getRootNodeId(objectId, tree.getViewName());
            IDataOrganizationNode newRoot = org.loadNode(rootId);
            // return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(newRoot, false)));
            return new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(newRoot, false));
        } catch (InvalidNodeIdException ex) {
            LOGGER.error("Failed to obtain valid file tree from provided JSON data. At least one contained nodeId is invalid.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (EntityExistsException ex) {

            LOGGER.error("There exists already a data organization view named " + viewName + " for object id " + objectId, ex);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to obtain root node of just created data organization view " + viewName + " for object id " + objectId, ex);

            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Check is the provided node is eligable to authorization-less access. This
     * method is called if no proper authorization context could be obtained
     * from the user request. If one of the configured rules applies this method
     * returns without any further notice. If no rule applies, the access is
     * denied and an error HTTP.FORBIDDEN is returned to the client.
     *
     *
     * There are multiple rules applied in the following order:
     *
     * <ul>
     * <li>Is the node in a specific view?</li>
     * <li>Has the node a specific attribute assigned?</li>
     * <li>Is the node a collection node and is authorization-less access to
     * collection nodes allowed?</li>
     * <li>Is the node a file node and does its name matches a defined regular
     * expression?</li>
     * </ul>
     *
     * All these rules are configured in the web.xml section of the data
     * organization rest service using the following init params:
     *
     * <ul>
     * <li>public.view.names - Semicolon-separated list of view names (e.g.
     * public;myView)</li>
     * <li>public.attribute.key - Attribute key that must be assigned to an
     * eligable node (e.g. public)</li>
     * <li>public.collection.node.access.allowed - true or false (default:
     * false)</li>
     * <li>public.file.node.filter - Regular expression used to match the node
     * name (e.g. (.*)\.jpg$ to accept all nodes ending with .jpg)</li>
     * </ul>
     *
     *
     * @param pNode The node to which the access is checked.
     * @param resource The resource config used to obtain the init-params
     * containing the rule list.
     */
    private void checkAuthorizationLessAccess(IDataOrganizationNode pNode, ResourceConfig resource) {
        boolean accessAllowed = false;
        String publicViews = (String) resource.getProperty("public.view.names");
        LOGGER.debug("Public view names: {}", publicViews);
        String publicCollectionAccess = (String) resource.getProperty("public.collection.node.access.allowed");
        LOGGER.debug("Public collection access allowed: {}", publicCollectionAccess);
        String publicFileNodeFilter = (String) resource.getProperty("public.file.node.filter");
        LOGGER.debug("Public file node filter: {}", publicFileNodeFilter);
        String publicAttribute = (String) resource.getProperty("public.attribute.key");
        LOGGER.debug("Public attribute key: {}", publicAttribute);

        List<String> allowedViews = new ArrayList<>();
        if (publicViews != null) {
            Collections.addAll(allowedViews, publicViews.trim().split(";"));
        }

        boolean collectionAccessAllowed = Boolean.parseBoolean(publicCollectionAccess);

        if (allowedViews.contains(pNode.getViewName())) {
            LOGGER.debug("Authorization-less access to node granted by view name {}", pNode.getViewName());
            accessAllowed = true;
        } else {
            LOGGER.debug("Node with in view {} not eligable to authorization-less access by view name", pNode.getViewName());
        }

        if (!accessAllowed && publicAttribute != null) {
            for (IAttribute attrib : pNode.getAttributes()) {
                if (attrib.getKey().equals(publicAttribute)) {
                    LOGGER.debug("Authorization-less access to node granted by existence of attribute {}", publicAttribute);
                    accessAllowed = true;
                }
            }
        }

        if (!accessAllowed) {
            if (pNode instanceof ICollectionNode && collectionAccessAllowed) {
                LOGGER.debug("Authorization-less access to node granted by type ICollectionNode");
                accessAllowed = true;
            } else {
                LOGGER.debug("Node with name {} not eligable to authorization-less access by type ICollectionNode", pNode.getName());
            }
        }

        if (!accessAllowed) {
            if ((pNode instanceof IFileNode) && publicFileNodeFilter != null && Pattern.matches(publicFileNodeFilter.trim(), pNode.getName().trim())) {
                LOGGER.debug("Authorization-less access to node granted by type IFileNode, pattern {} and node name {}", publicFileNodeFilter, pNode.getName());
                accessAllowed = true;
            } else {
                LOGGER.debug("Node with name {} not eligable to authorization-less access by file filter {}", pNode.getName(), publicFileNodeFilter);
            }
        }

        if (!accessAllowed) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
    }

    /**
     * Get the digital object id for the provided base id. If No object for
     * pBaseId exists, a WebApplicationException with error code 404 ist thrown.
     * If the accessing context is not authorized to access the object a
     * WebApplicationException with HTTP 401 is thrown.
     *
     * @param pBaseId The base id.
     * @param pGroupId The group if for authorization.
     * @param hc The HttpContext for authorization.
     *
     * @return The digital object id.
     */
    private DigitalObjectId getDigitalObjectId(Long pBaseId, String pGroupId, HttpContext hc) {
        return getDigitalObjectId(pBaseId, RestUtils.authorize(hc, new GroupId(pGroupId)));
    }

    /**
     * Get the digital object id for the provided base id. If No object for
     * pBaseId exists, a WebApplicationException with error code 404 ist thrown.
     * If the accessing context is not authorized to access the object a
     * WebApplicationException with HTTP 401 is thrown.
     *
     * @param pBaseId The base id.
     * @param pGroupId The group if for authorization.
     * @param hc The HttpContext for authorization.
     *
     * @return The digital object id.
     */
    private DigitalObjectId getDigitalObjectId(Long pBaseId, IAuthorizationContext pCtx) {
        LOGGER.debug("Getting digital object id for base id {} with context {}", new Object[]{pBaseId, pCtx});
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(pCtx);
        try {
            LOGGER.debug("Obtaining digital object for id {}", pBaseId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, pBaseId);
            if (object == null) {
                throw new WebApplicationException(new Exception("Object for id " + pBaseId + " not found"), 404);
            }
            return object.getDigitalObjectId();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain object for baseId " + pBaseId + ".", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

}
