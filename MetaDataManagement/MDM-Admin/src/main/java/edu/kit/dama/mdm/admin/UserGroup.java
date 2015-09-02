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
package edu.kit.dama.mdm.admin;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
@Entity
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("id")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("id"),
            @XmlNamedAttributeNode("groupId"),
            @XmlNamedAttributeNode("groupName"),
            @XmlNamedAttributeNode("description")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroup {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserGroup.class);
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String groupId;
  @Column(length = 256)
  private String groupName;
  @Column(length = 1024)
  private String description;

  /**
   * Get the id.
   *
   * @return The id.
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the id.
   *
   * @param id The id.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the group id.
   *
   * @return The group id.
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Set the group id.
   *
   * @param groupId The group id.
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Get the group name.
   *
   * @return The group name.
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Set the group name.
   *
   * @param groupName The group name.
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * Get the group description.
   *
   * @return The group description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the group description.
   *
   * @param description The group description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get all members of this group. This method performs a call to {@link GroupServiceLocal#getUsersIds(edu.kit.dama.authorization.entities.GroupId, int, int, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * }, therefore this method should only be used within the process running the
   * repository system application.
   *
   * @return A list of all members.
   */
  public final List<UserId> getMembers() {
    List<UserId> result;
    try {
      result = GroupServiceLocal.getSingleton().getUsersIds(new GroupId(groupId), 0, Integer.MAX_VALUE, AuthorizationContext.factorySystemContext());
    } catch (UnauthorizedAccessAttemptException ex) {
      //return empty list
      result = new ArrayList<>();
      LOGGER.error("Failed to obtain all users in group " + groupId, ex);
    }
    return result;
  }
}
