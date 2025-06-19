package de.vptr.midas.gui.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.vptr.midas.gui.service.AuthService;
import jakarta.inject.Inject;

@Route(value = "login", layout = MainLayout.class)
@PageTitle("Midas - Login")
public class LoginView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(LoginView.class);

    @Inject
    AuthService authService;

    public LoginView() {
        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);

        final var title = new H1("Midas - Login");

        final var usernameField = new TextField("Username");
        usernameField.setPrefixComponent(LineAwesomeIcon.USER_SOLID.create());
        usernameField.setRequired(true);
        usernameField.setWidth("300px");

        final var passwordField = new PasswordField("Password");
        passwordField.setPrefixComponent(LineAwesomeIcon.LOCK_SOLID.create());
        passwordField.setRequired(true);
        passwordField.setWidth("300px");

        final var loginButton = new Button("Login");
        loginButton.setIcon(LineAwesomeIcon.SIGN_IN_ALT_SOLID.create());
        loginButton.addClickListener(e -> {
            final var username = usernameField.getValue();
            final var password = passwordField.getValue();

            try {
                // Disable button during authentication
                loginButton.setEnabled(false);
                loginButton.setText("Authenticating...");

                // Perform authentication
                final var result = this.authService.authenticate(username, password);

                LOG.trace("Authentication result - Status: {}, Message: {}", result.getStatus(), result.getMessage());

                switch (result.getStatus()) {
                    case SUCCESS:
                        LOG.trace("Authentication successful, navigating to main view");
                        this.getUI().ifPresent(ui -> ui.navigate(""));
                        break;

                    case INVALID_CREDENTIALS:
                        LOG.trace("Invalid credentials, showing error message");
                        this.showError(result.getMessage());
                        passwordField.clear();
                        passwordField.focus();
                        break;

                    case BACKEND_UNAVAILABLE:
                        LOG.error("Backend unavailable during login, redirecting to error page");
                        this.getUI().ifPresent(ui -> ui.navigate("backend-error"));
                        break;

                    case INVALID_INPUT:
                        LOG.trace("Invalid input, showing warning");
                        this.showWarning(result.getMessage());
                        break;

                    default:
                        LOG.error("Unknown authentication result status: {}", result.getStatus());
                        this.showError("Unknown error occurred");
                        break;
                }

            } catch (final Exception ex) {
                LOG.error("Exception during authentication", ex);
                this.showError("Unexpected error: " + ex.getMessage());
            } finally {
                // Re-enable button
                loginButton.setEnabled(true);
                loginButton.setText("Login");
            }
        });

        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.setWidth("300px");

        this.add(title, usernameField, passwordField, loginButton);

        usernameField.focus();
    }

    private void showError(final String message) {
        LOG.trace("Showing error message: {}", message);
        final var notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setText(message);
        notification.setDuration(4000);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }

    private void showWarning(final String message) {
        LOG.trace("Showing warning message: {}", message);
        final var notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        notification.setText(message);
        notification.setDuration(3000);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }
}