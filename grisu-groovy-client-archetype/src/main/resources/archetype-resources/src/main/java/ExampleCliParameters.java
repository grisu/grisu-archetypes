package ${groupId};

import grisu.frontend.view.cli.GrisuCliParameters;

import com.beust.jcommander.Parameter;

public class ExampleCliParameters extends GrisuCliParameters {

	@Parameter(names = { "-f", "--file" }, description = "the path to a file")
	private String file;

	public String getFile() {
		return file;
	}

}
