package de.symeda.sormas.ui.travelentry;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.travelentry.TravelEntryDto;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.caze.CaseInfoLayout;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DetailSubComponentWrapper;
import de.symeda.sormas.ui.utils.LayoutUtil;
import de.symeda.sormas.ui.utils.ViewMode;

public class TravelEntryDataView extends AbstractTravelEntryView {

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/data";
	public static final String TRAVEL_ENTRY_LOC = "travelEntry";
	public static final String CASE_LOC = "case";

	private CommitDiscardWrapperComponent<TravelEntryDataForm> editComponent;

	public TravelEntryDataView() {
		super(VIEW_NAME);
	}

	@Override
	protected String getRootViewName() {
		return TravelEntriesView.VIEW_NAME;
	}

	@Override
	protected void initView(String params) {
		setHeightUndefined();

		String htmlLayout =
			LayoutUtil.fluidRow(LayoutUtil.fluidColumnLoc(8, 0, 12, 0, TRAVEL_ENTRY_LOC), LayoutUtil.fluidColumnLoc(4, 0, 6, 0, CASE_LOC));

		DetailSubComponentWrapper container = new DetailSubComponentWrapper(() -> editComponent);
		container.setWidth(100, Unit.PERCENTAGE);
		container.setMargin(true);
		setSubComponent(container);
		CustomLayout layout = new CustomLayout();
		layout.addStyleName(CssStyles.ROOT_COMPONENT);
		layout.setTemplateContents(htmlLayout);
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setHeightUndefined();
		container.addComponent(layout);

		TravelEntryDto travelEntryDto = FacadeProvider.getTravelEntryFacade().getByUuid(getReference().getUuid());

		editComponent = ControllerProvider.getTravelEntryController()
			.getTravelEntryDataEditComponent(getTravelEntryRef().getUuid(), ViewMode.NORMAL, travelEntryDto.isPseudonymized());
		editComponent.setMargin(false);
		editComponent.setWidth(100, Unit.PERCENTAGE);
		editComponent.getWrappedComponent().setWidth(100, Unit.PERCENTAGE);
		editComponent.addStyleName(CssStyles.MAIN_COMPONENT);
		layout.addComponent(editComponent, TRAVEL_ENTRY_LOC);

		CaseReferenceDto resultingCase = travelEntryDto.getResultingCase();
		if (resultingCase == null) {
			Button createCaseButton = ButtonHelper.createButton(
				Captions.travelEntryCreateCase,
				e -> ControllerProvider.getCaseController().createFromTravelEntry(travelEntryDto),
				ValoTheme.BUTTON_PRIMARY,
				CssStyles.VSPACE_2);
			layout.addComponent(createCaseButton, CASE_LOC);
		} else {
			layout.addComponent(createCaseInfoLayout(resultingCase.getUuid()), CASE_LOC);
		}

		setTravelEntryEditPermission(container);
	}

	private CaseInfoLayout createCaseInfoLayout(String caseUuid) {

		CaseDataDto caseDto = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
		CaseInfoLayout caseInfoLayout = new CaseInfoLayout(caseDto, true);
		caseInfoLayout.addStyleName(CssStyles.SIDE_COMPONENT);

		return caseInfoLayout;
	}
}
