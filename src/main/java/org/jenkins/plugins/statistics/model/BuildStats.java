package org.jenkins.plugins.statistics.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hthakkallapally on 3/16/2015.
 */
public class BuildStats {

    private String ciUrl;

    private String jobName;

    private String fullJobName;

    private int number;

    private SlaveInfo slaveInfo;

    private Date startTime;

    private Date endTime;

    private String startedUserId;

    private String startedUserName;

    private String result;

    private long duration;

    private Map<String, String> parameters;

    private SCMInfo scmInfo;

    private long queueTime;

    public BuildStats(String ciUrl,
                      String jobName,
                      String fullJobName,
                      int number,
                      SlaveInfo slaveInfo,
                      Date startTime,
                      Date endTime,
                      String startedUserId,
                      String startedUserName,
                      String result,
                      long duration,
                      Map<String, String> parameters,
                      SCMInfo scmInfo,
                      long queueTime) {
        this.ciUrl = ciUrl;
        this.jobName = jobName;
        this.fullJobName = fullJobName;
        this.number = number;
        this.slaveInfo = slaveInfo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startedUserId = startedUserId;
        this.startedUserName = startedUserName;
        this.result = result;
        this.duration = duration;
        this.parameters = parameters;
        this.scmInfo = scmInfo;
        this.queueTime = queueTime;
    }

    public BuildStats() {
        this.ciUrl = "";
        this.jobName = "";
        this.fullJobName = "";
        this.number = 0;
        this.slaveInfo = new SlaveInfo();
        this.startTime = new Date();
        this.endTime = new Date();
        this.startedUserId = "";
        this.startedUserName = "";
        this.result = "";
        this.duration = 0;
        this.parameters = new HashMap<>();
        this.scmInfo = new SCMInfo();
        this.queueTime = 0;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getFullJobName() {
        return fullJobName;
    }

    public void setFullJobName(String fullJobName) {
        this.fullJobName = fullJobName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public SlaveInfo getSlaveInfo() {
        return slaveInfo;
    }

    public void setSlaveInfo(SlaveInfo slaveInfo) {
        this.slaveInfo = slaveInfo;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStartedUserId() {
        return startedUserId;
    }

    public void setStartedUserId(String startedUserId) {
        this.startedUserId = startedUserId;
    }

    public String getStartedUserName() {
        return startedUserName;
    }

    public void setStartedUserName(String startedUserName) {
        this.startedUserName = startedUserName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public SCMInfo getScmInfo() {
        return scmInfo;
    }

    public void setScmInfo(SCMInfo scmInfo) {
        this.scmInfo = scmInfo;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }
}