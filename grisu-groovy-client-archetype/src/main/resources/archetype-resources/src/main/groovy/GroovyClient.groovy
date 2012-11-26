package ${groupId}

import grisu.frontend.control.login.LoginManager
import grisu.frontend.model.job.JobObject
import grisu.jcommons.constants.Constants
import grisu.model.GrisuRegistryManager
import grisu.control.exceptions.JobSubmissionException
import grisu.control.exceptions.JobPropertiesException
import grisu.control.ServiceInterface
import grisu.frontend.view.cli.GrisuCliClient
import grisu.model.FileManager



class GroovyClient extends GrisuCliClient<ExampleCliParameters> {

	static void main(String[] args) throws Exception{
		// basic housekeeping
		LoginManager.initGrisuClient("${artifactId}");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		ExampleCliParameters params = new ExampleCliParameters();
		// create the client
		GroovyClient client = null;
		try {
			client = new GroovyClient(params, args);
		} catch(Exception e) {
			System.err.println("Could not start ${artifactId}: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		// finally:
		// execute the "run" method below
		client.run();

		// exit properly
		System.exit(0);
	}

	public GroovyClient(ExampleCliParameters params, String[] args) throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		String file = getCliParameters().getFile();

		System.out.println("File to use for the job: " + file);

		// all login stuff is implemented in the parent class
		System.out.println("Getting serviceinterface...");
		ServiceInterface si = null;
		try {
			si = getServiceInterface();
		} catch (Exception e) {
			System.err.println("Could not login: " + e.getLocalizedMessage());
			System.exit(1);
		}

		System.out.println("Creating job...");
		JobObject job = new JobObject(si);
		String filename = FileManager.getFilename(file);
		job.setCommandline("cat " + filename);
		job.addInputFileUrl(file);
		job.setWalltimeInSeconds(60);

		job.setTimestampJobname("cat_job");

		System.out.println("Set jobname to be: " + job.getJobname());

		try {
			System.out.println("Creating job on backend...");
			job.createJob();
		} catch (JobPropertiesException e) {
			System.err.println("Could not create job: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		try {
			System.out.println("Submitting job to the grid...");
			job.submitJob();
		} catch (JobSubmissionException e) {
			System.err.println("Could not submit job: "
					+ e.getLocalizedMessage());
			System.exit(1);
		} catch (InterruptedException e) {
			System.err.println("Jobsubmission interrupted: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		System.out.println("Job submission finished.");
		System.out.println("Job submitted to: "
				+ job.getJobProperty(Constants.SUBMISSION_SITE_KEY));

		System.out.println("Waiting for job to finish...");

		// for a realy workflow, don't check every 5 seconds since that would
		// put too much load on the backend/gateways
		job.waitForJobToFinish(5);

		System.out.println("Job finished with status: "
				+ job.getStatusString(false));

		System.out.println("Stdout: " + job.getStdOutContent());
		System.out.println("Stderr: " + job.getStdErrContent());

	}
}
