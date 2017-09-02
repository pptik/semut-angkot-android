package project.bsts.semut.services;


import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class CheckStatusJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.i("job", "start job");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.i("job", "stop job");

        return true;
    }
}
