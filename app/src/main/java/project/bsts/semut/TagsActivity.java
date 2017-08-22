package project.bsts.semut;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import project.bsts.semut.fragments.map.SubmitTagFragment;


public class TagsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        getSupportActionBar().hide();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //TagsFragment tagsFragment = new TagsFragment();
        //fragmentTransaction.replace(R.id.container, tagsFragment);
        SubmitTagFragment tagsFragment = new SubmitTagFragment();
        tagsFragment.setContext(this);
        fragmentTransaction.replace(R.id.container, tagsFragment);

        fragmentTransaction.commit();
    }
}
