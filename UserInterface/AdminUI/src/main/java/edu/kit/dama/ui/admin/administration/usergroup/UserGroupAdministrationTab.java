/* 
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin.administration.usergroup;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import static edu.kit.dama.authorization.entities.Role.ADMINISTRATOR;
import static edu.kit.dama.authorization.entities.Role.MANAGER;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserGroupAdministrationTab extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupAdministrationTab.class);
    public final static String DEBUG_ID_PREFIX = UserGroupAdministrationTab.class.getName() + "_";

    private final AdminUIMainView parentApp;
    private VerticalLayout mainLayout;
    private UserGroupForm userGroupForm;
    private UserGroupSearch userGroupSearch;
    private UserGroupTablePanel userGroupTablePanel;

    public UserGroupAdministrationTab(AdminUIMainView pParentApp) {
        parentApp = pParentApp;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setCompositionRoot(getMainLayout());
        update();
    }

    /**
     *
     * @return
     */
    private VerticalLayout getMainLayout() {
        if (mainLayout == null) {
            buildMainLayout();
        }
        return mainLayout;
    }

    /**
     *
     */
    private void buildMainLayout() {
        String id = "mainLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        mainLayout = new VerticalLayout();
        mainLayout.setId(DEBUG_ID_PREFIX + id);
        mainLayout.setSizeFull();
        mainLayout.setImmediate(true);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        // Add components to mainLayout
        mainLayout.addComponent(getUserGroupForm());
        mainLayout.addComponent(getUserGroupSearch());
        mainLayout.addComponent(getUserGroupTablePanel());

        mainLayout.setComponentAlignment(
                getUserGroupSearch(), Alignment.BOTTOM_RIGHT);
    }

    /**
     *
     * @return
     */
    public UserGroupForm getUserGroupForm() {
        if (userGroupForm == null) {
            userGroupForm = new UserGroupForm(this);
        }
        return userGroupForm;
    }

    /**
     *
     * @return
     */
    public UserGroupSearch getUserGroupSearch() {
        if (userGroupSearch == null) {
            userGroupSearch = new UserGroupSearch(this);
        }
        return userGroupSearch;
    }

    /**
     *
     * @return
     */
    public UserGroupTablePanel getUserGroupTablePanel() {
        if (userGroupTablePanel == null) {
            userGroupTablePanel = new UserGroupTablePanel(this);
        }
        return userGroupTablePanel;
    }

    /**
     *
     * @return
     */
    public AdminUIMainView getParentApp() {
        return parentApp;
    }

    /**
     *
     * @param groupId
     * @return
     * @throws edu.kit.dama.authorization.exceptions.AuthorizationException
     */
    public IAuthorizationContext getACTX(GroupId groupId) throws AuthorizationException {
        if (parentApp.getAuthorizationContext().getRoleRestriction().atLeast(Role.ADMINISTRATOR)) {
            return parentApp.getAuthorizationContext();
        }
        return parentApp.getAuthorizationContext(groupId);
    }

    /**
     *
     */
    public void reload() {
        getUserGroupTablePanel().reloadUserGroupTable();
    }

    /**
     *
     */
    public void disable() {
        getUserGroupForm().clearGroupDataComponents();
        getUserGroupTablePanel().getUserGroupTable().removeAllItems();
        setEnabled(false);
    }

    /**
     *
     */
    public void enable() {
        reload();
        setEnabled(true);
    }

    /**
     *
     */
    public final void update() {
        Role loggedInUserRole = getParentApp().getLoggedInUser().getCurrentRole();
        switch (loggedInUserRole) {
            case ADMINISTRATOR:
                if (!isEnabled()) {
                    enable();
                }
                getUserGroupForm().update(ADMINISTRATOR);
                break;
            case MANAGER:
                if (!isEnabled()) {
                    enable();
                }
                getUserGroupForm().update(MANAGER);
                break;
            default:
                if (isEnabled()) {
                    disable();
                }
                UIComponentTools.showWarning("WARNING", "Unauthorized access attempt! " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Unauthorized access attempt!");
                break;
        }
        reload();
    }
}
