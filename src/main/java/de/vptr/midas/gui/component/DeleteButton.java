package de.vptr.midas.gui.component;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class DeleteButton extends Button {
    private final static String DEFAULT_TOOLTIP = "Delete";
    private final static String DEFAULT_CONFIRMATION_TITLE = "Confirm Deletion";
    private final static String DEFAULT_CONFIRMATION_TEXT = "Are you sure you want to delete this item?";

    private final ComponentEventListener<ClickEvent<Button>> deleteAction;
    private String confirmationTitle = DEFAULT_CONFIRMATION_TITLE;
    private String confirmationText = DEFAULT_CONFIRMATION_TEXT;

    public DeleteButton(final ComponentEventListener<ClickEvent<Button>> deleteAction, final String tooltipText) {
        super("");
        this.deleteAction = deleteAction;
        this.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        this.setIcon(LineAwesomeIcon.TRASH_ALT_SOLID.create());
        this.setTooltipText(tooltipText != null ? tooltipText : DEFAULT_TOOLTIP);
        this.addClickListener(this::showConfirmationDialog);
    }

    public DeleteButton(final ComponentEventListener<ClickEvent<Button>> deleteAction) {
        this(deleteAction, null);
    }

    private void showConfirmationDialog(final ClickEvent<Button> event) {
        final var confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader(this.confirmationTitle);
        confirmDialog.setText(this.confirmationText);
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");

        confirmDialog.addConfirmListener(e -> {
            if (this.deleteAction != null) {
                this.deleteAction.onComponentEvent(event);
            }
        });

        confirmDialog.open();
    }

    public void setConfirmationTitle(final String confirmationTitle) {
        this.confirmationTitle = confirmationTitle != null ? confirmationTitle : DEFAULT_CONFIRMATION_TITLE;
    }

    public void setConfirmationText(final String confirmationText) {
        this.confirmationText = confirmationText != null ? confirmationText : DEFAULT_CONFIRMATION_TEXT;
    }
}
