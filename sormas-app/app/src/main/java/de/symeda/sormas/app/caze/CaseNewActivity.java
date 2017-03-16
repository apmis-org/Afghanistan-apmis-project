package de.symeda.sormas.app.caze;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.caze.CaseDao;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.epidata.EpiData;
import de.symeda.sormas.app.backend.epidata.EpiDataDao;
import de.symeda.sormas.app.backend.hospitalization.Hospitalization;
import de.symeda.sormas.app.backend.hospitalization.HospitalizationDao;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.symptoms.Symptoms;
import de.symeda.sormas.app.backend.symptoms.SymptomsDao;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.component.SelectOrCreatePersonDialogBuilder;
import de.symeda.sormas.app.util.Callback;
import de.symeda.sormas.app.util.Consumer;
import de.symeda.sormas.app.util.DataUtils;


/**
 * Created by Stefan Szczesny on 21.07.2016.
 */
public class CaseNewActivity extends AppCompatActivity {


    private CaseNewTab caseNewTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sormas_root_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getText(R.string.headline_new_case) + " - " + ConfigProvider.getUser().getUserRole());
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        caseNewTab = new CaseNewTab();
        ft.add(R.id.fragment_frame, caseNewTab).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_action_bar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_action_help,false);
        menu.setGroupVisible(R.id.group_action_add,false);
        menu.setGroupVisible(R.id.group_action_save,true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                //Home/back button
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_save:
                try {
                    final Case caze = caseNewTab.getData();

                    boolean validData = true;

                    if (caze.getPerson().getFirstName() == null || caze.getPerson().getFirstName().isEmpty()) {
                        validData = false;
                        Toast.makeText(this, "Please enter a first name for the case person.", Toast.LENGTH_SHORT).show();
                    }

                    if (caze.getPerson().getLastName() == null || caze.getPerson().getLastName().isEmpty()) {
                        validData = false;
                        Toast.makeText(this, "Please enter a last name for the case person.", Toast.LENGTH_SHORT).show();
                    }

                    if (caze.getDisease() == null) {
                        validData = false;
                        Toast.makeText(this, "Please select a disease.", Toast.LENGTH_SHORT).show();
                    }

                    if (caze.getHealthFacility() == null) {
                        validData = false;
                        Toast.makeText(this, "Please select a health facility.", Toast.LENGTH_SHORT).show();
                    }

                    if (validData) {

                        List<Person> existingPersons = DatabaseHelper.getPersonDao().getAllByName(caze.getPerson().getFirstName(), caze.getPerson().getLastName());
                        if (existingPersons.size() > 0) {

                            AlertDialog.Builder dialogBuilder = new SelectOrCreatePersonDialogBuilder(this, caze.getPerson(), existingPersons, new Consumer() {
                                @Override
                                public void accept(Object parameter) {
                                    if (parameter instanceof Person) {
                                        try {
                                            caze.setPerson((Person) parameter);
                                            savePersonAndCase(caze);
                                        } catch (Exception e) {
                                            Toast.makeText(getApplicationContext(), "Error while saving the case. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            AlertDialog newPersonDialog = dialogBuilder.create();
                            newPersonDialog.show();

                        } else {
                            savePersonAndCase(caze);
                        }

//                      NavUtils.navigateUpFromSameTask(this);
                    }

                    return true;
                } catch (Exception e) {
                    Toast.makeText(this, "Error while saving the case. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

        }
        return super.onOptionsItemSelected(item);
    }

    private void showCaseEditView(Case caze) {
        // open case edit view
        Intent intent = new Intent(this, CaseEditActivity.class);
        intent.putExtra(CaseEditActivity.KEY_CASE_UUID, caze.getUuid());
        intent.putExtra(CaseEditActivity.KEY_PAGE, 1);
        startActivity(intent);
    }

    private void savePersonAndCase(final Case caze) throws IllegalAccessException, InstantiationException {

        // save the person
        DatabaseHelper.getPersonDao().save(caze.getPerson());

        caze.setCaseClassification(CaseClassification.POSSIBLE);
        caze.setInvestigationStatus(InvestigationStatus.PENDING);

        User user = ConfigProvider.getUser();
        caze.setReportingUser(user);
        if (user.getUserRole() == UserRole.SURVEILLANCE_OFFICER) {
            caze.setSurveillanceOfficer(user);
        } else if (user.getUserRole() == UserRole.INFORMANT) {
            caze.setSurveillanceOfficer(user.getAssociatedOfficer());
        }
        caze.setReportDate(new Date());

        DatabaseHelper.getSymptomsDao().save(caze.getSymptoms());
        DatabaseHelper.getHospitalizationDao().save(caze.getHospitalization());
        DatabaseHelper.getEpiDataDao().save(caze.getEpiData());

        CaseDao caseDao = DatabaseHelper.getCaseDao();
        caseDao.save(caze);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Saving case",
                "Cases are being synchronized...", true);

        SyncCasesTask.syncCases(new Callback() {
                                    @Override
                                    public void call() {
                                        Toast.makeText(CaseNewActivity.this, caze.getPerson().toString() + " saved", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        showCaseEditView(caze);
                                    }
                                });

    }


}
