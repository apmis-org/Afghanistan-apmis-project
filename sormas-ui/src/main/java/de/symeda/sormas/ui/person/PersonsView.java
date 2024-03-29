package de.symeda.sormas.ui.person;

import java.util.HashMap;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.CountryHelper;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.person.PersonAssociation;
import de.symeda.sormas.api.person.PersonCriteria;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.utils.AbstractView;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.FilteredGrid;
import de.symeda.sormas.ui.utils.LayoutUtil;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class PersonsView extends AbstractView {

	public static final String VIEW_NAME = "persons";

	private final PersonCriteria criteria;
	private final FilteredGrid<?, PersonCriteria> grid;
	private HashMap<Button, String> associationButtons;
	private Button activeAssociationButton;
	private PersonFilterForm filterForm;

	public PersonsView() {
		super(VIEW_NAME);

		// Avoid calling ALL associations at view start because the query tends to take long time
		PersonCriteria defaultCriteria = new PersonCriteria().personAssociation(PersonAssociation.CASE);
		criteria = ViewModelProviders.of(PersonsView.class).get(PersonCriteria.class, defaultCriteria);
		grid = new PersonGrid(criteria);
		final VerticalLayout gridLayout = new VerticalLayout();
		gridLayout.addComponent(createFilterBar());
		final HorizontalLayout associationFilterBar = createAssociationFilterBar();
		gridLayout.addComponent(associationFilterBar);
		gridLayout.setComponentAlignment(associationFilterBar, Alignment.MIDDLE_RIGHT);
		gridLayout.addComponent(grid);

		gridLayout.setMargin(true);
		gridLayout.setSpacing(false);
		gridLayout.setSizeFull();
		gridLayout.setExpandRatio(grid, 1);
		gridLayout.setStyleName("crud-main-layout");

		if (FacadeProvider.getGeocodingFacade().isEnabled()) {
			Button setMissingCoordinatesButton = ButtonHelper.createIconButton(
				I18nProperties.getCaption(Captions.personsSetMissingGeoCoordinates),
				VaadinIcons.MAP_MARKER,
				e -> showMissingCoordinatesPopUp(),
				ValoTheme.BUTTON_PRIMARY);
			addHeaderComponent(setMissingCoordinatesButton);
		}

		grid.getDataProvider().addDataProviderListener(e -> updateAssociationButtons());

		addComponent(gridLayout);
	}

	private void showMissingCoordinatesPopUp() {

		Label popupDescLabel = new Label(I18nProperties.getString(Strings.confirmationSetMissingGeoCoordinates));
		CheckBox popupCheckbox = new CheckBox(I18nProperties.getCaption(Captions.personsReplaceGeoCoordinates));
		popupCheckbox.setValue(false);

		VerticalLayout popupLayout = new VerticalLayout();
		popupLayout.setMargin(false);
		popupLayout.setSpacing(true);
		popupDescLabel.setWidth(100, Unit.PERCENTAGE);
		popupCheckbox.setWidth(100, Unit.PERCENTAGE);
		popupLayout.addComponent(popupDescLabel);
		popupLayout.addComponent(popupCheckbox);

		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getCaption(Captions.personsSetMissingGeoCoordinates),
			popupLayout,
			I18nProperties.getCaption(Captions.actionContinue),
			I18nProperties.getCaption(Captions.actionCancel),
			640,
			confirmed -> {
				if (confirmed) {
					long changedPersons = FacadeProvider.getPersonFacade().setMissingGeoCoordinates(popupCheckbox.getValue());
					Notification.show(
						I18nProperties.getCaption(Captions.personsUpdated),
						String.format(I18nProperties.getString(Strings.notificationPersonsUpdated), changedPersons),
						Notification.Type.TRAY_NOTIFICATION);
				}
			});
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
		String params = event.getParameters().trim();
		if (params.startsWith("?")) {
			params = params.substring(1);
			criteria.fromUrlParams(params);
		}

		updateFilterComponents();
	}

	public void updateFilterComponents() {
		// TODO replace with Vaadin 8 databinding
		applyingCriteria = true;

		updateAssociationButtons();

		filterForm.setValue(criteria);

		applyingCriteria = false;
	}

	private void updateAssociationButtons() {

		associationButtons.keySet().forEach(b -> {
			CssStyles.style(b, CssStyles.BUTTON_FILTER_LIGHT);
			b.setCaption(associationButtons.get(b));
			if (b.getData() == criteria.getPersonAssociation()) {
				activeAssociationButton = b;
			}
		});
		CssStyles.removeStyles(activeAssociationButton, CssStyles.BUTTON_FILTER_LIGHT);
		if (activeAssociationButton != null) {
			activeAssociationButton.setCaption(
				associationButtons.get(activeAssociationButton) + LayoutUtil.spanCss(CssStyles.BADGE, String.valueOf(grid.getItemCount())));
		}
	}

	public VerticalLayout createFilterBar() {
		VerticalLayout filterLayout = new VerticalLayout();
		filterLayout.setSpacing(false);
		filterLayout.setMargin(false);
		filterLayout.setWidth(100, Unit.PERCENTAGE);

		filterForm = new PersonFilterForm();
		filterForm.addValueChangeListener(e -> {
			if (!filterForm.hasFilter()) {
				navigateTo(null);
			}
		});
		filterForm.addResetHandler(e -> {
			ViewModelProviders.of(PersonsView.class).remove(PersonCriteria.class);
			navigateTo(null, true);
		});
		filterForm.addApplyHandler(e -> ((PersonGrid) grid).reload());
		filterLayout.addComponent(filterForm);

		return filterLayout;
	}

	public HorizontalLayout createAssociationFilterBar() {
		HorizontalLayout associationFilterLayout = new HorizontalLayout();
		associationFilterLayout.setSpacing(true);
		associationFilterLayout.setMargin(false);
		associationFilterLayout.setWidth(100, Unit.PERCENTAGE);
		associationFilterLayout.addStyleName(CssStyles.VSPACE_3);

		associationButtons = new HashMap<>();
		for (PersonAssociation association : PersonAssociation.values()) {
			if (association == PersonAssociation.IMMUNIZATION
				&& FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.IMMUNIZATION_MANAGEMENT)
				|| association == PersonAssociation.TRAVEL_ENTRY
					&& (FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.TRAVEL_ENTRIES)
						|| !FacadeProvider.getConfigFacade().isConfiguredCountry(CountryHelper.COUNTRY_CODE_GERMANY))
				|| association == PersonAssociation.CONTACT
					&& FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.CONTACT_TRACING)
				|| association == PersonAssociation.CASE
					&& FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.CASE_SURVEILANCE)
				|| association == PersonAssociation.EVENT_PARTICIPANT
					&& FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.EVENT_SURVEILLANCE)) {
				continue;
			}

			Button associationButton = ButtonHelper.createButton(association.toString(), e -> {
				if (!UserProvider.getCurrent().hasUserRole(UserRole.NATIONAL_USER) && association == PersonAssociation.ALL) {
					Label contentLabel = new Label(I18nProperties.getString(Strings.confirmationSeeAllPersons));
					VaadinUiUtil.showConfirmationPopup(
						I18nProperties.getString(Strings.headingSeeAllPersons),
						contentLabel,
						I18nProperties.getString(Strings.yes),
						I18nProperties.getString(Strings.no),
						640,
						ee -> {
							if (ee.booleanValue() == true) {
								criteria.personAssociation(association);
								navigateTo(criteria);
							}
						});
				} else {
					criteria.personAssociation(association);
					navigateTo(criteria);
				}
			}, ValoTheme.BUTTON_BORDERLESS, CssStyles.BUTTON_FILTER, CssStyles.BUTTON_FILTER_LIGHT);
			associationButton.setData(association);
			associationButton.setCaptionAsHtml(true);

			associationFilterLayout.addComponent(associationButton);
			associationFilterLayout.setComponentAlignment(associationButton, Alignment.MIDDLE_LEFT);
			associationButtons.put(associationButton, association.toString());
		}

		Label emptyLabel = new Label("");
		associationFilterLayout.addComponent(emptyLabel);
		associationFilterLayout.setComponentAlignment(emptyLabel, Alignment.MIDDLE_RIGHT);
		associationFilterLayout.setExpandRatio(emptyLabel, 1);

		return associationFilterLayout;
	}
}
