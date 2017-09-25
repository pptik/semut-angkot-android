package project.bsts.semut;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import project.bsts.semut.fragments.map.CheckInFragment;
import project.bsts.semut.fragments.map.OnWaitingFragment;
import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.setup.Constants;


public class CheckInActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        preferencesManager = new PreferencesManager(this);

        getSupportActionBar().hide();
        FragmentManager fragmentManager = getFragmentManager();
        if(preferencesManager.getBoolean(Constants.PREFS_IS_WAITING)){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            OnWaitingFragment tagsFragment = new OnWaitingFragment();
            fragmentTransaction.replace(R.id.container, tagsFragment);
            fragmentTransaction.commit();
        }else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            CheckInFragment tagsFragment = new CheckInFragment();
            fragmentTransaction.replace(R.id.container, tagsFragment);
            fragmentTransaction.commit();
        }
    }
}
