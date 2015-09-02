/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.staging.services.impl.download;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizationServiceLocal;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.interfaces.IDownloadInformationService;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.staging.util.TransferClientPropertiesUtils;
import edu.kit.dama.staging.handlers.impl.DownloadPreparationHandler;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local implementation of the DownloadInformationService. This service is
 * intended to be used server-sided, either by the user frontend or by external
 * services. This service offers a limited access to the DownloadInformation
 * persistence implementation, a method to prepare a download and some
 * administrative methods for cleanup.
 *
 * @author jejkal
 */
public final class DownloadInformationServiceLocal implements IDownloadInformationService<IAuthorizationContext> {

  /**
   * The logger
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(DownloadInformationServiceLocal.class);
  /**
   * DownloadInformationPersistanceImpl used by this service. The actual
   * implementation can be chosen during initialize()
   */
  private static DownloadInformationPersistenceImpl persistenceImpl = null;
  /**
   * The singleton instance
   */
  private static DownloadInformationServiceLocal SINGLETON = null;

  static {
    //intitially configure the sownload service
    initialize();
    SINGLETON = new DownloadInformationServiceLocal();
  }

  /**
   * Get the singleton instance of the local DownloadInformationService.
   *
   * @return The singleton instance.
   */
  public static DownloadInformationServiceLocal getSingleton() {
    return SINGLETON;
  }

  /**
   * Default constructor
   */
  DownloadInformationServiceLocal() {
  }

  /**
   * Initialize the service.
   */
  public static void initialize() {
    persistenceImpl = DownloadInformationPersistenceImpl.getSingleton();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<DownloadInformation> getAllDownloadInformation(int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getAllDownloadInformation()");
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getAllEntities(pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getAllDownloadInformationCount(IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationCount()");
    return persistenceImpl.getAllEntitiesCount(pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public Integer getDownloadInformationCountByOwner(final String pOwner, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationCountByOwner({})", pOwner);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesCountByOwner(new UserId(pOwner), pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public List<DownloadInformation> getDownloadInformationByOwner(final String pOwner, int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationByOwner({})", pOwner);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesByOwner(new UserId(pOwner), pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<DownloadInformation> getDownloadInformationByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationByDigitalObjectId({})", pDigitalObjectId);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesByDigitalObjectId(pDigitalObjectId, pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getDownloadInformationCountByDigitalObjectId(DigitalObjectId pDigitalObjectId, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationCountByDigitalObjectId({})", pDigitalObjectId);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesCountByDigitalObjectId(pDigitalObjectId, pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public DownloadInformation getDownloadInformationById(Long pDownloadId, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationById({})", pDownloadId);
    /**
     * general access check using aspects map pDownloadId and pSecurityContext
     * to a filter criteria retrieve the DownloadInformation entity and return
     * it
     */
    return persistenceImpl.getEntityById(pDownloadId, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<DownloadInformation> getDownloadInformationByStatus(Integer pStatusCode, int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationByStatus({})", pStatusCode);
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesByStatus(DOWNLOAD_STATUS.idToStatus(pStatusCode), pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getDownloadInformationCountByStatus(Integer pStatusCode, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationCountByStatus({})", pStatusCode);
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all download information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesCountByStatus(DOWNLOAD_STATUS.idToStatus(pStatusCode), pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<DownloadInformation> getExpiredDownloadInformation(int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getExpiredDownloadInformation()");
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all sownload information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getExpiredEntities(pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getExpiredDownloadInformationCount(@Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getExpiredDownloadInformationCount()");
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all sownload information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getExpiredEntitiesCount(pSecurityContext).intValue();
  }

  /**
   * Wrapper for better, internal access.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pProperties The transfer client properties.
   * @param pSecurityContext The security context.
   *
   * @return The newly created download information entity.
   *
   * @throws TransferPreparationException If the transfer preparation failed.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  public DownloadInformation scheduleDownload(DigitalObjectId pDigitalObjectId, TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    return scheduleDownload(pDigitalObjectId, null, TransferClientPropertiesUtils.propertiesToMap(pProperties), pSecurityContext);
  }

  /**
   * Wrapper for better, internal access.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pFileTree FileTree to download. This tree is a part of the FileTree
   * associated with the provided DigitalObjectId.
   * @param pProperties The transfer client properties.
   * @param pSecurityContext The security context.
   *
   * @return The newly created download information entity.
   *
   * @throws TransferPreparationException If the transfer preparation failed.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  public DownloadInformation scheduleDownload(DigitalObjectId pDigitalObjectId, IFileTree pFileTree, TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    return scheduleDownload(pDigitalObjectId, pFileTree, TransferClientPropertiesUtils.propertiesToMap(pProperties), pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public DownloadInformation scheduleDownload(DigitalObjectId pDigitalObjectId, IFileTree pFileTree, Map<String, String> pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    LOGGER.info("Executing scheduleDownload({}, {})", new Object[]{pDigitalObjectId, pProperties});

    if (null == pDigitalObjectId) {
      throw new IllegalArgumentException("Argument 'pDigitalObjectId' must not be null");
    }
    LOGGER.debug("Checking for digital object with id '{}'", pDigitalObjectId);
    checkObject(pDigitalObjectId, pSecurityContext);

    TransferClientProperties props = TransferClientPropertiesUtils.mapToProperties(pProperties);

    final IFileTree tmpTree;
    if (pFileTree != null) {
      //download the provided selection only
      tmpTree = pFileTree;
    } else {
      //download entire object
      try {
        tmpTree = DataOrganizationServiceLocal.getSingleton().loadFileTree(pDigitalObjectId, pSecurityContext);
      } catch (EntityNotFoundException enfe) {
        throw new TransferPreparationException("Unable to prepare download for digital object '" + pDigitalObjectId + "'. No data organization content found.", enfe);
      }
    }

    LOGGER.debug("Converting temporary tree to persistable version");
    IFileTree treeToDownload = DataOrganizationUtils.copyTree(tmpTree);
    final String accessPointId = props.getStagingAccessPointId();
    DownloadInformation existingEntity = null;
    if (accessPointId != null) {
      List<DownloadInformation> existingEntities = persistenceImpl.getEntitiesByDigitalObjectId(pDigitalObjectId, pSecurityContext);
      LOGGER.debug("Searching for download associated with access point {}", accessPointId);
      existingEntity = (DownloadInformation) CollectionUtils.find(existingEntities, new Predicate() {

        @Override
        public boolean evaluate(Object o) {
          return accessPointId.equals(((DownloadInformation) o).getAccessPointId());
        }
      });
    } else {
      LOGGER.warn("AccessPointId is null, skipping search for download with access point.");
    }

    DownloadInformation activeDownload;

    if (existingEntity != null) {
      LOGGER.debug("Existing download found. Removing local data.");
      StagingService.getSingleton().flushDownload(existingEntity.getId(), pSecurityContext);
      LOGGER.debug("Setting download to SCHEDULED");
      //reset status and error message
      existingEntity.setStatusEnum(DOWNLOAD_STATUS.SCHEDULED);
      existingEntity.setErrorMessage(null);
      existingEntity.setClientAccessURL(null);
      //reset expire timer
      existingEntity.setExpiresAt(System.currentTimeMillis() + DownloadInformation.DEFAULT_LIFETIME);
      existingEntity.setAccessPointId(props.getStagingAccessPointId());
      //merge the entity with the database
      activeDownload = persistenceImpl.mergeEntity(existingEntity, pSecurityContext);
    } else {
      LOGGER.debug("No entity found for ID '{}'. Creating new download entity.", pDigitalObjectId);
      //no entity for pDigitalObjectId found...create a new one
      activeDownload = persistenceImpl.createEntity(pDigitalObjectId,  props.getStagingAccessPointId(), pSecurityContext);
    }

    LOGGER.debug("Creating DownloadPreparationHandler");
    DownloadPreparationHandler handler = new DownloadPreparationHandler(persistenceImpl, activeDownload, treeToDownload);
    LOGGER.debug("Scheduling download");
    handler.prepareTransfer(TransferClientPropertiesUtils.mapToProperties(pProperties), pSecurityContext);
    LOGGER.debug("Download scheduling finished. Obtaining updated entity.");
    activeDownload = persistenceImpl.getEntityById(activeDownload.getId(), pSecurityContext);
    LOGGER.debug("Returning download entity.");
    return activeDownload;
  }

  /**
   * Check if a digital object for the provided id exists in order to allow a
   * download. If no entity exists or is not accessible using the provided
   * context, this method will throw an exception and the staging operation
   * should fail.
   *
   * @param pDigitalObjectId The id of the digital object to check for.
   * @param pSecurityContext The security context used to access the digital
   * object.
   *
   * @throws TransferPreparationException if anything goes wrong.
   */
  private void checkObject(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pSecurityContext);
    try {
      if (!new DigitalObjectSecureQueryHelper().objectByIdentifierExists(pDigitalObjectId.toString(), mdm, pSecurityContext)) {
        throw new TransferPreparationException("Digital object for id '" + pDigitalObjectId + "' not found");
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      throw new TransferPreparationException("Digital object for id '" + pDigitalObjectId + "' not accessible", ex);
    } catch (edu.kit.dama.authorization.exceptions.EntityNotFoundException ex) {
      throw new TransferPreparationException("Digital object for id '" + pDigitalObjectId + "' not found", ex);
    } finally {
      mdm.close();
    }
  }

  /**
   * Removes all sownloads which have failed or finished and which are expired.
   * This method should be used internally only, either by an administrator or
   * by an automated process.
   *
   * @param pSecurityContext The security context.
   *
   * @return The number of removed entities.
   */
  @SecuredMethod(roleRequired = Role.ADMINISTRATOR)
  public Integer cleanup(@Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing cleanup()");
    //obtain relevant entities
    LOGGER.debug("Obtaining expired download information");
    List<DownloadInformation> expiredDownloads = getExpiredDownloadInformation(-1, -1, pSecurityContext);
    LOGGER.debug("Obtaining download information with status DOWNLOAD_REMOVED");
    List<DownloadInformation> removedDownloads = getDownloadInformationByStatus(DOWNLOAD_STATUS.DOWNLOAD_REMOVED.getId(), -1, -1, pSecurityContext);

    int removeEntities = 0;
    //remove remaining entities
    LOGGER.debug("Removing {} expired  entities", expiredDownloads.size());
    for (DownloadInformation information : expiredDownloads) {
      StagingService.getSingleton().flushDownload(information.getId(), pSecurityContext);
      removeEntities += persistenceImpl.removeEntity(information.getId(), pSecurityContext);
    }

    LOGGER.debug("Removing {} removed entities", removedDownloads.size());
    for (DownloadInformation information : removedDownloads) {
      StagingService.getSingleton().flushDownload(information.getId(), pSecurityContext);
      removeEntities += persistenceImpl.removeEntity(information.getId(), pSecurityContext);
    }

    LOGGER.info("Cleanup done");
    return removeEntities;
  }

  /**
   * Remove a single sownload by its id. Normally this method should not be
   * called by a user as there may arise inconsistencies.
   *
   * @param pDownloadId The ID of the download to remove.
   * @param pSecurityContext The security context.
   *
   * @return The result number of affected rows.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer removeDownload(Long pDownloadId, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing removeDownload({})", pDownloadId);
    /*
     * General access check using aspects Check if removal is allowed (using
     * context) Remove download entity from data backend and cleanup all data
     * associated with this entry (DownloadClient, Staging)
     */
    return persistenceImpl.removeEntity(pDownloadId, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateStatus(Long pId, Integer pStatus, String pErrorMessage, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateStatus({}, {}, {})", new Object[]{pId, DOWNLOAD_STATUS.idToStatus(pStatus), pErrorMessage});
    return persistenceImpl.updateStatus(pId, DOWNLOAD_STATUS.idToStatus(pStatus), pErrorMessage, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateClientAccessUrl(Long pId, String pAccessURL, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateClientAccessUrl({}, {})", new Object[]{pId, pAccessURL});
    return persistenceImpl.updateClientAccessUrl(pId, pAccessURL, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateStagingUrl(Long pId, String pStagingURL, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateStagingUrl({}, {})", new Object[]{pId, pStagingURL});
    return persistenceImpl.updateStagingUrl(pId, pStagingURL, pSecurityContext);
  }

  @Override
  //@Access(minimalRole = Role.MEMBER)
  public Integer updateStorageUrl(Long pId, String pStorageURL, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateStorageUrl({}, {})", new Object[]{pId, pStorageURL});
    return persistenceImpl.updateStorageUrl(pId, pStorageURL, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer removeEntity(Long pId, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing removeEntity({})", new Object[]{pId});
    return persistenceImpl.removeEntity(pId, pSecurityContext);
  }
}