package org.jenkins.plugins.statistics.listeners;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import jenkins.model.Jenkins;
import org.jenkins.plugins.statistics.model.BuildStats;
import org.jenkins.plugins.statistics.model.SCMInfo;
import org.jenkins.plugins.statistics.model.SlaveInfo;
import org.jenkins.plugins.statistics.util.Constants;
import org.jenkins.plugins.statistics.util.JenkinsCauses;
import org.jenkins.plugins.statistics.util.PropertyLoader;
import org.jenkins.plugins.statistics.util.RestClientUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener used for storing the Build information to the DB.
 *
 * @author hthakkallapally
 */
@Extension
public class RunStatsListener extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(RunStatsListener.class.getName());

    public RunStatsListener() {
        //Necessary for jenkins
    }

    public RunStatsListener(Class<Run<?, ?>> targetType) {
        super(targetType);
    }

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        try {
            if (!(run instanceof AbstractBuild)) {
                return;
            }
            final String buildResult = run.getResult() == null ?
                    "INPROGRESS" : run.getResult().toString();
            BuildStats build = new BuildStats();
            build.setStartTime(run.getTimestamp().getTime());
            build.setCiUrl(Jenkins.getInstance().getRootUrl());
            build.setJobName(run.getParent().getName());
            build.setFullJobName(run.getParent().getFullName());
            build.setNumber(run.getNumber());
            build.setResult(buildResult);
            build.setQueueTime(run.getExecutor() != null ?
                    run.getExecutor().getTimeSpentInQueue() : 0);
            addUserDetails(run, build);
            addSCMInfo(run, listener, build);
            addParameters(run, build);
            addSlaveInfo(run, build);
            RestClientUtil.postToService(getRestUrl(), build);
            LOGGER.log(Level.INFO, "Started build and its status is : " + buildResult +
                    " and start time is : " + run.getTimestamp().getTime());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to call API " + getRestUrl() +
                    " for build " + run.getDisplayName(), e);
        }
    }

    /**
     * Construct REST URL for build resource.
     *
     * @return API end point url.
     */
    private String getRestUrl() {
        return PropertyLoader.getBuildEndPoint();
    }

    /**
     * Add the slave info to build model.
     *
     * @param run
     * @param build
     */
    private void addSlaveInfo(Run<?, ?> run, BuildStats build) throws InterruptedException {
        SlaveInfo slaveInfo = new SlaveInfo();
        if (run.getExecutor() != null) {
            try {
                Node node = run.getExecutor().getOwner().getNode();
                slaveInfo.setSlaveName(node.getNodeName());
                slaveInfo.setVmName(node.toComputer().getHostName());
                slaveInfo.setLabel(node.getLabelString());
                slaveInfo.setRemoteFs(node.getRootPath().getName());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to retrieve hostname of Slave " +
                        " for " + run.getUrl(), e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Failed to retrieve hostname of Slave " +
                        " for " + run.getUrl(), e);
                throw e;
            }
            build.setSlaveInfo(slaveInfo);
        }
    }

    /**
     * Get all the parameters used in the build.
     *
     * @param run
     * @param build
     */
    private void addParameters(Run<?, ?> run, BuildStats build) {
        ParametersAction paramsAction = run.getAction(ParametersAction.class);
        if (paramsAction != null) {
            EnvVars env = new EnvVars();
            for (ParameterValue value : paramsAction.getParameters()) {
                if (!value.isSensitive()) {
                    value.buildEnvironment(run, env);
                }
            }
            build.setParameters(env);
        }
    }

    /**
     * Get the SCM info from the build and add it to model.
     *
     * @param run
     * @param listener
     * @param build
     */
    private void addSCMInfo(Run<?, ?> run, TaskListener listener,
                            BuildStats build) throws InterruptedException {
        EnvVars environment = null;
        SCMInfo scmInfo = new SCMInfo();
        try {
            environment = run.getEnvironment(listener);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve environment" +
                    " for " + run.getUrl(), e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve environment" +
                    " for " + run.getUrl(), e);
            throw e;
        }
        if (environment != null) {
            if (environment.get("GIT_URL") != null) {
                scmInfo.setUrl(environment.get("GIT_URL"));
            } else if (environment.get("SVN_URL") != null) {
                scmInfo.setUrl(environment.get("SVN_URL"));
            }
            if (environment.get("GIT_BRANCH") != null) {
                scmInfo.setBranch(environment.get("GIT_BRANCH"));
            }
            if (environment.get("GIT_COMMIT") != null) {
                scmInfo.setCommit(environment.get("GIT_COMMIT"));
            } else if (environment.get("SVN_REVISION") != null) {
                scmInfo.setUrl(environment.get("SVN_REVISION"));
            }
        }
        build.setScmInfo(scmInfo);
    }

    /**
     * Add user name and user id details to build model.
     *
     * @param run
     * @param build
     */
    private void addUserDetails(Run<?, ?> run, BuildStats build) {
        List<Cause> causes = run.getCauses();
        for (Cause cause : causes) {
            if (cause instanceof Cause.UserIdCause) {
                String userId = ((Cause.UserIdCause) cause).getUserId();
                userId = (userId == null || userId.isEmpty()) ? Constants.ANONYMOUS : userId;
                build.setStartedUserId(userId);
                String userName = ((Cause.UserIdCause) cause).getUserName();
                userName = (userName == null || userName.isEmpty()) ?
                        Constants.ANONYMOUS : userName;
                build.setStartedUserName(userName);
            } else if (cause instanceof Cause.UpstreamCause) {
                build.setStartedUserId(JenkinsCauses.UPSTREAM);
                build.setStartedUserName(Constants.SYSTEM);
            } else if (cause instanceof SCMTrigger.SCMTriggerCause) {
                build.setStartedUserId(JenkinsCauses.SCM);
                build.setStartedUserName(Constants.SYSTEM);
            } else if (cause instanceof TimerTrigger.TimerTriggerCause) {
                build.setStartedUserId(JenkinsCauses.TIMER);
                build.setStartedUserName(Constants.SYSTEM);
            } else {
                build.setStartedUserId(Constants.UNKNOWN);
                build.setStartedUserName(Constants.SYSTEM);
            }
        }
    }

    @Override
    /**
     * Update the build status and duration.
     */
    public void onFinalized(final Run<?, ?> run) {
        try {
            if (!(run instanceof AbstractBuild)) {
                return;
            }

            final String buildResult = run.getResult() == null ?
                    Constants.UNKNOWN : run.getResult().toString();
            BuildStats build = new BuildStats();
            build.setCiUrl(Jenkins.getInstance().getRootUrl());
            build.setJobName(run.getParent().getName());
            build.setFullJobName(run.getParent().getFullName());
            build.setNumber(run.getNumber());
            build.setResult(buildResult);
            // Capture duration in milliseconds.
            build.setDuration(run.getDuration());
            build.setEndTime(Calendar.getInstance().getTime());

            RestClientUtil.postToService(getRestUrl(), build);
            LOGGER.log(Level.INFO, run.getParent().getName() + " build is completed " +
                    "its status is : " + buildResult +
                    " at time : " + new Date());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to call API " + getRestUrl() +
                    " for build " + run.getDisplayName(), e);
        }
    }
}