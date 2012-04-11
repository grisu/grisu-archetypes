package ${groupId}

import grisu.frontend.control.login.LoginManager
import grisu.frontend.model.job.JobObject
import grisu.jcommons.constants.Constants
import grisu.model.GrisuRegistryManager

class GroovyClient {

	static void main(String[] args) {
		LoginManager.initGrisuClient('$artifactId')


		def cli = new CliBuilder(usage:'$artifactId -g group [options] commandline')
		cli.b(longOpt:'backend', args:1, 'The backend (alias or url) to connect to (default: testbed)')
		cli.g(longOpt:'group', args:1, 'The group used to submit the job (required)')
		cli.f(longOpt: 'files', args:1, 'Comma seperated list of urls or paths to input files for this job')
		cli.j(longOpt: 'jobname', args:1, 'Jobname')
		
		def options = cli.parse(args)
		if (! options ) {
			cli.usage()
			System.exit(1)
		}
		// make sure a group is set
		if (! options.g) {
			println 'No group specified.'
			cli.usage()
			System.exit(1)
		}
		// make sure arguments exist
		if (! options.arguments() ) {
			println 'No commandline specified.'
			cli.usage()
			System.exit(1)
		}


		def commandline = options.arguments().join(' ')

		def backend = options.b
		if ( ! backend ) {
			backend = 'testbed'
		}
		
		def jobname = options.j
		if ( ! jobname ) {
			jobname = options.arguments()[0]+'_job'
		}


		def si = LoginManager.loginCommandline(backend)
		
		// check whether the specified group exists
		if (! si.getFqans().asSortedSet().contains(options.g) ) {
			println 'You are not a member of group "'+options.g+'".'
			System.exit(1)
		}

		def fm = GrisuRegistryManager.getDefault(si).getFileManager()
		// checking whether all input files exist
		def files = []
		if ( options.f ) {
			files = options.f.split(',')
			for ( def file : files ) {
				if ( ! fm.fileExists(file) ) {
					println 'Input file "'+file+'" does not exist'
					System.exit(1)
				}
			}
		}

		def job = new JobObject(si)

		job.setApplication(Constants.GENERIC_APPLICATION_NAME)

		// this is just to show how to access Java from within this script
		// doesn't make any sense in context of this script
		def echoString = ${groupId}.ExampleJavaClass.echoString

		job.setCommandline(commandline)

		job.setTimestampJobname(jobname)

		println 'Creating job "'+job.getJobname()+'"...'
		job.createJob(options.g)
		println 'Submitting job...'
		job.submitJob()

		println 'Waiting for job...'
		job.waitForJobToFinish(5)

		println 'Job finished with status: ' + job.getStatusString(false)

		println 'Stdout: ' + job.getStdOutContent()
		println 'Stderr: ' + job.getStdErrContent()

		System.exit(0)
	}
}
