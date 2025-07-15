package de.vptr.midas.gui.view;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;

import de.vptr.midas.gui.component.ThemeToggleButton;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.HealthService;
import de.vptr.midas.gui.service.ThemeService;
import jakarta.inject.Inject;

public class MainLayout extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(MainLayout.class);

    private Button logoutButton;

    @Inject
    AuthService authService;

    @Inject
    HealthService healthService;

    @Inject
    ThemeService themeService;

    private HorizontalLayout topBar;
    private boolean initialized = false;

    /**
     * Get the shared top bar for views that need to add additional components
     */
    public HorizontalLayout getTopBar() {
        return this.topBar;
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.updateLogoutButtonVisibility();
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        if (!this.initialized) {
            this.initializeLayout();
            this.initialized = true;
        }

        // Apply current theme on every navigation
        this.themeService.applyTheme(this.themeService.getCurrentTheme());

        final var targetView = event.getNavigationTarget();

        LOG.trace("MainLayout.beforeEnter - Target: {}", targetView.getSimpleName());

        // Skip all checks for the backend error view
        if (targetView == BackendErrorView.class) {
            LOG.trace("Navigating to backend error view, skipping all checks");
            this.removeLogoutButtonFromTopBar(); // Hide logout button on error view
            return;
        }

        // Check backend availability with timeout protection
        try {
            final var healthCheck = CompletableFuture.supplyAsync(() -> this.healthService.isBackendAvailable());

            // Wait maximum 3 seconds for health check
            final var isAvailable = healthCheck.get(3, TimeUnit.SECONDS);

            if (!isAvailable) {
                LOG.error("Backend unavailable, redirecting to error page");
                event.forwardTo(BackendErrorView.class);
                return;
            }

        } catch (final Exception e) {
            LOG.error("Backend health check failed or timed out: {}", e.getMessage());
            event.forwardTo(BackendErrorView.class);
            return;
        }

        // Skip auth check for the login view
        if (targetView == LoginView.class) {
            LOG.trace("Navigating to login view, skipping authentication check");
            this.removeLogoutButtonFromTopBar(); // Hide logout button on login view
            return;
        }

        // Check authentication for all other views
        if (!this.authService.isAuthenticated()) {
            LOG.trace("User not authenticated, redirecting to login");
            event.forwardTo(LoginView.class);
            return;
        }

        // User is authenticated - show logout button
        this.addLogoutButtonToTopBar();

        LOG.trace("All checks passed for {}", targetView.getSimpleName());
    }

    private void updateLogoutButtonVisibility() {
        if (this.authService.isAuthenticated()) {
            // Check current route to determine if logout button should be shown
            this.getUI().ifPresent(ui -> {
                final var location = ui.getInternals().getActiveViewLocation();
                if (location != null) {
                    final var path = location.getPath();
                    // Don't show logout button on login or error views
                    if (!"login".equals(path) && !"backend-error".equals(path)) {
                        this.addLogoutButtonToTopBar();
                    } else {
                        this.removeLogoutButtonFromTopBar();
                    }
                }
            });
        } else {
            this.removeLogoutButtonFromTopBar();
        }
    }

    private void initializeLayout() {
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);

        this.createTopBar();
    }

    private void createTopBar() {
        final var themeToggle = new ThemeToggleButton(this.themeService);

        this.topBar = new HorizontalLayout();
        this.topBar.setWidthFull();
        this.topBar.setJustifyContentMode(JustifyContentMode.END);
        this.topBar.setPadding(true);
        this.topBar.add(themeToggle);

        this.addComponentAsFirst(this.topBar);
    }

    private Button createLogoutButton() {
        final var button = new Button("", e -> {
            this.authService.logout();
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.setIcon(new Icon(VaadinIcon.SIGN_OUT));
        button.setTooltipText("Logout");
        return button;
    }

    private void addLogoutButtonToTopBar() {
        if (this.topBar != null) {
            // Remove existing logout button if present
            if (this.logoutButton != null) {
                this.topBar.remove(this.logoutButton);
            }

            // Create new logout button
            this.logoutButton = this.createLogoutButton();

            // Add logout button
            final var componentCount = this.topBar.getComponentCount();
            if (componentCount > 0) {
                this.topBar.addComponentAtIndex(componentCount - 1, this.logoutButton);
            } else {
                this.topBar.add(this.logoutButton);
            }
        }
    }

    private void removeLogoutButtonFromTopBar() {
        if (this.logoutButton != null && this.topBar != null) {
            this.topBar.remove(this.logoutButton);
            this.logoutButton = null;
        }
    }
}