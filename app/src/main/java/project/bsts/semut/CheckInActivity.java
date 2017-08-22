package project.bsts.semut;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import project.bsts.semut.fragments.map.CheckInFragment;



public class CheckInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        getSupportActionBar().hide();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CheckInFragment tagsFragment = new CheckInFragment();
        fragmentTransaction.replace(R.id.container, tagsFragment);
        fragmentTransaction.commit();
    }
}
