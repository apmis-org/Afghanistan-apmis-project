package de.symeda.sormas.api.contact;

import java.util.Date;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.person.ApproximateAgeType;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.person.SymptomJournalStatus;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.PersonalData;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.pseudonymization.Pseudonymizer;
import de.symeda.sormas.api.utils.pseudonymization.valuepseudonymizers.PostalCodePseudonymizer;

public class ContactIndexDetailedDto extends ContactIndexDto {

	private static final long serialVersionUID = 577830364406605991L;

	public static final String SEX = "sex";
	public static final String APPROXIMATE_AGE = "approximateAge";
	public static final String DISTRICT_NAME = "districtName";
	public static final String CITY = "city";
	public static final String STREET = "street";
	public static final String HOUSE_NUMBER = "houseNumber";
	public static final String ADDITIONAL_INFORMATION = "additionalInformation";
	public static final String POSTAL_CODE = "postalCode";
	public static final String PHONE = "phone";
	public static final String REPORTING_USER = "reportingUser";
	public static final String LATEST_EVENT_ID = "latestEventId";
	public static final String LATEST_EVENT_TITLE = "latestEventTitle";

	private String approximateAge;
	@PersonalData
	@SensitiveData
	private String city;
	@PersonalData
	@SensitiveData
	private String street;
	@PersonalData
	@SensitiveData
	private String houseNumber;
	@PersonalData
	@SensitiveData
	private String additionalInformation;
	@PersonalData
	@SensitiveData
	@Pseudonymizer(PostalCodePseudonymizer.class)
	private String postalCode;
	@SensitiveData
	private String phone;
	private UserReferenceDto reportingUser;
	private String latestEventId;
	private String latestEventTitle;
	private Long eventCount;

	//@formatter:off
	public ContactIndexDetailedDto(long id, String uuid, String personFirstName, String personLastName, Integer approximateAge, ApproximateAgeType approximateAgeType,
								   Integer birthdateDD, Integer birthdateMM, Integer birthdateYYYY, Sex sex, String cazeUuid,
								   Disease disease, String diseaseDetails, String caseFirstName, String caseLastName, String regionUuid, String regionName,
								   String districtUuid, String districtName, String communityUuid, Date lastContactDate, Date creationDate, ContactCategory contactCategory,
								   ContactProximity contactProximity, ContactClassification contactClassification, ContactStatus contactStatus, Float completeness,
								   FollowUpStatus followUpStatus, Date followUpUntil, SymptomJournalStatus symptomJournalStatus, Vaccination vaccination, String contactOfficerUuid, String reportingUserUuid, Date reportDateTime,
								   CaseClassification caseClassification, String caseReportingUserUid,
								   String caseResponsibleRegionUuid, String caseResponsibleDistrictUid, String caseResponsibleCommunityUid,String caseRegionUuid, String caseRegionName, String caseDistrictUuid,
								   String caseDistrictName, String caseCommunityUuid, String caseHealthFacilityUuid, String casePointOfEntryUuid,
								   Date changeDate, // XXX: unused, only here for TypedQuery mapping
								   String externalID, String externalToken,
								   String city, String street, String houseNumber, String additionalInformation, String postalCode, String phone,
								   String reportingUserFirstName, String reportingUserLastName, int visitCount
	) {
	//@formatter:on

		//@formatter:off
		super(id, uuid, personFirstName, personLastName, approximateAge, approximateAgeType,
			birthdateDD, birthdateMM, birthdateYYYY, sex, cazeUuid, disease, diseaseDetails, caseFirstName, caseLastName, regionUuid, regionName, districtUuid, districtName, communityUuid,
			lastContactDate, creationDate, contactCategory, contactProximity, contactClassification, contactStatus, completeness, followUpStatus, followUpUntil,
			symptomJournalStatus, vaccination, contactOfficerUuid, reportingUserUuid, reportDateTime, caseClassification,
			caseReportingUserUid, caseResponsibleRegionUuid, caseResponsibleDistrictUid, caseResponsibleCommunityUid, caseRegionUuid, caseRegionName, caseDistrictUuid, caseDistrictName, caseCommunityUuid, caseHealthFacilityUuid, casePointOfEntryUuid, changeDate, externalID, externalToken, visitCount);

		//@formatter:on

		this.approximateAge = ApproximateAgeType.ApproximateAgeHelper.formatApproximateAge(approximateAge, approximateAgeType);
		this.city = city;
		this.street = street;
		this.houseNumber = houseNumber;
		this.additionalInformation = additionalInformation;
		this.postalCode = postalCode;
		this.phone = phone;
		this.reportingUser = new UserReferenceDto(reportingUserUuid, reportingUserFirstName, reportingUserLastName, null);
	}

	public String getApproximateAge() {
		return approximateAge;
	}

	public void setApproximateAge(String approximateAge) {
		this.approximateAge = approximateAge;
	}

	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getPhone() {
		return phone;
	}

	public UserReferenceDto getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(UserReferenceDto reportingUser) {
		this.reportingUser = reportingUser;
	}

	public Long getEventCount() {
		return eventCount;
	}

	public void setEventCount(Long eventCount) {
		this.eventCount = eventCount;
	}

	public String getLatestEventId() {
		return latestEventId;
	}

	public void setLatestEventId(String latestEventId) {
		this.latestEventId = latestEventId;
	}

	public String getLatestEventTitle() {
		return latestEventTitle;
	}

	public void setLatestEventTitle(String latestEventTitle) {
		this.latestEventTitle = latestEventTitle;
	}
}
