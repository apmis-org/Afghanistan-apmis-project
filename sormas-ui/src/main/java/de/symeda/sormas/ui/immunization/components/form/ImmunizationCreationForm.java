package de.symeda.sormas.ui.immunization.components.form;

import static de.symeda.sormas.ui.utils.CssStyles.ERROR_COLOR_PRIMARY;
import static de.symeda.sormas.ui.utils.CssStyles.FORCE_CAPTION;
import static de.symeda.sormas.ui.utils.CssStyles.H3;
import static de.symeda.sormas.ui.utils.CssStyles.SOFT_REQUIRED;
import static de.symeda.sormas.ui.utils.CssStyles.VSPACE_3;
import static de.symeda.sormas.ui.utils.CssStyles.style;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLoc;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRow;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRowLocs;

import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.ui.Label;
import com.vaadin.v7.data.validator.EmailValidator;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.TextField;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Month;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.immunization.ImmunizationDto;
import de.symeda.sormas.api.immunization.ImmunizationManagementStatus;
import de.symeda.sormas.api.immunization.ImmunizationStatus;
import de.symeda.sormas.api.immunization.MeansOfImmunization;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PresentCondition;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.InfrastructureFieldsHelper;
import de.symeda.sormas.ui.utils.PhoneNumberValidator;

public class ImmunizationCreationForm extends AbstractEditForm<ImmunizationDto> {

	private static final String OVERWRITE_IMMUNIZATION_MANAGEMENT_STATUS = "overwriteImmunizationManagementStatus";
	private static final String RESPONSIBLE_JURISDICTION_HEADING_LOC = "responsibleJurisdictionHeadingLoc";

	//@formatter:off
	private static final String HTML_LAYOUT = fluidRowLocs(ImmunizationDto.REPORT_DATE, ImmunizationDto.EXTERNAL_ID)
		+ fluidRow(fluidColumnLoc(6, 0, ImmunizationDto.DISEASE))
		+ fluidRowLocs(ImmunizationDto.MEANS_OF_IMMUNIZATION, ImmunizationDto.MEANS_OF_IMMUNIZATION_DETAILS)
		+ fluidRowLocs(OVERWRITE_IMMUNIZATION_MANAGEMENT_STATUS)
		+ fluidRowLocs(ImmunizationDto.MANAGEMENT_STATUS, ImmunizationDto.IMMUNIZATION_STATUS)
		+ fluidRowLocs(RESPONSIBLE_JURISDICTION_HEADING_LOC)
		+ fluidRowLocs(ImmunizationDto.RESPONSIBLE_REGION, ImmunizationDto.RESPONSIBLE_DISTRICT, ImmunizationDto.RESPONSIBLE_COMMUNITY)
		+ fluidRowLocs(ImmunizationDto.START_DATE, ImmunizationDto.END_DATE)
		+ fluidRow(fluidColumnLoc(6, 0, ImmunizationDto.NUMBER_OF_DOSES))
		+ fluidRowLocs(PersonDto.FIRST_NAME, PersonDto.LAST_NAME)
		+ fluidRow(fluidRowLocs(PersonDto.BIRTH_DATE_YYYY, PersonDto.BIRTH_DATE_MM, PersonDto.BIRTH_DATE_DD),
		fluidRowLocs(PersonDto.SEX))
		+ fluidRowLocs(PersonDto.NATIONAL_HEALTH_ID, PersonDto.PASSPORT_NUMBER)
		+ fluidRowLocs(PersonDto.PRESENT_CONDITION, SymptomsDto.ONSET_DATE)
		+ fluidRowLocs(PersonDto.PHONE, PersonDto.EMAIL_ADDRESS);
	//@formatter:on

	private ComboBox birthDateDay;

	public ImmunizationCreationForm() {
		super(
			ImmunizationDto.class,
			ImmunizationDto.I18N_PREFIX,
			FieldVisibilityCheckers.withCountry(FacadeProvider.getConfigFacade().getCountryLocale()));
		setWidth(720, Unit.PIXELS);
		hideValidationUntilNextCommit();
	}

	@Override
	protected String createHtmlLayout() {
		return HTML_LAYOUT;
	}

	@Override
	protected void addFields() {
		addField(ImmunizationDto.REPORT_DATE, DateField.class);

		TextField externalIdField = addField(ImmunizationDto.EXTERNAL_ID, TextField.class);
		style(externalIdField, ERROR_COLOR_PRIMARY);

		addDiseaseField(ImmunizationDto.DISEASE, false);

		ComboBox meansOfImmunizationField = addField(ImmunizationDto.MEANS_OF_IMMUNIZATION, ComboBox.class);
		addField(ImmunizationDto.MEANS_OF_IMMUNIZATION_DETAILS, TextField.class);

		CheckBox overwriteImmunizationManagementStatus = addCustomField(OVERWRITE_IMMUNIZATION_MANAGEMENT_STATUS, Boolean.class, CheckBox.class);
		overwriteImmunizationManagementStatus.addStyleName(VSPACE_3);

		ComboBox managementStatusField = addCustomField(ImmunizationDto.MANAGEMENT_STATUS, ImmunizationManagementStatus.class, ComboBox.class);
		managementStatusField.setValue(ImmunizationManagementStatus.SCHEDULED);
		managementStatusField.setEnabled(false);

		ComboBox immunizationStatusField = addCustomField(ImmunizationDto.IMMUNIZATION_STATUS, ImmunizationStatus.class, ComboBox.class);
		immunizationStatusField.setValue(ImmunizationStatus.PENDING);
		immunizationStatusField.setEnabled(false);

		Label jurisdictionHeadingLabel = new Label(I18nProperties.getString(Strings.headingResponsibleJurisdiction));
		jurisdictionHeadingLabel.addStyleName(H3);
		getContent().addComponent(jurisdictionHeadingLabel, RESPONSIBLE_JURISDICTION_HEADING_LOC);

		ComboBox responsibleRegion = addField(ImmunizationDto.RESPONSIBLE_REGION);
		responsibleRegion.setRequired(true);
		ComboBox responsibleDistrictCombo = addField(ImmunizationDto.RESPONSIBLE_DISTRICT);
		responsibleDistrictCombo.setRequired(true);
		ComboBox responsibleCommunityCombo = addField(ImmunizationDto.RESPONSIBLE_COMMUNITY);
		responsibleCommunityCombo.setNullSelectionAllowed(true);
		responsibleCommunityCombo.addStyleName(SOFT_REQUIRED);

		InfrastructureFieldsHelper.initInfrastructureFields(responsibleRegion, responsibleDistrictCombo, responsibleCommunityCombo);

		addField(ImmunizationDto.START_DATE, DateField.class);
		addField(ImmunizationDto.END_DATE, DateField.class);

		addField(ImmunizationDto.NUMBER_OF_DOSES, TextField.class);

		addCustomField(PersonDto.FIRST_NAME, String.class, TextField.class);
		addCustomField(PersonDto.LAST_NAME, String.class, TextField.class);
		addCustomField(PersonDto.NATIONAL_HEALTH_ID, String.class, TextField.class);
		addCustomField(PersonDto.PASSPORT_NUMBER, String.class, TextField.class);

		birthDateDay = addCustomField(PersonDto.BIRTH_DATE_DD, Integer.class, ComboBox.class);
		// @TODO: Done for nullselection Bug, fixed in Vaadin 7.7.3
		birthDateDay.setNullSelectionAllowed(true);
		birthDateDay.addStyleName(FORCE_CAPTION);
		birthDateDay.setInputPrompt(I18nProperties.getString(Strings.day));
		ComboBox birthDateMonth = addCustomField(PersonDto.BIRTH_DATE_MM, Integer.class, ComboBox.class);
		// @TODO: Done for nullselection Bug, fixed in Vaadin 7.7.3
		birthDateMonth.setNullSelectionAllowed(true);
		birthDateMonth.addItems(DateHelper.getMonthsInYear());
		birthDateMonth.setPageLength(12);
		birthDateMonth.addStyleName(FORCE_CAPTION);
		birthDateMonth.setInputPrompt(I18nProperties.getString(Strings.month));
		setItemCaptionsForMonths(birthDateMonth);
		ComboBox birthDateYear = addCustomField(PersonDto.BIRTH_DATE_YYYY, Integer.class, ComboBox.class);
		birthDateYear.setCaption(I18nProperties.getPrefixCaption(PersonDto.I18N_PREFIX, PersonDto.BIRTH_DATE));
		// @TODO: Done for nullselection Bug, fixed in Vaadin 7.7.3
		birthDateYear.setNullSelectionAllowed(true);
		birthDateYear.addItems(DateHelper.getYearsToNow());
		birthDateYear.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ID_TOSTRING);
		birthDateYear.setInputPrompt(I18nProperties.getString(Strings.year));
		birthDateDay.addValidator(
			e -> ControllerProvider.getPersonController()
				.validateBirthDate((Integer) birthDateYear.getValue(), (Integer) birthDateMonth.getValue(), (Integer) e));
		birthDateMonth.addValidator(
			e -> ControllerProvider.getPersonController()
				.validateBirthDate((Integer) birthDateYear.getValue(), (Integer) e, (Integer) birthDateDay.getValue()));
		birthDateYear.addValidator(
			e -> ControllerProvider.getPersonController()
				.validateBirthDate((Integer) e, (Integer) birthDateMonth.getValue(), (Integer) birthDateDay.getValue()));

		// Update the list of days according to the selected month and year
		birthDateYear.addValueChangeListener(e -> {
			updateListOfDays((Integer) e.getProperty().getValue(), (Integer) birthDateMonth.getValue());
			birthDateMonth.markAsDirty();
			birthDateDay.markAsDirty();
		});
		birthDateMonth.addValueChangeListener(e -> {
			updateListOfDays((Integer) birthDateYear.getValue(), (Integer) e.getProperty().getValue());
			birthDateYear.markAsDirty();
			birthDateDay.markAsDirty();
		});
		birthDateDay.addValueChangeListener(e -> {
			birthDateYear.markAsDirty();
			birthDateMonth.markAsDirty();
		});

		ComboBox sex = addCustomField(PersonDto.SEX, Sex.class, ComboBox.class);
		sex.setCaption(I18nProperties.getCaption(Captions.Person_sex));
		ComboBox presentCondition = addCustomField(PersonDto.PRESENT_CONDITION, PresentCondition.class, ComboBox.class);
		presentCondition.setCaption(I18nProperties.getCaption(Captions.Person_presentCondition));

		addCustomField(
			SymptomsDto.ONSET_DATE,
			Date.class,
			DateField.class,
			I18nProperties.getPrefixCaption(SymptomsDto.I18N_PREFIX, SymptomsDto.ONSET_DATE));

		TextField phone = addCustomField(PersonDto.PHONE, String.class, TextField.class);
		phone.setCaption(I18nProperties.getCaption(Captions.Person_phone));
		TextField email = addCustomField(PersonDto.EMAIL_ADDRESS, String.class, TextField.class);
		email.setCaption(I18nProperties.getCaption(Captions.Person_emailAddress));

		phone.addValidator(new PhoneNumberValidator(I18nProperties.getValidationError(Validations.validPhoneNumber, phone.getCaption())));
		email.addValidator(new EmailValidator(I18nProperties.getValidationError(Validations.validEmailAddress, email.getCaption())));

		// Set initial visibilities & accesses
		initializeVisibilitiesAndAllowedVisibilities();

		setRequired(
			true,
			ImmunizationDto.REPORT_DATE,
			ImmunizationDto.DISEASE,
			ImmunizationDto.MEANS_OF_IMMUNIZATION,
			ImmunizationDto.START_DATE,
			PersonDto.FIRST_NAME,
			PersonDto.LAST_NAME,
			PersonDto.SEX);

		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			Collections.singletonList(ImmunizationDto.MEANS_OF_IMMUNIZATION_DETAILS),
			ImmunizationDto.MEANS_OF_IMMUNIZATION,
			Collections.singletonList(MeansOfImmunization.OTHER),
			true);

		overwriteImmunizationManagementStatus.addValueChangeListener(valueChangeEvent -> {
			boolean selectedValue = (boolean) valueChangeEvent.getProperty().getValue();
			managementStatusField.setEnabled(selectedValue);
		});

		meansOfImmunizationField.addValueChangeListener(valueChangeEvent -> {
			MeansOfImmunization meansOfImmunization = (MeansOfImmunization) valueChangeEvent.getProperty().getValue();
			if (MeansOfImmunization.RECOVERY.equals(meansOfImmunization) || MeansOfImmunization.OTHER.equals(meansOfImmunization)) {
				managementStatusField.setValue(ImmunizationManagementStatus.COMPLETED);
			} else {
				managementStatusField.setValue(ImmunizationManagementStatus.SCHEDULED);
			}
		});

		managementStatusField.addValueChangeListener(valueChangeEvent -> {
			ImmunizationManagementStatus managementStatusValue = (ImmunizationManagementStatus) valueChangeEvent.getProperty().getValue();
			switch (managementStatusValue) {
			case SCHEDULED:
			case ONGOING:
				immunizationStatusField.setValue(ImmunizationStatus.PENDING);
				break;
			case COMPLETED:
				immunizationStatusField.setValue(ImmunizationStatus.ACQUIRED);
				break;
			case CANCELED:
				immunizationStatusField.setValue(ImmunizationStatus.NOT_ACQUIRED);
				break;
			default:
				break;
			}
		});
	}

	private void setItemCaptionsForMonths(AbstractSelect months) {

		months.setItemCaption(1, I18nProperties.getEnumCaption(Month.JANUARY));
		months.setItemCaption(2, I18nProperties.getEnumCaption(Month.FEBRUARY));
		months.setItemCaption(3, I18nProperties.getEnumCaption(Month.MARCH));
		months.setItemCaption(4, I18nProperties.getEnumCaption(Month.APRIL));
		months.setItemCaption(5, I18nProperties.getEnumCaption(Month.MAY));
		months.setItemCaption(6, I18nProperties.getEnumCaption(Month.JUNE));
		months.setItemCaption(7, I18nProperties.getEnumCaption(Month.JULY));
		months.setItemCaption(8, I18nProperties.getEnumCaption(Month.AUGUST));
		months.setItemCaption(9, I18nProperties.getEnumCaption(Month.SEPTEMBER));
		months.setItemCaption(10, I18nProperties.getEnumCaption(Month.OCTOBER));
		months.setItemCaption(11, I18nProperties.getEnumCaption(Month.NOVEMBER));
		months.setItemCaption(12, I18nProperties.getEnumCaption(Month.DECEMBER));
	}

	private void updateListOfDays(Integer selectedYear, Integer selectedMonth) {

		Integer currentlySelected = (Integer) birthDateDay.getValue();
		birthDateDay.removeAllItems();
		birthDateDay.addItems(DateHelper.getDaysInMonth(selectedMonth, selectedYear));
		if (birthDateDay.containsId(currentlySelected)) {
			birthDateDay.setValue(currentlySelected);
		}
	}

	public PersonDto getPerson() {
		PersonDto person = PersonDto.build();

		person.setFirstName((String) getField(PersonDto.FIRST_NAME).getValue());
		person.setLastName((String) getField(PersonDto.LAST_NAME).getValue());
		person.setBirthdateDD((Integer) getField(PersonDto.BIRTH_DATE_DD).getValue());
		person.setBirthdateMM((Integer) getField(PersonDto.BIRTH_DATE_MM).getValue());
		person.setBirthdateYYYY((Integer) getField(PersonDto.BIRTH_DATE_YYYY).getValue());
		person.setSex((Sex) getField(PersonDto.SEX).getValue());
		person.setPresentCondition((PresentCondition) getField(PersonDto.PRESENT_CONDITION).getValue());

		String phone = (String) getField(PersonDto.PHONE).getValue();
		if (StringUtils.isNotEmpty(phone)) {
			person.setPhone(phone);
		}

		String emailAddress = (String) getField(PersonDto.EMAIL_ADDRESS).getValue();
		if (StringUtils.isNotEmpty(emailAddress)) {
			person.setEmailAddress(emailAddress);
		}

		person.setNationalHealthId((String) getField(PersonDto.NATIONAL_HEALTH_ID).getValue());
		person.setPassportNumber((String) getField(PersonDto.PASSPORT_NUMBER).getValue());

		return person;
	}

	@Override
	public ImmunizationDto getValue() {
		ImmunizationDto immunizationDto = super.getValue();
		immunizationDto.setImmunizationManagementStatus((ImmunizationManagementStatus) getField(ImmunizationDto.MANAGEMENT_STATUS).getValue());
		immunizationDto.setImmunizationStatus((ImmunizationStatus) getField(ImmunizationDto.IMMUNIZATION_STATUS).getValue());
		return immunizationDto;
	}
}
