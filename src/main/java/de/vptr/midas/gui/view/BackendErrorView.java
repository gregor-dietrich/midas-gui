package de.vptr.midas.gui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.vptr.midas.gui.service.ThemeService;
import jakarta.inject.Inject;

@Route(value = "backend-error", layout = MainLayout.class)
@PageTitle("Midas - Backend Unavailable")
public class BackendErrorView extends VerticalLayout {

    @Inject
    ThemeService themeService;

    public BackendErrorView() {
        this.setSizeFull();
        this.setAlignItems(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);

        final var errorIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
        errorIcon.setSize("64px");
        errorIcon.setColor("var(--lumo-error-color)");

        final var title = new H1("Backend Service Unavailable");
        title.getStyle().set("color", "var(--lumo-error-color)");

        final var subtitle = new H3("Cannot connect to the backend service. 🙁");

        final var explanation = new Paragraph(
                "The application cannot connect to the backend service. This might be due to network issues or the service being temporarily unavailable.");
        explanation.getStyle().set("text-align", "center");
        explanation.getStyle().set("max-width", "600px");

        final var buttonLayout = new HorizontalLayout();

        final var retryButton = new Button("Retry", e -> {
            this.getUI().ifPresent(ui -> ui.navigate(""));
        });
        retryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var retryIcon = new Icon(VaadinIcon.REFRESH);
        retryButton.setIcon(retryIcon);

        buttonLayout.add(retryButton);

        this.add(errorIcon, title, subtitle, explanation, buttonLayout);
        this.setSpacing(true);
        this.setPadding(true);
    }
}
