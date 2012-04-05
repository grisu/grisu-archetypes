package ${groupId}

import grisu.frontend.control.login.LoginManager
import grisu.frontend.model.job.JobObject
import grisu.jcommons.constants.Constants

LoginManager.initGrisuClient('$artifactId')

def si = LoginManager.loginCommandline('testbed')

def job = new JobObject(si)

job.setApplication(Constants.GENERIC_APPLICATION_NAME)

// this is just to show how to access Java from within this script
def echoString = ${groupId}.ExampleJavaClass.echoString

job.setCommandline('echo ' + echoString)

job.setTimestampJobname('groovy_job')

println 'Creating job...'
job.createJob('/none')
println 'Submitting job...'
job.submitJob()

println 'Waiting for job...'
job.waitForJobToFinish(5)

println 'Job finished with status: ' + job.getStatusString(false)

println 'Stdout: ' + job.getStdOutContent()
println 'Stderr: ' + job.getStdErrContent()

System.exit(0)
