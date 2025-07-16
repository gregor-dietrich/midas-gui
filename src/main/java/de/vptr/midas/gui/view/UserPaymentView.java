package de.vptr.midas.gui.view;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import de.vptr.midas.gui.dto.UserPayment;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.UserPaymentService;
import de.vptr.midas.gui.service.UserPaymentService.AuthenticationException;
import de.vptr.midas.gui.service.UserPaymentService.ServiceException;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "payments", layout = MainLayout.class)
public class UserPaymentView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(UserPaymentView.class);

    @Inject
    UserPaymentService paymentService;

    @Inject
    AuthService authService;

    private Grid<UserPayment> grid;
    private Button refreshButton;
    private Button createButton;
    private IntegerField limitField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button filterByDateButton;
    private Button filterRecentButton;

    private Dialog paymentDialog;
    private Binder<UserPayment> binder;
    private UserPayment currentPayment;

    public UserPaymentView() {
        this.setSizeFull();
        this.setSpacing(true);
        this.setPadding(true);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        LOG.info("UserPaymentView.beforeEnter - Starting view initialization");
        this.buildUI();
        this.loadPaymentsAsync();
        LOG.info("UserPaymentView.beforeEnter - View initialization completed");
    }

    private void loadPaymentsAsync() {
        LOG.info("Starting async payment loading");

        // Capture the auth header in the UI thread where VaadinSession is available
        final String authHeader;
        try {
            authHeader = this.authService.getBasicAuthHeader();
        } catch (final Exception e) {
            LOG.error("Failed to get auth header", e);
            this.getUI().ifPresent(ui -> ui.access(() -> {
                NotificationUtil.showError("Authentication failed");
            }));
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            LOG.info("Making REST call to load payments");
            try {
                return this.paymentService.getAllPayments(authHeader);
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading payments", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading payments", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading payments", e);
                throw new RuntimeException("Failed to load payments", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((payments, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading payments: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load payments: " + throwable.getMessage());
                            LOG.info("Successfully loaded 0 payments");
                        } else {
                            LOG.info("Successfully loaded {} payments", payments.size());
                            this.grid.setItems(payments);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("User Payments");
        this.add(header);

        // Filter controls
        final var filterLayout = this.createFilterLayout();
        this.add(filterLayout);

        // Action buttons
        final var buttonLayout = this.createButtonLayout();
        this.add(buttonLayout);

        // Grid
        this.createGrid();
        this.add(this.grid);

        // Payment dialog
        this.createPaymentDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var filterLayout = new HorizontalLayout();
        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setSpacing(true);

        // Recent payments filter
        this.limitField = new IntegerField("Recent Payments Limit");
        this.limitField.setValue(10);
        this.limitField.setMin(1);
        this.limitField.setMax(100);
        this.limitField.setWidth("150px");

        this.filterRecentButton = new Button("Load Recent", e -> this.loadRecentPayments());
        this.filterRecentButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Date range filter
        this.startDatePicker = new DatePicker("Start Date");
        this.startDatePicker.setWidth("150px");

        this.endDatePicker = new DatePicker("End Date");
        this.endDatePicker.setWidth("150px");

        this.filterByDateButton = new Button("Filter by Date", e -> this.filterByDateRange());
        this.filterByDateButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        filterLayout.add(this.limitField, this.filterRecentButton, this.startDatePicker, this.endDatePicker,
                this.filterByDateButton);
        return filterLayout;
    }

    private HorizontalLayout createButtonLayout() {
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        this.refreshButton = new Button("Refresh", e -> this.loadPaymentsAsync());
        this.refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.createButton = new Button("Create Payment", e -> this.openPaymentDialog(null));
        this.createButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        buttonLayout.add(this.refreshButton, this.createButton);
        return buttonLayout;
    }

    private void createGrid() {
        this.grid = new Grid<>(UserPayment.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns using lambda expressions
        this.grid.addColumn(payment -> payment.id).setHeader("ID").setWidth("80px").setFlexGrow(0);
        this.grid.addColumn(payment -> payment.user != null ? payment.user.getUsername() : "").setHeader("User")
                .setWidth("100px").setFlexGrow(0);
        this.grid.addColumn(payment -> payment.sourceId).setHeader("Source Account").setWidth("120px")
                .setFlexGrow(0);
        this.grid.addColumn(payment -> payment.targetId).setHeader("Target Account").setWidth("120px")
                .setFlexGrow(0);
        this.grid.addColumn(payment -> payment.amount != null ? payment.amount.toString() : "").setHeader("Amount")
                .setWidth("120px").setFlexGrow(0);
        this.grid.addColumn(payment -> payment.date).setHeader("Date").setWidth("120px").setFlexGrow(0);
        this.grid.addColumn(payment -> payment.comment).setHeader("Comment").setFlexGrow(1);
        this.grid.addColumn(payment -> payment.created).setHeader("Created").setWidth("120px").setFlexGrow(0);
        this.grid.addColumn(payment -> payment.lastEdit).setHeader("Last Edited").setWidth("100px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final UserPayment payment) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new Button("Edit", e -> this.openPaymentDialog(payment));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        final var deleteButton = new Button("Delete", e -> this.deletePayment(payment));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createPaymentDialog() {
        this.paymentDialog = new Dialog();
        this.paymentDialog.setWidth("400px");
        this.paymentDialog.setCloseOnEsc(true);
        this.paymentDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(UserPayment.class);
    }

    private void openPaymentDialog(final UserPayment payment) {
        this.paymentDialog.removeAll();
        this.currentPayment = payment != null ? payment : new UserPayment();

        final var title = new H3(payment != null ? "Edit Payment" : "Create Payment");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var sourceAccountField = new NumberField("Source Account ID");
        final var targetAccountField = new NumberField("Target Account ID");
        final var amountField = new BigDecimalField("Amount");
        final var dateField = new DatePicker("Payment Date");
        final var commentField = new TextField("Comment");

        this.binder.forField(sourceAccountField).bind(
                payment1 -> payment1.sourceId != null ? payment1.sourceId.doubleValue() : null,
                (payment1, value) -> payment1.sourceId = value != null ? value.longValue() : null);

        this.binder.forField(targetAccountField).bind(
                payment1 -> payment1.targetId != null ? payment1.targetId.doubleValue() : null,
                (payment1, value) -> payment1.targetId = value != null ? value.longValue() : null);

        this.binder.bind(amountField, payment1 -> payment1.amount, (payment1, value) -> payment1.amount = value);
        this.binder.bind(dateField, payment1 -> payment1.date, (payment1, value) -> payment1.date = value);
        this.binder.bind(commentField, payment1 -> payment1.comment, (payment1, value) -> payment1.comment = value);

        form.add(sourceAccountField, targetAccountField, amountField, dateField, commentField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.savePayment());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.paymentDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);

        this.paymentDialog.add(dialogLayout);

        // Load current payment data
        this.binder.readBean(this.currentPayment);

        this.paymentDialog.open();
    }

    private void savePayment() {
        try {
            this.binder.writeBean(this.currentPayment);

            if (this.currentPayment.id == null) {
                this.paymentService.createPayment(this.currentPayment);
                NotificationUtil.showSuccess("Payment created successfully");
            } else {
                this.paymentService.updatePayment(this.currentPayment);
                NotificationUtil.showSuccess("Payment updated successfully");
            }

            this.paymentDialog.close();
            this.loadPaymentsAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving payment: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving payment", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deletePayment(final UserPayment payment) {
        try {
            if (this.paymentService.deletePayment(payment.id)) {
                NotificationUtil.showSuccess("Payment deleted successfully");
                this.loadPaymentsAsync();
            } else {
                NotificationUtil.showError("Failed to delete payment");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting payment: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting payment", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void loadRecentPayments() {
        try {
            final int limit = this.limitField.getValue() != null ? this.limitField.getValue() : 10;
            final List<UserPayment> payments = this.paymentService.getRecentPayments(limit);
            this.grid.setItems(payments);
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error loading recent payments: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error loading recent payments", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void filterByDateRange() {
        final LocalDate startDate = this.startDatePicker.getValue();
        final LocalDate endDate = this.endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            NotificationUtil.showWarning("Please select both start and end dates");
            return;
        }

        if (startDate.isAfter(endDate)) {
            NotificationUtil.showWarning("Start date must be before end date");
            return;
        }

        try {
            final List<UserPayment> payments = this.paymentService.getPaymentsByDateRange(startDate, endDate);
            this.grid.setItems(payments);
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error filtering payments: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error filtering payments", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }
}
