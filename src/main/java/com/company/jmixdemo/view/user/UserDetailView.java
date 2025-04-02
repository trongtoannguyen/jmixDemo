package com.company.jmixdemo.view.user;

import com.company.jmixdemo.entity.Step;
import com.company.jmixdemo.entity.User;
import com.company.jmixdemo.entity.UserStep;
import com.company.jmixdemo.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.MessageTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static org.reflections.Reflections.log;

@Route(value = "users/:id", layout = MainView.class)
@ViewController(id = "User.detail")
@ViewDescriptor(path = "user-detail-view.xml")
@EditedEntityContainer("userDc")
public class UserDetailView extends StandardDetailView<User> {
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private CollectionPropertyContainer<UserStep> stepsDc;
    @ViewComponent
    private DataContext dataContext;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private TypedTextField<String> usernameField;
    @ViewComponent
    private PasswordField passwordField;
    @ViewComponent
    private PasswordField confirmPasswordField;
    @ViewComponent
    private ComboBox<String> timeZoneField;
    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private Notifications notifications;

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Subscribe
    public void onInit(final InitEvent event) {
        timeZoneField.setItems(List.of(TimeZone.getAvailableIDs()));
    }

    @Subscribe
    public void onInitEntity(final InitEntityEvent<User> event) {
        usernameField.setReadOnly(false);
        passwordField.setVisible(true);
        confirmPasswordField.setVisible(true);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            usernameField.focus();
        }
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        if (entityStates.isNew(getEditedEntity())
            && !Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
            event.getErrors().add(messageBundle.getMessage("passwordsDoNotMatch"));
        }
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));

            String entityCaption = messageTools.getEntityCaption(getEditedEntityContainer().getEntityMetaClass());
            notifications.create(messageBundle.formatMessage("noAssignedRolesNotification", entityCaption))
                    .withType(Notifications.Type.WARNING)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    @Subscribe(id = "generateButton", subject = "clickListener")
    public void onGenerateButtonClick(final ClickEvent<JmixButton> event) {
        User user = getEditedEntity();
        if (user.getJoiningDate() == null) {
            notifications.create("Cannot generate steps for user without 'Joining date'")
                    .show();
            return;
        }

        List<Step> steps = dataManager.load(Step.class)
                .query("select s from Step s order by s.sortValue asc")
                .list();

        for (Step step : steps) {
            if (stepsDc.getItems().stream().noneMatch(userStep ->
                    userStep.getStep().equals(step))) {
                UserStep userStep = dataContext.create(UserStep.class);
                userStep.setUser(user);
                userStep.setStep(step);
                userStep.setDueDate(user.getJoiningDate().plusDays(step.getDuration()));
                stepsDc.getMutableItems().add(userStep);
            }
        }
    }

    @Supply(to = "stepsDataGrid.completed", subject = "renderer")
    private Renderer<UserStep> stepsDataGridCompletedRenderer() {
        return new ComponentRenderer<>(userStep -> {
            Checkbox checkbox = uiComponents.create(Checkbox.class);
            checkbox.setValue(userStep.getCompletedDate() != null);
            checkbox.addValueChangeListener(e -> {
                if (userStep.getCompletedDate() == null) {
                    userStep.setCompletedDate(LocalDate.now());
                } else {
                    userStep.setCompletedDate(null);
                }
            });
            log.warn("Updated completed date: " + userStep.getCompletedDate());
            return checkbox;
        });
    }
}