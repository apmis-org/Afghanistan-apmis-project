/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.ui.sormastosormas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseIndexDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.ContactIndexDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.sormastosormas.SormasServerDescriptor;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestIndexDto;
import de.symeda.sormas.api.sormastosormas.validation.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrorMessage;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrors;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class SormasToSormasController {

	public SormasToSormasController() {
	}

	public void registerViews(Navigator navigator) {
		navigator.addView(ShareRequestsView.VIEW_NAME, ShareRequestsView.class);
	}

	public void shareCaseFromDetailsPage(CaseDataDto caze) {
		shareToSormasFromDetailPage(
			(options) -> FacadeProvider.getSormasToSormasCaseFacade().share(Collections.singletonList(caze.getUuid()), options),
			SormasToSormasOptionsForm.forCase(getCaseExcludedOrganizationIds(caze)));
	}

	public void shareSelectedCases(Collection<? extends CaseIndexDto> selectedRows, Runnable callback) {
		handleShareWithOptions((options) -> {
			FacadeProvider.getSormasToSormasCaseFacade()
				.share(selectedRows.stream().map(CaseIndexDto::getUuid).collect(Collectors.toList()), options);
		}, callback, SormasToSormasOptionsForm.forCase(null), new SormasToSormasOptionsDto());
	}

	public void shareContactFromDetailsPage(ContactDto contact) {
		shareToSormasFromDetailPage(
			(options) -> FacadeProvider.getSormasToSormasContactFacade().share(Collections.singletonList(contact.getUuid()), options),
			SormasToSormasOptionsForm.forContact(getContactExcludedOrganizationIds(contact)));
	}

	public void shareSelectedContacts(Collection<? extends ContactIndexDto> selectedRows, Runnable callback) {
		handleShareWithOptions((options) -> {
			FacadeProvider.getSormasToSormasContactFacade()
				.share(selectedRows.stream().map(ContactIndexDto::getUuid).collect(Collectors.toList()), options);
		}, callback, SormasToSormasOptionsForm.forContact(null), new SormasToSormasOptionsDto());
	}

	public void shareEventFromDetailsPage(EventDto event) {
		shareToSormasFromDetailPage(
			(options) -> FacadeProvider.getSormasToSormasEventFacade().share(Collections.singletonList(event.getUuid()), options),
			SormasToSormasOptionsForm.forEvent(getEventExcludedOrganizationIds(event)));
	}

	public void returnCase(CaseDataDto caze) {
		handleReturn(
			options -> FacadeProvider.getSormasToSormasCaseFacade().returnEntity(caze.getUuid(), options),
			SormasToSormasOptionsForm.forCase(null),
			caze.getSormasToSormasOriginInfo());
	}

	public void returnContact(ContactDto contact) {
		handleReturn(
			(options) -> FacadeProvider.getSormasToSormasContactFacade().returnEntity(contact.getUuid(), options),
			SormasToSormasOptionsForm.forContact(null),
			contact.getSormasToSormasOriginInfo());
	}

	public void returnEvent(EventDto event) {
		handleReturn(
			(options) -> FacadeProvider.getSormasToSormasEventFacade().returnEntity(event.getUuid(), options),
			SormasToSormasOptionsForm.forEvent(null),
			event.getSormasToSormasOriginInfo());
	}

	public void shareLabMessage(LabMessageDto labMessage, Runnable callback) {
		handleShareWithOptions((options) -> {
			FacadeProvider.getSormasToSormasLabMessageFacade().sendLabMessages(Collections.singletonList(labMessage.getUuid()), options);
		}, callback, SormasToSormasOptionsForm.withoutOptions(), new SormasToSormasOptionsDto());
	}

	public void rejectShareRequest(SormasToSormasShareRequestIndexDto request, Runnable callback) {
		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getString(Strings.headingRejectSormasToSormasShareRequest),
			new Label(I18nProperties.getString(Strings.confirmationRejectSormasToSormasShareRequest)),
			I18nProperties.getString(Strings.yes),
			I18nProperties.getString(Strings.no),
			640,
			confirmed -> {
				if (confirmed) {
					handleSormasToSormasRequest(() -> {
						FacadeProvider.getSormasToSormasFacade().rejectShareRequest(request.getDataType(), request.getUuid());
					}, callback);
				}
			});
	}

	public void acceptShareRequest(SormasToSormasShareRequestIndexDto request, Runnable callback) {
		handleSormasToSormasRequest(() -> {
			FacadeProvider.getSormasToSormasFacade().acceptShareRequest(request.getDataType(), request.getUuid());
		}, callback);
	}

	public void revokeShare(String shareInfoUuid, Runnable callback) {
		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getString(Strings.headingRevokeSormasToSormasShareRequest),
			new Label(I18nProperties.getString(Strings.confirmationRevokeSormasToSormasShareRequest)),
			I18nProperties.getString(Strings.yes),
			I18nProperties.getString(Strings.no),
			640,
			confirmed -> {
				if (confirmed) {
					handleSormasToSormasRequest(() -> {
						FacadeProvider.getSormasToSormasFacade().revokeShare(shareInfoUuid);
					}, callback);
				}
			});
	}

	private void shareToSormasFromDetailPage(
		HandleShareWithOptions handleShareWithOptions,
		SormasToSormasOptionsForm optionsForm) {
		handleShareWithOptions(handleShareWithOptions::handle, SormasUI::refreshView, optionsForm, new SormasToSormasOptionsDto());
	}

	private void handleShareWithOptions(
		HandleShareWithOptions handleShareWithOptions,
		Runnable callback,
		SormasToSormasOptionsForm optionsForm,
		SormasToSormasOptionsDto defaultOptions) {
		optionsForm.setValue(defaultOptions);

		CommitDiscardWrapperComponent<SormasToSormasOptionsForm> optionsCommitDiscard =
			new CommitDiscardWrapperComponent<>(optionsForm, optionsForm.getFieldGroup());
		optionsCommitDiscard.getCommitButton().setCaption(I18nProperties.getCaption(Captions.sormasToSormasShare));
		optionsCommitDiscard.setWidth(100, Sizeable.Unit.PERCENTAGE);

		Window optionsPopup = VaadinUiUtil.showPopupWindow(optionsCommitDiscard, I18nProperties.getCaption(Captions.sormasToSormasDialogTitle));

		optionsCommitDiscard.addCommitListener(() -> {
			SormasToSormasOptionsDto options = optionsForm.getValue();

			handleSormasToSormasRequest(() -> {
				handleShareWithOptions.handle(options);
			}, () -> {
				callback.run();
				optionsPopup.close();
			});
		});

		optionsCommitDiscard.addDiscardListener(optionsPopup::close);
	}

	private void handleSormasToSormasRequest(SormasToSormasRequest request, Runnable callback) {
		try {
			request.run();
			callback.run();
		} catch (SormasToSormasException ex) {
			if (ex.isWarning()) {
				VaadinUiUtil.showWarningPopup(ex.getMessage());
				callback.run();
			} else {
				Component messageComponent = buildShareErrorMessage(ex.getHumanMessage(), ex.getErrors());
				messageComponent.setWidth(100, Sizeable.Unit.PERCENTAGE);
				VaadinUiUtil
					.showPopupWindow(new VerticalLayout(messageComponent), I18nProperties.getCaption(Captions.sormasToSormasErrorDialogTitle));
			}
		} catch (SormasToSormasValidationException ex) {
			Component messageComponent = buildShareErrorMessage(ex.getMessage(), ex.getErrors());
			messageComponent.setWidth(100, Sizeable.Unit.PERCENTAGE);
			VaadinUiUtil.showPopupWindow(new VerticalLayout(messageComponent), I18nProperties.getCaption(Captions.sormasToSormasErrorDialogTitle));
		}
	}

	private void handleReturn(
		HandleShareWithOptions handleShareWithOptions,
		SormasToSormasOptionsForm optionsForm,
		SormasToSormasOriginInfoDto originInfo) {
		SormasToSormasOptionsDto defaultOptions = new SormasToSormasOptionsDto();
		defaultOptions.setOrganization(new SormasServerDescriptor(originInfo.getOrganizationId()));
		defaultOptions.setHandOverOwnership(true);
		defaultOptions.setWithAssociatedContacts(originInfo.isWithAssociatedContacts());
		defaultOptions.setWithSamples(originInfo.isWithSamples());
		defaultOptions.setWithEventParticipants(originInfo.isWithEventParticipants());

		optionsForm.disableAllOptions();

		handleShareWithOptions(handleShareWithOptions::handle, SormasUI::refreshView, optionsForm, defaultOptions);
	}

	private Component buildShareErrorMessage(String message, List<ValidationErrors> errors) {
		Label errorMessageLabel = new Label(message, ContentMode.HTML);

		if (errors == null || errors.size() == 0) {
			return errorMessageLabel;
		}

		VerticalLayout[] errorLayouts = errors.stream().map(e -> {
			Label groupLabel = new Label(e.getGroup().getHumanMessage());
			groupLabel.addStyleNames(CssStyles.LABEL_BOLD);

			VerticalLayout groupErrorsLayout = new VerticalLayout(formatSubGroupErrors(e));
			groupErrorsLayout.setMargin(false);
			groupErrorsLayout.setSpacing(false);
			groupErrorsLayout.setStyleName(CssStyles.HSPACE_LEFT_3);

			VerticalLayout layout = new VerticalLayout(groupLabel, groupErrorsLayout);
			layout.setMargin(false);
			layout.setSpacing(false);

			return layout;
		}).toArray(VerticalLayout[]::new);

		VerticalLayout errorsLayout = new VerticalLayout(errorMessageLabel);
		errorsLayout.addComponents(errorLayouts);
		errorsLayout.setMargin(false);
		errorsLayout.setSpacing(false);

		return errorsLayout;
	}

	private Component[] formatSubGroupErrors(ValidationErrors errors) {
		return errors.getSubGroups().stream().map(e -> {
			Label groupLabel = new Label(e.getHumanMessage() + ":");
			groupLabel.addStyleName(CssStyles.LABEL_BOLD);
			HorizontalLayout layout = new HorizontalLayout(
				groupLabel,
				new Label(
					String.join(
						", ",
						e.getMessages().stream().map(ValidationErrorMessage::getHumanMessage).collect(Collectors.toList()).toString())));
			layout.setMargin(false);

			return layout;
		}).toArray(Component[]::new);
	}

	private List<String> getCaseExcludedOrganizationIds(CaseDataDto caze) {
		return getExcludedOrganizationIds(caze.getSormasToSormasOriginInfo(), new SormasToSormasShareInfoCriteria().caze(caze.toReference()));
	}

	private List<String> getContactExcludedOrganizationIds(ContactDto contact) {
		return getExcludedOrganizationIds(
			contact.getSormasToSormasOriginInfo(),
			new SormasToSormasShareInfoCriteria().contact(contact.toReference()));
	}

	private List<String> getEventExcludedOrganizationIds(EventDto event) {
		return getExcludedOrganizationIds(event.getSormasToSormasOriginInfo(), new SormasToSormasShareInfoCriteria().event(event.toReference()));
	}

	private List<String> getExcludedOrganizationIds(SormasToSormasOriginInfoDto originInfo, SormasToSormasShareInfoCriteria criteria) {
		List<String> organizationIds = new ArrayList<>();

		if (originInfo != null) {
			organizationIds.add(originInfo.getOrganizationId());
		}

		List<SormasToSormasShareInfoDto> shares = FacadeProvider.getSormasToSormasShareInfoFacade()
			.getIndexList(criteria.requestStatuses(Arrays.asList(ShareRequestStatus.PENDING, ShareRequestStatus.ACCEPTED)), null, null);

		organizationIds.addAll(shares.stream().map(s -> s.getTargetDescriptor().getId()).collect(Collectors.toList()));

		return organizationIds;
	}

	public void showRequestDetails(SormasToSormasShareRequestIndexDto request) {
		SormasToSormasShareRequestDto shareRequest = FacadeProvider.getSormasToSormasShareRequestFacade().getShareRequestByUuid(request.getUuid());
		ShareRequestLayout shareRequestLayout = new ShareRequestLayout(shareRequest);
		shareRequestLayout.setWidth(900, Sizeable.Unit.PIXELS);
		shareRequestLayout.setMargin(true);

		VaadinUiUtil.showPopupWindow(shareRequestLayout, I18nProperties.getString(Strings.headingShareRequestDetails));
	}

	private interface HandleShareWithOptions {

		void handle(SormasToSormasOptionsDto options) throws SormasToSormasException;
	}

	private interface SormasToSormasRequest {

		void run() throws SormasToSormasException, SormasToSormasValidationException;
	}
}
