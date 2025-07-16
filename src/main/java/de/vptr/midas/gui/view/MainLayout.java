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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

import de.vptr.midas.gui.component.ThemeToggleButton;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.HealthService;
import de.vptr.midas.gui.service.ThemeService;
import jakarta.inject.Inject;

public class MainLayout extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(MainLayout.class);

    private Button logoutButton;
    private Tabs navigationTabs;

    @Inject
    AuthService authService;

    @Inject
    HealthService healthService;

    @Inject
    ThemeService themeService;

    private HorizontalLayout topBar;
    private HorizontalLayout rightSide;
    private boolean initialized = false;

    private Tabs createNavigationTabs() {
        final var tabs = new Tabs();

        tabs.add(new Tab(new RouterLink("Home", GreetView.class)));
        tabs.add(new Tab(new RouterLink("Accounts", UserAccountView.class)));
        tabs.add(new Tab(new RouterLink("Payments", UserPaymentView.class)));

        return tabs;
    }

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
            this.hideNavigationTabs(); // Hide navigation tabs on error view
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
            this.hideNavigationTabs(); // Hide navigation tabs on login view
            return;
        }

        // Check authentication for all other views
        if (!this.authService.isAuthenticated()) {
            LOG.trace("User not authenticated, redirecting to login");
            event.forwardTo(LoginView.class);
            return;
        }

        // User is authenticated - show logout button and navigation tabs
        this.addLogoutButtonToTopBar();
        this.showNavigationTabs();

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
                        this.showNavigationTabs();
                    } else {
                        this.removeLogoutButtonFromTopBar();
                        this.hideNavigationTabs();
                    }
                }
            });
        } else {
            this.removeLogoutButtonFromTopBar();
            this.hideNavigationTabs();
        }
    }

    private void initializeLayout() {
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);

        this.createTopBar();
    }

    private void createTopBar() {
        this.topBar = new HorizontalLayout();
        this.topBar.setWidthFull();
        this.topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        this.topBar.setPadding(true);

        // Create navigation menu
        this.navigationTabs = createNavigationTabs();

        // Create right side components container
        this.rightSide = new HorizontalLayout();
        this.rightSide.setSpacing(true);

        final var themeToggle = new ThemeToggleButton(this.themeService);
        this.rightSide.add(themeToggle);

        this.topBar.add(this.rightSide);
        this.addComponentAsFirst(this.topBar);
    }

    private void showNavigationTabs() {
        if (this.navigationTabs != null && this.topBar != null) {
            if (!this.topBar.getChildren().anyMatch(component -> component == this.navigationTabs)) {
                this.topBar.removeAll();
                this.topBar.add(this.navigationTabs, this.rightSide);
                this.topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
            }
        }
    }

    private void hideNavigationTabs() {
        if (this.navigationTabs != null && this.topBar != null) {
            this.topBar.removeAll();
            this.topBar.add(this.rightSide);
            this.topBar.setJustifyContentMode(JustifyContentMode.END);
        }
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
        if (this.rightSide != null) {
            // Remove existing logout button if present
            if (this.logoutButton != null) {
                this.rightSide.remove(this.logoutButton);
            }

            // Create new logout button
            this.logoutButton = this.createLogoutButton();

            // Add logout button
            final var componentCount = this.rightSide.getComponentCount();
            if (componentCount > 0) {
                this.rightSide.addComponentAtIndex(componentCount - 1, this.logoutButton);
            } else {
                this.rightSide.add(this.logoutButton);
            }
        }
    }

    private void removeLogoutButtonFromTopBar() {
        if (this.logoutButton != null && this.rightSide != null) {
            this.rightSide.remove(this.logoutButton);
            this.logoutButton = null;
        }
    }
}