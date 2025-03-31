package com.company.jmixdemo.view.user;

import com.company.jmixdemo.entity.User;
import com.company.jmixdemo.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "users", layout = MainView.class)
@ViewController(id = "User.list")
@ViewDescriptor(path = "user-list-view.xml")
@LookupComponent("usersDataGrid")
@DialogMode(width = "64em")
public class UserListView extends StandardListView<User> {
    private static final Logger log = LoggerFactory.getLogger(UserListView.class);
    @Autowired
    private DataComponents dataComponents;
    @ViewComponent
    private HorizontalLayout buttonsPanel;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @ViewComponent
    private JmixButton createButton;

    @Subscribe(id = "createButton", subject = "doubleClickListener")
    public void onCreateButtonClick(final ClickEvent<JmixButton> event) {
        final User user = (User) currentAuthentication.getUser();
        log.warn("User {} is trying to create a new user", user.getUsername());
    }
}